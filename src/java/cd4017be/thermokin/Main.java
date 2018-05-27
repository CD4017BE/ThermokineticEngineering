package cd4017be.thermokin;

import cd4017be.api.heat.HeatSimulation;
import cd4017be.api.recipes.RecipeScriptContext;
import cd4017be.api.registry.Environment;
import cd4017be.api.registry.PartRegistry;
import cd4017be.api.registry.ThermodynamicProperties;
import cd4017be.lib.script.ScriptFiles.Version;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * 
 * @author CD4017BE
 */
@Mod(modid = Main.ID, useMetadata = true)
public class Main {

	public static final String ID = "thermokin";
	static final String ConfigName = "thermokinEngin";

	@Instance
	public static Main instance;

	@SidedProxy(serverSide = "cd4017be." + ID + ".CommonProxy", clientSide = "cd4017be." + ID + ".ClientProxy")
	public static CommonProxy proxy;

	public Main() {
		RecipeScriptContext.scriptRegistry.add(new Version(ConfigName, 4, "/assets/" + ID + "/config/recipes.rcp"));
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		Objects.init();
		PartRegistry.init();
		ThermodynamicProperties.register();
		RecipeScriptContext.instance.run(ConfigName + ".PRE_INIT");
		proxy.init();
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		proxy.registerRenderers();
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		Environment.makeDefEnv();
		HeatSimulation.register();
	}

}
