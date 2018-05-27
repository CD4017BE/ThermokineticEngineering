package cd4017be.thermokin;

import cd4017be.lib.block.AdvancedBlock;
import cd4017be.lib.item.BaseItemBlock;
import cd4017be.lib.templates.TabMaterials;
import cd4017be.lib.util.TooltipUtil;
import cd4017be.thermokin.block.BlockModularMachine;
import cd4017be.thermokin.item.ItemCasing;
import cd4017be.thermokin.item.ItemPart;
import cd4017be.thermokin.item.ItemModularMachine;
import cd4017be.thermokin.item.ItemWrench;
import cd4017be.thermokin.module.Layout;
import cd4017be.thermokin.tileentity.Assembler;
import cd4017be.thermokin.tileentity.SolidFuelOven;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

/**
 * 
 * @author CD4017BE
 */
@EventBusSubscriber(modid = Main.ID)
@ObjectHolder(value = Main.ID)
public class Objects {

	public static TabMaterials tabThermokin = new TabMaterials(Main.ID);

	//Machine Layouts
	public static Layout ovenL;

	//Blocks
	public static final AdvancedBlock ASSEMBLER = null;
	public static final BlockModularMachine OVEN = null;

	//ItemBlocks
	public static final BaseItemBlock assembler = null;
	public static final ItemModularMachine oven = null;

	//Items
	public static final ItemWrench wrench = null;
	public static final ItemCasing casing = null;
	public static final ItemPart inv_io = null, inv_acc = null, inv_buff_s = null, inv_buff_m = null;

	static void init() {
		tabThermokin.item = new ItemStack(Blocks.PISTON);
		ovenL = new Layout(new ItemStack(oven)).inv(1,0,0);
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> ev) {
		TooltipUtil.CURRENT_DOMAIN = Main.ID;
		ev.getRegistry().registerAll(
			new AdvancedBlock("assembler", Material.WOOD, SoundType.WOOD, 0, Assembler.class).setCreativeTab(tabThermokin),
			new BlockModularMachine("oven", Material.ROCK, SoundType.STONE, 0, SolidFuelOven.class).setCreativeTab(tabThermokin)
		);
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> ev) {
		TooltipUtil.CURRENT_DOMAIN = Main.ID;
		ev.getRegistry().registerAll(
			new BaseItemBlock(ASSEMBLER),
			new ItemModularMachine(OVEN),
			new ItemWrench("wrench").setCreativeTab(tabThermokin),
			new ItemCasing("casing"),
			new ItemPart("inv_io"), new ItemPart("inv_acc"), new ItemPart("inv_buff_s"), new ItemPart("inv_buff_m")
		);
	}

}
