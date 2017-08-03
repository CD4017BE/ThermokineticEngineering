package cd4017be.thermokin;

import static cd4017be.thermokin.Objects.*;
import cd4017be.thermokin.gui.*;
import cd4017be.thermokin.recipe.ShaftMounts;
import cd4017be.thermokin.render.PistonRenderer;
import cd4017be.thermokin.render.ShaftRenderer;
import cd4017be.thermokin.tileentity.PneumaticPiston;
import cd4017be.thermokin.tileentity.Shaft;
import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.TileBlockRegistry;
import cd4017be.lib.render.ModelPipe;
import cd4017be.lib.render.SpecialModelLoader;
import cd4017be.lib.render.Util;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy extends CommonProxy {

	@Override
	public void registerRenderers() {
		MinecraftForge.EVENT_BUS.register(Util.instance);//Frame counter needed for rendering multiblock structures
		//BlockItems
		BlockItemRegistry.registerRender(shaft);
		BlockItemRegistry.registerRender(thermIns);
		BlockItemRegistry.registerRender(pneumaticPiston);
		BlockItemRegistry.registerRender(gasPipe);
		BlockItemRegistry.registerRender(solidFuelHeater);
		BlockItemRegistry.registerRender(airIntake);
		BlockItemRegistry.registerRender(gasVent);
		BlockItemRegistry.registerRender(heatedFurnace);
		BlockItemRegistry.registerRender(heatSink);
		BlockItemRegistry.registerRender(liqPump);
		BlockItemRegistry.registerRender(liqPipe);
		BlockItemRegistry.registerRender(crystallizer);
		BlockItemRegistry.registerRender(evaporator);
		BlockItemRegistry.registerRender(liqReservoir);
		BlockItemRegistry.registerRender(chimney);
		BlockItemRegistry.registerRender(chimneyBase);
		BlockItemRegistry.registerRender(hydraulicPress);
		//Items
		BlockItemRegistry.registerRender(rotationSensor);
		BlockItemRegistry.registerRender(thermometer);
		BlockItemRegistry.registerRender(manometer);
		//Tiles
		ClientRegistry.bindTileEntitySpecialRenderer(Shaft.class, new ShaftRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(PneumaticPiston.class, new PistonRenderer());
		SpecialModelLoader.registerTESRModel("thermokin:models/tileentity/shaft");
		SpecialModelLoader.registerTESRModel("thermokin:models/tileentity/shaftMass");
		for (ShaftMounts.Entry e : ShaftMounts.shaftMounts.values())
			SpecialModelLoader.registerTESRModel(e.model);
		SpecialModelLoader.registerTESRModel(PistonRenderer.model);
	
	}

	@Override
	public void init() {
		super.init();
		TileBlockRegistry.registerGui(pneumaticPiston, GuiPneumaticPiston.class);
		TileBlockRegistry.registerGui(gasPipe, GuiGasPipe.class);
		TileBlockRegistry.registerGui(solidFuelHeater, GuiSolidFuelHeater.class);
		TileBlockRegistry.registerGui(heatedFurnace, GuiHeatedFurnace.class);
		TileBlockRegistry.registerGui(evaporator, GuiEvaporator.class);
		TileBlockRegistry.registerGui(liqReservoir, GuiLiquidReservoir.class);
		TileBlockRegistry.registerGui(crystallizer, GuiCrystallizer.class);
		TileBlockRegistry.registerGui(liqPump, GuiLiquidPump.class);
		TileBlockRegistry.registerGui(hydraulicPress, GuiHydraulicPress.class);
		//set block transparencies
		Objects.pneumaticPiston.setBlockLayer(BlockRenderLayer.CUTOUT);
		//pipe models
		SpecialModelLoader.setMod("thermokin");
		SpecialModelLoader.registerBlockModel(Objects.shaft, new ModelPipe("thermokin:shaft", 0, 1));
		SpecialModelLoader.registerBlockModel(Objects.gasPipe, new ModelPipe("thermokin:gasPipe", 1, 1));
		SpecialModelLoader.registerBlockModel(Objects.liqPipe, new ModelPipe("thermokin:liqPipe", 1, 1));
	}

}
