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

import org.apache.commons.lang.StringUtils;

import com.strandls.naksha.NakshaConfig;

/**
 *
 * @author humeil
 */

public class generate_geoserver_layers {
	static String featuretype_xml_tpl;
	static String layer_xml_tpl;
	static String geoserver_data_dir_path = NakshaConfig.getString("tmpDirGeoserverPath");
	static String namespace = "NamespaceInfoImpl-21400543:1604535a380:-7ffe";
	static String datastore = "DataStoreInfoImpl--596c0b74:1604c3037af:-7fff";
	static String dirname = NakshaConfig.getString("tmpDir.path") + "layersqls/";
	static List<String> layers = new ArrayList<>();
	static String tmp_dir_path = NakshaConfig.getString("tmpDir.path");
	private static List<File> files = new ArrayList<>();

	private static void run_cmd(String command) throws IOException {

		ProcessBuilder pb = new ProcessBuilder();
		pb.command("bash", "-c", command);
		Process p1 = pb.start();
	}

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

	public static void geoserver_func(String dbname, String dbuser, String dbpassword)
			throws IOException, ClassNotFoundException, SQLException {

		System.out.println(System.getProperty("use.dir"));

		File file = new File(dirname);

		List<File> myfiles = doListing(file);

		for (File f : myfiles) {
			if (f.getName().endsWith(".sql")) {
				layers.add(f.getName().replace(".sql", ""));
			}
		}

		featuretype_xml_tpl = "<featureType>\n" + "<id>FeatureTypeInfoImpl-%s-wgp</id>\n" + "<name>%s</name>\n"
				+ "<nativeName>%s</nativeName>\n" + "<namespace>\n" + "<id>" + namespace + "</id>\n" + "</namespace>\n"
				+ "<title>%s</title>\n" + "<abstract>%s</abstract>\n" + "<keywords>\n %s" +

				"</keywords>\n" + "<srs>EPSG:4326</srs>\n" + "<nativeBoundingBox>\n %s" +

				"</nativeBoundingBox>\n" + "<latLonBoundingBox>\n %s" +

				"<crs>EPSG:4326</crs>\n" + "</latLonBoundingBox>\n"
				+ "<projectionPolicy>FORCE_DECLARED</projectionPolicy>\n" + "<enabled>true</enabled>\n" + "<metadata>\n"
				+ "<entry key=\"cachingEnabled\">false</entry>\n" + "</metadata>\n" + "<store class=\"dataStore\">\n"
				+ "<id>" + datastore + "</id>\n" + "</store>\n" + "<maxFeatures>0</maxFeatures>\n"
				+ "<numDecimals>0</numDecimals>\n" + "</featureType>";

		System.out.println(featuretype_xml_tpl);

		layer_xml_tpl = "<layer>\n" + "<name>%s</name>\n" + "<id>LayerInfoImpl-%s</id>\n" + "<type>VECTOR</type>\n"
				+ "<defaultStyle>\n" + "<id>%s</id>\n" + "</defaultStyle>\n" + "<styles class=\"linked-hash-set\">\n %s"
				+ "</styles>\n" + "<resource class=\"featureType\">\n" + "<id>FeatureTypeInfoImpl-%s-wgp</id>\n"
				+ "</resource>\n" + "<enabled>true</enabled>\n" + "<attribution>\n" + "<logoWidth>0</logoWidth>\n"
				+ "<logoHeight>0</logoHeight>\n" + "</attribution>\n" + "</layer>";

		System.out.println(layer_xml_tpl);

		generate_layer_xml(layers, geoserver_data_dir_path, dbuser, dbpassword, dbname);
	}

	static Connection connection = null;
	static Statement stmt = null;

