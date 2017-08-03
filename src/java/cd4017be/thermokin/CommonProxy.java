package cd4017be.thermokin;

import cd4017be.lib.TileBlockRegistry;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.thermokin.tileentity.*;
import static cd4017be.thermokin.Objects.*;

public class CommonProxy {

	public void registerRenderers() {}

	public void init() {
		TileBlockRegistry.register(shaft, Shaft.class, null);
		TileBlockRegistry.register(pneumaticPiston, PneumaticPiston.class, TileContainer.class);
		TileBlockRegistry.register(gasPipe, GasPipe.class, TileContainer.class);
		TileBlockRegistry.register(solidFuelHeater, SolidFuelHeater.class, TileContainer.class);
		TileBlockRegistry.register(airIntake, AirIntake.class, null);
		TileBlockRegistry.register(gasVent, GasVent.class, null);
		TileBlockRegistry.register(heatedFurnace, HeatedFurnace.class, TileContainer.class);
		TileBlockRegistry.register(evaporator, Evaporator.class, DataContainer.class);
		TileBlockRegistry.register(liqReservoir, LiquidReservoir.class, TileContainer.class);
		TileBlockRegistry.register(liqPipe, LiquidTube.class, null);
		TileBlockRegistry.register(liqPump, LiquidPump.class, DataContainer.class);
		TileBlockRegistry.register(crystallizer, Crystallizer.class, TileContainer.class);
		TileBlockRegistry.register(chimneyBase, Chimney.class, null);
		TileBlockRegistry.register(hydraulicPress, HydraulicPress.class, TileContainer.class);
	}

}
