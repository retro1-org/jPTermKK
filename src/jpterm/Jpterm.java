package jpterm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import jpterm.plugins.PluginIntr;
import jpterm.plugins.avatar.Avatar;

/**
 * JPterm
 * 
 * A Java implementation of the pterm terminal emulator.
 * 
 * Parts of this program, especialy PlatoSpecific.java and parts of this class, were copied  
 * from the pterm and pterm221 programs, which are copyright Tom Hunter and Paul Koning.
 * 
 * @author Ken Kachnowich
 * @version $Id$
 * Created on Apr 23, 2005
 * License: See file license.txt for license details
 * 
 * Copyright: Ken Kachnowich, 2005
 */
public class Jpterm extends JFrame implements Runnable, KeyListener, ActionListener {

	private static final String PTERMVERSION = "Jpterm";

	private static final int TERMTYPE = 10;

	/** Defaults incase we lose or config file */
	private static final String DEFAULTHOST = "cyberserv.org";
	private static final int DEFAULTPORT = 5004;

	private static final int M2ADDR = 0x2340; // PPT start address for set 2

	private static final int M3ADDR = 0x2740; // PPT start address for set 3

	private Thread thread = null;
	/** handles all communication with the server */
	private SocketWrapper platoSock = null;
	/** Class to control configuration data */
	private JptermConfig jpConf = null;
	private String configFile;
	
	private boolean running = false;
	/** array of defined plugins */
	private PluginIntr[] plugins = null;

	// GUI variables
	private JPanel contentPane;
	/** Handles the display */
	private DrawingArea drawA = null;

	// Plato Word variables
	private int wc = 0;

	private String hostName = null;

	private int currentY = 0;

	private int currentX = 0;

	private int wemode = 0;

	private int[] plato_m23 = new int[128 * 8];

	private int mode = 0;

	private int memaddr = 0;

	private boolean uncover = false;

	private int margin = 0;

	private int currentCharset;

	private int modewords = 0;

	private int mode4start = 0;
	
	private boolean debug = false;

	public Jpterm(String confFile) {
	    this.configFile = confFile;
	    // read XML config file
	    jpConf = new JptermConfig();
	    jpConf.readConfig(configFile);
	    
	    windowSetup();
		platoSock = new SocketWrapper();
		platoSock.setReadLimit(jpConf.getThrottle());
		
		// keep looping through the connections until we connect
		boolean connected = false;
		int port = DEFAULTPORT;
		do {
		   hostName = jpConf.getNextConnection();
		   // see if a port# was specified
		   if (hostName.indexOf(":") > 0){
		      port = Integer.parseInt(hostName.substring(hostName.indexOf(":") + 1));
		      hostName = hostName.substring(0,hostName.indexOf(":"));
		   }
		   connected = platoSock.init(hostName, port);
		}while(!connected);
		
		platoSock.start();
		
		thread = new Thread(this, "JPterm");
		thread.start();
	}

	public static void main(String[] args) {
	    String confFile = "JPtermConfig.xml";
	    if (args.length > 0)
	       confFile = args[0];
		Jpterm myTerm = new Jpterm(confFile);
		myTerm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		myTerm.show();

	}

