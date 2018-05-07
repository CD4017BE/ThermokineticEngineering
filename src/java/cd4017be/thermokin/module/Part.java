package cd4017be.thermokin.module;

import java.util.HashMap;

import javax.annotation.Nonnull;

import cd4017be.lib.util.ItemKey;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 
 * @author CD4017BE
 */
public class Part {

	public static int MAX_DUR = 250;
	public static final Part
			NULL_CASING = new Part(Type.CASING, 0, ItemStack.EMPTY, 1.0F, Float.POSITIVE_INFINITY, 0),
			NULL_MODULE = new Part(Type.MODULE, 0, ItemStack.EMPTY, 0, Float.POSITIVE_INFINITY, 0),
			NULL_MAIN = new Part(Type.MAIN, 0, ItemStack.EMPTY, 0, Float.POSITIVE_INFINITY, 0);

	public static final HashMap<ItemKey, Part> recipes = new HashMap<ItemKey, Part>();
	public static final Part[] casings = new Part[16], modules = new Part[16], cores = new Part[16];

	@Nonnull
	public static Part getPart(ItemStack stack) {
		return recipes.getOrDefault(new ItemKey(stack), NULL_MAIN);
	}

	@Nonnull
	public static Part getPart(Type type, int id) {
		Part p = null;
		switch(type) {
		case CASING:
			if (id < casings.length) p = casings[id];
			return p == null ? NULL_CASING : p;
		case MODULE:
			if (id < modules.length) p = modules[id];
			return p == null ? NULL_MODULE : p;
		default:
			if (id < cores.length) p = cores[id];
			return p == null ? NULL_MAIN : p;
		}
	}

	public final int id;
	public final Type type;
	/**the Item that represents this part */
	public final ItemStack item;
	/**[J/K/t] heat conductivity */
	public float Lh;
	/**[K] melting temperature */
	public float Tmax;
	/**[dmg/K/t] vulnerability to overheating */
	public float dmgH;

	@SideOnly(Side.CLIENT)
	public int modelId = -1;

	public Part(Type type, int id, ItemStack item, float Lh, float Tmax, float dmgH) {
		this.type = type;
		this.id = id;
		this.item = item;
		this.Lh = Lh;
		this.Tmax = Tmax;
		this.dmgH = dmgH;
	}

	public enum Type {
		CASING(0, 6),
		MODULE(6, 12),
		MAIN(12, 15);

		public final int slotS, slotE;

		private Type(int s, int e) {
			this.slotS = s;
			this.slotE = e;
		}

		public Part NULL() {
			switch(this) {
			case CASING: return NULL_CASING;
			case MODULE: return NULL_MODULE;
			default: return NULL_MAIN;
			}
		}

		public static Type forSlot(int s) {
			return s < 6 ? CASING : s < 12 ? MODULE : MAIN;
		}
	}

}