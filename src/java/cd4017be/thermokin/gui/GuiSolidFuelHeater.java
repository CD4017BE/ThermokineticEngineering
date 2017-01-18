package cd4017be.thermokin.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import cd4017be.thermokin.tileentity.SolidFuelHeater;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;

public class GuiSolidFuelHeater extends GuiMachine {

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
		guiComps.add(new Slider(1, 80, 24, 70, 198, 8, 4, 12, true));
		guiComps.add(new ProgressBar(2, 54, 31-14, 14, 14, 184, 22-14, (byte)1));
		guiComps.add(new ProgressBar(3, 53, 50-16, 8, 16, 176, 24-16, (byte)1).setTooltip("x*8+0;boiler.burnUp"));
		guiComps.add(new ProgressBar(4, 80, 16, 70, 8, 176, 0, (byte)0));
		guiComps.add(new Text(5, 115, 38, 0, 8, "Temp2").center().font(0x808040, 8));
		guiComps.add(new Button(6, 62, 35, 6, 6, -1));
		guiComps.add(new Button(7, 62, 43, 6, 6, -1));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 1: return (tile.Tref - 300F) / 2500F;
		case 2: return (float)tile.burn / (float)tile.fuel;
		case 3: return (float)tile.speed / 8F;
		case 4: return (tile.temp - 300F) / 2500F;
		case 5: return new Object[]{tile.temp - 273.15F, tile.Tref - 273.15F};
		default: return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		PacketBuffer dos = tile.getPacketTargetData();
		switch(id) {
		case 1: dos.writeByte(AutomatedTile.CmdOffset).writeFloat(tile.Tref = (Float)obj * 2500F + 300F); break;
		case 6: dos.writeByte(AutomatedTile.CmdOffset + 1); break;
		case 7: dos.writeByte(AutomatedTile.CmdOffset + 2); break;
		}
		if (send) BlockGuiHandler.sendPacketToServer(dos);
	}

}
