package com.bartz24.voidislandcontrol;

import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.api.IslandPos;
import com.bartz24.voidislandcontrol.config.ConfigOptions;
import com.bartz24.voidislandcontrol.config.ConfigOptions.CommandSettings.CommandBlockType;
import com.bartz24.voidislandcontrol.world.WorldTypeVoid;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiWorldSelection;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.border.WorldBorder;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.event.world.WorldEvent.Unload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Random;

@SuppressWarnings("unused")
public class EventHandler {

    private static final MethodHandle selectedIndex;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onOpenGui(GuiOpenEvent e) {
        if (ConfigOptions.islandSettings.defaultWorldType && e.getGui() instanceof GuiCreateWorld && Minecraft.getMinecraft().currentScreen instanceof GuiWorldSelection) {
            // Thanks YUNoMakeGoodMap :D
            GuiCreateWorld cw = (GuiCreateWorld) e.getGui();
            try {
                selectedIndex.invoke(cw, getType());
            }
            catch (Throwable ex) { throw new RuntimeException(ex); }
        }
    }

    private int getType() {
        for (int i = 0; i < WorldType.WORLD_TYPES.length; i++) {
            if (WorldType.WORLD_TYPES[i] instanceof WorldTypeVoid) return i;
        }

        return 0;
    }

    @SubscribeEvent
    public void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        World world = event.player.world;

