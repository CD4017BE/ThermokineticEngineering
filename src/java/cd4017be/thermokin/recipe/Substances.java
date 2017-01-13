package cd4017be.thermokin.recipe;

import java.util.HashMap;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.thermokin.physics.GasState;
import cd4017be.thermokin.physics.Substance;

public class Substances implements IRecipeHandler {

	public static final Substances instance = new Substances();
	public static final String SUBST = "substance", ENV = "environment";
	public static final HashMap<Integer, Environment> environments = new HashMap<Integer, Environment>();
	public static Environment defaultEnv;

	@Override
	public boolean addRecipe(Object... param) {
		if (param[0] == SUBST) {
			if (!(param.length == 8 && param[1] instanceof String && param[2] instanceof String &&
				param[3] instanceof Double && param[4] instanceof Double && param[5] instanceof Double &&
				param[6] instanceof Double && param[7] instanceof Double)) return false;
			String name = (String)param[1];
			Substance s = new Substance(name);
			s.setRegistryName(name);
			try {s.setColor(Integer.parseInt((String)param[2], 16));} catch(NumberFormatException e) {return false;}
			s.setDensities((Double)param[3], (Double)param[4]);
			s.setLiquidHeatCap((Double)param[5]);
			s.setEvapEnergyAndTemp((Double)param[6], (Double)param[7]);
			GameRegistry.register(s);
			return true;
		} else {
			if (!(param.length == 4 && param[1] instanceof Double && param[2] instanceof String &&
				param[3] instanceof Double && param[4] instanceof Double && param[5] instanceof Double)) return false;
			Substance s = Substance.REGISTRY.getObject(new ResourceLocation((String)param[2]));
			int dim = ((Double)param[1]).intValue();
			double P = (Double)param[3], T = (Double)param[4], dT = (Double)param[5];
			environments.put(dim, new Environment(s, P, T, dT));
			return true;
		}
	}

	public static Environment getEnvFor(World world) {
		Environment e = environments.get(world.provider.getDimension());
		return e != null ? e : defaultEnv;
	}

	public static class Environment {
		public final Substance type;
		public final double P;
		public final float T0, dT;

		public Environment(Substance type, double P, double T, double dT) {
			this.type = type;
			this.P = P;
			this.T0 = (float)T;
			this.dT = (float)dT;
		}

		public float getTemp(World world, BlockPos pos) {
			return T0 + dT * world.getBiomeGenForCoords(pos).getFloatTemperature(pos);
		}

		public GasState getGas(World world, BlockPos pos, double V) {
			double T = getTemp(world, pos);
			return new GasState(type, T, P * V / T, V);
		}
	}

}
