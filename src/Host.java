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

public abstract class Host
{
  protected String hostname;
  protected String lastUptime;
  protected int port;
  protected int statusUnchangedFor;
  protected boolean isAlive;
  protected boolean previousStatus;
  protected boolean statusChanged;
  protected boolean prepareToMail = false;
  protected Config config;
  protected HostStats stats;

  protected String protocol;

  public Host(String h, int p, Config c, String pr)
  {
    hostname = h;
    port = p;
    lastUptime = "never";
    previousStatus = false;
    isAlive = false;
    config = c;
    protocol = pr;
    stats = new HostStats(this);
  }

  public void setLastUptime(String s)
  {
    lastUptime = s;
  }

  public void setPort(int p)
  {
    port = p;
  }

  public void setHostname(String h)
  {
    hostname = h;
  }

  public boolean statusHasChanged()
  {
    return statusChanged;
  }

  public String getLastUptime()
  {
    return lastUptime;
  }

  public String stateOfBeing()
  {
    if (isAlive)
      return "alive";
    else
      return "DEAD";
  }

  public int getPort()
  {
    return port;
  }

  public int getStatusUnchangedFor()
  {
    return statusUnchangedFor;
  }

  public long getPing()
  {
    if (isAlive)
      return stats.getPing();
    else
      return -1;
  }

  public String getHostname()
  {
    return hostname;
  }

  public boolean isAlive()
  {
    return isAlive;
  }

  public boolean prepareToMail()
  {
    return prepareToMail;
  }

  public void setPrepareToMail(boolean b)
  {
    prepareToMail = b;
  }

  public boolean updateStatus() // true if alive, false if dead
  {
    previousStatus = isAlive;
    isAlive = checkHost();

    if (stats.getQueryNum() != 0)
    {
      if(isAlive != previousStatus)
      {
        statusChanged = true;
        statusUnchangedFor = 0;
        stats.resetUptime();
      }
      else
      {
        statusChanged = false;
        stats.updateUptime();
        statusUnchangedFor++;
      }
    }

    if (isAlive)
      lastUptime = Logger.timeStamp();

    stats.query();

    return isAlive;
  }

  abstract boolean checkHost();

  public String currentStatus()
  {
    if (isAlive)
    {
      return this + " is alive.";
    }
    else
    {
      return this + " is dead. Last up: " + lastUptime;
    }
  }

  public String toString()
  {
    return "(" + hostname + ":" + port + ")";
  }
}