package cd4017be.api.registry;

import java.util.HashMap;

import org.apache.logging.log4j.Level;

import cd4017be.thermokin.Main;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;

/**
 * provides some properties of the world environment like temperatures at certain locations and thermodynamic properties of blocks.
 * @author CD4017BE
 */
public class Environment {

	/**[K] worlds base temperature */
	public final float T0;
	/**[K/?] biome temperature modifier */
	public final float dT;
	/**[K*t*m²/J] atmospheric heat dissipation resistance */
	public final float R;

	/**
	 * @param T [K] worlds base temperature
	 * @param dT [K/?] biome temperature modifier
	 * @param R [K*t*m²/J] atmospheric heat dissipation resistance
	 */
	public Environment(double T, double dT, double R) {
		this.T0 = (float)T;
		this.dT = (float)dT;
		this.R = (float)R;
	}

	/**
	 * @param world
	 * @param pos
	 * @return [K] the temperature at the given position in the world
	 */
	public float getTemp(World world, BlockPos pos) {
		return T0 + dT * world.getBiome(pos).getFloatTemperature(pos);
	}

	/**
	 * @param state the block
	 * @param Rref [K*t/J] additional heat resistance to apply
	 * @return [J/K/t] environmental heat dissipation conductivity of the given block
	 */
	public float getCond(IBlockState state, float Rref) {
		BlockEntry e = blocks.get(state);
		if (e == null) e = materials.getOrDefault(state.getMaterial(), def_block);
		Rref += e.Re + R / e.Xe;
		return 1F / Rref;
	}

	//----------- Static ------------

	public static final HashMap<Integer, Environment> environments = new HashMap<Integer, Environment>();
	public static Environment defaultEnv;

	/** Default Heat resistance for unlisted materials */
	public static BlockEntry def_block;
	/** Heat resistance of registered block materials */
	public static final HashMap<Material, BlockEntry> materials = new HashMap<Material, BlockEntry>();
	/** Heat resistance of registered blocks */
	public static final HashMap<IBlockState, BlockEntry> blocks = new HashMap<IBlockState, BlockEntry>();

	public static Environment getEnvFor(World world) {
		Environment e = environments.get(world.provider.getDimension());
		return e != null ? e : defaultEnv;
	}

	public static float getResistanceFor(IBlockState state) {
		BlockEntry e = blocks.get(state);
		if (e == null) e = materials.getOrDefault(state.getMaterial(), def_block);
		return e.R;
	}

	public static void makeDefEnv() {
		FMLLog.log(Main.ID, Level.INFO, "Custom thermal environment properties registered for %d dimensions.", environments.size());
		if (Environment.defaultEnv == null) {
			FMLLog.log(Main.ID, Level.WARN, "No default thermal environment properties registered! FIX YOUR CONFIG !!!\nCreating environment default with fallback values.");
			defaultEnv = new Environment(270, 25, 1);
		}
		FMLLog.log(Main.ID, Level.INFO, "Custom thermal block properties registered for %d block materials and %d block states.", materials.size(), blocks.size());
		if (def_block == null) {
			FMLLog.log(Main.ID, Level.WARN, "No default thermal block properties registered! FIX YOUR CONFIG !!!\nCreating block default with fallback values.");
			def_block = new BlockEntry(1F);
		}
	}

	public static class BlockEntry {
		/**
		 * short version assuming a normal block with 5 faces exposed to air
		 * @param R [K*t/J] heat conduction resistance
		 */
		public BlockEntry(float R) {this.R = R; this.Re = R; this.Xe = 5F;}
		/**
		 * @param R [K*t/J] heat conduction resistance
		 * @param Re [K*t/J] base environment heat dissipation resistance
		 * @param Xe [m²] effective atmospheric heat dissipation surface
		 */
		public BlockEntry(float R, float Re, float Xe) {this.R = R; this.Re = Re; this.Xe = Xe;}
		/**[K*t/J] heat conduction resistance */
		public final float R;
		/**[K*t/J] base environment heat dissipation resistance */
		public final float Re;
		/**[m²] effective atmospheric heat dissipation surface */
		public final float Xe;
	}

}
