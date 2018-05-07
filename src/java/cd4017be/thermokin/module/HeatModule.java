package cd4017be.thermokin.module;

import cd4017be.api.heat.SidedHeatReservoir;
import cd4017be.thermokin.tileentity.ModularMachine;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import static java.lang.Float.*;

import java.util.List;

public class HeatModule extends SidedHeatReservoir implements IPartListener, ITickable {

	public static int CHECK_INTERVAL = 10;
	protected float Tacc, Tmax = NaN;
	protected int timer;

	public HeatModule(float C) {
		super(C);
	}

	@Override
	public void onPlaced(ModularMachine m, NBTTagCompound nbt) {
		for (int i = 0; i < 6; i++)
			onPartChanged(m, i);
	}

	@Override
	public void addDrops(ModularMachine m, NBTTagCompound nbt, List<ItemStack> drops) {
	}

	@Override
	public void readNBT(NBTTagCompound nbt, String k) {
		super.readNBT(nbt, k);
		onPlaced((ModularMachine)tile, nbt);
	}

	@Override
	public void onPartChanged(ModularMachine m, int i) {
		Tmax = NaN;
		if (i >= 12) return;
		i %= 6;
		EnumFacing side = EnumFacing.VALUES[i];
		float L = 0;
		L += m.components[i].Lh;
		L += m.components[i + 6].Lh;
		setR(side, 1F / L);
	}

	@Override
	public void update() {
		Tacc += T;
		if (++timer >= CHECK_INTERVAL) {
			timer = 0;
			ModularMachine m = (ModularMachine)tile;
			if (Tmax == NaN) {
				Tmax = POSITIVE_INFINITY;
				for (Part p : m.components)
					if (p.Tmax < Tmax)
						Tmax = p.Tmax;
			}
			Tacc /= CHECK_INTERVAL;
			if (Tacc > Tmax)
				for (int i = 0; i < m.components.length; i++) {
					Part p = m.components[i];
					float dT = Tacc - p.Tmax;
					if (dT > 0) m.damagePart(i, dT * p.dmgH * (float)CHECK_INTERVAL);
				}
		}
	}

}
