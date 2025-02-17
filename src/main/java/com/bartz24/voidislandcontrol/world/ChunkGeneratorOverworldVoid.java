package com.bartz24.voidislandcontrol.world;

import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.ChunkGeneratorOverworld;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

public class ChunkGeneratorOverworldVoid extends ChunkGeneratorOverworld {

    private final World world;
    private static final Field f_biomesForGeneration;

    public ChunkGeneratorOverworldVoid(World worldIn, long seed, boolean mapFeaturesEnabledIn, String generatorOptions) {
        super(worldIn, seed, mapFeaturesEnabledIn, generatorOptions);
        this.world = worldIn;
    }

    @Nonnull
    @Override
    public Chunk generateChunk(int x, int z) {
        ChunkPrimer chunkprimer = new ChunkPrimer();
        try {
            f_biomesForGeneration.set(this, this.world.getBiomeProvider().getBiomes((Biome[]) f_biomesForGeneration.get(this), x * 16, z * 16, 16, 16));
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        ChunkVoid chunk = new ChunkVoid(this.world, chunkprimer, x, z);
        byte[] abyte = chunk.getBiomeArray();
        Biome[] biomesForGeneration;
        try {
            biomesForGeneration = (Biome[]) f_biomesForGeneration.get(this);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < abyte.length; ++i) {
            abyte[i] = (byte) Biome.getIdForBiome(biomesForGeneration[i]);
        }
        chunk.generateSkylightMap();
        return chunk;
    }

    static {
        try {
            f_biomesForGeneration = ChunkGeneratorOverworld.class.getDeclaredField("biomesForGeneration");
            f_biomesForGeneration.setAccessible(true);
        }
        catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
