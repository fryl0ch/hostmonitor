
import java.io.*;
import java.net.*;

/*
*      Trys to grab a page from a given HTTP URL; if it cant grab it, host is dead
*/

public class HTTPHost extends Host
{
  public static String protocol = "HTTP";

  public HTTPHost(String h, int p, Config c)
  {
    super(h, p, c, "HTTP");
  }
  public boolean checkHost()
  {

    boolean alive = false; // plan for failure

    URL url;
    URLConnection conn;
    StringBuffer buffer;
    String line;
    BufferedReader dataInput;
    long startTime = System.currentTimeMillis();
    try
    {
      url = new URL("HTTP://" + hostname);

      conn = url.openConnection();
      InputStream in = conn.getInputStream();

      buffer = new StringBuffer();
      in = conn.getInputStream();
      dataInput = new BufferedReader(new InputStreamReader(in));
      while ((line = dataInput.readLine()) != null)
      {
        buffer.append(line);
        buffer.append('\n');
      }
      //String complete = buffer.toString().trim();
      //System.out.println(complete);
      alive = true;
    }
    catch (MalformedURLException e)
    {
      System.err.println("Unable to resolve " + this);
      return false;
    }
    catch (ConnectException e)
    {
      return false;
    }
    catch (IOException e)
    {System.err.println(e);}

    if (alive)
      stats.setLatency(System.currentTimeMillis() - startTime);
    return alive;
  }

  public String currentStatus()
  {
    if (isAlive)
    {
      return this + " is alive. ping: " + stats.getPing() + "ms";
    }
    else
    {
      return this + " is dead. Last up: " + lastUptime;
    }
  }

  public String toString()
  {
    return "(" + protocol.toLowerCase() + "://" + hostname + ")";
  }
}