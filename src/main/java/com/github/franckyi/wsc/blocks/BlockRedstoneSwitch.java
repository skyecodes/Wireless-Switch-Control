package com.github.franckyi.wsc.blocks;

import java.util.HashSet;
import java.util.Set;

import com.github.franckyi.wsc.WSCMod;
import com.github.franckyi.wsc.capability.RedstoneCapabilities;
import com.github.franckyi.wsc.capability.redstonelink.IRedstoneLink;
import com.github.franckyi.wsc.handlers.GuiHandler;
import com.github.franckyi.wsc.handlers.PacketHandler;
import com.github.franckyi.wsc.logic.BaseRedstoneController;
import com.github.franckyi.wsc.logic.FullRedstoneController;
import com.github.franckyi.wsc.logic.MasterRedstoneSwitch;
import com.github.franckyi.wsc.logic.SlaveRedstoneSwitch;
import com.github.franckyi.wsc.network.UpdateRedstoneControllerMessage;
import com.github.franckyi.wsc.tileentity.TileEntityRedstoneSwitch;
import com.github.franckyi.wsc.util.ChatUtil;
import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class BlockRedstoneSwitch extends Block {

	public BlockRedstoneSwitch(String name, Material mat, CreativeTabs tab, float hardness, float resistance,
			String tool, int harvest, float light) {
		super(mat);
		setUnlocalizedName(name);
		setRegistryName(name);
		setCreativeTab(tab);
		setHardness(hardness);
		setResistance(resistance);
		setHarvestLevel(tool, harvest);
		setLightLevel(light);
		isBlockContainer = true;
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		Optional<SlaveRedstoneSwitch> s = RedstoneCapabilities.getSwitch(world, pos);
		if (s.isPresent()) {
			Set<FullRedstoneController> updateControllers = new HashSet<FullRedstoneController>();
			for (final BlockPos controllerPos : s.get().getControllerPos()) {
				final Optional<BaseRedstoneController> controller = RedstoneCapabilities.getController(world,
						controllerPos);
				if (controller.isPresent()) {
					Optional<MasterRedstoneSwitch> toRemove = Optional.empty();
					for (MasterRedstoneSwitch controllerSwitch : controller.get().getSwitches()) {
						if (controllerSwitch.getSwitchPos().equals(pos)) {
							toRemove = Optional.of(controllerSwitch);
							break;
						}
					}
					if (toRemove.isPresent()) {
						controller.get().getSwitches().remove(toRemove.get());
						updateControllers.add(new FullRedstoneController(controller.get(), controllerPos));

					}
				}
			}
			WorldServer worldServer = (WorldServer) world;
			for (EntityPlayer player : worldServer.playerEntities) {
				EntityPlayerMP playerMP = (EntityPlayerMP) player;
				if (worldServer.getPlayerChunkMap()
						.getEntry(world.getChunkFromBlockCoords(pos).x, world.getChunkFromBlockCoords(pos).z)
						.containsPlayer(playerMP))
					PacketHandler.INSTANCE.sendTo(new UpdateRedstoneControllerMessage(updateControllers), playerMP);
			}
		}
		super.breakBlock(world, pos, state);
		world.removeTileEntity(pos);
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
	public boolean canProvidePower(IBlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityRedstoneSwitch();
	}

	@Override
	public int getWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		Optional<SlaveRedstoneSwitch> bls = RedstoneCapabilities.getSwitch(world, pos);
		if (bls.isPresent() && bls.get().isEnabled())
			return bls.get().getPower();
		return 0;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote && playerIn.isSneaking()) {
			Optional<SlaveRedstoneSwitch> s = RedstoneCapabilities.getSwitch(worldIn, pos);
			if (s.isPresent()) {
				IRedstoneLink link = RedstoneCapabilities.getLink(playerIn);
				link.setSwitch(new MasterRedstoneSwitch(s.get(), pos));
				ChatUtil.sendInfo(playerIn, "Switch '" + s.get().getName() + "' selected.");
			} else
				ChatUtil.sendError(playerIn, "Unable to get switch's data !");
		} else if (worldIn.isRemote && !playerIn.isSneaking())
			playerIn.openGui(WSCMod.instance, GuiHandler.REDSTONE_SWITCH_GUI, worldIn, pos.getX(), pos.getY(),
					pos.getZ());
		return true;
	}

	@Override
	public boolean shouldCheckWeakPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return false;
	}

}
