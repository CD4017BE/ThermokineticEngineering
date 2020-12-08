package cd4017be.kineng.tileentity;

import static cd4017be.kineng.physics.Ticking.dt;
import static cd4017be.lib.network.Sync.GUI;
import cd4017be.kineng.Main;
import cd4017be.kineng.physics.*;
import cd4017be.lib.Gui.AdvancedContainer;
import cd4017be.lib.Gui.AdvancedContainer.IStateInteractionHandler;
import cd4017be.lib.Gui.ModularGui;
import cd4017be.lib.Gui.comp.*;
import cd4017be.lib.network.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 
 * @author CD4017BE */
public class MechanicalDebug extends ShaftPart implements IGuiHandlerTile, IStateInteractionHandler {

	final ForceCon con = new ForceCon(this, 1.0);
	{
		con.maxF = Double.POSITIVE_INFINITY;
	}

	public @Sync(to = SAVE|GUI) float c0, c1;
	public @Sync(to = SAVE|GUI) double Eacc;
	public @Sync(to = GUI) float v, P, F;
	public @Sync(to = SAVE|GUI) byte mode;

	@Sync public void mode(byte mode) {
		if (mode == this.mode) return;
		this.mode = mode;
		if (world != null && world.isRemote) return;
		switch(mode) {
		case A_CONST_PWR:
			con.link(new ConstPower());
			break;
		case A_CONST_VEL:
			con.link(new ConstSpeed());
			break;
		case A_FRICTION:
			con.link(new Friction());
			break;
		default:
			con.link(null);
		}
	}

	@Override
	public double setShaft(ShaftAxis shaft) {
		con.setShaft(shaft);
		return super.setShaft(shaft);
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
		GuiFrame frame = new GuiFrame(gui, 144, 86, 5)
		.background(GUI_TEX, 0, 0).title("gui.kineng.debug.name", 0.5F);
		new Button(frame, 16, 16, 8, 16, 0, ()-> mode == A_CONST_PWR ? 1 : 0, (a)-> gui.sendPkt(A_CONST_PWR))
		.texture(240, 0).tooltip("gui.kineng.debug.mode1");
		new Button(frame, 16, 16, 8, 34, 0, ()-> mode == A_CONST_VEL ? 1 : 0, (a)-> gui.sendPkt(A_CONST_VEL))
		.texture(240, 0).tooltip("gui.kineng.debug.mode2");
		new Button(frame, 16, 16, 8, 52, 0, ()-> mode == A_FRICTION ? 1 : 0, (a)-> gui.sendPkt(A_FRICTION))
		.texture(240, 0).tooltip("gui.kineng.debug.mode3");
		new Button(frame, 18, 9, 7, 70, 0, null, (a)-> gui.sendPkt(A_RESET)).tooltip("gui.kineng.debug.reset");
		new TextField(frame, 70, 7, 66, 16, 12, ()-> Float.toString(c0), (t)-> {
			try {
				gui.sendPkt(A_C0, Float.parseFloat(t));
			} catch (NumberFormatException e) {}
		});
		new TextField(frame, 70, 7, 66, 27, 12, ()-> Float.toString(c1), (t)-> {
			try {
				gui.sendPkt(A_C1, Float.parseFloat(t));
			} catch (NumberFormatException e) {}
		});
		new Button(frame, 32, 20, 27, 15, 0, ()-> mode, null).texture(208, 0).tooltip("gui.kineng.debug.par#");
		new FormatText(frame, 70, 7, 66, 44, "\\%0$.6u\n%1$.6u\n%2$.6u\n%3$.6u", ()-> new Object[] {v, F, P, Eacc})
		.align(0F).color(0xff202020);
		gui.compGroup = frame;
		return gui;
	}

	@Override
	public boolean canInteract(EntityPlayer player, AdvancedContainer cont) {
		return canPlayerAccessUI(player);
	}

	static final byte A_NONE = 0, A_CONST_PWR = 1, A_CONST_VEL = 2, A_FRICTION = 3, A_C0 = 4, A_C1 = 5, A_RESET = 6; 

	@Override
	public void handleAction(PacketBuffer pkt, EntityPlayerMP sender) throws Exception {
		byte a = pkt.readByte();
		switch(a) {
		case A_CONST_PWR:
			if (con.force instanceof ConstPower) break;
			mode = a;
			c0 = 0F; //Power [W]
			c1 = 1F; //base velocity [m/s]
			con.link(new ConstPower());
			break;
		case A_CONST_VEL:
			if (con.force instanceof ConstSpeed) break;
			mode = a;
			c0 = 0F; //target speed [m/s]
			c1 = 0F; //strength [N*s/m]
			con.link(new ConstSpeed());
			break;
		case A_FRICTION:
			if (con.force instanceof Friction) break;
			mode = a;
			c0 = 0F;    //friction force [N]
			c1 = 0.01F; //null velocity [m/s]
			con.link(new Friction());
			break;
		case A_C0:
			c0 = pkt.readFloat();
			break;
		case A_C1:
			c1 = pkt.readFloat();
			break;
		case A_RESET:
			Eacc = 0;
			break;
		}
	}

	private class ConstPower extends DynamicForce {

		/** [J] */
		public double E;

		@Override
		public void work(double dE, double ds, double v1) {
			E += c0 * dt - dE;
			double v_ = c1 * (Math.abs(v1 / c1) + 1.0);
			F = E / (v_ * dt);
			Fdv = -F / v_;
			v = (float)v1;
			P = (float)(dE / dt);
			MechanicalDebug.this.F = (float)(dE / ds);
			Eacc += dE;
		}

	}

	private class Friction extends DynamicForce {

		@Override
		public void work(double dE, double ds, double v1) {
			if (c1 <= 0) {
				Fdv = 0;
				F = c0;
			} else {
				Fdv = -c0 / (Math.abs(v1) + c1);
				F = 0;
			}
			v = (float)v1;
			P = (float)(dE / dt);
			MechanicalDebug.this.F = (float)(dE / ds);
			Eacc += dE;
		}

	}

	private class ConstSpeed extends DynamicForce {

		@Override
		public void work(double dE, double ds, double v1) {
			F = c0 * c1;
			Fdv = -c1;
			v = (float)v1;
			P = (float)(dE / dt);
			MechanicalDebug.this.F = (float)(dE / ds);
			Eacc += dE;
		}

	}

}
