package cd4017be.thermokin.tileentity;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.templates.SharedNetwork;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.multiblock.GasContainer;
import cd4017be.thermokin.multiblock.GasPhysics.IGasCon;
import cd4017be.thermokin.multiblock.HeatReservoir;
import cd4017be.thermokin.multiblock.LiquidContainer;
import cd4017be.thermokin.multiblock.LiquidPhysics;
import cd4017be.thermokin.multiblock.LiquidPhysics.ILiquidCon;
import cd4017be.thermokin.physics.GasState;
import cd4017be.thermokin.physics.LiquidState;
import cd4017be.thermokin.physics.Substance;
import cd4017be.thermokin.physics.ThermodynamicUtil;

public class Evaporator extends ModTileEntity implements ITickable, IGuiData, IGasCon, ILiquidCon {

	public static double SizeL, SizeG;
	public static float C0, R0;

	public HeatReservoir heat;
	public LiquidContainer liq;
	public GasContainer gas;
	private double tl;

	public Evaporator() {
		gas = new GasContainer(this, SizeG);
		liq = new LiquidContainer(this, SizeL, gas);
		heat = new HeatReservoir(C0, R0);
	}

	@Override
	public void update() {
		if (worldObj.isRemote) return;
		gas.network.updateTick(gas);
		liq.network.updateTick(liq);
		heat.update(this);
		GasState gas = this.gas.network.gas;
		LiquidState liq = this.liq.getLiquid();
		double T = (gas.E() + liq.E() + (double)(heat.T * heat.C)) / (gas.nR + liq.C() + heat.C);
		heat.T = (float)(gas.T = liq.T = T);
		if (gas.type != liq.type && liq.type != null && liq.V > LiquidState.NULL) {
			if (tl >= 0) tl = -liq.type.Qe / T;
			GasState ngas = new GasState(liq.type, T, 0, SizeG - liq.V);
			tl = -ThermodynamicUtil.evaporate(liq, ngas, -tl);
			GasState test = ngas.copy(SizeG); test.V = liq.V;
			if (ThermodynamicUtil.shouldSpread(test, gas, this.gas.V)) {
				tl = -tl;
				gas = this.gas.evacuate();
				gas.type = ngas.type;
				gas.nR = ngas.nR;
				this.gas.V = gas.V = ngas.V;
				this.liq.setLiquid(liq);
			}
		} else {
			if (tl <= 0) tl = gas.type.Qe / T;
			double V = gas.V;
			tl = ThermodynamicUtil.evaporate(liq, gas, tl);
			this.gas.V += gas.V - V;
			this.liq.setLiquid(liq);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		LiquidState s = LiquidState.readFromNBT(nbt, "liq", SizeL);
		gas = GasContainer.readFromNBT(this, nbt, "gas", SizeG - s.V);
		new LiquidPhysics(liq = new LiquidContainer(this, SizeL, gas));
		liq.setLiquid(s);
		heat.load(nbt, "cas");
		tl = 0;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		liq.writeToNBT(nbt, "liq");
		gas.writeToNBT(nbt, "gas");
		heat.save(nbt, "cas");
		return super.writeToNBT(nbt);
	}

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing s) {
		if (cap == Objects.LIQUID_CAP || cap == Objects.GAS_CAP || cap == Objects.HEAT_CAP) return true;
		return super.hasCapability(cap, s);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing s) {
		if (cap == Objects.LIQUID_CAP) return (T)liq;
		if (cap == Objects.GAS_CAP) return (T)gas;
		if (cap == Objects.HEAT_CAP) return (T)heat.getCapability(this, s);
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
		gas.updateCon = true;
	}

	@Override
	public void validate() {
		super.validate();
		long uid = SharedNetwork.ExtPosUID(pos, dimensionId);
		liq.setUID(uid);
		gas.setUID(uid);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (liq.network != null) liq.network.remove(liq);
		if (gas.network != null) gas.network.remove(gas);
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (liq.network != null) liq.network.remove(liq);
		if (gas.network != null) gas.network.remove(gas);
	}

	@Override
	public void initContainer(DataContainer cont) {
		cont.extraRef = new LastState();
	}

	@Override
	public boolean detectAndSendChanges(DataContainer container, PacketBuffer dos) {
		if (gas.network == null) return false;
		GasState gas = this.gas.network.gas;
		LiquidState liq = this.liq.liquid;
		dos.writeByte(Substance.getId(gas.type));
		dos.writeByte(Substance.getId(liq.type));
		dos.writeFloat((float)liq.V);
		dos.writeFloat((float)gas.P());
		dos.writeFloat((float)gas.T);
		return true;
	}

	@Override
	public void updateClientChanges(DataContainer cont, PacketBuffer dis) {
		LastState ls = (LastState)cont.extraRef;
		ls.Sg = Substance.REGISTRY.getObjectById(dis.readByte());
		ls.Sl = Substance.REGISTRY.getObjectById(dis.readByte());
		ls.Vl = dis.readFloat();
		ls.P = dis.readFloat();
		ls.T = dis.readFloat();
		if (ls.Sg == null) ls.Sg = Substance.Default;
	}

	public static class LastState {
		public Substance Sl = null, Sg = Substance.Default;
		public float Vl, P, T;
	}

	@Override
	public boolean conGas(byte side) {
		return gas.canConnect(side);
	}

	@Override
	public boolean conLiquid(byte side) {
		return liq.canConnect(side);
	}

}
