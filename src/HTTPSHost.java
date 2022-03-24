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

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.*;
import java.security.cert.X509Certificate;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;
import javax.net.ssl.*;

/*
 *      Trys to grab a page from a given HTTPS URL; if it cant grab it, host is dead
 */

public class HTTPSHost extends Host
{
		public static String protocol = "HTTPS";

		public HTTPSHost(String h, int p, Config c)
		{
				super(h, p, c, "HTTPS");
		}
		public boolean checkHost()
		{
			long startTime = System.currentTimeMillis();
			boolean alive = false; // plan for failure
			try
			{
				URL url;
                StringBuffer buffer;
                String line;

                // ###########
                SSLContext sc = SSLContext.getInstance("SSLv3");
                TrustManager[] tma = { new TrustManager() };
                sc.init(null, tma, null);
                SSLSocketFactory ssf = sc.getSocketFactory();
                HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
                // ###########
                HttpsURLConnection connection = null;
                InputStream input;
                BufferedReader dataInput;
                String nurl = "HTTPS://" + hostname;
                try
                {
                    url = new URL(nurl);
                    HostnameVerifier hv = new HostnameVerifier()
	                {
                        public boolean verify(String urlHostName, SSLSession session)
                        {
                            return true;
                        }
			    	};

					HttpsURLConnection.setDefaultHostnameVerifier(hv);
		            connection = (HttpsURLConnection) url.openConnection();

                }
                catch (Exception e)
                {
                    System.err.println(e);
                }

                try
                {
                    buffer = new StringBuffer();
                    input = connection.getInputStream();
                    dataInput = new BufferedReader(new InputStreamReader(input));
                    while ((line = dataInput.readLine()) != null)
                    {
                        buffer.append(line);
                        buffer.append('\n');
                    }
                    alive = true;
                }
                catch (IOException e)
                {
				    return false; // can't open streams; dead
                }
                catch (Exception e)
                {
                    System.err.println(e);
                }
		    }
			catch (NoSuchAlgorithmException e)
            {
                System.out.println(e);
            }
			catch (KeyManagementException e)
            {
                System.out.println(e);
            }

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

    //,.-'`'-.,.-'`'-.,.-'`'-.,.-'`'-.,.-'`'-.,.-'`'-.,.-'`'-.,.-'`'-.,.-'`'-.,.-'`'-.,.-'`'-.\\
			// don't worry about certificates.. we just want to get the page
		class TrustManager implements javax.net.ssl.X509TrustManager
		{
	        // TrustManager Methods
            public void checkClientTrusted(X509Certificate[] chain, String authType)
            {}

            public void checkServerTrusted(X509Certificate[] chain, String authType)
            {}

            public X509Certificate[] getAcceptedIssuers()
            {
                return null;
            }
		}
    //,.-'`'-.,.-'`'-.,.-'`'-.,.-'`'-.,.-'`'-.,.-'`'-.,.-'`'-.,.-'`'-.,.-'`'-.,.-'`'-.,.-'`'-.\\
}
