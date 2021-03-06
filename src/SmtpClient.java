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

import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;

public class SmtpClient
{
  private String SmtpServer;
  private int SmtpPort;
  private boolean debug = true;
  private boolean connected = false;

  Socket sock;
  InputStreamReader ioReader;
  BufferedReader input;
  BufferedWriter output;
  PrintWriter ioWriter;
  int lastResponseCode;

  public SmtpClient(String server, int port)
  {
    SmtpServer = server;
    SmtpPort = port;
    lastResponseCode = -1;
  }

  public SmtpClient(String server)
  {
    SmtpServer = server;
    SmtpPort = 25; // default
    lastResponseCode = -1;
  }

  private String getResponse()
  {
    StringBuffer response = new StringBuffer();
    try
    {
    while (!input.ready()){} // wait for a response
    while (input.ready())
      response.append(input.readLine()+"\n");
    }
    catch (Exception e) {e.printStackTrace();}

    String rep = response.toString();
    lastResponseCode = Integer.parseInt(rep.substring(0,3));
    if (!rep.endsWith("\n"))
      return rep + "\n";
    else
      return rep;
  }

  private int getResponseCode()
  {
    return lastResponseCode;
  }

  private String doCommand(String command)
  {
    return doCommand(command, true);
  }

  private String doCommand(String command, boolean waitForResponse)
  {
    String response = "";

    if (!command.endsWith("\n"))
      command += "\n";

    try{
      print(command); output.write(command);
      output.flush();
      if (waitForResponse)
        response = getResponse();

    }
    catch (Exception e) {e.printStackTrace();}
    if (waitForResponse)
      print(response);
    print("S: " + getResponseCode());
    return response;
  }

  public void connect()
  {
    print("Connecting to " + SmtpServer + "... ");
    try
    {
      sock = new Socket(SmtpServer, SmtpPort);
      ioReader = new InputStreamReader(sock.getInputStream());
      input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
      output = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));
      connected = true;
      print("Successful.\n");
      //print(getResponse());
      //doCommand("EHLO");
      //doCommand("AUTH PLAIN");
      //doCommand("=");
      //doCommand("QUIT");
      doCommand("GET index.html");
      sock.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  private void print(String ln)
  {
    if (debug)
      System.out.print(ln);
  }

  public static void main(String args[])
  {
    SmtpClient bob = new SmtpClient("www.google.com", 80);
    bob.connect();
  }

  public boolean isConnected()
  {
    return connected;
  }
}
