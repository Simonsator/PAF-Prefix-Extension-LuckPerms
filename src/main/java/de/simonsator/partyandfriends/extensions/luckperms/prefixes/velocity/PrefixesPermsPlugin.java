package de.simonsator.partyandfriends.extensions.luckperms.prefixes.velocity;


import de.simonsator.partyandfriends.extensions.luckperms.prefixes.velocity.configuration.PPLPConfiguration;
import de.simonsator.partyandfriends.velocity.api.PAFExtension;
import de.simonsator.partyandfriends.velocity.api.pafplayers.DisplayNameProvider;
import de.simonsator.partyandfriends.velocity.api.pafplayers.OnlinePAFPlayer;
import de.simonsator.partyandfriends.velocity.api.pafplayers.PAFPlayer;
import de.simonsator.partyandfriends.velocity.api.pafplayers.PAFPlayerClass;
import de.simonsator.partyandfriends.velocity.utilities.ConfigurationCreator;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrefixesPermsPlugin extends PAFExtension implements DisplayNameProvider {
	private final Pattern HEX_PATTERN = Pattern.compile("&#" + "([A-Fa-f0-9]{6})");
	private UserManager userManager;
	private String displayNameTemplate;
	private HashMap<UUID, String> cache;
	private boolean reformatHexColor;

	public PrefixesPermsPlugin(Path folder) {
		super(folder);
	}

	public static String translateAlternateColorCodes(char altColorChar, String textToTranslate) {
		char[] b = textToTranslate.toCharArray();

		for (int i = 0; i < b.length - 1; ++i) {
			if (b[i] == altColorChar && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
				b[i] = 167;
				b[i + 1] = Character.toLowerCase(b[i + 1]);
			}
		}

		return new String(b);
	}

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
				PrefixesPermsPluginLoader.server.getScheduler().buildTask(this, () -> cache = new HashMap<>()).delay(config.getLong("Cache.TimeInSeconds"), TimeUnit.SECONDS).schedule();
			}
			registerAsExtension();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return "LuckPerm-Display-Names-For-PAF";
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
		displayName = translateAlternateColorCodes('&', displayName);
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
			return getDisplayName(pPlayer, userManager.loadUser(pPlayer.getUniqueId()).get());
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