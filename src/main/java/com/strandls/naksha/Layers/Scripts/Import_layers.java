/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strandls.naksha.Layers.Scripts;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author humeil
 */
public class Import_layers {

	static String DBNAME;
	static String DBUSER;

	public static Object Meta_Layer(String path) {
		state = 1;
		String indnt;
		indnt = "    ";
		theDictStr += indnt + "\"Meta_Layer\" : {\n";

		getKeyValues(0, indnt, path);
		theDictStr += indnt + "},\n";
		return null;

	}

	public static void Layer_Column_Description(String spc, String path) {
		state = 2;
		String indnt = "    " + spc;
		theDictStr += indnt + "\"Layer_Column_Description\" : {\n";
		getKeyValues(0, indnt, path);
		theDictStr += indnt + "},\n";

	}

	private static Object Meta_LinkTable(String path) {
		state = 3;
		String indnt;
		indnt = "    ";
		theDictStr += indnt + "\"Meta_LinkTable\" : {\n";
		getKeyValues(0, indnt, path);
		theDictStr += indnt + "},\n";
		return null;
	}

	public static String LinkTable_Column_Description(String spc, String path) {

		state = 4;
		String indnt = "    " + spc;
		theDictStr += indnt + "\"LinkTable_Column_Description\" : {\n";
		// ret = getKeyValues(0, indnt, path);
		theDictStr += indnt + "},\n";
		return getKeyValues(0, indnt, path);

	}

	private static Object Theme_layer_Mapping(String path) {
		state = 5;
		String indnt = "    ";
		theDictStr += indnt + "\"Theme_Layer_Mapping\" : {\n";
		getKeyValues(0, indnt, path);
		theDictStr += indnt + "},\n";
		return null;

	}

	private static Object Record_Types(String path) {
		state = 6;

		String indnt = "    ";
		theDictStr += indnt + "\"Record_Types\" : {\n";
		getDirectMappingDict(indnt);
		theDictStr += indnt + "},\n";
		return null;

	}

	public static void getDirectMappingDict(String indnt) {

	}

	public static String escapeStr(String str1) {
		str1 = str1.trim();
		str1 = str1.replace('"', '\"');
		str1 = str1.replace("\\", "\\\\");
		return str1;
	}

