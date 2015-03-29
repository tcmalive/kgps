package com.kosherapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class JSONParser {

	public static ArrayList<HashMap<String, String>> parseJSON(String input) {
		String methodInfo = "<JSOCParser.parseJSOC(String):ArrayList<HashMap<String, String>>>";

		if (input == null) {
			return null;
		}
		Common.Log(methodInfo, String.format("input: %s", input));

		ArrayList<HashMap<String, String>> records = new ArrayList<HashMap<String, String>>();

		Pattern recordPattern = null;
		String recordPatternString = "\\{.*?\\}";
		try {
			recordPattern = Pattern.compile(recordPatternString);
		} catch (PatternSyntaxException e) {
			e.printStackTrace();
		}

		Matcher recordMatcher = null;
		recordMatcher = recordPattern.matcher(input);

		while (recordMatcher.find()) {
			Common.Log(
					methodInfo,
					String.format("recordMatcher.group(): %s",
							recordMatcher.group()));
			Pattern fieldPattern = null;
			String fieldPatternString = "\"(.*?)\"\\:\"(.*?)\"[,|}]";
			fieldPattern = Pattern.compile(fieldPatternString);

			Matcher fieldMatcher = null;
			fieldMatcher = fieldPattern.matcher(recordMatcher.group());

			HashMap<String, String> record = new HashMap<String, String>();
			Boolean fieldsFound = false;
			while (fieldMatcher.find()) {
				fieldsFound = true;
				String key = fieldMatcher.group(1);
				String value = fieldMatcher.group(2);
				Common.Log(methodInfo,
						String.format("key-value: %s-%s", key, value));
				record.put(key, value);
			}
			if (fieldsFound) {
				Common.Log(methodInfo, "RECORD ADDED");
				Common.Log(
						methodInfo,
						String.format("record.size(): %s",
								String.valueOf(record.size())));
				records.add(record);
			}
		}

		Common.Log(
				methodInfo,
				String.format("records.size(): %s",
						String.valueOf(records.size())));
		return records;
	}
}