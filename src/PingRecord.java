
import java.util.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

public class PingRecord
{
	private List hosts;
	private List scans;
	private File pingLogTableFile;
	private Config config;
	public Date origin;

    /*
        The ping record consists of a list of scans:
            - a scan is a map with the hostname as a key and the ping as the data
            - a scan always contains a timestamp (Logger.time()) with "TIME" as the key
     */

	public PingRecord(Config c)
	{
		config = c;
		hosts = c.getHosts();
		scans = new ArrayList();
		pingLogTableFile = c.getPingLogTableFile();
		origin = Logger.time();
	}

	public void recordPings()
	{
		Map scan = new HashMap();
		scan.put("TIME", Logger.time());
		for (int i = 0; i < hosts.size(); i++)
		{
			Host h = (Host) hosts.get(i);
			long ping = h.getPing();
			scan.put(h.hostname, new Long(ping));
		}
		scans.add(scan);

		if (config.logPings())
			writeLog();
	}

	public List getRecordsSince(Date date, List hosts)
	{
		List records = new ArrayList();

		int startAt = 0;
		boolean done = false;
		if (scans.size() == 0)
			return records;

		/*
		System.err.println("date input: " + date);
		System.err.println("origin: " + origin);
		*/

		while (! done)
		{
			Map currentScan = (Map) scans.get(startAt);
			Date test = (Date) currentScan.get("TIME");

			if (test.after(date) || test.equals(date) || startAt + 1 == scans.size())
			{
				done = true;
			}
			else
			{
				startAt++;
			}
		}

		for (int i = startAt; i < scans.size(); i++)
		{
			records.add(scans.get(i));
		}

		return records;
	}

	public List getAllRecords()
	{
		return getRecordsSince(origin, config.getHosts());
	}

	public String getCommaTable(List records)
	{
		Map tableRows = new HashMap();

		tableRows.put("TIME", ","); // for timestamps

		for (int i = 0; i < hosts.size(); i++) // hostname row titles
		{
			Host h = (Host) hosts.get(i);
			tableRows.put(h.hostname, h.hostname + ",");
		}

		for (int s = 0; s < records.size(); s++) // timestamps
		{
			Map scan = (HashMap) records.get(s);
			String time = (String) tableRows.get("TIME");
			time += scan.get("TIME")  + ",";
			tableRows.put("TIME", time);
		}

		for (int i = 0; i < hosts.size(); i++) // ping info
		{
			Host h = (Host) hosts.get(i);
			for (int s = 0; s < records.size(); s++)
			{
				Map scan = (HashMap) records.get(s);
				String current = (String) tableRows.get(h.hostname);
				Long currentPing = (Long) scan.get(h.hostname);
				if (! currentPing.equals(new Long(-1)))
					current += currentPing.toString() + ",";
				else
					current += ",";
				tableRows.put(h.hostname, current);
			}
		}

		StringBuffer output = new StringBuffer();
		output.append((String) tableRows.get("TIME") + "\r\n");

		for (int i = 0; i < hosts.size(); i++)
		{
			Host h = (Host) hosts.get(i);
			output.append((String) tableRows.get(h.hostname) + "\r\n");
		}

		return output.toString();
	}

	private void writeLog()
	{
		List records = getRecordsSince(origin, config.getHosts());

		String excelOutput = getCommaTable(records);

		try
		{
			FileWriter writer = new FileWriter(pingLogTableFile);
			writer.write(excelOutput);
			writer.close();
		}
		catch (IOException e) {System.out.println(e);}
	}
}
