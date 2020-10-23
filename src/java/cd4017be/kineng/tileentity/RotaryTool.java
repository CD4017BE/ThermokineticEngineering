package cd4017be.kineng.tileentity;

import cd4017be.kineng.block.BlockRotaryTool;
import cd4017be.kineng.physics.*;
import cd4017be.lib.block.AdvancedBlock.INeighborAwareTile;
import cd4017be.lib.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;

/** 
 * @author CD4017BE */
public class RotaryTool extends ShaftPart implements IKineticLink, INeighborAwareTile {

	private ForceCon[] cons = new ForceCon[4];

	@Override
	public double setShaft(ShaftAxis shaft, double v0) {
		v0 = IKineticLink.super.setShaft(shaft, vSave);
		this.shaft = shaft;
		return v0;
	}

	@Override
	public ForceCon getCon(EnumFacing side) {
		Axis a = axis();
		int d = (1 - (side.getAxis().ordinal() - a.ordinal() + 4) % 3)
			* side.getAxisDirection().getOffset();
		if (d == 0) return null;
		int i = side.ordinal();
		if (i >= 2 && a != Axis.X) i -= 2;
		ForceCon con = cons[i];
		if (con != null) return con;
		BlockRotaryTool block = block();
		con = new ForceCon(this, block.r * d);
		con.maxF = block.maxF;
		return cons[i] = con;
	}

	@Override
	public IForceProvider findLink(EnumFacing side) {
		TileEntity te = world.getTileEntity(pos.offset(side));
		return te instanceof IForceProvider ? (IForceProvider)te : null;
	}

	@Override
	public int type() {
		return this.<BlockRotaryTool>block().type;
	}

	@Override
	public void neighborBlockChange(Block b, BlockPos src) {
		EnumFacing side = Utils.getSide(src, pos);
		if (side != null) check(side);
	}

	@Override
	public void neighborTileChange(TileEntity te, EnumFacing side) {}

}
