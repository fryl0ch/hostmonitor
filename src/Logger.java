
import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.io.*;

public class Logger
{
	private File logFile;
	private Config config;
	private PingRecord pingRecord;

	public Logger(Config c)
	{
		config = c;
		logFile = config.getLogFile();
		pingRecord = new PingRecord(c);
	}

	public PingRecord getPingRecord()
	{
		return pingRecord;
	}

	public void append(String s)
	{
		try
		{
			FileWriter writer = new FileWriter(logFile, true);
			writer.write("[" + Logger.timeStamp() + "] " + s + "\n");
			writer.close();
		}
		catch (IOException e) {System.out.println("I/O Error writing to " + logFile);}
	}

    // no longer used
	public void logPing(Host h, long ping)
	{
		try
		{
			FileWriter writer = new FileWriter("logs/" + h.hostname + ".log", true);
			writer.write(ping + ",");
			writer.close();
		}
		catch (IOException e) {System.out.println("I/O Error writing to " + h.hostname + ".log");}
	}

	public static String timeStamp()
	{
		Calendar c = Calendar.getInstance();
		return c.getTime().toString();
	}

	public static Date time()
	{
		Calendar c = Calendar.getInstance();
		return c.getTime();
	}
}
