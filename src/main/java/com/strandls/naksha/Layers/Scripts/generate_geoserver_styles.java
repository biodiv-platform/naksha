/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strandls.naksha.Layers.Scripts;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang.StringUtils;

import com.strandls.naksha.NakshaConfig;

/**
 *
 * @author humeil
 */
public class generate_geoserver_styles {

	private static String create_mgl_rule(String key, String value, String layer_type, String col_type,
			boolean lastRule) {

		String rule_mgl = null;
		if (col_type.startsWith("character")) {
			key = String.format("\"%s\"", key);
		}
		if (layer_type.equals("MULTIPOLYGON")) {

			rule_mgl = String.format(polygon_rule_tpl_mgl, key, value);
		} else if (layer_type.equals("POINT") || layer_type.equals("MULTIPOINT")) {
			rule_mgl = String.format(point_rule_tpl_mgl, key, value);
		} else if (layer_type.equals("MULTILINESTRING")) {
			rule_mgl = String.format(line_rule_tpl_mgl, key, value);
		}
		if (lastRule == true) {
			rule_mgl = rule_mgl.substring(0, rule_mgl.length());
		}
		return rule_mgl;
	}

	static String dirname = NakshaConfig.getString("tmpDir.path") + "layersqls/";
	static List<String> layers = new ArrayList<>();
	static String tmp_dir_path = NakshaConfig.getString("tmpDir.path");
	private static List<File> files = new ArrayList<>();
	static String geoserver_data_dir_path = NakshaConfig.getString("tmpDirGeoserverPath");

	static final double golden_ratio_conjugate = 0.618033988749895;

	static String xml_tpl = "<style>\n" + "<id>%s</id>\n" + "<name>%s</name>\n" + "<filename>%s</filename>\n"
			+ "<jsonfilename>%s</jsonfilename>\n" + "</style>";

	static String header_tpl_mgl = "{\n" + "\"version\": 8,\n" + "\"sources\": {\n" + "\"%s\": {\n"
			+ "\"type\": \"vector\",\n" + "\"scheme\": \"tms\",\n"
			+ "\"tiles\": [\"/geoserver/gwc/service/tms/1.0.0/humeil:%s@EPSG%%3A900913@pbf/{z}/{x}/{y}.pbf\"]\n" + "}\n"
			+ "},\n" + "\"layers\": [{\n" + "\"id\":\"%s\",\n" + "\"type\":\"%s\",\n" + "\"source\":\"%s\",\n"
			+ "\"source-layer\": \"%s\",\n" + "\"paint\": ";

	static String paint_tpl_fill_mgl = "{\n" + "\"fill-outline-color\": \"#aaaaaa\",\n" + "\"fill-opacity\": 0.5,\n"
			+ "\"fill-color\": {\n" + "\"property\": \"%s\",\n" + "\"type\": \"%s\",\n" + "\"stops\": [";

	static String paint_tpl_circle_mgl = "{\n" + "\"circle-radius\": 5,\n" + "\"circle-opacity\": 0.5,\n"
			+ "\"circle-color\": {\n" + "\"property\": \"%s\",\n" + "\"type\": \"%s\",\n" + "\"stops\": [";

	static String paint_tpl_line_mgl = "{\n" + "\"line-width\": 1,\n" + "\"line-color\": {\n"
			+ "\"property\": \"%s\",\n" + "\"type\": \"%s\",\n" + "\"stops\": [";

	static String polygon_rule_tpl_mgl = "" + "[%s, \"#%s\"],";

	static String point_rule_tpl_mgl = "" + "[%s, \"#%s\"],";

	static String line_rule_tpl_mgl = "" + "[%s, \"#%s\"],";

	static String footer_tpl_mgl = "\n" + "]\n" + "}\n" + "}\n" + "}]\n" + "}";

	static String header_tpl = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?\n"
			+ "<StyledLayerDescriptor version=\"1.0.0\"\n"
			+ "xsi:schemaLocation=\"http://www.opengis.net/sld StyledLayerDescriptor.xsd\"\n"
			+ "xmlns=\"http://www.opengis.net/sld\"\n" + "xmlns:ogc=\"http://www.opengis.net/ogc\"\n"
			+ "xmlns:xlink=\"http://www.w3.org/1999/xlink\"\n"
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + "<NamedLayer\n"
			+ "<Name>Attribute-based polygon</Name\n" + "<UserStyle>\n" + "<Title><![CDATA[%s]]></Title\n"
			+ "<FeatureTypeStyle>\n";

