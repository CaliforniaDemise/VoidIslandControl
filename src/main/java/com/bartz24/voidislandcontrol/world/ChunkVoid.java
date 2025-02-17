package com.bartz24.voidislandcontrol.world;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nonnull;

public class ChunkVoid extends Chunk {

    public ChunkVoid(World worldIn, ChunkPrimer primer, int x, int z) {
        super(worldIn, primer, x, z);
    }

    @Override
    protected void populate(@Nonnull IChunkGenerator generator) {
        if (!this.isTerrainPopulated()) {
            this.checkLight();
            this.markDirty();
        }
    }
}
