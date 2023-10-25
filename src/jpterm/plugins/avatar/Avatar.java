package jpterm.plugins.avatar;

import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import jpterm.Jpterm;
import jpterm.PlatoSpecific;
import jpterm.plugins.PluginIntr;

/*
 * Avatar
 * 
 * This is the avatar plugin for JPterm. It will display encounter and chat
 * lines in seperate scrolling windows. Also has an autospace feature and sound player.
 * 
 * @author Ken Kachnowich Created on May 22, 2005
 * Version $Id$
 * License: See file license.txt for license details
 * 
 * Copyright: Ken Kachnowich 2005 
 */
public class Avatar implements PluginIntr, Runnable {
   private static final String name = "Avatar";

   	// Plato avatar special character #s
   private static final int BOX = 126;         // ok
   private static final int SNAKE = 1134;      // ok
   private static final int BEAR = 2457;       // ok
   private static final int UNDEAD = 441;      // ok
   private static final int DEMON = 49;        // ok
   private static final int THIEF = 693;       // ok
   private static final int DRAGON = 43;       // ok
   private static final int MONKEYMAN = 1638;  // ok
   private static final int GIANT = 945;       // ok
   private static final int BEATLE = 1260;     // ok
   private static final int MERMAN = 3591;     // ok
   private static final int SLIME = 3024;      // ok
   private static final int ELEMENTAL = 1512;  // ok
   private static final int WARRIOR = 53;      // ok
   private static final int ENCHANTER = 59;    // ok
   private static final int GARGOYLE = 2961;   // ok
   // used by the tracker server/client code
   private static final int AUTOFIGHT = 102;
   private static final int AUTOSPELL = 43;
   private static final int SPELL0 = 32;
   private static final int SPELL1 = 33;
   private static final int SPELL2 = 34;
   private static final int SPELL3 = 35;
   private static final int SPELL4 = 36;
   private static final int SPELL5 = 37;
   private static final int SPELL6 = 38;
   private static final int SPELL7 = 39;
   private static final int SPELL8 = 40;
   private static final int SPELL9 = 41;
   
   private Thread thread;
   // circular arrays for saving character info
   private int[] chars = new int[1024];
   private int[] xloc = new int[1024];
   private int[] yloc = new int[1024];
   private int[] cset = new int[1024];
   private int nextRead = 0;
   private int nextWrite = 0;
   
   private boolean running = false;

   private Jpterm parent = null;
   /** save current line */
   private char[] currentLine = new char[70];
   /** row of current line */
   private int currentY = -1;
   /** combat list is up */ 
   private boolean encounterOn = false;
   /** chat list is up */
   private boolean chatOn = false;
   /** true if text in the currentLine buffer */
   protected boolean haveText = false;
   /** true if a box icon char came to status lines */
   private boolean boxLine = false;
   /** Helper class to display chat messages */
   private AvatarDisplayList chatList = null;
   /** Helper class to display combat messages */
   private AvatarDisplayList encounterList = null;
   /** Helper class to handle the auto space */
   private AutoSpace spaceSender = null;
   /** Helper class to play sounds */
   private SoundPlayer soundPlayer = null;
   /** if true sounds are on */
   private boolean soundOn = false;
   /** Socket server when we act as the tracker host */
   private TrackerServer trackerServer = null;
   /** Socket to get commands from pterm we are tracking */
   private TrackerClient trackerClient = null;

   public Avatar() {
   }

   public void init(Jpterm parent, HashMap params) {
      this.parent = parent;
      chatList = new AvatarDisplayList("Chat", this);
      encounterList = new AvatarDisplayList("Encounter", this);
      
      // init the sounds
      if (params != null){
        soundPlayer = new SoundPlayer(params);
        soundOn = true;
      }
   }
   
