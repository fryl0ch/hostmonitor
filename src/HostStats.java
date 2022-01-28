
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
