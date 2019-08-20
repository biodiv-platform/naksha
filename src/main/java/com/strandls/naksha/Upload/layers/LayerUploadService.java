package com.strandls.naksha.Upload.layers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import com.strandls.naksha.NakshaConfig;

import javax.script.ScriptException;
import com.strandls.naksha.Layers.Scripts.Import_data;

import org.apache.commons.io.FileUtils;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LayerUploadService {

	private final Logger logger = LoggerFactory.getLogger(LayerUploadService.class);
	private static final String TEMP_DIR_PATH = "tmpDir.path";
	private static final String GEOSERVER_DBNAME = "geoserver.dbname";
	private static final String GEOSERVER_DBUSER = "geoserver.dbuser";
	private static final String GEOSERVER_PASS = "db.password";
	
	
	public int uploadShpLayer(InputStream shpInputStream, InputStream dbfInputStream, InputStream metadataInputStream,
			InputStream shxInputStream, String layerName) throws IOException, ClassNotFoundException, SQLException, ScriptException, ParseException, InterruptedException {
			
		String dataPath = NakshaConfig.getString(TEMP_DIR_PATH) + File.separator + System.currentTimeMillis();
		String tmpDirPath = dataPath + File.separator + "final";
		String shpFilePath = tmpDirPath + File.separator + layerName + ".shp";
		File shpFile = new File(shpFilePath);
		String dbfFilePath = tmpDirPath + File.separator + layerName + ".dbf";
		File dbfFile = new File(dbfFilePath);
		String metadataFilePath = tmpDirPath + File.separator + "metadata.txt";
		File metadataFile = new File(metadataFilePath);
		String shxFilePath = tmpDirPath + File.separator + layerName + ".shx";
		File shxFile = new File(shxFilePath);

		Process p;
		logger.info("Trying to upload shp at {}", dataPath);
		try {
			FileUtils.copyInputStreamToFile(shpInputStream, shpFile);
			FileUtils.copyInputStreamToFile(dbfInputStream, dbfFile);
			FileUtils.copyInputStreamToFile(metadataInputStream, metadataFile);
			FileUtils.copyInputStreamToFile(shxInputStream, shxFile);

			int command = getCommand(dataPath);
			logger.info("Finished upload shp at {}", tmpDirPath);
			return command;

		} catch (IOException e) {
			logger.error("Error while creating data files.", e);
			
			throw e;
		}	
	}

	private void logScriptOutput(Process p) throws IOException {
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
		BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
		String s;
		while ((s = stdInput.readLine()) != null) {
			logger.info(s);
		}
		while ((s = stdError.readLine()) != null) {
			logger.error(s);
		}
	}

	private int getCommand(String dataPath) throws ClassNotFoundException, SQLException, IOException, ScriptException, ParseException, InterruptedException {
		String dbname = NakshaConfig.getString(GEOSERVER_DBNAME);
		String dbuser = NakshaConfig.getString(GEOSERVER_DBUSER);
		String dbpassword = NakshaConfig.getString(GEOSERVER_PASS);
		Import_data impo = new Import_data();
		int j = impo.main_data(dbname , dbuser ,dataPath, dbpassword);
		return j;
	}

//	public List<LayerAttributes> getLayerAttributes(String layerName) {
//		return layerDAO.getLayerAttributes(layerName);
//	}
//
//	public List<String> getLayerNamesWithTag(String tag) {
//		if (tag == null || tag.isEmpty())
//			return new ArrayList<>();
//
//		return layerDAO.getLayerNamesWithTag(tag);
//	}

}
