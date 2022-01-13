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
		set("DisplayName", "<%LUCKPERMS_PREFIX%> %PLAYER_NAME% <%LUCKPERMS_SUFFIX%>");
	}

}
