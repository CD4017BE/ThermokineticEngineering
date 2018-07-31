package cd4017be.thermokin;

import cd4017be.api.recipes.RecipeScriptContext;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.api.registry.ThermodynamicProperties;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TickRegistry;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.thermokin.module.Part;
import cd4017be.thermokin.module.PartIOModule;
import cd4017be.thermokin.module.PartIOModule.IOType;
import static cd4017be.thermokin.Objects.*;

/**
 * 
 * @author CD4017BE
 */
public class CommonProxy {

	public void init() {
		TickRegistry.register();
		setConfig();
		
		BlockGuiHandler.registerContainer(ASSEMBLER, TileContainer.class);
		BlockGuiHandler.registerContainer(DEBUG, DataContainer.class);
	}

	private void setConfig() {
		ConfigConstants cfg = new ConfigConstants(RecipeScriptContext.instance.modules.get(Main.ConfigName));
		ThermodynamicProperties.init(cfg);
		
		Part.NULL_CASING.Lh = (float)cfg.getNumber("casePlate_air_cond", 0.3125) / 20F;
		
		double[] t = cfg.getVect("module_itemIO", new double[] {0,1500,25000});
		float Lh = (float)t[0] / 20F, Tmax = (float)t[1], dmgH = (float)Part.MAX_DUR / (float)t[2] / 20F;
		module.add(new PartIOModule(IOType.INT_ACC, 0x1, 1, module.get(1), Lh, Tmax, dmgH, 0), "inv_io");
		module.add(new PartIOModule(IOType.EXT_ACC, 0x1, 2, module.get(2), Lh, Tmax, dmgH, 0), "inv_acc");
		module.add(new PartIOModule(IOType.BUFFER, 0x1, 3, module.get(3), Lh, Tmax, dmgH, 1), "inv_buff_s");
		module.add(new PartIOModule(IOType.BUFFER, 0x1, 4, module.get(4), Lh, Tmax, dmgH, 3), "inv_buff_m");
	}

	public void registerRenderers() {
	}

}
