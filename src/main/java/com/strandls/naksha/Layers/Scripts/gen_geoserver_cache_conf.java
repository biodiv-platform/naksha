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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.strandls.naksha.NakshaConfig;

public class gen_geoserver_cache_conf {

	static String workspaceName = "biodiv";
	static String header_xml_tpl = "<GeoServerTileLayer>\n" + "<id>LayerInfoImpl-%s</id>\n" + "<enabled>true</enabled>"
			+ "<inMemoryCached>true</inMemoryCached>" + "<name>" + workspaceName + ":%s</name>";

	static String format_xml_tpl = "\n  <mimeFormats>" + "<string>image/png</string>\n"
			+ "    <string>image/jpeg</string>\n" + "    <string>application/json;type=geojson</string>\n"
			+ "    <string>application/x-protobuf;type=mapbox-vector</string>\n" + "  </mimeFormats>\n"
			+ "  <gridSubsets>\n" + "    <gridSubset>\n" + "      <gridSetName>EPSG:900913</gridSetName>\n"
			+ "      <extent>\n" + "        <coords>\n" + "          <double>-2.003750834E7</double>\n"
			+ "          <double>-2.003750834E7</double>\n" + "          <double>2.003750834E7</double>\n"
			+ "          <double>2.003750834E7</double>\n" + "        </coords>\n" + "      </extent>\n"
			+ "    </gridSubset>\n" + "    <gridSubset>\n" + "      <gridSetName>EPSG:4326</gridSetName>\n"
			+ "      <extent>\n" + "        <coords>\n" + "          <double>-180.0</double>\n"
			+ "          <double>-90.0</double>\n" + "          <double>180.0</double>\n"
			+ "          <double>90.0</double>\n" + "        </coords>\n" + "      </extent>\n" + "    </gridSubset>\n"
			+ "  </gridSubsets>\n" + "  <metaWidthHeight>\n" + "    <int>4</int>\n" + "    <int>4</int>\n"
			+ "  </metaWidthHeight>\n" + "  <expireCache>0</expireCache>\n" + "  <expireClients>0</expireClients>";

	static String styles_xml_tpl = "<parameterFilters>\n" + "    <styleParameterFilter>\n" + "      <key>STYLES</key>\n"
			+ "      <defaultValue></defaultValue>\n" + "      <availableStyles class=\"sorted-set\">\n"
			+ "%s      </availableStyles>\n" + "      <defaultStyle>lyr_100_india_cpa_vill_name</defaultStyle>\n"
			+ "    </styleParameterFilter>\n" + "  </parameterFilters>";

	static String footer_xml_tpl = " <gutter>0</gutter>\n" + "</GeoServerTileLayer>";

	static String dirname = NakshaConfig.getString("tmpDir.path") + "layersqls/";
	static List<String> layers = new ArrayList<>();
	static String tmp_dir_path = NakshaConfig.getString("tmpDir.path");
	private static List<File> files = new ArrayList<>();
	static String geoserver_data_dir_path = NakshaConfig.getString("tmpDirGeoserverPath");

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

	private static void run_cmd(String command) throws IOException {

		System.out.print(command);
		ProcessBuilder pb = new ProcessBuilder();
		pb.command("bash", "-c", command);
		Process p1 = pb.start();
	}

	public static void generate_cache(String db, String user, String pass)
			throws SQLException, ClassNotFoundException, IOException {

		File file = new File(dirname);

		List<File> myfiles = doListing(file);

		for (File f : myfiles) {
			if (f.getName().endsWith(".sql")) {
				layers.add(f.getName().replace(".sql", ""));
			}
		}

		generate_cache_config(layers, geoserver_data_dir_path, db, user, pass);

	}

	static Connection connection = null;
	static Statement stmt = null;
	static Statement stmt1 = null;

	public static void generate_cache_config(List<String> tables, String geoserver_data_dir_path, String db,
			String user, String pass) throws SQLException, ClassNotFoundException, IOException {

		Class.forName("org.postgresql.Driver");

		try {
			String dbhost = NakshaConfig.getString("geoserver.dbhost");
			String dbport = NakshaConfig.getString("geoserver.dbport");
			connection = DriverManager.getConnection("jdbc:postgresql://" + dbhost + ":" + dbport + "/" + db, user, pass);// +db, user
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

		run_cmd("mkdir -p " + tmp_dir_path + "cache_config");

		for (String tablename : tables) {

			create_cache_xml(tablename);
			run_cmd("mv " + tmp_dir_path + "cache_config/LayerInfoImpl-* " + geoserver_data_dir_path + "/gwc-layers/");
		}

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

	private static void create_cache_xml(String tablename) throws SQLException, IOException {
		String[] content_type = { "bigint", "integer", "smallint", "double precision", "real" };
		List<String> styles = new ArrayList<>();
		String colname_datatype_query = "select column_name, data_type from information_schema.columns where table_name = '"
				+ tablename + "'";

		ResultSet rs = stmt.executeQuery(colname_datatype_query);
		List<HashMap<String, Object>> list1 = convertResultSetToList(rs);
		System.out.println(list1);

		for (HashMap<String, Object> hm1 : list1) {
			String col_type = (String) hm1.get("data_type");
			String column_name1 = (String) hm1.get("column_name");

			if (!column_name1.startsWith("__mlocate__") && col_type.startsWith("character")
					|| Arrays.asList(content_type).contains(col_type)) {
				styles.add(tablename + '_' + column_name1);
			}

		}
		styles.sort(Comparator.comparing(String::toString));
		String style_conf = "";
		for (String style : styles) {
			style_conf += String.format("		<string>%s</string>\n", style);
		}

		String filename = "LayerInfoImpl-" + tablename + ".xml";

		File conf_xml_file = new File(tmp_dir_path + "/cache_config/" + filename);
		@SuppressWarnings("resource")
		FileWriter fileWrite1 = new FileWriter(conf_xml_file);
		fileWrite1.write(String.format(header_xml_tpl, tablename, tablename));
		fileWrite1.write(String.format(format_xml_tpl));
		fileWrite1.write(String.format(styles_xml_tpl, style_conf));
		fileWrite1.write(String.format(footer_xml_tpl));

		fileWrite1.flush();
		fileWrite1.close();
	}
}
