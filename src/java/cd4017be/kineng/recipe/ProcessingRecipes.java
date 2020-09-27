package cd4017be.kineng.recipe;

import java.util.HashMap;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.lib.script.Parameters;
import cd4017be.lib.util.ItemKey;
import cd4017be.lib.util.OreDictStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/** 
 * @author CD4017BE */
public class ProcessingRecipes implements IRecipeHandler {

	public final HashMap<ItemKey, KineticRecipe> recipes = new HashMap<>();

	public KineticRecipe get(ItemStack ing) {
		KineticRecipe rcp = recipes.get(new ItemKey(ing));
		if (rcp == null && ing.getHasSubtypes())
			rcp = recipes.get(new ItemKey(new ItemStack(ing.getItem(), 1, OreDictionary.WILDCARD_VALUE)));
		return rcp;
	}

	public void add(KineticRecipe rcp) {
		recipes.put(new ItemKey(rcp.io[0]), rcp);
	}

	public static final ProcessingRecipes SAWMILL = new ProcessingRecipes();
	public static final ProcessingRecipes GRINDER = new ProcessingRecipes();

	@Override
	public void addRecipe(Parameters param) {
		Object[] out = param.getArrayOrAll(4);
		KineticRecipe rcp = new KineticRecipe(param.getNumber(3), param.getNumber(2), new ItemStack[out.length + 1]);
		System.arraycopy(out, 0, rcp.io, 1, out.length);
		Object o;
		if (param.param[1] instanceof OreDictStack)
			o = ((OreDictStack)param.param[1]).getItems();
		else o = param.get(1);
		for (Object e : o instanceof Object[] ? (Object[])o : new Object[] {o})
			if (e instanceof ItemStack) {
				if (rcp.io[0] == null) rcp.io[0] = (ItemStack)e;
				recipes.put(new ItemKey((ItemStack)e), rcp);
			}
	}

}
