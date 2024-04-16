package com.bartz24.voidislandcontrol;

import com.bartz24.voidislandcontrol.api.IslandManager;
import com.bartz24.voidislandcontrol.config.ConfigOptions;
import com.bartz24.voidislandcontrol.world.WorldTypeVoid;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@SuppressWarnings("unused")
public class ClientEventHandler {

    @SubscribeEvent
    public void playerUpdate(LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        World world = player.world;

        if (world.getWorldInfo().getTerrainType() instanceof WorldTypeVoid && player.dimension == ConfigOptions.worldGenSettings.baseDimension && !IslandManager.worldOneChunk && !ConfigOptions.otherSettings.hideToasts) {
            boolean atSpawn = Math.abs(player.posX) < 100 && Math.abs(player.posZ) < 100;
            IslandToast toast = minecraft.getToastGui().getToast(IslandToast.class, IslandToast.Type.Island);
            if (atSpawn && toast == null) {
                Minecraft.getMinecraft().getToastGui().add(new IslandToast(new TextComponentString("Create an island!"), new TextComponentString("/" + ConfigOptions.commandSettings.commandName + " for help")));
            }
            else if (!atSpawn && toast != null) toast.hide();
        }
        else {
            IslandToast toast = minecraft.getToastGui().getToast(IslandToast.class, IslandToast.Type.Island);
            if (toast != null) toast.hide();
        }
    }
}
