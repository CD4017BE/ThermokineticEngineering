package cd4017be.kineng;

import static cd4017be.kineng.Objects.*;
import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.kineng.recipe.ProcessingRecipes;
import cd4017be.kineng.tileentity.ManualPower;
import cd4017be.math.cplx.CplxF;
import net.minecraft.entity.passive.*;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

/** @author CD4017BE */
public class CommonProxy {

	public void preInit() {
		MinecraftForge.EVENT_BUS.register(this);
		for (ProcessingRecipes rcp : ProcessingRecipes.recipeList)
			if (rcp != null)
				RecipeAPI.Handlers.put(rcp.name, rcp);
	}

	public void init(ConfigConstants c) {
		RecipeAPI.Handlers.put("knife_cutting", flint_knife);
		M_WOOD.density = 750;
		M_WOOD.strength = 50e6;
		M_WOOD.friction = 0.05;
		M_WOOD.scrap = new ItemStack(Items.STICK, 2);
		M_STONE.density = 2500;
		M_STONE.strength = 20e6;
		M_STONE.friction = 0.1;
		M_STONE.scrap = new ItemStack(Blocks.COBBLESTONE);
		M_IRON.density = 7860;
		M_IRON.strength = 150e6;
		M_IRON.friction = 0.01;
		M_IRON.scrap = new ItemStack(Items.IRON_NUGGET, 6);
		M_BEDROCK.density = 1000;
		M_BEDROCK.strength = Double.POSITIVE_INFINITY;
		
		GRINDSTONE.setMaterials(M_STONE, 1.0, 0.625);
		GRINDSTONE.maxF = 50000;
		GRINDSTONE.scrap = new ItemStack(Items.FLINT, 4);
		SAWBLADE.setMaterials(M_IRON, 1.0, 0.0625);
		SAWBLADE.maxF = 10000;
		SAWBLADE.scrap = M_IRON.scrap;
		LATHE.setMaterials(M_WOOD, 0.5, 0.5);
		LATHE.maxF = 10000;
		LATHE.scrap = new ItemStack(Items.FLINT, 2);
		PRESS.setMaterials(M_STONE, 0.25, 0);
		PRESS.maxF = 50000;
		MAGNETS.setMaterials(M_IRON, 0.5, 0.625);
		MAGNETS.maxF = 2500000;
		MAGNETS.scrap = new ItemStack(Items.IRON_INGOT, 6);
		SHAFT_MAN.setMaterials(M_WOOD, 2.0, 0.125);
		SHAFT_MAN.av_max *= 0.5;
		SHAFT_MAN.maxF = 5000;
		SHAFT_MAN.scrap = new ItemStack(Items.STICK, 4);
		WATER_WHEEL.setMaterials(M_WOOD, 2.5, 1.0);
		WATER_WHEEL.maxF = 100000;
		ManualPower.ENTITY_STRENGTH.put(EntityPig.class, CplxF.C_(400F, 1200F));
		ManualPower.ENTITY_STRENGTH.put(EntityCow.class, CplxF.C_(400F, 1600F));
		ManualPower.ENTITY_STRENGTH.put(EntitySheep.class, CplxF.C_(400F, 1400F));
		ManualPower.ENTITY_STRENGTH.put(EntityHorse.class, CplxF.C_(400F, 3000F)); //P_max ~= 1hp
		ManualPower.ENTITY_STRENGTH.put(EntityDonkey.class, CplxF.C_(600F, 2400F));
	}

}
