package jpterm.plugins;

import java.util.HashMap;

import jpterm.Jpterm;

/*
 * An interface for plugins to the JPterm program
 * 
 * @author Ken Kachnowich Created on May 29, 2005
 * Version: $Id$
 * License: See file license.txt for license details
 * 
 * Copyright: Ken Kachnowich, 2005
 */
public interface PluginIntr {
   /** called after contruction, passes in JPterm reference */
   abstract void init(Jpterm parent, HashMap parameters);
   /** called when plugin selected from menu */
   abstract void start();
   /** called when plugin deselected from menu */
   abstract void shutdown();
   /** used to load plugin name in menu drop down */
   abstract String getName();
   /** return true to receive keys from Plato */
   abstract boolean isRunning();
   /** Pass a character from plato to plugin */
   abstract void charFromPlato(int x, int y, int charSet, int c);
   /** character typed on keyboard */
   abstract void charToPlato(int c);
   /** a FUNC-key was pressed on the pterm window */
   abstract void altKeyTyped(char c);
}
