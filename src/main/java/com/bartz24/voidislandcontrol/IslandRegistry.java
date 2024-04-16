package com.bartz24.voidislandcontrol;

import com.bartz24.voidislandcontrol.api.IslandGen;
import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.config.ConfigOptions;
import com.bartz24.voidislandcontrol.config.ConfigOptions.IslandSettings.BottomBlockType;
import com.bartz24.voidislandcontrol.config.ConfigOptions.IslandSettings.SandBlockType;
import com.bartz24.voidislandcontrol.world.GoGSupport;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraftforge.fml.common.Loader;

import java.util.Map;
import java.util.Objects;

public class IslandRegistry {

    public static void initIslands() {
        BlockPos genPos = new BlockPos(0, 2, 0);
        int halfSize = (int) Math.floor((float) ConfigOptions.islandSettings.islandSize / 2f);

        if (ConfigOptions.islandSettings.grassSettings.enableGrassIsland) {
            IslandManager.registerIsland(new IslandGen("grass", new BlockPos(0, 7, 0)) {
                public void generate(World world, BlockPos spawn) {
                    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

                    IBlockState GRASS = Objects.requireNonNull(Blocks.GRASS).getDefaultState();
                    IBlockState DIRT = Objects.requireNonNull(Blocks.DIRT).getDefaultState();
                    IBlockState COARSEDIRT = DIRT.withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);

                    IBlockState BEDROCK = Objects.requireNonNull(Blocks.BEDROCK).getDefaultState();

                    for (int x = -halfSize; x <= halfSize; x++) {
                        for (int z = -halfSize; z <= halfSize; z++) {
                            pos.setPos(spawn.getX() + x, spawn.getY(), spawn.getZ() + z);
                            IBlockState topBlock = null;

                            switch (ConfigOptions.islandSettings.grassSettings.grassBlockType) {
                                case GRASS: topBlock = GRASS; break;
                                case DIRT: topBlock = DIRT; break;
                                case COARSEDIRT: topBlock = COARSEDIRT; break;
                            }

                            world.setBlockState(pos.move(EnumFacing.DOWN, 3), topBlock, 2);

                            if (ConfigOptions.islandSettings.bottomBlockType == BottomBlockType.BEDROCK) {
                                world.setBlockState(pos.move(EnumFacing.DOWN), BEDROCK, 2);
                            }
                            else world.setBlockState(pos.move(EnumFacing.DOWN), DIRT, 2);
                        }
                    }

                    if (ConfigOptions.islandSettings.grassSettings.spawnTree) {
                        IBlockState LOG = Objects.requireNonNull(Blocks.LOG).getDefaultState();
                        IBlockState LEAVES = Objects.requireNonNull(Blocks.LEAVES).getDefaultState();
                        for (int y = 0; y < 5; y++) {
                            for (int x = -2; x < 3; x++) {
                                for (int z = -2; z < 3; z++) {
                                    pos.setPos(spawn.getX() + x, spawn.getY() - 2 + y, spawn.getZ() + z);
                                    if (x == 0 && z == 0) {
                                        if (y < 3)
                                            world.setBlockState(pos, LOG, 2);
                                        else
                                            world.setBlockState(pos, LEAVES, 2);
                                    } else if (y == 2 || y == 3) {
                                        world.setBlockState(pos, LEAVES, 2);
                                    } else if (y == 4 && x >= -1 && x <= 1 && z >= -1 && z <= 1) {
                                        world.setBlockState(pos, LEAVES, 2);
                                    }
                                }
                            }
                        }
                    }

                    if (ConfigOptions.islandSettings.spawnChest) {
                        pos.setPos(spawn.getX(), spawn.getY() - 2, spawn.getZ() - 1);
                        world.setBlockState(pos, Objects.requireNonNull(Blocks.CHEST).getDefaultState());
                    }

                    changeBiome(spawn.getX(), spawn.getZ(), world);
                }
            });
        }
        if (ConfigOptions.islandSettings.sandSettings.enableSandIsland) {
            IslandManager.registerIsland(new IslandGen("sand", genPos) {
                public void generate(World world, BlockPos spawn) {
                    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

                    IBlockState SAND = Objects.requireNonNull(Blocks.SAND).getStateFromMeta(ConfigOptions.islandSettings.sandSettings.sandBlockType == SandBlockType.RED ? 1 : 0);
                    IBlockState BEDROCK = Objects.requireNonNull(Blocks.BEDROCK).getDefaultState();
                    IBlockState SANDSTONE = Objects.requireNonNull(Blocks.SANDSTONE).getDefaultState();
                    IBlockState RED_SANDSTONE = Objects.requireNonNull(Blocks.RED_SANDSTONE).getDefaultState();

                    IBlockState CACTUS = Objects.requireNonNull(Blocks.CACTUS).getDefaultState();

                    for (int x = -halfSize; x <= halfSize; x++) {
                        for (int z = -halfSize; z <= halfSize; z++) {
                            pos.setPos(spawn.getX() + x, spawn.getY(), spawn.getZ() + z);
                            world.setBlockState(pos.move(EnumFacing.DOWN, 3), SAND, 2);
                            if (ConfigOptions.islandSettings.bottomBlockType == BottomBlockType.BEDROCK) {
                                world.setBlockState(pos.move(EnumFacing.DOWN), BEDROCK, 2);
                            }
                            else world.setBlockState(pos.move(EnumFacing.DOWN), ConfigOptions.islandSettings.sandSettings.sandBlockType == SandBlockType.NORMAL ? SANDSTONE : RED_SANDSTONE, 2);
                        }
                    }

                    pos.setPos(spawn.getX() - 1, spawn.getY(), spawn.getZ() + 1);
                    if (ConfigOptions.islandSettings.sandSettings.spawnCactus) {
                        world.setBlockState(pos, CACTUS, 2);
                        world.setBlockState(pos.move(EnumFacing.DOWN), CACTUS, 2);
                        world.setBlockState(pos.move(EnumFacing.DOWN), CACTUS, 2);
                    }

                    if (ConfigOptions.islandSettings.spawnChest) {
                        pos.setPos(spawn.getX(), spawn.getY() - 2, spawn.getZ() - 1);
                        world.setBlockState(pos, Objects.requireNonNull(Blocks.CHEST).getDefaultState());
                    }

                    changeBiome(spawn.getX(), spawn.getZ(), world);
                }
            });
        }

