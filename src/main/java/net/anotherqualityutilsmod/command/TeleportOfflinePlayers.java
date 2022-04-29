package net.anotherqualityutilsmod.command;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.serialization.Dynamic;
import net.anotherqualityutilsmod.AnotherQualityUtilsMod;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;

import java.io.File;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;


public class TeleportOfflinePlayers {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            LiteralCommandNode node = dispatcher.register(literal("teleportofflineplayer")
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                    .then(argument("target", GameProfileArgumentType.gameProfile())
                            .then(argument("location", BlockPosArgumentType.blockPos())
                                    .executes(TeleportOfflinePlayers::doTeleport)
                                    .build()
                            )
                            .executes(TeleportOfflinePlayers::doTeleportToSpawn)
                            .build()
                    )
            );
            dispatcher.register(literal("tpo")
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                    .redirect(node)
            );
        });
    }

    private static int doTeleportToSpawn(CommandContext<ServerCommandSource> context){
        try {
            ServerWorld overworld = AnotherQualityUtilsMod.getMinecraftServer().getOverworld();
            ServerPlayerEntity target = getRequestedPlayer(context);
            String playerName = target.getGameProfile().getName();

            target.setPos(overworld.getSpawnPos().getX(),overworld.getSpawnPos().getY(),overworld.getSpawnPos().getZ());
            target.setWorld(overworld);
            savePlayerData(target);

            context.getSource().sendFeedback(Text.of("Successfully Reset " + playerName + " to Overworld Spawn"), true);
            return Command.SINGLE_SUCCESS;
        }catch(Exception e){
            context.getSource().sendFeedback(Text.of("Teleport Failed"), false);
            AnotherQualityUtilsMod.LOGGER.error(e);
            return -1;
        }
    }

    private static int doTeleport(CommandContext<ServerCommandSource> context){
        try {
            ServerWorld overworld = AnotherQualityUtilsMod.getMinecraftServer().getOverworld();
            ServerPlayerEntity target = getRequestedPlayer(context);
            String playerName = target.getGameProfile().getName();
            BlockPos cords = BlockPosArgumentType.getBlockPos(context, "location");

            target.setPos(cords.getX(), cords.getY(), cords.getZ());
            savePlayerData(target);

            context.getSource().sendFeedback(Text.of("Successfully Reset " + playerName + " " + cords.toString()), true);
            return Command.SINGLE_SUCCESS;
        }catch(Exception e){
            context.getSource().sendFeedback(Text.of("Teleport Failed"), false);
            AnotherQualityUtilsMod.LOGGER.error(e);
            return -1;
        }
    }

    private static ServerPlayerEntity getRequestedPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        GameProfile requestedProfile = GameProfileArgumentType.getProfileArgument(context, "target").iterator().next();
        ServerPlayerEntity requestedPlayer = AnotherQualityUtilsMod.getMinecraftServer().getPlayerManager().getPlayer(requestedProfile.getName());

        if (requestedPlayer == null) {
            requestedPlayer = AnotherQualityUtilsMod.getMinecraftServer().getPlayerManager().createPlayer(requestedProfile);
            NbtCompound compound = AnotherQualityUtilsMod.getMinecraftServer().getPlayerManager().loadPlayerData(requestedPlayer);
            if (compound != null) {
                ServerWorld world = AnotherQualityUtilsMod.getMinecraftServer().getWorld(
                        DimensionType.worldFromDimensionNbt(new Dynamic<>(NbtOps.INSTANCE, compound.get("Dimension"))).result().get()
                );

                if (world != null) {
                    requestedPlayer.setWorld(world);
                }
            }
        }

        return requestedPlayer;
    }

    public static void savePlayerData(ServerPlayerEntity player) {
        File playerDataDir = AnotherQualityUtilsMod.getMinecraftServer().getSavePath(WorldSavePath.PLAYERDATA).toFile();
        try {
            NbtCompound compoundTag = player.writeNbt(new NbtCompound());
            File file = File.createTempFile(player.getUuidAsString() + "-", ".dat", playerDataDir);
            NbtIo.writeCompressed(compoundTag, file);
            File file2 = new File(playerDataDir, player.getUuidAsString() + ".dat");
            File file3 = new File(playerDataDir, player.getUuidAsString() + ".dat_old");
            Util.backupAndReplace(file2, file, file3);
        } catch (Exception var6) {
            LogManager.getLogger().warn("Failed to save player data for {}", player.getName().getString());
        }
    }
}