   /* 
    * Process characters from Plato
    */
   public void run() {
      while (running){
         // have a character so process it
         if (nextRead != nextWrite){
            processPlatoChar();
         }
         else {
            // wait 1 second for a character from Plato
            synchronized(chars){
               try {
                  chars.wait(1000);
               } catch (InterruptedException e) {}
               
               // time out so complete the line
               if (nextRead == nextWrite && haveText){
                  completeLine();
               }
            }
         }
      }
   }
   
   /**
    * Start the avatar actions, popup displays
    */
   public void start() {
      Point p = parent.getLocation();
      // popup the display lists next to jpterm window
      chatOn = true;
      chatList.setLocation((p.x + parent.getWidth()), (p.y + 260));
      chatList.setVisible(true);
      
      encounterOn = true;
      encounterList.setLocation((p.x + parent.getWidth()), p.y);
      encounterList.setVisible(true);
      
      // set the input line to all spaces
      clearLine();
      
      // start the thread processing characters
      this.running = true;
      nextRead = nextWrite = 0;
      thread = new Thread(this, "Avatar");
      thread.start();
   }

   /**
    * Stop the avatar actions, hide the displays
    */
   public void shutdown() {
      running = false;
      thread.interrupt();
      thread = null;
      
      chatList.setVisible(false);
      encounterList.setVisible(false);
      chatOn = false;
      encounterOn = false;
      
      if (trackerServer != null){
         trackerServer.shutdown();
         trackerServer = null;
      }
   }

   public String getName() {
      return name;
   }

   public boolean isRunning() {
      return this.running;
   }
   
   /**
    * Debug method to play all sounds
    */
   public void playAllSounds(){
      if (soundPlayer != null)
         soundPlayer.playAllSounds();
   }
   
   /**
    * Toggle the playing of sounds on/off
    */
   public void toggleSound(){
      soundOn = !soundOn;
   }

   /**
    * Got a character from Plato. Store info in circular queues
    * 
    * @param x x coord of character
    * @param y coord of character
    * @param charSet character set it  is in
    * @param c character number
    */
   public void charFromPlato(int x, int y, int charSet, int c) {
      synchronized(chars){
         chars[nextWrite] = c;
         xloc[nextWrite]  = x;
         yloc[nextWrite]  = y;
         cset[nextWrite]  = charSet;
         nextWrite++;
         if (nextWrite >= chars.length){
            nextWrite = 0;
         }   
         chars.notifyAll();
      }
   }
   
   /**
    * Process a char from Plato. See if its in the status or combat 
    * areas.
    *
    */
   public void processPlatoChar() {
      int x;
      int y;
      int charSet;
      int c;
      
      // get next char info from circular queues
      synchronized(chars){
         x = xloc[nextRead];
         y = yloc[nextRead];
         charSet = cset[nextRead];
         c = chars[nextRead];
         nextRead++;
         if (nextRead >= chars.length)
            nextRead = 0;
      }
      
      // convert coords to row and column
      x = x/8;
      y = (512 - y)/16;
      
      // Plato keeps sending me a space at 32,63 for some odd reason
      if (y == 32 && x == 63 && c == 45)
         return;
      
      // new row, so deal with the old one
      if (currentY != y) {
         if (haveText)
            completeLine();
         currentY = y;
      }
      
      // if its in the encounter or status area add it to the current line
      if ((chatOn && isStatusLine(y)) || (encounterOn && isEncounterLine(y))) {
         // one of ROM based characters, add to line
         if (charSet <= 1) {
            currentLine[x] = PlatoSpecific.romChar[charSet][c];
            if(!haveText){
               haveText = true;
            }
         // a avatar defined character   
         } else if (charSet <= 3) {
            // make avatar chars one big array space
            if (charSet == 3) c *= 63;

            // got an item from a box!
            if (isStatusLine(y) && c == BOX) {
               boxLine = true;
               currentLine[0] = ' ';
            }
            
            // Play a sound?
            if (soundOn && soundPlayer != null) {
               switch (c){
               case BOX:
                  if (isStatusLine(y))
                     soundPlayer.playSound("Box");
                  break;
               case SNAKE:
                  soundPlayer.playSound("Snake");
                  break;
               case THIEF:
                  soundPlayer.playSound("Thief");
                  break;
               case UNDEAD:
                  soundPlayer.playSound("Undead");
                  break;
               case DRAGON:
                  soundPlayer.playSound("Dragon");
                  break;
               case MONKEYMAN:
                  soundPlayer.playSound("Monkeyman");
                  break;
               case BEAR:
                  soundPlayer.playSound("Bear");
                  break;
               case DEMON:
                  soundPlayer.playSound("Demon");
                  break;
               case GIANT:
                  soundPlayer.playSound("Giant");
                  break;
               case BEATLE:
                  soundPlayer.playSound("Beatle");
                  break;
               case MERMAN:
                  soundPlayer.playSound("Merman");
                  break;
               case SLIME:
                  soundPlayer.playSound("Slime");
                  break;
               case ELEMENTAL:
                  soundPlayer.playSound("Elemental");
                  break;
               case WARRIOR:
                  soundPlayer.playSound("Warrior");
                  break;    
               case ENCHANTER:
                  soundPlayer.playSound("Enchanter");
                  break;
               case GARGOYLE:
                  soundPlayer.playSound("Gargoyle");
                  break;    
               }   
            }
         }
      }
   }
   
