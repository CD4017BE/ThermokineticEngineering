package cd4017be.api.registry;

import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.lib.script.Parameters;
import cd4017be.thermokin.module.Layout;
import cd4017be.thermokin.module.Part;
import cd4017be.thermokin.module.Part.Type;
import cd4017be.thermokin.render.ModularModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * 
 * @author CD4017BE
 */
public class PartRegistry implements IRecipeHandler {

	private static PartRegistry instance;
	private static final String MACHINE = "machine", CASING = "casePlate";

	public static void init() {
		if (instance == null) {
			instance = new PartRegistry();
			RecipeAPI.Handlers.put(MACHINE, instance);
			RecipeAPI.Handlers.put(CASING, instance);
		}
	}

	@Override
	public void addRecipe(Parameters param) {
		String k = param.getString(0);
		if (MACHINE.equals(k)) {
			Object[] ing = param.getArray(2);
			Layout l = Layout.addRecipe(param.get(1, ItemStack.class),
					ing.length > 0 && ing[0] instanceof ItemStack ? (ItemStack)ing[0] : ItemStack.EMPTY,
					ing.length > 1 && ing[1] instanceof ItemStack ? (ItemStack)ing[1] : ItemStack.EMPTY,
					ing.length > 2 && ing[2] instanceof ItemStack ? (ItemStack)ing[2] : ItemStack.EMPTY);
			//TODO add casing requirements
		} else if (CASING.equals(k)) {
			int id = (int)param.getNumber(1);
			double[] stats = param.getVector(3);
			Part p = new Part(Type.CASING, id, param.get(2, ItemStack.class), (float)stats[0], (float)stats[1], (float)stats[2]);
			String tex = param.getString(4);
			BlockRenderLayer layer = BlockRenderLayer.SOLID;
			int i = tex.lastIndexOf('@');
			if (i > 0) {
				String code = tex.substring(i+1).toLowerCase();
				tex = tex.substring(0, i);
				for (BlockRenderLayer l : BlockRenderLayer.values())
					if (l.toString().toLowerCase().startsWith(code)) {
						layer = l;
						break;
					}
			}
			p.opaque = layer == BlockRenderLayer.SOLID;
			if (FMLCommonHandler.instance().getSide().isClient())
				ModularModel.register(p, new ResourceLocation(tex), layer);
		}
	}

}
