package cd4017be.thermokin.recipe;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.oredict.OreDictionary;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.lib.util.OreDictStack;
import cd4017be.thermokin.physics.LiquidState;
import cd4017be.thermokin.physics.Substance;

public class Converting implements IRecipeHandler {

	public static final Converting instance = new Converting();
	public static final String LIQ = "liqConv", SOL = "melt";
	public static final HashMap<Fluid, LiqEntry> fluidLookup = new HashMap<Fluid, LiqEntry>();
	public static final HashMap<Integer, SolEntry> solidLookup = new HashMap<Integer, SolEntry>();
	public static final HashMap<Substance, ArrayList<LiqEntry>> substanceLookupL = new HashMap<Substance, ArrayList<LiqEntry>>();
	public static final HashMap<Substance, ArrayList<SolEntry>> substanceLookupS = new HashMap<Substance, ArrayList<SolEntry>>();
	public static double Vg, P0;

	@Override
	public boolean addRecipe(Object... param) {
		boolean L = param[0] == LIQ;
		if (!(param.length >= 6 && param[1] instanceof String && param[2] instanceof Double && param[3] instanceof Double && param[4] instanceof Double && (L ? param[5] instanceof FluidStack : param[5] instanceof ItemStack || param[5] instanceof OreDictStack))) return false;
		Substance s = Substance.REGISTRY.getObject(new ResourceLocation((String)param[1]));
		if (s == null) return false;
		if (L) {
			FluidStack fluid = (FluidStack)param[5];
			double V = (double)param[2], P = (double)param[3], T = (double)param[4];
			addRecipe(new LiqEntry(fluid.getFluid(), new LiquidState(s, Vg * (1.0 - Math.sqrt(P0 / P)), V / (double)fluid.amount, T)));
			return true;
		} else if (param.length <= 6 || param[6] instanceof ItemStack || param[6] instanceof OreDictStack) {
			Object cast = (param.length > 6) ? param[6] : null;
			double V = (double)param[2], Q = (double)param[3], T = (double)param[4];
			addRecipe(new SolEntry(param[5], new LiquidState(s, V, V, T), Q * s.Dl * s.m, cast));
			return true;
		} else return false;
	}

	public void addRecipe(LiqEntry rcp) {
		fluidLookup.put(rcp.fluid, rcp);
		ArrayList<LiqEntry> list = substanceLookupL.get(rcp.liquid.type);
		if (list == null) substanceLookupL.put(rcp.liquid.type, list = new ArrayList<LiqEntry>());
		list.add(rcp);
	}

	public void addRecipe(SolEntry rcp) {
		solidLookup.put(hashIngred(rcp.item), rcp);
		ArrayList<SolEntry> list = substanceLookupS.get(rcp.liquid.type);
		if (list == null) substanceLookupS.put(rcp.liquid.type, list = new ArrayList<SolEntry>());
		list.add(rcp);
	}

	public static LiqEntry toFluid(LiquidState liquid, float T) {
		ArrayList<LiqEntry> list = substanceLookupL.get(liquid.type);
		if (list == null) return null;
		for (LiqEntry e : list)
			if (e.checkConditions(liquid, T))
				return e;
		return null;
	}

	public static SolEntry getRecipe(LiquidState liquid, ItemStack solid, ItemStack cast) {
		if (liquid.type != null) {
			ArrayList<SolEntry> list = substanceLookupS.get(liquid.type);
			if (list == null) return null;
			for (SolEntry e : list) {
				if (liquid.T > e.liquid.T) {
					if (matchItem(e.item, solid)) return e.copy(solid);
				} else if (matchItem(e.cast, cast)) return e.copy();
			}
		} else if (solid != null) {
			int key = Item.getIdFromItem(solid.getItem()) | solid.getItemDamage() << 16;
			SolEntry e = solidLookup.get(key);
			if (e != null) return e.liquid.T < liquid.T ? e.copy(solid) : e.copy();
			for (int i : OreDictionary.getOreIDs(solid)) {
				e = solidLookup.get(-i);
				if (e != null) return e.liquid.T < liquid.T ? e.copy(solid) : e.copy();
			}
		}
		return null;
	}

	public static int hashItem(ItemStack item) {
		return Item.getIdFromItem(item.getItem()) | item.getItemDamage() << 16;
	}

	public static int hashIngred(Object obj) {
		if (obj instanceof OreDictStack) {
			return -((OreDictStack)obj).ID;
		} else if (obj instanceof ItemStack) {
			return Item.getIdFromItem(((ItemStack)obj).getItem()) | ((ItemStack)obj).getItemDamage() << 16;
		} else return 0;
	}

	public static boolean matchItem(Object req, ItemStack item) {
		if (req == null) return item == null;
		if (req instanceof OreDictStack) return ((OreDictStack)req).isEqual(item);
		if (req instanceof ItemStack) return ((ItemStack)req).isItemEqual(item);
		return false;
	}

	public static Object copyObjItem(Object item) {
		return item == null ? null : item instanceof ItemStack ? ((ItemStack)item).copy() : item instanceof OreDictStack ? ((OreDictStack)item).copy() : null;
	}

	public static class LiqEntry {
		public LiqEntry(Fluid fluid, LiquidState liquid) {
			this.fluid = fluid;
			this.liquid = liquid;
		}
		public final Fluid fluid;
		public final LiquidState liquid;
		public boolean checkConditions(LiquidState liq, float T) {
			return liq.V >= liquid.Vmax && (liq.T > liquid.T ^ T > liquid.T);
		}
	}

	public static SolEntry readFromNBT(NBTTagCompound nbt) {
		double dQ = nbt.getDouble("Q");
		LiquidState liq = LiquidState.readFromNBT(nbt, "l", nbt.getDouble("V"));
		Object item;
		if (nbt.hasKey("s", 10)) item = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("s"));
		else if (nbt.hasKey("s", 8)) item = new OreDictStack(nbt.getString("s"), 1);
		else return null;
		return item != null && liq.type != null ? new SolEntry(item, liq, dQ, null) : null;
	}

	public static class SolEntry {
		public SolEntry(Object item, LiquidState liquid, double dQ, Object cast) {
			this.item = item;
			this.cast = cast;
			this.liquid = liquid;
			this.dQ = dQ;
		}
		public double dQ;
		public final Object item, cast;
		public final LiquidState liquid;
		public SolEntry copy(ItemStack solid) {
			solid.stackSize--;
			return new SolEntry(ItemHandlerHelper.copyStackWithSize(solid, 1), liquid.copy(liquid.Vmax), dQ, copyObjItem(cast));
		}
		public SolEntry copy() {
			return new SolEntry(copyObjItem(item), liquid.copy(), dQ, copyObjItem(cast));
		}
		public NBTTagCompound writeToNBT() {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setDouble("Q", dQ);
			liquid.writeToNBT(nbt, "l");
			nbt.setDouble("V", liquid.Vmax);
			if (item instanceof ItemStack) nbt.setTag("s", ((ItemStack)item).writeToNBT(new NBTTagCompound()));
			else if (item instanceof OreDictStack) nbt.setString("s", ((OreDictStack)item).id);
			return nbt;
		}
	}

}
