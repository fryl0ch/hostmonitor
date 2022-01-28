import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/*
        Checks to see if the given host is listening for UDP connections on the given port
 */

public class UDPHost extends Host
{
	public UDPHost(String h, int p, Config c)
	{
		super(h, p, c, "UDP");
	}

	protected boolean checkHost()
	{
		boolean alive = false;
		DatagramSocket dataSock = null;

		try
		{
			dataSock = new DatagramSocket(config.getUDPPort());
			SocketAddress i = new InetSocketAddress(hostname, port);
			byte[] packetData = new byte[0];
			DatagramPacket dataPacket = new DatagramPacket(packetData, packetData.length, i);
			dataSock.setSoTimeout(config.getUDPTimeout());
			dataSock.connect(i);
				dataSock.send(dataPacket); // send an empty packet
				dataSock.receive(dataPacket); // if it times out with no errors, we win
		}
		catch (SocketException e)
		{
				//System.out.println(e);
				return false; // couldn't connect
		}

		catch (SocketTimeoutException e)
		{
				return true;
		}

		catch (IOException e)
		{
				return false;
		}
		finally
		{
			dataSock.disconnect();
			dataSock.close();
			alive = true; // if it gets this far, we can assume it's alive
		}
		return alive;
	}
}