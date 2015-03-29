package com.kosherapp.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Hashtable;
import org.kxml.*;
import org.kxml.io.*;
import org.kxml.kdom.*;
import org.kxml.parser.*;

import android.util.Log;

public class GeoCoding {
	Hashtable							ht														= null;
	private boolean	time												= true;
	private int					count											= 0;
	private String		citystreetvalue	= null;

	public GeoCoding() {
		System.out.println("Constructor call...");
		ht = new Hashtable();
	}

	// ***************************** Find Address of the latitude Longitude
	// ***********

	public String parseXml(String address) {

		try {

			System.out.println("Address is:" + address);
			// URL mUrl = new
			// URL("http://maps.google.com/maps/api/geocode/xml?address=tagore%20road%20rajkot&sensor=false");

			URL mUrl = new URL("http://maps.google.com/maps/api/geocode/xml?address="
				+ address + "&sensor=false");

			Log.e("Url", mUrl.toString());
			HttpURLConnection mConn = (HttpURLConnection) mUrl.openConnection();

			System.out.println("After address add...");
			InputStream is = mConn.getInputStream();
			Reader reader = new InputStreamReader(is);
			XmlParser parser = new XmlParser(reader);
			traverse(parser, "");

			mConn.disconnect();
			is.close();

			System.out.println("Close all connection...");
			if (ht.get("status").toString().equals("OK")) {
				System.out.println("result is Ok..." + ht.get("locality").toString()
					+ ht.get("street").toString());
				citystreetvalue = ht.get("latitude").toString() + ","
					+ ht.get("longitude").toString() + "," + ht.get("street").toString() + ","
					+ ht.get("locality").toString();
			} else {
				System.out.println("result is not Ok...");
				citystreetvalue = "InvalidLocation";
			}

		} catch (Exception e) {
			System.out.println(e.toString());
		}

		System.out.println("before returen statement...");
		return citystreetvalue;

	}

	public void traverse(XmlParser parser, String indent) throws Exception {
		System.out.println("in Traverse method....");

		boolean leave = false;
		String title = new String();
		String desc = new String();

		do {
			ParseEvent event = parser.read();
			ParseEvent pe;
			switch (event.getType()) {
			// For example,
				case Xml.START_TAG:
					// see API doc of StartTag for more access methods
					// Pick up Title for display
					if ("status".equals(event.getName())) {
						pe = parser.read();
						title = pe.getText();
						ht.put("status", title);
					}
					if (count < 2) {
						if ("lat".equals(event.getName())) {

							pe = parser.read();
							title = pe.getText();
							ht.put("latitude", title);
							count = count + 1;

						}
						if ("lng".equals(event.getName())) {
							pe = parser.read();
							desc = pe.getText();
							ht.put("longitude", desc);
							count = count + 1;

						}
					}

					if ("long_name".equals(event.getName())) {

						pe = parser.read();
						title = pe.getText();

					}

					if ("type".equals(event.getName())) {
						pe = parser.read();
						desc = pe.getText();

						if (desc.equals("route")) {
							System.out.println("street is:" + title);
							ht.put("street", title);
						}
					}
					if ("formatted_address".equals(event.getName())) {

						if (time) {
							pe = parser.read();
							title = pe.getText();
							ht.put("address", title);
							time = false;
						}

					}

					ht.put(desc, title);

					traverse(parser, ""); // recursion call for each
					break;
				// For example </title?
				case Xml.END_TAG:
					leave = true;
					break;
				// For example
				case Xml.END_DOCUMENT:
					leave = true;
					break;
				// For example, the text between tags
				case Xml.TEXT:
					break;
				case Xml.WHITESPACE:
					break;
				default:
			}

		} while (!leave);

	}

}