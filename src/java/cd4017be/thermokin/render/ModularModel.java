package cd4017be.thermokin.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.Function;

import cd4017be.lib.util.Orientation;
import cd4017be.thermokin.block.BlockModularMachine;
import cd4017be.thermokin.module.Part;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
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
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;

public class ModularModel implements IModel {

	private static final ModelRotation[] ORIENTS = new ModelRotation[6];
	static {
		for(EnumFacing s : EnumFacing.values())
			ORIENTS[s.ordinal()] = Orientation.fromFacing(s).getModelRotation();
	}
	ResourceLocation particleLoc;
	ArrayList<ResourceLocation> parts = new ArrayList<ResourceLocation>();
	
	HashMap<short[], IBakedModel> cache = new HashMap<short[], IBakedModel>();

	private Baked baked;
	private final ItemModels items = new ItemModels();

	public void register(Part part, ResourceLocation model) {
		if (part.modelId >= 0)
			parts.set(part.modelId, model);
		else {
			part.modelId = parts.size();
			parts.add(model);
		}
	}

	@Override
	public Collection<ResourceLocation> getDependencies() {
		return parts;
	}

	@Override
	public Collection<ResourceLocation> getTextures() {
		return Arrays.asList(particleLoc);
	}

	@Override
	public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		if (baked == null) baked = new Baked(format, bakedTextureGetter);
		return baked;
	}

	@Override
	public IModelState getDefaultState() {
		return ModelRotation.X0_Y0;
	}

	class Baked implements IBakedModel {

		final TextureAtlasSprite particleTex;
		final IBakedModel[] models;

		Baked(VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
			particleTex = bakedTextureGetter.apply(particleLoc);
			models = new IBakedModel[parts.size() * 6];
			int n = 0;
			for (ResourceLocation loc : parts) {
				IModel model = ModelLoaderRegistry.getModelOrMissing(loc);
				for (ModelRotation rot : ORIENTS)
					models[n++] = model.bake(rot, format, bakedTextureGetter);
			}
		}

		@Override
		public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
			IExtendedBlockState eState = (IExtendedBlockState)state;
			ArrayList<BakedQuad> list = new ArrayList<BakedQuad>();
			if (side == null && eState.getValue(BlockModularMachine.opaque_prop)) return list;
			short[] arr = eState.getValue(BlockModularMachine.parts_prop);
			if (arr != null)
				for (short s : arr)
					if (s >= 0 && s < models.length)
						list.addAll(models[s].getQuads(state, side, rand));
			return list;
		}

		@Override
		public boolean isAmbientOcclusion() {
			return false;
		}

		@Override
		public boolean isGui3d() {
			return false;
		}

		@Override
		public boolean isBuiltInRenderer() {
			return false;
		}

		@Override
		public TextureAtlasSprite getParticleTexture() {
			return particleTex;
		}

		@Override
		public ItemCameraTransforms getItemCameraTransforms() {
			return ItemCameraTransforms.DEFAULT;
		}

		@Override
		public ItemOverrideList getOverrides() {
			return items;
		}
		
	}

	class ItemModels extends ItemOverrideList {

		public ItemModels() {
			super(Collections.emptyList());
		}

		@Override
		public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
			
			return super.handleItemState(originalModel, stack, world, entity);
		}

	}

	class CachedModel extends Baked {

		private final BakedQuad[][] quads = new BakedQuad[7][];

		@Override
		public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
			int i = side == null ? 0 : side.getIndex() + 1;
			return quads[i] == null ? Collections.<BakedQuad>emptyList() : Arrays.asList(quads[i]);
		}

	}

}
