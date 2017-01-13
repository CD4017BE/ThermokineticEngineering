package cd4017be.thermokin;

import java.io.File;
import java.io.IOException;

import net.minecraftforge.fml.common.FMLLog;

import org.apache.logging.log4j.Level;

import cd4017be.lib.ConfigurationFile;

public class Config {

	public static ConfigurationFile data = new ConfigurationFile();

	public static void loadConfig(File file) {
		if (file != null) {
			FMLLog.log("thermokin", Level.INFO, "Loading config from File");
			try {
				data.load(file);
			} catch (IOException e) {
				FMLLog.log("thermokin", Level.WARN, e, "Config file loading failed!");
			}
		} else {
			FMLLog.log("thermokin", Level.WARN, "No config data loaded!");
		}
		
		//TODO config initialization
	}

}
