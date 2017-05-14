package cd4017be.thermokin.recipe;

import java.util.HashMap;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.lib.script.Parameters;
import cd4017be.lib.templates.IPipe;
import cd4017be.lib.templates.IPipe.Cover;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.multiblock.IHeatReservoir;
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

	public static void init(ConfigConstants cfg) {
		RecipeAPI.Handlers.put(SUBST, instance);
		RecipeAPI.Handlers.put(ENV, instance);
		RecipeAPI.Handlers.put(BLOCK, instance);
		addMat(cfg, "default", null, 2.5F, 2.5F, 1.0F);
		addMat(cfg, "IRON", Material.IRON, 0.01F, 0.01F, 1.0F);
		addMat(cfg, "GLASS", Material.GLASS, 1.0F, 1.0F, 1.0F);
		addMat(cfg, "ROCK", Material.ROCK, 1.2F, 1.2F, 1.0F);
		addMat(cfg, "CLAY", Material.CLAY, 1.0F, 1.0F, 1.0F);
		addMat(cfg, "GROUND", Material.GROUND, 1.5F, 1.5F, 1.0F);
		addMat(cfg, "GRASS", Material.GRASS, 2.0F, 2.0F, 1.0F);
		addMat(cfg, "SAND", Material.SAND, 4.0F, 4.0F, 1.0F);
		addMat(cfg, "WOOD", Material.WOOD, 5.0F, 5.0F, 1.0F);
		addMat(cfg, "PACKED_ICE", Material.PACKED_ICE, 0.4F, 0.4F, 1.0F);
		addMat(cfg, "ICE", Material.ICE, 0.5F, 0.5F, 1.0F);
		addMat(cfg, "CRAFTED_SNOW", Material.CRAFTED_SNOW, 12.0F, 12.0F, 1.0F);
		addMat(cfg, "AIR", Material.AIR, 25.0F, 0.0F, 10.0F);
		addMat(cfg, "SNOW", Material.SNOW, 15.0F, 15.0F, 1.0F);
		addMat(cfg, "CLOTH", Material.CLOTH, 100.0F, 100.0F, 1.0F);
		addMat(cfg, "LAVA", Material.LAVA, 2.0F, 1.6F, 0.5F);
		addMat(cfg, "WATER", Material.WATER, 1.2F, 1.0F, 0.25F);
	}

	private static void addMat(ConfigConstants cfg, String tag, Material m, float... R) {
		cfg.getVect("Rmat." + tag, R);
		BlockEntry e = new BlockEntry(R[0], R[1], R[2]);
		if (m != null) Substances.materials.put(m, e);
		else Substances.def_block = e;
	}

	@Override
	public void addRecipe(Parameters p) {
		if (SUBST.equals(p.getString(0))) {
			String name = p.getString(1);
			Substance s = new Substance(name);
			s.setRegistryName(name);
			s.setColor(Integer.parseInt(p.getString(2), 16));
			s.setDensities(p.getNumber(3), p.getNumber(4));
			s.setLiquidHeatCap(p.getNumber(5));
			s.setEvapEnergyAndTemp(p.getNumber(6), p.getNumber(7));
			GameRegistry.register(s);
		} else if (ENV.equals(p.getString(0))) {
			Substance s = Substance.REGISTRY.getObject(new ResourceLocation(p.getString(2)));
			if (s == null) throw new IllegalArgumentException(String.format("invalid substance: %s", p.param[2]));
			double P = p.getNumber(3), T = p.getNumber(4), dT = p.getNumber(5), R = p.getNumber(6);
			Environment e = new Environment(s, P, T, dT, R);
			if (p.param[1] instanceof Double) {
				int dim = (int)p.getNumber(1);
				environments.put(dim, e);
			} else if (defaultEnv == null) defaultEnv = e;
		} else {
			double R = p.getNumber(2);
			BlockEntry e;
			if (p.param.length == 5) {
				double Re = p.getNumber(3), Xe = p.getNumber(4);
				e = new BlockEntry((float)R, (float)Re, (float)Xe);
			} else e = new BlockEntry((float)R);
			ItemStack is = p.get(1, ItemStack.class);
			Item i = is.getItem();
			if (!(i instanceof ItemBlock)) throw new IllegalArgumentException(String.format("Item doesn't represent a block: %s", is));
			IBlockState out = ((ItemBlock)i).block.getStateFromMeta(i.getMetadata(is.getMetadata()));
			blocks.put(out, e);
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

		public Object getHeatObj(World world, BlockPos pos, EnumFacing side) {
			pos = pos.offset(side);
			if (!world.isBlockLoaded(pos)) return null;
			IBlockState state = world.getBlockState(pos);
			if (state.getBlock().hasTileEntity(state)) {
				TileEntity te = world.getTileEntity(pos);
				if (te == null) return null;
				IHeatReservoir hs = te.getCapability(Objects.HEAT_CAP, side.getOpposite());
				if (hs != null) return hs;
				if (te instanceof IPipe) {
					Cover c = ((IPipe)te).getCover();
					state = c == null ? Blocks.AIR.getDefaultState() : c.block;
				}
			}
			BlockEntry e = blocks.get(state);
			if (e == null) e = materials.getOrDefault(state.getMaterial(), def_block);
			return e.Re + e.Xe * R;
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
