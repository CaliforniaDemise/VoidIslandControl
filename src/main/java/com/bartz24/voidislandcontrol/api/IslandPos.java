package com.bartz24.voidislandcontrol.api;

import com.google.common.collect.Lists;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IslandPos {

    private int posX;
    private int posY;
    private String type;

    private ArrayList<UUID> playerUUIDs;

    public IslandPos(int x, int y, UUID... ids) {
        posX = x;
        posY = y;
        playerUUIDs = Lists.newArrayList(ids);
    }

    public IslandPos(String type, int x, int y, UUID... ids) {
        this(x, y, ids);
        this.type = type;
    }

    public void addNewPlayer(UUID playerUUID) {
        if (!playerUUIDs.contains(playerUUID))
            playerUUIDs.add(playerUUID);
    }

    public void removePlayer(UUID playerUUID) {
        playerUUIDs.remove(playerUUID);
    }

    public int getX() {
        return posX;
    }

    public int getY() {
        return posY;
    }

    public String getType() {
        return type;
    }

    public List<UUID> getPlayerUUIDs() {
        return playerUUIDs;
    }

    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("posX", posX);
        nbt.setInteger("posY", posY);
        if (!StringUtils.isEmpty(type))
            nbt.setString("type", type);

        NBTTagList list = new NBTTagList();

        for (UUID playerUUID : playerUUIDs) {
            NBTTagCompound stackTag = new NBTTagCompound();
            stackTag.setUniqueId("playerUUID", playerUUID);
            list.appendTag(stackTag);
        }

        nbt.setTag("UUIDs", list);
    }

    public void readFromNBT(NBTTagCompound nbt) {
        posX = nbt.getInteger("posX");
        posY = nbt.getInteger("posY");
        type = nbt.getString("type");

        NBTTagList list = nbt.getTagList("UUIDs", Constants.NBT.TAG_COMPOUND);
        playerUUIDs = new ArrayList<>(list.tagCount());

        String playerUUID = "playerUUID";

        for (int i = 0; i < list.tagCount(); ++i) {
            NBTTagCompound stackTag = list.getCompoundTagAt(i);
            UUID uuid;

            if (stackTag.hasKey(playerUUID, Constants.NBT.TAG_STRING)) {
                uuid = UUID.fromString(stackTag.getString(playerUUID));
            }
            else uuid = stackTag.getUniqueId(playerUUID);

            playerUUIDs.add(uuid);
        }
    }
}
