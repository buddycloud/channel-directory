/*
 * Copyright 2011 buddycloud
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.buddycloud.channeldirectory.cli;

import java.io.FileReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.buddycloud.channeldirectory.commons.ConfigurationUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author Abmar
 *
 */
public class Main {

	private static final String QUERIES_FILE = ConfigurationUtils.getChannelDirHome() 
			+ "/queries.json";
	
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws Exception {
		
		JsonElement rootElement = new JsonParser().parse(new FileReader(QUERIES_FILE));
		JsonArray rootArray = rootElement.getAsJsonArray();
		
		Map<String, Query> queries = new HashMap<String, Query>();
		
		for (int i = 0; i < rootArray.size(); i++) {
			JsonObject queryElement = rootArray.get(i).getAsJsonObject();
			String queryName = queryElement.get("name").getAsString();
			
			Query query = new Query(queryElement.get("agg").getAsString(), 
					queryElement.get("core").getAsString(), 
					queryElement.get("q").getAsString());
			
			queries.put(queryName, query);
		}
		
		LinkedList<String> queriesNames = new LinkedList<String>(queries.keySet());
		Collections.sort(queriesNames);
		
		Options options = new Options();
		options.addOption(OptionBuilder.isRequired(true)
									.withLongOpt("query")
									.hasArg(true)
									.withDescription("The name of the query. Possible queries are: " + queriesNames)
									.create('q'));
		
		options.addOption(OptionBuilder.isRequired(false)
									.withLongOpt("args")
									.hasArg(true)
									.withDescription("Arguments for the query")
									.create('a'));
		
		options.addOption(new Option("?", "help", false, "Print this message" ));
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			printHelpAndExit(options);
		}
		
		if (cmd.hasOption("help")) {
			printHelpAndExit(options);
		}
		
		String queryName = cmd.getOptionValue("q");
		String argsCmd = cmd.getOptionValue("a");
		
		Properties configuration = ConfigurationUtils.loadConfiguration();
		
		Query query = queries.get(queryName);
		if (query == null) {
			printHelpAndExit(options);
		}
		
		System.out.println(query.exec(argsCmd, configuration));
		
	}

	private static void printHelpAndExit(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("exec-query", options);
		System.exit(0);
	}
	
}
