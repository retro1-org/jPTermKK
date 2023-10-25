package jpterm;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Communicate with the Plato system via Java NIO
 * 
 * Some logic in handling of Plato words was copied from pterm
 * which is copyright Paul Koning
 * 
 * @author Ken Kachnowich Created on Apr 24, 2005
 * Version $Id$
 * License: See file license.txt for license details
 * 
 * Copyright: Ken Kachnowich, 2005 
 */
public class SocketWrapper extends Thread {

	private boolean running = false;

	private String hostName;

	private int hostPort;

	private Selector selector = null;

	private ByteBuffer buf = ByteBuffer.allocateDirect(2048);

	private SocketChannel socketChannel = null;

	// circular buffer of words read from network
	private int[] incomming = new int[8192];
	// circular buffer of words going to network
	private int[] outgoing = new int[256];
	
	/** if true we will limit words/sec going to reader */
	private boolean throttle = false;
	/** words/sec to limit reader to */
	private int readLimit = 120;
	/** current read count for reader (words read in current time slot) */
	private int currRead = 0;
	/** timestamp for half-life on currRead (half currRead every second) */
	private long readTime = 0;
	/** length of half-life time period (.25 second) */
	private long timePeriod = 100;

	private int inNextRead = 0;

	private int inNextWrite = 0;

	private int outNextRead = 0;

	private int outNextWrite = 0;

	public SocketWrapper() {
	}

	/**
	 * Create a socket channel to the host
	 * 
	 * @param host host address to connect to
	 * @param port port number to connect to
	 * @return true if connection established
	 */
	public boolean init(String host, int port) {
		if (null != host) {
			hostName = host;
		}
		if (port > 0) {
			hostPort = port;
		}
		
		readTime = System.currentTimeMillis() + timePeriod;
		
		return createConnection();
	}
	
	/**
	 * Set the words/sec throttle limit for reads.
	 * @param limit
	 */
	public void setReadLimit(int limit) {
	   double fct = timePeriod/1000.0;
	   // limit is words/second, convert to our time period
	   this.readLimit = (int)(limit * fct);
	   if (!this.throttle && this.readLimit > 0){
	      this.currRead = 0;
	      this.readTime = System.currentTimeMillis() + this.timePeriod;
	   }
	   this.throttle = (this.readLimit > 0);
	}

	public void run() {
		int keysAdded = 0;
		running = true;

		//	Wait for events
		while (running) {
			try {
				// Wait for an event
				keysAdded = selector.select(50);
			} catch (IOException e) {
				// Handle error with selector
				System.out.println("Selector error: " + e.getMessage());
				break;
			}

			if (keysAdded > 0) {
				// Get list of selection keys with pending events
				Iterator it = selector.selectedKeys().iterator();

				// Process each key one at a time
				while (it.hasNext()) {

					// Get the selection key
					SelectionKey selKey = (SelectionKey) it.next();

					// Remove it from the list to indicate that it is being
					// processed
					it.remove();

					try {
						processSelectionKey(selKey);
					} catch (IOException e) {
						// Handle error with channel and unregister
						selKey.cancel();
					}
				}
				//System.out.println("Processed " + keysAdded);
			}
			
			// anything to send?
			if (hasOutgoingWord()) {
				writeToChannel(getOutgoingWord());
			}
			
			// half-life the read throttle
			if (throttle && (System.currentTimeMillis() > readTime)){
			   readTime = System.currentTimeMillis() + timePeriod;
			   if (currRead > 0) {
			      synchronized(this) {
			         currRead >>= 1;
			         // let reader know he can read again
			      	 if (hasIncommingWord()){
			      	    notifyAll();
			      	 }
			      }
			   }
			}
		}
	}

	/**
	 * Got something in from the network so process it
	 * @param selKey
	 * @throws IOException
	 */
	public void processSelectionKey(SelectionKey selKey) throws IOException {
		// Get channel with connection request
		SocketChannel sChannel = (SocketChannel) selKey.channel();

			// this should never hit
		if (selKey.isValid() && sChannel.isConnectionPending()
				&& selKey.isConnectable()) {
			boolean success = sChannel.finishConnect();
			if (!success) {
				// An error occurred; handle it
				System.err.println("processSelectKey not successful");
				// Unregister the channel with this selector
				selKey.cancel();
			}
			System.out.println("Connected");
		}
		// we have something to read from the network
		if (selKey.isValid() && selKey.isReadable()) {
			readFromChannel(sChannel);
		}
		// channel writable and we have something to write
		if (selKey.isValid() && selKey.isWritable() && hasOutgoingWord()) {
			writeToChannel(getOutgoingWord());
		}
	}

