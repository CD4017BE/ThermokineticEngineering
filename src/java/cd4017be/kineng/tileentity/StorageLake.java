package cd4017be.kineng.tileentity;

import static net.minecraft.init.Blocks.AIR;
import java.util.Arrays;
import java.util.List;
import cd4017be.kineng.capability.StructureLocations;
import cd4017be.lib.block.AdvancedBlock.*;
import cd4017be.lib.tileentity.BaseTileEntity;
import cd4017be.lib.tileentity.BaseTileEntity.ITickableServerOnly;
import cd4017be.lib.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.*;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fluids.capability.*;


/** 
 * @author CD4017BE */
public class StorageLake extends BaseTileEntity
implements IFluidHandler, ITilePlaceHarvest, ISelfAwareTile, ITickableServerOnly, IInteractiveTile {

	public static float RAIN_MULT = 0.5F;

	public FluidStack content;
	/**{layer[0:15 blockMap pointer, 16:31 capacity [m³]],
	 *  size[0:5 r, 6:11 N, 12:17 S, 18:23 W, 24:29 E, 31 OF]} */
	public int[] layers = {0, 0};
	/** bit-map of blocks being part of the lake */
	public byte[] blockMap = new byte[16];
	public int level, lastAm, size, avgDrain;
	float rainfall = Float.NaN;
	private RainParser rp;

	@Override
	public void update() {
		avgDrain -= avgDrain >> 5;
		if (content == null || content.getFluid() != FluidRegistry.WATER) return;
		if (Float.isNaN(rainfall)) rp = new RainParser(pos).initialize();
		if (rp == null) {
			int n = Math.round(rainfall * (1F + world.rainingStrength));
			if (n > 0) fill(new FluidStack(FluidRegistry.WATER, n), true);
		} else {
			boolean done = !rp.doStep(world);
			rainfall = rp.rain * RAIN_MULT;
			if (done) rp = null;
		}
	}

	public void grow(int n) {
		n = content.amount += n;
		int d = Math.abs(n - lastAm);
		if (n == 0 ? d > 0 : level < layers.length && d > (layers[level] >> 16) * 10) {
			lastAm = content.amount;
			markDirty(SYNC);
		} else markDirty(SAVE);
	}

	@Override
	protected void storeState(NBTTagCompound nbt, int mode) {
		super.storeState(nbt, mode);
		if (content != null)
			nbt.setTag("fluid", content.writeToNBT(new NBTTagCompound()));
		nbt.setByte("lvl", (byte)level);
		if (mode <= CLIENT || redraw) {
			nbt.setIntArray("layers", layers);
			nbt.setByteArray("map", blockMap);
		}
	}

	@Override
	protected void loadState(NBTTagCompound nbt, int mode) {
		super.loadState(nbt, mode);
		content = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("fluid"));
		if (content != null && !validFluid(content.getFluid())) content = null;
		lastAm = content == null ? 0 : content.amount;
		level = nbt.getByte("lvl") & 0xff;
		if (mode <= CLIENT || redraw) {
			layers = nbt.getIntArray("layers");
			if (layers.length == 0) layers = new int[]{0};
			blockMap = nbt.getByteArray("map");
			for (int i = layers.length - 1; i >= 0; i--)
				if (layers[i] != 0) {
					int l = layers[i] & 0xffff;
					if (blockMap.length != l)
						blockMap = Arrays.copyOf(blockMap, l);
					break;
				}
			size = computeSize();
		}
	}

	/**mark layers above the current liquid line as invalid
	 * @param h [m] lowest invalid height level */
	public void updateLayers(int h) {
		h <<= 1;
		int l = Math.min(level, layers.length - 2);
		if (h < l) return;
		if (h == l) scan(h);
		for (h+=2; h < layers.length; h+=2)
			layers[h] = 0;
	}

	private boolean scan(int h) {
		int x = pos.getX(), y = pos.getY() + (h >> 1), z = pos.getZ();
		MutableBlockPos p = new MutableBlockPos();
		World w = world;
		if (h + 1 >= layers.length) layers = Arrays.copyOf(layers, h + 2);
		int i0 = h < 4 ? 0 : layers[h-4] & 0xffff;
		int i1 = h < 2 ? 0 : layers[h-2] & 0xffff;
		byte[] bm = blockMap;
		int m = 1;
		int i = i1, f = 0xff, prev = 0, r, of = 0;
		for (r = 1; r < 60; r++) {
			if (bm.length < i + r)
				if (i + r < 65536)
					blockMap = bm = Arrays.copyOf(bm, MathHelper.clamp(bm.length << 1, i + r, 65536));
				else {
					of |= 0xff00;
					break;
				}
			int n = 0, acc = 0;
			for (int b = 0; b < r; b++, i0++, i++) {
				if (b < r-1) f |= bm[i-r+1] & 0xff;
				f |= (i0 < i1 ? bm[i0] & 0xff : 0) << 8;
				if (f != 0) {
					f = visitBlocks(f | f >> 8, b, r, x, y, z, w, p, StorageLake::solid);
					of |= f;
					n += Integer.bitCount(f &= 0xff);
				}
				bm[i] = (byte)f;
				acc = acc(acc, f, prev, b);
				prev = prev & -256 | f;
			}
			if (n == 0) {
				i -= r;
				r--;
				break;
			}
			prev = merge(prev, acc, r) & -256;
			m += n;
		}
		prev >>= 2;
		of &= 0xff00;
		if (of != 0) {
			prev |= Integer.MIN_VALUE;
			if (level > h) level = layers.length;
		}
		layers[h] = m << 16 | i;
		int l = layers[h+1];
		layers[h+1] = r | prev;
		if (((l ^ prev) & 0x3fffffc0) != 0) {
			int acc = computeSize();
			if (acc != size) StructureLocations.setRange(this, 0,
				pos.getX() - (acc >> 18 & 0x3f), pos.getY(),
				pos.getZ() - (acc >> 6 & 0x3f),
				pos.getX() + (acc >> 24 & 0x3f) + 1, pos.getY() + 128,
				pos.getZ() + (acc >> 12 & 0x3f) + 1
			);
			size = acc;
		}
		if (((l ^ r) & 0x3f) != 0) {
			for (h+=2; h < layers.length; h+=2)
				layers[h] = 0;
			markDirty(REDRAW);
		}
		return of == 0;
	}

	private int computeSize() {
		int acc = 0;
		for (int j = 1; j < layers.length; j+=2) {
			int a = layers[j];
			int m = cmpge5x6(a, acc) * 0x3f;
			acc = acc & ~m | a & m;
		}
		return acc;
	}

	private static int acc(int acc, int f, int pf, int b) {
		acc |= f;
		f = f | pf >> 1;
		f = (f & 1) << 8 | (f & 4) << 12 | (f & 16) << 16 | (f & 64) << 20;
		return ~(f * 0x3f) & acc | b * f;
	}

	private static int merge(int all, int acc, int r) {
		acc = acc(acc, 0, all, r);
		int m
		= (acc <<  3 | acc <<  2) & 0x00000100
		| (acc <<  7 | acc << 10) & 0x00004000
		| (acc << 17 | acc << 20) & 0x00100000
		| (acc << 24 | acc << 25) & 0x04000000;
		acc = ~(m * 0x3f) & acc | r * m;
		m = cmpge5x6(acc >> 8, all >> 8) * 0xfc;
		return all & ~m | acc & m;
	}

	/**@param a = 5 * 6 bit int vector [0:5, 6:11, 12:17, 18:23, 24:29]
	 * @param b = 5 * 6 bit int vector [0:5, 6:11, 12:17, 18:23, 24:29]
	 * @return a >= b in bits[6, 12, 18, 24, 30] */
	static int cmpge5x6(int a, int b) {
		return (a & 0x3f03f03f) - (b & 0x3f03f03f) + 0x40040040 & 0x40040040
		     | (a & 0x00fc0fc0) - (b & 0x00fc0fc0) + 0x01001000 & 0x01001000;
	}

	/**@param open bit0 = should check, bit8 = is filled below
	 * @param world
	 * @param pos
	 * @return open >> 1 with bit8(open) = pos is open, bit16(open) = lake overflows */
	static int solid(int open, int x, int y, int z, World world, MutableBlockPos pos) {
		if ((open & 1) == 0 || world.getBlockState(pos.setPos(x, y, z)).getMaterial().blocksMovement())
			open &= 0xfeff;
		else if ((open & 0x100) == 0) {
			pos.setY(y - 1);
			if (world.getBlockState(pos).getMaterial().blocksMovement())
				open |= 0x100;
			else open |= 0x10000;
		}
		return open;
	}

	@Override
	public IFluidTankProperties[] getTankProperties() {
		return new IFluidTankProperties[] {new FluidTankProperties(content, capacity())};
	}

	private int capacity() {
		return level >= layers.length ? 0
			: (layers[level] >> 16) * 1000;
	}

	@Override
	public int fill(FluidStack resource, boolean doFill) {
		if (content == null) {
			Fluid fluid = resource.getFluid();
			if (!validFluid(fluid)) return 0;
			if (!doFill) return Math.min(resource.amount, capacity());
			content = new FluidStack(resource, 0);
			markDirty(SYNC);
		} else if (!content.isFluidEqual(resource)) return 0;
		int cap = capacity() - content.amount + avgDrain, n = 0;
		if (doFill && cap < resource.amount) {
			content.amount += cap;
			n = cap;
			fillLayer();
			cap = capacity() - content.amount + n + avgDrain;
		}
		cap = Math.min(cap, resource.amount);
		if (doFill) grow(cap - n);
		return cap;
	}

	@Override
	public FluidStack drain(FluidStack resource, boolean doDrain) {
		if (resource.isFluidEqual(content))
			return drain(resource.amount, doDrain);
		return null;
	}

	@Override
	public FluidStack drain(int maxDrain, boolean doDrain) {
		if (content == null) return null;
		if (doDrain && maxDrain > content.amount) drainLayer();
		maxDrain = Math.min(maxDrain, content.amount);
		if (doDrain) grow(-maxDrain);
		avgDrain += maxDrain;
		return new FluidStack(content, maxDrain);
	}

	private void fillLayer() {
		if (level >= layers.length || content.amount < 1000) return;
		MutableBlockPos p = new MutableBlockPos();
		World w = world;
		int x = pos.getX(), y = pos.getY() + (level >> 1), z = pos.getZ();
		fillBlock(1, x, y, z, w, p);
		int i1 = layers[level] & 0xffff;
		int i0 = level < 2 ? 0 : layers[level - 2] & 0xffff;
		for (int i = i0, r = 1; i < i1; r++)
			for (int b = 0; b < r; b++, i++) {
				int f = blockMap[i] & 0xff;
				if (f == 0) continue;
				f = visitBlocks(f, b, r, x, y, z, w, p, this::fillBlock);
				if (f != 0) return;
			}
		if (layers[level + 1] < 0) level = layers.length;
		else if ((level += 2) >= layers.length || layers[level] == 0)
			scan(level);
		markDirty(SYNC);
	}

	private void drainLayer() {
		if (level <= 0) return;
		while(level > 0 && layers[level -= 2] == 0);
		MutableBlockPos p = new MutableBlockPos();
		World w = world;
		int x = pos.getX(), y = pos.getY() + (level >> 1), z = pos.getZ();
		drainBlock(1, x, y, z, w, p);
		int i1 = layers[level] & 0xffff;
		int i0 = level < 2 ? 0 : layers[level - 2] & 0xffff;
		for (int i = i0, r = 1; i < i1; r++)
			for (int b = 0; b < r; b++, i++) {
				int f = blockMap[i] & 0xff;
				if (f != 0) visitBlocks(
					f, b, r, x, y, z, w, p, this::drainBlock
				);
			}
		markDirty(SYNC);
	}

	private int fillBlock(int m, int x, int y, int z, World world, MutableBlockPos pos) {
		if ((m & 1) == 0) return m;
		if (content.amount < 1000) return m | 0x100;
		IBlockState state = world.getBlockState(pos.setPos(x, y, z));
		Block block = state.getBlock();
		Material material = state.getMaterial();
		if (!material.isReplaceable() || material.isLiquid() && state == block.getDefaultState())
			return m;
		block.dropBlockAsItem(world, pos, state, 0);
		if (world.setBlockState(pos, content.getFluid().getBlock().getDefaultState(), layers[level+1] < 0 ? 3 : 2))
			content.amount -= 1000;
		return m;
	}

	private int drainBlock(int m, int x, int y, int z, World w, MutableBlockPos p) {
		if ((m & 1) == 0) return m;
		FluidStack fluid = Utils.getFluid(w, p.setPos(x, y, z), true);
		if (fluid == null) return m;
		if (content == null) content = fluid.copy();
		else if (content.isFluidEqual(fluid)) content.amount += fluid.amount;
		else return m;
		w.setBlockState(p, AIR.getDefaultState(), layers[level+1] < 0 ? 3 : 2);
		return m;
	}

	@Override
	public void onPlaced(EntityLivingBase entity, ItemStack item) {
		if (world.isRemote) return;
		BlockPos pos = this.pos;
		level = 0;
		while (scan((level += 2) - 2)) {
			FluidStack fluid = Utils.getFluid(world, pos = pos.up(), false);
			if (fluid == null) break;
			if (content == null) content = new FluidStack(fluid, 0);
			if (!content.isFluidEqual(fluid)) break;
		}
		drainLayer();
		markDirty(SYNC);
	}

	@Override
	public void breakBlock() {
		if (content == null) return;
		while(content.amount >= 1000 && level < layers.length)
			fillLayer();
	}

	@Override
	public List<ItemStack> dropItem(IBlockState state, int fortune) {
		return makeDefaultDrops(null);
	}

	@Override
	public void invalidate() {
		StructureLocations.remove(this, 0);
		super.invalidate();
	}

	public float partLevel() {
		return content == null || level >= layers.length ? 0
			: (float)content.amount / (float)((layers[level] >> 16) * 1000);
	}

	@Override
	public boolean hasFastRenderer() {
		return true;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(
			pos.getX() - (size >> 18 & 0x3f), pos.getY(),
			pos.getZ() - (size >> 6  & 0x3f),
			pos.getX() + (size >> 24 & 0x3f) + 1,
			pos.getY() + (layers.length >> 1) + 1,
			pos.getZ() + (size >> 12 & 0x3f) + 1
		);
	}

	public static boolean validFluid(Fluid fluid) {
		return fluid.canBePlacedInWorld() && !(fluid.isGaseous() || fluid.isLighterThanAir());
	}

	public static int visitBlocks(
		int mask, int b, int r, int x, int y, int z,
		World world, MutableBlockPos pos, BlockOperation op
	) {
		mask = op.visit(mask, x - r, y, z-b  , world, pos) >> 1;
		mask = op.visit(mask, x + r, y, z-b-1, world, pos) >> 1;
		mask = op.visit(mask, x + r, y, z+b  , world, pos) >> 1;
		mask = op.visit(mask, x - r, y, z+b+1, world, pos) >> 1;
		mask = op.visit(mask, x-b  , y, z + r, world, pos) >> 1;
		mask = op.visit(mask, x-b-1, y, z - r, world, pos) >> 1;
		mask = op.visit(mask, x+b  , y, z - r, world, pos) >> 1;
		mask = op.visit(mask, x+b+1, y, z + r, world, pos) >> 1;
		return mask;
	}

	@FunctionalInterface
	public interface BlockOperation {
		int visit(int mask, int x, int y, int z, World world, MutableBlockPos pos);
	}

	@Override
	public boolean onActivated(
		EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing s, float X, float Y, float Z
	) {
		if (world.isRemote) return true;
		String msg = String.format("rain water: %.0f mB/t", rainfall);
		if (rp != null) msg += " (still scanning ...)";
		player.sendStatusMessage(new TextComponentString(msg), true);
		return true;
	}

	@Override
	public void onClicked(EntityPlayer player) {}

}