	private static String getKeyValues(int has_rows, String indnt, String path) {
		String val = null;
		String val1;
		String[] parts = null;
		int i = 0;
		indnt = indnt + "   ";
		String spc = " ";

		if (has_rows != 0) {
			spc = "  ";
		}
		if (has_rows != 0) {
			theDictStr += indnt + "\"" + Integer.toString(i) + "\" : {\n";
		}

		while (cursr < fl_lines.size()) {
			System.out.println(cursr);
			String line = fl_lines.get(cursr).trim();
			System.out.println(line);
			if (line.startsWith("*")) {
				System.out.println("line starts with *");
				if (has_rows != 0) {
					theDictStr += indnt + "},\n";
				}
				cursr--;
				System.out.println("chalo wapis bahar");
				return "*";
			}

			else if (line.equals("#")) {
				if (has_rows != 0) {
					System.out.println("line starts with #");
					theDictStr += indnt + "},\n";
					i = i + 1;
					theDictStr += indnt + "\"" + Integer.toString(i) + "\" : {\n";
					cursr++;
					continue;
				} else {
					theDictStr += indnt + "},\n";
					cursr++;
					return "#";
				}

			}

			else if (line.equals("")) {
				cursr += 1;
				System.out.println("nothing and value is " + cursr);

				continue;
			}

			else if (line.equals("$Layer_Column_Description")) {
				cursr++;
				System.out.println("encountered layer column description  " + cursr);
				Layer_Column_Description("  ", path);
				System.out.println("layer clumn desc ka kaam khtam  " + cursr);
				continue;
			}

			else if (line.equals("$LinkTable_Column_Description")) {
				String ret = LinkTable_Column_Description("  ", path);
				if (has_rows != 0 && ret == "#") {
					i += 1;
					theDictStr += indnt + "\"" + Integer.toString(i) + "\" : {\n";
				}
				continue;
			}

			else {
				parts = line.split(Pattern.quote(":"));
				String key = parts[0]; // 004
				// if(parts[0].equals("")){continue;}
				if (parts.length == 2) {
					val = parts[1];
				} else {
					val = "";
				}

				key = key.trim().replace("\t", " ");

				if (key.equals("link_tablename")) {
					String tablename = "lnk_" + Integer.toString(link_seq) + "_"
							+ escapeStr(val).replace(".dbf", "").replace(".txt", "");
					theDictStr += indnt + spc + "\"" + escapeStr(key) + "\" : \"" + tablename + "\",\n";
					g_link_tablenames.put(escapeStr(val).replace(".dbf", "").replace(".txt", ""), tablename);
					// link_seq += 1;

					Path filePath = Paths.get(path, "linktable", escapeStr(val).replace(".dbf", "").replace(".txt", ""),
							".txt");
					theDictStr += indnt + spc + "\"linkTable_filename\" : \"" + escapeStr(filePath.toString())
							+ "\",\n";
				}

				else if (key.equals("layer_tablename")) {

					g_layer_tablename = "lyr_" + Integer.toString(layer_seq) + "_"
							+ escapeStr(val).replace(".dbf", "").replace(".txt", "");
					theDictStr += indnt + spc + "\"" + escapeStr(key) + "\" : \"" + g_layer_tablename + "\",\n";
					layer_seq += 1;
					// System.out.println(g_layer_tablename);
				}

				else if (Arrays.stream(columnnamekeys).anyMatch(key::equals)) {
					if (val.equals("")) {
						val1 = val;
					} else {
						val1 = sanitizecolumnnames(g_layer_tablename, escapeStr(val.toLowerCase()));
					}
					theDictStr += indnt + spc + "\"" + escapeStr(key) + "\" : \"" + val1 + "\",\n";
				}

				else if (key == "FID" && key == "SHAPE") {
				}

				else {

					theDictStr += indnt + spc + "\"" + escapeStr(key) + "\" : \"" + escapeStr(val) + "\",\n";
				}
				cursr += 1;
			}
		}
		if (has_rows != 0)
			theDictStr += indnt + "},\n";

		return theDictStr;
	}

	public static String sanitizecolumnnames(String tablename, String cols) {

		cols = StringUtils.strip(cols, " \n");
		String[] cols_lst = cols.split(",");

		List<String> iterable = Arrays.asList(cols_lst);
		Iterator itr = iterable.iterator();
		String new_cols = "";

		while (itr.hasNext()) {
			String column = (String) itr.next();
			String col = StringUtils.strip(column, " \r\n");

			if (!col.equals("")) {
				if (!col.startsWith("'")) {
					col = "'" + col;
					System.out.println(
							String.format("@REM Error: %s: ' missing in column name at start: %s", tablename, col));
				}
				if (!col.endsWith("'")) {
					col = col + "'";
					System.out.println(
							String.format("@REM Error: %s: ' missing in column name at end: %s", tablename, col));
				}

				new_cols += col + ",";
				System.out.println(new_cols);
			}
		}
		return new_cols.substring(0, new_cols.length() - 2);

	}

	private static String getOutput(Process process) throws IOException {

		// for reading the ouput from stream
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String s = null;
		while ((s = stdInput.readLine()) != null) {
			System.out.println(s);
		}
		return s;
	}

