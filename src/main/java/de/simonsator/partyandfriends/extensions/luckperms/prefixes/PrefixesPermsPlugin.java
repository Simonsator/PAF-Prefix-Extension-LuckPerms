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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrefixesPermsPlugin extends PAFExtension implements DisplayNameProvider {
	private UserManager userManager;
	private String displayNameTemplate;
	private HashMap<UUID, String> cache;
	private boolean reformatHexColor;
	private final Pattern HEX_PATTERN = Pattern.compile("&#" + "([A-Fa-f0-9]{6})");

	@Override
	public void onEnable() {
		try {
			ConfigurationCreator config = new PPLPConfiguration(new File(getConfigFolder(), "config.yml"),
					this);
			displayNameTemplate = config.getString("DisplayName");
			reformatHexColor = config.getBoolean("ReformatHexColors");
			userManager = LuckPermsProvider.get().getUserManager();
			PAFPlayerClass.setDisplayNameProvider(this);
			if (config.getBoolean("Cache.Activated")) {
				cache = new HashMap<>();
				ProxyServer.getInstance().getScheduler().schedule(this, () -> cache = new HashMap<>(), config.getLong(
						"Cache.TimeInSeconds"), config.getLong("Cache.TimeInSeconds"), TimeUnit.SECONDS);
			}
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
		String displayName = displayNameTemplate.replaceAll("%LUCKPERMS_PREFIX%", prefix).
				replaceAll("%LUCKPERMS_SUFFIX%", suffix).replaceAll("%PLAYER_NAME%", pPlayer.getName());
		if (reformatHexColor)
			displayName = fixHexColors(displayName);
		displayName = ChatColor.translateAlternateColorCodes('&', displayName);
		if (cache != null)
			cache.put(pPlayer.getUniqueId(), displayName);
		return displayName;
	}

	@Override
	public String getDisplayName(PAFPlayer pPlayer) {
		if (cache != null) {
			String displayName = cache.get(pPlayer.getUniqueId());
			if (displayName != null)
				return displayName;
		}
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
		if (cache != null) {
			String displayName = cache.get(pPlayer.getUniqueId());
			if (displayName != null)
				return displayName;
		}
		return getDisplayName(pPlayer, userManager.getUser(pPlayer.getUniqueId()));
	}

	private String fixHexColors(String pMessage) {
		Matcher matcher = HEX_PATTERN.matcher(pMessage);
		StringBuffer buffer = new StringBuffer(pMessage.length() + 4 * 8);
		while (matcher.find()) {
			String group = matcher.group(1);
			String COLOR_CHAR = "§";
			matcher.appendReplacement(buffer, COLOR_CHAR + "x"
					+ COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
					+ COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
					+ COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
			);
		}
		return matcher.appendTail(buffer).toString();
	}
}