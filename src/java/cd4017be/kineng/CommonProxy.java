package cd4017be.kineng;

import static cd4017be.kineng.Objects.*;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import net.minecraftforge.common.MinecraftForge;

/** @author CD4017BE */
public class CommonProxy {

	public void preInit() {
		MinecraftForge.EVENT_BUS.register(this);
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
	}

}
