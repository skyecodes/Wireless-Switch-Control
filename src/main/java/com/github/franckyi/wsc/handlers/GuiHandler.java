package com.github.franckyi.wsc.handlers;

import com.github.franckyi.wsc.capability.controllercap.ControllerProvider;
import com.github.franckyi.wsc.capability.switchcap.SwitchProvider;
import com.github.franckyi.wsc.gui.GuiRedstoneController;
import com.github.franckyi.wsc.gui.GuiRedstoneSwitch;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {
	
	public static final int REDSTONE_CONTROLLER_GUI = 0;
	public static final int REDSTONE_SWITCH_GUI = 1;

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    	return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
    	BlockPos pos = new BlockPos(x, y, z);
    	if (ID == REDSTONE_CONTROLLER_GUI)
            return new GuiRedstoneController(world.getTileEntity(pos).getCapability(ControllerProvider.CONTROLLER_CAP, null).getSwitches(), pos);
    	else if (ID == REDSTONE_SWITCH_GUI)
    		return new GuiRedstoneSwitch(world.getTileEntity(pos).getCapability(SwitchProvider.SWITCH_CAP, null).getSwitch(), pos);
    	return null;
    }
}