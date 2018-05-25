package cd4017be.thermokin.module;

import cd4017be.api.heat.SidedHeatReservoir;
import cd4017be.thermokin.tileentity.ModularMachine;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import static java.lang.Float.*;

import java.util.List;

/**
 * Provides an internal heat reservoir with integrated thermodynamics simulation to a ModularMachine
 * @author cd4017be
 */
public class HeatModule extends SidedHeatReservoir implements IPartListener, ITickable {

	public static int CHECK_INTERVAL = 10;
	protected float Tacc, Tmax = NaN;
	protected int timer;

	/**
	 * @param C [J/K] heat capacity
	 */
	public HeatModule(float C) {
		super(C);
	}

	@Override
	public void onPlaced(ModularMachine m, NBTTagCompound nbt) {
		for (int i = 0; i < 6; i++)
			onPartChanged(m, i, null);
	}

	@Override
	public void addDrops(ModularMachine m, NBTTagCompound nbt, List<ItemStack> drops) {
	}

	@Override
	public void readNBT(NBTTagCompound nbt, String k, TileEntity te) {
		super.readNBT(nbt, k, te);
		onPlaced((ModularMachine)te, nbt);
	}

	@Override
	public void onPartChanged(ModularMachine m, int i, Part old) {
		Tmax = NaN;
		if (i >= 12) return;
		i %= 6;
		float L = 0;
		L += m.components[i].Lh;
		L += m.components[i + 6].Lh;
		setR(EnumFacing.VALUES[i], 1F / L);
	}

	@Override
	public void onCfgChange(ModularMachine m, int i, int cfg) {
	}

	@Override
	public void update() {
		Tacc += T;
		if (++timer >= CHECK_INTERVAL) {
			timer = 0;
			ModularMachine m = (ModularMachine)tile;
			if (Float.isNaN(Tmax)) {
				Tmax = POSITIVE_INFINITY;
				for (Part p : m.components)
					if (p.Tmax < Tmax)
						Tmax = p.Tmax;
			}
			float t = Tacc / (float)CHECK_INTERVAL;
			if (t > Tmax)
				for (int i = 0; i < m.components.length; i++) {
					Part p = m.components[i];
					float dT = t - p.Tmax;
					if (dT > 0) m.damagePart(i, dT * p.dmgH * (float)CHECK_INTERVAL);
				}
			Tacc = 0;
		}
	}

}
