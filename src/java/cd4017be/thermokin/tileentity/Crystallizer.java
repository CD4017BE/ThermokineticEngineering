package cd4017be.thermokin.tileentity;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.SlotItemHandler;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.SharedNetwork;
import cd4017be.lib.templates.Inventory.IAccessHandler;
import cd4017be.lib.util.Utils;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.multiblock.HeatReservoir;
import cd4017be.thermokin.multiblock.LiquidContainer;
import cd4017be.thermokin.multiblock.LiquidPhysics.ILiquidCon;
import cd4017be.thermokin.physics.GasState;
import cd4017be.thermokin.physics.LiquidState;
import cd4017be.thermokin.physics.Substance;
import cd4017be.thermokin.recipe.Converting;
import cd4017be.thermokin.recipe.Converting.SolEntry;
import cd4017be.thermokin.recipe.Substances;

public class Crystallizer extends AutomatedTile implements IGuiData, IAccessHandler, ILiquidCon {

	public static double SizeL, SizeG;
	public static float C0, R0;

	private double P0;
	public LiquidContainer liq;
	public HeatReservoir heat;
	public SolEntry rcp;
	private boolean recipeUpdate, wasEmpty;

	public Crystallizer() {
		inventory = new Inventory(2, 2, this).group(0, 0, 1, Utils.ACC).group(1, 1, 2, Utils.ACC);
		liq = new LiquidContainer(this, SizeL, new GasState(Substance.Default, SizeG, Substances.defaultEnv.P, SizeG));
		heat = new HeatReservoir(C0, R0);
	}

