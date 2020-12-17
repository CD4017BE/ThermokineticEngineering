package cd4017be.kineng;

import static cd4017be.kineng.Objects.*;
import static cd4017be.kineng.physics.Ticking.dt;
import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeScriptContext;
import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.kineng.block.BlockRotaryTool;
import cd4017be.kineng.physics.ShaftStructure;
import cd4017be.kineng.physics.Ticking;
import cd4017be.kineng.recipe.*;
import cd4017be.kineng.tileentity.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

/** @author CD4017BE */
public class CommonProxy {

	public void preInit() {
		MinecraftForge.EVENT_BUS.register(this);
		for (ProcessingRecipes rcp : ProcessingRecipes.recipeList)
			if (rcp != null)
				RecipeAPI.Handlers.put(rcp.name, rcp);
		RecipeScriptContext.instance.modules.get("kinetic")
		.assign("animal_power", new AnimalStrength());
	}

	public void init(ConfigConstants c) {
		RecipeAPI.Handlers.put("knife_cutting", flint_knife);
		M_WOOD.setFromConfig(c, "wood", 750, 50e6, 0.05);
		M_STONE.setFromConfig(c, "stone", 2500, 20e6, 0.1);
		M_IRON.setFromConfig(c, "iron", 7860, 150e6, 0.01);
		M_ALUMINUM.setFromConfig(c, "aluminum", 2700, 300e6, 0.02);
		M_BEDROCK.setFromConfig(c, "bedrock", 1000, Double.POSITIVE_INFINITY, 0);
		
		GRINDSTONE.setMaterials(M_STONE, 1.0, 0.625);
		setFromConfig(GRINDSTONE, c, 50000);
		SAWBLADE.setMaterials(M_IRON, 1.0, 0.0625);
		setFromConfig(SAWBLADE, c, 10000);
		LATHE.setMaterials(M_WOOD, 0.5, 0.5);
		setFromConfig(LATHE, c, 10000);
		PRESS.setMaterials(M_STONE, 0.25, 0);
		setFromConfig(PRESS, c, 50000);
		MAGNETS.setMaterials(M_IRON, 0.5, 0.625);
		setFromConfig(MAGNETS, c, 2500000);
		SHAFT_MAN.setMaterials(M_WOOD, 2.0, 0.125);
		SHAFT_MAN.av_max *= 0.5;
		setFromConfig(SHAFT_MAN, c, 5000);
		WATER_WHEEL.setMaterials(M_WOOD, 2.5, 1.0);
		setFromConfig(WATER_WHEEL, c, 50000);
		MOB_GRINDER.setMaterials(M_IRON, 1.5, 0.125);
		setFromConfig(MOB_GRINDER, c, 50000);
		double[] F = c.getVect("maxF_wind_mill", new double[] {50000, 50000, 250000, 400000, 400000});
		double[] str = c.getVect("str_wind_mill", new double[] {0.25, 0.125, 0.25, 0.25, 0.20});
		WIND_MILL.setMaterial(1, M_WOOD, M_WOOD, 0.5, str[0], F[0]);
		WIND_MILL.setMaterial(2, M_WOOD, M_WOOD, 0.5, str[1], F[1]);
		WIND_MILL.setMaterial(3, M_IRON, M_IRON, 0.5, str[2], F[2]);
		WIND_MILL.setMaterial(4, M_IRON, M_ALUMINUM, 0.5, str[3], F[3]);
		WIND_MILL.setMaterial(5, M_IRON, M_ALUMINUM, 0.5, str[4], F[4]);
		Object[] scr = c.getArray("scrap_wind_mill", 5);
		for (int i = 0; i < 5; i++)
			WIND_MILL.scrap[i+1] = scr[i] instanceof ItemStack ? (ItemStack)scr[i] : ItemStack.EMPTY;
		
		KineticProcess.FRICTION_V0 = c.getNumber("machine_fric_v0", 0.001);
		ShaftStructure.FRICTION_V0 = c.getNumber("shaft_fric_v0", 0.2);
		ShaftStructure.SYNC_THRESHOLD = c.getNumber("shaft_sync_vel", 0.1);
		Ticking.OVERLOAD_CHECKS = Integer.highestOneBit((int)c.getNumber("overload_check_t", 16));
		Ticking.DEBUG = c.getNumber("log_debug", 0) >= 1.0;
		ManualPower.CHECK_INTERVAL = (int)c.getNumber("animal_power_tupd", 100);
		ManualPower.MAX_TIME = (int)c.getNumber("animal_power_tmax", 12000);
		ManualPower.CLICK_TICKS = (int)c.getNumber("manual_power_tupd", 10);
		ManualPower.EXHAUSTION_TICK = (float)c.getNumber("manual_power_exhaustion", 0.02);
		FluxCoil.J_RF = c.getNumber("energy_conv_RF", 10);
		FluxCoil.E_MAX = c.getNumber("flux_coil_cap", 10000) * FluxCoil.J_RF;
		Gear.A_CONTACT = c.getNumber("gear_maxF_area", 0.0125);
		LakeGate.g2 = c.getNumber("gravity", 9.81) * 2.0;
		LakeGate.A = c.getNumber("lake_gate_crsA", 1) * dt * 1000D/15D;
		LakeValve.CAP = (int)c.getNumber("lake_valve_cap", 1000);
		StorageLake.RAIN_MULT = (float)c.getNumber("lake_rain_mult", 0.25);
		LakeValve.NO_WATER_IN = c.getNumber("cheaty_water_supply", 0.0) < 1.0;
		WaterWheel.WATER_ONLY = c.getNumber("any_liquid_on_wheel", 0.0) < 1.0;
		WindTurbine.AIR_DENSITY = c.getNumber("air_density", 1.29);
		WindTurbine.WIND_SCALE = (float)c.getNumber("wind_scale", 1000);
		MobGrinder.T_CHECK = (int)c.getNumber("mob_grinder_tidle", 50);
		MobGrinder.MIN_HP = (float)c.getNumber("mob_grinder_HP0", 1.0);
		MobGrinder.F_BASE = c.getNumber("mob_grinder_F0", 1000);
		MobGrinder.DMG_J = (float)c.getNumber("mob_grinder_DPJ", 0.001);
	}

	private static void setFromConfig(BlockRotaryTool block, ConfigConstants c, double maxF) {
		String name = block.getRegistryName().getResourcePath();
		block.maxF = c.getNumber("maxF_" + name, maxF);
		block.scrap = c.get("scrap_" + name, ItemStack.class, ItemStack.EMPTY);
	}

}
