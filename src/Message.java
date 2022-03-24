/*

hostmonitor - ping sites and get emails when they go down
Copyright (C) 2004-2022 Eric Fry

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

import java.net.URL;
import java.net.URLConnection;
import java.io.*;

public class Message
{
  private String sender;
  private String recipiant;
  private String subject;
  private String body;
  private StringBuffer attachments = new StringBuffer();
  private String SMTPServer;

  //  www.planettribes.com/allyourbase
  private String boundry = "allYourBaseAreBelongToUs" + System.currentTimeMillis();

  private String makeHeader()
  {
    String header = "MIME-Version: 1.0\n";
    header += "Return-Path: " + sender + "\n";
    header += "From: " + sender + "\n";
    header += "To: " + recipiant + "\n";
    header += "Subject: " + subject + "\n";
    if (attachments.toString().length() != 0)
    {
      header += "Content-Type: multipart/mixed; boundary=\"" + boundry +"\"\n";
      header += "--" + boundry + "\nContent-Type: text/plain;charset=\"iso-8859-1\"\n";
      header += "Content-Transfer-Encoding: 7bit\n";
    }
    else
      header += "Content-Type: text/plain; charset=\"iso-8859-1\"\n";

    header += "\n";
    return header;
  }

  public void setBody(String body)
  {
    this.body = body;
  }

  public void setSMTPServer(String SMTPServer)
  {
    this.SMTPServer = SMTPServer;
  }

  public void setSender(String sender)
  {
    this.sender = sender;
  }

  public void setSubject(String subject)
  {
    this.subject = subject;
  }

  public void setRecpiant(String recipiant)
  {
    this.recipiant = recipiant;
  }

  public void addNewTextAttachment(String text, String filename)
  {
    attachments.append("--" + boundry + "\n");
    attachments.append("Content-Type: text/plain;name=\"" + filename + "\"\n");
    attachments.append("Content-Disposition: attachment; filename=\"" + filename + "\"\n");
    attachments.append("Content-Transfer-Encoding: quoted-printable\n\n");
    attachments.append(text + "\n");
  }

  public void addFileAttachment(String path)
  {
    File file = new File(path);
    String filename = file.getName();

    attachments.append("--" + boundry + "\n");
    attachments.append("Content-Type: application/octet-stream;name=\"" + filename + "\"\n");
    attachments.append("Content-Disposition: attachment; filename=\"" + filename + "\"\n");
    attachments.append("Content-Transfer-Encoding: base64\n\n");
    attachments.append(Base64.encode(Base64.getByteArray(path)) + "\n");

  }

  public void send()
  {
    if (SMTPServer == null)
    {
      System.err.println("Specify a SMTP server before trying to send mail!");
      return;
    }

    if (attachments.toString().length() != 0)
      attachments.append("--" + boundry + "--"); // end file

    StringBuffer finalMessage = new StringBuffer(); // start building final message

    finalMessage.append(this.makeHeader());         // grab the header
    finalMessage.append(body + "\n");               // grab the body
    finalMessage.append(attachments.toString());    // grab any attachments

    // Send the message
    URL url;
    URLConnection urlConnection;
    try
    {
      //System.getProperties().put("mail.host", SMTPServer);
      System.getProperties().put("mail.host", SMTPServer);
      System.getProperties().put("mail.smtp.auth", "true");
      url = new URL("mailto:" + recipiant);
      urlConnection  = url.openConnection();
      urlConnection.setDoInput(false);
      urlConnection.setDoOutput(true);
      urlConnection.connect();
      PrintWriter mail = new PrintWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
      mail.println(finalMessage.toString());
      mail.close();
    }
    catch (Exception e)
    {
      System.err.println("Error sending mail (" + subject + ") to " + recipiant + "\n");
      e.printStackTrace();
    }
  }
}
