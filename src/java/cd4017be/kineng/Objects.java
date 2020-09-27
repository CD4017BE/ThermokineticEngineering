package cd4017be.kineng;

import cd4017be.kineng.block.*;
import cd4017be.kineng.block.BlockShaft.ShaftMaterial;
import cd4017be.kineng.item.ItemBlockGear;
import cd4017be.kineng.tileentity.*;
import cd4017be.lib.block.OrientedBlock;
import cd4017be.lib.item.BaseItemBlock;
import cd4017be.lib.property.PropertyOrientation;
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
	public static ShaftMaterial M_IRON = new ShaftMaterial(Material.IRON, SoundType.METAL);
	public static ShaftMaterial M_BEDROCK = new ShaftMaterial(Material.ROCK, SoundType.STONE);

	// Creative Tabs
	public static TabMaterials tabKinetic = new TabMaterials(Main.ID);

	// Blocks
	public static final BlockShaft SHAFT_WOOD = null, SHAFT_IRON = null, SHAFT_DEBUG = null;
	public static final BlockGear GEAR_WOOD = null, GEAR_IRON = null;
	public static final BlockFillDirected FILL_DIR = null;
	public static final BlockFillShared FILL_SHARE = null;
	public static final OrientedBlock PROCESSING = null;
	public static final BlockRotaryTool GRINDSTONE = null, SAWBLADE = null;

	// ItemBlocks
	public static final BaseItemBlock shaft_wood = null, shaft_iron = null, shaft_debug = null;
	public static final ItemBlockGear gear_wood = null, gear_iron = null;
	public static final BaseItemBlock processing = null;
	public static final BaseItemBlock grindstone = null, sawblade = null;

	// Items

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
			new BlockGear("gear_wood", M_WOOD, Gear.class).setShape(0.25, 0.25).setHardness(0.5F),
			new BlockGear("gear_iron", M_IRON, Gear.class).setShape(0.25, 0.25).setHardness(1.5F),
			new BlockShaft("shaft_debug", M_BEDROCK, 0.25, MechanicalDebug.class).setBlockUnbreakable().setResistance(Float.POSITIVE_INFINITY),
			new BlockFillDirected("fill_dir", null).setHardness(1.0F),
			new BlockFillShared("fill_share", null).setHardness(1.0F),
			new OrientedBlock("processing", Material.ROCK, SoundType.STONE, 18, ProcessingBox.class, PropertyOrientation.XY_12_ROT),
			new BlockRotaryTool("grindstone", M_WOOD, IKineticLink.T_GRINDER, 1.0, RotaryTool.class).setShape(0.25, 0.625).setHardness(1.0F),
			new BlockRotaryTool("sawblade", M_WOOD, IKineticLink.T_SAWBLADE, 1.0, RotaryTool.class).setShape(0.25, 0.125).setHardness(1.0F)
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
			new BaseItemBlock(PROCESSING).setCreativeTab(tabKinetic),
			new BaseItemBlock(GRINDSTONE).setCreativeTab(tabKinetic),
			new BaseItemBlock(SAWBLADE).setCreativeTab(tabKinetic)
		);
	}

}
