package cd4017be.kineng;

import cd4017be.kineng.block.*;
import cd4017be.kineng.block.BlockShaft.ShaftMaterial;
import cd4017be.kineng.item.ItemBlockGear;
import cd4017be.kineng.tileentity.*;
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
	public static ShaftMaterial M_IRON = new ShaftMaterial(Material.IRON, SoundType.METAL);
	public static ShaftMaterial M_BEDROCK = new ShaftMaterial(Material.ROCK, SoundType.STONE);

	// Creative Tabs
	public static TabMaterials tabKinetic = new TabMaterials(Main.ID);

	// Blocks
	public static final BlockShaft SHAFT_WOOD = null;
	public static final BlockGear GEAR_WOOD = null;
	public static final BlockShaft SHAFT_DEBUG = null;
	public static final BlockFillDirected FILL_DIR = null;
	public static final BlockFillShared FILL_SHARE = null;

	// ItemBlocks
	public static final BaseItemBlock shaft_wood = null;
	public static final ItemBlockGear gear_wood = null;
	public static final BaseItemBlock shaft_debug = null;

	// Items

	// Sounds

	public static void init() {
		tabKinetic.item = new ItemStack(gear_wood, 1, 1);
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> ev) {
		TooltipUtil.CURRENT_DOMAIN = Main.ID;
		ev.getRegistry().registerAll(
			new BlockShaft("shaft_wood", M_WOOD, 0.25, ShaftPart.class).setCreativeTab(tabKinetic).setLightOpacity(0).setHardness(0.5F),
			new BlockGear("gear_wood", M_WOOD, Gear.class).setCreativeTab(tabKinetic).setLightOpacity(0).setHardness(0.5F),
			new BlockShaft("shaft_debug", M_BEDROCK, 0.25, MechanicalDebug.class).setCreativeTab(tabKinetic).setLightOpacity(0).setBlockUnbreakable().setResistance(Float.POSITIVE_INFINITY),
			new BlockFillDirected("fill_dir", null).setLightOpacity(0).setHardness(0.5F),
			new BlockFillShared("fill_share", null).setLightOpacity(0).setHardness(0.5F)
		);
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> ev) {
		TooltipUtil.CURRENT_DOMAIN = Main.ID;
		ev.getRegistry().registerAll(
			new BaseItemBlock(SHAFT_WOOD),
			new ItemBlockGear(GEAR_WOOD),
			new BaseItemBlock(SHAFT_DEBUG)
		);
	}

}
