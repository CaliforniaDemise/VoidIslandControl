package com.bartz24.voidislandcontrol.world;

import com.bartz24.voidislandcontrol.config.ConfigOptions;
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

    @Override
    public float getCloudHeight() {
        return ConfigOptions.worldGenSettings.cloudLevel;
    }
}
