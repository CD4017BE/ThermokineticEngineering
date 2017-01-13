package cd4017be.thermokin.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cd4017be.thermokin.tileentity.LiquidReservoir;
import cd4017be.thermokin.physics.LiquidState;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;

public class GuiLiquidReservoir extends GuiMachine {

	private final LiquidReservoir tile;

	public GuiLiquidReservoir(LiquidReservoir tile, EntityPlayer player) {
		super(new TileContainer(tile, player));
		this.tile = tile;
		this.MAIN_TEX = new ResourceLocation("automation", "textures/gui/evaporator.png");
	}

	@Override
	public void initGui() {
		xSize = 176;
		ySize = 150;
		super.initGui();
		guiComps.add(new Tooltip(2, 116, 16, 34, 34, "liqAll"));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		LiquidState liq = tile.liq.liquid;
		if (liq.type != null) {
			mc.renderEngine.bindTexture(MAIN_TEX);
			int n = (int)(liq.V / liq.Vmax * 34D);
			color(liq.type.color | 0xdf000000);
			drawTexturedModalRect(guiLeft + 116, guiTop + 50 - n, 184, 84, 34, n);
		}
	}

	@Override
	protected Object getDisplVar(int id) {
		if (id == 2) {
			LiquidState liq = tile.liq.liquid;
			return new Object[]{
				liq.type == null ? "" : liq.type.localizedName(),
				(float)liq.V, (float)liq.Vmax,
				(float)liq.T - 273.15F
			};
		} else return null;
	}

}
