package cd4017be.api.registry;

import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.lib.script.Parameters;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.module.Layout;
import cd4017be.thermokin.module.Part;
import net.minecraft.item.ItemStack;

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
			Objects.casing.addCasing((int)param.getNumber(1), param.getString(2), param.getString(4), (float)stats[0] / 20F, (float)stats[1], (float)Part.MAX_DUR / (float)stats[2] / 20F);
		}
	}

}
