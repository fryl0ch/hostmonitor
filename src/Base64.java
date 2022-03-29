/*

hostmonitor - ping sites and get emails when they go down
Copyright (C) 2004 Eric Fry

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

*/

import java.io.*;

// theres' a bunch of crap in here that i was using to try to fix the decode.
// i never managed to fix it, but it's not used in this program anyway.


public class Base64
{
  // make the encoding table
  static byte[] encodingTable = new byte[64];

  private static void fillEncodingTable()
  {
    // start to fill the encoding table
    int tablePos = 0;
    for (int i = 65; i <= 90; i++)
      encodingTable[tablePos++] = (byte) i; // A - Z

    for (int i = 97; i <= 122; i++)
      encodingTable[tablePos++] = (byte) i; // a - z

    for (int i = 48; i <= 57; i++)
      encodingTable[tablePos++] = (byte) i; // 0 - 9

    encodingTable[tablePos++] = 43; // +
    encodingTable[tablePos++] = 47; // /
  }

  public static String encode(byte[] input)
  {
    StringBuffer encodedInput = new StringBuffer();

    // fill the encoding table
    fillEncodingTable();

    // check if input size is a multiple of 3
    int padding = 0;

    if (input.length % 3 != 0)
      while ((input.length + padding) % 3 != 0)
        padding++;

    if (padding > 0) // then we need to resize the byte array
    {
      byte[] newInput = new byte[input.length + padding];
      int newInputPos = 0;

      for (int i = 0; i < input.length; i++) // fill with old array
        newInput[newInputPos++] = input[i];

      for (int i = 0; i < padding; i++) // add padding
        newInput[newInputPos++] = 0;

      input = newInput;
    }

    int inputPos = 0;

    // start the encoding loop
    while (inputPos < input.length)
    {

      byte[] b = new byte[3]; // source bytes (8-bit)
      byte[] s = new byte[4]; // result bytes (6-bit)

      // grab the input bytes
      b[0] = input[inputPos++];
      b[1] = input[inputPos++];
      b[2] = input[inputPos++];

      // shift 3 8-bit bytes into 4 6-bit groups
      s[0] = (byte) (63 & (b[0] >>> 2));
      s[1] = (byte) (63 & (((b[0] << 4) & 48) | ((b[1] >>> 4) &  15)));
      s[2] = (byte) (63 & (((b[1] << 2) & 60) | ((b[2] >>> 6) & 3)));
      s[3] = (byte) (63 & (b[2]));

      // encode and append to final output
      encodedInput.append((char) encodingTable[(int)s[0]]);
      encodedInput.append((char) encodingTable[(int)s[1]]);
      encodedInput.append((char) encodingTable[(int)s[2]]);
      encodedInput.append((char) encodingTable[(int)s[3]]);

      if (inputPos % 20 == 0)
        encodedInput.append("\n");

    }

    // if we needed to pad, replace the last 1 or 2 chars with an = or ==
    encodedInput.delete(encodedInput.length() - padding, encodedInput.length());
    if (padding != 0)
      for (int i = 0; i < padding; i++)
        encodedInput.append("=");

    // return encoded input
    return encodedInput.toString();
  }

  public static void decodeToFile(String input, File file)
  {
    byte[] decodedInput = decode(input);

    try
    {
      FileOutputStream fo = new FileOutputStream(file);
      fo.write(decodedInput);
    }
    catch (FileNotFoundException e) {e.printStackTrace();}
    catch (IOException e) {e.printStackTrace();}
  }

  public static byte[] decode(String input)
  {
    // fill encoding table
    fillEncodingTable();

    // position pointers
    int inputPos = 0;
    int outputPos = 0;

    // hexout
    int rowLength = 8;
    int curCol = 0;

    // decoded byte array

    byte[] in = new byte[input.length()];

    for (int i = 0; i < input.length(); i++)
      in[i] = (byte) input.charAt(i);

    System.out.println("in length:" + in.length);

    byte[] decodedInput = new byte[in.length];

    // start the decoding loop
    while (inputPos < in.length)
    {
      if (in[inputPos++] != 12)
      {

        // grab the 4 6-bit groups
        byte[] s = new byte[4];

        s[0] = in[inputPos++];
        s[1] = in[inputPos++];
        s[2] = in[inputPos++];
        s[3] = in[inputPos++];

        int padding = 0;

        // check for padding
        if ((char) s[3]=='=')
          if ((char) s[2]=='=')
            padding = 2;
          else
            padding = 1;
/*
        // replace padding with 0s
        if (padding != 0)
          for (int i = 0; i < padding; i++)
            s[s.length-i-1] = 0;*/

        // get the encoded value of each 6-bit group
        for (int i = 0; i < s.length; i++)
          for (int t = 0; t < encodingTable.length; t++)
            if ((char) s[i] == (char) encodingTable[t])
              s[i] = (byte) (63 & t);

        for (int i=0;i<s.length;i++)
        {
          System.out.print(toHex(s[i]) + " ");
          if (curCol++ >= rowLength)
          {
            curCol = 0;
            System.out.println();
          }
        }

        // grab the 3 bytes from the 4 6-bit groups
        decodedInput[outputPos++] = (byte) (((s[0] << 2) & 252) | ((s[1] >>> 4) & 3));
        decodedInput[outputPos++] = (byte) (((s[1] << 4) & 240) | ((s[2] >>> 2) & 15));
        decodedInput[outputPos++] = (byte) (((s[2] << 6) & 192) | (s[3] & 63));
      }
    }

    // remove all trailing zeros (padding)
    int zeros = 0;
    int pos  = decodedInput.length - 1;
    boolean looking = true;
    while (looking)
      if (decodedInput[pos--] == 0) zeros++;
      else looking = false;
    byte[] di = new byte[decodedInput.length - zeros];
    for (int i = 0; i < di.length; i ++)
      di[i] = decodedInput[i];
    decodedInput = di;
    return decodedInput;
  }