	private static void generate_layer_xml(List<String> tables, String geoserver_data_dir_path, String user,
			String pass, String db) throws ClassNotFoundException, SQLException, IOException {

		Class.forName("org.postgresql.Driver");

		try {
			connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/" + db, user, pass);
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

		for (String tablename : tables) {

			System.out.println("layer" + tablename);
			ProcessBuilder pb = new ProcessBuilder();
			pb.command("bash", "-c", "mkdir -p " + tmp_dir_path + "/layers/" + tablename);
			Process p1 = pb.start();
			create_featuretype_xml(tablename);
			create_layer_xml(tablename);
			System.out.println("mv " + tmp_dir_path + "layers/" + tablename + " " + geoserver_data_dir_path
					+ "/workspaces/" + NakshaConfig.getString("workspace") + "/" + NakshaConfig.getString("datastore")
					+ "/" + tablename);
			pb.command("bash", "-c",
					"mv " + tmp_dir_path + "layers/" + tablename + " " + geoserver_data_dir_path + "/workspaces/"
							+ NakshaConfig.getString("workspace") + "/" + NakshaConfig.getString("datastore") + "/");
			Process p = pb.start();

		}

	}

	private static void create_featuretype_xml(String tablename) throws SQLException, IOException {
		String layer_description;
		String layer_info_query = "select layer_name, layer_description from \"Meta_Layer\" where layer_tablename='"
				+ tablename + "'";
		ResultSet rs = stmt.executeQuery(layer_info_query);
		List<HashMap<String, Object>> list1 = convertResultSetToList(rs);
		System.out.println(list1);
		rs.first();

		String layer_name = rs.getString(1);

		System.out.println(layer_name + "   ");

		if (rs.getString(2) != null && !rs.getString(2).isEmpty()) {

			layer_description = rs.getString(2);

		} else {
			layer_description = layer_name;
		}

		String keywords = get_keywords_xml(tablename);
		System.out.println(keywords);
		String bbox = get_bounding_box(tablename);
		System.out.println(bbox);
		// System.out.println(featuretype_xml_tpl);
		String featuretype_xml = String.format(featuretype_xml_tpl, tablename, tablename, tablename, layer_name,
				layer_description, keywords, bbox, bbox);
		System.out.println(featuretype_xml);
		System.setProperty("user.dir", "/apps/biodiv/map_upload_tmp/layers/" + tablename + "/");
		File file = new File(tmp_dir_path + "/layers/" + tablename + "/featuretype.xml");
		System.out.println(file.getAbsolutePath());
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(featuretype_xml);
		fileWriter.flush();
		fileWriter.close();

	}

	private static void create_layer_xml(String tablename) throws SQLException, IOException {

		String title_column = null;
		String color_by = null;
		String layer_info_query = "select color_by, title_column, layer_type from \"Meta_Layer\" where layer_tablename='"
				+ tablename + "'";
		ResultSet rs = stmt.executeQuery(layer_info_query);
		rs.first();

		String layer_type = rs.getString(3);
		String default_style = "";
		System.out.println(rs.getString(1) + "  " + rs.getString(2) + "  " + rs.getString(3));
		if (rs.getString(1).isEmpty()) {
			System.out.println("no color specified " + tablename);
			return;
		}
		if (layer_type.equals("MULTIPOLYGON")) {
			color_by = StringUtils.strip(rs.getString(1), "'");
		} else if (layer_type.equals("POINT")) {
			color_by = StringUtils.strip(rs.getString(1), "'");
		}

		if (!color_by.isEmpty() && color_by != null)
			default_style = tablename + "_" + color_by;

		else {
			title_column = StringUtils.strip(rs.getString(2), "'");
			default_style = tablename + "_" + title_column;
		}

		String styles = create_styles_xml(tablename);
		String layer_xml = String.format(layer_xml_tpl, tablename, tablename, default_style, styles, tablename);
		System.out.println(layer_xml);

		File file = new File(tmp_dir_path + "/layers/" + tablename + "/layer.xml");
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(layer_xml);
		fileWriter.flush();
		fileWriter.close();

	}

	private static String get_keywords_xml(String tablename) throws SQLException {

		String[] layer_keywords = null;
		String keywords_xml = "";
		String layer_keywords_query = "select tags from \"Meta_Layer\" where layer_tablename='" + tablename + "'";
		ResultSet rs = stmt.executeQuery(layer_keywords_query);
		rs.first();

		String layer_keywords_results = rs.getString(1);
		if (!layer_keywords_results.isEmpty() && layer_keywords_results != null) {
			layer_keywords = layer_keywords_results.split(",");
		} else {
			layer_keywords[0] = "Miscellaneous";
		}

		for (String keyword : layer_keywords) {
			keywords_xml = keywords_xml + "<string>" + keyword.trim() + "</string>";
		}

		return keywords_xml;
	}

	private static String get_bounding_box(String tablename) throws SQLException {
		String bbox_xml = "";
		String layer_bbox_query = "select min(st_xMin(__mlocate__topology)), max(st_xMax(__mlocate__topology)), min(st_yMin(__mlocate__topology)), max(st_yMax(__mlocate__topology)) from "
				+ tablename;
		ResultSet rs = stmt.executeQuery(layer_bbox_query);
		rs.first();

		System.out.println(rs.getDouble(1));
		bbox_xml = bbox_xml + "<minx>" + rs.getString(1) + "</minx>";
		bbox_xml = bbox_xml + "<maxx>" + rs.getString(2) + "</maxx>";
		bbox_xml = bbox_xml + "<miny>" + rs.getString(3) + "</miny>";
		bbox_xml = bbox_xml + "<maxy>" + rs.getString(4) + "</maxy>";

		return bbox_xml;
	}

	private static String create_styles_xml(String tablename) throws SQLException {
		String[] cont_type = { "bigint", "integer", "smallint", "double precision", "real" };

		String styles = "";
		String colname_datatype_query = "select column_name, data_type from information_schema.columns where table_name = '"
				+ tablename + "'";

		ResultSet rs = stmt.executeQuery(colname_datatype_query);
		while (rs.next()) {
			if (rs.getString(1).startsWith("__malocate__") && rs.getString(2).startsWith("character")
					|| Arrays.asList(cont_type).contains(rs.getString(2)))
				;
			styles = styles + "<style>\n  <id>" + tablename + "_" + rs.getString(1) + "</id>\n</style>";

		}
		return styles;
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

}
