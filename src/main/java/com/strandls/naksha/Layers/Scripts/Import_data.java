/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strandls.naksha.Layers.Scripts;

import static com.strandls.naksha.Layers.Scripts.Import_layers.main_func;
import static com.strandls.naksha.Layers.Scripts.gen_geoserver_cache_conf.generate_cache;
import static com.strandls.naksha.Layers.Scripts.generate_geoserver_layers.geoserver_func;
import static com.strandls.naksha.Layers.Scripts.generate_geoserver_styles.generate_styles;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.script.ScriptException;

import org.json.simple.parser.ParseException;

import com.strandls.naksha.NakshaConfig;

/**
 *
 * @author humeil
 */
public class Import_data {

	@Inject
	DBexec database;

	String dbname;
	String dbuser;
	String datapath;
	String dbpassword;

	String tmp_dir_path = NakshaConfig.getString("tmpDir.path");

	public int main_data(String dbname, String dbuser, String dataPath, String dbpassword)
			throws ClassNotFoundException, SQLException, IOException, ScriptException, ParseException,
			InterruptedException {

		this.dbname = dbname;
		this.dbuser = dbuser;
		this.datapath = dataPath;
		this.dbpassword = dbpassword;

		System.getProperty("user.dir");
		System.setProperty("user.dir", tmp_dir_path);

		String sql_file_name = "sql_cmds";

		Import_layers imp = new Import_layers(dbname, dbuser, datapath, sql_file_name);
		int end_res = main_func(imp, database, dbname, dbpassword, dbuser);
		generate_geoserver_layers layer_obj = new generate_geoserver_layers();
		generate_geoserver_styles styles_obj = new generate_geoserver_styles();
		generate_styles(dbname, dbuser, dbpassword);
		geoserver_func(dbname, dbuser, dbpassword);
		generate_cache(dbname, dbuser, dbpassword);
		return end_res;
	}

	private static String getOutput(Process process) throws IOException {

		BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String s = null;
		while ((s = stdInput.readLine()) != null) {
			System.out.println(s);
		}
		return s;
	}

	public static List<String> getAllFiles(File curDir) {

		List<String> layers = new ArrayList<>();
		File[] filesList = curDir.listFiles();
		for (File f : filesList) {
			if (f.isDirectory())
				getAllFiles(f);
			if (f.isFile()) {
				layers.add(f.getName().replace(".sql", ""));
			}
		}
		return layers;
	}

}
