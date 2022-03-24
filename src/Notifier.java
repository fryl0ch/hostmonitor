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

import java.util.*;

public class Notifier
{
  private List mailTo;
  private Config config;
  private Logger log;
  private Date lastStatusUpdate;

  public Notifier(Config c, Logger l)
  {
    config = c;
    log = l;
    mailTo = config.getMailTo();
    lastStatusUpdate = log.getPingRecord().origin;
  }

  public void statusUpdateNotification()
  {
    if (config.isVerbose())
      System.out.println("Mail: Sending status update emails");

    PingRecord pr = log.getPingRecord();
    List hosts = config.getHosts();

    for (int i = 0; i < mailTo.size(); i++)
    {
      String mailToAddress = (String) mailTo.get(i);
      log.append("Mail: sending status update to: " + mailToAddress);
      StringBuffer body = new StringBuffer();

      body.append("Current Time: " + Logger.time() + "\r\n\r\n");

      for (int s = 0; s < hosts.size(); s++)
      {
        Host h = (Host) hosts.get(s);
        body.append(h + " is " + h.stateOfBeing() + ".\r\n");
      }

      body.append("\r\nStats since last e-mail (" + config.getStatusUpdateDelay() + " minutes ago):\r\n\r\n");
      body.append(statusUpdateBody(hosts, lastStatusUpdate));
      body.append("--\r\n\r\n");
      body.append("Stats since start of observation ( " + pr.origin +" ):\r\n\r\n");
      body.append(statusUpdateBody(hosts, pr.origin));
      Message mail = new Message();
      mail.setSubject("[HostMonitor] Periodic status update");
      mail.setBody(body.toString());
      mail.setSender(config.getReplyTo());
      mail.setRecpiant(mailToAddress);
      mail.setSMTPServer(config.getSMTPServer());
      mail.addNewTextAttachment(pr.getCommaTable(pr.getRecordsSince(lastStatusUpdate, hosts)), "pingsSinceLastEmail.txt");
      mail.addNewTextAttachment(pr.getCommaTable(pr.getAllRecords()), "pingsSinceStart.txt");
      mail.send();
    }

    lastStatusUpdate = Logger.time();
  }

  private String statusUpdateBody(List hosts, Date since)
  {
    PingRecord pr = log.getPingRecord();
    List records = pr.getRecordsSince(since, config.getHosts());
    StringBuffer body = new StringBuffer();

    for (int i = 0; i < hosts.size(); i++)
    {
      Host h = (Host) hosts.get(i);

      List medianList = new ArrayList();

      int pings = records.size();

      long mean = 0;

      for (int s = 0; s < records.size(); s++)
      {
        Map scan = (Map) records.get(s);
        Long ping = (Long) scan.get(h.hostname);

        if (! ping.equals(new Long(-1)))
        {
          medianList.add(ping);
          mean += ping.longValue();
        }
        else
          pings--;
      }
      //System.err.println(h.hostname + " pings: " + pings);
      if (pings != 0)
      {
        mean = mean/pings;
        Collections.sort(medianList);
        int medianIndex = medianList.size() / 2;
        Long m = (Long) medianList.get(medianIndex);
        long median = m.longValue();

        body.append(h + ":\r\n");
        body.append("Current status: " + h.stateOfBeing() + "\r\n");
        body.append("mean: " + mean + " ms\r\n");
        body.append("median: " + median + " ms\r\n");
        body.append("highest: " + h.stats.getHighestPing() + " ms\r\n");
        body.append("lowest: " + h.stats.getLowestPing() + " ms\r\n");
        if (h.isAlive())
          body.append("most recent: " + h.getPing() + " ms\r\n");
        body.append("Succesful pings: " + pings + "\r\n");
        body.append("Unsuccesful pings: " + (records.size() - pings) + "\r\n");
        body.append("\r\n");
      }
      else
      {
        body.append(h + " has been down for the entire reporting period; no stats to report\r\n\r\n");
      }
    }

    return body.toString();
  }

  public void notify(Host h)
  {
    //PingRecord pr = log.getPingRecord();

    for (int i = 0; i < mailTo.size(); i++)
    {
      String mailToAddress = (String) mailTo.get(i);

      log.append("Mail: sending to " +mailToAddress +": " + h.hostname + " status change");

      if (config.isVerbose())
        System.out.println("Mail: Sending email to " + mailToAddress);

      //System.out.println("mailto:" + h);
      //sendMail(mailToAddress, "[HostMonitor] Status update: " + h.hostname, h.currentStatus());
      Message mail = new Message();
      mail.setSender(config.getReplyTo());
      mail.setRecpiant(mailToAddress);
      mail.setSMTPServer(config.getSMTPServer());
      mail.setSubject("[HostMonitor] status change on: " + h.hostname);
      mail.setBody("Current Time: " + Logger.timeStamp() + "\r\n" + h.currentStatus());
      //mail.addTextAttachment(pr.getCommaTable(pr.getAllRecords()), "record.txt");
      //mail.addTextAttachment(pr.getCommaTable(pr.getAllRecords()), "record2.txt");
      mail.send();
    }
  }
}