	static String point_rule_tpl = "" + "<Rule>\n" + "<Name><![CDATA[%s]]></Name>\n" + "<Title><![CDATA[%s]]></Title>\n"
			+ "<ogc:Filter>\n" + "<ogc:And>\n" + "<ogc:PropertyIsGreaterThanOrEqualTo>\n"
			+ "<ogc:PropertyName>%s</ogc:PropertyName>\n" + "<ogc:Literal>%s</ogc:Literal>\n"
			+ "</ogc:PropertyIsGreaterThanOrEqualTo>\n" + "<ogc:PropertyIsLessThan>\n"
			+ "<ogc:PropertyName%s</ogc:PropertyName>\n" + "<ogc:Literal>%s</ogc:Literal>\n"
			+ "</ogc:PropertyIsLessThan>\n" + "</ogc:And>\n" + "</ogc:Filter>\n" +

			"<PointSymbolizer>\n" + "<Graphic>\n" + "<Mark>\n" + "<WellKnownName>circle</WellKnownName>\n" + "<Fill>\n"
			+ "<CssParameter name=\"fill\">\n" + "#<ogc:Function name=\"env\">\n" + "<ogc:Literal>%s</ogc:Literal>\n"
			+ "<ogc:Literal>%s</ogc:Literal>\n" + "</ogc:Function>\n" + "</CssParameter>\n" + "</Fill>\n" + "<Stroke>\n"
			+ "<CssParameter name=\"stroke\">\n" + "#<ogc:Function name=\"env\">\n"
			+ "<ogc:Literal>stroke</ogc:Literal>\n" + "<ogc:Literal>%s</ogc:Literal>\n" + "</ogc:Function>\n"
			+ "</CssParameter>\n" + "<CssParameter name=\"stroke-width\">\n" + "<ogc:Function name=\"env\">\n"
			+ "<ogc:Literal>stroke-width</ogc:Literal>" + "<ogc:Literal>%s</ogc:Literal>\n" + "</ogc:Function>\n"
			+ "</CssParameter>\n" + "</Stroke>\n" + "</Mark>\n" + "<Size>\n" + "<ogc:Function name=\"env\">\n"
			+ "<ogc:Literal>size</ogc:Literal>\n" + "<ogc:Literal>%s</ogc:Literal>\n" + "</ogc:Function>\n"
			+ "</Size>\n" + "</Graphic>\n" + "</PointSymbolizer>\n" + "</Rule>";

	static String polygon_rule_tpl = "" + "<Rule>\n" + "<Name><![CDATA[%s]]></Name>\n"
			+ "<Title><![CDATA[%s]]></Title>\n" + "<ogc:Filter>\n" + "<ogc:And>\n"
			+ "<ogc:PropertyIsGreaterThanOrEqualTo>\n" + "<ogc:PropertyName>%s</ogc:PropertyName>\n"
			+ "<ogc:Literal>%s</ogc:Literal>\n" + "</ogc:PropertyIsGreaterThanOrEqualTo>\n"
			+ "<ogc:PropertyIsLessThan>\n" + "<ogc:PropertyName>%s</ogc:PropertyName>\n"
			+ "<ogc:Literal>%s</ogc:Literal>\n" + "</ogc:PropertyIsLessThan>\n" + "</ogc:And>\n" + "</ogc:Filter>\n"
			+ "<PolygonSymbolizer>\n" + "<Fill>\n" + "<CssParameter name=\"fill\">\n" + "#<ogc:Function name=\"env\">\n"
			+ "<ogc:Literal>%s</ogc:Literal>\n" + "<ogc:Literal>%s</ogc:Literal>\n" + "</ogc:Function>\n"
			+ "</CssParameter>\n" + " </Fill>\n" + "<Stroke>\n" + "<CssParameter name=\"stroke\">\n"
			+ " #<ogc:Function name=\"env\">\n" + "<ogc:Literal>stroke</ogc:Literal>\n"
			+ "<ogc:Literal>%s</ogc:Literal>\n" + "</ogc:Function>\n" + " </CssParameter>\n"
			+ "<CssParameter name=\"stroke-width\">\n" + "<ogc:Function name=\"en\">\n"
			+ "<ogc:Literal>stroke-width</ogc:Literal>\n" + "<ogc:Literal>%s</ogc:Literal>\n" + "</ogc:Function>\n"
			+ "</CssParameter>\n" + "</Stroke>\n" + "</PolygonSymbolizer>\n" + "</Rule>";