	/**
	 * Have a word to go to the network 
	 * @param sChannel
	 */
	private void writeToChannel(int word) {
		// This is a composite word, send each piece recursivly
		if ((word >> 9) != 0) {
			writeToChannel(word >> 9);
			writeToChannel(word & 0777);
		}
		else {
			ByteBuffer buf = ByteBuffer.allocateDirect(3);
			try {
				buf.put((byte) (word >> 7));
				buf.put((byte) (0200 | word));
				buf.flip();
				int r = socketChannel.write(buf);
			//System.out.println("key to plato " + niuWord);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Read bytes from socket. If enough read to make a word (3) process bytes
	 * and put it on the incomming buffer
	 * 
	 * @param sChannel
	 */
	public void readFromChannel(SocketChannel sChannel) {

		byte b1 = 0;
		byte b2 = 0;
		byte b3 = 0;

		try {
			// Read bytes from socket
			int numBytesRead = sChannel.read(buf);
			//System.out.println("read " + numBytesRead + " bytes");

			if (numBytesRead == -1) {
				// No more bytes can be read from the channel
				socketChannel.close();
				System.out.println("Socket closed");
				// have enough to make a word
			} else if (numBytesRead >= 3) {
				// To read the bytes, flip the buffer
				buf.flip();

				byte[] bf = new byte[numBytesRead];
				buf.get(bf);
				buf.clear();

				//System.out.println("HEX: " + HexDump.dumpHexData(bf, 0,
				// numBytesRead));

				// go through the buffer and convert the bytes to words
				// add the word to the incomming circular buffer
				int i = 0;
				while (i < bf.length) {
					// not enough left to make a word
					if (i + 2 >= bf.length) {
						//System.out.println("bytes " + bf.length + " " + i);
						buf.put(bf, i, (bf.length - i));
						break;
					}

					b1 = bf[i++];
					if ((b1 & 0200) > 0) {
						System.out.println("Plato output out of sync byte 0");
						continue;
					}

					b2 = bf[i++];
					if ((b2 & 0300) != 0200) {
						System.out.println("Plato output out of sync byte 1");
						if ((b2 & 0200) == 0) {
							i--;
							continue;
						}
					}

					b3 = bf[i++];
					if ((b3 & 0300) != 0300) {
						System.out.println("Plato output out of sync byte 2");
						if ((b2 & 0200) == 0) {
							i--;
							continue;
						}
					}

					int wd = (b1 << 12) | ((b2 & 077) << 6) | (b3 & 077);

					// erase abort marker received, clear the incomming ring
					if (wd == 2) {
						//System.out.println("clearIncomming");
						clearIncomming();
					}
					
					// put word in the incomming ring
					putIncommingWord(wd);
					

					//System.out.println("NIU: " + nuiwd);
				}
			}
		} catch (IOException e) {
			// Connection may have been closed
			e.printStackTrace();
		}
	}

	/**
	 * Store a word in the incoming circular buffer
	 * 
	 * @param nui
	 */
	private synchronized void putIncommingWord(int word) {
		incomming[inNextWrite] = word;
		inNextWrite++;
		if (inNextWrite >= incomming.length)
			inNextWrite = 0;
		notifyAll();
	}

	/**
	 * Returns true if incoming word available to be read
	 * 
	 * @return
	 */
	public boolean hasIncommingWord() {
	   boolean _rtn = !(inNextRead == inNextWrite);
	   // lie to them if over throttle limit
	   if (throttle && currRead > readLimit){
	      _rtn = false;
	   }   
	   
	   return _rtn;
	}

	/**
	 * Remove next incoming word from ring and return it
	 * 
	 * @return
	 */
	public int getIncommingWord() {
		int _rtn = 0;

		synchronized (incomming) {
			_rtn = incomming[inNextRead];
			inNextRead++;
			if (inNextRead >= incomming.length)
				inNextRead = 0;
		}
		if (throttle) currRead++;
		return _rtn;
	}

	/**
	 * Wait until a word comes in from the network or we are interrupted
	 * @return word from network
	 */
	public synchronized int waitForIncommingWord() {
		
		while (!hasIncommingWord()){
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		return getIncommingWord();
	}
	
	/**
	 * Reset incomming ring buffer to empty state
	 *  
	 */
	private void clearIncomming() {
		synchronized (incomming) {
			inNextRead = 0;
			inNextWrite = 0;
		}
	}

	/**
	 * Add an outgoing word to the circular buffer
	 * @param nui
	 */
	public void putOutgoingWord(int word) {
		synchronized (outgoing) {
			outgoing[outNextWrite] = word;
			outNextWrite++;
			if (outNextWrite >= outgoing.length)
				outNextWrite = 0;
		}
		// wake up the selector
		selector.wakeup();
	}

	/**
	 * Return true if outgoing words in buffer
	 * @return
	 */
	public boolean hasOutgoingWord() {
		return !(outNextRead == outNextWrite);
	}

	/**
	 * Get the next outgoing word
	 * @return
	 */
	public int getOutgoingWord() {
		int _rtn = 0;

		synchronized (outgoing) {
			_rtn = outgoing[outNextRead];
			outNextRead++;
			if (outNextRead >= outgoing.length)
				outNextRead = 0;
		}
		return _rtn;
	}

	// Creates a non-blocking socket channel for the specified host name and
	// port.
	// connect() is called on the new channel before it is returned.
	public static SocketChannel createSocketChannel(String hostName, int port)
			throws IOException {
		// Create a non-blocking socket channel
		SocketChannel sChannel = SocketChannel.open();
		sChannel.configureBlocking(false);

		// Send a connection request to the server; this method is non-blocking
		sChannel.connect(new InetSocketAddress(hostName, port));
		return sChannel;
	}

	public boolean createConnection() {
	    boolean _rtn = false;
	    int cnt = 0;
		System.out.println("Create connection to " + hostName + ":" + hostPort);
		// Create a non-blocking socket and check for connections
		try {
			// Create the selector
			selector = Selector.open();

			// Create a non-blocking socket channel
			socketChannel = createSocketChannel(hostName, hostPort);

			//	Register the channel with selector, listening for all events
			SelectionKey selKey = socketChannel.register(selector,
					SelectionKey.OP_READ);

			while (!socketChannel.finishConnect() && cnt < 5000) {
			   try {
			      Thread.sleep(100);
			   } catch (InterruptedException e1) {
               // do nothing
			   }
			   cnt++;
			}
			if (socketChannel.isConnected())
			   _rtn = true;
		} catch (IOException e) {
		   _rtn = false;
			System.out.println("Connection issues: " + e.getMessage());
		}
		return _rtn;
	}

	public void closeConnection() {
		try {
			socketChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
