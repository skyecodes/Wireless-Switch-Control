package com.github.franckyi.wsc.waila;

import java.util.List;

import com.github.franckyi.wsc.capability.redstonecontroller.RedstoneControllerProvider;
import com.github.franckyi.wsc.tileentity.TileEntityRedstoneController;
import com.github.franckyi.wsc.util.MasterRedstoneSwitch;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RedstoneControllerDataProvider implements IWailaDataProvider {
	
	public static final RedstoneControllerDataProvider INSTANCE = new RedstoneControllerDataProvider();

	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config) {
		return null;
	}

	@Override
	public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		return currenttip;
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		TileEntity tile = accessor.getTileEntity();
		if (tile instanceof TileEntityRedstoneController) {
			List<MasterRedstoneSwitch> switches = tile.getCapability(RedstoneControllerProvider.CONTROLLER_CAP, null)
					.getSwitches();
			currenttip.add("Linked switches :");
			for (MasterRedstoneSwitch mls : switches)
				currenttip.add(" - " + (mls.isEnabled() ? "§a" : "§c") + mls.getName() + " [" + String.valueOf(mls.getPower())
						+ "]§r");
		}
		return currenttip;
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		return currenttip;
	}

	@Override
	public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world,
			BlockPos pos) {
		return tag;
	}

}