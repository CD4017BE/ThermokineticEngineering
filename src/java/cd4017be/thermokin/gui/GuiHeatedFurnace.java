package cd4017be.thermokin.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cd4017be.thermokin.tileentity.HeatedFurnace;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;

public class GuiHeatedFurnace extends GuiMachine {

	private final float TempOffset = 273.15F;
	private final HeatedFurnace tile;

	public GuiHeatedFurnace(HeatedFurnace tile, EntityPlayer player) {
		super(new TileContainer(tile, player));
		this.tile = tile;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/heatedFurnace.png");
	}

	@Override
	public void initGui() {
		this.xSize = 176;
		this.ySize = 150;
		super.initGui();
		guiComps.add(new ProgressBar(1, 36, 19, 32, 10, 176, 8, (byte)0));
		guiComps.add(new ProgressBar(2, 17, 38, 70, 8, 176, 0, (byte)0));
		guiComps.add(new Text(3, 61, 25, 0, 8, "\\%s").center().font(0xffc04040, 8));
		guiComps.add(new Text(4, 133, 38, 0, 8, "Temp2").center().font(0xff808040, 8));
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 1: return tile.getProgress();
		case 2: return (tile.temp - TempOffset) / (HeatedFurnace.NeededTemp - TempOffset) / 2F;
		case 3: return tile.num > 0 ? "x" + tile.num : "";
		case 4: return new Object[]{tile.temp - TempOffset, HeatedFurnace.NeededTemp - TempOffset};
		default: return null;
		}
	}

}
