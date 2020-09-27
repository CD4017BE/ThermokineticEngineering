package cd4017be.kineng.recipe;

import static net.minecraftforge.items.ItemHandlerHelper.canItemStacksStack;
import static net.minecraftforge.items.ItemHandlerHelper.copyStackWithSize;
import java.util.Arrays;
import cd4017be.kineng.physics.DynamicForce;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;


/** 
 * @author CD4017BE */
public class KineticProcess extends DynamicForce implements IItemHandler {

	public final ItemStack[] inv;
	public ProcessingRecipes recipes;
	public KineticRecipe rcp;
	/** [m] distance moved for current operation */
	public double s;
	public double v;
	public boolean working;

	public KineticProcess(int slots) {
		Arrays.fill(inv = new ItemStack[slots], ItemStack.EMPTY);
	}

	public KineticProcess setMode(ProcessingRecipes recipes) {
		this.recipes = recipes;
		this.rcp = null;
		this.v = 0;
		updateRecipe();
		return this;
	}

	public void stop() {
		rcp = null;
		updateState();
	}

	@Override
	public int getSlots() {
		return inv.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inv[slot];
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (slot != 0) return stack;
		ItemStack stack0 = inv[slot];
		int n = stack0.getCount();
		int m = Math.min(Math.min(getSlotLimit(slot), stack.getMaxStackSize()) - n, stack.getCount()); 
		if (m <= 0) return stack;
		if (n == 0) {
			if (!simulate) {
				inv[slot] = copyStackWithSize(stack, m);
				updateRecipe();
			}
		} else if (canItemStacksStack(stack0, stack)) {
			if (!simulate) {
				stack0.grow(m);
				if (!working) updateState();
			}
		} else return stack;
		return copyStackWithSize(stack, stack.getCount() - m);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (slot == 0 && rcp != null) return ItemStack.EMPTY;
		ItemStack stack0 = inv[slot];
		if (stack0.getCount() < amount) amount = stack0.getCount();
		if (amount <= 0) return ItemStack.EMPTY;
		if (!simulate) {
			if (amount == stack0.getCount()) inv[slot] = ItemStack.EMPTY;
			else stack0.shrink(amount);
			if (working ^ slot != 0) updateState();
		}
		return ItemHandlerHelper.copyStackWithSize(stack0, amount);
	}

	private void updateRecipe() {
		if (recipes != null && (rcp == null || !canItemStacksStack(rcp.io[0], inv[0]))) {
			KineticRecipe rcp1 = recipes.get(inv[0]);
			if (rcp1 != rcp) {
				rcp = rcp1 == null || rcp1.io.length > inv.length ? null : rcp1;
				s = 0; //reset progress because it's not compatible between recipes
			}
		}
		updateState();
	}

	private void updateState() {
		if (!(working = rcp != null && limit() > 0))
			Fdv = 0;
	}

	protected int limit() {
		int l = inv[0].getCount() / rcp.io[0].getCount(), n;
		for (int i = rcp.io.length - 1; i > 0; i--) {
			ItemStack stack = rcp.io[i];
			if ((n = stack.getCount()) == 0) continue;
			int m = inv[i].getCount();
			if (m > 0 && !canItemStacksStack(inv[i], stack)) return 0;
			l = Math.min((stack.getMaxStackSize() - m + n - 1) / n, l);
		}
		return l;
	}

	protected void output(int slot, ItemStack stack, int n) {
		if ((n *= stack.getCount()) == 0) return;
		ItemStack stack0 = inv[slot];
		if (stack0.isEmpty())
			(inv[slot] = stack.copy()).setCount(n);
		else stack0.grow(n);
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public void work(double dE, double ds, double v) {
		this.v = v;
		if (!working) return;
		if ((s -= dE / rcp.F) > rcp.s) {
			int l = limit();
			int n = (int)Math.floor(s / rcp.s);
			if (n >= l) {
				n = l;
				working = false;
			}
			s -= rcp.s * n;
			inv[0].shrink(n * rcp.io[0].getCount());
			for (int i = rcp.io.length - 1; i > 0; i--)
				output(i, rcp.io[i], n);
		}
		Fdv = working ? -rcp.F / (Math.abs(v) + 0.001) : 0;
	}

}
