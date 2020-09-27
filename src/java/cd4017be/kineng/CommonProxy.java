package cd4017be.kineng;

import static cd4017be.kineng.Objects.*;
import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.kineng.physics.Formula;
import cd4017be.kineng.recipe.ProcessingRecipes;
import net.minecraftforge.common.MinecraftForge;

/** @author CD4017BE */
public class CommonProxy {

	public void preInit() {
		MinecraftForge.EVENT_BUS.register(this);
		RecipeAPI.Handlers.put("grinder", ProcessingRecipes.GRINDER);
		RecipeAPI.Handlers.put("sawmill", ProcessingRecipes.SAWMILL);
	}

	public void init(ConfigConstants c) {
		M_WOOD.density = 750;
		M_WOOD.strength = 50e6;
		M_WOOD.friction = 0.05;
		M_IRON.density = 7860;
		M_IRON.strength = 150e6;
		M_IRON.friction = 0.01;
		M_BEDROCK.density = 1000;
		M_BEDROCK.strength = Double.POSITIVE_INFINITY;
		
		GRINDSTONE.J_dens = Formula.J_cylinder(0.25, 0.375) * M_WOOD.density + Formula.J_cylinder(1.0, 0.625) * 2500;
		GRINDSTONE.maxF = 50000;
		SAWBLADE.J_dens = Formula.J_cylinder(0.25, 0.9375) * M_WOOD.density + Formula.J_cylinder(1.0, 0.0625) * M_IRON.density;
		SAWBLADE.maxF = 10000;
	}

}