        if (ConfigOptions.islandSettings.snowSettings.enableSnowIsland) {
            IslandManager.registerIsland(new IslandGen("snow", genPos) {
                public void generate(World world, BlockPos spawn) {
                    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

                    IBlockState SNOW_LAYER = Objects.requireNonNull(Blocks.SNOW_LAYER).getDefaultState();
                    IBlockState SNOW = Objects.requireNonNull(Blocks.SNOW).getDefaultState();
                    IBlockState PACKED_ICE = Objects.requireNonNull(Blocks.PACKED_ICE).getDefaultState();
                    IBlockState BEDROCK = Objects.requireNonNull(Blocks.BEDROCK).getDefaultState();
                    IBlockState PUMPKIN = Objects.requireNonNull(Blocks.PUMPKIN).getDefaultState();

                    for (int x = -halfSize - 1; x <= halfSize + 1; x++) {
                        for (int z = -halfSize - 1; z <= halfSize + 1; z++) {

                            pos.setPos(spawn.getX() + x, spawn.getY(), spawn.getZ() + z);

                            if (world.getBiome(pos).getTemperature(new BlockPos(pos)) > 1.0F) {
                                world.getChunk(pos).getBiomeArray()[(pos.getZ() & 15) << 4 | (pos.getX() & 15)] = (byte) Biome.getIdForBiome(Biomes.PLAINS);
                            }

                            if (x == -halfSize - 1 || x == halfSize + 1 || z == -halfSize - 1 || z == halfSize + 1) {
                                if (ConfigOptions.islandSettings.snowSettings.spawnIgloo) {
                                    world.setBlockState(pos.move(EnumFacing.DOWN), PACKED_ICE, 2);
                                    world.setBlockState(pos.move(EnumFacing.DOWN), PACKED_ICE, 2);
                                    world.setBlockState(pos.move(EnumFacing.DOWN), PACKED_ICE, 2);
                                }
                            } else {
                                if (!(x == 0 && z == 0) && ConfigOptions.islandSettings.snowSettings.spawnIgloo) world.setBlockState(pos, PACKED_ICE, 2);
                                world.setBlockState(pos.move(EnumFacing.DOWN, 3), SNOW, 2);

                                if (ConfigOptions.islandSettings.bottomBlockType == BottomBlockType.BEDROCK) {
                                    world.setBlockState(pos.move(EnumFacing.DOWN), BEDROCK, 2);
                                }
                                else world.setBlockState(pos.move(EnumFacing.DOWN), PACKED_ICE, 2);

                                if (((x == -1 && z == 1) || (x == 1 && z == 1)) && ConfigOptions.islandSettings.snowSettings.spawnPumpkins) {
                                    world.setBlockState(pos.move(EnumFacing.UP, 2), PUMPKIN, 2);
                                    world.setBlockState(pos.move(EnumFacing.UP), SNOW_LAYER, 2);
                                }
                                else world.setBlockState(pos.move(EnumFacing.UP, 2), SNOW_LAYER, 2);
                            }
                        }
                    }

                    if (ConfigOptions.islandSettings.spawnChest) {
                        pos.setPos(spawn.getX(), spawn.getY() - 2, spawn.getZ() - 1);
                        world.setBlockState(pos, Objects.requireNonNull(Blocks.CHEST).getDefaultState());
                    }

                    changeBiome(spawn.getX(), spawn.getZ(), world);
                }
            });
        }

