package cd4017be.kineng.jeiPlugin;

import cd4017be.kineng.Main;
import cd4017be.kineng.recipe.ProcessingRecipes;
import cd4017be.lib.util.TooltipUtil;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;


/**
 * @author CD4017BE
 *
 */
public class KineticCategory  implements IRecipeCategory<KineticRecipeW> {


	public final String uid;
	final IDrawable icon;
	final String title;

	public KineticCategory(IGuiHelper guiHelper, String uid, int type) {
		this.uid = uid;
		this.icon = guiHelper.createDrawable(ProcessingRecipes.GUI_TEX, 208, type * 16, 16, 16);
		this.title = TooltipUtil.translate("gui.kineng.pr_mode" + type);
	}

	@Override
	public String getUid() {
		return uid;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getModName() {
		return Main.ID;
	}

	@Override
	public IDrawable getBackground() {
		return KineticRecipeW.BACKGROUND;
	}

	@Override
	public IDrawable getIcon() {
		return icon;
	}

	@Override
	public void drawExtras(Minecraft minecraft) {
		icon.draw(minecraft, 19, 10);
	}

	@Override
	public void setRecipe(IRecipeLayout layout, KineticRecipeW wrapper, IIngredients ingredients) {
		IGuiItemStackGroup items = layout.getItemStacks();
		items.init(0, true, 0, 9);
		items.init(1, false, 72, 9);
		items.init(2, false, 90, 9);
		items.set(ingredients);
	}

}
