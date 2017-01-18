package cd4017be.thermokin.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cd4017be.thermokin.tileentity.Evaporator;
import cd4017be.thermokin.tileentity.Evaporator.LastState;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.util.Utils;

public class GuiEvaporator extends GuiMachine {

	private final DataContainer cont;

	public GuiEvaporator(Evaporator tile, EntityPlayer player) {
		super(new DataContainer(tile, player));
		this.cont = (DataContainer)inventorySlots;
		this.MAIN_TEX = new ResourceLocation("thermokin", "textures/gui/evaporator.png");
		this.bgTexX = 176;
	}

	@Override
	public void initGui() {
		xSize = 75;
		ySize = 84;
		super.initGui();
		guiComps.add(new ProgressBar(0, 59, 16, 8, 60, 176, 84, (byte)1));
		guiComps.add(new Tooltip(1, 59, 16, 8, 60, "Temp1"));
		guiComps.add(new Tooltip(2, 8, 16, 32, 60, "evap.mix"));
	}

	@Override
	protected Object getDisplVar(int id) {
		LastState data = (LastState)cont.extraRef;
		switch(id) {
		case 0: return data.T / 1200F;
		case 1: return data.T - 273.15F;
		case 2: return new Object[]{
			(float)Evaporator.SizeG - data.Vl,
			data.Sg.localizedName(),
			Utils.formatNumber(data.P, 3, 0),
			data.Vl,
			data.Sl == null ? "" : data.Sl.localizedName()
		};
		default: return null;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		LastState data = (LastState)cont.extraRef;
		int n = (int)(data.Vl / (float)Evaporator.SizeG * 60F);
		if (data.Sl != null) {
			color(data.Sl.color | 0xdf000000);
			drawTexturedModalRect(guiLeft + 8, guiTop + 76 - n, 184, 84, 32, n);
		}
		color(data.Sg.color | 0x60000000);
		drawTexturedModalRect(guiLeft + 8, guiTop + 16, 184, 84, 32, 60 - n);
	}
}
