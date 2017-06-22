package com.github.franckyi.wsc.blocks;

import java.util.Arrays;
import java.util.List;

import com.github.franckyi.wsc.capability.Capabilities;
import com.github.franckyi.wsc.capability.linkcap.ILink;
import com.github.franckyi.wsc.handlers.PacketHandler;
import com.github.franckyi.wsc.network.ControllerDataMessage;
import com.github.franckyi.wsc.network.UnlinkingMessage;
import com.github.franckyi.wsc.tileentity.TileEntityController;
import com.github.franckyi.wsc.util.ChatUtil;
import com.github.franckyi.wsc.util.MasterLogicalSwitch;
import com.github.franckyi.wsc.util.SlaveLogicalSwitch;
import com.google.common.base.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;

public class BlockRedstoneController extends Block {

	public static ItemStack tileEntityToItemStack(ItemStack is, TileEntityController te) {
		return is;
	}

	public BlockRedstoneController(String name, Material mat, CreativeTabs tab, float hardness, float resistance,
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
		List<MasterLogicalSwitch> switches = Capabilities.getControllerSwitches(world, pos);
		for (MasterLogicalSwitch mls : switches)
			PacketHandler.INSTANCE.sendToServer(new UnlinkingMessage(mls.getPos(), pos));
		super.breakBlock(world, pos, state);
		world.removeTileEntity(pos);
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		return new TileEntityController();
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		TileEntityController te = world.getTileEntity(pos) instanceof TileEntityController
				? (TileEntityController) world.getTileEntity(pos)
				: null;
		if (te != null)
			return Arrays.asList(tileEntityToItemStack(new ItemStack(state.getBlock()), te));
		return super.getDrops(world, pos, state, fortune);

	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (!worldIn.isRemote) {
			List<MasterLogicalSwitch> list = Capabilities.getControllerSwitches(worldIn, pos);
			if (playerIn.isSneaking()) {
				ILink link = Capabilities.getLink(playerIn);
				if (link.isPresent()) {
					for (MasterLogicalSwitch mls : list)
						if (mls.getPos().equals(link.getSwitch().getPos())) {
							ChatUtil.sendError(playerIn, "The switch is already linked to this controller !");
							return true;
						}
					if (list.size() < 4) {
						link.getSwitch().setLinked(true);
						list.add(link.getSwitch());
						Optional<SlaveLogicalSwitch> osls = Capabilities.getSwitch(worldIn, link.getSwitch().getPos());
						if (osls.isPresent()) {
							osls.get().getControllers().add(pos);
							osls.get().setLinked(true);
							Capabilities.updateTileEntity(worldIn, link.getSwitch().getPos());
							Capabilities.getLink(playerIn).reset();
							ChatUtil.sendSuccess(playerIn,
									"The switch has been successfully linked to this controller !");
						} else
							ChatUtil.sendError(playerIn, "Unable to access the Capability.");
					} else
						ChatUtil.sendError(playerIn, "The controller is full !");
				} else
					ChatUtil.sendError(playerIn, "You must select a switch first.");

			} else
				PacketHandler.INSTANCE.sendTo(new ControllerDataMessage(Side.SERVER, list, pos),
						(EntityPlayerMP) playerIn);
		}
		return true;
	}

}