	private static String dbimport(JSONArray theDict, File sql_file) throws IOException, SQLException {
		List<String> list = new ArrayList<>();
		List<String> list2 = new ArrayList<>();
		System.out.println("database import");
		for (Object o : theDict) {
			JSONObject layer = (JSONObject) o;
			System.out.println(layer.get("shp_filename"));
			System.out.println(layer);
			err = 0;
			JSONObject Meta_Layer = (JSONObject) layer.get("Meta_Layer");
			System.out.println(Meta_Layer);
			if (!Meta_Layer.containsKey("layer_tablename")) {
				System.out.println(layer.get("shp_filename"));
			}
			String l = (String) Meta_Layer.get("layer_tablename");
			String layer_tablename = l.replace(".dbf", "");

			String cmmd = "shp2pgsql -s -1 -W \"latin1\" -I -p -g " + GEOMCOL + " " + layer.get("shp_filename") + " "
					+ layer_tablename + " " + DBNAME;
			System.out.println(cmmd);

			ProcessBuilder pb = new ProcessBuilder();
			pb.command("bash", "-c", cmmd);
			Process process1 = pb.start();

			InputStream[] is = { process1.getInputStream(), process1.getErrorStream() };

			InputStreamReader[] isr = { new InputStreamReader(is[0]), new InputStreamReader(is[1]) };

			BufferedReader[] br = { new BufferedReader(isr[0]), new BufferedReader(isr[1]) };
			String arr = null;
			int k = 0;
			while ((arr = br[1].readLine()) != null) {
				list.add(arr);
				System.out.println(arr);
			}

			String arr1 = null;

			while ((arr1 = br[0].readLine()) != null) {
				list2.add(arr1);
				System.out.println(list2.get(k));
				k++;
			}

			String layer_type = list.get(list.size() - 1).replace("Postgis type: ", "").replace("\n", "");
			int indx = layer_type.indexOf("[");
			if (indx != -1) {
				layer_type = layer_type.substring(0, indx);
				System.out.println(layer_type);
			}

			List<String> c = modifyCreateLayer(list2);

			for (String c1 : c) {
				System.out.println(c1);
			}

			List<String> ret = parse_Meta_Layer(layer_tablename, layer_type, Meta_Layer);

			String Meta_Layer_sql = ret.get(0);
			System.out.println("Meta_Layer_sql");
			System.out.println(Meta_Layer_sql);
			String layer_colcomments = ret.get(1);

			System.out.println(Import_layers.cwdir);
			String dir1 = Import_layers.cwdir.concat("layersqls");
			System.out.println(dir1);
			String sqlfl = dir1.concat(String.format("/%s.sql", layer_tablename));
			System.out.println(sqlfl);
			System.out.println("sql  " + sqlfl);

			String cmmd2 = "shp2pgsql -s -1 -W \"latin1\" -I -a -g " + GEOMCOL + " " + layer.get("shp_filename") + " "
					+ layer_tablename + " " + DBNAME;
			pb.command("bash", "-c", String.format("%s > %s", cmmd2, sqlfl));
			Process process2 = pb.start();
			// getOutput(process2);
			System.out.println("\n\n\n\n\n\n\n\n" + String.format("%s > %s", cmmd2, sqlfl));
			InputStream[] is2 = { process2.getInputStream(), process2.getErrorStream() };

			InputStreamReader[] isr2 = { new InputStreamReader(is2[0]), new InputStreamReader(is2[1]) };

			BufferedReader[] br2 = { new BufferedReader(isr2[0]), new BufferedReader(isr2[1]) };
			String arr3 = null;
			int i = 0;
			while ((arr3 = br2[1].readLine()) != null) {
				list.add(i, arr3); // a
				i++;
			}

			String arr4 = null;
			i = 0;
			while ((arr4 = br2[0].readLine()) != null) {
				list2.add(i, arr4);
				i++;
			} // b

			String sql1 = String.format("%s\n\n%s\n", c.stream().collect(Collectors.joining("\n")), layer_colcomments);
			System.out.println(sql1);
			String sql2 = "\nUPDATE " + layer_tablename
					+ " SET __mlocate__layer_id = (SELECT currval('\"Meta_Layer_layer_id_seq\"')), __mlocate__status = 1, __mlocate__created_by = 1, __mlocate__created_date = now(), __mlocate__modified_by = 1, __mlocate__modified_date = now(), __mlocate__validated_by = 1, __mlocate__validated_date = now();\n\n";
			// SELECT currval('\"Meta_Layer_layer_id_seq\"');
			sql2 = String.format("\n%s\n%s\n", Meta_Layer_sql, sql2);
			System.out.println(sql2);

			if (err == 1) {
				System.out.println("error in data\n");

			} else {
				insertsql(sqlfl, sql1, sql2);
				System.out.println(String.format("@REM # -- Related shape file: %s", layer.get("shp_filename")));

				return sqlfl;
			}

		}
		return null;

	}

