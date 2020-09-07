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
		M_WOOD.ρ = 750;
		M_WOOD.strength = 100;
		M_WOOD.μR = 0.05;
		M_IRON.ρ = 7860;
		M_IRON.strength = 500;
		M_IRON.μR = 0.01;
		M_BEDROCK.ρ = 50;
		M_BEDROCK.strength = Double.POSITIVE_INFINITY;
	}

}
