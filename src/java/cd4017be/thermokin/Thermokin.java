package cd4017be.thermokin;

import org.apache.logging.log4j.Level;

import cd4017be.api.recipes.RecipeAPI;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.ConfigurationFile;
import cd4017be.thermokin.physics.Substance;
import cd4017be.thermokin.physics.ThermodynamicUtil;
import cd4017be.thermokin.recipe.Converting;
import cd4017be.thermokin.recipe.Substances;
import cd4017be.thermokin.recipe.Substances.Environment;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.SidedProxy;

@Mod(modid = "thermokin", useMetadata = true)
public class Thermokin {

	@Instance("thermokin")
	public static Thermokin instance;

	@SidedProxy(clientSide="cd4017be.thermokin.ClientProxy", serverSide="cd4017be.thermokin.CommonProxy")
	public static CommonProxy proxy;

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Config.loadConfig(ConfigurationFile.init(event, "thermokinetic.cfg", "/assets/thermokin/config/preset.cfg", true));
		Objects.init();
		BlockGuiHandler.registerMod(this);
		proxy.init();
		RecipeAPI.Handlers.put(Substances.SUBST, Substances.instance);
		RecipeAPI.Handlers.put(Substances.ENV, Substances.instance);
		RecipeAPI.Handlers.put(Substances.BLOCK, Substances.instance);
		RecipeAPI.Handlers.put(Converting.LIQ, Converting.instance);
		RecipeAPI.Handlers.put(Converting.SOL, Converting.instance);
		RecipeAPI.registerScript(event, "thermokinetic.rcp", "/assets/thermokin/config/recipes.rcp");
	}

	@Mod.EventHandler
	public void load(FMLInitializationEvent event) {
		proxy.registerRenderers();
	}

	private static final ResourceLocation fallbackSubstance = new ResourceLocation("thermokin:air");

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		Config.postInit();
		if (Substances.defaultEnv == null) {
			FMLLog.log("thermokin", Level.ERROR, "A default Environment is missing! Please check your config file, it has probably crashed or is missing the folowing entry:\nadd(\"environment\", nil, \"thermokin:air\", 101250, 270, 25, 0.8);");
			Substance s = Substance.REGISTRY.getObject(fallbackSubstance);
			if (s == null) GameRegistry.register(s = new Substance("air").setRegistryName(fallbackSubstance));
			Substances.defaultEnv = new Environment(s, ThermodynamicUtil.Pn, 270, 25, 0.8);
		}
		Substance.Default = Substances.defaultEnv.type;
	}

}
