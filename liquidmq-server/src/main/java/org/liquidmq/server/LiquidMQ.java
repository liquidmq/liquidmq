package org.liquidmq.server;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.liquidmq.MqServer;
import org.liquidmq.server.config.ConfigUtil;

import com.esotericsoftware.minlog.Log;
import com.thoughtworks.xstream.XStream;

public class LiquidMQ {
	
	public static class Options extends org.apache.commons.cli.Options {
		public Options() {
			addOption("f", "file", true, "config XML file");
		}
	}

	public static void main(String[] args) {
		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "info");
		Log.NONE();
		
		CommandLine cli;
		try {
			cli = new PosixParser().parse(new Options(), args);
		} catch(ParseException pe) {
			new HelpFormatter().printHelp("liquidmq", new Options());
			System.exit(-1);
			return;
		}

		URL config = LiquidMQ.class.getResource("liquidmq.xml");
		if(cli.hasOption("file")) {
			try {
				config = new File(cli.getOptionValue("file")).toURI().toURL();
			} catch(IOException ioe) {
			}
		}
		
		XStream x = new XStream();
		ConfigUtil.registerConverters(x);
		
		MqServer server = (MqServer) x.fromXML(config);
		try {
			server.start();
		} catch(Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
	}

}
