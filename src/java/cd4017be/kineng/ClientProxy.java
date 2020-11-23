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
import cd4017be.kineng.physics.ShaftRenderInfo;
import cd4017be.kineng.render.*;
import cd4017be.kineng.tileentity.*;
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
		
		ShaftRenderInfo.LIGHT_UPDATE_INTERVAL = (int)cc.getNumber("shaft_light_updates", 20);
		bindTileEntitySpecialRenderer(ShaftPart.class, new ShaftRenderer<>());
		bindTileEntitySpecialRenderer(StorageLake.class, new LakeRenderer());
		bindTileEntitySpecialRenderer(Gear.class, new ChainRenderer());
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
		M_STONE.texture = registerTexture(new ResourceLocation("blocks/stone_granite"));
		M_BEDROCK.texture = registerTexture(new ResourceLocation("blocks/bedrock"));
		int[]
		m = new int[] {PartModels.SHAFT, 0, 4};
		SHAFT_WOOD.model = m;
		SHAFT_IRON.model = m;
		SHAFT_DEBUG.model = m;
		TRANSDUCER.model = m;
		SHAFT_MAN.model = new int[] {PartModels.MANUAL, 0, 4, 24};
		m = new int[] {PartModels.GEAR, 0, 4, 0};
		GEAR_WOOD.model = m;
		GEAR_IRON.model = m;
		int t = M_STONE.texture;
		GRINDSTONE.model = new int[] {PartModels.WHEEL, 0, 4, t, 16, 10, t};
		t = registerTexture(new ResourceLocation("blocks/iron_block"));
		SAWBLADE.model = new int[] {PartModels.WHEEL, 0, 4, t, 16, 1, t};
		t = M_WOOD.texture;
		LATHE.model = new int[] {PartModels.WHEEL, 0, 4, t, 8, 8, t};
		PRESS.model = new int[] {PartModels.SHAFT, 0, 4};
		t = registerTexture(new ResourceLocation(Main.ID, "blocks/magnets"));
		int t1 = registerTexture(new ResourceLocation(Main.ID, "blocks/magnets1"));
		MAGNETS.model = new int[] {PartModels.WHEEL, 0, 4, t, 8, 10, t1};
		WATER_WHEEL.model = new int[] {PartModels.WATER_WHEEL, 0, 4, 40, 56};
		t = registerTexture(new ResourceLocation("blocks/wool_colored_white"));
		WIND_MILL.bladeTex = new int[] {0, t, t, M_IRON.texture, M_IRON.texture, M_IRON.texture};
		WIND_MILL.model = new int[] {PartModels.TURBINE, 0, 4, 0, 0, 16};
		t = registerTexture(new ResourceLocation(Main.ID, "blocks/tachometer"));
		t1 = registerTexture(new ResourceLocation(Main.ID, "blocks/tachometer1"));
		TACHOMETER.model = new int[] {PartModels.WHEEL, 0, 4, t, 8, 1, t1};
		t = registerTexture(new ResourceLocation(Main.ID, "blocks/transducer"));
		TRANSDUCER.setModel(PartModels.SHAFT, t, 4);
		//TODO mobgrinder model
		MOB_GRINDER.model = new int[] {PartModels.MANUAL, 0, 4, 32};
		setShaftRender(
			SHAFT_WOOD, SHAFT_IRON, SHAFT_DEBUG, SHAFT_MAN,
			GEAR_WOOD, GEAR_IRON,
			GRINDSTONE, SAWBLADE, LATHE, PRESS, MAGNETS,
			WATER_WHEEL, WIND_MILL,
			FILL_DIR, FILL_SHARE,
			TACHOMETER, TRANSDUCER,
			WIND_MILL, MOB_GRINDER
		);
		new ShaftItemRenderer(shaft_wood, SHAFT_WOOD);
		new ShaftItemRenderer(shaft_iron, SHAFT_IRON);
		new ShaftItemRenderer(gear_wood, GEAR_WOOD);
		new ShaftItemRenderer(gear_iron, GEAR_IRON);
		new ShaftItemRenderer(shaft_debug, SHAFT_DEBUG);
		new ShaftItemRenderer(shaft_man, SHAFT_MAN);
		new ShaftItemRenderer(grindstone, GRINDSTONE);
		new ShaftItemRenderer(sawblade, SAWBLADE);
		new ShaftItemRenderer(lathe, LATHE);
		new ShaftItemRenderer(press, PRESS);
		new ShaftItemRenderer(magnets, MAGNETS);
		new ShaftItemRenderer(water_wheel, WATER_WHEEL);
		new ShaftItemRenderer(wind_mill, WIND_MILL);
		new ShaftItemRenderer(tachometer, TACHOMETER);
		new ShaftItemRenderer(transducer, TRANSDUCER);
		new ShaftItemRenderer(mob_grinder, MOB_GRINDER);
		registerRender(processing);
		registerRender(rf_coil);
		registerRender(flint_knife);
		registerRender(lake);
		registerRender(lake_valve);
		registerRender(lake_gate);
		registerRender(anemometer);
		registerRender(chain);
	}

	static final StateMap SHAFT_MAPPER = new StateMap.Builder().ignore(AXIS, DIAMETER, FACING, ORIENT, HALF).build();

	private static void setShaftRender(AdvancedBlock... blocks) {
		for (AdvancedBlock block : blocks) {
			block.setRenderType(EnumBlockRenderType.ENTITYBLOCK_ANIMATED);
			setCustomStateMapper(block, SHAFT_MAPPER);
		}
	}

}
