import java.net.Socket;
import java.net.UnknownHostException;
import java.io.IOException;

/*
        Checks to see if the given host is listening for TCP connections on the given port
 */

public class TCPHost extends Host
{
		public static String protocol = "TCP";

		public TCPHost(String h, int p, Config c)
		{
				super(h, p, c, "TCP");
		}

		public boolean checkHost()
		{
			Socket sock = null;
			try
			{
				sock = new Socket(this.hostname, this.port);
				isAlive = sock.isConnected();
				sock.close();
			}
			catch (UnknownHostException e)
			{
				return false;
			}
			catch (IOException e)
			{
				return false;
			}
			finally
			{
				sock = null;
			}

			return isAlive;
		}
}
