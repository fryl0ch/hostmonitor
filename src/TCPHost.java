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
