package cd4017be.api.registry;

import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.lib.script.Parameters;
import cd4017be.thermokin.Objects;
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
			double[] stats = param.getVector(3);
			addCasing((int)param.getNumber(1), param.getString(2), param.getString(4), (float)stats[0] / 20F, (float)stats[1], (float)Part.MAX_DUR / (float)stats[2] / 20F);
		}
	}

	public static Part addCasing(int id, String name, String texture, float Lh, float Tmax, float dmgH) {
		Part p = new Part(Type.CASING, id, Objects.casing.get(id), Lh, Tmax, dmgH);
		Objects.casing.add(p, name);
		BlockRenderLayer layer = BlockRenderLayer.SOLID;
		int i = texture.lastIndexOf('@');
		if (i > 0) {
			String code = texture.substring(i+1).toLowerCase();
			texture = texture.substring(0, i);
			for (BlockRenderLayer l : BlockRenderLayer.values())
				if (l.toString().toLowerCase().startsWith(code)) {
					layer = l;
					break;
				}
		}
		p.opaque = layer == BlockRenderLayer.SOLID;
		if (FMLCommonHandler.instance().getSide().isClient())
			ModularModel.register(p, new ResourceLocation(texture), layer);
		return p;
	}

}
