package cd4017be.kineng;

import org.apache.logging.log4j.Logger;
import cd4017be.api.recipes.RecipeScriptContext;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.kineng.capability.StructureLocations;
import cd4017be.kineng.physics.Ticking;
import cd4017be.lib.script.ScriptFiles.Version;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;

/** @author CD4017BE */
@Mod(modid = Main.ID, useMetadata = true)
public class Main {

	public static final String ID = "kineng";

	@Instance(ID)
	public static Main instance;

	public static Logger LOG;

	@SidedProxy(clientSide = "cd4017be." + ID + ".ClientProxy", serverSide = "cd4017be." + ID + ".CommonProxy")
	public static CommonProxy proxy;

	public Main() {
		RecipeScriptContext.scriptRegistry.add(new Version("kinetic", "/assets/" + ID + "/config/recipes.rcp"));
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		LOG = event.getModLog();
		proxy.preInit();
		StructureLocations.register();
		RecipeScriptContext.instance.run("kinetic.PRE_INIT");
	}

	@Mod.EventHandler
	public void load(FMLInitializationEvent event) {
		Objects.init();
		proxy.init(new ConfigConstants(RecipeScriptContext.instance.modules.get("kinetic")));
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		Ticking.init();
	}

	@Mod.EventHandler
	public void onShutdown(FMLServerStoppingEvent event) {
		Ticking.clear();
	}

}
