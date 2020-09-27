package cd4017be.kineng;

import static cd4017be.kineng.Objects.*;
import static cd4017be.kineng.block.BlockGear.DIAMETER;
import static cd4017be.kineng.render.IPartModel.registerTexture;
import static net.minecraft.block.BlockDirectional.FACING;
import static cd4017be.kineng.block.BlockFillShared.ORIENT;
import static cd4017be.kineng.block.BlockFillDirected.HALF;
import static cd4017be.lib.BlockItemRegistry.registerRender;
import static cd4017be.lib.render.SpecialModelLoader.setMod;
import static net.minecraft.block.BlockRotatedPillar.AXIS;
import static net.minecraftforge.client.model.ModelLoader.setCustomStateMapper;
import static net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer;
import org.apache.commons.lang3.tuple.Pair;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.kineng.render.*;
import cd4017be.kineng.tileentity.ShaftPart;
import cd4017be.lib.block.AdvancedBlock;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** @author CD4017BE */
public class ClientProxy extends CommonProxy {

	@Override
	public void init(ConfigConstants cc) {
		super.init(cc);
		bindTileEntitySpecialRenderer(ShaftPart.class, new ShaftRenderer());
	}

	@SubscribeEvent
	public void registerTextures(TextureStitchEvent.Pre event) {
		TextureMap map = event.getMap();
		if (!"textures".equals(map.getBasePath())) return;
		for (Pair<ResourceLocation, TextureAtlasSprite> e : IPartModel.TEXTURES)
			map.registerSprite(e.getLeft());
	}

	@SubscribeEvent
	public void registerTextures(TextureStitchEvent.Post event) {
		TextureMap map = event.getMap();
		if (!"textures".equals(map.getBasePath())) return;
		for (Pair<ResourceLocation, TextureAtlasSprite> e : IPartModel.TEXTURES)
			e.setValue(map.getAtlasSprite(e.getLeft().toString()));
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent ev) {
		setMod(Main.ID);
		M_WOOD.texture = registerTexture(new ResourceLocation("blocks/planks_oak"));
		M_IRON.texture = registerTexture(new ResourceLocation("blocks/iron_block"));
		M_BEDROCK.texture = registerTexture(new ResourceLocation("blocks/bedrock"));
		int[]
		m = new int[] {PartModels.SHAFT, 0, 4};
		SHAFT_WOOD.model = m;
		SHAFT_IRON.model = m;
		SHAFT_DEBUG.model = m;
		m = new int[] {PartModels.GEAR, 0, 4, 0};
		GEAR_WOOD.model = m;
		GEAR_IRON.model = m;
		GRINDSTONE.model = new int[] {PartModels.WHEEL, 0, 4, registerTexture(new ResourceLocation("blocks/stone_granite")), 16, 10};
		SAWBLADE.model = new int[] {PartModels.WHEEL, 0, 4, registerTexture(new ResourceLocation("blocks/iron_block")), 16, 1};
		setShaftRender(
			SHAFT_WOOD, SHAFT_IRON, SHAFT_DEBUG,
			GEAR_WOOD, GEAR_IRON,
			GRINDSTONE, SAWBLADE,
			FILL_DIR, FILL_SHARE
		);
		registerRender(shaft_wood);
		registerRender(shaft_iron);
		registerRender(gear_wood, 1, 5);
		registerRender(gear_iron, 1, 5);
		registerRender(shaft_debug);
		registerRender(processing);
		registerRender(grindstone);
		registerRender(sawblade);
	}

	static final StateMap SHAFT_MAPPER = new StateMap.Builder().ignore(AXIS, DIAMETER, FACING, ORIENT, HALF).build();

	private static void setShaftRender(AdvancedBlock... blocks) {
		for (AdvancedBlock block : blocks) {
			block.setRenderType(EnumBlockRenderType.ENTITYBLOCK_ANIMATED);
			setCustomStateMapper(block, SHAFT_MAPPER);
		}
	}

}
