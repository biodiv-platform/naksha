package com.strandls.naksha.geoserver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

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
			InputStream shxInputStream, String layerName) throws ScriptException, IOException, URISyntaxException {

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

		StringWriter writer = new StringWriter();
		ScriptContext context = new SimpleScriptContext();
		context.setWriter(writer);

		logger.info("Trying to upload shp at {}", dataPath);
		try {
			FileUtils.copyInputStreamToFile(shpInputStream, shpFile);
			FileUtils.copyInputStreamToFile(dbfInputStream, dbfFile);
			FileUtils.copyInputStreamToFile(metadataInputStream, metadataFile);
			FileUtils.copyInputStreamToFile(shxInputStream, shxFile);

			context.setAttribute("dbname", NakshaConfig.getString(GEOSERVER_DBNAME), ScriptContext.ENGINE_SCOPE);
			context.setAttribute("dbuser", NakshaConfig.getString(GEOSERVER_DBUSER), ScriptContext.ENGINE_SCOPE);
			context.setAttribute("datapath", dataPath, ScriptContext.ENGINE_SCOPE);

			URI scriptUri = LayerService.class.getClassLoader().getResource("scripts/data_import.py").toURI();
			Path scriptPath = Paths.get(scriptUri);
			Reader scriptReader = Files.newBufferedReader(scriptPath);
			scriptEngine.eval(scriptReader, context);

		} catch (ScriptException e) {
			logger.error("Error while uploading shp file", e);
			throw e;
		} catch (IOException e) {
			logger.error("Error while creating data files.", e);
			throw e;
		} finally {
			String debug = writer.toString();
			logger.debug("Shp upload script execution- {}", debug);
		}
		logger.info("Successfully uploaded shp at {}", tmpDirPath);
	}

}
