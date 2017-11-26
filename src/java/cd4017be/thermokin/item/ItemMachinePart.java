package cd4017be.thermokin.item;

import cd4017be.lib.item.BaseItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.Constants;

public class ItemMachinePart extends BaseItem {

	public static int MAX_DUR = 250;

	public ItemMachinePart(String id) {
		super(id);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return stack.hasTagCompound() == stack.getTagCompound().hasKey("dur", Constants.NBT.TAG_BYTE);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return stack.hasTagCompound() ? 1.0 - (double)(stack.getTagCompound().getByte("dur") & 0xff) / (double)MAX_DUR : 0;
	}

}
