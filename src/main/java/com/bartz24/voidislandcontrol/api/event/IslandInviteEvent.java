package com.bartz24.voidislandcontrol.api.event;

import com.bartz24.voidislandcontrol.api.IslandPos;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;
import java.util.UUID;

/**
 * This event is fired after a player is invited to an island
 * This event is not cancelable, and has no result
 * This event is fired on MinecraftForge#EVENT_BUS
 * The EntityPlayer is the player being invited, the IslandPos is the island being sent to
 */
@SuppressWarnings("unused")
public class IslandInviteEvent extends Event {

    private final IslandPos islandPosition;
    private final UUID playerUUID;

    public IslandInviteEvent(@Nonnull EntityPlayer entityPlayer, IslandPos isPosition) {
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
