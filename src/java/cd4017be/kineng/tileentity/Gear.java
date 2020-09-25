package cd4017be.kineng.tileentity;

import static cd4017be.kineng.block.BlockGear.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import cd4017be.kineng.physics.*;

/** @author CD4017BE */
public class Gear extends ShaftPart implements IGear {

	public static double F_FRICTION = 0, A_CONTACT = 0.0625;
	private GearLink[] cons = new GearLink[4];

	@Override
	public double setShaft(ShaftAxis shaft, double v0) {
		v0 = IGear.super.setShaft(shaft, vSave);
		this.shaft = shaft;
		return v0;
	}

	@Override
	public GearLink getCon(EnumFacing side) {
		Axis a = axis();
		int d = (1 - (side.getAxis().ordinal() - a.ordinal() + 4) % 3)
			* side.getAxisDirection().getOffset();
		if (d == 0) return null;
		int i = side.ordinal();
		if (i >= 2 && a != Axis.X) i -= 2;
		GearLink con = cons[i];
		if (con != null) return con;
		con = new GearLink(this, diameter() * d * 0.5);
		ShaftMaterial mat = material();
		con.maxF = mat.strength * A_CONTACT;
		con.fricD = mat.friction;
		con.fricS = mat.friction * F_FRICTION;
		return cons[i] = con;
	}

	@Override
	public int diameter() {
		return getBlockState().getValue(DIAMETER);
	}

}
