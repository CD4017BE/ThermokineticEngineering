package cd4017be.kineng.tileentity;

import java.util.List;

import cd4017be.lib.block.AdvancedBlock.INeighborAwareTile;
import cd4017be.lib.block.AdvancedBlock.ITilePlaceHarvest;
import cd4017be.lib.tileentity.BaseTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;

public class CircularMultiblockPart extends BaseTileEntity implements INeighborAwareTile, ITilePlaceHarvest {

	public CircularMultiblockCore link;
	public double r, m, v;
	public IBlockState storedBlock = Blocks.AIR.getDefaultState();
	public boolean isLinked;

	public void link(CircularMultiblockCore target) {
		if (link == target) return;
		link = target;
		isLinked = true;
		r = Math.sqrt(pos.distanceSq(target.getPos()));
		link.mergeMomentum(m * r * r, m * r * v);
		markDirty(REDRAW);
	}

	public void unlink() {
		if (link == null) return;
		link.remove(this);
		link = null;
		isLinked = false;
		if (!unloaded) markDirty(REDRAW);
	}

	@Override
	protected void onUnload() {
		super.onUnload();
		unlink();
	}

	@Override
	protected void storeState(NBTTagCompound nbt, int mode) {
		super.storeState(nbt, mode);
		if (mode == SAVE) {
			nbt.setDouble("m", m);
			if (link != null) v = link.shaft.av() * r;
			nbt.setDouble("v", v);
		}
		nbt.setInteger("block", Block.getStateId(storedBlock));
		if (mode >= CLIENT) nbt.setBoolean("lk", isLinked);
	}

	@Override
	protected void loadState(NBTTagCompound nbt, int mode) {
		super.loadState(nbt, mode);
		if (mode == SAVE) {
			m = nbt.getDouble("m");
			v = nbt.getDouble("v");
		}
		storedBlock = Block.getStateById(nbt.getInteger("block"));
		if (mode >= CLIENT) isLinked = nbt.getBoolean("lk");
	}

	@Override
	public void neighborBlockChange(Block b, BlockPos src) {
		if (link != null) link.neighborBlockChange(b, src);
	}

	@Override
	public void onPlaced(EntityLivingBase entity, ItemStack item) {}

	@Override
	public List<ItemStack> dropItem(IBlockState state, int fortune) {
		NonNullList<ItemStack> list = NonNullList.create();
		storedBlock.getBlock().getDrops(list, world, pos, storedBlock, fortune);
		return list;
	}

	@Override
	public void neighborTileChange(TileEntity te, EnumFacing side) {}

}