	private void windowSetup() {
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		this.setSize(getDisplaySize(jpConf.getCharSize()));
		this.setTitle("Jpterm");
		this.setFocusable(true);
		this.addKeyListener(this);
		this.setFocusTraversalKeysEnabled(false);
		
		JMenuBar menuBar = new JMenuBar();
		// stop the F10 key from poping the File menu
		menuBar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F10"), "none");
		
		JMenu fileMenu = new JMenu("File");
		JMenuItem increaseMenu = new JMenuItem("Bigger");
		increaseMenu.addActionListener(this);
		fileMenu.add(increaseMenu);
		JMenuItem decreaseMenu = new JMenuItem("Smaller");
		decreaseMenu.addActionListener(this);
		fileMenu.add(decreaseMenu);
		JMenuItem fgMenu = new JMenuItem("Foreground Color...");
		fgMenu.addActionListener(this);
		fileMenu.add(fgMenu);
		JMenuItem bgMenu = new JMenuItem("Background Color...");
		bgMenu.addActionListener(this);
		fileMenu.add(bgMenu);
		JMenuItem tMenu = new JMenuItem("Throttle...");
		tMenu.addActionListener(this);
		fileMenu.add(tMenu);
		JMenuItem saveMenu = new JMenuItem("Save");
		saveMenu.addActionListener(this);
		fileMenu.add(saveMenu);
		JMenuItem fileExitMenu = new JMenuItem("Exit");
		fileExitMenu.addActionListener(this);
		fileMenu.add(fileExitMenu);
		menuBar.add(fileMenu);
		
		JMenu pluginMenu = new JMenu("Plugins");
		
		// load plugins from the XML config file
		int np = jpConf.getNumberPlugins();
		
		plugins = new PluginIntr[np];
		for (int i = 0; i < np; i++){
		   try {
		      String piName = jpConf.getPlugin(i);
		      plugins[i] = (PluginIntr)Class.forName(piName).newInstance();
 		  	  plugins[i].init(this, jpConf.getPluginParameters(piName));
		   } catch (Exception e) {
		      plugins[i] = null;
		      e.printStackTrace();
        	}
		}

		// add plugins to Plugin menu display
		for (int i = 0; i < plugins.length; i++){
		   if (plugins[i] != null)
		      pluginMenu.add(new JMenuItem(plugins[i].getName()));
		}
		// add use as listener for plugin menu items
		for (int i = 0; i < pluginMenu.getItemCount(); i++) {
		   pluginMenu.getItem(i).addActionListener(this);
		}

		// these are used in debugging display code
		menuBar.add(pluginMenu);
		JMenu debugMenu = new JMenu("Debug");
		JMenuItem dbDspMenu = new JMenuItem("Debug Display");
		dbDspMenu.addActionListener(this);
		debugMenu.add(dbDspMenu);
		JMenuItem dbBlkEMenu = new JMenuItem("Debug Blk Erase");
		dbBlkEMenu.addActionListener(this);
		debugMenu.add(dbBlkEMenu);
		JMenuItem dbGridMenu = new JMenuItem("Toggle Grid");
		dbGridMenu.addActionListener(this);
		debugMenu.add(dbGridMenu);
		JMenuItem dbLineMenu = new JMenuItem("Debug Lines");
		dbLineMenu.addActionListener(this);
		debugMenu.add(dbLineMenu);
		JMenuItem dbCharsMenu = new JMenuItem("Dispay Chars");
		dbCharsMenu.addActionListener(this);
		debugMenu.add(dbCharsMenu);
		JMenuItem sndsMenu = new JMenuItem("Play Sounds");
		sndsMenu.addActionListener(this);
		debugMenu.add(sndsMenu);
		
		menuBar.add(debugMenu);
		this.setJMenuBar(menuBar);

		drawA = new DrawingArea();
		drawA.init(jpConf.getForegroundColor(), jpConf.getBackgroundColor(), jpConf.getCharSize());
		contentPane.add(drawA);
	}

	/**
	 * Display a color selector and tell the drawing area about the new foreground color
	 *
	 */
	private void changeForegroundColor(){
	   Color c = JColorChooser.showDialog(this, "Pick Foreground color", jpConf.getForegroundColor());
	   if (c != null){
	      drawA.setForegroundColor(c);
	      jpConf.setForegroundColor(c);
	      System.out.println("fg: " + c.getRGB());
	   }
	}
	
	/**
	 * Display a color selector and tell the drawing area about the new background color
	 *
	 */
	private void changeBackgroundColor(){
	   Color c = JColorChooser.showDialog(this, "Pick Background color", jpConf.getBackgroundColor());
	   if (c != null) {
	      drawA.setBackgroundColor(c);
	      jpConf.setBackgroundColor(c);
	   }
	}
	
	/**
	 * Allow user to set a display throttle on the speed (words/sec) that characters
	 * are drawn on the display.
	 *
	 */
	private void setReadThrottle() {
	   String dspThrottle = String.valueOf(jpConf.getThrottle());
	   String s = (String)JOptionPane.showInputDialog(
             this,
             "Set display throttle (characters per second), 0 for no throttle\n"
             + "200-400 seem to be good values to start with.",
             "Throttle Dialog",
             JOptionPane.PLAIN_MESSAGE,
             null,
             null,
             dspThrottle);

	   //If a string was returned, set the throttle
	   if ((s != null) && (s.length() > 0)) {
	      try {
	         int wcnt = Integer.parseInt(s);
	         platoSock.setReadLimit(wcnt);
	         jpConf.setThrottle(wcnt);
	      } catch(Exception ex){}
	   }
	}
	
	/**
	 * Return the display window size based on the width of a character
	 * @param w
	 */
	private Dimension getDisplaySize(int w){
	   return new Dimension(((w * 66) + 8), ((w*2*36) + 32));
	}
	
