package de.simonsator.partyandfriends.extensions.luckperms.prefixes;

import de.simonsator.partyandfriends.api.PAFExtension;
import de.simonsator.partyandfriends.api.pafplayers.DisplayNameProvider;
import de.simonsator.partyandfriends.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.partyandfriends.api.pafplayers.PAFPlayer;
import de.simonsator.partyandfriends.api.pafplayers.PAFPlayerClass;
import de.simonsator.partyandfriends.extensions.luckperms.prefixes.configuration.PPLPConfiguration;
import de.simonsator.partyandfriends.utilities.ConfigurationCreator;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class PrefixesPermsPlugin extends PAFExtension implements DisplayNameProvider {
	private UserManager userManager;
	private String displayNameTemplate;
	private HashMap<UUID, String> cache = new HashMap<>();

	@Override
	public void onEnable() {
		try {
			ConfigurationCreator config = new PPLPConfiguration(new File(getConfigFolder(), "config.yml"),
					this);
			displayNameTemplate = config.getString("DisplayName");
			PAFPlayerClass.setDisplayNameProvider(this);
			userManager = LuckPermsProvider.get().getUserManager();
			ProxyServer.getInstance().getScheduler().schedule(this, () -> {
				cache = new HashMap<>();
			}, config.getLong("Cache.TimeInSeconds"), config.getLong("Cache.TimeInSeconds"), TimeUnit.SECONDS);
			registerAsExtension();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getDisplayName(PAFPlayer pPlayer, User luckpermsUser) {
		if (luckpermsUser == null)
			return pPlayer.getName();
		String prefix = luckpermsUser.getCachedData().getMetaData().getPrefix();
		if (prefix == null)
			prefix = "";
		String suffix = luckpermsUser.getCachedData().getMetaData().getSuffix();
		if (suffix == null)
			suffix = "";
		String displayName = ChatColor.translateAlternateColorCodes('&',
				displayNameTemplate.replaceAll("%LUCKPERMS_PREFIX%", prefix).
						replaceAll("%LUCKPERMS_SUFFIX%", suffix).replaceAll("%PLAYER_NAME%", pPlayer.getName()));
		cache.put(pPlayer.getUniqueId(), displayName);
		return displayName;
	}

	@Override
	public String getDisplayName(PAFPlayer pPlayer) {
		String displayName = cache.get(pPlayer.getUniqueId());
		if (displayName != null)
			return displayName;
		try {
			User luckpermsUser = userManager.loadUser(pPlayer.getUniqueId()).get();
			return getDisplayName(pPlayer, luckpermsUser);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return pPlayer.getName();
	}

	@Override
	public String getDisplayName(OnlinePAFPlayer pPlayer) {
		String displayName = cache.get(pPlayer.getUniqueId());
		if (displayName != null)
			return displayName;
		return getDisplayName(pPlayer, userManager.getUser(pPlayer.getUniqueId()));
	}
}