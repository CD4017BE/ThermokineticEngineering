package cd4017be.thermokin;

import cd4017be.lib.render.Util;
import net.minecraftforge.common.MinecraftForge;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerRenderers() {
		MinecraftForge.EVENT_BUS.register(Util.instance);//Frame counter needed for rendering multiblock structures
	}

	@Override
	public void init() {
		super.init();
	}

}
