package cd4017be.kineng.physics;

import static net.minecraft.util.EnumFacing.getFacingFromAxis;
import static net.minecraft.util.EnumFacing.AxisDirection.*;
import java.util.function.Function;
import org.lwjgl.opengl.GL11;
import cd4017be.lib.render.Util;
import cd4017be.lib.render.model.IntArrayModel;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author CD4017BE
 *
 */
public class ShaftRenderInfo {

	public static int LIGHT_UPDATE_INTERVAL = 20;

	public int lastFrame, t;
	int[] vertexCache, partPtrs;
	public BlockPos origin;
	
	public void invalidate() {
		vertexCache = null;
	}

	public void draw(ShaftAxis shaft, Function<String, IntArrayModel> models) {
		lastFrame = Util.RenderFrame;
		BufferBuilder vb = Tessellator.getInstance().getBuffer();
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		if (vertexCache != null) {
			if (--t <= 0) updateLight(shaft);
			vb.addVertexData(vertexCache);
			return;
		}
		if (shaft.parts.isEmpty()) return;
		IShaftPart part = shaft.parts.get(0);
		origin = ((TileEntity)part).getPos();
		int l = shaft.parts.size();
		partPtrs = new int[l + 2];
		for (int i = 0; i < l; i++) {
			part = shaft.parts.get(i);
			TileEntity te = (TileEntity)part;
			World world = te.getWorld();
			BlockPos pos = te.getPos();
			if (i == 0) partPtrs[0] = drawModel(
				vb, models.apply(part.capModel(false)), world,
				pos.offset(getFacingFromAxis(NEGATIVE, part.axis())), i
			);
			partPtrs[i + 1] = drawModel(vb, models.apply(part.model()), world, pos, i);
			if (i == l - 1) partPtrs[l + 1] = drawModel(
				vb, models.apply(part.capModel(true)), world,
				pos.offset(getFacingFromAxis(POSITIVE, part.axis())), i
			);
		}
		vertexCache = Util.extractData(vb, 0, vb.getVertexCount());
		t = LIGHT_UPDATE_INTERVAL;
	}

	private void updateLight(ShaftAxis shaft) {
		t = LIGHT_UPDATE_INTERVAL;
		if (shaft.parts.isEmpty()) return;
		IShaftPart part = shaft.parts.get(0);
		World world = ((TileEntity)part).getWorld();
		EnumFacing dir = getFacingFromAxis(POSITIVE, part.axis());
		for (int j = 6, i = 0; i < partPtrs.length; i++) {
			int l = world.getCombinedLight(origin.offset(dir, i - 1), 0);
			for (int p = partPtrs[i]; j < p; j+=7)
				vertexCache[j] = l;
		}
	}

	private static int drawModel(BufferBuilder vb, IntArrayModel m, World world, BlockPos pos, int i) {
		if (m != null) {
			m.setBrightness(world.getCombinedLight(pos, 0));
			m.setOffset(i, Axis.Z);
			vb.addVertexData(m.vertexData);
		}
		return vb.getVertexCount() * 7;
	}

}
