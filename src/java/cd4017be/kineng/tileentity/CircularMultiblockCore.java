package cd4017be.kineng.tileentity;

import java.util.HashMap;
import cd4017be.kineng.physics.ShaftAxis;
import cd4017be.lib.block.AdvancedBlock.INeighborAwareTile;
import cd4017be.lib.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

public abstract class CircularMultiblockCore extends ShaftPart implements INeighborAwareTile {

	private final HashMap<BlockPos, CircularMultiblockPart> parts = new HashMap<BlockPos, CircularMultiblockPart>();
	protected boolean updateRings = true;
	protected double J, dJ, dL;
	protected float R = 1;
	/**inner most invalid square radius = point to check from */
	private int firstInvalid;
	/**outer most valid square radius = current size of the structure*/
	private int lastValid;

	public void updateRings() {
		Axis axis = axis();
		int start = 1;
		while(start * start * 2 < firstInvalid) start++;
		firstInvalid = Integer.MAX_VALUE;
		for (int r = start; r * r < firstInvalid; r++)
			Utils.forRing(pos, axis, r, this::check);
		int end = Math.max(lastValid + 1, firstInvalid);
		for (int r = start; r * r < end; r++)
			Utils.forRing(pos, axis, r, this::fix);
		lastValid = firstInvalid - 1;
		float r = (float)Math.sqrt(firstInvalid);
		if (r != R) {
			R = r;
			markDirty(SYNC);
		}
		updateRings = false;
	}

	private void check(BlockPos pos, int sqr) {
		if (sqr >= firstInvalid || parts.containsKey(pos)) return;
		IBlockState state = world.getBlockState(pos);
		Block block = state.getBlock();
		if (block == multiblockPart()) {
			TileEntity te = world.getTileEntity(pos);
			if (te instanceof CircularMultiblockPart) {
				CircularMultiblockPart part = (CircularMultiblockPart)te;
				if (part.link == null || part.link == this) return;
			}
		} else if (valid(state, pos)) return;
		firstInvalid = sqr;
	}

	private void fix(BlockPos pos, int sqr) {
		if (sqr <= lastValid ^ sqr < firstInvalid) 
			if (sqr < firstInvalid) {
				IBlockState state = world.getBlockState(pos);
				Block block = state.getBlock();
				CircularMultiblockPart part;
				if (block == multiblockPart()) {
					part = (CircularMultiblockPart)world.getTileEntity(pos);
				} else {
					world.setBlockState(pos, multiblockPart().getDefaultState());
					part = (CircularMultiblockPart)world.getTileEntity(pos);
					part.storedBlock = state;
					create(part);
				}
				part.link(this);
				parts.put(pos, part);
			} else {
				CircularMultiblockPart part = parts.remove(pos);
				if (part != null) part.unlink();
			}
	}

	protected abstract Block multiblockPart();
	protected abstract void create(CircularMultiblockPart part);
	protected abstract boolean valid(IBlockState state, BlockPos pos);

	@Override
	public double setShaft(ShaftAxis shaft) {
		if (updateRings && shaft != null) updateRings();
		double av = super.setShaft(shaft);
		av += (dL - dJ * av) / J();
		dJ = 0;
		dL = 0;
		return av;
	}

	public void mergeMomentum(double J, double L) {
		this.J += J;
		dJ += J;
		dL += L;
	}

	public void remove(CircularMultiblockPart part) {
		if (unloaded) {
			part.v = vSave * part.r;
			return;
		}
		part.v = shaft.av() * part.r;
		double m = part.m * part.r * part.r;
		J -= m;
		shaft.markInvalid(false);
		BlockPos pos = part.getPos();
		parts.remove(pos);
		int d = (int)pos.distanceSq(this.pos);
		if (d < firstInvalid) {
			firstInvalid = d;
			updateRings = true;
		}
	}

	@Override
	public double J() {
		return super.J() + J;
	}

	@Override
	public void neighborBlockChange(Block b, BlockPos src) {
		if (updateRings) return;
		Axis axis = axis();
		if (Utils.coord(src, axis) != Utils.coord(pos, axis)) return;
		IBlockState state = world.getBlockState(src);
		if (state.getBlock() == multiblockPart() || valid(state, src)) {
			updateRings = true;
			shaft.markInvalid(false);
		}
	}

	@Override
	public void neighborTileChange(TileEntity te, EnumFacing side) {}

	@Override
	protected void storeState(NBTTagCompound nbt, int mode) {
		super.storeState(nbt, mode);
		nbt.setFloat("R", R);
	}

	@Override
	protected void loadState(NBTTagCompound nbt, int mode) {
		super.loadState(nbt, mode);
		float R1 = nbt.getFloat("R");
		if (mode == SYNC && R1 != R && shaft.renderInfo != null)
			shaft.renderInfo.invalidate();
		R = R1;
	}

	@Override
	public void onLoad() {
		super.onLoad();
		updateRings = true;
	}

	@Override
	protected void onUnload() {
		super.onUnload();
		for (CircularMultiblockPart part : parts.values()) part.unlink();
		parts.clear();
	}

}