   /**
    * Clear out the current line
    */
   private void clearLine(){
      Arrays.fill(currentLine,' ');
   }

   /**
    * Character user sent to Plato
    * 
    * @param c
    */
   public void charToPlato(int c) {
      // turn off auto space if on
      if (spaceSender != null) {
         stopAutoSpace();
      }
   }
   
   /**
    * We either recived a command from the tracker server or are the server
    * Send the proper action command(s) on to Plato
    * @param c
    */
   public void charFromServer(char c) {
      switch(c) {
      // stop autospace and enter autofight
      case 'f':
         stopAutoSpace();
         parent.putChar(AUTOFIGHT);
         break;
      // toggle autospace on/off   
      case 's':
         if (spaceSender == null)
            startAutoSpace();
         else
            stopAutoSpace();
         break;
      // stop autospace and enter autospell
      case 'g':
         stopAutoSpace();
         parent.putChar(AUTOSPELL);
         break;
      // cast spell 0
      case '<':
      case ',':   
         stopAutoSpace();
         parent.putChar(SPELL0);
         break;
      // cast spell 1
      case '>':
      case '.':
         stopAutoSpace();
         parent.putChar(SPELL1);
         break;
      // cast spell 2
      case '[':
         stopAutoSpace();
         parent.putChar(SPELL2);
         break;
      // cast spell 3
      case ']':
         stopAutoSpace();
         parent.putChar(SPELL3);
         break;
      // cast spell 4
      case '$':
      case '4':
         stopAutoSpace();
         parent.putChar(SPELL4);
         break;
      // cast spell 5
      case '%':
      case '5':
         stopAutoSpace();
         parent.putChar(SPELL5);
         break;
      // cast spell 6
      case '_':
      case '-':
         stopAutoSpace();
         parent.putChar(SPELL6);
         break;
      // cast spell 7
      case '\'':
         stopAutoSpace();
         parent.putChar(SPELL7);
         break;
      // cast spell 8
      case '*':
      case '8':
         stopAutoSpace();
         parent.putChar(SPELL8);
         break;
      // cast spell 9
      case '(':
      case '9':
         stopAutoSpace();
         parent.putChar(SPELL9);
         break;
      }
   }
   
   /**
    * A ALT-character was typed. Deside if we need to send commands to any 
    * listening tracers and also back to plato
    */
   public void altKeyTyped(char c) {
      // Function key is for special Tracked fighting
      // f  send autofight to plato and autofight to tracers
      // s  send autospace toggle to tracers   
      // g  send autofight to plato and autospell to tracers
      // 0  send autofight to plato and spell0 then autofight to tracers
      // 1  send autofight to plato and spell1 then autofight to tracers 
      if (trackerServer != null) {
         trackerServer.putCommand(c);
      }
      // we enter autofight for these commands
      if (c == 'f' || c == 'g' || c == '0' || c == '1'){
         charFromServer('f');
      }
   }

