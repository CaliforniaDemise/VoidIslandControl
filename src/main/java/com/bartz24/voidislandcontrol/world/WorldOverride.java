package com.bartz24.voidislandcontrol.world;

import com.bartz24.voidislandcontrol.VoidIslandControl;
import com.bartz24.voidislandcontrol.config.ConfigOptions;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;

public class WorldOverride {

    public static void registerWorldProviders() {
        try {
            DimensionManager.unregisterDimension(0);
            DimensionManager.registerDimension(0, DimensionType.register("VoidOverworld", "_overworld", 0, WorldProviderSurfaceVoid.class, true));
        }
        catch (Exception e) {
            VoidIslandControl.logger.error("Could not override overworld dimension to be void!", e);
        }
        if (ConfigOptions.worldGenSettings.netherVoid) {
            try {
                DimensionManager.unregisterDimension(-1);
                DimensionManager.registerDimension(-1, DimensionType.register("VoidNether",
                        "_nether", -1, WorldProviderNetherVoid.class, true));
            }
            catch (Exception e) {
                VoidIslandControl.logger.error("Could not override the nether dimension to be void!", e);
            }
        }
        if (ConfigOptions.worldGenSettings.endVoid) {
            try {
                DimensionManager.unregisterDimension(1);
                DimensionManager.registerDimension(1, DimensionType.register("VoidEnd",
                        "_end", 1, WorldProviderEndVoid.class, true));
            }
            catch (Exception e) {
                VoidIslandControl.logger.error("Could not override the end dimension to be void!", e);
            }
        }
    }
}
