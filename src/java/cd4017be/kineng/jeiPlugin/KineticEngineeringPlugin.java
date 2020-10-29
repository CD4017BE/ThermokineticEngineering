package cd4017be.kineng.jeiPlugin;

import static cd4017be.kineng.Objects.*;
import static cd4017be.kineng.recipe.ProcessingRecipes.recipeList;
import java.util.*;
import java.util.Map.Entry;
import org.apache.commons.lang3.tuple.Pair;
import cd4017be.kineng.block.BlockProcessing;
import cd4017be.kineng.block.BlockRotaryTool;
import cd4017be.kineng.recipe.ProcessingRecipes;
import cd4017be.lib.util.ItemKey;
import mezz.jei.api.*;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.ItemStack;


/**
 * @author CD4017BE
 *
 */
@JEIPlugin
public class KineticEngineeringPlugin implements IModPlugin {

	ArrayList<String> allIds = new ArrayList<>();

	@Override
	public void register(IModRegistry registry) {
		addTool(registry, GRINDSTONE);
		addTool(registry, SAWBLADE);
		addTool(registry, LATHE);
		addTool(registry, PRESS);
		addMachine(registry, PROCESSING);
		for (ProcessingRecipes rcp : ProcessingRecipes.recipeList)
			if (rcp != null) {
				String id = rcp.jeiName();
				allIds.add(id);
				registry.handleRecipes(Entry.class, KineticRecipeW::new, id);
				registry.addRecipes(parseRecipeMap(rcp.recipes, KineticRecipeW::compare), id);
			}
	}

	@Override
	public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
		ArrayList<String> all = this.allIds;
		IRecipesGui gui = jeiRuntime.getRecipesGui();
		ProcessingRecipes.JEI_SHOW_RECIPES = (mode)-> {
			ProcessingRecipes rcp = ProcessingRecipes.getRecipeList(mode);
			gui.showCategories(rcp == null ? all : Collections.singletonList(rcp.jeiName()));
		};
	}

	private static void addTool(IModRegistry registry, BlockRotaryTool block) {
		ProcessingRecipes rcp = ProcessingRecipes.getRecipeList(block.type);
		if (rcp == null) return;
		registry.addRecipeCatalyst(new ItemStack(block), rcp.jeiName());
	}

	private static void addMachine(IModRegistry registry, BlockProcessing block) {
		ItemStack stack = new ItemStack(block);
		for (int i = 0, j = block.types >> 8; j != 0; j >>>= 1, i += 256) {
			if ((j & 1) == 0) continue;
			ProcessingRecipes rcp = ProcessingRecipes.getRecipeList(i);
			if (rcp == null) continue;
			registry.addRecipeCatalyst(stack, rcp.jeiName());
		}
	}

	@Override
	public void registerCategories(IRecipeCategoryRegistration registry) {
		IGuiHelper guiHelper = registry.getJeiHelpers().getGuiHelper();
		KineticRecipeW.BACKGROUND = guiHelper.createDrawable(ProcessingRecipes.GUI_TEX, 34, 15, 108, 27);
		KineticRecipeW.PROGRESSBAR = guiHelper.createDrawable(ProcessingRecipes.GUI_TEX, 224, 0, 32, 10);
		for (int i = 0; i < recipeList.length; i++) {
			ProcessingRecipes rcp = recipeList[i];
			if (rcp == null) continue;
			registry.addRecipeCategories(new KineticCategory(guiHelper, rcp.jeiName(), i));
		}
	}

	private static <T> ArrayList<Entry<ItemKey, T>> parseRecipeMap(HashMap<ItemKey, T> map, Comparator<Entry<ItemKey, T>> comp) {
		ArrayList<Entry<ItemKey, T>> recipes = new ArrayList<>(map.entrySet());
		Collections.sort(recipes, comp);
		ArrayList<ItemStack> buffer = new ArrayList<>();
		T lr = null;
		int j = 0;
		for (int i = 0; i < recipes.size(); i++) {
			Entry<ItemKey, T> r = recipes.get(i);
			if (lr != null && !lr.equals(r.getValue())) {
				if (buffer.size() > 1)
					recipes.set(j, Pair.of(new ItemKey(buffer.toArray(new ItemStack[buffer.size()])), lr));
				recipes.set(++j, r);
				buffer.clear();
			}
			buffer.add(r.getKey().items[0]);
			lr = r.getValue();
		}
		if (lr != null) {
			if (buffer.size() > 1)
				recipes.set(j, Pair.of(new ItemKey(buffer.toArray(new ItemStack[buffer.size()])), lr));
			j++;
		}
		recipes.subList(j, recipes.size()).clear();
		return recipes;
	}

}
