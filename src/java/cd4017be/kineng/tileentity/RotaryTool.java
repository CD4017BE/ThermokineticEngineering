package cd4017be.kineng.tileentity;

import cd4017be.kineng.block.BlockRotaryTool;
import cd4017be.kineng.physics.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

/** 
 * @author CD4017BE */
public class RotaryTool extends ShaftPart implements IKineticLink {

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
	public int radius() {
		return (int)Math.ceil(block().r);
	}

	@Override
	public int type() {
		return this.<BlockRotaryTool>block().type;
	}

}
