package cd4017be.thermokin.tileentity;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.SlotItemHandler;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.SlotItemType;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.SharedNetwork;
import cd4017be.lib.templates.Inventory.IAccessHandler;
import cd4017be.lib.util.Utils;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.multiblock.LiquidContainer;
import cd4017be.thermokin.multiblock.LiquidPhysics.ILiquidCon;
import cd4017be.thermokin.physics.GasState;
import cd4017be.thermokin.physics.LiquidState;
import cd4017be.thermokin.physics.Substance;

public class HydraulicPress extends AutomatedTile implements IAccessHandler, ILiquidCon, IGuiData {

	public static double SizeG = 150, P0 = 102500;
	public LiquidContainer liqIn, liqOut;
	public Recipe rcp;
	private boolean ingredChanged;

	public HydraulicPress() {
		inventory = new Inventory(3, 2, this).group(0, 0, 2, Utils.IN).group(1, 2, 3, Utils.OUT);
		liqIn = new LiquidContainer(this, 0, new GasState(Substance.Default, SizeG, P0, SizeG));
		liqOut = new LiquidContainer(this, 0, new GasState(Substance.Default, SizeG, P0, SizeG));
		liqIn.con = 0b000011;
		liqOut.con = 0b111100;
	}

	@Override
	public void update() {
		if (worldObj.isRemote) return;
		inventory.update(this);
		liqIn.network.updateTick(liqIn);
		liqOut.network.updateTick(liqOut);
		if (rcp != null) {
			if (liqIn.liquid.V > liqIn.liquid.Vmax - LiquidState.NULL &&
					(rcp.res = inventory.insertItem(2, rcp.res, false)) == null) {
				rcp = null;
				swapContainers(liqIn, liqOut, liqIn.liquid.Vmax);
				liqOut.setLiquid(liqIn.getLiquid());
				liqIn.setLiquid(new LiquidState(0));
			}
		} else if (ingredChanged && liqOut.liquid.type == null) {
			ingredChanged = false;
			rcp = findRecipe(inventory.items[0], inventory.items[1]);
			if (rcp != null) {
				if (rcp.ingr1 != null) inventory.extractItem(0, rcp.ingr1.stackSize, false);
				if (rcp.ingr2 != null) inventory.extractItem(1, rcp.ingr2.stackSize, false);
				swapContainers(liqOut, liqIn, rcp.V);
				GasState gas = liqIn.getBufferGas();
				gas.nR = P0;
				gas.T = gas.V = rcp.getVmax();
			}
		}
	}

	private void swapContainers(LiquidContainer src, LiquidContainer dst, double V) {
		dst.network.content.Vmax += V - dst.liquid.Vmax;
		dst.liquid.Vmax = V;
		src.network.content.Vmax -= src.liquid.Vmax;
		src.liquid.Vmax = 0;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		if (nbt.hasKey("rcp", 10)) {
			rcp = loadRecipe(nbt.getCompoundTag("rcp"));
			liqIn = LiquidContainer.readFromNBT(this, nbt, "liq", rcp.V, rcp.getVmax(), P0);
			liqOut = new LiquidContainer(this, 0, new GasState(Substance.Default, SizeG, P0, SizeG));
		} else {
			rcp = null;
			liqIn = new LiquidContainer(this, 0, new GasState(Substance.Default, SizeG, P0, SizeG));
			liqOut = LiquidContainer.readFromNBT(this, nbt, "liq", nbt.getDouble("rcp"), SizeG, P0);
		}
		liqIn.con = 0b000011;
		liqOut.con = 0b111100;
		ingredChanged = true;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		if (rcp != null) {
			nbt.setTag("rcp", rcp.writeToNBT());
			liqIn.writeToNBT(nbt, "liq");
		} else {
			nbt.setDouble("rcp", liqOut.liquid.Vmax);
			liqOut.writeToNBT(nbt, "liq");
		}
		return super.writeToNBT(nbt);
	}

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing s) {
		if (cap == Objects.LIQUID_CAP) return true;
		return super.hasCapability(cap, s);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing s) {
		if (cap == Objects.LIQUID_CAP)
			return (T)(s == EnumFacing.UP || s == EnumFacing.DOWN ? liqIn : liqOut);
		return super.getCapability(cap, s);
	}

	@Override
	public void onNeighborTileChange(BlockPos pos) {
		liqIn.updateCon = true;
		liqOut.updateCon = true;
	}

	@Override
	public void setSlot(int g, int s, ItemStack item) {
		inventory.items[s] = item;
		if (s < 2) ingredChanged = true;
	}

	@Override
	public void validate() {
		super.validate();
		long uid = SharedNetwork.ExtPosUID(pos, dimensionId);
		liqIn.setUID(uid);
		liqOut.setUID(uid^0x8000000000000000L);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (liqIn.network != null) liqIn.network.remove(liqIn);
		if (liqOut.network != null) liqOut.network.remove(liqOut);
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (liqIn.network != null) liqIn.network.remove(liqIn);
		if (liqOut.network != null) liqOut.network.remove(liqOut);
	}

	@Override
	public boolean conLiquid(byte side) {
		return true;
	}

	@Override
	public void initContainer(DataContainer container) {
		TileContainer cont = (TileContainer)container;
		cont.addItemSlot(new SlotItemHandler(inventory, 0, 26, 16));
		cont.addItemSlot(new SlotItemHandler(inventory, 1, 26, 34));
		cont.addItemSlot(new SlotItemType(inventory, 2, 134, 34));
		cont.addPlayerInventory(8, 68);
	}

	@Override
	public int[] getSyncVariables() {
		LiquidContainer liq = rcp != null ? liqIn : liqOut;
		return new int[]{
			Substance.getId(liq.liquid.type),
			Float.floatToIntBits((float)liq.liquid.V),
			Float.floatToIntBits((float)liq.liquid.Vmax),
			Float.floatToIntBits((float)liq.getBufferGas().P()),
			Float.floatToIntBits(rcp != null ? (float)(rcp.P + P0) : Float.NaN)
		};
	}

	private static Recipe findRecipe(ItemStack in1, ItemStack in2) {
		if (in1 != null && in1.getItem() == Items.REDSTONE && in1.stackSize >= 3 && in2 != null && in2.getItem() == Items.QUARTZ)
			return new Recipe(new ItemStack(Items.REDSTONE, 3), new ItemStack(Items.QUARTZ), new ItemStack(Items.COMPARATOR), 0.2, 50000);
		else return null;
	}

	private static Recipe loadRecipe(NBTTagCompound nbt) {
		return new Recipe(null, null, ItemStack.loadItemStackFromNBT(nbt), nbt.getDouble("V"), nbt.getDouble("P"));
	}

	public static class Recipe {
		public ItemStack ingr1, ingr2;
		public ItemStack res;
		public double V, P;
		public Recipe(ItemStack in0, ItemStack in1, ItemStack out, double V, double P) {
			ingr1 = in0; ingr2 = in1; res = out; this.V = V; this.P = P;
		}
		public double getVmax() {
			return V * (P0 / P * (Math.sqrt(P / P0 + 1.0) + 1.0) + 1.0);
		}
		public NBTTagCompound writeToNBT() {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setDouble("V", V);
			nbt.setDouble("P", P);
			return res.writeToNBT(nbt);
		}
	}

}
