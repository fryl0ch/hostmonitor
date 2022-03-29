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
