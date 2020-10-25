package cd4017be.kineng.physics;

import static cd4017be.kineng.render.IPartModel.render;
import static cd4017be.kineng.render.QuadBuilder.ELN;
import static net.minecraft.util.EnumFacing.getFacingFromAxis;
import static net.minecraft.util.EnumFacing.AxisDirection.*;
import cd4017be.kineng.render.QuadBuilder;
import cd4017be.lib.render.Util;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
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

	public void draw(ShaftAxis shaft, QuadBuilder qb) {
		lastFrame = Util.RenderFrame;
		if (vertexCache != null) {
			if (--t <= 0) updateLight(shaft);
			qb.vb.addVertexData(vertexCache);
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
			if (i == 0) partPtrs[0] = render(
				qb.all(ELN, world.getCombinedLight(pos.offset(getFacingFromAxis(NEGATIVE, part.axis())), 0)),
				i, part.capModel(false)
			);
			partPtrs[i + 1] = render(qb.all(ELN, world.getCombinedLight(pos, 0)), i, part.model());
			if (i == l - 1) partPtrs[l + 1] = render(
				qb.all(ELN, world.getCombinedLight(pos.offset(getFacingFromAxis(POSITIVE, part.axis())), 0)),
				i, part.capModel(true)
			);
		}
		vertexCache = Util.extractData(qb.vb, 0, qb.vb.getVertexCount());
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

}
