package cd4017be.kineng.tileentity;

import static cd4017be.kineng.block.BlockGear.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import static net.minecraft.util.EnumFacing.AxisDirection.*;
import cd4017be.kineng.physics.*;

/** @author CD4017BE */
public class Gear extends ShaftPart implements IGear {

	public static double F_FRICTION = 0, A_CONTACT = 0.0625;
	private static final double[] J_dens = new double[6];
	static {
		for (int i = 0; i < J_dens.length; i++)
			J_dens[i] = Formula.J_cylinder(i * 0.5, 0.25) + ShaftPart.J_dens * 0.75;
	}
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
		int d = (side.getAxis().ordinal() - a.ordinal() + 4) % 3 - 1;
		if (d == 0) return null;
		int i = side.ordinal();
		if (i >= 2 && a != Axis.X) i -= 2;
		GearLink con = cons[i];
		if (con != null) return con;
		con = new GearLink(this, diameter() * d * (side.getAxisDirection() == NEGATIVE ? 0.5 : -0.5));
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

	@Override
	public double J() {
		return J_dens[diameter()] * material().density;
	}

	@Override
	public String model() {
		return "gear" + diameter() * 8 + " " + material().texture.toString();
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return gearSize(pos, axis(), diameter());
	}

}