	public void run() {
		int word;
		System.out.println("Running Jpterm...");
		running = true;
		// wait for incoming words from the server and process them
		while (running) {
			word = platoSock.waitForIncommingWord();
			processWord(word);
		}
	}

	/*
	 * A key was pressed on the keyboard. Process it and see if we need 
	 * to pass it on to the server.
	 */
	public void keyPressed(KeyEvent e) {
		int toPlato = PlatoSpecific.platoKeypress(e);
		
		if (toPlato > 0) {
			platoSock.putOutgoingWord(toPlato);
			//System.out.println("c: "+ e.getKeyChar() + ":" + toPlato);
			// Pass char on to any active plugins
			for (int i = 0; i < plugins.length; i++) {
			   if (plugins[i] != null && plugins[i].isRunning()){
			      plugins[i].charToPlato(toPlato);
			   }
			}
		}
	}

	/*
	 * part of keylistener interface
	 */
	public void keyReleased(KeyEvent e) {
		// not implemented
	}

	/*
	 * A key was typed (pressed and released). Process it and see if we need 
	 * to pass it on to the server.
	 */
	public void keyTyped(KeyEvent e) {
		int platoKey = PlatoSpecific.platoKeyType(e);

		if (platoKey >= 0) {
		   System.out.println("c: " + e.getKeyChar() + ":" + platoKey);
			platoSock.putOutgoingWord(platoKey);
            // Pass KeyEvent on to any active plugins
			for (int i = 0; i < plugins.length; i++) {
			   if (plugins[i] != null && plugins[i].isRunning()){
			      plugins[i].charToPlato(platoKey);
			   }
			}
		}
		// if they hit alt key then pass this info on to any plugins
		else if (e.isAltDown()) {
		   // Pass KeyEvent on to any active plugins
		   for (int i = 0; i < plugins.length; i++) {
		      if (plugins[i] != null && plugins[i].isRunning()){
		         plugins[i].altKeyTyped(e.getKeyChar());
		      }
		   }
		}
	}
	
	/**
	 * Allows a plugin process to send a key to the server.
	 * @param key
	 */
	public void putChar(int key){
	   platoSock.putOutgoingWord(key);
	}
	
	private void doDebug(String msg){
		if(debug) System.out.println(msg);
	}

   /* 
    * Handle GUI events from menus
    */
   public void actionPerformed(ActionEvent e) {
      String action = e.getActionCommand();
   
      if (action.equals("Exit")) {
         platoSock.closeConnection();
         System.exit(0);
      }
      else if (action.equals("Save")) {
         jpConf.save();
      }
      else if (action.equals("Bigger")){
         int s = jpConf.getCharSize();
         s++;
         this.setSize(getDisplaySize(s));
         drawA.setCharSize(s);
         jpConf.setCharSize(s);
         this.validate();
      }
      else if (action.equals("Smaller")){
         int s = jpConf.getCharSize();
         if (s > 1){
            s--;
            this.setSize(getDisplaySize(s));
            drawA.setCharSize(s);
            jpConf.setCharSize(s);
         }
      }
      else if (action.equals("Foreground Color...")){
         changeForegroundColor();
      }
      else if (action.equals("Background Color...")){
         changeBackgroundColor();
      }
      else if (action.equals("Throttle...")){
         setReadThrottle();
      }
      else if (action.equals("Debug Display")){
         drawA.toggleDebug();
      }
      else if (action.equals("Debug Blk Erase")){
         drawA.debugBlkErase();
      }
      else if (action.equals("Toggle Grid")){
         drawA.gridToggle();
      }
      else if (action.equals("Debug Lines")){
         drawA.debugLines();
      }
      else if (action.equals("Dispay Chars")){
         drawA.displayChar();
      }
      else if (action.equals("Play Sounds")){
         ((Avatar)plugins[0]).playAllSounds();
      }
      // check if its a plugin name
      else {
         for (int i = 0; i < plugins.length; i++){
            if (action.equals(plugins[i].getName())){
               if (plugins[i].isRunning()){
                  plugins[i].shutdown();
                  // update menu display
               }
               else {
                  plugins[i].start();
                  // update menu display
               }
            }
         }
      }
   }
   
