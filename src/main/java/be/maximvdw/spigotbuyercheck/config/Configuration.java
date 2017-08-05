package be.maximvdw.spigotbuyercheck.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * YAML Configuration
 */
public class Configuration {
	// Logging
	private final Logger logger = LoggerFactory.getLogger(Configuration.class);

	/* Configuration version */
	private static int configVersion = 1;
	private static Yaml yaml = new Yaml();
	private static File configFile = null;
	private static Map<String, Object> result = new HashMap<String, Object>();
    private static String folder = "";

	/**
	 * Initialize the configuration
	 * 
	 * @param folder
	 *            folder
	 * @param config
	 *            Config version
	 */
	public Configuration(String folder, int config) {
		Configuration.configVersion = config;
        this.folder = folder;
		logger.info("Configuration version: " + configVersion);
		loadConfig();
	}

	@SuppressWarnings("unchecked")
	public void loadConfig() {
        logger.info("Loading configuration ...");
		configFile = new File(folder,"config.yml");
		if (!configFile.exists()) {
			// Create file
			try {
			    new File(folder).mkdir();
				InputStream jarURL = this.getClass().getResourceAsStream(
						"/config.yml");
				if (jarURL != null) {
                    logger.info("Copying '" + configFile
							+ "' from the resources!");
					copyFile(jarURL, configFile);
				} else {
                    logger.error("Configuration file not found inside the application!");
				}
				jarURL.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (configFile.exists()) {
			// Load the configuration file
			try {
				InputStream ios = new FileInputStream(configFile);
				result = (Map<String, Object>) yaml.load(ios);
				ios.close();
				if (getInt("config") != Configuration.configVersion) {
					// Configuration version mismatch
                    logger.warn("Config version does not match!");
				}
			} catch (FileNotFoundException e) {
				// Error while loading the file
			} catch (IOException e) {
				// Error while closing
			}
		}
	}

	public static File getConfigFile() {
		return configFile;
	}

	public static void setConfigFile(File configFile) {
		Configuration.configFile = configFile;
	}

	@SuppressWarnings("unchecked")
	public static Object get(String path) {
		String[] pathArr = path.split("\\.");
		if (pathArr.length == 0) {
			pathArr = new String[1];
			pathArr[0] = path;
		}
		Object lastObj = result;
		for (int i = 0; i < pathArr.length; i++) {
			lastObj = ((Map<String, Object>) lastObj).get(pathArr[i]);
		}
		return lastObj;
	}

	/**
	 * Get boolean value from configuration
	 * 
	 * @param path
	 *            Configuration path
	 * @return Boolean value
	 */
	public static boolean getBoolean(String path) {
		Object lastObj = get(path);
		if (lastObj instanceof Boolean)
			return (Boolean) lastObj;
		return false;
	}

	/**
	 * Get integer value from configuration
	 * 
	 * @param path
	 *            Configuration path
	 * @return Integer result
	 */
	public static int getInt(String path) {
		Object lastObj = get(path);
		if (lastObj instanceof Integer)
			return (Integer) lastObj;
		return 0;
	}

	/**
	 * Get string value from configuration
	 * 
	 * @param path
	 *            Configuration path
	 * @return String result
	 */
	public static String getString(String path) {
		Object lastObj = get(path);
		if (lastObj instanceof String)
			return (String) lastObj;
		return "";
	}

	/**
	 * Copies a file to a new location.
	 * 
	 * @param in
	 *            InputStream
	 * @param out
	 *            File
	 * 
	 * @throws Exception
	 */
	static private void copyFile(InputStream in, File out) throws Exception {
		InputStream fis = in;
		FileOutputStream fos = new FileOutputStream(out);
		try {
			byte[] buf = new byte[1024];
			int i = 0;
			while ((i = fis.read(buf)) != -1) {
				fos.write(buf, 0, i);
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (fis != null) {
				fis.close();
			}
			if (fos != null) {
				fos.close();
			}
		}
	}
}
