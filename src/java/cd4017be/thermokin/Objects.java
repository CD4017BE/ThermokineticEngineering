package cd4017be.thermokin;

import cd4017be.lib.block.AdvancedBlock;
import cd4017be.lib.item.BaseItemBlock;
import cd4017be.lib.templates.TabMaterials;
import cd4017be.lib.util.TooltipUtil;
import cd4017be.thermokin.tileentity.Assembler;
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

	//Blocks
	public static final AdvancedBlock ASSEMBLER = null;

	//ItemBlocks
	public static final BaseItemBlock assembler = null;

	//Items

	static void init() {
		tabThermokin.item = new ItemStack(Blocks.PISTON);
	}

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> ev) {
		TooltipUtil.CURRENT_DOMAIN = Main.ID;
		ev.getRegistry().registerAll(
			new AdvancedBlock("assembler", Material.WOOD, SoundType.WOOD, 0, Assembler.class).setCreativeTab(tabThermokin)
		);
	}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> ev) {
		TooltipUtil.CURRENT_DOMAIN = Main.ID;
		ev.getRegistry().registerAll(
			new BaseItemBlock(ASSEMBLER)
		);
	}

}
