package cd4017be.kineng.jeiPlugin;

import java.util.*;
import java.util.Map.Entry;
import cd4017be.kineng.recipe.KineticRecipe;
import cd4017be.lib.util.ItemKey;
import cd4017be.lib.util.TooltipUtil;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeWrapper;
import mezz.jei.gui.TickTimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.ItemStack;


/**
 * @author CD4017BE
 *
 */
public class KineticRecipeW implements IRecipeWrapper {

	public static IDrawableStatic PROGRESSBAR, BACKGROUND;
	public static long t0;

	public final List<ItemStack> ingred, output;
	final String info;
	final int t;

	public KineticRecipeW(Entry<ItemKey, KineticRecipe> entry) {
		this.ingred = Arrays.asList(entry.getKey().items);
		KineticRecipe rcp = entry.getValue();
		this.output = new ArrayList<>(rcp.io.length - 1);
		for (int i = 1; i < rcp.io.length; i++)
			output.add(rcp.io[i]);
		this.info = TooltipUtil.format("\\%.3uN * %.3um", rcp.F, rcp.s);
		this.t = Math.max((int)(rcp.s * 500.0), 1);
	}

	@Override
	public void getIngredients(IIngredients ingredients) {
		ingredients.setInputLists(ItemStack.class, Collections.singletonList(ingred));
		ingredients.setOutputs(ItemStack.class, output);
	}

	@Override
	public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
		PROGRESSBAR.draw(minecraft, 38, 13, 0, 0, 0, TickTimer.getValue(t0, System.currentTimeMillis(), 32, t, true));
		FontRenderer fr = minecraft.fontRenderer;
		fr.drawString(info, 54 - (fr.getStringWidth(info) >> 1), 0, 0x404040);
	}

	public static int compare(Entry<ItemKey, KineticRecipe> a, Entry<ItemKey, KineticRecipe> b) {
		KineticRecipe rcpA = a.getValue(), rcpB = b.getValue();
		if (rcpA == rcpB) return 0;
		int c = Double.compare(rcpA.F, rcpB.F);
		if (c != 0) return c;
		ItemStack iA = rcpA.io[0], iB = rcpB.io[0];
		c = iA.getItem().getRegistryName().compareTo(iB.getItem().getRegistryName());
		if (c != 0) return c;
		return iA.getMetadata() - iB.getMetadata();
	}

}