	/** *******************************************************************************************
	 * We received a word from the server. Since this is a terminal emulator
	 * deal with all the wierd things it may do. 
	 * 
	 * This method and those that follow were mostly copied from pterm and pterm221
	 * which is copyright Paul Koning (see header comments for details).
	 * 
	 * @param d
	 */
	public void processWord(int d) {

		String name = null;
		String msg = "";

		if ((d & 01700000) == 0) {
			// NOP command...
			//doDebug("NOP command");
			if ((d & 1) > 0) {
				wc = (wc + 1) & 0177;
			}
		} else {
			wc = (wc + 1) & 0177;
		}

		if ((d & 01000000) > 0) {
			//System.out.println("mode command " + mode + " called");
			modewords++;
			switch (mode) {
			case 0:
				mode0(d);
				break;
			case 1:
				mode1(d);
				break;
			case 2:
				mode2(d);
				break;
			case 3:
				mode3(d);
				break;
			case 4:
				mode4(d);
				break;
			case 5:
				mode5(d);
				break;
			case 6:
				mode6(d);
				break;
			case 7:
				mode7(d);
				break;
			default:
				System.err.println("Unknown mode " + mode);
			}
		} else {
			switch ((d >> 15) & 7) {
			case 0: // nop
				if ((d & 077000) == 042000) {
					// Special code to tell pterm the station number
					d &= 0777;
					if (hostName != null) {
						name = "Pterm " + PTERMVERSION + ": station "
								+ (d >> 5) + "-" + (d & 31) + hostName;
					} else {
						name = "Pterm " + PTERMVERSION + ": station "
								+ (d >> 5) + "-" + (d & 31);
					}
					//doDebug("NAME: " + name);
				}
				break;

			case 1: // load mode
				modewords = 0;
				if ((d & 020000) != 0) {
					// load wc bit is set
					wc = (d >> 6) & 0177;
				}

				wemode = (d >> 1) & 3;
				mode = (d >> 3) & 7;

				if ((d & 1) > 0) {
					// full screen erase
					//doDebug("Fullscreen erase");
					drawA.fullScreenErase();
				}
				// bit 15 set is DISable
				//ptermTouchPanel ((d & 0100000) == 0);
				//doDebug("load mode " + mode + " inhibit "
				//		+ ((d >> 15) &1) + " wemode " + wemode + " screen "
				//		+ (d & 1));
				break;

			case 2: // load coordinate
				boolean isY = ((d & 01000) > 0);
				if (isY)
					msg = "Y " + (d & 0777);
				else
					msg = "X " + (d & 0777);
				// add or subtract from current coordinate
				if ((d & 04000) > 0) {
					// sub
					if ((d & 02000) > 0) {
						if (isY)
							currentY -= (d & 0777);
						else
							currentX -= (d & 0777);
					}
					// add
					else {
						if (isY)
							currentY += (d & 0777);
						else
							currentX += (d & 0777);
					}
				} else {
					if (isY) {
						currentY = d & 0777;
					} else {
						currentX = d & 0777;
					}
				}
				
				if ((d & 010000) > 0){
					msg += " margin";
					if (isY)
						margin = currentY;
					else
						margin = currentX;
				}
				//doDebug("load coord " + msg);
				break;
			case 3: // echo
				// 160 is terminal type query
				if ((d & 0177) == 0160) {
					//doDebug("load echo termtype " + TERMTYPE);
					d = 0160 + TERMTYPE;
				}
				else if ((d & 01777) == 0x7b){
					System.out.println("beep!");
				}
				else if ((d & 0177) == 0x7d){
					// hex 7d is report memory
					d = memaddr;
					//doDebug("report MAR " + memaddr);
				}
				else {
				    //doDebug("load echo " + (d & 0177));
				}
				
				platoSock.putOutgoingWord((d & 0177) + 0200);
				break;

			case 4: // load address
				memaddr = d & 077777;
				//doDebug("load address " + memaddr);
				break;
				
			case 5:
				switch ((d >> 10) & 037) {
	            case 1: // Touch panel control ?
	                //doDebug("ssf touch " + d);
	                //m_canvas->ptermTouchPanel ((d & 040) != 0);
	                break;
	            default:
	                //doDebug("ssf " + d);
	                break;  // ignore
	            }
	            break;

	        case 6:
	        case 7:
	            d &= 0177777;
	            //doDebug("Ext " + d);
	            // Take no other action here -- it's been done already
	            // when the word was fetched
	            break;			

			default: // ignore
				System.out.println("ignored command word " + d);
				break;
			}
		}

	}

