package cd4017be.thermokin;

import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.ClientInputHandler;
import cd4017be.lib.render.SpecialModelLoader;
import cd4017be.thermokin.render.CasingModel;
import cd4017be.thermokin.render.ModularModel;
import cd4017be.thermokin.render.gui.GuiAssembler;
import cd4017be.thermokin.render.gui.GuiDebug;
import cd4017be.thermokin.render.tesr.OvenRenderer;
import cd4017be.thermokin.tileentity.SolidFuelOven;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import static cd4017be.thermokin.Objects.*;

/**
 * 
 * @author CD4017BE
 */
public class ClientProxy extends CommonProxy {

	public ModularModel modModel;

	@Override
	public void init() {
		super.init();
		ClientInputHandler.init();
		SpecialModelLoader.setMod(Main.ID);
		
		SpecialModelLoader.registerBlockModel(DEBUG, new ModularModel(DEBUG));
		SpecialModelLoader.registerBlockModel(OVEN, new ModularModel(OVEN));
		SpecialModelLoader.registerItemModel(casing, new CasingModel());
		
		BlockGuiHandler.registerGui(ASSEMBLER, GuiAssembler.class);
		BlockGuiHandler.registerGui(DEBUG, GuiDebug.class);
	}

	@Override
	public void registerRenderers() {
		super.registerRenderers();
		
		BlockItemRegistry.registerRender(ASSEMBLER);
		BlockItemRegistry.registerRender(DEBUG);
		BlockItemRegistry.registerRender(OVEN);
		BlockItemRegistry.registerRender(wrench);
		BlockItemRegistry.registerRender(casing, null);
		
		ClientRegistry.bindTileEntitySpecialRenderer(SolidFuelOven.class, new OvenRenderer());
	}

}
