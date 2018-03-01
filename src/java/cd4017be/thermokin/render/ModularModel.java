package cd4017be.thermokin.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import cd4017be.lib.block.MultipartBlock;
import cd4017be.lib.property.PropertyByte;
import cd4017be.lib.render.model.BakedModel;
import cd4017be.lib.render.model.MultipartModel;
import cd4017be.lib.render.model.RawModelData;
import cd4017be.lib.util.Orientation;
import cd4017be.thermokin.Main;
import cd4017be.thermokin.module.Part;
import cd4017be.thermokin.module.Part.Type;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
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


/**
 * @author CD4017BE
 *
 */
public class ModularModel extends MultipartModel {

	public static IModelProvider[] PROVIDERS = new IModelProvider[12];
	public static ItemHandler ITEM_HANDLER = new ItemHandler();
	static {
		for (EnumFacing s : EnumFacing.values()) {
			PROVIDERS[s.ordinal()] = new ModelProviderCasing(s);
			PROVIDERS[s.ordinal() + 6] = new ModelProviderModule(s);
		}
	}

	public static void register(Part part, ResourceLocation model) {
		if (part.modelId >= 0) return;
		if (model == null) {
			part.modelId = -1;
		} else if (part.type == Type.CASING) {
			part.modelId = ModelProviderCasing.textures.size();
			ModelProviderCasing.textures.add(model);
		} else if (part.type == Type.MODULE) {
			part.modelId = ModelProviderModule.models.size();
			ModelProviderModule.models.add(model);
		}
	}

	/**
	 * @param block
	 */
	public ModularModel(MultipartBlock block) {
		super(block);
		System.arraycopy(PROVIDERS, 0, modelProvider, 0, PROVIDERS.length);
		itemHandler = ITEM_HANDLER;
	}

	static abstract class ModelProviderSided implements IModelProvider {

		protected IBakedModel[] baked;
		protected final Orientation side;

		/**
		 * @param face
		 */
		ModelProviderSided(EnumFacing face) {
			this.side = Orientation.fromFacing(face);
		}

		@Override
		public IBakedModel getModelFor(Object val) {
			int id = ((Byte)val).intValue() & 0xff;
			return id < baked.length ? baked[id] : null;
		}

		@Override
		public void bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> textureGetter) {
			if (baked != null) return;
			Collection<ResourceLocation> locs = getDependencies();
			baked = new IBakedModel[locs.size()];
			int i = 0;
			for (ResourceLocation loc : locs) {
				IModel model = ModelLoaderRegistry.getModelOrLogError(loc, "missing");
				baked[i++] = model.bake(model.getDefaultState(), format, textureGetter);
			}
		}

	}

	static class ModelProviderCasing extends ModelProviderSided {

		static final ArrayList<ResourceLocation> textures = new ArrayList<ResourceLocation>();
		static final ResourceLocation mainModel = new ResourceLocation(Main.ID, "block/.casing");

		/**
		 * @param face
		 */
		ModelProviderCasing(EnumFacing face) {
			super(face);
		}

		@Override
		public Collection<ResourceLocation> getDependencies() {
			ArrayList<ResourceLocation> modelLocs = new ArrayList<ResourceLocation>(textures.size());
			for (ResourceLocation tex : textures)
				modelLocs.add(new ResourceLocation(mainModel + "#" + side.getName() + "$" + tex));
			return modelLocs;
		}

	}

	static class ModelProviderModule extends ModelProviderSided {

		static final ArrayList<ResourceLocation> models = new ArrayList<ResourceLocation>();

		/**
		 * @param face
		 */
		ModelProviderModule(EnumFacing face) {
			super(face);
		}

		@Override
		public Collection<ResourceLocation> getDependencies() {
			ArrayList<ResourceLocation> modelLocs = new ArrayList<ResourceLocation>(models.size());
			for (ResourceLocation loc : models)
				modelLocs.add(new ResourceLocation(loc + "#" + side.getName()));
			return modelLocs;
		}

	}

	static class ItemHandler extends ItemOverrideList {

		Cache<int[], BakedModel> modelCache = CacheBuilder.newBuilder().maximumSize(100).expireAfterAccess(5, TimeUnit.MINUTES).build();

		ItemHandler() {
			super(Collections.emptyList());
		}

		@Override
		public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
			if (stack.hasTagCompound()) {
				int[] comps = stack.getTagCompound().getIntArray("comp");
				try {
					return modelCache.get(comps, ()-> generate((BakedMultipart)originalModel, stack, comps));
				} catch (ExecutionException e) {e.printStackTrace();}
			}
			return originalModel;
		}

		BakedModel generate(BakedMultipart model, ItemStack item, int[] comps) {
			int i = item.getItem().getMetadata(item.getItemDamage());
			IBakedModel base = model.base[i];
			BakedModel result = new BakedModel(base.getParticleTexture(), RawModelData.DEFAULT_TRANSFORM, base.isAmbientOcclusion(), base.isGui3d());
			MultipartBlock block = model.getOwner();
			IExtendedBlockState state = (IExtendedBlockState) block.getDefaultState().withProperty(model.getOwner().baseState, i);
			for (int j = 0; j < block.modules.length; j++) {
				Part p = Part.getPart(Type.forSlot(j), comps[j]);
				state = state.withProperty(block.modules[j], PropertyByte.cast(p.modelId));
			}
			result.quads[0] = model.getQuads(state, null, 0);
			for (EnumFacing f : EnumFacing.values())
				result.quads[f.ordinal() + 1] = model.getQuads(state, f, 0);
			return result;
		}

	}

}