	static String point_cat_rule_tpl = "" + "<Rule>\n" + "<Name><![CDATA[%s]]></Name>\n"
			+ "<Title><![CDATA[%s]]></Title>\n" + "<ogc:Filter>\n" + "<ogc:PropertyIsEqualTo>\n"
			+ "<ogc:PropertyName>%s</ogc:PropertyName>\n" + "<ogc:Literal><![CDATA[%s]]></ogc:Literal>\n"
			+ "</ogc:PropertyIsEqualTo>\n" + "</ogc:Filter>\n" +

			"<PointSymbolizer>\n" + "<Graphic>\n" + "<Mark>\n" + "<WellKnownName>circle</WellKnownName>\n" + "<Fill>\n"
			+ "<CssParameter name=\"fill\">\n" + "#<ogc:Function name=\"env\">\n" + "<ogc:Literal>%s</ogc:Literal>\n"
			+ "<ogc:Literal>%s</ogc:Literal>\n" + "</ogc:Function>" + "</CssParameter>" + "</Fill>" +

			"<Stroke>\n" + "<CssParameter name=\"stroke\">\n" + "#<ogc:Function name=\"env\">\n"
			+ "<ogc:Literal>stroke</ogc:Literal>\n" + "<ogc:Literal>%s</ogc:Literal>\n" + "</ogc:Function>\n"
			+ "</CssParameter>\n" + "<CssParameter name=\"stroke-width\">\n" + "<ogc:Function name=\"env\">\n"
			+ "<ogc:Literal>stroke-width</ogc:Literal>\n" + "<ogc:Literal>%s</ogc:Literal>\n" + "</ogc:Function>\n"
			+ "</CssParameter>\n" + "</Stroke>\n" + "</Mark>\n" + "<Size>\n" + "<ogc:Function name=\"env\">\n"
			+ "<ogc:Literal>size</ogc:Literal>\n" + "<ogc:Literal>%s</ogc:Literal>\n" + "</ogc:Function>\n"
			+ " </Size>\n" + "</Graphic>\n" + "</PointSymbolizer>\n" + "</Rule>";

	static String polygon_cat_rule_tpl = "" + "<Rule>\n" + "<Name><![CDATA[%s]]></Name>\n"
			+ "<Title><![CDATA[%s]]></Title>\n" + "<ogc:Filter>\n" + "<ogc:PropertyIsEqualTo>\n"
			+ "<ogc:PropertyName>%s</ogc:PropertyName>\n" + "<ogc:Literal><![CDATA[%s]]></ogc:Literal>\n"
			+ " </ogc:PropertyIsEqualTo>\n" + "</ogc:Filter>\n" + "<PolygonSymbolizer>\n" + "<Fill>\n"
			+ "<CssParameter name=\"fil\">\n" + "#<ogc:Function name=\"env\">\n" + "<ogc:Literal>%s</ogc:Literal>\n"
			+ "<ogc:Literal>%s</ogc:Literal>\n" + "</ogc:Function>\n" + "</CssParameter>\n" + "</Fill>\n" + "<Stroke>\n"
			+ "<CssParameter name=\"stroke\">\n" + "#<ogc:Function name=\"env\">\n"
			+ "<ogc:Literal>stroke</ogc:Literal>\n" + "<ogc:Literal>%s</ogc:Literal>\n" + "</ogc:Function>\n"
			+ "</CssParameter>\n" + "<CssParameter name=\"stroke-width\">\n" + "<ogc:Function name=\"env\">\n"
			+ "<ogc:Literal>stroke-width</ogc:Literal>\n" + "<ogc:Literal>%s</ogc:Literal>\n" + "</ogc:Function>\n"
			+ "</CssParameter>\n" + "</Stroke>\n" + "</PolygonSymbolizer>\n" + "</Rule>";

	static String footer_tpl = "" + "</FeatureTypeStyle>\n" + "</UserStyle>\n" + "</NamedLayer>\n"
			+ "</StyledLayerDescriptor>\n" + "";

	private static List<File> doListing(File dirname) {

		File[] fileList = dirname.listFiles();

		for (File file : fileList) {

			if (file.isFile()) {

				files.add(file);
			} else if (file.isDirectory()) {

				files.add(file);
				doListing(file);
			}
		}

		return files;
	}