	private static List<String> modifyCreateLayer(List<String> b) {

		List<String> c = new ArrayList<>();
		int insrtd_inherits = 0;
		for (int i = 0; i < b.size(); i++) {
			String d = b.get(i).replace("\n", "");
			System.out.println(d);
			if (i == 3) {

			}
			if (d.endsWith(");") && insrtd_inherits == 0) {
				d = d.substring(0, d.length() - 1);
				c.add("  " + d);
				c.add("  INHERITS (\"Layer_template\");");
				insrtd_inherits = 1;
			} else if (d.endsWith("END;"))
				continue;
			else if (d.startsWith("CREATE INDEX"))
				continue;
			else if (d.endsWith("(gid);")) {

			} else
				c.add("  " + d);
		}

		return c;

	}

	private static List<String> parse_Meta_Layer(String layer_tablename, String layer_type, JSONObject Meta_Layer) {
		System.out.println(Meta_Layer);
		List<String> ret = new ArrayList<>();
		String Meta_Layer_sql = "INSERT INTO \"Meta_Layer\" (";
		String vals = "";
		Iterator<String> keys = Meta_Layer.keySet().iterator();
		String layer_colcomments = "" + "COMMENT ON COLUMN \"%s\".__mlocate__id IS 'ID';\n"
				+ "COMMENT ON COLUMN \"%s\".__mlocate__status IS 'Status';\n"
				+ "COMMENT ON COLUMN \"%s\".__mlocate__layer_id IS 'Layer ID';\n"
				+ "COMMENT ON COLUMN \"%s\".__mlocate__nid IS 'NID';\n"
				+ "COMMENT ON COLUMN \"%s\".__mlocate__created_by IS 'Created By';\n"
				+ "COMMENT ON COLUMN \"%s\".__mlocate__created_date IS 'Created Date';\n"
				+ "COMMENT ON COLUMN \"%s\".__mlocate__modified_by IS 'Modified By';\n"
				+ "COMMENT ON COLUMN \"%s\".__mlocate__modified_date IS 'Modified Date';\n"
				+ "COMMENT ON COLUMN \"%s\".__mlocate__validated_by IS 'Validated By';\n"
				+ "COMMENT ON COLUMN \"%s\".__mlocate__validated_date IS 'Validated Date';\n";

		layer_colcomments = layer_colcomments.replace("%s", layer_tablename);
		// here x is keysStr

		while (keys.hasNext()) {

			String objStr = (String) keys.next();

			if (!objStr.equals("shp_filename") && !objStr.equals("theme_id") && !objStr.equals("related_layers")
					&& !objStr.equals("page_info")) {

				if (objStr.equals("Layer_Column_Description") && Meta_Layer.get(objStr) instanceof JSONObject)
					layer_colcomments += addcolcomments(layer_tablename, (JSONObject) Meta_Layer.get(objStr));

				else {
					String str = (String) Meta_Layer.get(objStr);
					System.out.println(objStr + ":" + str.length());

					if (objStr.equals("layer_type"))
						continue;
					else if (objStr.equals("min_scale")) {
						Meta_Layer_sql += "\"" + objStr + "\",";
						if (str.equals(""))
							vals += "'5',";
						else
							vals += "'" + str + "',";
					} else if (objStr.equals("max_scale")) {
						if (str.equals(""))
							continue;
						else {
							Meta_Layer_sql += "\"" + objStr + "\",";
							vals += "'" + str.replace("'", "''") + "',";
						}
					} else {
						Meta_Layer_sql += "\"" + objStr + "\",";
						vals += "'" + str.replace("'", "''") + "',";
					}

				}

			}
		}
		Meta_Layer_sql += "\"layer_type\",";
		System.out.println(layer_type);
		vals += String.format("'%s',", layer_type);

		Meta_Layer_sql = Meta_Layer_sql.substring(0, Meta_Layer_sql.length() - 1);
		vals = vals.substring(0, vals.length() - 1);

		Meta_Layer_sql = String.format("%s) values (%s);", Meta_Layer_sql, vals);

		ret.add(Meta_Layer_sql);
		System.out.println(ret.get(0));
		ret.add(layer_colcomments);
		System.out.println(ret.get(1));
		return ret;
	}

