package de.simonsator.partyandfriends.extensions.luckperms.prefixes.common;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LuckpermsPrefixManager {
	private final Pattern HEX_PATTERN = Pattern.compile("&#" + "([A-Fa-f0-9]{6})");
	private final UserManager USER_MANAGER;
	private final String DISPLAY_NAME_TEMPLATE;
	private HashMap<UUID, String> cache;
	private final boolean REFORMAT_HEX_COLOR;

	public LuckpermsPrefixManager(String displayNameTemplate, boolean reformatHexColor, boolean useCache) {
		this.USER_MANAGER = LuckPermsProvider.get().getUserManager();
		this.DISPLAY_NAME_TEMPLATE = displayNameTemplate;
		if (useCache) {
			this.cache = new HashMap<>();
		} else {
			cache = null;
		}
		this.REFORMAT_HEX_COLOR = reformatHexColor;
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

	private String translateColorCodes(String textToTranslate) {
		char[] b = textToTranslate.toCharArray();

		for (int i = 0; i < b.length - 1; ++i) {
			if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
				b[i] = 167;
				b[i + 1] = Character.toLowerCase(b[i + 1]);
			}
		}

		return new String(b);
	}

	private String getDisplayName(String pPlayerName, UUID pPlayerUUID, User luckpermsUser) {
		if (luckpermsUser == null)
			return pPlayerName;
		String prefix = luckpermsUser.getCachedData().getMetaData().getPrefix();
		if (prefix == null)
			prefix = "";
		String suffix = luckpermsUser.getCachedData().getMetaData().getSuffix();
		if (suffix == null)
			suffix = "";
		String displayName = DISPLAY_NAME_TEMPLATE.replaceAll("%LUCKPERMS_PREFIX%", prefix).
				replaceAll("%LUCKPERMS_SUFFIX%", suffix).replaceAll("%PLAYER_NAME%", pPlayerName);
		if (REFORMAT_HEX_COLOR)
			displayName = fixHexColors(displayName);
		displayName = translateColorCodes(displayName);
		if (cache != null)
			cache.put(pPlayerUUID, displayName);
		return displayName;
	}

	public String getDisplayNameOfflinePlayer(String pPlayerUserName, UUID pPlayerUUID) {
		if (cache != null) {
			String displayName = cache.get(pPlayerUUID);
			if (displayName != null)
				return displayName;
		}
		try {
			return getDisplayName(pPlayerUserName, pPlayerUUID, USER_MANAGER.loadUser(pPlayerUUID).get());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return pPlayerUserName;
	}

	public String getDisplayNameOnlinePlayer(String pPlayerUserName, UUID pPlayerUUID) {
		if (cache != null) {
			String displayName = cache.get(pPlayerUUID);
			if (displayName != null)
				return displayName;
		}
		return getDisplayName(pPlayerUserName, pPlayerUUID, USER_MANAGER.getUser(pPlayerUserName));
	}

	public void resetCache() {
		cache = new HashMap<>();
	}

	public boolean usesCache() {
		return cache != null;
	}
}
