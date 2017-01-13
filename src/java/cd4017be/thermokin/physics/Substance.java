package cd4017be.thermokin.physics;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry.Impl;
import static cd4017be.thermokin.physics.ThermodynamicUtil.*;

public class Substance extends Impl<Substance>{

	public static final RegistryNamespaced<ResourceLocation, Substance> REGISTRY = getSubstanceRegistry();

	public static Substance Default;

	/**[Pa] a constant for calculating temperature dependent evaporation pressure: <br> P = <b>K</b> / exp(<b>Qe</b> / T) */
	public double K = 3.44e10;
	/**[J/molR] energy required to evaporate a specific amount of liquid into gas */
	public double Qe = 4755;
	/**[1] heat capacity of the liquid compared to its gas form */
	public double Cl = 9.062;
	/**[molR/m³] specific density of the liquid */
	public double Dl = 461915;
	/**[kg/molR] specific mass */
	public double m = 2.1649e-3;
	/**[0xrrggbb] color used for rendering in GUIs */
	public int color = 0x8080ff;
	/** unlocalized name */
	public String name;

	public Substance(String name){
		this.name = name;
	}

	public Substance setColor(int rgb) {
		color = rgb;
		return this;
	}

	/**
	 * sets specific mass and specific liquid density.
	 * @param liq [kg/m³] liquid density
	 * @param mol [g/mol] mol-density (also called molecular weight)
	 * @return this instance for construction convenience
	 */
	public Substance setDensities(double liq, double mol) {
		m = mol / R / 1000D;
		Dl = liq / m;
		return this;
	}

	/**
	 * 
	 * @param C [J/kg/K] 
	 * @return this instance for construction convenience
	 */
	public Substance setLiquidHeatCap(double C) {
		Cl = C * m;
		return this;
	}

	/**
	 * sets evaporation energy and temperature. Densities must be set first!
	 * @param E [J/kg] energy in Joule required to evaporate 1kg liquid into gas
	 * @param T [K] minimum temperature in Kelvin required to let the liquid evaporate at normal atmospheric pressure. 
	 * @return this instance for construction convenience
	 */
	public Substance setEvapEnergyAndTemp(double E, double T) {
		Qe = E * m;
		K = NormalPressure * Math.exp(Qe / T);
		return this;
	}

	/**
	 * Sets the constants from Fluid properties (density, temperature, viscosity).
	 * However it is not recommended to use this method because mod authors set these values arbitrarily so they could be very inaccurate up to entire nonsense!
	 * @param liquid the Fluid representing the liquid state
	 * @param gas the Fluid representing the gas state (can be null)
	 * @return
	 */
	public Substance setStatsFromFluid(Fluid liquid, Fluid gas) {
		double lD, lV, lT, gD, gT;
		lT = (double)liquid.getTemperature();
		lD = (double)liquid.getDensity();
		lV = (double)liquid.getViscosity() * 0.001;
		if (gas != null) {
			gT = (double)gas.getTemperature();
			gD = (double)gas.getDensity();
			//Try to make sense out of negative density values
			if (gD <= 0) gD = 1.25 / (1.0 - gD);
			else gD += 1.25;
		} else {
			gT = 300;
			gD = lD / 800D;
		}
		m = gD / NormalPressure * gT;
		Dl = lD / m;
		return setLiquidHeatCap(4.18 * lV).setEvapEnergyAndTemp(2.20e3 * lV, lT + 73.0 * lV);
	}

	public static int getId(Substance s) {
		return s == null ? -1 : REGISTRY.getIDForObject(s);
	}

	public String localizedName() {
		return I18n.translateToLocal("subst." + name + ".name");
	}

}
