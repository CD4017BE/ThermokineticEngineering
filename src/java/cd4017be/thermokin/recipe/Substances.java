package cd4017be.thermokin.recipe;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.logging.log4j.Level;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.registry.GameRegistry;
import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.thermokin.Config;
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

	public static void init() {
		RecipeAPI.Handlers.put(SUBST, instance);
		RecipeAPI.Handlers.put(ENV, instance);
		RecipeAPI.Handlers.put(BLOCK, instance);
		addMat("default", null, 2.5F, 2.5F, 1.0F);
		addMat("IRON", Material.IRON, 0.01F, 0.01F, 1.0F);
		addMat("GLASS", Material.GLASS, 1.0F, 1.0F, 1.0F);
		addMat("ROCK", Material.ROCK, 1.2F, 1.2F, 1.0F);
		addMat("CLAY", Material.CLAY, 1.0F, 1.0F, 1.0F);
		addMat("GROUND", Material.GROUND, 1.5F, 1.5F, 1.0F);
		addMat("GRASS", Material.GRASS, 2.0F, 2.0F, 1.0F);
		addMat("SAND", Material.SAND, 4.0F, 4.0F, 1.0F);
		addMat("WOOD", Material.WOOD, 5.0F, 5.0F, 1.0F);
		addMat("PACKED_ICE", Material.PACKED_ICE, 0.4F, 0.4F, 1.0F);
		addMat("ICE", Material.ICE, 0.5F, 0.5F, 1.0F);
		addMat("CRAFTED_SNOW", Material.CRAFTED_SNOW, 12.0F, 12.0F, 1.0F);
		addMat("AIR", Material.AIR, 25.0F, 0.0F, 10.0F);
		addMat("SNOW", Material.SNOW, 15.0F, 15.0F, 1.0F);
		addMat("CLOTH", Material.CLOTH, 100.0F, 100.0F, 1.0F);
		addMat("LAVA", Material.LAVA, 2.0F, 1.6F, 0.5F);
		addMat("WATER", Material.WATER, 1.2F, 1.0F, 0.25F);
	}

	private static void addMat(String tag, Material m, float R, float Re, float Xe) {
		float[] args = Config.data.getFloatArray("Rmat." + tag);
		if (args.length == 3) {
			R = args[0];
			Re = args[1];
			Xe = args[2];
		}
		BlockEntry e = new BlockEntry(R, Re, Xe);
		if (m != null) Substances.materials.put(m, e);
		else Substances.def_block = e;
	}

	@Override
	public boolean addRecipe(Object... param) {
		if (SUBST.equals(param[0])) {
			if (!(param.length == 8 && param[1] instanceof String && param[2] instanceof String &&
				param[3] instanceof Double && param[4] instanceof Double && param[5] instanceof Double &&
				param[6] instanceof Double && param[7] instanceof Double)) {
				FMLLog.log("RECIPE", Level.ERROR, "expected: [\"%s\", <string>, <string>, <number>, <number>, <number>, <number>, <number>]\ngot: %s", SUBST, Arrays.deepToString(param));
				return false;
			}
			String name = (String)param[1];
			Substance s = new Substance(name);
			s.setRegistryName(name);
			try {s.setColor(Integer.parseInt((String)param[2], 16));} catch(NumberFormatException e) {
				FMLLog.log("RECIPE", Level.ERROR, "not a hexadecimal number: %s", param[2]);
				return false;
			}
			s.setDensities((Double)param[3], (Double)param[4]);
			s.setLiquidHeatCap((Double)param[5]);
			s.setEvapEnergyAndTemp((Double)param[6], (Double)param[7]);
			GameRegistry.register(s);
			return true;
		} else if (ENV.equals(param[0])) {
			if (!(param.length == 7 && param[2] instanceof String && param[3] instanceof Double &&
				param[4] instanceof Double && param[5] instanceof Double && param[6] instanceof Double)) {
				FMLLog.log("RECIPE", Level.ERROR, "expected: [\"%s\", <string>, <number>, <number>, <number>, <number>]\ngot: %s", ENV, Arrays.deepToString(param));
				return false;
			}
			Substance s = Substance.REGISTRY.getObject(new ResourceLocation((String)param[2]));
			if (s == null) {
				FMLLog.log("RECIPE", Level.ERROR, "invalid substance: %s", param[2]);
				return false;
			}
			double P = (Double)param[3], T = (Double)param[4], dT = (Double)param[5], R = (Double)param[6];
			Environment e = new Environment(s, P, T, dT, R);
			if (param[1] instanceof Double) {
				int dim = ((Double)param[1]).intValue();
				environments.put(dim, e);
			} else if (defaultEnv == null) defaultEnv = e;
			return true;
		} else {
			if (!(param.length >= 3 && param[1] instanceof ItemStack && param[2] instanceof Double)) {
				FMLLog.log("RECIPE", Level.ERROR, "expected: [\"%s\", <itemstack>, <number>, ...] \ngot: %s", BLOCK, Arrays.deepToString(param));
				return false;
			}
			double R = (Double)param[2];
			BlockEntry e;
			if (param.length == 5) {
				if (!(param[3] instanceof Double && param[4] instanceof Double)) {
					FMLLog.log("RECIPE", Level.ERROR, "expected: [\"%s\", <itemstack>, <number>, <number>, <number>] \ngot: %s", BLOCK, Arrays.deepToString(param));
					return false;
				}
				double Re = (Double)param[3], Xe = (Double)param[4];
				e = new BlockEntry((float)R, (float)Re, (float)Xe);
			} else e = new BlockEntry((float)R);
			ItemStack is = (ItemStack)param[1];
			Item i = is.getItem();
			if (!(i instanceof ItemBlock)) {
				FMLLog.log("RECIPE", Level.ERROR, "item doesn't represent a block: %s", param[1]);
				return false;
			}
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
