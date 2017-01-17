package cd4017be.thermokin.physics;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.util.text.translation.I18n;
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
	 * @param T [K] minimum temperature in Kelvin required to let the liquid evaporate at normal pressure (101.25kPa). 
	 * @return this instance for construction convenience
	 */
	public Substance setEvapEnergyAndTemp(double E, double T) {
		Qe = E * m;
		K = Pn * Math.exp(Qe / T);
		return this;
	}

	public static int getId(Substance s) {
		return s == null ? -1 : REGISTRY.getIDForObject(s);
	}

	public String localizedName() {
		return I18n.translateToLocal("subst." + name + ".name");
	}

}
