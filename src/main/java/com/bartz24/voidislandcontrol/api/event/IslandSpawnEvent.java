package com.bartz24.voidislandcontrol.api.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * This event is fired after a player goes to spawn
 * This event is not cancelable, and has no result
 * This event is fired on MinecraftForge#EVENT_BUS
 * The EntityPlayer is the player going to spawn
 */
@SuppressWarnings("unused")
public class IslandSpawnEvent extends Event {

    private final UUID playerUUID;

    public IslandSpawnEvent(@Nonnull EntityPlayer entityPlayer) {
        playerUUID = entityPlayer.getUniqueID();
    }

    @Nonnull
    public UUID getPlayerUUID() {
        return playerUUID;
    }
}
