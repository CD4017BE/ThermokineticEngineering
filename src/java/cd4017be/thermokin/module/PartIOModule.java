package cd4017be.thermokin.module;

import net.minecraft.item.ItemStack;

public class PartIOModule extends Part {

	public final IOType invType;
	public final int size;
	public final boolean hasItem, hasFluid;

	public PartIOModule(IOType type, int mode, int id, ItemStack item, float Lh, float Tmax, float dmgH, int size) {
		super(Type.MODULE, id, item, Lh, Tmax, dmgH);
		this.invType = type;
		this.size = size;
		this.hasItem = (mode & 1) != 0;
		this.hasFluid = (mode & 2) != 0;
		if (type == IOType.EXT_ACC && Integer.bitCount(mode) > 1)
			throw new IllegalArgumentException("Access modules can only have one resource type!");
	}

	public enum IOType {
		/**external devices have access to processing resources */
		EXT_ACC,
		/**the machine has access to outside resource storage */
		INT_ACC,
		/**machine and external devices access a shared buffer */
		BUFFER
	}

}
