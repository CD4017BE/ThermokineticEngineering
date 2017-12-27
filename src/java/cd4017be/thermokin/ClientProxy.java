package cd4017be.thermokin;

import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.ClientInputHandler;
import cd4017be.lib.render.SpecialModelLoader;
import cd4017be.thermokin.render.gui.GuiAssembler;
import static cd4017be.thermokin.Objects.*;

public class ClientProxy extends CommonProxy {

	@Override
	public void init() {
		super.init();
		ClientInputHandler.init();
		SpecialModelLoader.setMod(Main.ID);
		
		BlockGuiHandler.registerGui(ASSEMBLER, GuiAssembler.class);
	}

	@Override
	public void registerRenderers() {
		super.registerRenderers();
		
		BlockItemRegistry.registerRender(ASSEMBLER);
	}

}
