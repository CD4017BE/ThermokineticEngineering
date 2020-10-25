package cd4017be.kineng.render;

import java.util.ArrayList;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** 
 * @author CD4017BE */
@SideOnly(Side.CLIENT)
public interface IPartModel {

	void render(QuadBuilder qb, int[] args, float ofsZ);

	static int render(QuadBuilder qb, float ofsZ, int[] args) {
		if (args != null && args.length != 0)
			MODELS.get(args[0]).render(qb, args, ofsZ);
		return qb.vb.getVertexCount() * 7;
	}

	static QuadBuilder texture(QuadBuilder qb, int tex) {
		return qb.sprite(TEXTURES.get(tex).getRight());
	}

	static int registerModel(IPartModel model) {
		int i = MODELS.size();
		MODELS.add(model);
		return i;
	}

	static int registerTexture(ResourceLocation id) {
		int i = TEXTURES.size();
		for (int j = 0; j < i; j++)
			if (TEXTURES.get(j).getLeft().equals(id))
				return j;
		TEXTURES.add(MutablePair.of(id, null));
		return i;
	}

	final ArrayList<IPartModel> MODELS = new ArrayList<>();
	final ArrayList<Pair<ResourceLocation, TextureAtlasSprite>> TEXTURES = new ArrayList<>();

}
