/*

hostmonitor - ping sites and get emails when they go down
Copyright (C) 2022 Eric Fry

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

public class HostStats
{
  private long currentPing;
  private long lowestPing=100000;
  private long highestPing=0;
  private long totalPing;
  private long lastUpdateTime;

  private int pingQueryNum;
  private int query;

  private double uptime; // or downtime, as the case may be
  private double totalUptime;
  private double totalDowntime;

  private Host host;

  public HostStats(Host h)
  {
    lastUpdateTime = 0;
    currentPing = 0;
    totalPing = 0;
    uptime = 0;
    query = 0;
    host = h;
  }

  public void setLatency(long l)
  {
    totalPing += l;
    currentPing = l;
    if (currentPing > highestPing)
      highestPing = currentPing;
    if (currentPing < lowestPing)
      lowestPing = currentPing;
    pingQueryNum++;
  }

  public void resetLatency()
  {
    totalPing = 0;
    currentPing = 0;
    pingQueryNum = 0;
  }

  public long getPing()
  {
    return currentPing;
  }

  public double getUptime()
  {
    return uptime;
  }

  public void updateUptime()
  {
    uptime += (System.currentTimeMillis() - lastUpdateTime)/1000;
  }

  public void resetUptime()
  {
    uptime = 0;
  }

  public double getTotalUptime()
  {
    return totalUptime;
  }

  public double getTotalDowntime()
  {
    return totalDowntime;
  }

  public void query()
  {
    long msSinceUpdate = System.currentTimeMillis() - lastUpdateTime;

    if (host.isAlive())
      totalUptime += msSinceUpdate/1000;
    else
      totalDowntime += msSinceUpdate/1000;

    lastUpdateTime = System.currentTimeMillis();
    updateUptime();
    query++;
  }

  public int getQueryNum()
  {
    return query;
  }

  public long getAveragePing()
  {
    return totalPing / pingQueryNum;
  }

  public long getHighestPing()
  {
    return highestPing;
  }

  public long getLowestPing()
  {
    return lowestPing;
  }
}
