package cd4017be.kineng.tileentity;

import static cd4017be.kineng.tileentity.IKineticLink.*;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
import cd4017be.kineng.Main;
import cd4017be.kineng.physics.DynamicForce;
import cd4017be.kineng.recipe.KineticProcess;
import cd4017be.kineng.recipe.ProcessingRecipes;
import cd4017be.lib.Gui.*;
import cd4017be.lib.Gui.AdvancedContainer.IStateInteractionHandler;
import cd4017be.lib.Gui.comp.*;
import cd4017be.lib.capability.BasicInventory;
import cd4017be.lib.network.*;
import cd4017be.lib.tileentity.BaseTileEntity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;


/** 
 * @author CD4017BE */
public class ProcessingBox extends BaseTileEntity implements IForceProvider, IGuiHandlerTile, IStateInteractionHandler {

	KineticProcess machine = new KineticProcess(3);

	@Override
	public DynamicForce connect(IKineticLink link, EnumFacing side) {
		if (side != getOrientation().back) return null;
		if (link == null) {
			mode = 0;
			return machine.setMode(null);
		}
		switch(link.type() & T_SHAPE) {
		case T_SAWBLADE:
			mode = 0x100;
			return machine.setMode(ProcessingRecipes.SAWMILL);
		case T_GRINDER:
			mode = 0x200;
			return machine.setMode(ProcessingRecipes.GRINDER);
		default: return null;
		}
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
	public AdvancedContainer getContainer(EntityPlayer player, int id) {
		AdvancedContainer cont = new AdvancedContainer(this, ssb.build(world.isRemote), player);
		IItemHandler inv = world.isRemote ? new BasicInventory(3) : machine;
		cont.addItemSlot(new GlitchSaveSlot(inv, 0, 35, 25, false), false);
		cont.addItemSlot(new GlitchSaveSlot(inv, 1, 107, 25, false), false);
		cont.addItemSlot(new GlitchSaveSlot(inv, 2, 125, 25, false), false);
		cont.addPlayerInventory(8, 68);
		return cont;
	}

	private static final StateSynchronizer.Builder ssb = StateSynchronizer.builder().addFix(4, 4, 4, 4, 4);
	private static final ResourceLocation GUI_TEX = new ResourceLocation(Main.ID, "textures/gui/processing.png");
	private static final byte A_STOP = 0;

	@Override
	@SideOnly(Side.CLIENT)
	public ModularGui getGuiScreen(EntityPlayer player, int id) {
		ModularGui gui = new ModularGui(getContainer(player, id));
		GuiFrame frame = new GuiFrame(gui, 176, 150, 6)
		.background(GUI_TEX, 0, 0).title("gui.kineng.processing.name", 0.5F);
		new Button(frame, 8, 16, 8, 25, 0, ()-> mode & 3, (a)-> gui.sendPkt(A_STOP))
		.texture(200, 0).tooltip("gui.kineng.pr_status#");
		new Button(frame, 16, 16, 53, 25, 0, ()-> mode >> 8, null)
		.texture(208, 0).tooltip("gui.kineng.pr_mode#");
		new Progressbar(frame, 32, 10, 72, 28, 224, 0, Progressbar.H_FILL, ()-> s / s1);
		new Tooltip(frame, 32, 10, 72, 28, "\\%.3um / %.3um", ()-> new Object[] {s, s1});
		new FormatText(frame, 32, 8, 72, 16, "\\%.3uN", ()-> new Object[] {F}).align(0.5F);
		new FormatText(frame, 32, 8, 72, 42, "\\%.3um/s", ()-> new Object[] {Math.abs(v)}).align(0.5F);
		gui.compGroup = frame;
		return gui;
	}


	@Override
	public void writeState(StateSyncServer state, AdvancedContainer cont) {
		state.putAll(
			(float)(machine.rcp != null ? machine.rcp.F : 0),
			(float)(machine.rcp != null ? machine.rcp.s : 0),
			(float)machine.s, (float)machine.v,
			mode | (machine.working ? 2 : machine.rcp != null ? 1 : 0)
		).endFixed();
	}

	float F, s1, s, v;
	int mode;

	@Override
	public void readState(StateSyncClient state, AdvancedContainer cont) throws Exception {
		F = state.get(F);
		s1 = state.get(s1);
		s = state.get(s);
		v = state.get(v);
		mode = state.get(mode);
	}

	@Override
	public boolean canInteract(EntityPlayer player, AdvancedContainer cont) {
		return canPlayerAccessUI(player);
	}

	@Override
	public void handleAction(PacketBuffer pkt, EntityPlayerMP sender) throws Exception {
		if(pkt.readByte() == A_STOP) machine.stop();
	}

}
