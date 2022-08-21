package de.simonsator.partyandfriends.extensions.luckperms.prefixes.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import de.simonsator.partyandfriends.velocity.VelocityExtensionLoadingInfo;
import de.simonsator.partyandfriends.velocity.main.PAFPlugin;

import java.nio.file.Path;

@Plugin(id = "luckperm-display-names-for-paf", name = "LuckPerm-Display-Names-For-PAF", version = "1.0.3-RELEASE",
        url = "https://www.spigotmc.org/resources/luckperms-display-names-for-party-and-friends-for-bungeecord.99298/", description = "An add-on for party and friends to add display names from luckperm to the names", authors = {"JT122406", "Simonsator"}, dependencies = {@Dependency(id = "partyandfriends")})
public class PrefixesPermsPluginLoader {

    public static ProxyServer server = null;
    private final Path folder;

    @Inject
    public PrefixesPermsPluginLoader(@DataDirectory final Path folder, ProxyServer server) {
        PrefixesPermsPluginLoader.server = server;
        this.folder = folder;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        PAFPlugin.loadExtension(new VelocityExtensionLoadingInfo(new PrefixesPermsPlugin(folder),
                "luckperm-display-names-for-paf",
                "LuckPerm-Display-Names-For-PAF",
                "1.0.3-RELEASE", "JT122406"));
    }
}
