package cd4017be.kineng.tileentity;

import static cd4017be.lib.network.Sync.GUI;
import cd4017be.kineng.Main;
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
public class Tachometer extends ShaftPart
implements ITickableServerOnly, IRedstoneTile, IGuiHandlerTile, IStateInteractionHandler {

	public @Sync(to=SAVE|GUI, tag="ref") float refV = 1F;
	public @Sync(to=GUI) float v;
	public @Sync(to=SAVE|GUI, tag="rs") int lastRS;

	@Override
	public void update() {
		if (shaft == null) return;
		v = (float)shaft.av();
		int rs = Math.round(v / refV);
		if (rs != lastRS) {
			lastRS = rs;
			world.notifyNeighborsOfStateChange(pos, blockType, false);
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
	public AdvancedContainer getContainer(EntityPlayer player, int id) {
		return new AdvancedContainer(this, StateSyncAdv.of(false, this), player);
	}

	private static final ResourceLocation GUI_TEX = new ResourceLocation(Main.ID, "textures/gui/debug.png");

	@Override
	@SideOnly(Side.CLIENT)
	public ModularGui getGuiScreen(EntityPlayer player, int id) {
		ModularGui gui = new ModularGui(getContainer(player, id));
		GuiFrame frame = new GuiFrame(gui, 106, 42, 5)
		.background(GUI_TEX, 0, 86).title("gui.kineng.tachometer.name", 0.5F);
		
		new TextField(frame, 32, 7, 7, 27, 6,
			()-> String.format(Math.abs(refV) < 1F ? "%+.3f" : "%+.4g", refV),
			t -> {
				try {
					gui.sendPkt(Float.parseFloat(t));
				} catch(NumberFormatException e) {}
			}).tooltip("gui.kineng.ref_vel");
		new FormatText(frame, 30, 7, 68, 27, "\\%d", ()-> new Object[] {lastRS}).align(0.5F);
		new Tooltip(frame, 30, 7, 68, 27, "gui.kineng.rs_out.tip", null);
		new FormatText(frame, 48, 7, 29, 16, "\\%+.3f", ()-> new Object[] {v}).align(0.5F);
		new Tooltip(frame, 48, 7, 29, 16, "gui.kineng.ang_vel.tip", null);
		gui.compGroup = frame;
		return gui;
	}

	@Override
	public boolean canInteract(EntityPlayer player, AdvancedContainer cont) {
		return canPlayerAccessUI(player);
	}

	@Override
	public void handleAction(PacketBuffer pkt, EntityPlayerMP sender) throws Exception {
		refV = pkt.readFloat();
		markDirty(SAVE);
	}

}
