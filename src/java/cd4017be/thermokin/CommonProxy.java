package cd4017be.thermokin;

import cd4017be.api.recipes.RecipeScriptContext;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.api.registry.ThermodynamicProperties;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.TickRegistry;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.thermokin.module.Part;
import cd4017be.thermokin.module.PartIOModule;
import cd4017be.thermokin.module.PartIOModule.IOType;
import net.minecraft.item.ItemStack;
import static cd4017be.thermokin.Objects.*;

/**
 * 
 * @author CD4017BE
 */
public class CommonProxy {

	public void init() {
		TickRegistry.register();
		setConfig();
		
		BlockGuiHandler.registerContainer(Objects.ASSEMBLER, TileContainer.class);
	}

	private void setConfig() {
		ConfigConstants cfg = new ConfigConstants(RecipeScriptContext.instance.modules.get(Main.ConfigName));
		ThermodynamicProperties.init(cfg);
		
		double[] t = cfg.getVect("module_itemIO", new double[] {0,1500,25000});
		float Lh = (float)t[0] / 20F, Tmax = (float)t[1], dmgH = (float)Part.MAX_DUR / (float)t[2] / 20F;
		new PartIOModule(IOType.INT_ACC, 0x1, 1, new ItemStack(inv_io), Lh, Tmax, dmgH, 0);
		new PartIOModule(IOType.EXT_ACC, 0x1, 2, new ItemStack(inv_acc), Lh, Tmax, dmgH, 0);
		new PartIOModule(IOType.BUFFER, 0x1, 3, new ItemStack(inv_buff_s), Lh, Tmax, dmgH, 1);
		new PartIOModule(IOType.BUFFER, 0x1, 4, new ItemStack(inv_buff_m), Lh, Tmax, dmgH, 3);
	}

	public void registerRenderers() {
	}

}
