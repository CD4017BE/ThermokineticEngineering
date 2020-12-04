package cd4017be.kineng.tileentity;

import cd4017be.kineng.physics.*;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import static net.minecraft.util.EnumFacing.*;
import static net.minecraft.util.EnumFacing.AxisDirection.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** @author CD4017BE */
public interface IGear extends IShaftPart {

	static int MAX_DIAMETER = 5;

	GearLink getCon(EnumFacing side);

	int diameter();

	BlockPos chainLink();

	void linkChain(BlockPos pos, ItemStack stack);

	@Override
	default double setShaft(ShaftAxis shaft, double v0) {
		v0 = IShaftPart.super.setShaft(shaft, v0);
		for(EnumFacing side : EnumFacing.values()) {
			GearLink con = getCon(side);
			if(con == null) continue;
			con.setShaft(shaft);
			if(shaft == null) con.connect(null);
		}
		return v0;
	}

	@Override
	default void connect(boolean client) {
		if (invalid()) return;
		World world = ((TileEntity)this).getWorld();
		BlockPos pos = ((TileEntity)this).getPos();
		Axis axis = axis();
		int d0 = diameter();
		boolean hasCon = false;
		for(EnumFacing side : VALUES) {
			GearLink con = getCon(side);
			if(con == null) continue;
			findCon: {
				if(con.other != null) break findCon;
				BlockPos pos1 = pos.offset(side, d0 + 1 >> 1);
				//find straight gear connection
				GearLink con1 = findGear(world, pos1, side, axis, d0 & 1);
				if(con1 != null) {
					con.connect(con1);
					break findCon;
				}
				//find right angle gear connections
				if ((d0 & 1) != 0) break findCon;
				con1 = findGear(world, pos1, getFacingFromAxis(NEGATIVE, axis), side.getAxis(), 0);
				if(con1 != null) {
					con.connect(con1);
					break findCon;
				}
				con1 = findGear(world, pos1, getFacingFromAxis(POSITIVE, axis), side.getAxis(), 0);
				if (con1 != null)
					con.connect(con1);
			}
			hasCon |= con.other != null;
		}
		if (!hasCon) {
			GearLink con1 = findChainedGear(world, pos, chainLink(), axis);
			if (con1 != null)
				getCon(null).connect(con1);
		}
		IShaftPart.super.connect(client);
	}

	static boolean link(IGear a, IGear b, ItemStack stack) {
		a.linkChain(null, ItemStack.EMPTY);
		b.linkChain(null, ItemStack.EMPTY);
		for (EnumFacing side : VALUES) {
			GearLink con = a.getCon(side);
			if(con != null && con.other != null)
				return false;
			con = b.getCon(side);
			if(con != null && con.other != null)
				return false;
		}
		a.linkChain(((TileEntity)b).getPos(), stack.splitStack(stack.getCount() >> 1));
		b.linkChain(((TileEntity)a).getPos(), stack);
		a.getCon(null).connect(b.getCon(null));
		return true;
	}

	static GearLink findGear(World world, BlockPos pos, EnumFacing dir, Axis axis, int d) {
		for(; d <= MAX_DIAMETER; d += 2, pos = pos.offset(dir)) {
			if(!world.isBlockLoaded(pos)) break;
			TileEntity te = world.getTileEntity(pos);
			if(!(te instanceof IGear)) continue;
			IGear g = (IGear)te;
			if(g.axis() != axis || g.diameter() != d) break;
			return g.getCon(dir.getOpposite());
		}
		return null;
	}

	static GearLink findChainedGear(World world, BlockPos pos, BlockPos pos1, Axis axis) {
		if (pos1 == null) return null;
		if(!world.isBlockLoaded(pos1)) return null;
		TileEntity te = world.getTileEntity(pos1);
		if(!(te instanceof IGear)) return null;
		IGear g = (IGear)te;
		if(g.axis() != axis || !pos.equals(g.chainLink())) return null;
		return g.getCon(null);
	}

}
