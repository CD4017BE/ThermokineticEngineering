package cd4017be.thermokin.render;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Function;

import cd4017be.lib.render.IHardCodedModel;
import cd4017be.lib.render.model.MultipartModel.IModelProvider;
import cd4017be.thermokin.module.Part;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;

/**
 * @author CD4017BE
 *
 */
public class CasingModel implements IModel, IHardCodedModel {

	private final IModelProvider provider;
	private final ItemHandler handler;

	public CasingModel() {
		provider = ModularModel.PROVIDERS[0];
		handler = new ItemHandler();
	}

	@Override
	public Collection<ResourceLocation> getDependencies() {
		return provider.getDependencies();
	}

	@Override
	public Collection<ResourceLocation> getTextures() {
		return Collections.emptyList();
	}

	@Override
	public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		provider.bake(state, format, bakedTextureGetter);
		return new Baked();
	}

	@Override
	public IModelState getDefaultState() {
		return ModelRotation.X0_Y0;
	}

	private class Baked implements IBakedModel {

		@Override
		public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
			return Collections.emptyList();
		}

		@Override
		public boolean isAmbientOcclusion() {
			return false;
		}

		@Override
		public boolean isGui3d() {
			return true;
		}

		@Override
		public boolean isBuiltInRenderer() {
			return false;
		}

		@Override
		public TextureAtlasSprite getParticleTexture() {
			return null;
		}

		@Override
		public ItemCameraTransforms getItemCameraTransforms() {
			return ItemCameraTransforms.DEFAULT;
		}

		@Override
		public ItemOverrideList getOverrides() {
			return handler;
		}

	}

	class ItemHandler extends ItemOverrideList {

		ItemHandler() {
			super(Collections.emptyList());
		}

		@Override
		public IBakedModel handleItemState(IBakedModel model, ItemStack stack, World world, EntityLivingBase entity) {
			Part p = Part.getPart(stack);
			if (p.modelId >= 0) return provider.getModelFor((byte)p.modelId);
			return model;
		}

	}

	@Override
	public void onReload() {
	}

}
