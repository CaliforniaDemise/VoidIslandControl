package com.bartz24.voidislandcontrol.world;

import com.bartz24.voidislandcontrol.config.ConfigOptions;
import com.bartz24.voidislandcontrol.config.ConfigOptions.WorldGenSettings.WorldGenType;
import net.minecraft.init.Biomes;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.biome.BiomeProviderSingle;
import net.minecraft.world.gen.ChunkGeneratorFlat;
import net.minecraft.world.gen.ChunkGeneratorOverworld;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

public class WorldTypeVoid extends WorldType {

    private WorldType overridenWorldType;

    public WorldTypeVoid() {
        super("voidworld");
        if (ConfigOptions.worldGenSettings.worldGenType == WorldGenType.WORLDTYPE)
            overridenWorldType = WorldType.byName(ConfigOptions.worldGenSettings.worldGenSpecialParameters);
    }

    public boolean hasInfoNotice() {
        return true;
    }

    @Override
    public int getMinimumSpawnHeight(@Nonnull World world) {
        return ConfigOptions.islandSettings.islandYLevel;
    }

    public int getSpawnFuzz() {
        return 2;
    }

    @Override
    public double getHorizon(@Nonnull World world) {
        return ConfigOptions.worldGenSettings.horizonLevel;
    }

    @Nonnull
    @Override
    public BiomeProvider getBiomeProvider(@Nonnull World world) {
        if (overridenWorldType != null) return overridenWorldType.getBiomeProvider(world);
        if (ConfigOptions.worldGenSettings.worldBiomeID > -1) {
            return new BiomeProviderSingle(Biome.getBiome(ConfigOptions.worldGenSettings.worldBiomeID, Biomes.PLAINS));
        }
        else return new BiomeProvider(world.getWorldInfo());
    }

    @SideOnly(Side.CLIENT)
    public void onCustomizeButton(net.minecraft.client.Minecraft mc, net.minecraft.client.gui.GuiCreateWorld guiCreateWorld) {
        if (ConfigOptions.worldGenSettings.worldGenType == WorldGenType.CUSTOMIZED) {
            mc.displayGuiScreen(new net.minecraft.client.gui.GuiCustomizeWorldScreen(guiCreateWorld, guiCreateWorld.chunkProviderSettingsJson));
        }
        else if (ConfigOptions.worldGenSettings.worldGenType == WorldGenType.WORLDTYPE && overridenWorldType != null)
            overridenWorldType.onCustomizeButton(mc, guiCreateWorld);
    }

    public boolean isCustomizable() {
        return ConfigOptions.worldGenSettings.worldGenType == WorldGenType.CUSTOMIZED || (ConfigOptions.worldGenSettings.worldGenType == WorldGenType.CUSTOMIZED && overridenWorldType != null && overridenWorldType.isCustomizable());
    }

    @Override
    public IChunkGenerator getChunkGenerator(World world, String generatorOptions) {
        if (overridenWorldType != null) return overridenWorldType.getChunkGenerator(world, generatorOptions);
        if (ConfigOptions.worldGenSettings.worldGenType == WorldGenType.VOID) return new ChunkGeneratorOverworldVoid(world, world.getSeed(), false, generatorOptions);
        else if (ConfigOptions.worldGenSettings.worldGenType != WorldGenType.OVERWORLD && ConfigOptions.worldGenSettings.worldGenType != WorldGenType.CUSTOMIZED) {
            String genSettings = "3;1*minecraft:air";
            if (ConfigOptions.worldGenSettings.worldGenType == WorldGenType.SUPERFLAT) genSettings = ConfigOptions.worldGenSettings.worldGenSpecialParameters;
            ChunkGeneratorFlat provider = new ChunkGeneratorFlat(world, world.getSeed(), false, genSettings);
            world.setSeaLevel(63);
            return provider;
        }
        else return new ChunkGeneratorOverworld(world, world.getSeed(), false, generatorOptions);
    }

    public boolean handleSlimeSpawnReduction(java.util.Random random, World world) {
        if (overridenWorldType != null) return overridenWorldType.handleSlimeSpawnReduction(random, world);
        return super.handleSlimeSpawnReduction(random, world);
    }
}
