package com.bartz24.voidislandcontrol.api;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.bartz24.voidislandcontrol.References;
import com.bartz24.voidislandcontrol.config.ConfigOptions;
import com.google.common.base.Strings;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class IslandManager {

    public static final ArrayList<IslandGen> IslandGenerations = new ArrayList<>();
    public static final ArrayList<IslandPos> CurrentIslandsList = new ArrayList<>();
    public static final ArrayList<String> spawnedPlayers = new ArrayList<>();

    public static boolean worldOneChunk = false;
    public static boolean worldLoaded = false;
    public static int initialIslandDistance = ConfigOptions.islandSettings.islandDistance;

    public static void registerIsland(IslandGen gen) {
        IslandGenerations.add(gen);
    }

    public static List<String> getIslandGenTypes() {
        List<String> types = new ArrayList<>();
        IslandGenerations.forEach(g -> types.add(g.Identifier));
        return types;
    }

    public static int getIndexOfIslandType(String type) {
        for (int i = 0; i < IslandGenerations.size(); i++)
            if (IslandGenerations.get(i).Identifier.equals(type))
                return i;
        return -1;
    }

    public static IslandPos getNextIsland() {
        int size = (int) Math.floor(Math.sqrt(CurrentIslandsList.size()));
        if ((size & 1) == 1) size++;
        size /= 2;

        for (int x = -size; x <= size; x++) {
            for (int z = -size; z <= size; z++) {
                if (!hasPosition(x, z)) {
                    return new IslandPos(x, z);
                }
            }
        }

        return null;
    }

    public static IslandPos getPlayerIsland(UUID playerUUID) {
        for (IslandPos pos : CurrentIslandsList) {
            if (pos.getPlayerUUIDs().contains(playerUUID)) return pos;
        }

        return null;
    }

    public static IslandPos getIslandAtPos(int x, int y) {
        for (IslandPos pos : CurrentIslandsList) {
            if (pos.getX() == x && pos.getY() == y) return pos;
        }

        return null;
    }

    public static List<String> getPlayerNames(World world) {
        List<String> names = new ArrayList<>();
        for (IslandPos pos : CurrentIslandsList) {
            for (UUID uuid : pos.getPlayerUUIDs()) {
                names.add(Objects.requireNonNull(world.getPlayerEntityByUUID(uuid)).getName());
            }
        }

        return names;
    }

    public static boolean hasPosition(int x, int y) {
        for (IslandPos pos : CurrentIslandsList) {
            if (pos.getX() == x && pos.getY() == y)
                return true;
        }

        return false;
    }

    public static boolean playerHasIsland(UUID playerUUID) {
        for (IslandPos pos : CurrentIslandsList) {
            if (pos.getPlayerUUIDs().contains(playerUUID.toString()))
                return true;
        }

        return false;
    }

    public static void addPlayer(UUID playerUUID, IslandPos posAdd) {
        for (IslandPos pos : CurrentIslandsList) {
            if (pos.getX() == posAdd.getX() && pos.getY() == posAdd.getY()) {
                pos.addNewPlayer(playerUUID);
                return;
            }
        }
    }

    public static void removePlayer(UUID playerUUID) {
        IslandPos pos = getPlayerIsland(playerUUID);
        assert pos != null;
        pos.removePlayer(playerUUID);
    }

    public static boolean hasPlayerSpawned(UUID playerUUID) {
        return spawnedPlayers.contains(playerUUID.toString());
    }

    public static void setStartingInv(EntityPlayerMP player) {
        if (ConfigOptions.islandSettings.resetInventory) {
            player.inventory.clear();
            int invSize = player.inventory.getSizeInventory();

            for (String stackString : ConfigOptions.islandSettings.startingItems) {
                Pair<Integer, ItemStack> pair = fromString(player, stackString);

                if (!pair.getRight().isEmpty()) {
                    if (pair.getLeft() >= 0) {
                        if (pair.getLeft() < invSize) {
                            player.inventory.setInventorySlotContents(pair.getLeft(), pair.getRight());
                        }
                        else if (Loader.isModLoaded(References.BAUBLES)) {
                            IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);
                            baubles.setStackInSlot(pair.getLeft() - invSize + 1, pair.getRight());
                        }
                    }
                    else {
                        World world = player.world;
                        EntityItem entity = new EntityItem(world, player.posX, player.posY + 0.5f, player.posZ, pair.getRight());
                        entity.setNoPickupDelay();
                        world.spawnEntity(entity);
                    }
                }
            }
        }
    }

    public static void tpPlayerToPos(EntityPlayer player, BlockPos pos, IslandPos islandPos) {

        if (getSpawnOffset(islandPos) != null) {
            pos = pos.add(getSpawnOffset(islandPos));
        }
        else pos = pos.add(getSpawnOffset(IslandManager.CurrentIslandsList.get(0)));

        if (ConfigOptions.islandSettings.forceSpawn) {
            if (!player.getEntityWorld().isAirBlock(pos) && !player.getEntityWorld().isAirBlock(pos.up())) {
                pos = player.getEntityWorld().getTopSolidOrLiquidBlock(pos.up(2));

                player.sendMessage(new TextComponentString("Failed to spawn. Sent to top block of platform spawn."));
            }
        }
        player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, ConfigOptions.islandSettings.buffTimer, 20, false, false));
        player.extinguish();
        player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, ConfigOptions.islandSettings.buffTimer, 20, false, false));
        player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, ConfigOptions.islandSettings.buffTimer, 20, false, false));


        if (player.dimension != ConfigOptions.worldGenSettings.baseDimension && player instanceof EntityPlayerMP) {
            Objects.requireNonNull(player.getServer()).getPlayerList().transferPlayerToDimension((EntityPlayerMP) player, ConfigOptions.worldGenSettings.baseDimension,
                    new VICTeleporter(player.getServer().getWorld(ConfigOptions.worldGenSettings.baseDimension), pos.getX() + 0.5f, pos.getY() + 2.6f, pos.getZ() + 0.5f));
        }
        else player.setPositionAndUpdate(pos.getX() + 0.5, pos.getY() + 2.6, pos.getZ() + 0.5);
    }

    public static BlockPos getSpawnOffset(IslandPos islandPos) {
        if (islandPos == null)
            return null;
        if (islandPos.getX() == 0 && islandPos.getY() == 0) {
            if (ConfigOptions.islandSettings.islandMainSpawnType.equals("bedrock") || ConfigOptions.islandSettings.islandMainSpawnType.equals("random")) {
                return new BlockPos(0, 7, 0);
            }
            else if (getIndexOfIslandType(ConfigOptions.islandSettings.islandMainSpawnType) != -1) {
                return IslandGenerations.get(getIndexOfIslandType(ConfigOptions.islandSettings.islandMainSpawnType)).spawnOffset;
            }
            else return BlockPos.ORIGIN;
        }
        return IslandGenerations.get(getIndexOfIslandType(islandPos.getType())).spawnOffset;
    }

    public static void tpPlayerToPosSpawn(EntityPlayer player, BlockPos pos, IslandPos islandPos) {
        tpPlayerToPos(player, pos, islandPos);

        if (getSpawnOffset(islandPos) != null) {
            pos = pos.add(getSpawnOffset(islandPos));
        }
        else pos = pos.add(getSpawnOffset(IslandManager.CurrentIslandsList.get(0)));

        player.setSpawnPoint(pos, true);
    }

    public static void setVisitLoc(EntityPlayer player, int x, int y) {
        NBTTagCompound persist = setPlayerData(player);
        persist.setInteger("VICVisitX", x);
        persist.setInteger("VICVisitY", y);
    }

    public static void removeVisitLoc(EntityPlayer player) {
        NBTTagCompound persist = setPlayerData(player);
        persist.removeTag("VICVisitX");
        persist.removeTag("VICVisitY");
    }

    public static boolean hasVisitLoc(EntityPlayer player) {
        NBTTagCompound persist = setPlayerData(player);
        return persist.hasKey("VICVisitX") && persist.hasKey("VICVisitY");
    }

    public static IslandPos getVisitLoc(EntityPlayer player) {
        NBTTagCompound persist = setPlayerData(player);
        return hasVisitLoc(player) ? new IslandPos(persist.getInteger("VICVisitX"), persist.getInteger("VICVisitY")) : null;
    }

    public static void setJoinLoc(EntityPlayer player, int x, int y) {
        NBTTagCompound persist = setPlayerData(player);
        persist.setInteger("VICJoinX", x);
        persist.setInteger("VICJoinY", y);
        persist.setInteger("VICJoinTime", 400);
    }

    public static void setLeaveConfirm(EntityPlayer player) {
        NBTTagCompound persist = setPlayerData(player);
        persist.setInteger("VICLeaveTime", 400);
    }

    public static void removeJoinLoc(EntityPlayer player) {
        NBTTagCompound persist = setPlayerData(player);
        persist.removeTag("VICJoinX");
        persist.removeTag("VICJoinY");
        persist.removeTag("VICJoinTime");
    }

    public static void removeLeaveConfirm(EntityPlayer player) {
        NBTTagCompound persist = setPlayerData(player);
        persist.removeTag("VICLeaveTime");
    }

    public static boolean hasJoinLoc(EntityPlayer player) {
        NBTTagCompound persist = setPlayerData(player);
        return persist.hasKey("VICJoinX") && persist.hasKey("VICJoinY");
    }

    public static boolean hasLeaveConfirm(EntityPlayer player) {
        NBTTagCompound persist = setPlayerData(player);
        return persist.hasKey("VICLeaveTime");
    }

    public static IslandPos getJoinLoc(EntityPlayer player) {
        NBTTagCompound persist = setPlayerData(player);
        return hasJoinLoc(player) ? new IslandPos(persist.getInteger("VICJoinX"), persist.getInteger("VICJoinY")) : null;
    }

    public static int getJoinTime(EntityPlayer player) {
        NBTTagCompound persist = setPlayerData(player);
        return hasJoinLoc(player) ? persist.getInteger("VICJoinTime") : -1;
    }

    public static int getLeaveTime(EntityPlayer player) {
        NBTTagCompound persist = setPlayerData(player);
        return hasLeaveConfirm(player) ? persist.getInteger("VICLeaveTime") : -1;
    }

    public static void setJoinTime(EntityPlayer player, int val) {
        NBTTagCompound persist = setPlayerData(player);
        persist.setInteger("VICJoinTime", val);
    }

    public static void setLeaveTime(EntityPlayer player, int val) {
        NBTTagCompound persist = setPlayerData(player);
        persist.setInteger("VICLeaveTime", val);
    }

    public static NBTTagCompound setPlayerData(EntityPlayer player) {
        NBTTagCompound data = player.getEntityData();
        if (!data.hasKey(EntityPlayer.PERSISTED_NBT_TAG))
            data.setTag(EntityPlayer.PERSISTED_NBT_TAG, new NBTTagCompound());
        return data.getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG);
    }

    private static Pair<Integer, ItemStack> fromString(EntityPlayer player, String s) {
        if (Strings.isNullOrEmpty(s)) throw new NullPointerException("One of the starting item is null");

        int numberCharIndex = s.indexOf('@'); // Can be -1
        int idCharIndex = s.indexOf(':');
        if (idCharIndex == -1) throw new NullPointerException("Item id is not set properly");
        idCharIndex = s.indexOf(':', idCharIndex + 1);

        int amountCharIndex = s.indexOf('*');
        int nbtCharIndex = s.indexOf("#");

        if (idCharIndex == -1) {
            throw new NullPointerException("One of the starting items metadata is not specified");
        }
        if (amountCharIndex == -1) {
            throw new NullPointerException("One of the starting items amount is not specified");
        }

        int slot;

        if (numberCharIndex == -1) slot = -1;
        else slot = Integer.parseInt(s.substring(0, numberCharIndex));

        Item item = Item.getByNameOrId(s.substring(numberCharIndex + 1, idCharIndex));
        if (item == null) throw new NullPointerException("Could not find the item associated with given id while trying to get starting items");

        int metadata = Integer.parseInt(s.substring(idCharIndex + 1, amountCharIndex));
        int amount = Integer.parseInt(s.substring(amountCharIndex + 1, nbtCharIndex == -1 ? s.length() : nbtCharIndex));

        ItemStack stack = new ItemStack(item, amount, metadata);

        if (nbtCharIndex != -1) {
            try {
                NBTTagCompound compound = JsonToNBT.getTagFromJson(s.substring(nbtCharIndex + 1));
                stack.setTagCompound(compound);
            } catch (NBTException e) {
                throw new RuntimeException(e);
            }
        }

        return Pair.of(slot, stack);
    }
}
