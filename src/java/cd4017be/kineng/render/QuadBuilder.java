package cd4017be.kineng.render;

import static java.lang.Float.floatToIntBits;
import static java.lang.Float.intBitsToFloat;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 
 * @author CD4017BE */
@SideOnly(Side.CLIENT)
public class QuadBuilder implements AutoCloseable {

	/** vertex indices */
	public static final int VA = 0, VB = 7, VC = 14, VD = 21;
	/** element indices */
	public static final int EX = 0, EY = 1, EZ = 2, EC = 3, EU = 4, EV = 5, ELN = 6;

	public static final QuadBuilder INSTANCE = new QuadBuilder();

	public final int[] vertexData = new int[28];
	public BufferBuilder vb;
	public TextureAtlasSprite texture;
	public int i;
	public boolean calcNormals;

	public QuadBuilder setVB(BufferBuilder vb) {
		this.vb = vb;
		return this;
	}

	public QuadBuilder init(BufferBuilder vb, boolean item) {
		vb.begin(GL11.GL_QUADS, item ? DefaultVertexFormats.ITEM : DefaultVertexFormats.BLOCK);
		this.calcNormals = item;
		return setVB(vb);
	}

	public QuadBuilder sprite(TextureAtlasSprite texture) {
		this.texture = texture;
		return this;
	}

	public QuadBuilder add() {
		if (calcNormals) calcNormals();
		vb.addVertexData(vertexData);
		i = 0;
		return this;
	}

	public void calcNormals() {
		float ux = intBitsToFloat(vertexData[VC+EX]) - intBitsToFloat(vertexData[VA+EX]);
		float uy = intBitsToFloat(vertexData[VC+EY]) - intBitsToFloat(vertexData[VA+EY]);
		float uz = intBitsToFloat(vertexData[VC+EZ]) - intBitsToFloat(vertexData[VA+EZ]);
		float vx = intBitsToFloat(vertexData[VD+EX]) - intBitsToFloat(vertexData[VB+EX]);
		float vy = intBitsToFloat(vertexData[VD+EY]) - intBitsToFloat(vertexData[VB+EY]);
		float vz = intBitsToFloat(vertexData[VD+EZ]) - intBitsToFloat(vertexData[VB+EZ]);
		float nx = uy * vz - uz * vy, ny = uz * vx - ux * vz, nz = ux * vy - uy * vx;
		float r = (float)MathHelper.fastInvSqrt(nx * nx + ny * ny + nz * nz) * 127F;
		vertexData[VD+ELN] = vertexData[VC+ELN] = vertexData[VB+ELN] = vertexData[VA+ELN] =
		(int)(nx * r) & 0xff | (int)(ny * r) << 8 & 0xff00 | (int)(nz * r) << 16 & 0xff0000;
	}

	public BakedQuad build(int tintIdx, EnumFacing face, boolean diffuseLight) {
		return new BakedQuad(vertexData.clone(), tintIdx, face, texture, diffuseLight, DefaultVertexFormats.BLOCK);
	}

	public QuadBuilder next() {
		if ((i += 7) >= 28) return add();
		return this;
	}

	public QuadBuilder xyz(float x, float y, float z) {
		return xyz(i, x, y, z);
	}

	public QuadBuilder xyz(int i, float x, float y, float z) {
		vertexData[i+EX] = floatToIntBits(x);
		vertexData[i+EY] = floatToIntBits(y);
		vertexData[i+EZ] = floatToIntBits(z);
		return this;
	}

	public QuadBuilder rect(int ex, int ey, float x, float y, float w, float h) {
		vertexData[VD+ex] = vertexData[VA+ex] = floatToIntBits(x);
		vertexData[VB+ey] = vertexData[VA+ey] = floatToIntBits(y);
		vertexData[VC+ex] = vertexData[VB+ex] = floatToIntBits(x + w);
		vertexData[VC+ey] = vertexData[VD+ey] = floatToIntBits(y + h);
		return this;
	}

	public QuadBuilder uv(float u, float v) {
		return uv(i, u, v);
	}

	public QuadBuilder uv(int i, float u, float v) {
		if (texture != null) {
			u = texture.getInterpolatedU(u);
			v = texture.getInterpolatedV(v);
		}
		vertexData[i+EU] = floatToIntBits(u);
		vertexData[i+EV] = floatToIntBits(v);
		return this;
	}

	public QuadBuilder uv(float u0, float v0, float u1, float v1) {
		if (texture != null) {
			u0 = texture.getInterpolatedU(u0);
			v0 = texture.getInterpolatedV(v0);
			u1 = texture.getInterpolatedU(u1);
			v1 = texture.getInterpolatedV(v1);
		}
		vertexData[VD+EU] = vertexData[VA+EU] = floatToIntBits(u0);
		vertexData[VC+EU] = vertexData[VB+EU] = floatToIntBits(u1);
		vertexData[VB+EV] = vertexData[VA+EV] = floatToIntBits(v0);
		vertexData[VC+EV] = vertexData[VD+EV] = floatToIntBits(v1);
		return this;
	}

	public QuadBuilder argb(int argb) {
		vertexData[i+EC] = argb;
		return this;
	}

	public QuadBuilder light(int l) {
		vertexData[i+ELN] = l;
		return this;
	}

	public QuadBuilder set(int i, float val) {
		return set(i, floatToIntBits(val));
	}

	public QuadBuilder set(int i, int val) {
		vertexData[i] = val;
		return this;
	}

	public QuadBuilder all(int i, float val) {
		return all(i, floatToIntBits(val));
	}

	public QuadBuilder all(int i, int val) {
		vertexData[VD+i] =
		vertexData[VC+i] =
		vertexData[VB+i] =
		vertexData[VA+i] = val;
		this.i = 0;
		return this;
	}

	@Override
	public void close() {
		vb = null;
		texture = null;
		calcNormals = false;
		i = 0;
	}

}
