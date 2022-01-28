import java.util.*;
import java.io.IOException;

public class HostMonitor
{
	public static void main(String args[])
	{
		Config config = new Config();
		try
		{
			config.load("hostmonitor.conf");
		}
		catch (IOException e)
		{
			System.err.println("Error loading config: " + e.getMessage());
			System.exit(1);
		}
		HostMonitor monitor = new HostMonitor(config);
        System.out.println("HostMonitor 0.1.1");
        System.out.println("Scanning " + config.getHosts().size() + " hosts every " + config.getScanDelay() + " seconds");
        System.out.println("Logging response times to: " + config.getPingLogTableFile());
        System.out.println("Logging host status changes to: " + config.getLogFile());
		monitor.scan(); // start scanning
	}

	private List hosts = new ArrayList();
	private static Timer timer = new Timer();
	private int delay = 30;
	private int scans = 0;
	private Logger log;
	private Config config;
	private Notifier notifier;
	private boolean waitForPrevious;

	public HostMonitor(Config c)
	{
		config = c;
        log = new Logger (config);
		notifier = new Notifier (config, log);
		hosts = config.getHosts();
		delay = config.getScanDelay();
		waitForPrevious = config.waitForPrevious();
		if (config.sendStatusUpdates())
		{
			//new StatusUpdate(config.statusUpdateDelay(), notifier).run();
			timer.schedule(new StatusUpdate(config.getStatusUpdateDelay(), notifier), config.getStatusUpdateDelay()*1000*60);
		}

	}

	public void scan()
  {
		new HostScan(this, delay, waitForPrevious).run();
	}

	// --==::||::==--==::||::==--==::||::==--==::||::==--==::||::==--
	class HostScan extends TimerTask
	{
		HostMonitor scanner;
		int scanDelay;
		boolean wait;

		public HostScan(HostMonitor h, int s, boolean w)
		{
			super();
			scanner = h;
			scanDelay = s;
			wait = w;
		}
    public void run()
    {
	    if (! wait)
	      timer.schedule(new HostScan(scanner,scanDelay, wait), scanDelay*1000);

	    scanner.testHosts();

	    if (wait)
	      timer.schedule(new HostScan(scanner,scanDelay, wait), scanDelay*1000);
    }
	}
	// --==::||::==--==::||::==--==::||::==--==::||::==--==::||::==--

	class StatusUpdate extends TimerTask
	{
		Notifier n;
		int minutesToWait;

		public StatusUpdate(int min, Notifier n)
		{
			super();
			notifier = n;
			minutesToWait = min;
		}
    public void run()
    {
	    notifier.statusUpdateNotification();
	    timer.schedule(new StatusUpdate(minutesToWait, notifier), minutesToWait*1000*60);
    }
	}
	// --==::||::==--==::||::==--==::||::==--==::||::==--==::||::==--


	public void testHosts()
	{
		scans++;
		if (config.isVerbose())
			System.out.println("-- Scan #" + scans + " started " + Logger.timeStamp());
		for (int i = 0; i < hosts.size(); i++)
		{
			Host h = (Host) hosts.get(i);

			boolean isAlive = h.updateStatus();

			if (h.statusHasChanged() && ! isAlive) // host goes down
			{
				if (config.notifyOnDeath())
					h.setPrepareToMail(true);

				log.append(h.currentStatus());

				if (config.isVerbose())
						System.out.println("[" + Logger.timeStamp() + "] " + h + " has just DIED");
			}
			else if (h.statusHasChanged() && isAlive) // host comes up
			{
				if (config.notifyOnWakeup() )
					h.setPrepareToMail(true);

				log.append(h.currentStatus());

				if (config.isVerbose())
						System.out.println("[" + Logger.timeStamp() + "] " + h + " is now ALIVE");
			}
			else // no change
			{
				if (config.isVerbose())
					System.out.println("[" + Logger.timeStamp() + "] " + h.currentStatus());
			}

			if (h.prepareToMail)
			{
				if (h.getStatusUnchangedFor() == config.getWaitToEmail())
				{
					if (config.isVerbose())
						System.out.println("Mail: " + h.hostname + " status stable for " + config.getWaitToEmail() + " scans; sending email notifications");
					notifier.notify(h);
				}
			}
		}
		log.getPingRecord().recordPings();
	}
}