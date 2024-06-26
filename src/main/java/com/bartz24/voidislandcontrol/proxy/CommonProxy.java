package com.bartz24.voidislandcontrol.proxy;

import com.bartz24.voidislandcontrol.EventHandler;
import com.bartz24.voidislandcontrol.IslandRegistry;
import com.bartz24.voidislandcontrol.StructureLoader;
import com.bartz24.voidislandcontrol.config.ConfigOptions;
import com.bartz24.voidislandcontrol.world.GoGSupport;
import com.bartz24.voidislandcontrol.world.WorldOverride;
import com.bartz24.voidislandcontrol.world.WorldTypeVoid;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class CommonProxy {

    public static WorldTypeVoid worldTypeVoid;

    public void preInit(FMLPreInitializationEvent e) {
        StructureLoader.preInit(e);
        worldTypeVoid = new WorldTypeVoid();
    }

    public void init(FMLInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
        MinecraftForge.EVENT_BUS.register(new ConfigOptions());
        if (IslandRegistry.isValidGoG()) MinecraftForge.EVENT_BUS.register(new GoGSupport());

        IslandRegistry.initIslands();
        WorldOverride.registerWorldProviders();
    }

    public void postInit(FMLPostInitializationEvent e) {
    }
}
