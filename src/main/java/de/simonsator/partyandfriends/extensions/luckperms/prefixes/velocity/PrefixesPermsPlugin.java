package de.simonsator.partyandfriends.extensions.luckperms.prefixes.velocity;


import de.simonsator.partyandfriends.extensions.luckperms.prefixes.common.LuckpermsPrefixManager;
import de.simonsator.partyandfriends.extensions.luckperms.prefixes.velocity.configuration.PPLPConfiguration;
import de.simonsator.partyandfriends.velocity.api.PAFExtension;
import de.simonsator.partyandfriends.velocity.api.pafplayers.DisplayNameProvider;
import de.simonsator.partyandfriends.velocity.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.partyandfriends.velocity.api.pafplayers.PAFPlayer;
import de.simonsator.partyandfriends.velocity.api.pafplayers.PAFPlayerClass;
import de.simonsator.partyandfriends.velocity.utilities.ConfigurationCreator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class PrefixesPermsPlugin extends PAFExtension implements DisplayNameProvider {
	private LuckpermsPrefixManager prefixManager;

	public PrefixesPermsPlugin(Path folder) {
		super(folder);
	}

	@Override
	public void onEnable() {
		try {
			ConfigurationCreator config = new PPLPConfiguration(new File(getConfigFolder(), "config.yml"),
					this);
			prefixManager = new LuckpermsPrefixManager(config.getString("DisplayName"), config.getBoolean("ReformatHexColors"), config.getBoolean("Cache.Activated"));
			if (prefixManager.usesCache()) {
				PrefixesPermsPluginLoader.server.getScheduler().buildTask(this, () -> prefixManager.resetCache()).delay(config.getLong("Cache.TimeInSeconds"), TimeUnit.SECONDS).schedule();
			}
			PAFPlayerClass.setDisplayNameProvider(this);
			registerAsExtension();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return "LuckPerm-Display-Names-For-PAF";
	}

	@Override
	public String getDisplayName(PAFPlayer pPlayer) {
		return prefixManager.getDisplayNameOfflinePlayer(pPlayer.getName(), pPlayer.getUniqueId());
	}

	@Override
	public String getDisplayName(OnlinePAFPlayer pPlayer) {
		return prefixManager.getDisplayNameOnlinePlayer(pPlayer.getName(), pPlayer.getUniqueId());
	}
}