package cd4017be.kineng.recipe;

import static net.minecraftforge.items.ItemHandlerHelper.canItemStacksStack;
import static net.minecraftforge.items.ItemHandlerHelper.copyStackWithSize;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;


/** 
 * @author CD4017BE */
public interface IBasicItemHandler extends IItemHandlerModifiable {

	@Override
	default ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		ItemStack stack0 = getStackInSlot(slot);
		int n = stack0.getCount();
		int m = Math.min(Math.min(getSlotLimit(slot), stack.getMaxStackSize()) - n, stack.getCount()); 
		if (m <= 0) return stack;
		if (n == 0) {
			if (!simulate) setStackInSlot(slot, copyStackWithSize(stack, m));
		} else if (canItemStacksStack(stack0, stack)) {
			if (!simulate) {
				stack0.grow(m);
				setStackInSlot(slot, stack0);
			}
		} else return stack;
		return copyStackWithSize(stack, stack.getCount() - m);
	}

	@Override
	default ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack stack0 = getStackInSlot(slot);
		if (stack0.getCount() < amount) amount = stack0.getCount();
		if (amount <= 0) return ItemStack.EMPTY;
		if (!simulate) {
			if (amount == stack0.getCount()) setStackInSlot(slot, ItemStack.EMPTY);
			else {
				stack0.shrink(amount);
				setStackInSlot(slot, stack0);
			}
		}
		return ItemHandlerHelper.copyStackWithSize(stack0, amount);
	}

}
