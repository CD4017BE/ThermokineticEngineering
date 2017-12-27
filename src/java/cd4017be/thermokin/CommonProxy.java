package cd4017be.thermokin;

import cd4017be.api.recipes.RecipeScriptContext;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TickRegistry;
import cd4017be.lib.Gui.TileContainer;

public class CommonProxy {

	public void init() {
		TickRegistry.register();
		setConfig();
		
		BlockGuiHandler.registerContainer(Objects.ASSEMBLER, TileContainer.class);
	}

	private void setConfig() {
		ConfigConstants cfg = new ConfigConstants(RecipeScriptContext.instance.modules.get(Main.ConfigName));
		
	}

	public void registerRenderers() {
	}

}
