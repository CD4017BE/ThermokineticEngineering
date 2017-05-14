package cd4017be.thermokin.recipe;

import java.util.HashMap;

import net.minecraft.item.ItemStack;
import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.lib.script.Parameters;
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
	public void addRecipe(Parameters p) {
		byte type = MAG.equals(p.getString(0)) ? (byte)1 : (byte)0;
		shaftMounts.put(new ItemKey(p.get(1, ItemStack.class)), new Entry(type, p.getNumber(3), p.getNumber(4), p.getString(2)));
	}

	public static class Entry {
		Entry(byte type, double m, double s, String model) {this.type = type; this.m = (float)m; this.s = (float)s; this.model = "thermokin:models/tileentity/" + model;}
		public final byte type;
		public final float m, s;
		public final String model;
	}

}
