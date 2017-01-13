package cd4017be.thermokin.physics;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class LiquidState {
	/** liquid amounts below this value are treated as empty to fix tiny leftovers from rounding errors */
	public static final double NULL = 0x1.0p-36;

	public Substance type;
	public double Vmax;
	public double V;
	public double T;

	public double Vrem() {return Vmax - V;}

	public LiquidState(Substance type, double Vmax, double V, double T) {
		this.type = type;
		this.Vmax = Vmax;
		this.V = V;
		this.T = T;
	}

	public static LiquidState readFromNBT(NBTTagCompound nbt, String k, double Vmax) {
		return new LiquidState(Substance.REGISTRY.getObject(new ResourceLocation(nbt.getString(k + "id"))), Vmax, nbt.getDouble(k + "V"), nbt.getDouble(k + "T"));
	}

	public void writeToNBT(NBTTagCompound nbt, String k) {
		nbt.setString(k + "id", type == null ? "" : type.getRegistryName().toString());
		nbt.setDouble(k + "V", V);
		nbt.setDouble(k + "T", T);
	}

	public void checkFullEmpty() {
		if (V < NULL) {
			V = 0;
			type = null;
		} else if (V > Vmax - NULL) {
			V = Vmax;
		}
	}

	public LiquidState copy() {
		return new LiquidState(type, Vmax, V, T);
	}

	public LiquidState copy(double V) {
		return new LiquidState(type, V, V, T);
	}

	public void merge(LiquidState s) {
		if (type == null) type = s.type;
		Vmax += s.Vmax;
		T = T * V + s.T * s.V;
		V += s.V;
		T /= V;
	}

	public void remove(LiquidState s) {
		Vmax -= s.Vmax;
		T = T * V - s.T * s.V;
		if ((V -= s.V) < NULL) type = null;
		else T /= V;
	}

	public double insert(LiquidState s) {
		if (type == null) type = s.type;
		double dV = Math.min(Vmax - V, s.V);
		T = T * V + s.T * dV;
		V += dV;
		T /= V;
		return dV;
	}

	public LiquidState drain(double dV) {
		Substance s = type;
		V -= dV;
		if (V < NULL) {dV += V; V = 0; type = null;}
		return new LiquidState(s, dV, dV, T);
	}

	public double E() {
		return type == null ? 0 : T * V * type.Dl * type.Cl;
	}

	public double C() {
		return type == null ? 0 : V * type.Dl * type.Cl;
	}

}
