package cd4017be.api.registry;

import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.lib.script.Parameters;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import static cd4017be.api.registry.Environment.*;

/**
 * @author CD4017BE
 *
 */
public class ThermodynamicProperties implements IRecipeHandler {

	public static final String ENV = "environment", BLOCK = "heatBlock";
	public static final ThermodynamicProperties instance = new ThermodynamicProperties();

	public static void register() {
		RecipeAPI.Handlers.put(ENV, instance);
		RecipeAPI.Handlers.put(BLOCK, instance);
	}

	public static void init(ConfigConstants cfg) {
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
		if (m != null) materials.put(m, e);
		else def_block = e;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void addRecipe(Parameters p) {
		if (ENV.equals(p.getString(0))) {
			double T = p.getNumber(2), dT = p.getNumber(3), R = p.getNumber(4);
			Environment e = new Environment(T, dT, R);
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

}
