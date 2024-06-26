package com.bartz24.voidislandcontrol.api.event;

import com.bartz24.voidislandcontrol.api.IslandPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * This event is fired after a player returns to their home island
 * This event is not cancelable, and has no result
 * This event is fired on MinecraftForge#EVENT_BUS
 * The EntityPlayer is the player returning home, the IslandPos is the island being returned to
 */
@SuppressWarnings("unused")
public class IslandHomeEvent extends Event {

    private final IslandPos islandPosition;
    private final UUID playerUUID;

    public IslandHomeEvent(@Nonnull EntityPlayer entityPlayer, IslandPos isPosition) {
        playerUUID = entityPlayer.getUniqueID();
        islandPosition = isPosition;
    }

    @Nonnull
    public UUID getPlayerUUID() {
        return playerUUID;
    }

    @Nonnull
    public IslandPos getIslandPosition() {
        return islandPosition;
    }
}