	@Override
	public void update() {
		if (worldObj.isRemote) return;
		liq.network.updateTick(liq);
		heat.update(this);
		LiquidState liquid = liq.getLiquid();
		double C = liquid.C(), E = heat.T * heat.C + liquid.T * C;
		C += heat.C;
		if (liq.liquid.type == null) wasEmpty = true;
		else if (wasEmpty) {recipeUpdate = true; wasEmpty = false;}
		if (rcp == null && recipeUpdate) {
			ItemStack item = inventory.items[0];
			liquid.T = E / C;
			rcp = Converting.getRecipe(liquid, item, inventory.items[1]);
			if (item != null && item.stackSize <= 0) inventory.items[0] = null;
			recipeUpdate = false;
		}
		Substance type;
		if (rcp != null && (liquid.type == (type = rcp.liquid.type) || liquid.type == null)) {
			boolean rcpEmpty = false;
			double dE = E - rcp.liquid.T * C;
			double dV = dE / rcp.dQ;
			if (dV > 0) {
				dV = Math.min(dV, liquid.Vmax - liquid.V);
				if (dV >= rcp.liquid.V) {
					ItemStack item = inventory.items[0];
					if (rcp.item.equals(item)) {
						double V = rcp.liquid.V;
						rcp = rcp.copy(item);
						rcp.liquid.V += V;
						if (item.stackSize <= 0) inventory.items[0] = null;
						if (dV > rcp.liquid.V) dV = rcp.liquid.V;
					} else {
						dV = rcp.liquid.V;
						rcpEmpty = true;
					}
				}
			} else {
				dV = Math.max(dV, -liquid.V);
				double Vrem = rcp.liquid.V - rcp.liquid.Vmax;
				if (dV < Vrem + LiquidState.NULL) {
					dV = Vrem;
					ItemStack item = inventory.items[0];
					if (item == null) {
						inventory.items[0] = rcp.item.asItem();
						recipeUpdate = rcpEmpty = true;
					} else if (item.stackSize < item.getMaxStackSize() && rcp.item.equals(item)) {
						item.stackSize++;
						recipeUpdate = rcpEmpty = true;
					}
				}
			}
			if (dV != 0) {
				liquid.V += dV;
				rcp.liquid.V -= dV;
				double dC = dV * type.Cl * type.Dl;
				C += dC;
				E += rcp.liquid.T * dC - dV * rcp.dQ;
				GasState gas = liq.getBufferGas();
				E += dV * (P0 - gas.E() / (gas.V - dV));
				if (liquid.type == null) liquid.type = type;
				liq.setLiquid(liquid);
			}
			if (rcpEmpty) rcp = null;
		}
		heat.T = (float)(liq.liquid.T = E / C);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		liq = LiquidContainer.readFromNBT(this, nbt, "liq", SizeL, SizeG, Substances.defaultEnv.P);
		heat.load(nbt, "cas");
		if (nbt.hasKey("rcp", 10)) rcp = Converting.readFromNBT(nbt.getCompoundTag("rcp"));
		else if (rcp != null && !worldObj.isRemote) {rcp = null; recipeUpdate = true;}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		liq.writeToNBT(nbt, "liq");
		heat.save(nbt, "cas");
		if (rcp != null) nbt.setTag("rcp", rcp.writeToNBT());
		return super.writeToNBT(nbt);
	}

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing s) {
		if (cap == Objects.LIQUID_CAP || cap == Objects.HEAT_CAP) return true;
		return super.hasCapability(cap, s);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing s) {
		if (cap == Objects.LIQUID_CAP) return (T)liq;
		else if (cap == Objects.HEAT_CAP) return (T)heat.getCapability(this, s);
		return super.getCapability(cap, s);
	}

	@Override
	public void onNeighborBlockChange(Block b) {
		heat.check = true;
	}

	@Override
	public void onNeighborTileChange(BlockPos pos) {
		heat.check = true;
		liq.updateCon = true;
	}

	@Override
	public void initContainer(DataContainer container) {
		TileContainer cont = (TileContainer)container;
		cont.addItemSlot(new SlotItemHandler(inventory, 0, 17, 25));
		cont.addItemSlot(new SlotItemHandler(inventory, 1, 71, 16));
		cont.addPlayerInventory(8, 68);
		if (worldObj.isRemote && rcp == null) rcp = new SolEntry(null, new LiquidState(null, 0, 0, 0), 0, null); 
	}

	@Override
	public void setWorldObj(World world) {
		super.setWorldObj(world);
		P0 = Substances.getEnvFor(world).P;
		GasState gas = liq.getBufferGas();
		double V = gas.V;
		gas.V = SizeG - SizeL * 0.5;
		gas.nR = P0 * gas.V / gas.T;
		gas.adiabat(V);
	}

	@Override
	public void validate() {
		super.validate();
		long uid = SharedNetwork.ExtPosUID(pos, dimensionId);
		liq.setUID(uid);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (liq.network != null) liq.network.remove(liq);
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (liq.network != null) liq.network.remove(liq);
	}

	@Override
	public void setSlot(int g, int s, ItemStack item) {
		inventory.items[s] = item;
		recipeUpdate = true;
	}

	@Override
	public int[] getSyncVariables() {
		boolean b = rcp != null;
		return new int[]{
			Float.floatToIntBits(heat.T),
			Float.floatToIntBits((float)liq.liquid.V),
			Substance.getId(liq.liquid.type),
			b ? Substance.getId(rcp.liquid.type) : -1,
			Float.floatToIntBits(b ? (float)rcp.liquid.V : 0),
			Float.floatToIntBits(b ? (float)rcp.liquid.Vmax : 0),
			Float.floatToIntBits(b ? (float)rcp.liquid.T : Float.NaN),
			Float.floatToIntBits(b ? (float)rcp.dQ : 0),
		};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: liq.liquid.T = heat.T = Float.intBitsToFloat(v); break;
		case 1: liq.liquid.V = Float.intBitsToFloat(v); break;
		case 2: liq.liquid.type = Substance.REGISTRY.getObjectById(v); break;
		case 3: rcp.liquid.type = Substance.REGISTRY.getObjectById(v); break;
		case 4: rcp.liquid.V = Float.intBitsToFloat(v); break;
		case 5: rcp.liquid.Vmax = Float.intBitsToFloat(v); break;
		case 6: rcp.liquid.T = Float.intBitsToFloat(v); break;
		case 7: rcp.dQ = Float.intBitsToFloat(v); break;
		}
	}

	@Override
	public boolean conLiquid(byte side) {
		return true;
	}

}
