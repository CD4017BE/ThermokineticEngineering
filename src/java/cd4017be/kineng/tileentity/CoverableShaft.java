package cd4017be.kineng.tileentity;

import cd4017be.lib.block.AdvancedBlock.ICoverableTile;
import cd4017be.lib.block.AdvancedBlock.ISelfAwareTile;
import cd4017be.lib.templates.Cover;
import net.minecraft.nbt.NBTTagCompound;

/** 
 * @author CD4017BE */
public class CoverableShaft extends ShaftPart implements ICoverableTile, ISelfAwareTile {

	public Cover cover = new Cover();

	@Override
	public Cover getCover() {
		return cover;
	}

	@Override
	protected void storeState(NBTTagCompound nbt, int mode) {
		super.storeState(nbt, mode);
		if (mode < SYNC || (redraw & 1) != 0)
			cover.writeNBT(nbt, "cover", mode == SYNC);
	}

	@Override
	protected void loadState(NBTTagCompound nbt, int mode) {
		super.loadState(nbt, mode);
		if (mode < SYNC || (redraw & 1) != 0)
			cover.readNBT(nbt, "cover", mode == SYNC ? this : null);
	}

	@Override
	public void breakBlock() {
		cover.onBreak(this);
	}

}
