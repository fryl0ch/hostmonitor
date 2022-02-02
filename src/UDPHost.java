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