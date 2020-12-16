package cd4017be.kineng.tileentity;

import static cd4017be.kineng.physics.Ticking.dt;
import static cd4017be.lib.network.Sync.GUI;
import static cd4017be.lib.network.Sync.Type.F32;
import static net.minecraftforge.energy.CapabilityEnergy.ENERGY;
import cd4017be.kineng.Main;
import cd4017be.kineng.physics.DynamicForce;
import cd4017be.kineng.physics.ForceCon;
import cd4017be.lib.Gui.AdvancedContainer;
import cd4017be.lib.Gui.AdvancedContainer.IStateInteractionHandler;
import cd4017be.lib.Gui.ModularGui;
import cd4017be.lib.Gui.comp.*;
import cd4017be.lib.network.*;
import cd4017be.lib.tileentity.BaseTileEntity;
import cd4017be.lib.tileentity.BaseTileEntity.ITickableServerOnly;
import cd4017be.lib.util.Utils;
import cd4017be.math.cplx.CplxD;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/** 
 * @author CD4017BE */
public class FluxCoil extends BaseTileEntity implements IForceProvider, IGuiHandlerTile, IStateInteractionHandler, ITickableServerOnly {

	public static double J_RF, E_MAX;

	@Sync(to = SAVE)
	public final Coil coil = new Coil();

	@Override
	public void update() {
		int e = coil.getEnergyStored();
		if (e <= 0) return;
		IEnergyStorage es = Utils.neighborCapability(this, getOrientation().front, ENERGY);
		if (es == null) return;
		coil.extractEnergy(es.receiveEnergy(e, false), false);
	}

	@Override
	public DynamicForce connect(IKineticLink link, EnumFacing side) {
		if (side != getOrientation().back || link != null && link.type() != IKineticLink.T_MAGNETIC) return null;
		return coil;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == ENERGY;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == ENERGY ? (T)coil : null;
	}

	@Override
	public AdvancedContainer getContainer(EntityPlayer player, int id) {
		return new AdvancedContainer(this, StateSyncAdv.of(false, coil), player);
	}

	private static final ResourceLocation GUI_TEX = new ResourceLocation(Main.ID, "textures/gui/debug.png");

	@Override
	@SideOnly(Side.CLIENT)
	public ModularGui getGuiScreen(EntityPlayer player, int id) {
		ModularGui gui = new ModularGui(getContainer(player, id));
		GuiFrame frame = new GuiFrame(gui, 122, 86, 5)
		.background(GUI_TEX, 0, 128).title("gui.kineng.fluxcoil.name", 0.5F);
		new Progressbar(frame, 16, 62, 8, 16, 210, 128, Progressbar.V_FILL, ()-> coil.E / coil.Emax);
		new Tooltip(frame, 16, 62, 8, 16, "gui.kineng.fluxcoil.charge", ()-> new Object[] {coil.E / J_RF, coil.Emax / J_RF});
		new Progressbar(frame, 7, 62, 26, 16, 226, 128, Progressbar.V_FILL, coil::efficiency);
		new Tooltip(frame, 7, 62, 26, 16, "gui.kineng.fluxcoil.eff", ()-> new Object[] {
			coil.efficiency() * 100D, coil.dE0 < 0 ? "<-" : "->", Math.abs(coil.dE0 / J_RF), Math.abs(coil.dE1 / dt)
		});
		new Progressbar(frame, 7, 62, 71, 16, 233, 128, Progressbar.V_FILL, ()-> coil.force() / coil.maxForce());
		new Progressbar(frame, 7, 62, 71, 16, 233, 128, Progressbar.V_FILL, ()-> coil.force() / coil.maxForce(), 0.0, -1.0);
		new Tooltip(frame, 7, 62, 71, 16, "gui.kineng.fluxcoil.force", ()-> new Object[] {coil.force()});
		new Progressbar(frame, 16, 62, 80, 16, 240, 128, Progressbar.V_FILL, ()-> coil.v / (coil.v0 * 2.0));
		new Tooltip(frame, 16, 62, 80, 16, "gui.kineng.fluxcoil.vel", ()-> new Object[] {coil.v});
		new Slider(frame, 16, 5, 66, 99, 14, 240, 190, false, ()-> coil.v0, (x)-> {
			coil.v0 = Math.rint(x * 2.0) * 0.5;
		}, ()-> gui.sendPkt((byte)0, (float)coil.v0), 30, -30).scroll(0.5F).tooltip("gui.kineng.fluxcoil.target");
		new Button(frame, 16, 16, 44, 39, 0, ()-> coil.engaged ? 1 : 0, (a)-> gui.sendPkt((byte)1))
		.texture(194, 128).tooltip("gui.kineng.fluxcoil.on#");
		gui.compGroup = frame;
		return gui;
	}

