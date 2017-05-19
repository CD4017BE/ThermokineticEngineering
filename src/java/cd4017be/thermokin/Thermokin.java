package cd4017be.thermokin;

import org.apache.logging.log4j.Level;

import cd4017be.api.recipes.RecipeScriptContext;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.lib.BlockGuiHandler;
import cd4017be.lib.script.ScriptFiles.Version;
import cd4017be.thermokin.physics.Substance;
import cd4017be.thermokin.physics.ThermodynamicUtil;
import cd4017be.thermokin.recipe.Converting;
import cd4017be.thermokin.recipe.ShaftMounts;
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

	public Thermokin() {
		RecipeScriptContext.scriptRegistry.add(new Version("thermokinetic", 2, "/assets/thermokin/config/recipes.rcp"));
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ConfigConstants cfg = new ConfigConstants(RecipeScriptContext.instance.modules.get("thermokinetic"));
		Objects.init();
		BlockGuiHandler.registerMod(this);
		proxy.init();
		Substances.init(cfg);
		Converting.init();
		ShaftMounts.init();
		Objects.initConstants(cfg);
		RecipeScriptContext.instance.run("thermokinetic.PRE_INIT");
	}

	@Mod.EventHandler
	public void load(FMLInitializationEvent event) {
		proxy.registerRenderers();
	}

	private static final ResourceLocation fallbackSubstance = new ResourceLocation("thermokin:air");

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		if (Substances.defaultEnv == null) {
			FMLLog.log("thermokin", Level.ERROR, "A default Environment is missing! Please check your config file, it has probably crashed or is missing the folowing entry:\nadd(\"environment\", nil, \"thermokin:air\", 101250, 270, 25, 0.8);");
			Substance s = Substance.REGISTRY.getObject(fallbackSubstance);
			if (s == null) GameRegistry.register(s = new Substance("air").setRegistryName(fallbackSubstance));
			Substances.defaultEnv = new Environment(s, ThermodynamicUtil.Pn, 270, 25, 0.8);
		}
		Substance.Default = Substances.defaultEnv.type;
	}

}
