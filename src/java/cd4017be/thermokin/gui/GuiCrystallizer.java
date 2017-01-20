package cd4017be.thermokin.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.util.Utils;
import cd4017be.thermokin.physics.LiquidState;
import cd4017be.thermokin.recipe.Converting.SolEntry;
import cd4017be.thermokin.tileentity.Crystallizer;

public class GuiCrystallizer extends GuiMachine {

	private final Crystallizer tile;

	public GuiCrystallizer(Crystallizer tile, EntityPlayer player) {
		super(new TileContainer(tile, player));
		this.tile = tile;
		this.MAIN_TEX = new ResourceLocation("thermokin", "textures/gui/crystallizer.png");
	}

	@Override
	public void initGui() {
		xSize = 176;
		ySize = 150;
		super.initGui();
		guiComps.add(new Tooltip(1, 116, 16, 34, 34, "liqAll"));
		guiComps.add(new Tooltip(2, 44, 16, 16, 34, "melt.stat"));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		mc.renderEngine.bindTexture(MAIN_TEX);
		LiquidState liq = tile.liq.liquid;
		if (liq.type != null) {
			int n = (int)(liq.V / liq.Vmax * 34D);
			color(liq.type.color | 0xdf000000);
			drawTexturedModalRect(guiLeft + 116, guiTop + 50 - n, 192, 0, 34, n);
		}
		liq = tile.rcp.liquid;
		if (liq.type != null) {
			int n = (int)(liq.V / liq.Vmax * 34D);
			color(liq.type.color | 0xff000000);
			drawTexturedModalRect(guiLeft + 44, guiTop + 50 - n, 176, 0, 16, n);
		}
	}

	@Override
	protected Object getDisplVar(int id) {
		if (id == 1) {
			LiquidState liq = tile.liq.liquid;
			return new Object[]{
				liq.type == null ? "" : liq.type.localizedName(),
				(float)liq.V, (float)liq.Vmax,
				(float)liq.T - 273.15F
			};
		} else if (id == 2) {
			SolEntry rcp = tile.rcp;
			return new Object[]{
				rcp.liquid.type == null ? "" : rcp.liquid.type.localizedName(),
				(float)rcp.liquid.V, (float)rcp.liquid.Vmax,
				Utils.formatNumber(rcp.liquid.V * rcp.dQ, 3, 0), Utils.formatNumber(rcp.liquid.Vmax * rcp.dQ, 3, 0),
				(float)rcp.liquid.T - 273.15F
			};
		} else return null;
	}

}
