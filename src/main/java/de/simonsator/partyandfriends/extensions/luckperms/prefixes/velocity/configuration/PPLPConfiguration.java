package de.simonsator.partyandfriends.extensions.luckperms.prefixes.velocity.configuration;



import de.simonsator.partyandfriends.velocity.api.PAFPluginBase;
import de.simonsator.partyandfriends.velocity.utilities.ConfigurationCreator;

import java.io.File;
import java.io.IOException;

public class PPLPConfiguration extends ConfigurationCreator {
	public PPLPConfiguration(File file, PAFPluginBase pPlugin) throws IOException {
		super(file, pPlugin, true);
		copyFromJar();
		readFile();
		loadDefaults();
		saveFile();
		process();
	}

	private void loadDefaults() {
		set("DisplayName", "&e<%LUCKPERMS_PREFIX%&e> %PLAYER_NAME% &e<%LUCKPERMS_SUFFIX%&e>");
		set("ReformatHexColors", false);
		set("Cache.Activated", true);
		set("Cache.TimeInSeconds", 240);
	}

}