  private static String toHex(byte b)
  {
    String hex = "";

    int val = (int) b;
    if (val < 0)
      val += 256;
    int ho = val / 16;
    if (ho > 9)
      hex += (char)((ho - 10) + 65);
    else
      hex += ho;
    int lo = val % 16;
    if (lo > 9)
      hex += (char)((lo - 10) + 65);
    else
      hex += lo;
    return hex;
  }

  public static void test(String path)
  {
    System.out.println("Loading file " + path);
    byte[] originalFile = getByteArray(path);
    System.out.println("Encoding File...");
    String encodedFile = encode(originalFile);
    System.out.println("Decoding File...");
    byte[] decodedFile = decode(encodedFile);

    System.out.println("Original File: " + originalFile.length + " bytes");
    System.out.println("Decoded File: " + decodedFile.length + " bytes");

    if (originalFile.length == decodedFile.length)
    {
      System.out.println("Comparing files:");

      int[] ho = new int[3]; // high order difference
      int[] lo = new int[3]; // low order difference

      for (int i = 0; i < originalFile.length; i++)
      {
        if (originalFile[i] != decodedFile[i])
        {
          String b1 = Base64.toHex(originalFile[i]);
          String b2 = Base64.toHex(decodedFile[i]);

          int byteNum = (i % 3);

          if (b1.charAt(0) != b2.charAt(0))
            ho[byteNum]++;
          if (b1.charAt(1) != b2.charAt(1))
            lo[byteNum]++;
        }
      }

      for (int i = 0; i < 3; i++)
        System.out.println("Byte " + (i+1) + ": ho: " + ho[i] + " lo: " + lo[i]);
    }
  }

  public static byte[] getByteArray(String path)
  {
    InputStream in = null;
    byte[] fileByteArray = new byte[0];
    File file;

    try
    {
      file = new File(path);
      long bytes = file.length();
      fileByteArray = new byte[(int)bytes];
      in = new FileInputStream(file);
      for (int i = 0; i < bytes; i++)
      {
        fileByteArray[i] = (byte) in.read();
      }
    }
    catch (FileNotFoundException e)
    {
      System.err.println("File not found: " + path);
    }
    catch (IOException e)
    {
      System.err.println("I/O Error when attempting to read " + path);
    }

    return fileByteArray;
  }

  class ByteComparison
  {
    byte byte1;
    byte byte2;

    public boolean highOrderDifference;
    public boolean lowOrderDifference;

    public ByteComparison(byte b1, byte b2)
    {
      byte1 = b1;
      byte2 = b2;
      compare();
    }

    private void compare()
    {
      String b1 = Base64.toHex(byte1);
      String b2 = Base64.toHex(byte2);

      if (b1.charAt(0) != b2.charAt(0))
        highOrderDifference = true;
      else
        highOrderDifference = false;
      if (b1.charAt(1) != b2.charAt(1))
        lowOrderDifference = true;
      else
        lowOrderDifference = false;
    }

  }

  public static void main(String[] args)
  {
    int fileArg = 0;
    String file = args[0];
    if (args.length < 1)
    {
      System.err.println("Usage: Base64 (-t) <file>");
      System.exit(1);
    }
    else if (args.length >= 1)
    {
      file = args[fileArg];

      //file.replaceAll ("\\","\\\\"); // make escape \s
      if (args[0].startsWith("-t"))
      {
        file = "C:\\index.html";
      }
    }

    System.out.print(Base64.encode(Base64.getByteArray(file))+"\n");
    System.out.println(Base64.decode(Base64.encode(Base64.getByteArray(file)))+ "\n");
  }
}
