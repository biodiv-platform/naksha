package com.strandls.naksha.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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

	public static Document convertStringToDocument(String xmlStr) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

    private static String followCVSformat(Object value) {
    	if(value == null)
    		return "";

        String result = value.toString();
        result = result.replace("\n", "").replace("\r", "");
        //https://tools.ietf.org/html/rfc4180
        if (result.contains("\"")) {
            result = result.replace("\"", "\"\"");
        }
        if(result.contains(",")) {
        	result = "\"" + result + "\"";
        }
        return result;
    }

	public static void writeCsvLine(Writer w, Collection<Object> values) throws IOException {
        boolean first = true;

        StringBuilder sb = new StringBuilder();
        for (Object value : values) {
            if (!first)
                sb.append(CSV_SEPARATOR);

            sb.append(followCVSformat(value));
            first = false;
        }

        sb.append("\n");
        w.append(sb.toString());
    }
}