	/**
	 * Draw a point. 
	 * 
	 * Method mostly copied from pterm/pterm221.
	 * 
	 * @param d
	 */
	private void mode0(int d) {
		int x, y;

		x = (d >> 9) & 0777;
		y = d & 0777;
		drawA.drawPoint(x, y);
		currentX = x;
		currentY = y;
	}

	/**
	 * Draw a line
	 * 
	 * Mostly copied from pterm/pterm221.
	 * 
	 * @param d
	 */
	private void mode1(int d) {
		int x, y;

		x = (d >> 9) & 0777;
		y = d & 0777;
		drawA.drawLine(currentX, currentY, x, y, wemode);
		currentX = x;
		currentY = y;
	}

	/**
	 * Get a character definition from Plato. 
	 * 
	 * Mostly copied from pterm/pterm221.
	 * 
	 * @param d
	 */
	private void mode2(int d) {
		int ch, chaddr;

		// memaddr is a PPT RAM address; convert it to a character
		// memory address
		chaddr = memaddr - M2ADDR;
		//doDebug("memaddr " + memaddr + " chaddr " + chaddr);
		if (chaddr < 0 || chaddr > 127 * 16) {
			//System.out.println("memaddr outside character memory range");
			return;
		}
		chaddr /= 2;
		if (((d >> 16) & 3) == 0) {
			// load data
			//doDebug.out.println("character memdata " + (d & 0xffff) + " to " + chaddr);
			plato_m23[chaddr] = d & 0xffff;
			if ((++chaddr & 7) == 0) {
				// character is done -- load it to display
				ch = (chaddr / 8) - 1;
				drawA.loadPlatoChar(plato_m23, (chaddr - 8), (2 + ch/64), (ch%64));
			}
		}
		memaddr += 2;
	}

	/**
	 * Process Mode 3 data word
	 * 
	 * Mostly copied from pterm/pterm221.
	 * 
	 * @param d
	 */
	private void mode3(int d) {
		plotChar(d >> 12);
		plotChar(d >> 6);
		plotChar(d);
	}

	/**
	 * Block erase.  
	 * 
	 * Mostly copied from pterm/pterm221.
	 * 
	 * @param d
	 */
	private void mode4(int d) {
		int x1, y1, x2, y2;

		if ((modewords & 1) > 0) {
			mode4start = d;
			return;
		}
		x1 = (mode4start >> 9) & 0777;
		y1 = mode4start & 0777;
		x2 = (d >> 9) & 0777;
		y2 = d & 0777;

		drawA.blockErase(x1, y1, x2, y2, wemode);
		currentX = x1;
		currentY = y1 - 15;
	}

	private void mode5(int d) {
		System.out.println("Enter mode 5");
	}

	private void mode6(int d) {
		System.out.println("Enter mode 6");
	}

	private void mode7(int d) {
		System.out.println("Enter mode 7");
	}

	/**
	 * Look for some special terminal characters, plot other to display.
	 * 
	 *  Mostly copied from pterm/pterm221.
	 * 
	 * @param c
	 */
	private void plotChar(int c) {
		c &= 077;
		if (c == 077) {
			uncover = true;
			return;
		}
		if (uncover) {
			uncover = false;
			switch (c) {
			case 010: // backspace
				currentX = (currentX - 8) & 0777;
				break;
			case 011: // tab
				currentX = (currentX + 8) & 0777;
				break;
			case 012: // linefeed
				currentY = (currentY - 16) & 0777;
				break;
			case 013: // vertical tab
				currentY = (currentY + 16) & 0777;
				break;
			case 014: // form feed
				currentX = 0;
				currentY = 496;
				break;
			case 015: // carriage return
				currentX = margin;
				currentY = (currentY - 16) & 0777;
				break;
			case 016: // superscript
				currentY = (currentY + 5) & 0777;
				break;
			case 017: // subscript
				currentY = (currentY - 5) & 0777;
				break;
			case 020: // select M0
			case 021: // select M1
			case 022: // select M2
			case 023: // select M3
			case 024: // select M4
			case 025: // select M5
			case 026: // select M6
			case 027: // select M7
				currentCharset = c - 020;
				break;
			default:
				break;
			}
		} else {
			drawA.drawChar(currentX, currentY, currentCharset, c, wemode);
			
			// Pass char on to any active plugins
			for (int i = 0; i < plugins.length; i++) {
			   if (plugins[i] != null && plugins[i].isRunning()){
			      plugins[i].charFromPlato(currentX,  currentY, currentCharset, c);
			   }
			}
			
			currentX = (currentX + 8) & 0777;
		}
	}
	
}