	private static String addcolcomments(String tablename, JSONObject theDict) { // check again

		String sql = "";
		for (Object o : theDict.keySet()) {
			String str = (String) o;
			try {
				String str1 = (String) theDict.get(str);
				sql += String.format("COMMENT ON COLUMN \"%s\".\"%s\" IS '%s';\n", tablename, str.toLowerCase(),
						str1.replace("'", "''"));
			} catch (Exception e) {
				System.out.println("@REM # -- " + str);
				System.out.println("@REM # -- " + theDict.get(str));
			}
		}
		return sql;
	}

	private static String createRoles(String layer_tablename) {

		String sql = "\n";

		for (String role : ROLE_TYPES) {

			sql += String.format("insert into role(name) values ('%s %s');\n", layer_tablename, role);
			sql += String.format(
					"insert into permission (rid, perm) values ((select rid from role where name = '%s %s'), '%s');\n",
					layer_tablename, role, Role_perm.get(role));
		}
		sql += "\n";

		return sql;

	}

	private static void insertsql(String fln1, String sql, String sql2) throws FileNotFoundException, IOException {
		String fln2 = fln1 + "new";
		FileInputStream instream = null;
		FileOutputStream outstream = null;
		File f1 = new File(fln1);
		File f2 = new File(fln2);
		FileReader fr = new FileReader(f1);
		FileWriter fileWriter = new FileWriter(f2);
		String s;
		try {
			BufferedReader br = new BufferedReader(fr);

			while ((s = br.readLine()) != null) {

				String trimmedLine = s.trim();

				String replaceString = s.replaceAll("END;\nBEGIN;\n", "");
				if (replaceString.equals("")) {
					fileWriter.write(s);
				} else {
					fileWriter.write(replaceString);
				}

			}
			fileWriter.flush();
			fileWriter.close();
			br.close();
		}

		catch (Exception e) {
			e.printStackTrace();
		}
		fr.close();

		instream = new FileInputStream(f1);
		outstream = new FileOutputStream(f2);

		byte[] buffer = new byte[1000000];

		int length;

		while ((length = instream.read(buffer)) > 0) {
			outstream.write(buffer, 0, length);
		}

		outstream.close();

		System.out.println("File copied successfully!!");

		FileReader fr1 = new FileReader(f1);
		FileWriter fileWriter1 = new FileWriter(f2);

		int i = 0;
		int j = 0;

		BufferedReader reader = new BufferedReader(fr1);
		String line = reader.readLine();
		while (line != null) {
			if (i == 1) {
				fileWriter1.write(sql + "\n");
			}
			if (line.startsWith("CREATE INDEX")) {
				fileWriter1.write("\n");
			}
			if (j == 0 && line.startsWith("COMMIT")) {
				fileWriter1.write(sql2);
				j += 1;
			}
			fileWriter1.write(line);
			if (i == 0)
				fileWriter1.write("set client_encoding = Latin1;\n");

			line = reader.readLine();
			i = i + 1;
		}
		reader.close();
		fileWriter1.flush();
		fileWriter1.close();
		fr1.close();

		f2.getAbsolutePath();

		instream = new FileInputStream(f2);
		outstream = new FileOutputStream(f1);

		byte[] buffer1 = new byte[1000000000];

		int length1;

		while ((length1 = instream.read(buffer1)) > 0) {
			outstream.write(buffer1, 0, length1);
		}

		instream.close();
		outstream.close();

		System.out.println("File copied successfully!!");

	}

	static String string;
	String path;
	String Sql_file_name;
	static String theDictStr = "";
	static int cursr = 0;
	static String GEOMCOL = "__mlocate__topology";
	String shp2pgsql = "shp2pgsql";
	String psql = "psql";
	static String cwdir = System.getProperty("user.dir");
	static int err = 0;
	static int state = 0;
	static String g_layer_tablename = "";
	static Dictionary g_link_tablenames = new Hashtable();
	static String columnnamekeys[] = { "summary_columns", "filter_columns", "search_columns", "editable_columns",
			"title_column", "color_by", "size_by", "linked_column", "layer_column", "resource_column", "table_column",
			"media_columns", "italics_columns", "mandatory_columns", "record_type_column" };
	static int layer_seq;
	static int link_seq;
	static String ROLE_TYPES[] = new String[] { "admin", "validator", "member" };
	String ALLOWED_COLUMNS[] = new String[] { "INTEGER", "TEXT", "DECIMAL", "DATE" };
	static List<String> fl_lines;
	static int j = 0;
	static String pass = "";

