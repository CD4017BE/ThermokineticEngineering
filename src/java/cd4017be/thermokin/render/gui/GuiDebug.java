package cd4017be.thermokin.render.gui;

import cd4017be.lib.Gui.AdvancedGui;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.thermokin.tileentity.Debug;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;


/**
 * @author CD4017BE
 *
 */
public class GuiDebug extends AdvancedGui {

	private final Debug tile;

	/**
	 * @param container
	 */
	public GuiDebug(IGuiData tile, EntityPlayer player) {
		super(new DataContainer(tile, player));
		this.tile = (Debug)tile;
		this.MAIN_TEX = new ResourceLocation("thermokin:textures/gui/debug.png");
	}

	@Override
	public void initGui() {
		xSize = 90;
		ySize = 121;
		super.initGui();
		guiComps.add(new TextField(0, 23, 16, 48, 8, 10).setTooltip("debug.T"));
		guiComps.add(new Text<Object[]>(1, 23, 24, 48, 81, "debug.dQ").font(0xff404040, 9));
		guiComps.add(new Button(2, 23, 34, 48, 8, -1).setTooltip("debug.reset"));
		for (int i = 0; i < tile.dQ.length; i++)
			guiComps.add(new Button(i + 3, 14, 42 + i * 9, 9, 9, 1).texture(90, 0));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mx, int my) {
		super.drawGuiContainerForegroundLayer(mx, my);
		if (mx >= guiLeft + 7 && mx < guiLeft + 23 && my >= guiTop + 42) {
			int i = (my - guiTop - 42) / 9;
			if (i < 6)
				drawSideCube(-64, 15, i, (byte)3);
		}
	}

	@Override
	protected Object getDisplVar(int id) {
		switch(id) {
		case 0: return "" + tile.fixT;
		case 1: 
			Object[] obj = new Object[tile.dQ.length + 2];
			obj[0] = tile.envT;
			obj[1] = (double)tile.Q;
			for (int i = 0; i < tile.dQ.length; i++) obj[i + 2] = Float.isNaN(tile.dQ[i]) ? 0.0 : (double)tile.dQ[i] * 20.0;
			return obj;
		case 2: return null;
		default: id -= 3;
			if (id < tile.dQ.length)
				return Float.isNaN(tile.dQ[id]) ? 0 : 1;
			else return null;
		}
	}

	@Override
	protected void setDisplVar(int id, Object obj, boolean send) {
		switch(id) {
		case 0: try {
				sendPkt((byte)0, Float.parseFloat((String)obj));
				break;
			} catch (NumberFormatException e) { return; }
		case 2: sendPkt((byte)1); break;
		}
	}

}
