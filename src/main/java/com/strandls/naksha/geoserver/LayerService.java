package com.strandls.naksha.geoserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.inject.Inject;
import javax.script.ScriptEngine;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.naksha.NakshaConfig;

public class LayerService {

	@Inject
	ScriptEngine scriptEngine;

	private final Logger logger = LoggerFactory.getLogger(LayerService.class);

	private static final String TEMP_DIR_PATH = "tmpDir.path";
	private static final String GEOSERVER_DBNAME = "geoserver.dbname";
	private static final String GEOSERVER_DBUSER = "geoserver.dbuser";

	public void uploadShpLayer(InputStream shpInputStream, InputStream dbfInputStream, InputStream metadataInputStream,
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

			String dbname = NakshaConfig.getString(GEOSERVER_DBNAME);
			String dbuser = NakshaConfig.getString(GEOSERVER_DBUSER);
			p = Runtime.getRuntime().exec("python scripts/data_import.py " + dbname + " " + dbuser + " " + dataPath);

		} catch (IOException e) {
			logger.error("Error while creating data files.", e);
			throw e;
		}

		BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));
		BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));
		String s;
        while ((s = stdInput.readLine()) != null) {
            logger.info(s);
        }
        while ((s = stdError.readLine()) != null) {
            logger.error(s);
        }

		logger.info("Finished upload shp at {}", tmpDirPath);
	}

}
