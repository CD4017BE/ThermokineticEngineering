package cd4017be.kineng.recipe;

import static cd4017be.kineng.tileentity.IKineticLink.*;
import java.util.*;
import java.util.function.*;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.kineng.Main;
import cd4017be.lib.script.Parameters;
import cd4017be.lib.util.ItemKey;
import cd4017be.lib.util.OreDictStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;

/** 
 * @author CD4017BE */
public class ProcessingRecipes implements IRecipeHandler {

	public final HashMap<ItemKey, KineticRecipe> recipes = new HashMap<>();
	public final String name;

	public ProcessingRecipes(int type, String name) {
		this.name = name;
		type >>= 8;
		if (type >= recipeList.length)
			recipeList = Arrays.copyOf(recipeList, recipeList.length << 1);
		recipeList[type] = this;
	}

	public KineticRecipe get(ItemStack ing) {
		KineticRecipe rcp = recipes.get(new ItemKey(ing));
		if (rcp == null && ing.getHasSubtypes())
			rcp = recipes.get(new ItemKey(new ItemStack(ing.getItem(), 1, OreDictionary.WILDCARD_VALUE)));
		return rcp;
	}

	public void add(KineticRecipe rcp) {
		recipes.put(new ItemKey(rcp.io[0]), rcp);
	}

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

	public String jeiName() {
		return Main.ID + ":" + name;
	}

	public static ProcessingRecipes[] recipeList = new ProcessingRecipes[16];
	public static final ProcessingRecipes SAWMILL = new ProcessingRecipes(T_SAWBLADE, "sawmill");
	public static final ProcessingRecipes GRINDER = new ProcessingRecipes(T_GRINDER, "grinder");
	public static final ProcessingRecipes LATHE = new ProcessingRecipes(T_ANGULAR, "lathe");
	public static IntConsumer JEI_SHOW_RECIPES;
	public static final ResourceLocation GUI_TEX = new ResourceLocation(Main.ID, "textures/gui/processing.png");

	public static ProcessingRecipes getRecipeList(int mode) {
		mode >>>= 8;
		return mode < recipeList.length ? recipeList[mode] : null;
	}

}
