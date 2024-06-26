package com.bartz24.voidislandcontrol;

import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.config.ConfigOptions;
import com.bartz24.voidislandcontrol.proxy.CommonProxy;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = References.ModID, name = "Void Island Control", useMetadata = true)
public class VoidIslandControl {

	@SidedProxy(clientSide = "com.bartz24.voidislandcontrol.proxy.ClientProxy", serverSide = "com.bartz24.voidislandcontrol.proxy.CommonProxy")
	public static CommonProxy proxy;

	@Mod.Instance
	public static VoidIslandControl instance;

	public static final Logger logger = LogManager.getLogger(References.ModID);

	@Mod.EventHandler
	public void serverLoading(FMLServerStartingEvent event) {
		logger.info("Registering Void Island Control commands.");
		event.registerServerCommand(new PlatformCommand());
		event.registerServerCommand(new StartingInvCommand());
		event.registerServerCommand(new AdminCommand());
		logger.info("Finished registering Void Island Control commands.");

		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			IslandManager.CurrentIslandsList.clear();
			IslandManager.spawnedPlayers.clear();
			IslandManager.worldOneChunk = false;
			IslandManager.initialIslandDistance = ConfigOptions.islandSettings.islandDistance;
			World world = event.getServer().getEntityWorld();
			if (!world.isRemote) {
				VoidIslandControlSaveData worldData = (VoidIslandControlSaveData) world.loadData(VoidIslandControlSaveData.class, VoidIslandControlSaveData.dataName);

				if (worldData == null) {
					worldData = new VoidIslandControlSaveData(VoidIslandControlSaveData.dataName);
					world.setData(VoidIslandControlSaveData.dataName, worldData);
				}

				VoidIslandControlSaveData.setInstance(world.provider.getDimension(), worldData);
			}
		}
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		proxy.preInit(event);
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.init(event);
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		proxy.postInit(event);
	}

	public VoidIslandControl() {
		FluidRegistry.enableUniversalBucket();
	}
}
