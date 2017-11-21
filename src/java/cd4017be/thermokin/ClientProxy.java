package cd4017be.thermokin;

import cd4017be.lib.ClientInputHandler;
import cd4017be.lib.render.SpecialModelLoader;

public class ClientProxy extends CommonProxy {

	@Override
	public void init() {
		super.init();
		ClientInputHandler.init();
		SpecialModelLoader.setMod(Main.ID);
		
	}

	@Override
	public void registerRenderers() {
		super.registerRenderers();
		
	}

}
