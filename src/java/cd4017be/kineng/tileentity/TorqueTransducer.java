package cd4017be.kineng.tileentity;

import static cd4017be.lib.network.Sync.GUI;
import java.util.ArrayList;
import cd4017be.kineng.Main;
import cd4017be.kineng.physics.*;
import cd4017be.lib.Gui.AdvancedContainer;
import cd4017be.lib.Gui.AdvancedContainer.IStateInteractionHandler;
import cd4017be.lib.Gui.ModularGui;
import cd4017be.lib.Gui.comp.*;
import cd4017be.lib.block.AdvancedBlock.IRedstoneTile;
import cd4017be.lib.network.*;
import cd4017be.lib.tileentity.BaseTileEntity.ITickableServerOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 
 * @author CD4017BE */
public class TorqueTransducer extends ShaftPart
implements ITickableServerOnly, IRedstoneTile, IGuiHandlerTile, IStateInteractionHandler {

	public @Sync(to=SAVE|GUI, tag="ref") float refM = 1F;
	public @Sync(to=GUI) float M;
	public @Sync(to=SAVE|GUI, tag="rs") int lastRS;
	double dJ;
	int idx;

	@Override
	public void update() {
		if (shaft == null) return;
		if (idx < 0) updateShaft();
		M = 0;
		int i = idx;
		for (Connection con : shaft.cons)
			M += (--i < 0 ? dJ - 1D : dJ) * con.M;
		M /= shaft.x;
		int rs = Math.round(M / refM);
		if (rs != lastRS) {
			lastRS = rs;
			world.notifyNeighborsOfStateChange(pos, blockType, false);
		}
	}

	private void updateShaft() {
		ArrayList<Connection> cons = shaft.cons;
		double dJ = 0, sJ = 0;
		int i = 0;
		boolean found = false;
		for (IShaftPart part : shaft.parts) {
			double J = part.J();
			sJ += J;
			if (part == this) {
				dJ = -dJ;
				found = true;
			} else {
				dJ += J;
				if (!found)
					while(i < cons.size() && cons.get(i).host == part)
						i++;
			}
		}
		this.idx = i;
		this.dJ = dJ / sJ * 0.5;
	}

	@Override
	public double setShaft(ShaftAxis shaft) {
		idx = -1;
		return super.setShaft(shaft);
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
	public AdvancedContainer getContainer(EntityPlayer player, int id) {
		return new AdvancedContainer(this, StateSyncAdv.of(false, this), player);
	}

	private static final ResourceLocation GUI_TEX = new ResourceLocation(Main.ID, "textures/gui/debug.png");

	@Override
	@SideOnly(Side.CLIENT)
	public ModularGui getGuiScreen(EntityPlayer player, int id) {
		ModularGui gui = new ModularGui(getContainer(player, id));
		GuiFrame frame = new GuiFrame(gui, 106, 42, 5)
		.background(GUI_TEX, 106, 86).title("gui.kineng.transducer.name", 0.5F);
		
		new TextField(frame, 32, 7, 7, 27, 6,
			()-> String.format(Math.abs(refM) < 1000F ? "%+.3f" : "%+.4g", refM * 0.001F),
			t -> {
				try {
					gui.sendPkt(Float.parseFloat(t) * 1000F);
				} catch(NumberFormatException e) {}
			}).tooltip("gui.kineng.ref_trq");
		new FormatText(frame, 30, 7, 68, 27, "\\%d", ()-> new Object[] {lastRS}).align(0.5F);
		new Tooltip(frame, 30, 7, 68, 27, "gui.kineng.rs_out.tip", null);
		new FormatText(frame, 48, 7, 29, 16, "\\%+.2f", ()-> new Object[] {M * 0.001}).align(0.5F);
		new Tooltip(frame, 48, 7, 29, 16, "gui.kineng.torque.tip", null);
		gui.compGroup = frame;
		return gui;
	}

	@Override
	public boolean canInteract(EntityPlayer player, AdvancedContainer cont) {
		return canPlayerAccessUI(player);
	}

	@Override
	public void handleAction(PacketBuffer pkt, EntityPlayerMP sender) throws Exception {
		refM = pkt.readFloat();
		markDirty(SAVE);
	}

}
