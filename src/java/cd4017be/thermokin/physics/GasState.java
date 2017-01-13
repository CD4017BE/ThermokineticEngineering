package cd4017be.thermokin.physics;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

/**
 * Stores thermodynamic data about a gas volume
 * @author CD4017BE
 */
public class GasState {

	/**[K] Temperature */
	public double T;
	/**[J/K] Gas amount multiplied by universal gas constant */
	public double nR;
	/**[m³] Volume */
	public double V;
	/**Substance the gas is made of */
	public Substance type;

	public GasState(Substance s, double T, double nR, double V){
		this.type = s;
		this.T = T;
		this.nR = nR;
		this.V = V;
	}

	public static GasState readGasFromNBT(NBTTagCompound nbt, String k, double V) {
		Substance s = Substance.REGISTRY.getObject(new ResourceLocation(nbt.getString(k + "id")));
		return new GasState(s == null ? Substance.Default : s, nbt.getDouble(k + "T"), nbt.getDouble(k + "nR"), V);
	}

	public void writeGasToNBT(NBTTagCompound nbt, String k) {
		nbt.setString(k + "id", type.getRegistryName().toString());
		nbt.setDouble(k + "nR", nR);
		nbt.setDouble(k + "T", T);
	}

	public GasState copy(double n_V) {
		return new GasState(type, T, nR * n_V / V, n_V);
	}

	/**@return [Pa] Pressure */
	public double P() {return nR * T / V;}
	/**@return [J] Stored heat energy */
	public double E() {return nR * T;};
	/**
	 * Performs an adiabatic expansion/compression on this gas volume towards the given volume.
	 * @param n_V new volume
	 */
	public void adiabat(double n_V) {
		T *= V / n_V;
		V = n_V;
	}
	/**
	 * Splits the given volume off from this gas volume and returns it as new GasState.
	 * @param n_V split volume
	 * @return new GasState
	 */
	public GasState split(double n_V) {
		nR /= V;
		GasState s = new GasState(type, T, nR * n_V, n_V);
		V -= n_V;
		nR *= V;
		return s;
	}
	/**
	 * Extracts the given volume out of this volume by adiabatically expanding the gas.
	 * @param n_V extract volume
	 * @return new GasState
	 */
	public GasState extract(double n_V) {
		double x = V / (V + n_V);
		T *= x;
		nR *= x;
		return new GasState(type, T, nR * n_V / V, n_V);
	}
	/**
	 * Combines this gas volume with the given gas volume. Gases must be of same type!
	 * @param s other gas
	 */
	public void merge(GasState s) {
		if (type != s.type) throw new IllegalArgumentException(String.format("can not merge GasStates of different types: %s and %s", type.getRegistryName().toString(), s.type.getRegistryName().toString()));
		V += s.V;
		T = nR * T + s.nR * s.T;
		nR += s.nR;
		T /= nR;
	}
	/**
	 * Injects the given gas volume into this gas volume by adiabatically compressing the gas. If the gas types are different, the one with higher amount is taken.
	 * @param s other gas
	 */
	public void inject(GasState s) {
		if (s.nR > nR) type = s.type;
		T = nR * T + s.nR * s.T;
		nR += s.nR;
		T *= (V + s.V) / V / nR;
	}
	/**
	 * Moves gas from this volume into the given volume to achieve equal pressure.
	 * @param s other gas
	 * @return amount of gas moved (nR)
	 */
	public double exchange(GasState s) {
		if (s.type != type || P() <= s.P()) return 0F;
		double x = (double)Math.sqrt(s.V * s.nR * s.T / V / nR / T);
		double dV = (s.V - V * x) / (1 + x);
		T *= V / (dV + V);
		s.T *= s.V / (s.V - dV);
		double dnR = nR * dV / V;
		nR -= dnR; s.T = (s.T * s.nR + dnR * T) / (s.nR += dnR);
		return dnR;
	}

}
