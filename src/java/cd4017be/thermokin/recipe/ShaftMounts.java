package cd4017be.thermokin.recipe;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.logging.log4j.Level;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLLog;
import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.thermokin.multiblock.IGear;
import cd4017be.thermokin.multiblock.IMagnet;
import cd4017be.thermokin.tileentity.Shaft;

public class ShaftMounts implements IRecipeHandler {

	public static final String GEAR = "shaftGear", MAG = "shaftMagnet";
	public static final ShaftMounts instance = new ShaftMounts();
	public static final HashMap<ItemKey, Entry> shaftMounts = new HashMap<ItemKey, Entry>();

	public static void init() {
		RecipeAPI.Handlers.put(GEAR, instance);
		RecipeAPI.Handlers.put(MAG, instance);
		Shaft.handlers.add((tile, item) -> {
			Entry e = shaftMounts.get(new ItemKey(item));
			return e == null ? null : e.type == 0 ? 
				new IGear.SimpleGear(tile, e.m, e.s, e.model) : 
				new IMagnet.SimpleMagnet(tile, e.m, e.s, e.model);
		});
	}

	@Override
	public boolean addRecipe(Object... param) {
		if (!(param.length == 5 && param[1] instanceof ItemStack && param[2] instanceof String && param[3] instanceof Double && param[4] instanceof Double)) {
			FMLLog.log("RECIPE", Level.ERROR, "expected: [\"%s\", <itemstack>, <string>, <number>, <number>]\ngot: %s", param[0], Arrays.deepToString(param));
			return false;
		}
		byte type = MAG.equals(param[0]) ? (byte)1 : (byte)0;
		shaftMounts.put(new ItemKey((ItemStack)param[1]), new Entry(type, (Double)param[3], (Double)param[4], (String)param[2]));
		return true;
	}

	public static class Entry {
		Entry(byte type, double m, double s, String model) {this.type = type; this.m = (float)m; this.s = (float)s; this.model = "thermokin:models/tileentity/" + model;}
		public final byte type;
		public final float m, s;
		public final String model;
	}

}
