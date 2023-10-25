This is Jpterm 1.0, a Java implementation of pterm.

Please add any comments to the Note file for JPterm in avatar.


To use:

  You will need a JDK or JRE 1.4 or greater installed and working
  Unzip the JPterm.zip file
  java -jar JPterm.jar [config file]

  or

  java -classpath JPterm.jar:. jpterm.Jpterm [config file]

  Jpterm will look for a config file called JPtermConfig.xml in the
  current directory, but you can also supply an alternate path/name.

The rest should be self evident.

Changes:
  1.0
   Fixed the F10 problem with the File menu
   Added license info to source
  0.5
   Big change is added sounds, which is why the jar is so much larger
   Will play a sound clip based on Plato icons; boxs and monsters. You can
   change or turn off sounds from the config file.
  0.4
   Some small fixes with the Avatar plugin, made it threaded
  0.3
   Resize window on Bigger/Smaller
   Correct a display problem with line draw
   Position Avatar plugins based on main window size
   Add a chat timer to Avatar plugin for more timely writes
   Cleanup line handling a bit in Avatar plugin
   Allow passing parameters to plugins

  0.2
   Added a Save option to File Menu
   Made JAR file executable with -jar param
   Keep trying all the connection addresses until one works