package cd4017be.thermokin.module;

import cd4017be.api.heat.SidedHeatReservoir;
import cd4017be.thermokin.tileentity.ModularMachine;
import net.minecraft.util.EnumFacing;
import static java.lang.Float.*;

public class HeatModule extends SidedHeatReservoir implements IPartListener {

	public static int CHECK_INTERVAL = 10;
	protected float Tacc, Tmax = NaN;

	public HeatModule(float C) {
		super(C);
	}

	@Override
	public void onPartsLoad(ModularMachine m) {
		for (int i = 0; i < 6; i++) onPartChanged(m, i);
	}

	@Override
	public void onPartChanged(ModularMachine m, int i) {
		Tmax = NaN;
		if (i >= 12) return;
		i %= 6;
		EnumFacing side = EnumFacing.VALUES[i];
		float L = 0;
		Part part = m.components[i];
		if (part != null) L += part.Lh;
		part = m.components[i + 6];
		if (part != null) L += part.Lh;
		setR(side, 1F / L);
	}

	@Override
	public void prepareTick() {
		Tacc += T;
		super.prepareTick();
	}

	@Override
	public void runTick() {
		if (tile.getWorld().getTotalWorldTime() % CHECK_INTERVAL == 0) {
			ModularMachine m = (ModularMachine)tile;
			if (Tmax == NaN) {
				Tmax = POSITIVE_INFINITY;
				for (Part p : m.components)
					if (p != null && p.Tmax < Tmax)
						Tmax = p.Tmax;
			}
			Tacc /= CHECK_INTERVAL;
			if (Tacc > Tmax)
				for (int i = 0; i < m.components.length; i++) {
					Part p = m.components[i];
					if (p != null) {
						float dT = Tacc - p.Tmax;
						if (dT > 0) m.damagePart(i, dT * p.dmgH * (float)CHECK_INTERVAL);
					}
				}
		}
		super.runTick();
	}

}
