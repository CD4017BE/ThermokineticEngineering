package cd4017be.thermokin.module;

import java.util.ArrayList;
import java.util.HashMap;

import cd4017be.lib.util.ItemKey;
import cd4017be.thermokin.module.Part.Type;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * 
 * @author cd4017be
 */
public class Layout {

	public static final Layout NULL = new Layout(-1, ItemStack.EMPTY);

	public final int id;
	public final ItemStack item;
	public String name;
	public int invIn, invOut, invAcc;
	public int tankIn, tankOut, tankAcc;
	/**bit[0,1]: need bottom/top, bits[4-7]: min amount */
	public byte casings = 0x53;

	public Layout(int id, ItemStack item) {
		this.id = id;
		this.item = item;
		this.name = item.getItem().getRegistryName().toString();
	}

	public int ioCount() {
		return invIn + invOut + tankIn + tankOut;
	}

	public boolean isPartValid(int slot, Part part) {
		Type t = part.type;
		if (t.slotS > slot || slot >= t.slotE) return false;
		//TODO check main parts
		return true;
	}

	public ItemStack getResult(Part[] parts, ItemStack[] items, long cfg) {
		ItemStack res = item.copy();
		int[] comp = new int[15];
		byte[] dur = new byte[15];
		for (int i = 0; i < 15; i++) {
			comp[i] = parts[i].id;
			int m = items[i].getMaxDamage();
			dur[i] = (byte) (m > 0 ? (m - items[i].getItemDamage()) * Part.MAX_DUR / m : Part.MAX_DUR);
		}
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setByteArray("dur", dur);
		nbt.setIntArray("comp", comp);
		nbt.setLong("cfg", cfg);
		res.setTagCompound(nbt);
		return res;
	}

	public boolean isComplete(Part[] parts) {
		int n = 0;
		for (int i = 0; i < 6; i++) {
			boolean b = parts[i] != Part.NULL_CASING;
			if (b) n++;
			else if (i < 2 && (casings >> i & 1) != 0) return false;
		}
		return n > casings >> 4;
	}

	public static final ArrayList<Layout> layouts = new ArrayList<Layout>();
	public static final HashMap<ItemKey, Layout> recipes = new HashMap<ItemKey, Layout>();

	public static Layout fromRecipe(ItemStack item0, ItemStack item1, ItemStack item2) {
		Layout layout = recipes.get(new ItemKey(item0, item1, item2));
		return layout == null ? NULL : layout;
	}

	public static Layout fromId(int id) {
		return id >= 0 && id < layouts.size() ? layouts.get(id) : NULL;
	}

}
