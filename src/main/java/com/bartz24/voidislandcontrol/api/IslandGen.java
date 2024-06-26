package com.bartz24.voidislandcontrol.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class IslandGen {

    public final String Identifier;
    public final BlockPos spawnOffset;

    public IslandGen(String id, BlockPos spawnOffset) {
        Identifier = id;
        this.spawnOffset = spawnOffset;
    }

    public abstract void generate(World world, BlockPos pos);
}
