package cd4017be.thermokin;

import cd4017be.api.recipes.RecipeAPI;
import cd4017be.lib.ConfigurationFile;
import cd4017be.thermokin.recipe.Converting;
import cd4017be.thermokin.recipe.Substances;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
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
		proxy.init();
		RecipeAPI.Handlers.put(Substances.SUBST, Substances.instance);
		RecipeAPI.Handlers.put(Substances.ENV, Substances.instance);
		RecipeAPI.Handlers.put(Converting.LIQ, Converting.instance);
		RecipeAPI.Handlers.put(Converting.SOL, Converting.instance);
		RecipeAPI.registerScript(event, "thermokinetic.rcp", "/assets/thermokin/config/recipes.rcp");
	}

	@Mod.EventHandler
	public void load(FMLInitializationEvent event) {
		proxy.registerRenderers();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		
	}

}
