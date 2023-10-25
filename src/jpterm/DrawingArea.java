package jpterm;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * Handles all screen painting
 * 
 * @author khkachn
 * Created on May 13, 2005
 * Version $Id$
 * License: See file license.txt for lisence details
 * 
 * Copyright: Ken Kachnowich 2005
 */
public class DrawingArea extends JPanel {
   private static final int ROWS = 32;

   private static final int COLUMNS = 64;

   private static final int SCREENWIDTH = 512;

   private static final int SCREENHEIGHT = 512;

   private static final int TOPMARGIN = 0;

   private static final int LEFTMARGIN = 0;

   // off screen rendering
   private Graphics2D dbg;

   private Image dbImage = null;

   /** Character set to use in display */
   private BufferedImage[][][] ptermChars = null;

   /** current height of a display char */
   private int charHeight = 16;

   /** current width of display char */
   private int charWidth = 8;

   /** how much bigger/smaller then default the screen is */
   private float scaleF = 1.0f;
   
   /** width of display area (default is 512) */
   private int XSIZE = SCREENWIDTH;

   /** height of the display area (defualt is 512) */
   private int YSIZE = SCREENHEIGHT;

   /** color of characters */
   private Color fgColor = new Color(255, 102, 0, 255);

   /** color of background */
   private Color bgColor = new Color(0, 0, 0, 255);
   
   private GraphicsConfiguration gc;
   private RenderingHints hints = null;
   
   /** used to toggle various debug modes on/off */
   private boolean debug = false;

   private boolean blkEdebug = false;

   private boolean gridOn = false;

   private boolean debugLine = false;

   private boolean displayChars = false;

   public DrawingArea() {
      // get this device's graphics configuration
      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
      hints = new RenderingHints(null);
      hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
   }
   
   /**
    * dbImage is null if I try this in the constructor, not
    * sure what that is all about
    *
    */
   public void init(Color fgc, Color bgc, int charW) {
      // get the display image
      int c = 0;
      while (dbImage == null && c < 100) {
         //dbImage = createImage((SCREENWIDTH+8), (SCREENHEIGHT+16));
         dbImage = new BufferedImage((SCREENWIDTH+8), (SCREENHEIGHT+16), BufferedImage.TYPE_4BYTE_ABGR);
         if (dbImage == null) {
            try {
               Thread.sleep(100);
            } catch (InterruptedException e) {
               c = 51;
            }
         } else {
            dbg = (Graphics2D)dbImage.getGraphics();
         }   
         c++;
      }
      
      setCharSize(charW);
      
      this.bgColor = bgc;
      this.fgColor = fgc;

      setBackground(bgColor);
      setForeground(fgColor);
      
      // load the ROM character sets into images
      ptermLoadRomChars();
   }

   /**
    * Set the character size based on the width. Height is just 2 times width
    * @param s
    */
   public void setCharSize(int s) {
      charHeight = s * 2;
      charWidth = s;
      recalcScreen();
      this.repaint();
   }
   
   /**
    * Make the display 1 unit bigger, set the screen size and scaleFactor
    */
   public void bigger() {
      charHeight += 2;
      charWidth += 1;
      recalcScreen();
      this.repaint();
   }

   /**
    * Make the display one unit smaller, set the screen size and scaleFactor
    */
   public void smaller() {
      charWidth -= 1;
      charHeight -= 2;
      recalcScreen();
      this.repaint();
   }
   
   /**
    * Recalculate the sreen size and how much bigger/smaller we are compaired to
    * the default screen
    *
    */
   private void recalcScreen(){
      XSIZE = charWidth * COLUMNS;
      YSIZE = charHeight * ROWS;
      scaleF = (float)XSIZE/(float)SCREENWIDTH;
   }

   /**
    * Set the background color
    * 
    * @param c new Color 
    */
   public void setBackgroundColor(Color c) {
      int oldc = bgColor.getRGB();
      int newc = c.getRGB();
      // convert background in all loaded character images
      colorConvert(oldc, newc);
      this.bgColor = c;
      this.setBackground(bgColor);
      // change the color in the displayed image
      changeImageColor(oldc, newc);
      this.repaint();
   }

   /**
    * Set the foreground color
    * 
    * @param c new Color
    */
   public void setForegroundColor(Color c) {
      int oldc = fgColor.getRGB();
      int newc = c.getRGB();
      // convert the color in all the loaded character images
      colorConvert(oldc, newc);
      this.fgColor = c;
      this.setForeground(fgColor);
      // change the color in the display image
      changeImageColor(oldc, newc);
      this.repaint();
   }
   
