package com.bartz24.voidislandcontrol.world;

import com.bartz24.voidislandcontrol.config.ConfigOptions;
import net.minecraft.world.WorldProviderSurface;

public class WorldProviderSurfaceVoid extends WorldProviderSurface {

    @Override
    public float getCloudHeight() {
        return world.getWorldType() instanceof WorldTypeVoid ? ConfigOptions.worldGenSettings.cloudLevel : super.getCloudHeight();
    }
}
