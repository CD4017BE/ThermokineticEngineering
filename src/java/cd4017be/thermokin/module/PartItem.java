package cd4017be.thermokin.module;

import net.minecraft.item.ItemStack;

public class PartItem extends Part {

	public final Type invType;
	public final int size;

	public PartItem(Type type, ItemStack item, float Lh, float Tmax, float dmgH, int size) {
		super(Part.Type.ITEM, item, Lh, Tmax, dmgH);
		this.invType = type;
		this.size = size;
	}

	public enum Type {
		ACCESS, AUTOMATIC, BUFFER
	}

}
