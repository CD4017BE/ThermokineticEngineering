package cd4017be.thermokin.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import cd4017be.lib.Gui.GuiMachine;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.util.Utils;
import cd4017be.thermokin.physics.LiquidState;
import cd4017be.thermokin.physics.Substance;
import cd4017be.thermokin.tileentity.HydraulicPress;

public class GuiHydraulicPress extends GuiMachine {

	public GuiHydraulicPress(HydraulicPress tileEntity, EntityPlayer player) {
		super(new TileContainer(tileEntity, player));
		this.MAIN_TEX = new ResourceLocation("thermokin", "textures/gui/hydraulicPress.png");
	}

	@Override
	public void initGui() {
		xSize = 176;
		ySize = 150;
		super.initGui();
		final int[] data = ((TileContainer)inventorySlots).refInts;
		guiComps.add(new Tooltip<Object[]>(1, 71, 16, 34, 34, "press.state", ()-> {
			LiquidState s = getLiquid();
			return new Object[]{
				s.type == null ? "Empty" : s.type.localizedName(), s.V, s.Vmax,
				Utils.formatNumber(Float.intBitsToFloat(data[3]), 4, 0),
				Utils.formatNumber(Float.intBitsToFloat(data[4]), 4, 0),
			};
		}));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		LiquidState liq = getLiquid();
		mc.renderEngine.bindTexture(MAIN_TEX);
		int n = (int)(liq.V / liq.Vmax * 15D);
		if (liq.type != null) color(liq.type.color | 0xdf000000);
		drawTexturedModalRect(guiLeft + 71, guiTop + 16, 176, 15 - n, 34, 34);
	}

	private LiquidState getLiquid() {
		int[] data = ((TileContainer)inventorySlots).refInts;
		return new LiquidState(Substance.REGISTRY.getObjectById(data[0]),
			Float.intBitsToFloat(data[2]), Float.intBitsToFloat(data[1]), 0);
	}

}
