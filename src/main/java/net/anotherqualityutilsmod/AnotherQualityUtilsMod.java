package net.anotherqualityutilsmod;

import net.anotherqualityutilsmod.command.TeleportOfflinePlayers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

public class AnotherQualityUtilsMod implements ModInitializer {
	public static final String MOD_ID = "aqum";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID.toUpperCase(Locale.ROOT));
	private static MinecraftServer minecraftServer;

	@Override
	public void onInitialize() {
		TeleportOfflinePlayers.register();
		ServerLifecycleEvents.SERVER_STARTING.register(this::onLogicalServerStarting);
	}

	private void onLogicalServerStarting(MinecraftServer server) {
		minecraftServer = server;
	}

	public static MinecraftServer getMinecraftServer() {
		return minecraftServer;
	}

}
