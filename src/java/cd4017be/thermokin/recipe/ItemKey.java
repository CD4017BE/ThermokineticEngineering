package cd4017be.thermokin.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import cd4017be.lib.util.OreDictStack;

public class ItemKey {

	private final ItemStack item;
	private final OreDictStack ore;

	public ItemKey(Object obj) {
		if (obj instanceof ItemStack) {this.item = (ItemStack)obj; this.ore = null;}
		else if (obj instanceof OreDictStack) {this.ore = (OreDictStack)obj; this.item = null;}
		else {this.item = null; this.ore = null;}
	}
	public ItemKey() { this.item = null; this.ore = null; }
	public ItemKey(ItemStack item) { this.item = item; this.ore = null; }
	public ItemKey(OreDictStack ore) { this.item = null; this.ore = ore; }

	@Override
	public int hashCode() {
		return item != null ? item.getItem().hashCode() + item.getItemDamage() * 31 : ore != null ? ore.ID : 0;
	}

	/**
	 * Warning, this implementation violates the general contract of equals in case the argument is not an ItemKey: <br>
	 * If the argument is an instance of ItemKey it will return true if both have <b>ore</b> with equal OreIds <br> or both have <b>item</b> with equal Item and Damage <br> ore both have neither. <br>
	 * If the argument is an instance of ItemStack it will return true if it has the OreDictionary tag of <b>ore</b> <br> or matches with <b>item</b> in Item and Damage. <br>
	 * If the argument is null it will return true if both <b>item</b> and <b>ore</b> are null.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ItemKey) {
			ItemKey k = (ItemKey)obj;
			if (item != null) return item.isItemEqual(k.item);
			if (ore != null) return k.ore != null && ore.ID == k.ore.ID;
			return k.item == null && k.ore == null;
		} else if (obj instanceof ItemStack) {
			ItemStack stack = (ItemStack)obj;
			if (ore != null) return ore.equals(stack);
			else return stack.isItemEqual(item);
		} else return obj == null && ore == null && item == null;
	}

	public ItemKey copy() {
		return ore != null ? new ItemKey(ore.copy()) : item != null ? new ItemKey(item.copy()) : new ItemKey();
	}

	public int stacksize() {
		return ore != null ? ore.stacksize : item != null ? item.stackSize : 0;
	}

	public ItemStack asItem() {
		if (item != null) return item.copy();
		if (ore != null) return ore.asItem();
		return null;
	}

	public void writeToNBT(NBTTagCompound nbt, String k) {
		if (ore != null) nbt.setString(k, ore.stacksize + "*" + ore.id);
		else if (item != null) nbt.setTag(k, item.writeToNBT(new NBTTagCompound()));
		else nbt.removeTag(k);
	}

	public static ItemKey readFromNBT(NBTTagCompound nbt, String k) {
		NBTBase tag = nbt.getTag(k);
		if (tag instanceof NBTTagString) return new ItemKey(OreDictStack.deserialize(((NBTTagString)tag).getString()));
		if (tag instanceof NBTTagCompound) return new ItemKey(ItemStack.loadItemStackFromNBT((NBTTagCompound)tag));
		return new ItemKey();
	}

}
