package com.strandls.naksha.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Common utility methods
 * 
 * @author mukund
 *
 */
public class Utils {

	private static final char CSV_SEPARATOR = ',';

	private static final Logger logger = LoggerFactory.getLogger(Utils.class);

	private Utils() {}

	public static Document convertStringToDocument(String xmlStr) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			return builder.parse(new InputSource(new StringReader(xmlStr)));
		} catch (Exception e) {
			logger.error("Error while reading xml source string- {}", e);
		}
		return null;
	}

	private static String followCVSformat(Object value) {
		if (value == null)
			return "";

		String result = value.toString();
		result = result.replace("\n", "").replace("\r", "");
		// https://tools.ietf.org/html/rfc4180
		if (result.contains("\"")) {
			result = result.replace("\"", "\"\"");
		}
		if (result.contains(",")) {
			result = "\"" + result + "\"";
		}
		return result;
	}

	public static String getCsvString(Collection<Object> values) {
		boolean first = true;

		StringBuilder sb = new StringBuilder();
		for (Object value : values) {
			if (!first)
				sb.append(CSV_SEPARATOR);

			sb.append(followCVSformat(value));
			first = false;
		}

		sb.append("\n");
		return sb.toString();
	}

	public static byte[] getCsvBytes(Collection<Object> values) {
		return getCsvString(values).getBytes();
	}

	public static void writeCsvLine(Writer w, Collection<Object> values) throws IOException {
		w.write(getCsvString(values));
	}
}
