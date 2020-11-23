package cd4017be.kineng.tileentity;

import cd4017be.lib.Gui.AdvancedContainer;
import cd4017be.lib.Gui.AdvancedContainer.IStateInteractionHandler;
import cd4017be.lib.block.AdvancedBlock.IRedstoneTile;
import cd4017be.lib.network.*;
import cd4017be.lib.tileentity.BaseTileEntity.ITickableServerOnly;
import cd4017be.lib.util.Utils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

/** 
 * @author CD4017BE */
public class Tachometer extends ShaftPart
implements ITickableServerOnly, IRedstoneTile, IGuiHandlerTile, IStateInteractionHandler {

	double refV;
	int lastRS;

	@Override
	public void update() {
		int rs = (int)Math.round(shaft.av() / refV);
		if (rs != lastRS) {
			lastRS = rs;
			Utils.notifyNeighborTile(this, null);
		}
	}

	@Override
	public int redstoneLevel(EnumFacing side, boolean strong) {
		return strong ? 0 : lastRS;
	}

	@Override
	public boolean connectRedstone(EnumFacing side) {
		return true;
	}

	@Override
	protected void storeState(NBTTagCompound nbt, int mode) {
		super.storeState(nbt, mode);
		if (mode == SAVE) {
			nbt.setDouble("ref", refV);
			nbt.setInteger("rs", lastRS);
		}
	}

	@Override
	protected void loadState(NBTTagCompound nbt, int mode) {
		super.loadState(nbt, mode);
		if (mode == SAVE) {
			refV = nbt.getDouble("ref");
			lastRS = nbt.getInteger("rs");
		}
	}

	StateSynchronizer.Builder ssb = StateSynchronizer.builder().addFix(4, 4);

	@Override
	public AdvancedContainer getContainer(EntityPlayer player, int id) {
		return new AdvancedContainer(this, ssb.build(world.isRemote), player);
	}

	@Override
	public GuiScreen getGuiScreen(EntityPlayer player, int id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeState(StateSyncServer state, AdvancedContainer cont) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void readState(StateSyncClient state, AdvancedContainer cont) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean canInteract(EntityPlayer player, AdvancedContainer cont) {
		return canPlayerAccessUI(player);
	}

}
