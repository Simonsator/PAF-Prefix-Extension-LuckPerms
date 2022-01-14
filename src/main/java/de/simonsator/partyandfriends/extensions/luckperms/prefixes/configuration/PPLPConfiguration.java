package de.simonsator.partyandfriends.extensions.luckperms.prefixes.configuration;

import de.simonsator.partyandfriends.api.PAFPluginBase;
import de.simonsator.partyandfriends.utilities.ConfigurationCreator;

import java.io.File;
import java.io.IOException;

public class PPLPConfiguration extends ConfigurationCreator {
	public PPLPConfiguration(File file, PAFPluginBase pPlugin) throws IOException {
		super(file, pPlugin);
		readFile();
		loadDefaults();
		saveFile();
	}

	private void loadDefaults() {
		set("DisplayName", "&e<%LUCKPERMS_PREFIX%&e> %PLAYER_NAME% &e<%LUCKPERMS_SUFFIX%&e>");
		set("Cache.Activated", true);
		set("Cache.TimeInSeconds", 240);
	}

}
