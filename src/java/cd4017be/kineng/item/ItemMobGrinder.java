package cd4017be.kineng.item;

import static net.minecraft.init.Enchantments.*;
import cd4017be.lib.item.BaseItemBlock;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;


/** 
 * @author CD4017BE */
public class ItemMobGrinder extends BaseItemBlock {

	public ItemMobGrinder(Block id) {
		super(id);
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return true;
	}

	@Override
	public int getItemEnchantability() {
		return 1;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment ench) {
		return ench == FIRE_ASPECT || ench == SHARPNESS
			|| ench == LOOTING || ench == UNBREAKING;
	}

}
