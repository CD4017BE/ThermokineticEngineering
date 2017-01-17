package cd4017be.thermokin.tileentity;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.physics.GasState;
import cd4017be.thermokin.physics.LiquidState;
import cd4017be.thermokin.physics.Substance;
import cd4017be.thermokin.recipe.Converting;
import cd4017be.thermokin.recipe.Converting.LiqEntry;
import cd4017be.thermokin.multiblock.HeatReservoir;
import cd4017be.thermokin.multiblock.LiquidContainer;
import cd4017be.thermokin.multiblock.LiquidPhysics.ILiquidCon;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.SlotTank;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.Gui.TileContainer.TankSlot;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.SharedNetwork;
import cd4017be.lib.templates.TankContainer;
import cd4017be.lib.util.Utils;

public class LiquidReservoir extends AutomatedTile implements IGuiData, ILiquidCon {

	public static double SizeL, SizeG, P0;
	public static float C0, R0;

	public HeatReservoir heat;
	public LiquidContainer liq;

	public LiquidReservoir() {
		liq = new LiquidContainer(this, SizeL, new GasState(Substance.Default, SizeG, P0, SizeG));
		heat = new HeatReservoir(C0, R0);
		tanks = new TankContainer(1, 1).tank(0, 1000, Utils.ACC, 0, 1);
		inventory = new Inventory(2, 0, null);
	}

	@Override
	public void update() {
		if (worldObj.isRemote) return;
		super.update();
		liq.network.updateTick(liq);
		heat.update(this);
		LiquidState ls = liq.getLiquid();
		int am = tanks.getAmount(0), max;
		if (am > 0) {
			LiqEntry s = Converting.fluidLookup.get(tanks.fluids[0].getFluid());
			if (s != null && (s.liquid.type == ls.type || ls.type == null) && (max = (int)((ls.Vmax * s.liquid.Vmax - ls.V) / s.liquid.V)) > 0) {
				am = Math.min(am, max);
				ls.insert(s.liquid.copy(s.liquid.V * (double)am));
				tanks.drain(0, am, true);
				liq.setLiquid(ls);
			}
		}//TODO add the other way around
		if (ls.V > 0) {
			double T = (heat.T * heat.C + ls.E()) / (heat.C + ls.C());
			heat.T = (float)(liq.liquid.T = T);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		liq = LiquidContainer.readFromNBT(this, nbt, "liq", SizeL, SizeG, P0);
		heat.load(nbt, "cas");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		liq.writeToNBT(nbt, "liq");
		heat.save(nbt, "cas");
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
		if (cap == Objects.HEAT_CAP) return (T)heat;
		return super.getCapability(cap, s);
	}

	@Override
	public void onNeighborBlockChange(Block b) {
		heat.check = true;
	}

	@Override
	public void onNeighborTileChange(BlockPos pos) {
		liq.updateCon = true;
		heat.check = true;
	}

	@Override
	public void validate() {
		super.validate();
		liq.setUID(SharedNetwork.ExtPosUID(pos, dimensionId));
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
	public void initContainer(DataContainer container) {
		TileContainer cont = (TileContainer)container;
		cont.addItemSlot(new SlotTank(inventory, 0, 8, 16));
		cont.addItemSlot(new SlotTank(inventory, 1, 8, 34));
		cont.addTankSlot(new TankSlot(tanks, 0, 44, 16, (byte)0x22));
		cont.addPlayerInventory(8, 68);
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{
			Float.floatToIntBits(heat.T),
			Float.floatToIntBits((float)liq.liquid.V),
			Substance.getId(liq.liquid.type)
		};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: liq.liquid.T = heat.T = Float.intBitsToFloat(v); break;
		case 1: liq.liquid.V = Float.intBitsToFloat(v); break;
		case 2: liq.liquid.type = Substance.REGISTRY.getObjectById(v); break;
		}
	}

	@Override
	public boolean conLiquid(byte side) {
		return liq.canConnect(side);
	}

}
