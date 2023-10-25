package jpterm;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Reads and saves an JPterm XML configuration file
 * 
 * @author Ken Kachnowich Created on Jun 5, 2005
 * version $Id$
 * License: See file license.txt for license details
 * 
 * Copyright: Ken Kachnowich 2005
 */
public class JptermConfig {

   private String fileName;

   private ArrayList connection = new ArrayList();

   private ArrayList plugins = new ArrayList();

   private HashMap pluginParams = new HashMap();

   private int nextCon = 0;

   private Color bgColor;

   private Color fgColor;

   private int charSize = 8;
   
   private int throttle = 0;

   public boolean readConfig(String conf) {
      this.fileName = conf;
      Document doc = null;
      int fgInt = -13312;
      int bgInt = 0;
      File configFile = new File(conf);

      if (!configFile.exists()) {
         System.out.println(configFile + " does not exist");
         return false;
      }

      try {

         // Initialize the XML DOM Parser to be a non validating parser
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);

         DocumentBuilder builder = factory.newDocumentBuilder();

         //URL xmlUrl = ExifTagData.class.getClassLoader().getResource(conf);
         //InputSource saxIn = new InputSource(xmlUrl.openStream());
         //doc = builder.parse(saxIn);

         doc = builder.parse(configFile);

         // normalize text representation
         doc.getDocumentElement().normalize();

         Element root = doc.getDocumentElement();

         // load the connections
         NodeList nodes = root.getElementsByTagName("Connection");

         for (int j = 0; j < nodes.getLength(); j++) {
            Element conElem = (Element) nodes.item(j);
            String addrs = conElem.getAttribute("Address");
            connection.add(addrs);
            //System.out.println("Conn:"+addrs);
         }

         // load the plugins
         nodes = root.getElementsByTagName("Plugin");

         for (int j = 0; j < nodes.getLength(); j++) {
            Element plugNode = (Element) nodes.item(j);
            String pluginClass = plugNode.getAttribute("Class");
            plugins.add(pluginClass);

            // check for Plugin parameter data
            NodeList nList = plugNode.getElementsByTagName("Param");
            if (nList.getLength() > 0) {
               HashMap paramMap = new HashMap();
               for (int i = 0; i < nList.getLength(); i++) {
                  Element paramElem = (Element) nList.item(i);
                  String name = paramElem.getAttribute("Name");
                  String val = paramElem.getAttribute("Value");
                  paramMap.put(name, val);
               }
               this.pluginParams.put(pluginClass, paramMap);
            }
         }

         nodes = root.getElementsByTagName("Foreground");
         String tmpTxt = ((Element) nodes.item(0)).getAttribute("Color");
         if (tmpTxt != null && !"".equals(tmpTxt.trim())) {
            fgInt = Integer.parseInt(tmpTxt);
         }
         fgColor = new Color(fgInt);

         nodes = root.getElementsByTagName("Background");
         tmpTxt = ((Element) nodes.item(0)).getAttribute("Color");
         if (tmpTxt != null && !"".equals(tmpTxt.trim())) {
            bgInt = Integer.parseInt(tmpTxt);
         }
         bgColor = new Color(bgInt);

         nodes = root.getElementsByTagName("CharSize");
         tmpTxt = ((Element) nodes.item(0)).getAttribute("Size");
         if (tmpTxt != null && !"".equals(tmpTxt.trim())) {
            charSize = Integer.parseInt(tmpTxt);
         }
         
         nodes = root.getElementsByTagName("Throttle");
         if (null != nodes && null != nodes.item(0)){
            tmpTxt = ((Element) nodes.item(0)).getAttribute("WordsPerSecond");
            if (tmpTxt != null && !"".equals(tmpTxt.trim())) {
               throttle = Integer.parseInt(tmpTxt);
            }
         }

      } catch (Exception e) {
         e.printStackTrace();
         return false;
      }

      return true;
   }

   public Color getForegroundColor() {
      return fgColor;
   }

   public void setForegroundColor(Color c) {
      this.fgColor = c;
   }

   public Color getBackgroundColor() {
      return bgColor;
   }

   public void setBackgroundColor(Color c) {
      this.bgColor = c;
   }
   
   public int getThrottle() {
      return this.throttle;
   }
   
   public void setThrottle(int throttle){
      if (throttle < 0) throttle = 0;
      this.throttle = throttle;
   }

   public int getCharSize() {
      return this.charSize;
   }

   public void setCharSize(int s) {
      this.charSize = s;
   }

   /**
    * Return the number of plugins loaded
    * 
    * @return
    */
   public int getNumberPlugins() {
      return plugins.size();
   }

   /**
    * Return the class name of the plugin requested or null if there isn't one
    * 
    * @param c
    * @return
    */
   public String getPlugin(int c) {
      String _rtn = null;
      if (c >= 0 && c < plugins.size())
         _rtn = (String) plugins.get(c);
      return _rtn;
   }

   /**
    * Return the HashMap of parameters for the plugin or null if no parameters
    * defined
    * 
    * @param plugin
    * @return
    */
   public HashMap getPluginParameters(String plugin) {
      HashMap _rtn = null;

      if (this.pluginParams.containsKey(plugin)) {
         _rtn = (HashMap) pluginParams.get(plugin);
      }

      return _rtn;
   }

   /**
    * Return the next connection string, keep looping through the list
    * 
    * @return
    */
   public String getNextConnection() {
      String _rtn = null;
      if (nextCon >= connection.size()) {
         nextCon = 0;
      }
      _rtn = (String) connection.get(nextCon);
      nextCon++;

      return _rtn;
   }

   public void save() {
      File outFile = new File(fileName);
      try {
         FileWriter out = new FileWriter(outFile);
         out.write(toString());
         out.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   /**
    * Return the config info as an XML formated string
    */
   public String toString() {
      StringBuffer buf = new StringBuffer(
            "<?xml version=\"1.0\" standalone=\"yes\"?>\n\n");

      buf.append("<JPtermConfig>\n  <ConnectionInfo>\n");
      for (int i = 0; i < connection.size(); i++) {
         buf.append("    <Connection Address=\"" + (String) connection.get(i)
               + "\" />\n");
      }

      buf.append("  </ConnectionInfo>\n\n  <KnownPlugins>\n");
      for (int i = 0; i < plugins.size(); i++) {
         String piClass = (String) plugins.get(i);
         buf.append("    <Plugin Class=\"" + piClass + "\" >\n");
         // if the plugin has parameters defined add them to the XML
         if (pluginParams.containsKey(piClass)) {
            HashMap pp = (HashMap) pluginParams.get(piClass);
            Iterator itr = pp.keySet().iterator();
            while (itr.hasNext()) {
               String name = (String) itr.next();
               String val = (String) pp.get(name);
               buf.append("      <Param Name=\"" + name + "\" Value=\"" + val
                     + "\" />\n");
            }
         }
         buf.append("    </Plugin>\n");
      }

      buf.append("  </KnownPlugins>\n\n  <DisplayInfo>\n");
      buf.append("    <Foreground Color=\"" + fgColor.getRGB() + "\" />\n");
      buf.append("    <Background Color=\"" + bgColor.getRGB() + "\" />\n");
      buf.append("    <CharSize Size=\"" + charSize + "\" />\n");
      buf.append("    <Throttle WordsPerSecond=\"" + throttle + "\" />\n");

      buf.append("  </DisplayInfo>\n</JPtermConfig>\n");

      return buf.toString();
   }

}
