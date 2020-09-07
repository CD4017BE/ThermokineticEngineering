package cd4017be.kineng;

import static cd4017be.kineng.Objects.*;
import static cd4017be.kineng.block.BlockGear.DIAMETER;
import static net.minecraft.block.BlockDirectional.FACING;
import static cd4017be.kineng.block.BlockFillShared.ORIENT;
import static cd4017be.kineng.block.BlockFillDirected.HALF;
import static cd4017be.lib.BlockItemRegistry.registerRender;
import static cd4017be.lib.render.SpecialModelLoader.setMod;
import static net.minecraft.block.BlockRotatedPillar.AXIS;
import static net.minecraftforge.client.model.ModelLoader.setCustomStateMapper;
import static net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.kineng.render.ShaftRenderer;
import cd4017be.kineng.tileentity.ShaftPart;
import net.minecraft.client.renderer.block.statemap.StateMap;
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
		map.registerSprite(M_WOOD.texture = new ResourceLocation("blocks/planks_oak"));
		map.registerSprite(M_IRON.texture = new ResourceLocation("blocks/iron_block"));
		map.registerSprite(M_BEDROCK.texture = new ResourceLocation("blocks/bedrock"));
	}

	@SubscribeEvent
	public void registerModels(ModelRegistryEvent ev) {
		setMod(Main.ID);
		StateMap map = new StateMap.Builder().ignore(AXIS, DIAMETER, FACING, ORIENT, HALF).build();
		SHAFT_WOOD.setRenderType(EnumBlockRenderType.ENTITYBLOCK_ANIMATED);
		GEAR_WOOD.setRenderType(EnumBlockRenderType.ENTITYBLOCK_ANIMATED);
		SHAFT_DEBUG.setRenderType(EnumBlockRenderType.ENTITYBLOCK_ANIMATED);
		FILL_DIR.setRenderType(EnumBlockRenderType.ENTITYBLOCK_ANIMATED);
		FILL_SHARE.setRenderType(EnumBlockRenderType.ENTITYBLOCK_ANIMATED);
		setCustomStateMapper(SHAFT_WOOD, map);
		setCustomStateMapper(GEAR_WOOD, map);
		setCustomStateMapper(SHAFT_DEBUG, map);
		setCustomStateMapper(FILL_DIR, map);
		setCustomStateMapper(FILL_SHARE, map);
		registerRender(shaft_wood);
		registerRender(gear_wood, 1, 5);
		registerRender(shaft_debug);
	}

}
