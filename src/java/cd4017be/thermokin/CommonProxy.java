package cd4017be.thermokin;

import cd4017be.api.recipes.RecipeScriptContext;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.lib.TickRegistry;

public class CommonProxy {

	public void init() {
		TickRegistry.register();
		setConfig();
		
	}

	private void setConfig() {
		ConfigConstants cfg = new ConfigConstants(RecipeScriptContext.instance.modules.get(Main.ConfigName));
		
	}

	public void registerRenderers() {
	}

}
