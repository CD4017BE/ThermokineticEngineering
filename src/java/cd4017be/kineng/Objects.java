package cd4017be.kineng;

import static cd4017be.kineng.tileentity.IKineticLink.*;
import static cd4017be.lib.property.PropertyOrientation.ALL_AXIS;
import static cd4017be.lib.property.PropertyOrientation.HOR_AXIS;
import cd4017be.kineng.block.*;
import cd4017be.kineng.block.BlockShaft.ShaftMaterial;
import cd4017be.kineng.item.*;
import cd4017be.kineng.tileentity.*;
import cd4017be.lib.block.AdvancedBlock;
import cd4017be.lib.block.OrientedBlock;
import cd4017be.lib.item.BaseItemBlock;
import cd4017be.lib.templates.TabMaterials;
import cd4017be.lib.util.TooltipUtil;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

/** @author CD4017BE */
@EventBusSubscriber(modid = Main.ID)
@ObjectHolder(value = Main.ID)
public class Objects {

	public static ShaftMaterial M_WOOD = new ShaftMaterial(Material.WOOD, SoundType.WOOD);
	public static ShaftMaterial M_STONE = new ShaftMaterial(Material.ROCK, SoundType.STONE);
	public static ShaftMaterial M_IRON = new ShaftMaterial(Material.IRON, SoundType.METAL);
	public static ShaftMaterial M_BEDROCK = new ShaftMaterial(Material.ROCK, SoundType.STONE);

	// Creative Tabs
	public static TabMaterials tabKinetic = new TabMaterials(Main.ID);

	// Blocks
	public static final BlockShaft SHAFT_WOOD = null, SHAFT_IRON = null, SHAFT_DEBUG = null;
	public static final BlockGear GEAR_WOOD = null, GEAR_IRON = null;
	public static final BlockFillDirected FILL_DIR = null;
	public static final BlockFillShared FILL_SHARE = null;
	public static final BlockProcessing PROCESSING = null, RF_COIL = null;
	public static final BlockRotaryTool GRINDSTONE = null, SAWBLADE = null, MAGNETS = null;
	public static final BlockRotaryTool LATHE = null, PRESS = null, SHAFT_MAN = null;
	public static final AdvancedBlock LAKE = null;
	public static final OrientedBlock LAKE_VALVE = null, LAKE_GATE = null;
	public static final BlockRotaryTool WATER_WHEEL = null;
	public static final BlockTurbine WIND_MILL = null;
	public static final BlockShaft TACHOMETER = null, TRANSDUCER = null;

	// ItemBlocks
	public static final BaseItemBlock shaft_wood = null, shaft_iron = null, shaft_debug = null;
	public static final ItemBlockGear gear_wood = null, gear_iron = null;
	public static final BaseItemBlock processing = null, rf_coil = null;
	public static final BaseItemBlock grindstone = null, sawblade = null, magnets = null;
	public static final BaseItemBlock lathe = null, press = null, shaft_man = null;
	public static final BaseItemBlock lake = null, lake_valve = null, lake_gate = null;
	public static final ItemBlockGear water_wheel = null;
	public static final ItemBlockGear wind_mill = null;
	public static final BaseItemBlock tachometer = null, transducer = null;

	// Items
	public static final ItemBreakRecipe flint_knife = null;
	public static final ItemAerometer anemometer = null;
	public static final ItemChain chain = null;

	// Sounds

