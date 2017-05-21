package cd4017be.thermokin.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.physics.GasState;
import cd4017be.thermokin.tileentity.SolidFuelHeater;
import cd4017be.thermokin.tileentity.SolidFuelHeater.LastState;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.util.Utils;

public class GuiSolidFuelHeater extends GuiMachine {

	private static final float T_OFS = 300F, T_RANGE = 2500F;
	private final SolidFuelHeater tile;

	public GuiSolidFuelHeater(SolidFuelHeater tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.tile = tileEntity;
		this.MAIN_TEX = new ResourceLocation("thermokin", "textures/gui/heater.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 150;
		super.initGui();
		guiComps.add(new Slider(1, 89, 16, 70, 180, 8, 4, 12, true));
		guiComps.add(new Slider(2, 89, 38, 70, 176, 8, 4, 12, true));
		guiComps.add(new ProgressBar(3, 89, 29, 70, 8, 176, 0, (byte)0));
		guiComps.add(new ProgressBar(4, 26, 16, 4, 34, 200, 8, (byte)1));
		guiComps.add(new AirMix(5, 44, 16, 34, 34));
		guiComps.add(new ProgressBar(6, 54, 35, 14, 14, 184, 8, (byte)1));
		guiComps.add(new Tooltip<Object[]>(7, 44, 16, 34, 34, "heater.mix"));
		guiComps.add(new Tooltip<Float>(8, 89, 16, 70, 12, "thermostat.max"));
		guiComps.add(new Tooltip<Float>(9, 89, 38, 70, 12, "thermostat.min"));
		guiComps.add(new Tooltip<Float>(10, 89, 29, 70, 8, "Temp1"));
	}

	@Override
	protected Object getDisplVar(int id) {
		LastState ls = (LastState)((DataContainer)inventorySlots).extraRef;
		switch(id) {
		case 1: return (ls.Tmax - T_OFS) / T_RANGE;
		case 2: return (ls.Tmin - T_OFS) / T_RANGE;
		case 3: return (ls.temp - T_OFS) / T_RANGE;
		case 4: return ls.fuel / ls.maxFuel;
		case 5: return ls;
		case 6: return ls.speed / 14.0F;
		case 7: return new Object[]{ls.in.localizedName(), ls.dV * 100F, ls.Tin - 273.15F, Utils.formatNumber(ls.P, 4, 0)};
		case 8: return ls.Tmax - 273.15F;
		case 9: return ls.Tmin - 273.15F;
		case 10: return ls.temp - 273.15F;
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		LastState ls = (LastState)((DataContainer)inventorySlots).extraRef;
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		case 1: dos.writeByte(AutomatedTile.CmdOffset).writeFloat(ls.Tmax = (Float)obj * T_RANGE + T_OFS); break;
		case 2: dos.writeByte(AutomatedTile.CmdOffset + 1).writeFloat(ls.Tmin = (Float)obj * T_RANGE + T_OFS); break;
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

	private class AirMix extends GuiComp<GasState> {

		public AirMix(int id, int px, int py, int w, int h) {
			super(id, px, py, w, h);
		}

		@Override
		public void draw() {
			LastState ls = (LastState)getDisplVar(id);
			int f = (int)(ls.dV * (float)h), f1 = h - f;
			color(ls.in.color | 0x60000000);
			drawTexturedModalRect(px, py + f1, 204, 8 + f1, w, f);
			color(Objects.combustionWaste.color | 0x60000000);
			drawTexturedModalRect(px, py, 204, 8, w, f1);
			color(0xffffffff);
		}

	}

}