	static String[][] a1 = { { "ffff7e", "f9d155", "f1a430", "a75118", "6c0000" },
			{ "f8f6f9", "cfc4d1", "a691b4", "886296", "61397f" },
			{ "eee4e5", "cdaaea", "bd82f8", "9c55f1", "8129fa" } };
	static List<List<String>> color_schemes = new ArrayList<>();

	public static void generate_styles(String dbname, String dbuser, String dbpassword)
			throws SQLException, ClassNotFoundException, IOException {

		for (int i = 0; i < a1.length; i++) {
			List<String> lst1 = Arrays.asList(a1[i]);
			color_schemes.add(lst1);
		}

		File file = new File(dirname);

		List<File> myfiles = doListing(file);

		for (File f : myfiles) {
			if (f.getName().endsWith(".sql")) {
				layers.add(f.getName().replace(".sql", ""));
			}
		}

		generate_style(layers, geoserver_data_dir_path, dbname, dbuser, dbpassword);

	}

	static Connection connection = null;
	static Statement stmt = null;
	static Statement stmt1 = null;

	private static void generate_style(List<String> tables, String geoserver_data_dir_path, String db, String user,
			String pass) throws SQLException, ClassNotFoundException, IOException {

		Class.forName("org.postgresql.Driver");

		try {

			connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/" + db, user, pass);// +db, user
																											// ,pass

		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
			return;
		}

		if (connection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");

		}

		System.out.println("Creating statement...");
		stmt = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

		String sql;
		String[] cont_type = { "bigint", "integer", "smallint", "double precision", "real" };

		for (String tablename : tables) {

			ProcessBuilder pb = new ProcessBuilder();
			pb.command("bash", "-c", "mkdir -p " + tmp_dir_path + "/styles/" + tablename);
			Process p1 = pb.start();

			String meta_layer_query = "select color_by, layer_type from \"Meta_Layer\" where layer_tablename=\'"
					+ tablename + "\'";
			ResultSet rs = stmt.executeQuery(meta_layer_query);
			rs.first();

			String layer_type = StringUtils.strip(rs.getString(2), "'");

			String colname_datatype_query = "select column_name, data_type from information_schema.columns where table_name = '"
					+ tablename + "'";
			rs = stmt.executeQuery(colname_datatype_query);
			List<HashMap<String, Object>> list1 = convertResultSetToList(rs);

			System.out.println(list1);
			for (HashMap<String, Object> hm1 : list1) {
				String col_type = (String) hm1.get("data_type");
				String column_name1 = (String) hm1.get("column_name");
				System.out.println(col_type);

				if (!column_name1.startsWith("__mlocate__") && col_type.startsWith("character")) {
					System.out.println("getting column name and type " + column_name1);
					create_categorical_style_files(tablename, column_name1, column_name1, layer_type, col_type);
				}

				else if (!column_name1.startsWith("__mlocate__") && Arrays.asList(cont_type).contains(col_type)) {
					System.out.println("continous style");
					String min_max_query = "SELECT min(" + column_name1 + "), max(" + column_name1 + ") FROM "
							+ tablename;
					rs = stmt.executeQuery(min_max_query);

					List<HashMap<String, Object>> list_max_min = convertResultSetToList(rs);
					System.out.println(list_max_min);
					for (HashMap<String, Object> hm2 : list_max_min) {

						if (hm2.get("max") != null && hm2.get("min") != null) {

							String max = hm2.get("max").toString();
							System.out.println(max);
							String min = (String) hm2.get("min").toString();
							System.out.println(min);
							create_style_files(tablename, column_name1, column_name1, min, max, 5, layer_type,
									col_type);
						}
					}

				}
			}
			ProcessBuilder pb1 = new ProcessBuilder();

			System.out.println(System.getProperty("user.dir"));
			pb1.command("bash", "-c",
					"mv " + tmp_dir_path + "/styles/" + tablename + "/*" + " " + geoserver_data_dir_path + "/styles");
			Process p = pb1.start();
		}

	}

	private static String get_column_name(String tablename, String property_name) throws SQLException {
		String col_description_query = "select col_description((select oid from pg_class where relname = '" + tablename
				+ "'), (select ordinal_position from information_schema.columns where table_name='" + tablename + "'"
				+ property_name + "'))";
		ResultSet rs = stmt.executeQuery(col_description_query);
		rs.first();
		System.out.println(rs.getString(1));
		String column_name = rs.getString(1);
		return column_name;
	}

