package cd4017be.kineng.tileentity;

import static cd4017be.kineng.recipe.KineticProcess.A_STOP;
import static cd4017be.kineng.tileentity.IKineticLink.T_SHAPE;
import static cd4017be.kineng.tileentity.IKineticLink.T_TIER;
import static cd4017be.lib.Gui.comp.Progressbar.H_FILL;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
import cd4017be.kineng.block.BlockProcessing;
import cd4017be.kineng.physics.DynamicForce;
import cd4017be.kineng.recipe.KineticProcess;
import cd4017be.kineng.recipe.ProcessingRecipes;
import cd4017be.lib.Gui.*;
import cd4017be.lib.Gui.comp.*;
import cd4017be.lib.network.*;
import cd4017be.lib.tileentity.BaseTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/** 
 * @author CD4017BE */
public class ProcessingBox extends BaseTileEntity implements IForceProvider, IGuiHandlerTile {

	KineticProcess machine = new KineticProcess(3);

	@Override
	public DynamicForce connect(IKineticLink link, EnumFacing side) {
		if (side != getOrientation().back) return null;
		if (link == null)
			return machine.setMode(Integer.MAX_VALUE);
		int type = link.type();
		int types = ((BlockProcessing)getBlockState().getBlock()).types;
		if ((types & 0x100 << (type >> 8)) == 0) return null;
		return machine.setMode(Math.min(type, types & T_TIER | type & T_SHAPE));
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == ITEM_HANDLER_CAPABILITY;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == ITEM_HANDLER_CAPABILITY ? (T)machine : null;
	}

	@Override
	protected void storeState(NBTTagCompound nbt, int mode) {
		super.storeState(nbt, mode);
		nbt.setTag("dev", machine.serializeNBT());
	}

	@Override
	protected void loadState(NBTTagCompound nbt, int mode) {
		super.loadState(nbt, mode);
		machine.deserializeNBT(nbt.getCompoundTag("dev"));
	}

	@Override
	protected void onUnload() {
		super.onUnload();
		machine.unload();
	}

	@Override
	public AdvancedContainer getContainer(EntityPlayer player, int id) {
		return machine.getContainer(player, world.isRemote);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModularGui getGuiScreen(EntityPlayer player, int id) {
		ModularGui gui = new ModularGui(getContainer(player, id));
		GuiFrame frame = new GuiFrame(gui, 176, 150, 6)
		.background(ProcessingRecipes.GUI_TEX, 0, 0)
		.title("gui.kineng.processing.name", 0.5F);
		
		new Button(frame, 8, 16, 8, 25, 0, machine::status, (a)-> gui.sendPkt(A_STOP))
		.texture(200, 0).tooltip("gui.kineng.pr_status#");
		new Button(frame, 16, 16, 53, 25, 0, machine::recipeMode, machine::showRecipes)
		.texture(208, 0).tooltip("gui.kineng.pr_mode#");
		new Progressbar(frame, 32, 10, 72, 28, 224, 0, H_FILL, machine::progress);
		new Tooltip(frame, 32, 10, 72, 28, "\\%.3um / %.3um", machine::progressInfo);
		new FormatText(frame, 32, 8, 72, 16, "\\%.3uN", machine::forceInfo).align(0.5F);
		new FormatText(frame, 32, 8, 72, 42, "\\%.3um/s", machine::speedInfo).align(0.5F);
		gui.compGroup = frame;
		return gui;
	}

}
