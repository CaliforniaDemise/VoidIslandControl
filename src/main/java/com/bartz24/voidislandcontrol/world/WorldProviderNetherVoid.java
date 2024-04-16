package com.bartz24.voidislandcontrol.world;

import com.bartz24.voidislandcontrol.config.ConfigOptions;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nonnull;

public class WorldProviderNetherVoid extends WorldProviderHell {

    @Nonnull
    @Override
    public IChunkGenerator createChunkGenerator() {
        if (getDimension() == -1 && world.getWorldType() instanceof WorldTypeVoid)
            return new ChunkGeneratorNetherVoid(world, world.getSeed());
        return super.createChunkGenerator();
    }
}