   /**
    * Change a color in the display image
    * @param oldc
    * @param newc
    */
   private void changeImageColor(int oldc, int newc){
      BufferedImage bimg = (BufferedImage) dbImage;
      int[] tst = bimg.getRGB(0, 0, bimg.getWidth(), bimg.getHeight(), null, 0,
            bimg.getWidth());
      for (int i = 0; i < tst.length; i++) {
         if (tst[i] == oldc)
            tst[i] = newc;
      }
      bimg.setRGB(0, 0, bimg.getWidth(), bimg.getHeight(), tst, 0, bimg
            .getWidth());
   }

   /**
    * Convert a Plato Y coord to one more to our liking
    * 
    * @param y
    * @return
    */
   public int adjustY(int y) {
      return (int) (((SCREENHEIGHT - y) + TOPMARGIN));
   }

   public int adjustX(int x) {
      return (int) (LEFTMARGIN + x);
   }

   /****************************************************************************
    * Method that draws the screen
    */
   public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g;
      g2.setBackground(bgColor);
      g2.setRenderingHints(hints);

      // debug code
      if (displayChars) {
         displayCharacters(g2);
      }
      else {
         g2.drawImage(dbImage, 0, 0, (XSIZE + charWidth), (YSIZE + charHeight), null);

         if (gridOn)
            paintGrid(g2);
      }
   }

   /**
    * Clear the display image and repaint
    *  
    */
   public void fullScreenErase() {
      dbg.setBackground(bgColor);
      dbg.clearRect(0, 0, (XSIZE+8), (YSIZE+16));
      this.repaint();
   }

   /**
    * Add a character to the display and repaint area it's in. 
    * 
    * @param x x coord for char
    * @param y y coord
    * @param charSet character set char is in
    * @param c char number
    * @param wemode write/erase mode
    */
   public void drawChar(int x, int y, int charSet, int c, int wemode) {
      int xo = x;
      int yo = y;
      
      // adjust Plato coord for our screen system
      x = adjustX(x);
      y = adjustY(y);

      if (x < 0 || x > (SCREENWIDTH + LEFTMARGIN)) {
         System.out.println("DC: bad x: " + x);
         return;
      }
      if (y < 0 || y > (SCREENHEIGHT + TOPMARGIN)) {
         System.out.println("DC: bad y " + y);
         return;
      }

      if (debug) {
         System.out.print(fmt(c, 2) + ":" + fmt(y, 3) + "," + fmt(x, 3) +
               " (" + fmt(yo,3) + "," + fmt(xo,3) + ") " + " we: " + wemode+ " cs:" + charSet);
         if (charSet < 2)
            System.out.println(" - " + PlatoSpecific.romChar[charSet][c]);
         else
            System.out.println("");
      }

      // 0 inverse mode
      // 1 rewrite mode (clear background)
      // 2 erase mode
      // 3 write mode (alpha background)
      dbg.drawImage(getPtermChar(charSet, c, wemode), x, y, null);
      
      repaintArea(x, y, (x + charWidth), (y + charHeight));
   }

   private static String fmt(int c, int s) {
      String tmp = "000" + c;
      return tmp.substring(tmp.length() - s);
   }

   /**
    * Add a point to the screen, really a very small rectangle.
    * 
    * @param x
    * @param y
    */
   public void drawPoint(int x, int y) {
      x = adjustX(x);
      y = adjustY(y - 16);
      dbg.drawRect(x, y, (x + 1), (y + 1));
      this.repaint(x, y, 1, 1);
   }

   /**
    * Add a line to the display and repaint area of line
    * 
    * @param sx from point
    * @param sy
    * @param ex to point
    * @param ey
    * @param wemode write/erase mode
    */
   public void drawLine(int sx, int sy, int ex, int ey, int wemode) {
      // adjust to our screen coord system
      sx = adjustX(sx);
      sy = adjustY(sy - 15);
      ex = adjustX(ex);
      ey = adjustY(ey - 15);
      
      if (sx > ex){
         int t = sx;
         sx = ex;
         ex = t;
         t = sy;
         sy = ey;
         ey = t;
      }
      
      if (debugLine){
         System.out.println("ln: " + fmt(sy, 3) + "," + fmt(sx, 3) + " to "
               + fmt(ey, 3) + "," + fmt(ex, 3) + " wemode:" + wemode);
      }   

      // erase line mode
      if (wemode == 2) {
         dbg.setColor(bgColor);
      } else {
         dbg.setColor(fgColor);
      }

      dbg.drawLine(sx, sy, ex, ey);
      
      repaintArea(sx, sy, ex, ey);
   }

   /**
    * Erase a rectangular area
    * 
    * @param x1 start x
    * @param y1 start y
    * @param x2 end x
    * @param y2 end y
    * @param wemode write/erase mode
    */
   public void blockErase(int sx, int sy, int ex, int ey, int wemode) {
      if (blkEdebug)
         System.out.println("BE: " + sy + "," + sx + " to " + ey + "," + ex
               + " wemode:" + wemode);

      sx = adjustX(sx);
      sy = adjustY(sy - 15);
      ex = adjustX(ex);
      ey = adjustY(ey - 16);

      if (sy > ey) {
         int t = sy;
         sy = ey;
         ey = t;
      }
      if (sx > ex) {
         int t = sx;
         sx = ex;
         ex = t;
      }
      
      // not sure why this is needed
      ex++;

      if (wemode == 3)
      	dbg.setBackground(fgColor);
      else
      	dbg.setBackground(bgColor);
      dbg.clearRect(sx, sy, (ex - sx), (ey - sy));
      
      if (blkEdebug) {
         System.out.println("BE Adj: " + sy + "," + sx + " to " + ey + "," + ex);
      }
      
      repaintArea(sx, sy, ex, ey);
   }
   
   /**
    * The image may not be the same size as the screen, so need to calc where to repaint
    * @param sx
    * @param sy
    * @param ex
    * @param ey
    */
   private void repaintArea(int sx, int sy, int ex, int ey){
      if (ex < sx){
         int t = ex;
         ex = sx;
         sx = t;
      }
      if (ey < sy){
         int t = ey;
         ey = sy;
         sy = t;
      }
      int h = (int)(((ey - sy) + 4) * scaleF);
      int w = (int)(((ex - sx) + 4) * scaleF);
      sx = (int)((sx * scaleF) - 2);
      sy = (int)((sy * scaleF) - 2);
      
      if (debugLine) {
         System.out.println("RP:"+sy+","+sx+" "+w+"x"+h);
         //dbg.setColor(Color.blue);
         //dbg.drawRect(sx,sy,w,h);
         //System.out.println("CS:"+this.getHeight() +","+this.getWidth()+" "+scaleF);
      }
      
      repaint(sx,sy,w,h);
   }

   /****************************************************************************
    * Convert a color of all the images to another color
    *  
    */
   private void colorConvert(int oldRGB, int newRGB) {
      int[] tst = new int[256];

      for (int m = 0; m < 4; m++) {
         for (int cs = 0; cs < ptermChars[m].length; cs++) {
            for (int c = 0; c < ptermChars[m][cs].length; c++) {
               if (ptermChars[m][cs][c] != null) {
                  ptermChars[m][cs][c].getRGB(0, 0, 8, 16, tst, 0, 8);
                  for (int i = 0; i < tst.length; i++) {
                     if (tst[i] == oldRGB)
                        tst[i] = newRGB;
                  }
                  ptermChars[m][cs][c].setRGB(0, 0, 8, 16, tst, 0, 8);
               }
            }
         }
      }
   }

   /** 
    * Convert a BufferedImage to one (possibly) optimized for our display
    * @param im
    * @return
    */
   	private BufferedImage getOptimizedImage(BufferedImage im) {
   	   int transparency = im.getColorModel().getTransparency();
   	   BufferedImage copy = gc.createCompatibleImage(
   	         im.getWidth(), im.getHeight(), transparency);
       // create a graphics context
   	   Graphics2D g2d = copy.createGraphics();
   	   // copy image
   	   g2d.drawImage(im,0,0,null);
   	   g2d.dispose();
   	   return copy;
   	}
   	
   /**
    * Convert a Plato defined character to a Java Image. Character can be one 
    * of these write/erase modes:
    * 
    *  0 inverse mode
    *  1 rewrite mode
    *  2 erase mode
    *  3 write mode
    * 
    * @param raw raw Plato character data array
    * @param off offset into raw data array
    * @return BufferedImage for the pixles 
    */
   private BufferedImage loadChar(int[] raw, int off, int wemode) {
      int w = 8;
      int h = 16;
      int col;
      int bg = bgColor.getRGB();
      int fg = fgColor.getRGB();
      // inverse and erase
      if (wemode == 0 || wemode == 2) {
         int t = bg;
         bg = fg;
         fg = t;
      }

      BufferedImage _rtn = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);

      // set pixles to background color for inverse and write
      if (wemode == 1 || wemode == 0) {
         int[] tst = _rtn.getRGB(0, 0, w, h, null, 0, w);
         for (int i = 0; i < tst.length; i++)
            tst[i] = bg;
         _rtn.setRGB(0, 0, w, h, tst, 0, w);
      }

      for (int y = 0; y < 8; y++) {
         col = raw[off + y];
         for (int x = 0; x < 16; x++) {
            if ((col & 1) > 0) {
               // drawing char pixel
               _rtn.setRGB(y, (15 - x), fg);
            }
            col >>= 1;
         }
      }
      return getOptimizedImage(_rtn);
   }

   /**
    * Load all the ROM based characters in the various w/e modes
    *  
    */
   private void ptermLoadRomChars() {
      ptermChars = new BufferedImage[4][5][(PlatoSpecific.plato_m0.length / 8)];

      for (int m = 0; m < 4; m++) {
         for (int i = 0; i < (PlatoSpecific.plato_m0.length / 8); i++) {
            ptermChars[m][0][i] = loadChar(PlatoSpecific.plato_m0, (i * 8), m);
            ptermChars[m][1][i] = loadChar(PlatoSpecific.plato_m1, (i * 8), m);
         }
      }
   }

   /**
    * Return Image for requested character
    * 
    * @param charSet character set character is in
    * @param chr number of the character
    * @return
    */
   private BufferedImage getPtermChar(int charSet, int chr, int wemode) {
      return ptermChars[wemode][charSet][chr];
   }


   /**
    * Convert a charater from Plato to an Image
    * 
    * @param raw
    * @param off
    * @param charNum
    * @param area
    */
   public void loadPlatoChar(int[] raw, int off, int setNum, int charNum) {
      //System.out.println("Load char off:" + off + " num:" + charNum + " set:" + setNum);
      for (int m = 0; m < 4; m++) {
         ptermChars[m][setNum][charNum] = loadChar(raw, off, m);
      }
   }

   /****************************************************************************
    * Debugging methods
    * 
    * Draw a grid and row/column numbers for debuging
    * 
    * @param g2
    */
   private void paintGrid(Graphics2D g2) {

      g2.setColor(Color.BLUE);

      Rectangle2D r = new Rectangle2D.Float(LEFTMARGIN, TOPMARGIN, XSIZE, YSIZE);
      g2.draw(r);

      int c = 27;
      int x = 0;
      int y = 0;
      for (int i = 0; i <= ROWS; i++) {
         g2.drawImage(ptermChars[3][0][c], x, y, charWidth, charHeight, null);
         y += charHeight;
         c++;
         if (c > 36) {
            c = 27;
         }
         g2.draw(new Line2D.Float(x, y, XSIZE, y));
      }

      c = 28;
      x = charWidth;
      y = 0;
      for (int i = 1; i <= COLUMNS; i++) {
         g2.drawImage(ptermChars[3][0][c], x, y, charWidth, charHeight, null);
         x += charWidth;
         c++;
         if (c > 36)
            c = 27;
      }
   }

   /**
    * Display the character sets for debugging
    * 
    * @param g2
    */
   private void displayCharacters(Graphics2D g2) {
      int y = (TOPMARGIN + charHeight);
      int x = (LEFTMARGIN + charWidth);

      for (int m = 2; m < ptermChars[3].length; m++) {
         for (int i = 0; i < ptermChars[3][m].length; i++) {
            if (ptermChars[3][m][i] != null) {
               g2.drawImage(ptermChars[3][m][i], x, y, charWidth, charHeight,
                     null);
            }
            x += (charWidth * 2);
            if (x >= (20 * charWidth)) {
               x = charWidth + LEFTMARGIN;
               y += (charHeight * 2);
            }
         }
         y += charHeight;
         x = charWidth + LEFTMARGIN;
      }

      if (gridOn)
         paintGrid(g2);
   }

   /**
    * These methods toggle various debug modes
    *  
    */
   public void toggleDebug() {
      debug = !debug;
   }

   public void debugBlkErase() {
      blkEdebug = !blkEdebug;
   }

   public void gridToggle() {
      gridOn = !gridOn;
      this.repaint();
   }

   public void debugLines() {
      debugLine = !debugLine;
   }

   public void displayChar() {
      displayChars = !displayChars;
      this.repaint();
   }
}
