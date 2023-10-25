package jpterm.utils;

import java.applet.Applet;
import java.applet.AudioClip;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * @author KenK
 * 
 */
public class SoundTest {

   public SoundTest() {
      try {
         System.out.println("before");
         AudioClip clip = Applet.newAudioClip(getClass().getResource("fanfare_x.wav") );
         clip.play();
         System.out.println("after");
      } catch (Exception e) {
      }  
   }
   
   public void complex() {
      try {
         AudioInputStream stream = AudioSystem.getAudioInputStream(getClass()
               .getResource("fanfare_x.wav"));

         AudioFormat format = stream.getFormat();

         // convert ULAW/ALAW formats to PCM format
         if ((format.getEncoding() == AudioFormat.Encoding.ULAW)
               || (format.getEncoding() == AudioFormat.Encoding.ALAW)) {
            AudioFormat newFormat = new AudioFormat(
                  AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(),
                  format.getSampleSizeInBits() * 2, format.getChannels(),
                  format.getFrameSize() * 2, format.getFrameRate(), true); // big
                                                                           // endian
            // update stream and format details
            stream = AudioSystem.getAudioInputStream(newFormat, stream);
            System.out.println("Converted Audio format: " + newFormat);
            format = newFormat;
         }

         DataLine.Info info = new DataLine.Info(Clip.class, format);

         // make sure sound system supports data line
         if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Unsupported Clip File: ");
            return;
         }

         // get clip line resource
         Clip clip = (Clip) AudioSystem.getLine(info);

         // listen to clip for events
         //clip.addLineListener(this);

         clip.open(stream); // open the sound file as a clip
         stream.close(); // we're done with the input stream
         
         clip.start();

         //checkDuration();
      } // end of try block

      catch (UnsupportedAudioFileException audioException) {
         System.out.println("Unsupported audio file: ");
      } catch (LineUnavailableException noLineException) {
         System.out.println("No audio line available for : ");
      } catch (IOException ioException) {
         System.out.println("Could not read: ");
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public static void main(String[] args) {
      SoundTest myTest = new SoundTest();
      
   }
}
