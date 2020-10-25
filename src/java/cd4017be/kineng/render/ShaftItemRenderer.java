package cd4017be.kineng.render;

import static net.minecraft.client.renderer.GlStateManager.*;
import java.util.*;
import java.util.function.Function;
import javax.vecmath.Matrix4f;
import org.apache.commons.lang3.tuple.Pair;
import com.google.common.collect.ImmutableList;
import cd4017be.kineng.block.BlockShaft;
import cd4017be.lib.render.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/** 
 * @author CD4017BE */
@SideOnly(Side.CLIENT)
public class ShaftItemRenderer extends TileEntityItemStackRenderer implements IHardCodedModel, IModel, IBakedModel {

	private final BlockShaft shaft;
	private final ResourceLocation parentName;
	private IBakedModel parent;

	public ShaftItemRenderer(Item item, BlockShaft block) {
		this.shaft = block;
		ResourceLocation loc = item.getRegistryName();
		this.parentName = new ResourceLocation(loc.getResourceDomain(), "item/" + loc.getResourcePath() + "_0");
		SpecialModelLoader.registerItemModel(item, this);
		ModelLoader.setCustomMeshDefinition(item, new SingleTextureDefinition(loc.toString()));
		ModelBakery.registerItemVariants(item, loc);
		item.setTileEntityItemStackRenderer(this);
	}

	@Override
	public void renderByItem(ItemStack stack, float t) {
		IBlockState state = shaft.getStateForPlacement(
			null, null, EnumFacing.NORTH, 0.5F, 0.5F, 0,
			stack.getItem().getMetadata(stack.getMetadata()), null
		);
		Minecraft mc = Minecraft.getMinecraft();
		translate(0.5F, 0.5F, 0.5F);
		if (shaft.getRenderType(state) == EnumBlockRenderType.MODEL)
			mc.getRenderItem().renderItem(stack, parent);
		double r = shaft.radius(state);
		if (r > 0.75) scale(r = 0.75 / r, r, r);
		Tessellator tess = Tessellator.getInstance();
		try (QuadBuilder qb = QuadBuilder.INSTANCE.init(tess.getBuffer(), true)) {
			int[] m = shaft.model(state);
			IPartModel.render(qb, 0, m);
			if (m.length >= 3) {
				m = new int[] {PartModels.SHAFT_CAPS, m[1], m[2]};
				IPartModel.render(qb, 0, m);
				m[2] = -m[2];
				IPartModel.render(qb, 0, m);
			}
		}
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		tess.draw();
	}

	@Override
	public Collection<ResourceLocation> getDependencies() {
		return ImmutableList.of(parentName);
	}

	@Override
	public IBakedModel bake(
		IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter
	) {
		IModel m = ModelLoaderRegistry.getModelOrLogError(parentName, "missing base model");
		this.parent = m.bake(state, format, bakedTextureGetter);
		return this;
	}

	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
		return parent.getQuads(state, side, rand);
	}

	@Override
	public boolean isAmbientOcclusion() {
		return parent.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return parent.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer() {
		return true;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return parent.getParticleTexture();
	}

	@Override
	public ItemOverrideList getOverrides() {
		return parent.getOverrides();
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(TransformType transform) {
		Pair<? extends IBakedModel, Matrix4f> p = parent.handlePerspective(transform);
		return Pair.of(this, p.getRight());
	}

	@Override
	public void onReload() {
		parent = null;
	}

}
