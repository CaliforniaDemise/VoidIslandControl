package com.bartz24.voidislandcontrol.world;

import com.bartz24.voidislandcontrol.config.ConfigOptions;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkGeneratorHell;

public class ChunkGeneratorNetherVoid extends ChunkGeneratorHell {

    World world;

    public ChunkGeneratorNetherVoid(World par1World, long par2) {
        super(par1World, ConfigOptions.worldGenSettings.netherVoidStructures, par2);
        world = par1World;
    }
}
