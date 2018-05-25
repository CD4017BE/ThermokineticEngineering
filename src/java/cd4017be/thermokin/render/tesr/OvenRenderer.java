package cd4017be.thermokin.render.tesr;

import cd4017be.thermokin.tileentity.SolidFuelOven;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;


/**
 * @author CD4017BE
 *
 */
public class OvenRenderer extends TileEntitySpecialRenderer<SolidFuelOven> {

	@Override
	public void renderTileEntityAt(SolidFuelOven te, double x, double y, double z, float partialTicks, int destroyStage) {
		GaugeRenderer.renderTemp(te.heat, x + 0.5, y + 0.5, z + 1.001, 0.5);
	}

}
