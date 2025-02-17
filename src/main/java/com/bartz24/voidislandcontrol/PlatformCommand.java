package com.bartz24.voidislandcontrol;

import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.api.IslandPos;
import com.bartz24.voidislandcontrol.api.event.*;
import com.bartz24.voidislandcontrol.config.ConfigOptions;
import com.bartz24.voidislandcontrol.world.WorldTypeVoid;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class PlatformCommand extends CommandBase implements ICommand {

    private static List<String> aliases;

    public PlatformCommand() {
        aliases = new ArrayList<String>();
        if (ConfigOptions.commandSettings.commandName.equals("island")) {
            aliases.add("island");
        } else
            aliases.add(ConfigOptions.commandSettings.commandName);

    }

    @Override
    public List<String> getAliases() {
        return aliases;
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
                                          @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "create", "invite", "join", "leave", "kick", "home", "spawn",
                    "reset", "visit", "onechunk");
        } else {
            String subCommand = args[0];
            subCommand = subCommand.trim();

            if (subCommand.equals("create")) {
                return args.length == 2 ? getListOfStringsMatchingLastWord(args, IslandManager.getIslandGenTypes())
                        : Collections.<String>emptyList();
            } else if (subCommand.equals("invite")) {
                return args.length == 2 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames())
                        : Collections.<String>emptyList();
            } else if (subCommand.equals("reset")) {
                return args.length == 2 ? getListOfStringsMatchingLastWord(args, IslandManager.getIslandGenTypes())
                        : Collections.<String>emptyList();
            } else if (subCommand.equals("visit")) {
                return args.length == 2 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames())
                        : Collections.<String>emptyList();
            } else if (subCommand.equals("kick")) {
                return args.length == 2 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames())
                        : Collections.<String>emptyList();
            }
        }
        return Collections.<String>emptyList();

    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        World world = sender.getEntityWorld();
        Entity entity = sender.getCommandSenderEntity();
        if (entity == null) return;

        EntityPlayerMP player = (EntityPlayerMP) entity;

        if (!(world.getWorldInfo().getTerrainType() instanceof WorldTypeVoid)) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.wrong_world_type"));
            return;
        }

        if (args.length == 0)
            showHelp(player);
        else {
            String subCommand = args[0];
            subCommand = subCommand.trim();

            if (subCommand.equals("create")) {
                if (args.length > 1 && args[1].equals("bypass")) args = new String[]{args[0]};
                newPlatform(player, args);
                MinecraftForge.EVENT_BUS.post(
                        new IslandCreateEvent(player, IslandManager.getPlayerIsland(player.getGameProfile().getId())));
            } else if (subCommand.equals("invite")) {
                inviteOther(player, args, world);
                MinecraftForge.EVENT_BUS.post(
                        new IslandInviteEvent(player, IslandManager.getPlayerIsland(player.getGameProfile().getId())));
            } else if (subCommand.equals("join")) {
                joinPlatform(player, args, world);
            } else if (subCommand.equals("leave")) {
                IslandPos pos = IslandManager.getPlayerIsland(player.getGameProfile().getId());
                if (pos == null) {
                    player.sendMessage(new TextComponentTranslation(References.ModID + ".command.no_island"));
                    return;
                }
                leavePlatform(player, args);
                MinecraftForge.EVENT_BUS.post(new IslandLeaveEvent(player, pos));
            } else if (subCommand.equals("home")) {
                tpHome(player, args);
                MinecraftForge.EVENT_BUS.post(
                        new IslandHomeEvent(player, IslandManager.getPlayerIsland(player.getGameProfile().getId())));
            } else if (subCommand.equals("spawn")) {
                tpSpawn(player, args);
                MinecraftForge.EVENT_BUS.post(new IslandSpawnEvent(player));
            } else if (subCommand.equals("reset")) {
                IslandPos pos = IslandManager.getPlayerIsland(player.getGameProfile().getId());
                if (pos == null) {
                    player.sendMessage(new TextComponentTranslation(References.ModID + ".command.no_island"));
                    return;
                }
                reset(player, args, world);
                MinecraftForge.EVENT_BUS.post(
                        new IslandResetEvent(player, IslandManager.getPlayerIsland(player.getGameProfile().getId())));
            } else if (subCommand.equals("visit")) {
                visit(player, args);
                MinecraftForge.EVENT_BUS.post(
                        new IslandVisitEvent(player, IslandManager.getPlayerIsland(player.getGameProfile().getId())));
            } else if (subCommand.equals("kick")) {
                kick(player, args);
            } else if (subCommand.equals("onechunk")) {

                if (!ConfigOptions.commandSettings.oneChunkCommandAllowed) {
                    player.sendMessage(new TextComponentTranslation(References.ModID + ".command.not_allowed"));
                    return;

                }

                if (IslandManager.worldOneChunk) {
                    player.sendMessage(new TextComponentTranslation(References.ModID + ".command.one_chunk.already_activated"));
                    return;
                }
                IslandManager.CurrentIslandsList.clear();

                IslandManager.CurrentIslandsList.add(new IslandPos(0, 0));
                WorldBorder border = world.getMinecraftServer().worlds[0].getWorldBorder();

                border.setCenter(0, 0);
                border.setTransition(16);
                border.setWarningDistance(1);

                IslandManager.worldOneChunk = true;
                reset(player, args, world);
            }
        }

    }

    public static void visit(EntityPlayerMP player, String[] args) throws CommandException {
        if (!ConfigOptions.commandSettings.allowVisitCommand) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.disabled"));
            return;
        }
        if (args.length != 2) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.no_argument"));
            return;
        }
        if (IslandManager.worldOneChunk) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.wrong_mode"));
            return;
        }
        if (IslandManager.initialIslandDistance != ConfigOptions.islandSettings.islandDistance) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.different_distance"));
            return;
        }

        EntityPlayerMP player2 = (EntityPlayerMP) player.getEntityWorld().getPlayerEntityByName(args[1]);

        IslandPos isPos = player2 == null ? null : IslandManager.getPlayerIsland(player2.getGameProfile().getId());

        if (args[1].equals(player.getName())) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.visiting_yourself"));
            return;
        }

        if (isPos == null) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.not_found"));
            return;
        }

        BlockPos visitPos = new BlockPos(isPos.getX() * ConfigOptions.islandSettings.islandDistance,
                ConfigOptions.islandSettings.islandYLevel, isPos.getY() * ConfigOptions.islandSettings.islandDistance);

        IslandManager.setVisitLoc(player, isPos.getX(), isPos.getY());
        player.setGameType(GameType.SPECTATOR);

        player.connection.setPlayerLocation(visitPos.getX() + 0.5, visitPos.getY(), visitPos.getZ() + 0.5,
                player.rotationYaw, player.rotationPitch);

    }

    public static void kick(EntityPlayerMP player, String[] args) throws CommandException {
        if (args.length != 2) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.no_argument"));
            return;
        }
        if (IslandManager.worldOneChunk) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.wrong_mode"));
            return;
        }

        EntityPlayerMP player2 = (EntityPlayerMP) player.getEntityWorld().getPlayerEntityByName(args[1]);

        IslandPos isPos = IslandManager.getPlayerIsland(player2.getGameProfile().getId());

        if (args[1].equals(player.getName())) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.kicking_yourself"));
            return;
        }

        if (isPos == null) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.not_found"));
            return;
        }

        if (!isPos.getPlayerUUIDs().contains(player2.getGameProfile().getId())) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.not_in_your_island"));
            return;
        }

        if (!isPos.getPlayerUUIDs().get(0).equals(player.getGameProfile().getId())) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.not_owner"));
            return;
        }

        for (int i = 0; i < player2.inventory.getSizeInventory(); i++) {
            ItemStack stack = player2.inventory.getStackInSlot(i).copy();
            EntityItem item = new EntityItem(player.world);
            item.setItem(stack);
            item.posX = player.posX;
            item.posY = player.posY;
            item.posZ = player.posZ;
            player.world.spawnEntity(item);
        }
        EventHandler.spawnPlayer(player2, new BlockPos(0, ConfigOptions.islandSettings.islandYLevel, 0), false);
        player2.sendMessage(new TextComponentTranslation(References.ModID + ".command.kicked"));

    }

    public static void reset(EntityPlayerMP player, String[] args, World world) throws CommandException {
        if (!ConfigOptions.islandSettings.allowIslandCreation) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.creation_not_allowed").setStyle(new Style().setColor(TextFormatting.RED)));
            return;
        }
        if (!IslandManager.worldOneChunk) {
            leavePlatform(player, new String[]{""});
            newPlatform(player, args);
        } else {

            PlayerList players = world.getMinecraftServer().getPlayerList();
            for (EntityPlayerMP p : players.getPlayers()) {
                p.sendMessage(new TextComponentTranslation(References.ModID + ".command.reset_lag"));
            }
            for (int x = -8; x < 9; x++) {
                for (int z = -8; z < 9; z++) {
                    for (int y = 0; y < 256; y++) {
                        world.setBlockState(new BlockPos(x, y, z), Blocks.AIR.getDefaultState(), 2);
                    }
                }
            }
            if (args.length > 1) {
                Integer i = -1;

                try {
                    i = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    i = IslandManager.getIndexOfIslandType(args[1]);
                }

                if (i > -1 && i < IslandManager.IslandGenerations.size()) {

                    EventHandler.spawnPlayer(player, new BlockPos(0, ConfigOptions.islandSettings.islandYLevel, 0), i);
                }
            } else {
                EventHandler.createSpawn(player, world, new BlockPos(0, ConfigOptions.islandSettings.islandYLevel, 0));
            }
            for (EntityPlayerMP p : players.getPlayers()) {
                p.inventory.clear();

                EventHandler.spawnPlayer(p, new BlockPos(0, ConfigOptions.islandSettings.islandYLevel, 0), false);
                p.sendMessage(new TextComponentTranslation(References.ModID + ".command.chunk_reset"));
            }
        }
    }

    @SuppressWarnings("deprecation")
    void showHelp(EntityPlayerMP player) {
        String optional = I18n.translateToLocal(References.ModID + ".command.desc.optional");

        player.sendMessage(new TextComponentString(TextFormatting.RED + "create (" + optional + " int/string)<type>"
                + TextFormatting.WHITE + " : " + I18n.translateToLocal(References.ModID + ".command.create.desc")));
        player.sendMessage(new TextComponentString(TextFormatting.RED + "invite <player>" + TextFormatting.WHITE
                + " : " + I18n.translateToLocal(References.ModID + ".command.invite.desc")));
        player.sendMessage(new TextComponentString(TextFormatting.RED + "join" + TextFormatting.WHITE
                + " : " + I18n.translateToLocal(References.ModID + ".command.join.desc")));
        player.sendMessage(new TextComponentString(TextFormatting.RED + "leave" + TextFormatting.WHITE
                + " : " + I18n.translateToLocal(References.ModID + ".command.leave.desc")));
        player.sendMessage(new TextComponentString(TextFormatting.RED + "home" + TextFormatting.WHITE
                + " : " + I18n.translateToLocalFormatted(References.ModID + ".command.home.desc", ConfigOptions.islandSettings.protectionBuildRange)));
        player.sendMessage(new TextComponentString(
                TextFormatting.RED + "spawn" + TextFormatting.WHITE + " : " + I18n.translateToLocal(References.ModID + ".command.spawn.desc")));
        player.sendMessage(new TextComponentString(TextFormatting.RED + "reset (" + optional + "int/string)<type>"
                + TextFormatting.WHITE
                + " : " + I18n.translateToLocal(References.ModID + ".command.reset.desc")));
        player.sendMessage(new TextComponentString(TextFormatting.RED + "onechunk" + TextFormatting.WHITE
                + " : " + I18n.translateToLocal(References.ModID + ".command.onechunk.desc") + ((ConfigOptions.commandSettings.oneChunkCommandAllowed ? "" : TextFormatting.RED + I18n.translateToLocal(References.ModID + ".command.onechunk.desc.warning")))));
        player.sendMessage(new TextComponentString(TextFormatting.RED + "visit <player>" + TextFormatting.WHITE
                + " : " + I18n.translateToLocal(References.ModID + ".command.visit.desc")));
    }

    public static void newPlatform(EntityPlayerMP player, String... args) throws CommandException {
        if ((args.length == 1 || (args.length > 1 && !args[1].equals("bypass"))) && !ConfigOptions.islandSettings.allowIslandCreation) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.creation_not_allowed").setStyle(new Style().setColor(TextFormatting.RED)));
            return;
        }
        if (args.length > 2) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.too_many_arguments"));
            return;
        }
        if (IslandManager.worldOneChunk) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.wrong_mode"));
            return;
        }
        if (IslandManager.initialIslandDistance != ConfigOptions.islandSettings.islandDistance) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.different_distance"));
            return;
        }

        if (IslandManager.playerHasIsland(player.getGameProfile().getId())) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.already_have_island"));
            return;
        }

        IslandPos position = IslandManager.getNextIsland();
        if (args.length > 1 && args[1].equals("bypass"))
            args = new String[]{args[0]};

        if (args.length > 1 && ConfigOptions.islandSettings.islandSpawnType.equals("random")) {

            Integer i = -1;

            try {
                i = Integer.parseInt(args[1]);
            } catch (Exception e) {
                i = IslandManager.getIndexOfIslandType(args[1]);
            }

            if (i > -1 && i < IslandManager.IslandGenerations.size()) {

                EventHandler.spawnPlayer(player,
                        new BlockPos(position.getX() * ConfigOptions.islandSettings.islandDistance,
                                ConfigOptions.islandSettings.islandYLevel,
                                position.getY() * ConfigOptions.islandSettings.islandDistance),
                        i);
            }
        } else {
            if (args.length > 1) {
                player.sendMessage(new TextComponentTranslation(References.ModID + ".command.island_config_override"));
            }
            EventHandler.spawnPlayer(player,
                    new BlockPos(position.getX() * ConfigOptions.islandSettings.islandDistance,
                            ConfigOptions.islandSettings.islandYLevel,
                            position.getY() * ConfigOptions.islandSettings.islandDistance),
                    true);

            IslandManager.setStartingInv(player);
        }

        if (IslandManager.hasVisitLoc(player)) {
            player.setGameType(GameType.SURVIVAL);
            IslandManager.removeVisitLoc(player);
        }
    }

    public static void inviteOther(EntityPlayerMP player, String[] args, World world) throws CommandException {
        if (args.length != 2) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.no_argument"));
            return;
        }
        if (IslandManager.worldOneChunk) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.wrong_mode"));
            return;
        }
        if (IslandManager.initialIslandDistance != ConfigOptions.islandSettings.islandDistance) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.different_distance"));
            return;
        }
        EntityPlayerMP player2 = (EntityPlayerMP) player.getEntityWorld().getPlayerEntityByName(args[1]);

        if (player2 == null) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.player_not_exist", args[1]));
            return;
        }

        if (player2.getName().equals(player.getName())) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.player_is_you", player2.getName()));
            return;
        }

        if (!IslandManager.playerHasIsland(player.getGameProfile().getId())) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.no_island"));
            return;
        }

        if (IslandManager.hasJoinLoc(player2)) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.player_has_invite", player2.getName()));
            return;
        }

        IslandPos position = IslandManager.getPlayerIsland(player.getGameProfile().getId());
        IslandManager.setJoinLoc(player2, position.getX(), position.getY());
        player.sendMessage(new TextComponentTranslation(References.ModID + ".command.player_invited", player2.getName()));

        player2.sendMessage(new TextComponentTranslation(References.ModID + ".command.got_invited", player.getName(), aliases.get(0)));
    }

    public static void joinPlatform(EntityPlayerMP player, String[] args, World world) throws CommandException {
        IslandPos position = IslandManager.getJoinLoc(player);
        if (position == null) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.no_invites"));
            return;
        }

        IslandManager.addPlayer(player.getGameProfile().getId(), position);

        position = IslandManager.getPlayerIsland(player.getGameProfile().getId());

        for (UUID name : position.getPlayerUUIDs()) {
            EntityPlayerMP p = (EntityPlayerMP) world.getPlayerEntityByUUID(name);
            if (p != null) p.sendMessage(new TextComponentTranslation(References.ModID + ".command.player_join", player.getName()));
        }

        if (IslandManager.hasVisitLoc(player)) {
            player.setGameType(GameType.SURVIVAL);
            IslandManager.removeVisitLoc(player);
        }

        IslandManager.tpPlayerToPosSpawn(player,
                new BlockPos(position.getX() * ConfigOptions.islandSettings.islandDistance,
                        ConfigOptions.islandSettings.islandYLevel,
                        position.getY() * ConfigOptions.islandSettings.islandDistance), position);

    }

    public static void leavePlatform(EntityPlayerMP player, String[] args) throws CommandException {
        if (!ConfigOptions.islandSettings.allowIslandCreation) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.no_leaving").setStyle(new Style().setColor(TextFormatting.RED)));
            return;
        }
        if (args.length > 1) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.need_no_argument"));
            return;
        }
        if (IslandManager.worldOneChunk) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.wrong_mode"));
            return;
        }
        if (IslandManager.initialIslandDistance != ConfigOptions.islandSettings.islandDistance) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.different_distance"));
            return;
        }

        if (IslandManager.getPlayerIsland(player.getGameProfile().getId()).getPlayerUUIDs().size() == 1 && !IslandManager.hasLeaveConfirm(player)) {
            IslandManager.setLeaveConfirm(player);
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.leave_confirmation", ConfigOptions.commandSettings.commandName));
            return;
        }

        if (!IslandManager.playerHasIsland(player.getGameProfile().getId())) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.no_island"));
            return;
        }

        IslandManager.removePlayer(player.getGameProfile().getId());
        player.sendMessage(new TextComponentTranslation(References.ModID + ".command.free_to_join"));

        if (!ConfigOptions.islandSettings.resetInventory)
            player.inventory.clear();

        if (IslandManager.hasVisitLoc(player)) {
            player.setGameType(GameType.SURVIVAL);
            IslandManager.removeVisitLoc(player);
        }

        IslandManager.tpPlayerToPosSpawn(player, new BlockPos(0, ConfigOptions.islandSettings.islandYLevel, 0), IslandManager.getIslandAtPos(0, 0));
    }

    public static void tpHome(EntityPlayerMP player, String[] args) throws CommandException {
        if (!ConfigOptions.commandSettings.allowHomeCommand) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.disabled"));
            return;
        }
        if (args.length > 1) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.need_no_argument"));
            return;
        }
        if (IslandManager.worldOneChunk) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.wrong_mode"));
            return;
        }
        if (IslandManager.initialIslandDistance != ConfigOptions.islandSettings.islandDistance) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.different_distance"));
            return;
        }

        IslandPos isPos = IslandManager.getPlayerIsland(player.getGameProfile().getId());

        if (isPos == null) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.no_island"));
            return;
        }

        BlockPos home = new BlockPos(isPos.getX() * ConfigOptions.islandSettings.islandDistance,
                ConfigOptions.islandSettings.islandYLevel, isPos.getY() * ConfigOptions.islandSettings.islandDistance);

        if (player.dimension == ConfigOptions.worldGenSettings.baseDimension && Math.hypot(player.posX - home.getX() - 0.5,
                player.posZ - home.getZ() - 0.5) < ConfigOptions.islandSettings.protectionBuildRange) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.not_far_enough_home", ConfigOptions.islandSettings.protectionBuildRange));
            return;
        }

        if (IslandManager.hasVisitLoc(player)) {
            player.setGameType(GameType.SURVIVAL);
            IslandManager.removeVisitLoc(player);
        }

        IslandManager.tpPlayerToPos(player, home, isPos);

    }

    public static void tpSpawn(EntityPlayerMP player, String[] args) throws CommandException {
        if (IslandManager.worldOneChunk) {
            player.sendMessage(new TextComponentTranslation(References.ModID + ".command.wrong_mode"));
            return;
        }

        if (IslandManager.hasVisitLoc(player)) {
            player.setGameType(GameType.SURVIVAL);
            IslandManager.removeVisitLoc(player);
        }
        IslandManager.tpPlayerToPos(player, new BlockPos(0, ConfigOptions.islandSettings.islandYLevel, 0), IslandManager.getIslandAtPos(0, 0));
    }

    @Override
    public String getName() {
        return aliases.get(0);
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return ConfigOptions.commandSettings.commandName;
    }
}