	@Override
	public boolean canInteract(EntityPlayer player, AdvancedContainer cont) {
		return canPlayerAccessUI(player);
	}

	@Override
	public void handleAction(PacketBuffer pkt, EntityPlayerMP sender) throws Exception {
		switch(pkt.readByte()) {
		case 0:
			double v0 = pkt.readFloat();
			if (Math.abs(v0) > 30) v0 = Math.copySign(30, v0);
			else if (Double.isNaN(v0)) v0 = 0.0;
			coil.v0 = v0;
			break;
		case 1:
			coil.engaged = !coil.engaged;
			break;
		}
	}

	public static class Coil extends DynamicForce implements IEnergyStorage {

		@Sync(to=GUI|SAVE, type=F32)
		public double v0 = 5.0;
		@Sync(to=GUI|SAVE)
		public double E;
		@Sync(to=GUI, type=F32)
		public double v, dE0, dE1, Emax = E_MAX, Jrf = J_RF;
		@Sync(to=GUI|SAVE, tag="on")
		public boolean engaged;

		@Override
		public void work(double dE, double ds, double v1) {
			this.v = v1;
			this.E -= dE0 = v1 != 0 ? dE * v0 / v1 : 0;
			if (this.E < 0) this.E = 0;
			dE1 = dE;
		}

		@Override
		public ForceCon getM(CplxD M, double av) {
			if (engaged) {
				double E = MathHelper.clamp(this.E, av * r / v0 > 1.0 ? 1 : 0, Emax);
				Fdv = -E * (Emax - E) / (Emax * dt * (v0 * v0 + 5.0));
				F = -v0 * Fdv;
			} else {
				Fdv = 0;
				F = 0;
			}
			return super.getM(M, av);
		}

		@Override
		public int receiveEnergy(int maxReceive, boolean simulate) {
			maxReceive = Math.max(0, Math.min(maxReceive, -getEnergyStored()));
			if (!simulate) E += maxReceive * Jrf;
			return maxReceive;
		}

		@Override
		public int extractEnergy(int maxExtract, boolean simulate) {
			maxExtract = Math.max(0, Math.min(maxExtract, getEnergyStored()));
			if (!simulate) E -= maxExtract * Jrf;
			return maxExtract;
		}

		@Override
		public int getEnergyStored() {
			return (int)((E - Emax * 0.5) / Jrf);
		}

		@Override
		public int getMaxEnergyStored() {
			return MathHelper.floor(Emax * 0.5 / Jrf);
		}

		@Override
		public boolean canExtract() {
			return true;
		}

		@Override
		public boolean canReceive() {
			return true;
		}

		public double efficiency() {
			return dE0 > 0 ? dE1 / dE0 : dE1 < 0 ? dE0 / dE1 : 1.0;
		}

		public double force() {
			return dE1 == 0 ? 0 : dE1 / (v * dt);
		}

		public double maxForce() {
			return 0.25 * Emax * v0 / ((v0 * v0 + 1.0) * dt);
		}

	}

}
