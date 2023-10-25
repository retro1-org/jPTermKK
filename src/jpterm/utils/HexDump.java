package jpterm.utils;

//import java.io.*;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2002
 * Company:
 * @author
 * @version 1.0
 */

public class HexDump {

   private static final int ROW_BYTES = 16;
   private static final int ROW_QTR1 = 3;
   private static final int ROW_HALF = 7;
   private static final int ROW_QTR2 = 11;

   public static String dumpHexData(byte[] buf, int offset, int numBytes ) {

      StringBuffer buff = new StringBuffer();

      int rows, residue, i, j;
      byte[] save_buf= new byte[ ROW_BYTES+2 ];

      String[] hex_chars = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B",
                            "C", "D", "E", "F"};
      rows = numBytes >> 4;
      residue = numBytes & 0x0000000F;
      for ( i = 0 ; i < rows ; i++ ) {
         int hexVal = (i * ROW_BYTES);

         buff.append(hex_chars[ ((hexVal >> 12) & 15) ] +
                     hex_chars[ ((hexVal >> 8) & 15) ] +
                     hex_chars[ ((hexVal >> 4) & 15) ] +
                     hex_chars[ (hexVal & 15) ] + ": ");

         for ( j = 0 ; j < ROW_BYTES ; j++ ) {
            save_buf[j] = buf[ offset + (i * ROW_BYTES) + j ];

            buff.append(hex_chars[ (save_buf[j] >> 4) & 0x0F ] +
                        hex_chars[ save_buf[j] & 0x0F ] + " ");

            if ( j == ROW_QTR1 || j == ROW_HALF || j == ROW_QTR2 )
               buff.append(" ");

            if ( save_buf[j] < 0x20 || save_buf[j] > 0x7E )
               save_buf[j] = (byte) '.';
         }

         String saveStr = new String( save_buf, 0, j );
         buff.append(" | " + saveStr + " |\n");
      }

      if ( residue > 0 ) {
         int hexVal = (i * ROW_BYTES);

         buff.append(hex_chars[ ((hexVal >> 12) & 15) ] +
                     hex_chars[ ((hexVal >> 8) & 15) ] +
                     hex_chars[ ((hexVal >> 4) & 15) ] +
                     hex_chars[ (hexVal & 15) ] + ": ");

         for ( j = 0 ; j < residue ; j++ ) {
            save_buf[j] = buf[ offset + (i * ROW_BYTES) + j ];

            buff.append(hex_chars[ (save_buf[j] >> 4) & 0x0F ] +
                        hex_chars[ save_buf[j] & 0x0F ] + " ");

            if ( j == ROW_QTR1 || j == ROW_HALF || j == ROW_QTR2 )
              buff.append(" ");

            if ( save_buf[j] < 0x20 || save_buf[j] > 0x7E )
               save_buf[j] = (byte) '.';
         }

         for ( /*j INHERITED*/ ; j < ROW_BYTES ; j++ ) {
            save_buf[j] = (byte) ' ';
            buff.append("   ");
            if ( j == ROW_QTR1 || j == ROW_HALF || j == ROW_QTR2 )
              buff.append(" ");
         }

         String saveStr = new String( save_buf, 0, j );
         buff.append(" | " + saveStr + " |\n");
      }

      return buff.toString();

   }

   static public void main( String[] args ) {

      byte[] data = new byte[132];
      for ( int i = 0 ; i < 132 ; ++i ) data[i] = (byte)i;

      System.err.println( dumpHexData( data, 0, 132 ) );

   }

}