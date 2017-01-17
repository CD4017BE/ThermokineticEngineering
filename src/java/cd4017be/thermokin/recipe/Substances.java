package cd4017be.thermokin.recipe;

import java.util.HashMap;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.thermokin.physics.GasState;
import cd4017be.thermokin.physics.Substance;

public class Substances implements IRecipeHandler {

	public static final Substances instance = new Substances();
	public static final String SUBST = "substance", ENV = "environment", BLOCK = "heatBlock";
	public static final HashMap<Integer, Environment> environments = new HashMap<Integer, Environment>();
	public static Environment defaultEnv;

	/** Default Heat resistance for unlisted materials */
	public static BlockEntry def_block;
	/** Default internal Heat resistance of machines for connected/disconnected sides */
	public static float def_con = 0.004F, def_discon;
	/** Heat resistance of block materials used as cover (no cover = air) */
	public static final HashMap<Material, BlockEntry> materials = new HashMap<Material, BlockEntry>();
	/** Heat resistance of blocks used as cover (no cover = air) */
	public static final HashMap<IBlockState, BlockEntry> blocks = new HashMap<IBlockState, BlockEntry>();

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
		} else if (param[0] == ENV){
			if (!(param.length == 7 && param[2] instanceof String && param[3] instanceof Double &&
				param[4] instanceof Double && param[5] instanceof Double && param[6] instanceof Double)) return false;
			Substance s = Substance.REGISTRY.getObject(new ResourceLocation((String)param[2]));
			if (s == null) return false;
			double P = (Double)param[3], T = (Double)param[4], dT = (Double)param[5], R = (Double)param[6];
			Environment e = new Environment(s, P, T, dT, R);
			if (param[1] instanceof Double) {
				int dim = ((Double)param[1]).intValue();
				environments.put(dim, e);
			} else if (defaultEnv == null) defaultEnv = e;
			return true;
		} else {
			if (!(param.length >= 3 && param[1] instanceof ItemStack && param[2] instanceof Double)) return false;
			double R = (Double)param[2];
			BlockEntry e;
			if (param.length == 5) {
				if (!(param[3] instanceof Double && param[4] instanceof Double)) return false;
				double Re = (Double)param[3], Xe = (Double)param[4];
				e = new BlockEntry((float)R, (float)Re, (float)Xe);
			} else e = new BlockEntry((float)R);
			ItemStack is = (ItemStack)param[1];
			Item i = is.getItem();
			if (!(i instanceof ItemBlock)) return false;
			IBlockState out = ((ItemBlock)i).block.getStateFromMeta(i.getMetadata(is.getMetadata()));
			blocks.put(out, e);
			return true;
		}
	}

	public static Environment getEnvFor(World world) {
		Environment e = environments.get(world.provider.getDimension());
		return e != null ? e : defaultEnv;
	}

	public static float getResistanceFor(IBlockState state) {
		BlockEntry e = blocks.get(state);
		if (e == null) e = materials.getOrDefault(state.getMaterial(), def_block);
		return e.R;
	}

	public static class Environment {
		public final Substance type;
		public final double P;
		public final float T0, dT, R;

		public Environment(Substance type, double P, double T, double dT, double R) {
			this.type = type;
			this.P = P;
			this.T0 = (float)T;
			this.dT = (float)dT;
			this.R = (float)R;
		}

		public float getTemp(World world, BlockPos pos) {
			return T0 + dT * world.getBiomeGenForCoords(pos).getFloatTemperature(pos);
		}

		public GasState getGas(World world, BlockPos pos, double V) {
			double T = getTemp(world, pos);
			return new GasState(type, T, P * V / T, V);
		}

		public float getCond(IBlockState state, float Rref) {
			BlockEntry e = blocks.get(state);
			if (e == null) e = materials.getOrDefault(state.getMaterial(), def_block);
			Rref += e.Re + e.Xe * R;
			return 1F / Rref;
		}
	}

	public static class BlockEntry {
		public BlockEntry(float R) {this.R = R; this.Re = R; this.Xe = 1.0F;}
		public BlockEntry(float R, float Re, float Xe) {this.R = R; this.Re = Re; this.Xe = Xe;}
		public final float R, Re, Xe;
	}

}
