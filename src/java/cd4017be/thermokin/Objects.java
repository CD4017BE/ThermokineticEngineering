package cd4017be.thermokin;

import org.apache.logging.log4j.Level;

import cd4017be.api.Capabilities.EmptyCallable;
import cd4017be.api.Capabilities.EmptyStorage;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
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
import cd4017be.thermokin.physics.Substance;
import cd4017be.thermokin.tileentity.AirIntake;
import cd4017be.thermokin.tileentity.Crystallizer;
import cd4017be.thermokin.tileentity.Evaporator;
import cd4017be.thermokin.tileentity.GasPipe;
import cd4017be.thermokin.tileentity.HeatedFurnace;
import cd4017be.thermokin.tileentity.LiquidPump;
import cd4017be.thermokin.tileentity.LiquidReservoir;
import cd4017be.thermokin.tileentity.PneumaticPiston;
import cd4017be.thermokin.tileentity.Shaft;
import cd4017be.thermokin.tileentity.SolidFuelHeater;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class Objects {

	public static CreativeTabThermokin tabThermokin;

	public static ItemRotationSensor rotationSensor;
	public static ItemThermometer thermometer;
	public static ItemManometer manometer;

	public static DefaultBlock thermIns;
	public static DefaultBlock heatSink;
	public static BlockShaft shaft;
	public static TileBlock pneumaticPiston;
	public static BlockPipe gasPipe;
	public static TileBlock solidFuelHeater;
	public static TileBlock gasVent;
	public static TileBlock airIntake;
	public static TileBlock heatedFurnace;
	public static TileBlock evaporator;
	public static TileBlock liqReservoir;
	public static TileBlock liqPump;
	public static BlockPipe liqPipe;
	public static TileBlock crystallizer;
	public static TileBlock chimney;
	public static TileBlock chimneyBase;

	public static Substance combustionWaste;

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
		
		new DefaultItemBlock((thermIns = new DefaultBlock("thermIns", Material.ROCK)).setCreativeTab(tabThermokin).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((heatSink = TileBlock.create("heatSink", Material.IRON, SoundType.METAL, 0x22)).setCreativeTab(tabThermokin).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((shaft = new BlockShaft("shaft", Material.IRON, 0x20)).setCreativeTab(tabThermokin).setHardness(2.0F).setResistance(10F));
		new DefaultItemBlock((pneumaticPiston = TileBlock.create("pneumaticPiston", Material.IRON, SoundType.METAL, 0x22)).setCreativeTab(tabThermokin).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((gasPipe = new BlockPipe("gasPipe", Material.IRON, SoundType.METAL, 0x20)).setCreativeTab(tabThermokin).setHardness(1.0F).setResistance(20F));
		new DefaultItemBlock((solidFuelHeater = TileBlock.create("solidFuelHeater", Material.ROCK, SoundType.STONE, 1)).setCreativeTab(tabThermokin).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((airIntake = TileBlock.create("airIntake", Material.IRON, SoundType.METAL, 0x22).setBlockBounds(new AxisAlignedBB(0.0, 0.0, 0, 1.0, 1.0, 0.5))).setCreativeTab(tabThermokin).setHardness(1.0F).setResistance(10F));
		new DefaultItemBlock((gasVent = TileBlock.create("gasVent", Material.GLASS, SoundType.GLASS, 0x22).setBlockBounds(new AxisAlignedBB(0.25, 0.25, 0, 0.75, 0.75, 0.125))).setCreativeTab(tabThermokin).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((heatedFurnace = TileBlock.create("heatedFurnace", Material.ROCK, SoundType.STONE, 1)).setCreativeTab(tabThermokin).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((evaporator = TileBlock.create("evaporator", Material.IRON, SoundType.METAL, 1)).setCreativeTab(tabThermokin).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((liqReservoir = TileBlock.create("liqReservoir", Material.IRON, SoundType.METAL, 0)).setCreativeTab(tabThermokin).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((liqPump = TileBlock.create("liqPump", Material.IRON, SoundType.METAL, 0x22)).setCreativeTab(tabThermokin).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((liqPipe = new BlockPipe("liqPipe", Material.IRON, SoundType.METAL, 0x20)).setCreativeTab(tabThermokin).setHardness(1.0F).setResistance(20F));
		new DefaultItemBlock((crystallizer = TileBlock.create("crystallizer", Material.ROCK, SoundType.STONE, 0x20)).setCreativeTab(tabThermokin).setHardness(1.5F).setResistance(10F));
		new DefaultItemBlock((chimney = new BlockChimney("chimney", Material.ROCK, SoundType.STONE, 0x20)).setCreativeTab(tabThermokin).setHardness(2.0F).setResistance(10F));
		new DefaultItemBlock((chimneyBase = new BlockChimney("chimneyBase", Material.ROCK, SoundType.STONE, 0x20)).setCreativeTab(tabThermokin).setHardness(2.0F).setResistance(10F));
		gasPipe.size = 0.5F;
		liqPipe.size = 0.25F;
		
		CapabilityManager.INSTANCE.register(ShaftComponent.class, new EmptyStorage<ShaftComponent>(), new EmptyCallable<ShaftComponent>());
		CapabilityManager.INSTANCE.register(GasContainer.class, new EmptyStorage<GasContainer>(), new EmptyCallable<GasContainer>());
		CapabilityManager.INSTANCE.register(LiquidComponent.class, new EmptyStorage<LiquidComponent>(), new EmptyCallable<LiquidComponent>());
		CapabilityManager.INSTANCE.register(IHeatReservoir.class, new EmptyStorage<IHeatReservoir>(), new EmptyCallable<IHeatReservoir>());
	}

	public static void initConstants(ConfigConstants cfg) {
		Crystallizer.C0 = (float)cfg.getNumber("crystallizer_C", 5000F);
		Crystallizer.R0 = (float)cfg.getNumber("crystallizer_R", 0.004F);
		Crystallizer.SizeL = cfg.getNumber("crystallizer_Vl", 1.0);
		Crystallizer.SizeG = cfg.getNumber("crystallizer_Vg", 4.0);
		Evaporator.C0 = (float)cfg.getNumber("evaporator_C", 5000F);
		Evaporator.R0 = (float)cfg.getNumber("evaporator_R", 0.004F);
		Evaporator.SizeL = cfg.getNumber("evaporator_Vl", 0.8);
		Evaporator.SizeG = cfg.getNumber("evaporator_Vg", 1.0);
		HeatedFurnace.C0 = (float)cfg.getNumber("hFurnace_C", 10000F);
		HeatedFurnace.R0 = (float)cfg.getNumber("hFurnace_R", 0.004F);
		HeatedFurnace.Energy = (float)cfg.getNumber("hFurnace_Ew", 250000F);
		HeatedFurnace.NeededTemp = (float)cfg.getNumber("hFurnace_Tw", 1200F);
		HeatedFurnace.TRwork = (float)cfg.getNumber("hFurnace_Rw", 20F);
		LiquidReservoir.C0 = (float)cfg.getNumber("liqReservoir_C", 5000F);
		LiquidReservoir.R0 = (float)cfg.getNumber("liqReservoir_R", 0.004F);
		LiquidReservoir.SizeL = cfg.getNumber("liqReservoir_Vl", 1.0);
		LiquidReservoir.SizeG = cfg.getNumber("liqReservoir_Vg", 4.0);
		LiquidReservoir.P0 = cfg.getNumber("liqReservoir_P0", 101300);
		SolidFuelHeater.C0 = (float)cfg.getNumber("sfHeater_C", 10000);
		SolidFuelHeater.R0 = (float)cfg.getNumber("sfHeater_R", 0.004);
		SolidFuelHeater.Size = cfg.getNumber("sfHeater_Vg", 1.0);
		SolidFuelHeater.BurnEnergy = cfg.getNumber("sfHeater_E", 17280); //693molR/kg  12MJ/kg
		SolidFuelHeater.IgnitionTemp = (float)cfg.getNumber("sfHeater_T0", 1000);
		SolidFuelHeater.FlameOutReact = cfg.getNumber("sfHeater_flameOut", 1.0) / SolidFuelHeater.BurnEnergy;
		SolidFuelHeater.ReactMult = cfg.getNumber("sfHeater_speed", 0.0005);
		SolidFuelHeater.FuelDensity = (float)cfg.getNumber("fuelDensity", 10);
		SolidFuelHeater.MinFuel = SolidFuelHeater.FuelDensity * 10F;
		GasPipe.size = cfg.getNumber("gasPipe_Vg", 0.25);
		AirIntake.size = cfg.getNumber("gasVent_Vg", 5F);
		LiquidPump.Amin = (float)cfg.getNumber("liqPump_Amin", 0.0001F);
		LiquidPump.Amax = (float)cfg.getNumber("liqPump_Amax", 0.01F);
		PneumaticPiston.Amin = (float)cfg.getNumber("piston_Amin", 0.001F);
		PneumaticPiston.Amax = (float)cfg.getNumber("piston_Amax", 0.1F);
		Shaft.M0 = (float)cfg.getNumber("shaft_mass", 1000F);
		combustionWaste = Substance.REGISTRY.getObject(new ResourceLocation(cfg.get("subst_combWaste", String.class, "thermokin:combWaste")));
		if (combustionWaste == null) {
			FMLLog.log("thermokin", Level.ERROR, "The substance set as combustion waste does'nt exist! Please fix your config!");
			GameRegistry.register(combustionWaste = new Substance("combWaste").setRegistryName("thermokin:combWaste"));
		}
	}

}