   /**
    * We have a complete line from Plato
    *  
    */
   private void completeLine() {
      
      String ln = new String(currentLine).trim();
      // row falls in the status lines
      if (isStatusLine(currentY)) {
         // A chat message of some sort
         if (chatOn && (ln.indexOf("*A") >= 0 || ln.indexOf("*E") >= 0
               || ln.indexOf("*P") >= 0)) {
            chatList.addLine(ln.substring(1));
            //System.out.println("CHAT:" + currentLine.toString());
         }
         // someone stole something or we got an item from a box
         // so put it in the encounter list
         else if (encounterOn && (ln.indexOf("Thanks for the ") >= 0 || boxLine)) {
            encounterList.addLine(ln);
         }
      }
      // row falls in the combat lines
      else if (encounterOn && isEncounterLine(currentY)) {
         // we entered the city so stop logging encounter messages
         if (ln.indexOf("users") >= 0) {
            encounterOn = false;
            encounterList.stop();
            if (soundOn && soundPlayer != null)
               soundPlayer.playSound("City");
         }
         // some lines to not log in combat
         else if (!ln.equals(">") && !ln.equals("*") && !ln.equals("") && 
               ln.indexOf("Waiting for your") < 0) {
            encounterList.addLine(ln);
         }
         
         // player dead
         if (ln.indexOf("You are dead") >= 0){
            if (soundOn && soundPlayer != null)
               soundPlayer.playSound("Dead");
         }
      }
      
      clearLine();
      haveText = false;
      boxLine = false;
   }

   /**
    * Return true if row is in avatar status block
    */
   public boolean isStatusLine(int y) {
      return (y > 28 && y <= 32	);
   }

   /**
    * Return true if row is in avatar enconter/combat block
    */
   public boolean isEncounterLine(int y) {
      return (y >= 7 && y < 12);
   }

   /**
    * Stop/Start a display list
    * @param name
    */
   public void toggleDisplay(String name) {
      if ("Chat".equals(name)) {
         chatOn = !chatOn;
      } else if ("Encounter".equals(name)) {
         encounterOn = !encounterOn;
      }
   }
   
   /**
    * Start the autospacebar timer task
    *
    */
   public void startAutoSpace() {
      if (spaceSender == null) {
         // get interval from popup dialog
         spaceSender = new AutoSpace(parent);
         spaceSender.start(1000);
         encounterList.setAutospaceText("NoAutoSpace");
      }
   }

   /**
    * Stop the auto spacer and reset button text on encounter list
    */
   public void stopAutoSpace() {
      if (spaceSender != null) {
         spaceSender.stop();
         spaceSender = null;
         encounterList.setAutospaceText("AutoSpace");
      }
   }
   
   /**
    * Start a Tracker Server process running on the specified port
    * @param p port to listen on
    */
   protected void startTrackerServer(String p) throws IOException {
      int port;
      
      try {
         port = Integer.parseInt(p);
      }catch(Exception e){
         throw new IOException("Bad integer entered for port: " + p);
      }
      
      if (port < 1024 || port > 65536) {
         throw new IOException("Port needs to be between 1025 and 65536");
      }
      
      try {
         trackerServer = new TrackerServer(port);
         trackerServer.start();
      } catch (IOException e) {
         trackerServer = null;
         throw e;
      }
   }
   
   /**
    * Stop the Tracker server
    *
    */
   protected void stopTrackerServer() {
      if (trackerServer != null) {
         trackerServer.shutdown();
         trackerServer = null;
      }
   }
   
