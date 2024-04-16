package com.bartz24.voidislandcontrol;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import com.bartz24.voidislandcontrol.config.ConfigOptions;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.common.Loader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class StartingInvCommand extends CommandBase implements ICommand {

	private final List<String> aliases;

	public StartingInvCommand() {
		aliases = new ArrayList<>();
		aliases.add("startingInv");
		aliases.add("startingInventory");
	}

	@Nonnull
	@Override
	public List<String> getAliases() {
		return aliases;
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Nonnull
	public List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, @Nullable BlockPos targetPos) {
		return Collections.<String> emptyList();
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, ICommandSender sender, String[] args) {
		EntityPlayerMP player = (EntityPlayerMP) sender.getCommandSenderEntity();
		if (player == null) return;

		int inventorySize = player.inventory.getSizeInventory();
		List<String> list = new ArrayList<>();

		for (int i = 0; i < inventorySize; i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (!stack.isEmpty()) {
				list.add(asString(i, stack));
			}
		}

		if (Loader.isModLoaded(References.BAUBLES)) baublesIntegration(list, player);

		ConfigOptions.islandSettings.startingItems = list.toArray(new String[list.size()]);
		ConfigManager.sync(References.ModID, Config.Type.INSTANCE);

		player.sendMessage(new TextComponentString("Starting Inventory config set!"));
	}

	@Nonnull
	@Override
	public String getName() {
		return aliases.get(0);
	}

	@Nonnull
	@Override
	public String getUsage(@Nonnull ICommandSender sender) {
		return "";
	}

	private static String asString(int slot, ItemStack stack) {
		String name = Objects.requireNonNull(stack.getItem().getRegistryName()).toString();
		int count = stack.getCount();
		int meta = stack.getMetadata();

		NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound().copy() : null;

		StringBuilder builder = new StringBuilder(slot + "@");
		builder.append(name);
		builder.append(":").append(meta);
		builder.append("*").append(count);

		if (nbt != null) builder.append("#").append(nbt);

		return builder.toString();
	}

	private static void baublesIntegration(List<String> list, EntityPlayerMP player) {
		int invSize = player.inventory.getSizeInventory();
		IBaublesItemHandler baubles = BaublesApi.getBaublesHandler(player);

		for (int i = invSize; i < invSize + baubles.getSlots(); i++) {
			ItemStack stack = baubles.getStackInSlot(i - invSize);
			if (!stack.isEmpty()) list.add(asString(invSize, stack));
		}
	}
}
