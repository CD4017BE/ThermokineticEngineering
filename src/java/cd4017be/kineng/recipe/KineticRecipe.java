package cd4017be.kineng.recipe;

import net.minecraft.item.ItemStack;

/** 
 * @author CD4017BE */
public class KineticRecipe {

	public final ItemStack[] io;
	/** [m] distance to move per operation */
	public final double s;
	/** [N] force to apply */
	public final double F;

	public KineticRecipe(double s, double F, ItemStack... io) {
		this.io = io;
		this.s = s;
		this.F = F;
	}

}
