package de.yamori.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Config {
	
	private final static String defaultConfigFileFolder = System.getProperty("user.home") + File.separator + ".yamori";
	private final static String defaultConfigFilename = "config.properties";
	
	private static Config INSTANCE = new Config();
	
	private String dvdDevice = "/dev/dvd";
	private String tmp = "/tmp";
	private String outputPath = System.getProperty("user.home");
	
	public static Config getInstance() {
		return INSTANCE;
	}
	
	public static void setInstance(Config config) {
		INSTANCE = config;
	}

	private Config() {
		// hide
	}
	
	public String getDvdDevice() {
		return dvdDevice;
	}

	public void setDvdDevice(String dvdDevice) {
		this.dvdDevice = dvdDevice;
	}
	
	public String getTmp() {
		return tmp;
	}
	
	public void setTmp(String tmp) {
		this.tmp = tmp;
	}
	
	public String getOutputPath() {
		return outputPath;
	}
	
	public void setOutputPath(String outputPath) {
		this.outputPath = outputPath;
	}
	
	public File getConfigFolder() {
		return new File(defaultConfigFileFolder);
	}
	
	public static Config loadConfig() {
		Properties properties = new Properties();
		File folder = new File(defaultConfigFileFolder);
		if (folder.exists() && folder.canRead()) {
			File file = new File(folder, defaultConfigFilename);
			if (file.exists() && file.canRead()) {
				try (InputStream in = new FileInputStream(file)) {
					properties.load(in);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		Config config = new Config();
		config.setDvdDevice(properties.getProperty("io.dvdDevice", config.getDvdDevice()));
		config.setTmp(properties.getProperty("io.tmp", config.getTmp()));
		config.setOutputPath(properties.getProperty("io.outputPath", config.getOutputPath()));
		return config;
	}
	
	public static void storeConfig(Config config) {
		Properties properties = new Properties();
		properties.setProperty("io.dvdDevice", config.getDvdDevice());
		properties.setProperty("io.tmp", config.getTmp());
		properties.setProperty("io.outputPath", config.getOutputPath());
		
		File folder = new File(defaultConfigFileFolder);
		if (!folder.exists()) {
			folder.mkdir();
		}
		if (folder.exists() && folder.canRead()) {
			File file = new File(folder, defaultConfigFilename);
			try (OutputStream out = new FileOutputStream(file)) {
				properties.store(out, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}