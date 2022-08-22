package de.simonsator.partyandfriends.extensions.luckperms.prefixes.bungee;

import de.simonsator.partyandfriends.api.PAFExtension;
import de.simonsator.partyandfriends.api.pafplayers.DisplayNameProvider;
import de.simonsator.partyandfriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.partyandfriends.api.pafplayers.PAFPlayer;
import de.simonsator.partyandfriends.api.pafplayers.PAFPlayerClass;
import de.simonsator.partyandfriends.extensions.luckperms.prefixes.bungee.configuration.PPLPConfiguration;
import de.simonsator.partyandfriends.extensions.luckperms.prefixes.common.LuckpermsPrefixManager;
import de.simonsator.partyandfriends.utilities.ConfigurationCreator;
import net.md_5.bungee.api.ProxyServer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class PrefixesPermsPlugin extends PAFExtension implements DisplayNameProvider {
	private LuckpermsPrefixManager prefixManager;

	@Override
	public void onEnable() {
		try {
			ConfigurationCreator config = new PPLPConfiguration(new File(getConfigFolder(), "config.yml"),
					this);
			prefixManager = new LuckpermsPrefixManager(config.getString("DisplayName"), config.getBoolean("ReformatHexColors"), config.getBoolean("Cache.Activated"));
			if (prefixManager.usesCache()) {
				ProxyServer.getInstance().getScheduler().schedule(this, () -> prefixManager.resetCache(), config.getLong(
						"Cache.TimeInSeconds"), config.getLong("Cache.TimeInSeconds"), TimeUnit.SECONDS);
			}
			PAFPlayerClass.setDisplayNameProvider(this);
			registerAsExtension();
		} catch (IOException e) {
			e.printStackTrace();
		}
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