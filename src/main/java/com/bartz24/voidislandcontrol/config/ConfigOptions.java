package com.bartz24.voidislandcontrol.config;

import com.bartz24.voidislandcontrol.References;
import net.minecraft.block.BlockPlanks;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;

@Config(modid = References.ModID)
public class ConfigOptions {

    @Config.Comment("Config Settings for the world generation")
    public static WorldGenSettings worldGenSettings = new WorldGenSettings();
    @Config.Comment("Config Settings for the world generation")
    public static IslandSettings islandSettings = new IslandSettings();
    @Config.Comment("Config Settings for the world generation")
    public static CommandSettings commandSettings = new CommandSettings();
    @Config.Comment("Config Settings for other stuff")
    public static OtherSettings otherSettings = new OtherSettings();

    public static String[] emptyFilledArray(int length) {
        String[] array = new String[length];
        Arrays.fill(array, "");
        return array;
    }

    @SubscribeEvent
    @SuppressWarnings("unused")
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(References.ModID)) {
            ConfigManager.sync(References.ModID, Config.Type.INSTANCE);
        }
    }

    public static class WorldGenSettings {
        @Config.Comment("Nether dimension will be a void world")
        public boolean netherVoid = true;
        @Config.Comment("Nether dimension will generate structures (Only takes effect if nether is a void world)")
        public boolean netherVoidStructures = true;
        @Config.Comment("End dimension will be a void world")
        public boolean endVoid = true;
        @Config.Comment("End dimension will generate structures (Only takes effect if end is a void world)")
        public boolean endVoidStructures = true;
        @Config.Comment("Overworld generation type")
        public WorldGenType worldGenType = WorldGenType.VOID;
        @Config.Comment("VOID-NOT USED, OVERWORLD-NOT USED, SUPERFLAT-Use the string as used for normal flat worlds, WORLDTYPE-world type to be used (set like server level-types), CUSTOMIZED-NOT USED")
        public String worldGenSpecialParameters = "";
        @Config.Comment("Biome used for entire world")
        public int worldBiomeID = -1;
        @Config.Comment("Level where clouds appear")
        public int cloudLevel = 32;
        @Config.Comment("Level where the horizon appears")
        public int horizonLevel = 40;
        @Config.Comment("Dimension for island management to occur in")
        public int baseDimension = 0;

        public enum WorldGenType {
            VOID, OVERWORLD, SUPERFLAT, WORLDTYPE, CUSTOMIZED
        }
    }

    public static class IslandSettings {
        @Config.Comment("This is for the island at 0,0! Valids are random, bedrock, sand, snow, wood, grass, gog, or others added by addons/custom islands")
        public String islandMainSpawnType = "bedrock";
        @Config.Comment("Valids are random, sand, snow, wood, grass, gog, or others added by addons/custom islands")
        public String islandSpawnType = "random";
        @Config.Comment("Distance between islands")
        public int islandDistance = 1000;
        @Config.Comment("Range where players from other islands are not allowed and the range furthest players of an island can go. Affects spawn too (Max of islandDistance/2)")
        public int protectionBuildRange = 500;
        @Config.Comment("Protect spawn from building, destroying, interactions with blocks and machines, etc. Those in creative are ignored")
        public boolean spawnProtection = true;
        @Config.Comment("Disables effect of protectionBuildRange")
        public boolean islandProtection = true;
        @Config.Comment("Width of islands")
        public int islandSize = 3;
        @Config.Comment("Spawn a chest on the island")
        public boolean spawnChest = false;
        @Config.Comment("Start the world in one chunk mode")
        public boolean oneChunk = false;
        @Config.Comment("Starting items given to new players. Use the /startingInv command in game")
        // change this to list
        public String[] startingItems = emptyFilledArray(0);
        @Config.Comment("Biome used for islands")
        public int islandBiomeID = -1;
        @Config.Comment("Biome range (width) used for islands")
        public int islandBiomeRange = 0;
        @Config.Comment("Y Level to spawn islands at (Set to 2 above where you want the ground block)")
        public int islandYLevel = 88;
        @Config.Comment("Type of block to spawn under islands")
        public BottomBlockType bottomBlockType = BottomBlockType.BEDROCK;
        @Config.Comment("Automatically give new players islands")
        public boolean autoCreate = false;
        @Config.Comment("ONLY TAKES EFFECT ON DEDICATED SERVERS-Automatically give new players islands")
        public boolean autoCreateServersOnly = false;
        @Config.Comment("Allow players to create or reset their islands")
        public boolean allowIslandCreation = true;
        @Config.Comment("Reset players inventory with the starting inventory")
        public boolean resetInventory = true;
        @Config.Comment("Custom islands using the structure block data. Files are to be placed in the "
                + References.ModID
                + "structures config folder. The names in this list should be the same as the structure names. "
                + "These names are the ids for the island type as well")
        public String[] customIslands = emptyFilledArray(0);
        @Config.Comment("Forces players to spawn at the spawn position. By default, the player will be teleported to a safe spot above it if spawning fails. This config disables that")
        public boolean forceSpawn = false;
        @Config.Comment("Sets how long the buffs are given when spawning on an island in ticks (I think)")
        public int buffTimer = 1200;
        @Config.Comment("Should Void Island Control world type the default option")
        public boolean defaultWorldType = true;

        @Config.Comment("Settings for the grass island")
        public GrassIslandSettings grassSettings = new GrassIslandSettings();
        @Config.Comment("Settings for the sand island")
        public SandIslandSettings sandSettings = new SandIslandSettings();
        @Config.Comment("Settings for the snow island")
        public SnowIslandSettings snowSettings = new SnowIslandSettings();
        @Config.Comment("Settings for the wood island")
        public WoodIslandSettings woodSettings = new WoodIslandSettings();
        @Config.Comment("Settings for the Garden of Glass island (Requires Botania and Garden of Glass!)")
        public GoGSettings gogSettings = new GoGSettings();

        public enum GrassBlockType {
            GRASS, DIRT, COARSEDIRT
        }

        public enum SandBlockType {
            NORMAL, RED
        }

        public enum WoodBlockType {
            OAK, SPRUCE, BIRCH, JUNGLE, ACACIA, DARKOAK
        }

        public enum BottomBlockType {
            BEDROCK, SECONDARYBLOCK
        }

        public static class GrassIslandSettings {
            @Config.Comment("Allow grass island to be used")
            public boolean enableGrassIsland = true;
            @Config.Comment("Spawn a tree")
            public boolean spawnTree = true;
            @Config.Comment("Type of grass/dirt")
            public GrassBlockType grassBlockType = GrassBlockType.GRASS;
        }

        public static class SandIslandSettings {
            @Config.Comment("Allow sand island to be used")
            public boolean enableSandIsland = true;
            @Config.Comment("Spawn a cactus")
            public boolean spawnCactus = true;
            @Config.Comment("Type of sand")
            public SandBlockType sandBlockType = SandBlockType.RED;
        }

        public static class SnowIslandSettings {

            @Config.Comment("Allow snow island to be used")
            public boolean enableSnowIsland = true;
            @Config.Comment("Spawn pumpkins")
            public boolean spawnPumpkins = true;
            @Config.Comment("Spawn an igloo")
            public boolean spawnIgloo = false;
        }

        public static class WoodIslandSettings {
            @Config.Comment("Allow wood island to be used")
            public boolean enableWoodIsland = true;
            @Config.Comment("Spawn water")
            public boolean spawnWater = true;
            @Config.Comment("Spawn string")
            public boolean spawnString = true;
            @Config.Comment("Type of wood")
            private WoodBlockType woodType = WoodBlockType.DARKOAK;

            @Config.Ignore
            public BlockPlanks.EnumType woodBlockType = BlockPlanks.EnumType.byMetadata(woodType.ordinal());
        }

        public static class GoGSettings {
            @Config.Comment("Allow garden of glass island to be used")
            public boolean enableGoGIsland = true;
        }
    }

    public static class CommandSettings {
        @Config.Comment("Name of the main command")
        public String commandName = "island";
        @Config.Comment("Allow the one chunk command to be used")
        public boolean oneChunkCommandAllowed = false;

        @Config.Comment("Offset position for command block from the center block above the bedrock")
        public CommandBlockPos commandBlockPos = new CommandBlockPos();
        @Config.Comment("Type of command block to spawn")
        public CommandBlockType commandBlockType = CommandBlockType.NONE;
        @Config.Comment("Run always or require redstone")
        public boolean commandBlockAuto = false;
        @Config.Comment("Allows the visit command to be used")
        public boolean allowVisitCommand = true;
        @Config.Comment("Allows the home command to be used")
        public boolean allowHomeCommand = true;
        @Config.Comment("Command for the command block to run")
        public String commandBlockCommand = "";
        @Config.Comment("Command Block direction to face")
        public EnumFacing commandBlockDirection = EnumFacing.UP;
        @Config.Comment("Commands to run when the world loads")
        public String[] worldLoadCommands = emptyFilledArray(0);

        public enum CommandBlockType {
            NONE, IMPULSE, REPEATING, CHAIN
        }

        public static class CommandBlockPos {

            @Config.Comment("The x coordinate (Offset from the center block above the bedrock)")
            public int x = 0;

            @Config.Comment("The y coordinate (Offset from the center block above the bedrock)")
            public int y = 0;

            @Config.Comment("The z coordinate (Offset from the center block above the bedrock)")
            public int z = 0;
        }
    }

    public static class OtherSettings {
        @Config.Comment("Hide the toasts when at spawn")
        public boolean hideToasts = false;
    }
}
