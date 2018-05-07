package com.strandls.naksha.geoserver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
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

	public void uploadShpLayer(InputStream shpInputStream, InputStream dbfInputStream, InputStream metadataInputStream) throws ScriptException, IOException {

		String tmpDirPath = NakshaConfig.getString(TEMP_DIR_PATH) + File.separator + System.currentTimeMillis();
		String shpFilePath = tmpDirPath + File.separator + "shpFile.shp";
		File shpFile = new File(shpFilePath);
		String dbfFilePath = tmpDirPath + File.separator + "dbfFile.dbf";
		File dbfFile = new File(dbfFilePath);
		String metadataFilePath = tmpDirPath + File.separator + "metadataFile.txt";
		File metadataFile = new File(metadataFilePath);

		StringWriter writer = new StringWriter();
		ScriptContext context = new SimpleScriptContext();
		context.setWriter(writer);
		
		logger.info("Trying to upload shp at {}", tmpDirPath);
		try {
			FileUtils.copyInputStreamToFile(shpInputStream, shpFile);
			FileUtils.copyInputStreamToFile(dbfInputStream, dbfFile);
			FileUtils.copyInputStreamToFile(metadataInputStream, metadataFile);
			
			context.setAttribute("dbname", NakshaConfig.getString(GEOSERVER_DBNAME), ScriptContext.ENGINE_SCOPE);
			context.setAttribute("dbuser", NakshaConfig.getString(GEOSERVER_DBUSER), ScriptContext.ENGINE_SCOPE);
			context.setAttribute("datapath", tmpDirPath, ScriptContext.ENGINE_SCOPE);
			Path scriptPath = Paths.get("scripts/data_import.py");
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