	private static void create_categorical_style_files(String tablename, String property_title, String property_name,
			String layer_type, String col_type) throws SQLException, IOException {

		System.out.println("start");
		System.setProperty("user.dir", "/apps/biodiv/styles/" + tablename + "/");
		String sld_filename = tablename + "_" + property_name + ".sld";
		String json_filename = tablename + "_" + property_name + ".json";

		boolean isLastRule = false;
		String paint_prop = null;
		String cat_values_query = "select distinct(\"" + property_name + "\") from " + tablename;
		ResultSet rs = stmt.executeQuery(cat_values_query);

		create_style_file_xml(tablename, property_name, sld_filename, json_filename);

		System.out.println("running 1");

		File file_sld = new File(tmp_dir_path + "/styles/" + tablename + "/" + sld_filename);
		FileWriter fileWriter1 = new FileWriter(file_sld);
		fileWriter1.write(String.format(header_tpl, property_title));
		File file_json = new File(tmp_dir_path + "/styles/" + tablename + "/" + json_filename);
		FileWriter fileWriter2 = new FileWriter(file_json);

		System.out.println("running 2");

		if (layer_type.equals("MULTIPOLYGON")) {

			fileWriter2.write(
					String.format(header_tpl_mgl, tablename, tablename, tablename, "fill", tablename, tablename));
			paint_prop = String.format(paint_tpl_fill_mgl, property_name, "categorical");
		} else if (layer_type.equals("POINT") || layer_type.equals("MULTIPOINT")) {
			fileWriter2.write(
					String.format(header_tpl_mgl, tablename, tablename, tablename, "circle", tablename, tablename));
			paint_prop = String.format(paint_tpl_circle_mgl, property_name, "categorical");
		} else if (layer_type.equals("MULTILINESTRING")) {
			fileWriter2.write(
					String.format(header_tpl_mgl, tablename, tablename, tablename, "line", tablename, tablename));
			paint_prop = String.format(paint_tpl_line_mgl, property_name, "categorical");
		}
		fileWriter2.write(paint_prop);

		int i = 1;
		rs.last();
		int total = rs.getRow();
		rs.beforeFirst();

		while (rs.next()) {
			String colr_code = "c" + Integer.toString(i);
			String colr_hex = get_rand_color();

			if (layer_type.equals("MULTIPOLYGON")) {
				String cat_rule = String.format(polygon_cat_rule_tpl, rs.getString(1), rs.getString(1), property_name,
						rs.getString(1), colr_code, colr_hex, "aaaaaa", 1);
				fileWriter1.write(cat_rule);
			} else if (layer_type.equals("POINT")) {
				String cat_rule = String.format(point_cat_rule_tpl, rs.getString(1), rs.getString(1), property_name,
						rs.getString(1), colr_code, "fd7569", "e5e5e5", 2, 12);
				fileWriter1.write(cat_rule);
			}

			if (i == total) {
				isLastRule = true;
			}
			String rule_mgl = create_mgl_rule(rs.getString(1), colr_hex, layer_type, col_type, isLastRule);
			fileWriter2.write(rule_mgl);
			i++;

		}

		fileWriter1.write(footer_tpl);
		fileWriter2.write(footer_tpl_mgl);

		fileWriter1.flush();
		fileWriter2.flush();
		fileWriter2.close();
		fileWriter1.close();

	}

	private static void create_style_file_xml(String tablename, String property_name, String sld_filename,
			String json_filename) throws IOException {

		System.setProperty("user.dir", "/apps/biodiv/styles/" + tablename + "/");
		String xml_filename = tablename + "_" + property_name + ".xml";
		System.out.println(xml_filename);
		String xml_id = tablename + "_" + property_name;

		System.out.println(tmp_dir_path + "/styles/" + xml_filename);
		File file = new File(tmp_dir_path + "/styles/" + tablename + "/" + xml_filename);

		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(String.format(xml_tpl, xml_id, xml_id, sld_filename, json_filename));
		fileWriter.flush();
		fileWriter.close();

	}

	private static String get_rand_color() {
		int rndr = ThreadLocalRandom.current().nextInt(150, 400 + 1);

		double r = Math.floor(rndr * golden_ratio_conjugate);

		int rndg = ThreadLocalRandom.current().nextInt(150, 400 + 1);
		double g = Math.floor(rndg * golden_ratio_conjugate);

		int rndb = ThreadLocalRandom.current().nextInt(150, 400 + 1);
		double b = Math.floor(rndb * golden_ratio_conjugate);

		return String.format("%x%x%x", (int) r, (int) g, (int) b);
	}