   /**
    * Start a Tracker client process and connect to a Tracker server 
    * @param s host:port of Tracker server
    */
   protected void startTrackerClient(String hp) throws IOException {
      String parts[] = hp.split(":");
      int port = 0;
      
      if (parts.length != 2) {
         throw new IOException("Client format is host:port");
      }
      
      try {
         port = Integer.parseInt(parts[1]);
      }catch (Exception e) {
         throw new IOException("Port has to be an interger 1024 - 65536");
      }
      
      if (port < 1024 || port > 65536) {
         throw new IOException("Port has to be an interger 1024 - 65536");
      }
      
      trackerClient = new TrackerClient(parts[0], port, this);
      trackerClient.start();
   }
   
   /**
    * Disconnect from the Tracker server and destroy the tracker client.
    *
    */
   protected void stopTrackerClient() {
      if (trackerClient != null) {
         trackerClient.shutdown();
         trackerClient = null;
      }
   }

   /** ********************************************************************************************
    * Class to display lines in scrolling list
    */
   private class AvatarDisplayList extends JFrame implements ActionListener {

      private String name;

      private Avatar parent;

      private JList dspList = new JList();
      
      private JMenuBar menuBar = null;
      private JButton stopBtn = new JButton("Stop");
      private JButton autoBtn = new JButton("AutoSpace");
      private JToggleButton soundBtn = new JToggleButton();
      private JMenuItem serverMenu = null;
      private JMenuItem clientMenu = null;

      public AvatarDisplayList(String name, Avatar parent) {
         this.name = name;
         this.parent = parent;
         JPanel contentPane = (JPanel) this.getContentPane();
         contentPane.setLayout(new BorderLayout());
         this.setSize(new Dimension(545, 250));
         this.setTitle(name);
        
         DefaultListModel model = new DefaultListModel();
         //model.setSize(2000);
         
         dspList.setFont(new Font("Lucida Sans Typewriter",Font.PLAIN, 14));
         dspList.setModel(model);
         dspList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
         dspList.setLayoutOrientation(JList.VERTICAL);
         dspList.setVisibleRowCount(-1);
         
         JScrollPane sp = new JScrollPane(dspList);
         contentPane.add(sp, BorderLayout.CENTER);

         JPanel btnPanel = new JPanel();

         stopBtn.addActionListener(this);
         stopBtn.setToolTipText("Stop writting to list");
         btnPanel.add(stopBtn);
         JButton clearBtn = new JButton("Clear");
         clearBtn.setToolTipText("Clear list");
         clearBtn.addActionListener(this);
         btnPanel.add(clearBtn);
         
         // if we are an encounter window show the autospace button and sound toggle
         // also add a menu bar for the Tracker client/server options
         if ("Encounter".equals(name)){
            autoBtn.setToolTipText("Toggle auto space");
            autoBtn.addActionListener(this);
            btnPanel.add(autoBtn);
            
            soundBtn.setMargin(new Insets(0,0,0,0));
            URL imageUrl = getClass().getResource("play.png");
            soundBtn.setIcon(new ImageIcon(imageUrl,"Sound"));
            imageUrl = getClass().getResource("play2.png");
            soundBtn.setSelectedIcon(new ImageIcon(imageUrl,"Nosound"));
            soundBtn.setActionCommand("Sound");
            soundBtn.setToolTipText("Toggle sound");
            soundBtn.addActionListener(this);
            btnPanel.add(soundBtn);
            
            menuBar = new JMenuBar();
    		// stop the F10 key from poping the File menu
    		menuBar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("F10"), "none");
    		
    		JMenu trackerMenu = new JMenu("Tracker");
    		menuBar.add(trackerMenu);
    		
    		serverMenu = new JMenuItem("Start Server...");
    		serverMenu.addActionListener(this);
    		trackerMenu.add(serverMenu);
    		
    		clientMenu = new JMenuItem("Start Client...");
    		clientMenu.addActionListener(this);
    		trackerMenu.add(clientMenu);
    		
    		JMenuItem helpMenu = new JMenuItem("Using Tracker...");
    		helpMenu.addActionListener(this);
    		trackerMenu.add(helpMenu);
 
    		this.setJMenuBar(menuBar);
         }
         contentPane.add(btnPanel, BorderLayout.NORTH);

