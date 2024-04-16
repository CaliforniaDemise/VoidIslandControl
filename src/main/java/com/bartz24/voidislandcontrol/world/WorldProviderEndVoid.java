package com.bartz24.voidislandcontrol.world;

import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nonnull;

public class WorldProviderEndVoid extends WorldProviderEnd {

    @Nonnull
    @Override
    public IChunkGenerator createChunkGenerator() {
        if (getDimension() == 1 && world.getWorldType() instanceof WorldTypeVoid)
            return new ChunkGeneratorEndVoid(world, world.getSeed());
        return super.createChunkGenerator();
    }
}
