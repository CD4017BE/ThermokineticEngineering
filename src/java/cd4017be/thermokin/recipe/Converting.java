package cd4017be.thermokin.recipe;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;
import cd4017be.api.recipes.RecipeAPI;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.lib.script.Parameters;
import cd4017be.lib.util.OreDictStack;
import cd4017be.thermokin.physics.LiquidState;
import cd4017be.thermokin.physics.Substance;
import cd4017be.thermokin.tileentity.LiquidReservoir;

public class Converting implements IRecipeHandler {

	public static final Converting instance = new Converting();
	public static final String LIQ = "liqConv", SOL = "melt";
	public static final HashMap<Fluid, LiqEntry> fluidLookup = new HashMap<Fluid, LiqEntry>();
	public static final HashMap<ItemKey, SolEntry> solidLookup = new HashMap<ItemKey, SolEntry>();
	public static final HashMap<Substance, ArrayList<LiqEntry>> substanceLookupL = new HashMap<Substance, ArrayList<LiqEntry>>();
	public static final HashMap<Substance, ArrayList<SolEntry>> substanceLookupS = new HashMap<Substance, ArrayList<SolEntry>>();

	public static void init() {
		RecipeAPI.Handlers.put(LIQ, instance);
		RecipeAPI.Handlers.put(SOL, instance);
	}

	@Override
	public void addRecipe(Parameters p) {
		boolean L = LIQ.equals(p.getString(0));
		Substance s = Substance.REGISTRY.getObject(new ResourceLocation(p.getString(1)));
		if (s == null) throw new IllegalArgumentException(String.format("invalid substance: %s", p.getString(1)));
		if (L) {
			FluidStack fluid = p.get(5, FluidStack.class);
			double V = p.getNumber(2), P = p.getNumber(3), T = p.getNumber(4);
			addRecipe(new LiqEntry(fluid.getFluid(), new LiquidState(s, LiquidReservoir.SizeG * (1.0 - Math.sqrt(LiquidReservoir.P0 / P)), V / (double)fluid.amount, T)));
		} else {
			ItemKey cast = p.param.length > 6 ? new ItemKey(p.get(6)) : new ItemKey();
			double V = p.getNumber(2), Q = p.getNumber(3), T = p.getNumber(4);
			addRecipe(new SolEntry(new ItemKey(p.get(5)), new LiquidState(s, V, 0, T), Q * s.Dl * s.m, cast));
		}
	}

	public void addRecipe(LiqEntry rcp) {
		fluidLookup.put(rcp.fluid, rcp);
		ArrayList<LiqEntry> list = substanceLookupL.get(rcp.liquid.type);
		if (list == null) substanceLookupL.put(rcp.liquid.type, list = new ArrayList<LiqEntry>());
		list.add(rcp);
	}

	public void addRecipe(SolEntry rcp) {
		solidLookup.put(rcp.item, rcp);
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
					if (e.item.equals(solid)) return e.copy(solid);
				} else if (e.cast.equals(cast)) return e.copy();
			}
		} else if (solid != null) {
			SolEntry e = solidLookup.get(new ItemKey(solid));
			if (e != null) return e.liquid.T < liquid.T ? e.copy(solid) : e.copy();
			for (OreDictStack o : OreDictStack.get(solid)) {
				e = solidLookup.get(new ItemKey(o));
				if (e != null) return e.liquid.T < liquid.T ? e.copy(solid) : e.copy();
			}
		}
		return null;
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
		if (liq.type == null) return null;
		ItemKey item = ItemKey.readFromNBT(nbt, "s");
		return item != null ? new SolEntry(item, liq, dQ, null) : null;
	}

	public static class SolEntry {
		public SolEntry(ItemKey item, LiquidState liquid, double dQ, ItemKey cast) {
			this.item = item;
			this.cast = cast;
			this.liquid = liquid;
			this.dQ = dQ;
		}
		public double dQ;
		public final ItemKey item, cast;
		public final LiquidState liquid;
		public SolEntry copy(ItemStack solid) {
			solid.stackSize--;
			return new SolEntry(new ItemKey(ItemHandlerHelper.copyStackWithSize(solid, 1)), liquid.copy(liquid.Vmax), dQ, cast.copy());
		}
		public SolEntry copy() {
			return new SolEntry(item.copy(), liquid.copy(), dQ, cast.copy());
		}
		public NBTTagCompound writeToNBT() {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setDouble("Q", dQ);
			liquid.writeToNBT(nbt, "l");
			nbt.setDouble("V", liquid.Vmax);
			item.writeToNBT(nbt, "s");
			return nbt;
		}
	}

}
