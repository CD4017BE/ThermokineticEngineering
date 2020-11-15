package cd4017be.kineng.render;

import static cd4017be.kineng.render.QuadBuilder.*;
import static cd4017be.kineng.tileentity.StorageLake.visitBlocks;
import cd4017be.kineng.tileentity.StorageLake;
import cd4017be.kineng.tileentity.StorageLake.BlockOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraftforge.client.model.animation.FastTESR;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;


/** 
 * @author CD4017BE */
public class LakeRenderer extends FastTESR<StorageLake> {

	@Override
	public void renderTileEntityFast(
		StorageLake te, double dx, double dy, double dz,
		float t, int destr, float partial, BufferBuilder buffer
	) {
		try (QuadBuilder qb = INSTANCE.setVB(buffer)) {
			float fill = te.partLevel();
			if (fill > 0) {
				qb.all(EY, (float)dy + (float)(te.level >> 1) + fill);
				drawLiquidLevel(te, qb, (float)dx, (float)dz);
			}
			int of = te.lastOverflow;
			if (of != 0) {
				drawOverflow(qb, 
					(float)dx + (float)(byte)of,
					(float)dy + (float)(of >> 8 & 0xff),
					(float)dz + (float)(byte)(of >> 16)
				);
			}
		}
	}

	private void drawLiquidLevel(StorageLake te, QuadBuilder qb, float x0, float z0) {
		int l = te.level;
		int i0 = l < 2 ? 0 : te.layers[l-2] & 0xffff;
		int i1 = te.layers[l] & 0xffff;
		byte[] map = te.blockMap;
		if (i0 >= i1 || i1 >= map.length) return;
		FluidStack stack = te.content;
		Fluid fluid = stack.getFluid();
		qb.sprite(Minecraft.getMinecraft().getTextureMapBlocks()
			.getAtlasSprite(fluid.getStill(stack).toString()
		)).uv(0, 0, 16, 16);
		qb.all(EC, fluid.getColor(stack));
		qb.all(ELN, 0xf00000 | fluid.getLuminosity() << 4);
		BlockOperation op = (m, x, y, z, w, p) -> {
			if ((m & 1) == 0) return m;
			qb.rect(EX, EZ, x0 + (float)x, z0 + (float)z, 1F, 1F).add();
			return m;
		};
		op.visit(1, 0, 0, 0, null, null);
		for (int r = 1; i0 < i1; r++)
			for (int b = 0; b < r && i0 < i1; b++, i0++)
				visitBlocks(map[i0], b, r, 0, 0, 0, null, null, op);
	}

	private void drawOverflow(QuadBuilder qb, float x, float y, float z) {
		qb.sprite(Minecraft.getMinecraft().getTextureMapBlocks()
			.getAtlasSprite("minecraft:white")).uv(0, 0, 16, 16);
		qb.all(EC, 0x3f3f3fff).all(ELN, 0xf00000);
		float d = 1.125F; x -= 0.0625F; y -= 0.0625F; z -= 0.0625F;
		qb.all(EX, x    ).rect(EZ, EY, z, y, d, d).add();
		qb.all(EX, x + d).rect(EZ, EY, z, y, d, d).add();
		qb.all(EY, y    ).rect(EX, EZ, x, z, d, d).add();
		qb.all(EY, y + d).rect(EX, EZ, x, z, d, d).add();
		qb.all(EZ, z    ).rect(EX, EY, x, y, d, d).add();
		qb.all(EZ, z + d).rect(EX, EY, x, y, d, d).add();
	}

	@Override
	public boolean isGlobalRenderer(StorageLake te) {
		return true;
	}

}