        if (ConfigOptions.islandSettings.woodSettings.enableWoodIsland) {
            IslandManager.registerIsland(new IslandGen("wood", genPos) {
                public void generate(World world, BlockPos spawn) {
                    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

                    IBlockState WATER = Objects.requireNonNull(Blocks.WATER).getDefaultState();
                    IBlockState PLANKS = Objects.requireNonNull(Blocks.PLANKS).getDefaultState().withProperty(BlockPlanks.VARIANT, ConfigOptions.islandSettings.woodSettings.woodBlockType);
                    IBlockState BEDROCK = Objects.requireNonNull(Blocks.BEDROCK).getDefaultState();

                    for (int x = -halfSize; x <= halfSize; x++) {
                        for (int z = -halfSize; z <= halfSize; z++) {
                            pos.setPos(spawn.getX() + x, spawn.getY(), spawn.getZ() + z);

                            if (world.getBiome(pos).getTemperature(new BlockPos(pos)) < 0.5F) {
                                world.getChunk(pos).getBiomeArray()[(pos.getZ() & 15) << 4 | (pos.getX() & 15)] = (byte) Biome.getIdForBiome(Biomes.PLAINS);
                            }

                            if (x == 0 && z == 0 && ConfigOptions.islandSettings.woodSettings.spawnWater) {
                                world.setBlockState(pos.move(EnumFacing.DOWN, 3), WATER, 2);
                            }
                            else world.setBlockState(pos.move(EnumFacing.DOWN, 3), PLANKS, 2);

                            if (ConfigOptions.islandSettings.bottomBlockType == BottomBlockType.BEDROCK) {
                                world.setBlockState(pos.move(EnumFacing.DOWN), BEDROCK, 2);
                            }
                            else world.setBlockState(pos.move(EnumFacing.DOWN), PLANKS, 2);
                        }
                    }

                    pos.setPos(spawn.getX() - 1, spawn.getY(), spawn.getZ() + 1);
                    if (ConfigOptions.islandSettings.spawnChest) {
                        world.setBlockState(pos, Objects.requireNonNull(Blocks.CHEST).getDefaultState());
                    }

                    if (ConfigOptions.islandSettings.woodSettings.spawnString) {
                        world.setBlockState(pos.move(EnumFacing.DOWN, 2), Objects.requireNonNull(Blocks.TRIPWIRE).getDefaultState(), 2);
                    }

                    changeBiome(spawn.getX(), spawn.getZ(), world);
                }
            });
        }