	public static List<HashMap<String, Object>> convertResultSetToList(ResultSet rs) throws SQLException {
		ResultSetMetaData md = rs.getMetaData();
		int columns = md.getColumnCount();
		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();

		while (rs.next()) {
			HashMap<String, Object> row = new HashMap<String, Object>(columns);
			for (int i = 1; i <= columns; ++i) {
				row.put(md.getColumnName(i), rs.getObject(i));
			}
			list.add(row);
		}

		return list;
	}

	private static void create_style_files(String tablename, String property_title, String property_name,
			String property_min, String property_max, int bincount, String layer_type, String col_type)
			throws IOException {

		System.out.println("start_of_something");
		boolean isLastRule = false;
		String paint_prop = null;

		System.setProperty("user.dir", "/apps/biodiv/styles/" + tablename + "/");

		List<String> color_scheme = color_schemes.get(ThreadLocalRandom.current().nextInt(1, color_schemes.size()));
		String sld_filename = tablename + "_" + property_name + ".sld";
		String json_filename = tablename + "_" + property_name + ".json";

		create_style_file_xml(tablename, property_name, sld_filename, json_filename);

		File file_sld = new File(tmp_dir_path + "/styles/" + tablename + "/" + sld_filename);
		FileWriter fileWriter1 = new FileWriter(file_sld);
		fileWriter1.write(String.format(header_tpl, property_title));

		File file_json = new File(tmp_dir_path + "/styles/" + tablename + "/" + json_filename);
		FileWriter fileWriter2 = new FileWriter(file_json);

		fileWriter1.write(String.format(header_tpl, property_title));

		int size = get_bin_size(property_min, property_max, bincount);
		double min_doub = Double.parseDouble(property_min);
		double max_doub = Double.parseDouble(property_min);
		int mi = (int) min_doub;
		int mx = (int) max_doub + size;
		System.out.println(mi);
		System.out.println(mx);

		if (layer_type.equals("MULTIPOLYGON")) {

			fileWriter2.write(
					String.format(header_tpl_mgl, tablename, tablename, tablename, "fill", tablename, tablename));
			paint_prop = String.format(paint_tpl_fill_mgl, property_name, "intervel");
		} else if (layer_type.equals("POINT") || layer_type.equals("MULTIPOINT")) {
			fileWriter2.write(
					String.format(header_tpl_mgl, tablename, tablename, tablename, "circle", tablename, tablename));
			paint_prop = String.format(paint_tpl_circle_mgl, property_name, "intervel");
		} else if (layer_type.equals("MULTILINESTRING")) {
			fileWriter2.write(
					String.format(header_tpl_mgl, tablename, tablename, tablename, "line", tablename, tablename));
			paint_prop = String.format(paint_tpl_line_mgl, property_name, "intervel");
		}
		fileWriter2.write(paint_prop);

		for (int i = 1; i <= bincount; i++) {

			String rule_name = String.format("%s to %s", mi, mx);
			String colr_code = "c" + Integer.toString(i);
			String color_hex = color_scheme.get(i - 1);

			if (layer_type.equals("MULTIPOLYGON")) {
				String rule = String.format(polygon_rule_tpl, rule_name, rule_name, property_name,
						String.format("%s", mi), property_name, String.format("%s", mx), colr_code, color_hex, "aaaaaa",
						1);
				fileWriter1.write(rule);
			} else if (layer_type.equals("POINT")) {
				String rule = String.format(polygon_rule_tpl, rule_name, rule_name, property_name,
						String.format("%s", mi), property_name, String.format("%s", mx), colr_code, color_hex, "e5e5e5",
						2, 12);
				fileWriter1.write(rule);
			}

			if (i == bincount) {
				isLastRule = true;
			}
			String rule_mgl = create_mgl_rule(String.format("%s", mi), color_hex, layer_type, col_type, isLastRule);
			fileWriter2.write(rule_mgl);

			mi = mx;
			mx = (mi + size) * (i + 1);

		}

		fileWriter1.write(footer_tpl);
		fileWriter2.write(footer_tpl_mgl);

		fileWriter1.flush();
		fileWriter2.flush();
		fileWriter2.close();
		fileWriter1.close();

	}

	public static int get_bin_size(String min, String max, int bincount) {
		double min_doub = Double.parseDouble(min);
		double max_doub = Double.parseDouble(max);
		int min1 = (int) min_doub;
		int max1 = (int) max_doub;

		int size = (int) ((max1 - min1) / bincount);
		return size;

	}
}
