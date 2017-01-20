package cd4017be.thermokin.physics;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraftforge.fml.common.registry.RegistryBuilder;

public class ThermodynamicUtil {

	public static final ResourceLocation substanceRegistry = new ResourceLocation("cd4017be_lib:substances");

	public static final double Pn = 101250;
	public static final double R = 8.314472;

	@SuppressWarnings("unchecked")
	public static RegistryNamespaced<ResourceLocation, Substance> getSubstanceRegistry() {
		RegistryBuilder<Substance> builder = new RegistryBuilder<Substance>();
		builder.setType(Substance.class);
		builder.setName(substanceRegistry);
		builder.setIDRange(0, 255);
		return (RegistryNamespaced<ResourceLocation, Substance>)builder.create();
	}

	/**
	 * Calculates an evaporation from liquid to gas or a condensation from gas to liquid state following these rules: <br>
	 * 1. no Energy is lost or gained <br>
	 * 2. no Matter is lost or gained <br>
	 * 3. the total Volume doesn't expand or shrink <br>
	 * 4. Entropy is maximized: P = K / exp(Qe / T)
	 * @param liq the part in liquid form
	 * @param gas the part in gas form 
	 * @param tl number to use for temperature approximation: tl = Qe / T
	 * @return value of tl that should be given to this method next tick
	 */
	public static double evaporate(LiquidState liq, GasState gas, double tl) {
		Substance tp = gas.type;
		double Nges = liq.V * tp.Dl + gas.nR,
			Vges = liq.V + gas.V,
			Eges = gas.nR * (gas.T + tp.Qe) + liq.V * tp.Dl * tp.Cl * liq.T;
		if (tp.Qe * Nges / Eges > 1.0 - 1.0 / tp.Cl) {
			double a1 = -Nges * tp.Cl * tp.Qe,
				a2 = ((Eges / tp.Qe / Nges) / tp.Dl - Vges) * tp.K,
				a3 = ((tp.Cl - 1.0) * Vges - Nges / tp.Dl) * tp.K;
			double x = Math.exp(tl);
			double b0 = x * (Eges * tl + a1) + (a2 * tl + a3) * tl,
				b1 = x * (Eges * tl + Eges + a1) + 2.0 * a2 * tl + a3,
				b2 = x * (Eges * tl + a1) / 2.0 + Eges + a2;
			double p = b1 / b2 * 0.5, q = p * p - b0 / b2;
			tl += (q > 0 ? (p > 0 ? 1.0 : -1.0) * Math.sqrt(q) : 0) - p;
			if (!Double.isNaN(tl) && tl > 0) {
				double T = tp.Qe / tl;
				double y = tp.Qe + (1.0 - tp.Cl) * T;
				if (y > 0) {
					gas.nR = (Eges - Nges * tp.Cl * T) / y;
					liq.V = (Nges - gas.nR) / tp.Dl;
					if (liq.V > 0) {
						liq.type = tp;
						if (liq.V > liq.Vmax) {
							liq.V = liq.Vmax;
							gas.nR = Nges - liq.V * tp.Dl;
							T = (Eges - gas.nR * tp.Qe) / (gas.nR + liq.V * tp.Dl * tp.Cl);
						}
						gas.V = Vges - liq.V;
						gas.T = liq.T = T;
						return tl;
					}
				} else 
					tl = tp.Qe / gas.T;
			} else 
				tl = tp.Qe / gas.T;
		} else 
			tl = tp.Qe / gas.T;
		liq.V = 0;
		liq.type = null;
		gas.nR = Nges;
		gas.V = Vges;
		gas.T = liq.T = Eges / Nges - tp.Qe;
		return tl;
	}

	public static final double MinEvacFactor = 10.0;
	/**
	 * In case connected pipes contain different gas types, this is used to check whether the gas in the source pipe should spread over into a volume segment of the destination pipe
	 * @param src GasState of the source pipe
	 * @param dst GasState of the destination pipe
	 * @param dV size of the volume segment to spread in
	 * @return do spread
	 */
	public static boolean shouldSpread(GasState src, GasState dst, double dV) {
		double Pa0 = src.P(), Pb0 = dst.P();
		if (Pa0 <= Pb0) return false;
		double Pa1 = Pa0 * src.V / (src.V + dV), Pb1 = Pb0 * Math.min(MinEvacFactor, dst.V / (dst.V - dV));
		return Pa1 >= Pb1;
	}

}
