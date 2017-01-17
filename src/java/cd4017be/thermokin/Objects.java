package cd4017be.thermokin;

import cd4017be.api.Capabilities.EmptyCallable;
import cd4017be.api.Capabilities.EmptyStorage;
import cd4017be.lib.DefaultBlock;
import cd4017be.lib.DefaultItemBlock;
import cd4017be.lib.TileBlock;
import cd4017be.lib.templates.BlockPipe;
import cd4017be.thermokin.block.*;
import cd4017be.thermokin.item.*;
import cd4017be.thermokin.multiblock.GasContainer;
import cd4017be.thermokin.multiblock.IHeatReservoir;
import cd4017be.thermokin.multiblock.LiquidComponent;
import cd4017be.thermokin.multiblock.ShaftComponent;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class Objects {

	public static CreativeTabThermokin tabThermokin;

	public static ItemRotationSensor rotationSensor;
	public static ItemThermometer thermometer;
	public static ItemManometer manometer;

	public static BlockShaft shaft;
	public static DefaultBlock thermIns;//TODO also add a heat sink block
	public static TileBlock pneumaticPiston;
	public static BlockPipe gasPipe;
	public static TileBlock solidFuelHeater;
	public static TileBlock gasVent;
	public static TileBlock heatedFurnace;
	public static TileBlock evaporator;
	public static TileBlock liqReservoir;
	public static TileBlock liqPump;
	public static BlockPipe liqPipe;
	public static TileBlock crystallizer;

	@CapabilityInject(ShaftComponent.class)
	public static Capability<ShaftComponent> SHAFT_CAP;
	@CapabilityInject(GasContainer.class)
	public static Capability<GasContainer> GAS_CAP;
	@CapabilityInject(LiquidComponent.class)
	public static Capability<LiquidComponent> LIQUID_CAP;
	@CapabilityInject(IHeatReservoir.class)
	public static Capability<IHeatReservoir> HEAT_CAP;

	public static void init() {
		tabThermokin = new CreativeTabThermokin("thermokin");
		
		rotationSensor = new ItemRotationSensor("rotationSensor");
		thermometer = new ItemThermometer("thermometer");
		manometer = new ItemManometer("manometer");
		
		new DefaultItemBlock((shaft = new BlockShaft("shaft", Material.IRON, 0x20)).setCreativeTab(tabThermokin).setHardness(2.0F).setResistance(10F));
		new DefaultItemBlock((thermIns = new DefaultBlock("thermIns", Material.ROCK)).setCreativeTab(tabThermokin).setHardness(1.5F).setResistance(10F));//TODO define as IHeatBlock
		new DefaultItemBlock((pneumaticPiston = TileBlock.create("pneumaticPiston", Material.IRON, SoundType.METAL, 0x22)).setCreativeTab(tabThermokin).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((gasPipe = new BlockPipe("gasPipe", Material.IRON, SoundType.METAL, 0x20)).setCreativeTab(tabThermokin).setHardness(1.0F).setResistance(20F));
		new DefaultItemBlock((solidFuelHeater = TileBlock.create("solidFuelHeater", Material.ROCK, SoundType.STONE, 1)).setCreativeTab(tabThermokin).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((gasVent = TileBlock.create("gasVent", Material.GLASS, SoundType.GLASS, 0x22)).setCreativeTab(tabThermokin).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((heatedFurnace = TileBlock.create("heatedFurnace", Material.ROCK, SoundType.STONE, 1)).setCreativeTab(tabThermokin).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((evaporator = TileBlock.create("evaporator", Material.IRON, SoundType.METAL, 1)).setCreativeTab(tabThermokin).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((liqReservoir = TileBlock.create("liqReservoir", Material.IRON, SoundType.METAL, 0)).setCreativeTab(tabThermokin).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((liqPump = TileBlock.create("liqPump", Material.IRON, SoundType.METAL, 0x22)).setCreativeTab(tabThermokin).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((liqPipe = new BlockPipe("liqPipe", Material.IRON, SoundType.METAL, 0x20)).setCreativeTab(tabThermokin).setHardness(1.0F).setResistance(20F));
		gasPipe.size = 0.5F;
		liqPipe.size = 0.25F;
		
		CapabilityManager.INSTANCE.register(ShaftComponent.class, new EmptyStorage<ShaftComponent>(), new EmptyCallable<ShaftComponent>());
		CapabilityManager.INSTANCE.register(GasContainer.class, new EmptyStorage<GasContainer>(), new EmptyCallable<GasContainer>());
		CapabilityManager.INSTANCE.register(LiquidComponent.class, new EmptyStorage<LiquidComponent>(), new EmptyCallable<LiquidComponent>());
		CapabilityManager.INSTANCE.register(IHeatReservoir.class, new EmptyStorage<IHeatReservoir>(), new EmptyCallable<IHeatReservoir>());
	}

}
