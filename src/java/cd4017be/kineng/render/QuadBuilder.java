package cd4017be.kineng.render;

import static java.lang.Float.floatToIntBits;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 
 * @author CD4017BE */
@SideOnly(Side.CLIENT)
public class QuadBuilder implements AutoCloseable {

	/** vertex indices */
	public static final int VA = 0, VB = 7, VC = 14, VD = 21;
	/** element indices */
	public static final int EX = 0, EY = 1, EZ = 2, EC = 3, EU = 4, EV = 5, EL = 6;

	public static final QuadBuilder INSTANCE = new QuadBuilder();

	public final int[] vertexData = new int[28];
	public BufferBuilder vb;
	public TextureAtlasSprite texture;
	public int i;

	public QuadBuilder init(BufferBuilder vb) {
		vb.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		this.vb = vb;
		return this;
	}

	public QuadBuilder sprite(TextureAtlasSprite texture) {
		this.texture = texture;
		return this;
	}

	public QuadBuilder add() {
		vb.addVertexData(vertexData);
		i = 0;
		return this;
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
		vertexData[i+EL] = l;
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
		i = 0;
	}

}