         this.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

      }

      /**
       * Clear the list
       *
       */
      private void clearList() {
         final DefaultListModel model = (DefaultListModel) dspList.getModel();
         model.clear();
         SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               dspList.ensureIndexIsVisible(model.getSize() - 1);
            }
         });
      }

      /**
       * Add a new line to the list
       * @param line
       */
      public void addLine(String line) {
         final DefaultListModel model = (DefaultListModel) dspList.getModel();
         model.addElement(line);
         SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               dspList.ensureIndexIsVisible(model.getSize() - 1);
            }
         });
      }

      /**
       * Reset the Start/Stop button on the encounter list
       *
       */
      public void stop() {
         stopBtn.setText("Start");
         stopBtn.setToolTipText("Start writting to list");
      }
      
      /**
       * Clean up the Tracker menu after a client connection is closed on us
       *
       */
      public void clientOffMenuCleanup(){
         clientMenu.setText("Start Client...");
         serverMenu.setVisible(true);
         this.setTitle(name);
      }
      
      /*
       * Handle actions from the display
       */
      public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();

         if (command.equals("Exit")) {
            this.setVisible(false);
         } else if (command.equals("Clear")) {
            clearList();
         } else if (command.equals("Stop")) {
            parent.toggleDisplay(name);
            stopBtn.setText("Start");
         } else if (command.equals("Start")) {
            parent.toggleDisplay(name);
            stopBtn.setText("Stop");
         } else if (command.equals("AutoSpace")) {
            startAutoSpace();
         } else if (command.equals("NoAutoSpace")) {
            stopAutoSpace();
         }
         else if (command.equals("Sound")){
            parent.toggleSound();
         }
         else if (command.equals("Start Server...")){
            // popup input to get port# for server to listen on
            String s = (String)JOptionPane.showInputDialog(
                  this,
                  "Enter the port# you want the Tracker server to listen for clients on.",
                  "Tracker Server Dialog",
                  JOptionPane.PLAIN_MESSAGE,
                  null,
                  null,
                  "9099");

     	   //If a string was returned, start the server 
     	   if ((s != null) && (s.length() > 0)) {
     	      try {
     	         parent.startTrackerServer(s);
     	         serverMenu.setText("Stop Server");
     	         clientMenu.setVisible(false);
     	         this.setTitle(name + " - Tracker Server on port " + s);
     	      }catch(IOException ex) {
     	         ex.printStackTrace();
     	      }
     	   } 
         }
         else if (command.equals("Stop Server")){
            parent.stopTrackerServer();
            serverMenu.setText("Start Server...");
            clientMenu.setVisible(true);
            this.setTitle(name);
         }
         else if (command.equals("Start Client...")){
            // popup input to get host:port for server to listen on
            String s = (String)JOptionPane.showInputDialog(
                  this,
                  "Enter the host:port of the Tracker server to connect to.",
                  "Tracker Client Dialog",
                  JOptionPane.PLAIN_MESSAGE,
                  null,
                  null,
                  "localhost:9099");
            
            // If a string was returned, start the server 
      	   if ((s != null) && (s.length() > 0)) {
      	      try {
      	         parent.startTrackerClient(s);
      	         clientMenu.setText("Stop Client");
      	         serverMenu.setVisible(false);
      	         this.setTitle(name + " - Tracker Client to " + s);
      	      }catch(IOException ex) {
      	         System.out.println("Exception starting client:");
      	         ex.printStackTrace();
      	      }
      	   } 
         }
         else if (command.equals("Stop Client")){
            parent.stopTrackerClient();
            clientMenu.setText("Start Client...");
            serverMenu.setVisible(true);
            this.setTitle(name);
         }
         else if (command.equals("Using Tracker...")){
            displayTrackerHelp();
         }
      }
      
      /**
       * Change the text for the autospace button
       * @param txt
       */
      public void setAutospaceText(String txt){
         autoBtn.setText(txt);
      }
      
      /**
       * Display a help popup window for the Tracker server options
       */
      private void displayTrackerHelp() {
         JOptionPane.showMessageDialog(this, 
           "Use the Tracker functions to pass commands from a tracked JPterm to tracking JPterms.\n" +
           "To use, track (T) your main character as normal.\n" +
           "Display the Avatar plugin Encounter windows for each character.\n" +
           "On your main character select Tracker->Start Server and pick a port number to listen on\n" +
           "For the tracking characters select Tracker->Start Client.\n" +
           "Specify the host or IP and port for the server (localhost if on the same system).\n" +
           "You can now use the ALT key from the main character to perform some limited actions on the\n" +
           "client characters. These actions are:\n\n" +
           "    ALT-f   start autofight on main and clients\n" +
           "    ALT-s   start/stop auto space on clients\n" +
           "    ALT-g   start autofight on main and autospell on clients\n" +
           "    ALT-<   cast spell 0 on clients (spell loaded in buffer 0)\n" +
           "    ALT->   cast spell 1 on clients\n" +
           "    ALT-[   cast spell 2 on clients\n" +
           "    ALT-]   cast spell 3 on clients\n" +
           "    ALT-$   cast spell 4 on clients\n" +
           "    ALT-%   cast spell 5 on clients\n" +
           "    ALT-_   cast spell 6 on clients\n" +
           "    ALT-'   cast spell 7 on clients\n" +
           "    ALT-*   cast spell 8 on clients\n" +
           "    ALT-(   cast spell 9 on clients\n" +
           "\nI may make this all configurable at some point, but not tonight."
         );
      }
   }
   
   /** *********************************************************************************************
    * A timer task to periodicly send a space to Plato for Tracking players
    * 
    */
   private class AutoSpace extends TimerTask {

      private Timer timer = null;
      private Jpterm parent;
      
      public AutoSpace(Jpterm parent){
         this.parent = parent;
      }
      
      public void start(long interval){
         timer = new Timer();
         timer.scheduleAtFixedRate(this, interval, interval);
      }
      
      public void stop(){
         timer.cancel();
         timer = null;
      }
      
      public void run() {
         parent.putChar(0100);
      } 
   }

   /** *********************************************************************************************
    * A class to play sounds for Avatar
    * 
    */
   private class SoundPlayer {
      private HashMap sounds = new HashMap();
      private String soundDir = "";
      
      public SoundPlayer(HashMap params){
         if (params.containsKey("SoundDir")){
            soundDir = (String)params.get("SoundDir") + "/";
         }
        
         Iterator itr = params.keySet().iterator();
         while(itr.hasNext()){
            String name = (String)itr.next();
            String val = (String)params.get(name);
            if (name.startsWith("Sound")) {
                  String[] snds = val.split(":");
                  if (snds.length == 2){
                     try {
                        AudioClip clip = Applet.newAudioClip(getClass().getResource(soundDir + snds[1]));
                        sounds.put(snds[0], clip);
                     }catch(Exception e){
                        System.out.println("Unable to load sound file " + soundDir + snds[1] + 
                              ": " + e.getMessage());
                     }
                  }
            }   
         }
      }
      
      public void playSound(String key){
         if (sounds.containsKey(key)){
            ((AudioClip)sounds.get(key)).play();
         }
      }
      
      public void playAllSounds() {
         Iterator itr = sounds.keySet().iterator();
         while (itr.hasNext()){
            String key = (String)itr.next();
            System.out.println(key + ":");
            ((AudioClip)sounds.get(key)).play();
            try {
               Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
         }
      }
   }

   /** *********************************************************************************************
    * A Tracker server deamon that listens for clients and creates processors to handle them
    * 
    */
   private class TrackerServer extends Thread {

      private int port;
      private ServerSocket serverSocket = null;
      private boolean running  = false;
      private ArrayList clients = new ArrayList(5);
      
      public TrackerServer(int port) throws IOException {
         this.port = port;
         serverSocket = new ServerSocket(port);
         this.running = true;
         System.out.println("Tracker server listening on port " + port);
      }
      
      /**
       * Disconnect all connected clients and shutdown the server
       */
      public void shutdown() {
         for (int i = 0; i < clients.size(); i++) {
            ((TrackerServerProcessor)clients.get(i)).stop();
         }
         try {
            serverSocket.close();
         } catch (IOException e) {
            // do nothing
         }
         clients.clear();
         this.running = false;
         this.interrupt();
      }
      
      /**
       * Pass an ALT command to all the connected clients
       * @param cmd
       */
      public void putCommand(char cmd){
         for (int i = 0; i < clients.size(); i++) {
            ((TrackerServerProcessor)clients.get(i)).putCommand(cmd);
         }
      }
      
      /**
       * Wait for clients to connect, create a process to handle them and add them to the list
       */
      public void run() {
         while(running) {
            try {
               Socket clientSocket = serverSocket.accept();
               try {
                  TrackerServerProcessor newClient = new TrackerServerProcessor(clientSocket, this);
                  clients.add(newClient);
               } catch(IOException ex){
                  ex.printStackTrace();
               }
            } catch (IOException e) {
                //System.out.println("Accept failed: " + e.getMessage());
            }
         }
      }
      
      /**
       * Remove a client from the list
       * @param client
       */
      public void removeClient(Object client){
         if (clients.contains(client)){
            clients.remove(client);
         }
      }
      
   }
   
   /** ****************************************************************************************
    * This class handles actual client connects. It will pass ALT-commands to the client
    */
   private class TrackerServerProcessor {
      private Socket socket = null;
      private OutputStream out = null;
      private TrackerServer parent = null;
      
      public TrackerServerProcessor(Socket socket, TrackerServer parent) throws IOException {
         this.socket = socket;
         out = socket.getOutputStream();
         this.parent = parent;
         //System.out.println("Accept client " + socket.toString());
      }
      
      public void stop() {
         try {
            out.write(-1);
            out.flush();
            out.close();
            socket.close();
         } catch (IOException e) {
            // do nothing
         }
      }
      
      public void putCommand(char cmd) {
         try {
            out.write(cmd);
            out.flush();
         } catch (IOException e) {
            // client must have gone away
            try {
               out.close();
               socket.close();
            } catch (IOException e1) {
               // do nothing
            }
            parent.removeClient(this);
         }
      }
   }

   /** **************************************************************************************
    * Class to listen to a tracker server and pass ALT commands through to Plato
    */
   private class TrackerClient extends Thread {

      private String host = null;
      private int port;
      private Socket socket = null;
      private InputStream in = null;
      private boolean running = false;
      private Avatar parent = null;
      
      public TrackerClient(String host, int port, Avatar parent) throws IOException {
         this.host = host;
         this.port = port;
         this.parent = parent;
         
         try {
            socket = new Socket(host, port);
            in = socket.getInputStream();
         } catch (UnknownHostException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
         }
         
         System.out.println("Starting tracker client...");
      }
      
      public void shutdown() {
         try {
            in.close();
            socket.close();
         } catch (IOException e) {
            e.printStackTrace();
         }
         this.running = false;
         this.interrupt();
      }
      
      public void run() {
         int c;
         this.running = true;
         try {
            while (running) {
               c = in.read();
               
               if (c == -1){
                  parent.encounterList.clientOffMenuCleanup();
                  this.shutdown();
               }
               else {
                  charFromServer((char)c);
               }
            }
         } catch (IOException e) {
            //e.printStackTrace();
            this.shutdown();
            parent.encounterList.clientOffMenuCleanup();
         }
         
         System.out.println("Stopping tracker client");
      }
      
   }
}
