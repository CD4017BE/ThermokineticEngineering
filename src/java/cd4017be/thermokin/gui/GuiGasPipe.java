package cd4017be.thermokin.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cd4017be.thermokin.tileentity.GasPipe;
import cd4017be.thermokin.physics.GasState;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.util.Utils;

public class GuiGasPipe extends GuiMachine {

	private GasState data;

	public GuiGasPipe(GasPipe tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/tesla.png");
		this.bgTexX = 176; this.bgTexY = 80;
	}

	@Override
	public void initGui() {
		data = (GasState)((TileContainer)this.inventorySlots).extraRef;
		this.xSize = 80;
		this.ySize = 56;
		super.initGui();
		guiComps.add(new Text(0, 8, 16, 64, 32, "gas.stat").font(0x808040, 8));
	}

	@Override
	protected Object getDisplVar(int id) {
		return new Object[]{
			Utils.formatNumber(data.V, 3),
			Utils.formatNumber(data.P(), 4, 0),
			data.T - 273.15F,
			Utils.formatNumber(data.nR / 8.314472F, 3, 0)
		};
	}

}