	static HashMap<String, String> Role_perm = new HashMap<>();

	HashMap<String, String> SQL_COLUMNS = new HashMap<>();

	static String PATH = System.getProperty("user.dir");

	public Import_layers(String DBNAME, String DBUSER, String path, String Sql_file_name) {
		this.DBNAME = DBNAME;
		this.DBUSER = DBUSER;
		this.path = path;
		this.Sql_file_name = Sql_file_name;

	}

	Import_layers() {
		throw new UnsupportedOperationException("Not supported yet.");

	}

	public static int main_func(Import_layers imp, DBexec database, String dbname, String dbpassword, String dbuser)
			throws IOException, ScriptException, ParseException, InterruptedException, SQLException {

		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("javaScript");
		Import_layers.pass = dbpassword;

		imp.Role_perm.put("admin",
				"create node_mlocate, delete any node_mlocate, delete own node_mlocate, edit any node_mlocate, edit own node_mlocate, create node_mlocate_layerinfo, delete any node_mlocate_layerinfo, delete own node_mlocate_layerinfo, edit any node_mlocate_layerinfo, edit own node_mlocate_layerinfo, access node_mlocate_participation, create node_mlocate_participation, delete any node_mlocate_participation, delete own node_mlocate_participation, edit any node_mlocate_participation, edit own node_mlocate_participation");
		imp.Role_perm.put("validator",
				"create node_mlocate, delete own node_mlocate, edit own node_mlocate, create node_mlocate_layerinfo, delete own node_mlocate_layerinfo, edit own node_mlocate_layerinfo");
		imp.Role_perm.put("member",
				"create node_mlocate, delete own node_mlocate, edit own node_mlocate, create node_mlocate_layerinfo, delete own node_mlocate_layerinfo, edit own node_mlocate_layerinfo");

		imp.SQL_COLUMNS.put("INTEGER", "INTEGER");
		imp.SQL_COLUMNS.put("TEXT", "VARCHAR(1024)");
		imp.SQL_COLUMNS.put("DECIMAL", "float(24)");
		imp.SQL_COLUMNS.put("DATE", "DATE");

		layer_seq = getNextSeqFromDB("\\\"Meta_Layer_layer_id_seq\\\"", dbpassword) + 1;// +1
		System.out.println(layer_seq);

		String directoryName1 = PATH.concat("/layersqls");
		String directoryName2 = PATH.concat("/logs");
		String directoryName3 = PATH.concat("/layers.json");

		File directory1 = new File(directoryName1);
		File directory2 = new File(directoryName2);
		File directory3 = new File(directoryName3);

		if (directory1.exists())
			FileUtils.deleteDirectory(directory1);
		new File(directoryName1).mkdirs();

		if (directory2.exists())
			FileUtils.deleteDirectory(directory2);
		new File(directoryName2).mkdirs();

		if (directory3.exists())
			FileUtils.forceDelete(directory3);

		String y = imp.getLayerInfo(imp.path); // actual string
		JSONParser jsonParser = new JSONParser();
		JSONArray json = (JSONArray) jsonParser.parse(y);
		System.out.println(y);
		String[] lines = y.split(System.getProperty("line.separator"));
		try (FileWriter file1 = new FileWriter(directory3)) {

			file1.write(json.toString());
			file1.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
		File file = new File(imp.Sql_file_name);
		String sql_fl = dbimport(json, file);

		int res = DBexec.main_func_generation(sql_fl, dbname, dbpassword, dbuser);
		return res;

	}

	public static int getNextVal(String SequenceName, String dbpassword) throws IOException {

		String s1 = String.format("PGPASSWORD=" + dbpassword
				+ "  psql -h localhost  -d %s -U %s -t -c \"select nextval('\\\"Meta_Layer_layer_id_seq\\\"')\"",
				DBNAME, DBUSER);

		String s2 = String.format("PGPASSWORD=" + dbpassword
				+ "  psql -h localhost  -d %s -U %s -t -c \"select setval('\\\"Meta_Layer_layer_id_seq\\\"' , (select nextval('\\\"Meta_Layer_layer_id_seq\\\"')-2))\"",
				DBNAME, DBUSER);

		String s3 = String.format("PGPASSWORD=" + dbpassword
				+ "  psql -h localhost  -d %s -U %s -t -c \"select pg_sequences.last_value from pg_sequences where schemaname = 'public' and sequencename = %s\"",
				DBNAME, DBUSER, SequenceName);
		int val = 0;
		String[] arr = { s1, s2, s3 };

		for (String s : arr) {
			System.out.println(s);
			ProcessBuilder pb = new ProcessBuilder();
			pb.command("bash", "-c", s);
			Process process = pb.start();
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String s11 = null;
			while ((s11 = stdInput.readLine()) != null) {
				s11 = s11.trim();
				val = Integer.parseInt(s11);
				System.out.println(val);
				break;
			}
			stdInput.close();
		}
		return val;
	}

	public static int getNextSeqFromDB(String SequenceName, String dbpassword) throws IOException {
		int val = getNextVal(SequenceName, dbpassword);

//		String str = "";
//		str = String.format("PGPASSWORD=" + dbpassword
//				+ "  psql -h localhost  -d %s -U %s -t -c \"select pg_sequences.last_value from pg_sequences where schemaname = 'public' and sequencename = %s\"",
//				DBNAME, DBUSER, SequenceName);
//		ProcessBuilder pb1 = new ProcessBuilder();
//		pb1.command("bash", "-c", str);
//		Process pb_process = pb1.start();
//		BufferedReader stdInput1 = new BufferedReader(new InputStreamReader(pb_process.getInputStream()));
//		System.out.println(stdInput1.readLine());
//		String s12 = null;
//		while ((s12 = stdInput1.readLine()) != null) {
//			System.out.println(str);
//			s12 = s12.trim();
//			return Integer.parseInt(s12);
//		}

		return val;
	}

	public final void validate_column_types(String[] types) {

		for (int i = 0; i < types.length; i++) {

			if (!Arrays.stream(ALLOWED_COLUMNS).anyMatch(types[i]::equals)) {
				System.out.println("unknown column " + types[i]);
				System.exit(2);
			}
			continue;
		}
	}

	public final void validate_column_names(String[] names) {
		String[] NOT_ALLOWED_CHARS = { " ", "\t" };
	}

	public static void convert_to_utf8(File filename) throws IOException {

		String fpath = filename.getAbsolutePath();
		String newfile = fpath + ".bak";
		File newfilename = new File(newfile);

		String[] charsetsToBeTested = { "macgreek", "windows-1253", "ISO-8859-7" };
		Charset charset = detectCharset(filename, charsetsToBeTested);
		FileInputStream fis = new FileInputStream(filename);
		InputStreamReader isr = new InputStreamReader(fis, charset);
		Reader in = new BufferedReader(isr);
		FileOutputStream fos = new FileOutputStream(filename);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
		Writer out = new BufferedWriter(osw);

		int ch;
		while ((ch = in.read()) > -1) {
			out.write(ch);
		}

		out.close();
		in.close();

		FileUtils.copyFile(filename, newfilename);

	}

	public static Charset detectCharset(File f, String[] charsets) {

		Charset charset = null;

		for (String charsetName : charsets) {
			charset = detectCharset(f, Charset.forName(charsetName));
			if (charset != null) {
				break;
			}
		}

		return charset;
	}

	private static Charset detectCharset(File f, Charset charset) {
		try {
			BufferedInputStream input = new BufferedInputStream(new FileInputStream(f));

			CharsetDecoder decoder = charset.newDecoder();
			decoder.reset();

			byte[] buffer = new byte[512];
			boolean identified = false;
			while ((input.read(buffer) != -1) && (!identified)) {
				identified = identify(buffer, decoder);
			}

			input.close();

			if (identified) {
				return charset;
			} else {
				return null;
			}

		} catch (Exception e) {
			return null;
		}
	}

	private static boolean identify(byte[] bytes, CharsetDecoder decoder) {
		try {
			decoder.decode(ByteBuffer.wrap(bytes));
		} catch (CharacterCodingException e) {
			return false;
		}
		return true;
	}

	public String getLayerInfo(String path) {
		String theFinalDictStr = "[\n";
		System.out.println(path);
		theFinalDictStr = walk(path, theFinalDictStr);
		theFinalDictStr += "]\n";

		return theFinalDictStr;
	}

	public static String walk(String path, String theFinalDictStr) {
		String[] path1_sp;
		File root = new File(path);
		System.out.println(root.isDirectory());
		File[] list = root.listFiles();

		if (list == null)
			return " ";

		for (File f : list) {
			if ((f.isDirectory()) && (f.getName().equals("final"))) {

				System.out.println("found final ---- " + root.getAbsolutePath());
				Path rt = Paths.get(root.getAbsolutePath());
				Path filePath = Paths.get(rt.toString(), f.getName());
				String pth1 = filePath.toString();
				System.out.println("pth1 to final : " + pth1);
				String shpfl = "";
				String metadatafl = "";
				if (System.getProperty("os.name") == "nt")
					path1_sp = pth1.split("\\");
				else
					path1_sp = pth1.split("/");

				String layer_name = path1_sp[path1_sp.length - 2];
				if (!new File(PATH.concat(pth1 + "/" + layer_name + "/metadata.txt")).exists()
						|| !new File(PATH.concat(pth1 + "/" + layer_name + "/meta.txt")).exists()) {
					System.setProperty("user.dir", pth1);
					System.out.println("step done");
					System.setProperty(pth1, "user.dir");
				}

				File folder = new File(pth1);
				File[] fls = folder.listFiles();

				for (File flname : fls) {
					System.out.println(flname);
					if (flname.getName().endsWith(".shp")) {
						shpfl = Paths.get(rt.toString(), flname.getName()).toString();
						shpfl = flname.getAbsolutePath();
						System.out.println(shpfl);
					}

					else if (flname.getName().endsWith("metadata.txt") || flname.getName().endsWith(".meta.txt")) {

						metadatafl = flname.getAbsolutePath();
						getlayermetadata(metadatafl, pth1);
					}
				}

				if (err == 1) {
					continue;
				}
				String spc = "    ";
				String str1 = spc + "\"shp_filename\": " + "\"" + shpfl.replace("\\", "\\\\") + "\"," + "\n";

				int indx = theDictStr.lastIndexOf("    },\n");
				System.out.println(theDictStr + "  " + indx);
				theDictStr = theDictStr.substring(0, indx + 7) + str1
						+ theDictStr.substring(indx + 7, theDictStr.length() - 1);
				theFinalDictStr += theDictStr;
				System.out.println(theFinalDictStr);
				theDictStr = "";
				cursr = 0;

			} else if ((f.isDirectory()) && !(f.getName().equals("final"))) {
				walk(f.getAbsolutePath(), theFinalDictStr);
				System.out.println("Dir:" + f.getAbsoluteFile());
			} else {
				System.out.println("File:" + f.getAbsoluteFile());
			}
		}

		return theFinalDictStr;

	}

	public static void getlayermetadata(String filename, String pth) {

		theDictStr += "  {\n";
		try {
			fl_lines = Files.readAllLines(Paths.get(filename));

			while (cursr < fl_lines.size()) {

				String line = fl_lines.get(cursr).trim();
				if (line.startsWith("*")) {
					cursr += 1;
					System.out.println("hello encountered something ");
					getblock(line.replace("*", "").trim(), pth);
					System.out.println(cursr + " value of cursor and line is " + line);
				}

				cursr++;
			}
			theDictStr += "  }\n";
		}

		catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void getblock(String line, String path) {

		if (err == 0) {

			if (line.equals("Meta_Layer")) {
				System.out.println("entered in meta layer");
				Meta_Layer(path);
			}
			if (line.equals("Theme_Layer_Mapping")) {
				System.out.println("theme layer entered");
				Theme_layer_Mapping(path);
			}
			System.out.println("cursr value kya hai " + cursr);

		}

	}

}
