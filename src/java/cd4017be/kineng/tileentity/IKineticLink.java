package cd4017be.kineng.tileentity;

import static net.minecraft.util.EnumFacing.VALUES;
import cd4017be.kineng.physics.*;
import net.minecraft.util.EnumFacing;

/** 
 * @author CD4017BE */
public interface IKineticLink extends IShaftPart {

	/** {@link #type()} bit-masks */
	public static final int T_TIER = 0xff, T_SHAPE = ~T_TIER;
	/** {@link #type()} standard shapes */
	public static final int
	T_ANGULAR = 0x000, T_BELT = 0x100, T_LINEAR = 0x200, T_MAGNETIC = 0x300,
	T_GRINDER = 0x400, T_SAWBLADE = 0x500;

	/**@return the link type as a {@link #T_SHAPE} with a {@link #T_TIER}.
	 * Used to determine connection compatibility. */
	int type();

	ForceCon getCon(EnumFacing side);

	IForceProvider findLink(EnumFacing side);

	@Override
	default double setShaft(ShaftAxis shaft, double v0) {
		v0 = IShaftPart.super.setShaft(shaft, v0);
		for(EnumFacing side : EnumFacing.values()) {
			ForceCon con = getCon(side);
			if(con == null) continue;
			con.setShaft(shaft);
			if (shaft == null)
				con.link(null);
		}
		return v0;
	}

	@Override
	default void connect(boolean client) {
		if (invalid()) return;
		if (!client)
			for(EnumFacing side : VALUES)
				check(side);
		IShaftPart.super.connect(client);
	}

	default void check(EnumFacing side) {
		ForceCon con = getCon(side);
		if(con == null) return;
		IForceProvider fp = findLink(side);
		con.link(fp == null ? null : fp.connect(this, side.getOpposite()));
	}

}
