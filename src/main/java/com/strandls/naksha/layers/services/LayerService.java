package com.strandls.naksha.layers.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.script.ScriptEngine;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.naksha.NakshaConfig;
import com.strandls.naksha.dao.LayerDAO;
import com.strandls.naksha.layers.models.LayerAttributes;

/**
 * All services related to layers in db
 * 
 * @author mukund
 *
 */
public class LayerService {

	@Inject
	ScriptEngine scriptEngine;

	@Inject
	LayerDAO layerDAO;

	private final Logger logger = LoggerFactory.getLogger(LayerService.class);

	private static final String TEMP_DIR_PATH = "tmpDir.path";
	private static final String GEOSERVER_DBNAME = "geoserver.dbname";
	private static final String GEOSERVER_DBUSER = "geoserver.dbuser";

	public int uploadShpLayer(InputStream shpInputStream, InputStream dbfInputStream, InputStream metadataInputStream,
			InputStream shxInputStream, String layerName) throws IOException {

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

			String command = getCommand(dataPath);
			p = Runtime.getRuntime().exec(command);

		} catch (IOException e) {
			logger.error("Error while creating data files.", e);
			throw e;
		}

		logScriptOutput(p);
		logger.info("Finished upload shp at {}", tmpDirPath);

		return p.exitValue();
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

	private String getCommand(String dataPath) {
		String dbname = NakshaConfig.getString(GEOSERVER_DBNAME);
		String dbuser = NakshaConfig.getString(GEOSERVER_DBUSER);
		String scriptPath = LayerService.class.getClassLoader().getResource("scripts/data_import.py").getPath();

		StringBuilder builder = new StringBuilder();
		builder.append("python");
		builder.append(" ");
		builder.append(scriptPath);
		builder.append(" ");
		builder.append(dbname);
		builder.append(" ");
		builder.append(dbuser);
		builder.append(" ");
		builder.append(dataPath);

		return builder.toString();
	}

	public List<LayerAttributes> getLayerAttributes(String layerName) {
		return layerDAO.getLayerAttributes(layerName);
	}

	public List<String> getLayerNamesWithTag(String tag) {
		if(tag == null || tag.isEmpty())
			return new ArrayList<>();

		return layerDAO.getLayerNamesWithTag(tag);
	}
}