        if (!world.isRemote) {
            EntityPlayer player = event.player;

            if (world.getWorldInfo().getTerrainType() instanceof WorldTypeVoid && player.dimension == ConfigOptions.worldGenSettings.baseDimension) {
                if (IslandManager.spawnedPlayers.isEmpty() || !IslandManager.hasPlayerSpawned(player.getGameProfile().getId())) {
                    BlockPos spawn = new BlockPos(0, ConfigOptions.islandSettings.islandYLevel, 0);

                    if (IslandManager.CurrentIslandsList.isEmpty()) {
                        IslandManager.CurrentIslandsList.add(new IslandPos(0 , 0));
                        BlockPos pos = spawn.add(IslandManager.getSpawnOffset(IslandManager.CurrentIslandsList.get(0)));
                        world.setSpawnPoint(pos);
                        createSpawn(player, world, spawn);
                    }

                    IslandManager.tpPlayerToPos(player, spawn, IslandManager.CurrentIslandsList.get(0));

                    boolean autoCreate = ConfigOptions.islandSettings.autoCreate || (Objects.requireNonNull(player.getServer()).isDedicatedServer() && ConfigOptions.islandSettings.autoCreateServersOnly);

                    if (autoCreate && !IslandManager.worldOneChunk) {
                        if (player instanceof EntityPlayerMP) {
                            try {
                                PlatformCommand.newPlatform((EntityPlayerMP) player, "create", "bypass");
                            }
                            catch (CommandException e) { player.sendMessage(new TextComponentString(e.getMessage())); }
                        }
                    } else {
                        if (ConfigOptions.islandSettings.oneChunk) {
                            WorldBorder border = Objects.requireNonNull(world.getMinecraftServer()).worlds[0].getWorldBorder();
                            border.setCenter(0, 0);
                            border.setTransition(16);
                            border.setWarningDistance(1);
                            IslandManager.worldOneChunk = true;
                        }

                        spawnPlayer(player, spawn, false);
                        player.extinguish();
                    }

                    IslandManager.spawnedPlayers.add(player.getGameProfile().getId().toString());
                }
            }
        }
    }

    @SubscribeEvent
    public void playerUpdate(LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        World world = player.world;

        if (!world.isRemote) {
            if (world.getWorldInfo().getTerrainType() instanceof WorldTypeVoid && IslandManager.hasVisitLoc(player) && player.dimension == ConfigOptions.worldGenSettings.baseDimension && !player.isCreative()) {
                if (((EntityPlayerMP) player).interactionManager.getGameType() != GameType.SPECTATOR) {
                    player.setGameType(GameType.SPECTATOR);
                }

                IslandPos visitLoc = Objects.requireNonNull(IslandManager.getVisitLoc(player));
                int posX = visitLoc.getX() * ConfigOptions.islandSettings.islandDistance;
                int posY = visitLoc.getY() * ConfigOptions.islandSettings.islandDistance;

                if (ConfigOptions.islandSettings.islandProtection && (Math.abs(player.posX - posX) > ConfigOptions.islandSettings.protectionBuildRange || Math.abs(player.posZ - posY) > ConfigOptions.islandSettings.protectionBuildRange)) {
                    if (player.ticksExisted % 60 == 0) {
                        player.sendMessage(new TextComponentString(TextFormatting.RED + "You can't be visiting that far away!"));
                    }

                    player.setGameType(GameType.SURVIVAL);
                    IslandManager.removeVisitLoc(player);
                    IslandManager.tpPlayerToPos(player, new BlockPos(posX, ConfigOptions.islandSettings.islandYLevel, posY), IslandManager.getVisitLoc(player));
                }
            }

            if (IslandManager.hasJoinLoc(player)) {
                int time = IslandManager.getJoinTime(player);
                if (time > 0) IslandManager.setJoinTime(player, time - 1);
                else IslandManager.removeJoinLoc(player);
            }
            if (IslandManager.hasLeaveConfirm(player)) {
                int time = IslandManager.getLeaveTime(player);
                if (time > 0)
                    IslandManager.setLeaveTime(player, time - 1);
                else
                    IslandManager.removeLeaveConfirm(player);
            }

            loadWorld(player);
        }
    }

    private static void loadWorld(EntityPlayer player) {
        if (!IslandManager.worldLoaded) {
            ICommandManager manager = Objects.requireNonNull(player.world.getMinecraftServer()).getCommandManager();
            for (String s : ConfigOptions.commandSettings.worldLoadCommands) {
                if (!StringUtils.isBlank(s)) manager.executeCommand(player, s);
            }
        }

        IslandManager.worldLoaded = true;
    }

    public static void spawnPlayer(EntityPlayer player, BlockPos pos, boolean spawnPlat) {
        if (spawnPlat) createSpawn(player, player.getEntityWorld(), pos);
        if (player instanceof EntityPlayerMP) {
            EntityPlayerMP pmp = (EntityPlayerMP) player;
            IslandManager.tpPlayerToPosSpawn(player, pos, IslandManager.getPlayerIsland(pmp.getUniqueID()));
        }
    }

    public static void spawnPlayer(EntityPlayer player, BlockPos pos, int forceType) {
        spawnPlat(player, player.getEntityWorld(), pos, forceType);
        spawnPlayer(player, pos, false);
    }

    public static void createSpawn(EntityPlayer player, World world, BlockPos spawn) {
        if (spawn.getX() == 0 && spawn.getZ() == 0 && !IslandManager.worldOneChunk) {
            if (ConfigOptions.islandSettings.islandMainSpawnType.equals("bedrock")) mainSpawn(world, spawn);
            else {
                Random random = world.rand;
                int type = ConfigOptions.islandSettings.islandMainSpawnType.equals("random") ? random.nextInt(IslandManager.IslandGenerations.size()) : IslandManager.getIndexOfIslandType(ConfigOptions.islandSettings.islandMainSpawnType);
                spawnPlat(null, world, spawn, type);
            }

            return;
        }

        Random random = world.rand;
        int type = ConfigOptions.islandSettings.islandSpawnType.equals("random") ? random.nextInt(IslandManager.IslandGenerations.size()) : IslandManager.getIndexOfIslandType(ConfigOptions.islandSettings.islandSpawnType);
        spawnPlat(player, world, spawn, type);
    }

    private static void spawnPlat(@Nullable EntityPlayer player, World world, BlockPos spawn, int type) {
        if (player != null) {
            IslandPos position = Objects.requireNonNull(IslandManager.getNextIsland());
            IslandManager.CurrentIslandsList.add(new IslandPos(IslandManager.IslandGenerations.get(type).Identifier, position.getX(), position.getY(), player.getGameProfile().getId()));
        }

        IslandManager.IslandGenerations.get(type).generate(world, spawn);

        if (ConfigOptions.commandSettings.commandBlockType != CommandBlockType.NONE) {
            Block cmdBlock = null;
            switch (ConfigOptions.commandSettings.commandBlockType) {
                case IMPULSE: cmdBlock = Blocks.COMMAND_BLOCK; break;
                case CHAIN: cmdBlock = Blocks.CHAIN_COMMAND_BLOCK; break;
                case REPEATING: cmdBlock = Blocks.REPEATING_COMMAND_BLOCK; break;
            }

            if (cmdBlock != null) {
                BlockPos down = spawn.add(ConfigOptions.commandSettings.commandBlockPos.x, ConfigOptions.commandSettings.commandBlockPos.y - 3, ConfigOptions.commandSettings.commandBlockPos.z);
                world.setBlockState(down, cmdBlock.getDefaultState().withProperty(BlockCommandBlock.FACING, ConfigOptions.commandSettings.commandBlockDirection), 3);
                TileEntityCommandBlock te = (TileEntityCommandBlock) Objects.requireNonNull(world.getTileEntity(down));
                te.getCommandBlockLogic().setCommand(ConfigOptions.commandSettings.commandBlockCommand);
                te.setAuto(ConfigOptions.commandSettings.commandBlockAuto);
            }
        }
    }

    private static void mainSpawn(World world, BlockPos spawn) {
        int halfSize = (int) Math.floor((float) ConfigOptions.islandSettings.islandSize / 2F);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = -halfSize; x <= halfSize; x++) {
            for (int z = -halfSize; z <= halfSize; z++) {
                pos.setPos(spawn.getX() + x, spawn.getY(), spawn.getZ() + z);
                world.setBlockState(pos.move(EnumFacing.DOWN, 3), Blocks.BEDROCK.getDefaultState(), 2);
                world.setBlockState(pos.move(EnumFacing.DOWN), Blocks.BEDROCK.getDefaultState(), 2);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        EntityPlayer player = event.player;
        World world = player.world;

        if (world.getWorldInfo().getTerrainType() instanceof WorldTypeVoid) {
            BlockPos bedLoc = player.getBedLocation();
            if (bedLoc == null || EntityPlayer.getBedSpawnLocation(world, bedLoc, true) == null) {
                IslandPos iPos = IslandManager.getPlayerIsland(player.getGameProfile().getId());
                BlockPos pos;
                if (iPos != null) pos = new BlockPos(iPos.getX() * ConfigOptions.islandSettings.islandDistance, ConfigOptions.islandSettings.islandYLevel, iPos.getY() * ConfigOptions.islandSettings.islandDistance);
                else pos = new BlockPos(0, ConfigOptions.islandSettings.islandYLevel, 0);
                IslandManager.tpPlayerToPos(player, pos, iPos);
            }
        }
    }

    @SubscribeEvent
    public void onSave(Save event) {
        VoidIslandControlSaveData.setDirty(0);
    }

    @SubscribeEvent
    public void onUnload(Unload event) {
        VoidIslandControlSaveData.setDirty(0);
    }

    @SubscribeEvent
    public static PlayerInteractEvent spawnProtection(PlayerInteractEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        World world = player.getEntityWorld();

        if (!ConfigOptions.islandSettings.spawnProtection || Math.abs(player.posX) > ConfigOptions.islandSettings.protectionBuildRange || Math.abs(player.posZ) > ConfigOptions.islandSettings.protectionBuildRange) {
            return event;
        }
        else {
            if (!player.isCreative() && event.isCancelable()) event.setCanceled(true);
            return null;
        }
    }

    static {
        try {
            Field field = GuiCreateWorld.class.getDeclaredField(FMLLaunchHandler.isDeobfuscatedEnvironment() ? "selectedIndex" : "field_146331_K");
            field.setAccessible(true);
            selectedIndex = MethodHandles.publicLookup().unreflectSetter(field);
        }
        catch (NoSuchFieldException | IllegalAccessException e) { throw new RuntimeException(e); }
    }
}
