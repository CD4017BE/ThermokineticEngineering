package cd4017be.kineng.tileentity;

import static cd4017be.kineng.physics.Ticking.dt;
import static net.minecraft.block.BlockDirectional.FACING;
import cd4017be.kineng.Objects;
import cd4017be.kineng.capability.StructureLocations;
import cd4017be.kineng.capability.StructureLocations.Entry;
import cd4017be.lib.block.AdvancedBlock.IInteractiveTile;
import cd4017be.lib.block.AdvancedBlock.INeighborAwareTile;
import cd4017be.lib.util.Utils;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.FluidStack;

/** 
 * @author CD4017BE */
public class LakeGate extends LakeConnection implements INeighborAwareTile, IInteractiveTile {

	public static final double g2 = 9.81 * 2.0, A = 1000D * dt / 15D;

	/** linked list of connected WaterWheels */
	FlowNode first, last;
	BlockPos scan = Utils.NOWHERE;
	StorageLake lake1;
	/** bits[0..2] = dir, bits[4..7] = [0-7:scanning, 8:dead end, 15:lake] */
	byte state;
	int rs, n;
	double v;

	@Override
	protected void tickLakeInteract() {
		n = 0;
		if (rs <= 0) return;
		if (state >= 0) {
			scanStep();
			return;
		}
		double h = relLiquidLvl();
		if (h <= 0) return;
		if (!areChunksLoaded()) return;
		int h1 = Integer.MIN_VALUE;
		if (lake1 != null) {
			h1 = (lake1.level >> 1) + scan.getY();
			if (h1 >= pos.getY() && (h -= h1 - pos.getY()) <= 0) return;
		}
		h *= g2;
		FluidStack stack = lake.drain(MathHelper.ceil((v = Math.sqrt(h)) * A * rs), true);
		if (stack == null) return;
		n = stack.amount;
		if (lake1 != null) lake1.fill(stack, true);
		for (FlowNode fn = first; fn != null; fn = fn.next)
			h = fn.tile.passLiquid(h + fn.dy * g2, stack, fn.dir);
	}

	private boolean areChunksLoaded() {
		TileEntity te;
		if (state == -16) {
			if (!world.isBlockLoaded(scan)) return false;
			if (lake1 == null || lake1.isInvalid()) {
				te = world.getTileEntity(scan);
				if (te instanceof StorageLake)
					lake1 = (StorageLake)te;
				else {
					reset();
					return false;
				}
			}
		}
		for (FlowNode fn = first; fn != null; fn = fn.next)
			if (!fn.refresh(world)) return false;
			else if (fn.tile == null) {
				reset();
				return false;
			}
		return true;
	}

	private void reset() {
		lake1 = null;
		scan = Utils.NOWHERE;
		state = 0;
		first = last = null;
	}

	@SuppressWarnings("deprecation")
	private void scanStep() {
		BlockPos pos1;
		if (scan.getY() < 0) {
			EnumFacing dir = getOrientation().front;
			pos1 = pos.offset(dir);
			if (!isSolidOrSource(world, pos1)) {
				FluidStack stack = lake.drain(1000, true);
				if (stack == null) return;
				Block liq = stack.getFluid().getBlock();
				world.setBlockState(pos1, liq.getStateFromMeta(stack.amount < 1000 ? 8 : 0), 2);
			}
			scan = pos1;
			state = (byte)dir.getHorizontalIndex();
			return;
		}
		EnumFacing dir = EnumFacing.HORIZONTALS[state & 3];
		checkWaterWheel(scan, dir);
		if (!isSolidOrSource(world, pos1 = scan.down())) state &= 3;
		else if (!isSolidOrSource(world, pos1 = scan.offset(dir))) state += 16;
		else { //can't go forward or down
			BlockPos pos2 = scan.offset(EnumFacing.HORIZONTALS[state+1 & 3]);
			BlockPos pos3 = scan.offset(EnumFacing.HORIZONTALS[state+3 & 3]);
			int s = (isSolidOrSource(world, pos2) ? 1 : 0) | (isSolidOrSource(world, pos3) ? 2 : 0);
			if (s == 0)
				s = (isSolidOrSource(world, pos2.down()) ? 1 : 0) | (isSolidOrSource(world, pos3.down()) ? 2 : 0);
			switch(s) {
			case 1: //right blocked but left open
				pos1 = pos3;
				state = (byte)(state + 0x13 & 0xf3);
				break;
			case 2: //left blocked but right open
				pos1 = pos2;
				state = (byte)(state + 0x11 & 0xf3);
				break;
			case 3: //both blocked -> try backwards
				if (state < 16 && !isSolidOrSource(world, pos2 = scan.offset(dir.getOpposite()))) {
					pos1 = pos2;
					state = (byte)(state + 0x12 & 0xf3);
					break;
				}
			default: //undecidable -> end
				state = -128;
			}
		}
		if (state >= 0) {
			Block liq = lake.content.getFluid().getBlock();
			if (liq instanceof BlockLiquid || liq instanceof BlockFluidClassic) {
				IBlockState bs = world.getBlockState(pos1);
				bs.getBlock().dropBlockAsItem(world, pos1, bs, 0);
				world.setBlockState(pos1, liq.getStateFromMeta(state == 0 ? 8 : state >> 4), 2);
			}
			scan = pos1;
		} else
			for (Entry e : StructureLocations.find(world, scan, 0)) {
			pos1 = e.core();
			if (state == -128 || pos1.getY() >= scan.getY()) {
				scan = pos1;
				state = -16;
			}
		}
	}

