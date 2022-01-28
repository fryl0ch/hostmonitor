
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.io.*;

public class Config
{
	private Properties properties;
	private String configFile = "hostmonitor.conf";
	private String logFile = "hostmonitor.log";
	private String pingLogTableFile = "pingLogTable.log";
	private String SMTPServer;
	private String replyTo;
	private int scanDelay = 30; // in seconds
	private int statusUpdateDelay = 60; // in minutes
	private int UDPPort = 6688; // port to launch UDP scans from
	private int UDPTimeout = 500;
	private int waitToEmail = 3; // wait for this many consecutive true/false querys to send mail
	private boolean verbose = false;
	private boolean notifyOnDeath = true;
	private boolean notifyOnWakeup = true;
	private boolean logPings = false;
	private boolean waitForPrevious = true;
	private boolean sendStatusUpdates = true;
	private List hosts;
	private List mailTo; // email addresses

	public Config()
	{
		hosts = new ArrayList();
		mailTo = new ArrayList();
	}

	public File getLogFile()
	{
		return new File(logFile);
	}

	public File getPingLogTableFile()
	{
		return new File(pingLogTableFile);
	}

	public File getConfigFile()
	{
		return new File(configFile);
	}

	public int getUDPPort()
	{
		return UDPPort;
	}

	public int getWaitToEmail()
	{
		return waitToEmail;
	}

	public int getUDPTimeout()
	{
		return UDPTimeout;
	}

	public int getStatusUpdateDelay()
	{
		return statusUpdateDelay;
	}

	public int getScanDelay()
	{
		return scanDelay;
	}

	public List getHosts()
	{
		return hosts;
	}

	public boolean isVerbose()
	{
		return verbose;
	}

	public boolean sendStatusUpdates()
	{
		return sendStatusUpdates;
	}

	public boolean waitForPrevious()
	{
		return waitForPrevious;
	}

	public boolean notifyOnDeath()
	{
		return notifyOnDeath;
	}

	public boolean notifyOnWakeup()
	{
		return notifyOnWakeup;
	}

	public boolean logPings()
	{
		return logPings;
	}

	public List getMailTo()
	{
		return mailTo;
	}

	public String getSMTPServer()
	{
		return SMTPServer;
	}

	public String getReplyTo()
	{
		return replyTo;
	}

	public void load(String f) throws IOException
	{
		configFile = f;
		this.properties = new Properties();
		InputStream in = null;
		try
		{
				in = new FileInputStream(configFile);
				properties.load(in);
		}
		catch (FileNotFoundException e)
		{
				System.err.println("File not found: " + configFile);
				return;
		}
		catch (IOException e)
		{
				System.err.println("I/O Error when attempting to read " + configFile);
				return;
		}

		// grab values from config
		logFile = properties.getProperty("logFile", "hostmonitor.log");
		scanDelay = Integer.parseInt(properties.getProperty("scanDelay", "30"));
		UDPPort = Integer.parseInt(properties.getProperty("UDPPort", "6688"));
		UDPTimeout = Integer.parseInt(properties.getProperty("UDPTimeout", "500"));
		verbose = properties.getProperty("silent", "false").toUpperCase().equals("FALSE");
		notifyOnDeath = properties.getProperty("notifyOnDeath").toUpperCase().equals("TRUE");
		notifyOnWakeup = properties.getProperty("notifyOnWakeup").toUpperCase().equals("TRUE");
		logPings = properties.getProperty("logPings", "FALSE").toUpperCase().equals("TRUE");
		pingLogTableFile = properties.getProperty("pingLogTableFile", "pingLogTable.txt");
		waitForPrevious = properties.getProperty("waitForPrevious", "TRUE").toUpperCase().equals("TRUE");
		waitToEmail = Integer.parseInt(properties.getProperty("waitToEmail", "3"));
		SMTPServer = properties.getProperty("SMTPServer", "undefined");
		replyTo = properties.getProperty("replyTo", "replyto@example.com");
		sendStatusUpdates = properties.getProperty("sendStatusUpdates", "TRUE").toUpperCase().equals("TRUE");
		statusUpdateDelay =  Integer.parseInt(properties.getProperty("statusUpdateDelay", "60"));


		if (SMTPServer.equals("undefined") && (notifyOnWakeup || notifyOnDeath || sendStatusUpdates))
		{
			throw new IOException("If you want to send mail, you have to specify a SMTP server!");
		}

		int numHosts = 0;
		int numMail = 0;

		boolean done = false;
		String test;

		// count the hosts
		while (! done)
		{
				test = properties.getProperty("host" + (numHosts));
				if (test == null)
						done = true;
				else
						numHosts++;
		}

		done = false; // reset for mail

		// count the emails
		while (! done)
		{
				test = properties.getProperty("mail" + (numMail));
				if (test == null)
						done = true;
				else
						numMail++;
		}

		// grab protocol/hostname/port from the string
		String hostname = "";
		String protocol = "";
		int port = -1;

		if (numHosts > 0)
			for (int i = 0; i < numHosts; i++)
			{
				String hostInfo = properties.getProperty("host" + i);

				StringTokenizer hostTokens = new StringTokenizer(hostInfo, ":");

				int tokens = hostTokens.countTokens();

				if (tokens < 2)
					throw new IOException("Invalid host" + i + " definition");

				protocol = hostTokens.nextToken();
				hostname = hostTokens.nextToken();

				if (hostname.startsWith("//"))
					hostname = hostname.substring(2);

				if (hostTokens.hasMoreTokens())
					port = Integer.parseInt(hostTokens.nextToken());

				// make hosts with appropriate protocols, using default ports if undefined

				if (protocol.toUpperCase().equals("TCP"))
				{
					if (port == -1)
						port = 7;
					hosts.add(new TCPHost(hostname, port, this));
				}
				else if (protocol.toUpperCase().equals("UDP"))
				{
					if (port == -1)
						port = 7;
					hosts.add(new UDPHost(hostname, port, this));
				}
				else if (protocol.toUpperCase().equals("HTTP"))
				{
					if (port == -1)
						port = 80;
					hosts.add(new HTTPHost(hostname, port, this));
				}
				else if (protocol.toUpperCase().equals("HTTPS"))
				{
					if (port == -1)
						port = 443;
					hosts.add(new HTTPSHost(hostname, port, this));
				}
				else
					throw new IOException("Unknown protocol " + protocol.toUpperCase() + " in host" + i);
			}

		if (numMail > 0)
			for (int i = 0; i < numMail; i++)
			{
				mailTo.add(properties.getProperty("mail" + i));
			}
	}
}