        if (isValidGoG()) {
            IslandManager.registerIsland(new IslandGen("gog", genPos) {
                public void generate(World world, BlockPos spawn) {
                    GoGSupport.spawnGoGIsland(world, spawn);
                    if (ConfigOptions.islandSettings.spawnChest) {
                        BlockPos pos = new BlockPos(spawn.getX(), spawn.getY() - 2, spawn.getZ() - 1);
                        world.setBlockState(pos, Objects.requireNonNull(Blocks.CHEST).getDefaultState());
                    }
                    changeBiome(spawn.getX(), spawn.getZ(), world);
                }
            });
        }

        for (String s : ConfigOptions.islandSettings.customIslands) {
            IslandManager.registerIsland(new IslandGen(s, getCustomIslandSpawnPoint(s)) {
                public void generate(World world, BlockPos spawn) {
                    generateCustomIsland(s, world, spawn);
                    changeBiome(spawn.getX(), spawn.getZ(), world);
                }
            });
        }
    }

    public static boolean isValidGoG() {
        return ConfigOptions.islandSettings.gogSettings.enableGoGIsland && Loader.isModLoaded("gardenofglass");
    }

    private static void changeBiome(int xIs, int zIs, World world) {
        int halfSize = (int) Math.floor((float) ConfigOptions.islandSettings.islandSize / 2f);
        if (ConfigOptions.islandSettings.islandBiomeID >= 0) {
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int x = xIs - halfSize; x <= xIs + halfSize; x++) {
                for (int z = zIs - halfSize; z <= zIs + halfSize; z++) {
                    world.getChunk(pos.setPos(x, 64, z)).getBiomeArray()[(z & 15) << 4 | (x & 15)] = (byte) ConfigOptions.islandSettings.islandBiomeID;
                }
            }
        }
    }

    private static BlockPos getCustomIslandSpawnPoint(String id) {
        Template t = StructureLoader.tempManager.get(null, new ResourceLocation(id));
        if (t != null) {
            PlacementSettings settings = new PlacementSettings().setIgnoreStructureBlock(false);
            BlockPos genPos = new BlockPos(-t.getSize().getX() / 2, 0, -t.getSize().getZ() / 2);
            Map<BlockPos, String> dataBlocks = t.getDataBlocks(new BlockPos(0, 0, 0), settings);
            for (BlockPos dataPos : dataBlocks.keySet()) {
                if (dataBlocks.get(dataPos).equals("spawn_point")) {
                    return dataPos.add(genPos);
                }
            }
        }
        return new BlockPos(0, 0, 0);
    }

    private static void generateCustomIsland(String id, World world, BlockPos pos) {
        Template t = StructureLoader.tempManager.get(world.getMinecraftServer(), new ResourceLocation(id));
        if (t != null) {
            BlockPos genPos = new BlockPos(pos.getX() - t.getSize().getX() / 2, pos.getY(), pos.getZ() - t.getSize().getZ() / 2);
            PlacementSettings settings = new PlacementSettings().setIgnoreStructureBlock(false);
            t.addBlocksToWorld(world, genPos, settings, 3);
            Map<BlockPos, String> dataBlocks = t.getDataBlocks(genPos, settings);
            for (BlockPos dataPos : dataBlocks.keySet()) {
                if (dataBlocks.get(dataPos).equals("spawn_point")) {
                    world.setBlockToAir(dataPos);
                }
            }
        } else world.setBlockState(pos.down(2), Objects.requireNonNull(Blocks.BEDROCK).getDefaultState());
    }
}