	private void checkWaterWheel(BlockPos pos, EnumFacing dir) {
		TileEntity te;
		check: {
			while((te = world.getTileEntity(pos = pos.down())) == null) {
				IBlockState state = world.getBlockState(pos);
				if (state.getBlock() != Objects.FILL_DIR || state.getValue(FACING) != EnumFacing.DOWN) break;
			}
			if (te instanceof IWaterWheel) break check;
			dir = dir.getOpposite();
			while((te = world.getTileEntity(pos = pos.up())) == null) {
				IBlockState state = world.getBlockState(pos);
				if (state.getBlock() != Objects.FILL_DIR || state.getValue(FACING) != EnumFacing.UP) break;
			}
			if (te instanceof IWaterWheel) break check;
			return;
		}
		int y = this.pos.getY() - scan.getY();
		for (FlowNode fn = first; fn != null; fn = fn.next) y -= fn.dy;
		FlowNode fn = new FlowNode(y, dir, pos, (IWaterWheel)te);
		if (last == null) first = fn;
		else last.next = fn;
		last = fn;
	}

	static boolean isSolidOrSource(World world, BlockPos pos) {
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		return !block.isReplaceable(world, pos)
			|| state.getMaterial().isLiquid() && state == block.getDefaultState();
	}

	@Override
	protected void storeState(NBTTagCompound nbt, int mode) {
		super.storeState(nbt, mode);
		nbt.setByte("state", state);
		nbt.setLong("scan", scan.toLong());
		nbt.setByte("rs", (byte)rs);
		NBTTagList list = new NBTTagList();
		for (FlowNode fn = first; fn != null; fn = fn.next) {
			NBTTagCompound tag = new NBTTagCompound();
			tag.setLong("p", fn.pos.toLong());
			tag.setByte("d", (byte)fn.dir.ordinal());
			tag.setShort("y", (short)fn.dy);
			list.appendTag(tag);
		}
		nbt.setTag("wheels", list);
	}

	@Override
	protected void loadState(NBTTagCompound nbt, int mode) {
		super.loadState(nbt, mode);
		state = nbt.getByte("state");
		scan = BlockPos.fromLong(nbt.getLong("scan"));
		rs = nbt.getByte("rs");
		NBTTagList list = nbt.getTagList("wheels", NBT.TAG_COMPOUND);
		first = last = null;
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			FlowNode fn = new FlowNode(
				tag.getShort("y"),
				EnumFacing.getFront(tag.getByte("d")),
				BlockPos.fromLong(tag.getLong("p")),
				null
			);
			if (last == null) first = fn;
			else last.next = fn;
			last = fn;
		}
	}

	@Override
	public void neighborBlockChange(Block b, BlockPos src) {
		rs = MathHelper.clamp(world.isBlockIndirectlyGettingPowered(pos), 0, 15);
	}

	@Override
	public void neighborTileChange(TileEntity te, EnumFacing side) {}

	@Override
	public boolean onActivated(
		EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing s, float X, float Y, float Z
	) {
		if (world.isRemote) return true;
		String msg;
		if (player.isSneaking()) {
			BlockPos pos = this.pos.offset(getOrientation().front);
			FluidStack fluid = Utils.getFluid(world, pos, true);
			if (fluid != null) {
				world.setBlockToAir(pos);
				if (lake != null) lake.fill(fluid, true);
			}
			rs = 0;
			reset();
			msg = "§6Path reset!";
		} else if (state >= 0) msg = "Pathing ...";
		else {
			int m = 0;
			for (FlowNode fn = first; fn != null; fn = fn.next) m++;
			msg = String.format(
				"%d mB/t @ %.1f m/s -> §9%d Wheels§f -> %s",
				n, v, m + 1, state == 9 ? "§aLake" : "§cVoid"
			);
		}
		player.sendStatusMessage(new TextComponentString(msg), true);
		return true;
	}

	@Override
	public void onClicked(EntityPlayer player) {}

	static class FlowNode {
		final int dy;
		final EnumFacing dir;
		final BlockPos pos;
		IWaterWheel tile;
		FlowNode next;

		FlowNode(int dy, EnumFacing dir, BlockPos pos, IWaterWheel tile) {
			this.dy = dy;
			this.dir = dir;
			this.pos = pos;
			this.tile = tile;
		}

		boolean refresh(World world) {
			if (!world.isBlockLoaded(pos)) return false;
			if (tile == null || tile.invalid()) {
				TileEntity te = world.getTileEntity(pos);
				tile = te instanceof IWaterWheel ? (IWaterWheel)te : null;
			}
			return true;
		}
	}

}
