package cd4017be.thermokin.module;

import net.minecraft.item.ItemStack;

/**
 * 
 * @author CD4017BE
 */
public class Part {

	public static int MAX_DUR = 250;

	public static Part getPart(ItemStack stack) {
		return null;
	}

	public final Type type;
	/**the Item that represents this part */
	public final ItemStack item;
	/**[J/K/t] heat conductivity */
	public final float Lh;
	/**[K] melting temperature */
	public final float Tmax;
	/**[dmg/K/t] vulnerability to overheating */
	public final float dmgH;

	public Part(Type type, ItemStack item, float Lh, float Tmax, float dmgH) {
		this.type = type;
		this.item = item;
		this.Lh = Lh;
		this.Tmax = Tmax;
		this.dmgH = dmgH;
	}

	public enum Type {
		CASING, THERMAL, ITEM, FLUID
	}

}