	public static void init() {
		tabKinetic.item = new ItemStack(gear_wood, 1, 1);
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> ev) {
		TooltipUtil.CURRENT_DOMAIN = Main.ID;
		ev.getRegistry().registerAll(
			new BlockShaft("shaft_wood", M_WOOD, 0.25, ShaftPart.class).setHardness(0.5F),
			new BlockShaft("shaft_iron", M_IRON, 0.25, ShaftPart.class).setHardness(1.5F),
			new BlockGear("gear_wood", M_WOOD, 0.5, Gear.class).setShape(0.25, 0.25).setHardness(0.5F),
			new BlockGear("gear_iron", M_IRON, 0.5, Gear.class).setShape(0.25, 0.25).setHardness(1.5F),
			new BlockShaft("shaft_debug", M_BEDROCK, 0.25, MechanicalDebug.class).setBlockUnbreakable().setResistance(Float.POSITIVE_INFINITY),
			new BlockRotaryTool("shaft_man", M_WOOD, -1, 1.0, ManualPower.class).setShape(0.25, 0.125).setHardness(0.5F),
			new BlockFillDirected("fill_dir", null).setHardness(1.0F),
			new BlockFillShared("fill_share", null).setHardness(1.0F),
			new BlockProcessing("processing", Material.ROCK, SoundType.STONE, 18, ProcessingBox.class).addRcp(0, T_GRINDER, T_SAWBLADE).setLightOpacity(0),
			new BlockProcessing("rf_coil", Material.IRON, SoundType.METAL, 18, FluxCoil.class).setLightOpacity(0),
			new BlockRotaryTool("grindstone", M_WOOD, T_GRINDER, 1.0, RotaryTool.class).setShape(0.25, 0.625).setHardness(1.0F),
			new BlockRotaryTool("sawblade", M_WOOD, T_SAWBLADE, 1.0, RotaryTool.class).setShape(0.25, 0.125).setHardness(1.0F),
			new BlockRotaryTool("lathe", M_WOOD, T_ANGULAR, 0.5, ProcessingShaft.class).setShape(0.25, 0.5).setHardness(1.0F),
			new BlockRotaryTool("press", M_STONE, T_BELT, 0.25, ProcessingShaft.class).setHardness(1.0F),
			new BlockRotaryTool("magnets", M_IRON, T_MAGNETIC, 0.5, RotaryTool.class).setShape(0.25, 0.625).setHardness(1.5F),
			new AdvancedBlock("lake", Material.ROCK, SoundType.STONE, 0, StorageLake.class),
			new OrientedBlock("lake_valve", Material.IRON, SoundType.METAL, 0, LakeValve.class, ALL_AXIS),
			new OrientedBlock("lake_gate", Material.WOOD, SoundType.WOOD, 0, LakeGate.class, HOR_AXIS),
			new BlockRotaryTool("water_wheel", M_WOOD, T_ANGULAR, 2.5, WaterWheel.class).setShape(0.25, 1.0).setHardness(1.0F),
			new BlockTurbine("wind_mill", M_IRON, 2.5, WindTurbine.class).setHardness(1.0F),
			new BlockShaft("tachometer", M_WOOD, 0.25, Tachometer.class).setHardness(1.0F),
			new BlockShaft("transducer", M_IRON, 0.25, TorqueTransducer.class).setHardness(1.0F)
		);
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> ev) {
		TooltipUtil.CURRENT_DOMAIN = Main.ID;
		ev.getRegistry().registerAll(
			new BaseItemBlock(SHAFT_WOOD).setCreativeTab(tabKinetic),
			new BaseItemBlock(SHAFT_IRON).setCreativeTab(tabKinetic),
			new ItemBlockGear(GEAR_WOOD).setCreativeTab(tabKinetic),
			new ItemBlockGear(GEAR_IRON).setCreativeTab(tabKinetic),
			new BaseItemBlock(SHAFT_DEBUG).setCreativeTab(tabKinetic),
			new BaseItemBlock(SHAFT_MAN).setCreativeTab(tabKinetic),
			new BaseItemBlock(PROCESSING).setCreativeTab(tabKinetic),
			new BaseItemBlock(RF_COIL).setCreativeTab(tabKinetic),
			new BaseItemBlock(GRINDSTONE).setCreativeTab(tabKinetic),
			new BaseItemBlock(SAWBLADE).setCreativeTab(tabKinetic),
			new BaseItemBlock(LATHE).setCreativeTab(tabKinetic),
			new BaseItemBlock(PRESS).setCreativeTab(tabKinetic),
			new BaseItemBlock(MAGNETS).setCreativeTab(tabKinetic),
			new BaseItemBlock(LAKE).setCreativeTab(tabKinetic),
			new BaseItemBlock(LAKE_VALVE).setCreativeTab(tabKinetic),
			new BaseItemBlock(LAKE_GATE).setCreativeTab(tabKinetic),
			new ItemBlockGear(WATER_WHEEL, 5).setCreativeTab(tabKinetic),
			new ItemBlockGear(WIND_MILL).setCreativeTab(tabKinetic),
			new BaseItemBlock(TACHOMETER).setCreativeTab(tabKinetic),
			new BaseItemBlock(TRANSDUCER).setCreativeTab(tabKinetic),
			new ItemBreakRecipe("flint_knife").setMaxDamage(32).setCreativeTab(tabKinetic),
			new ItemAerometer("anemometer").setCreativeTab(tabKinetic),
			new ItemChain("chain").setCreativeTab(tabKinetic)
		);
	}

}
