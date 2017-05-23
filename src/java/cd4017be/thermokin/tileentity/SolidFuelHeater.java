package cd4017be.thermokin.tileentity;

import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.SlotItemHandler;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.multiblock.GasContainer;
import cd4017be.thermokin.multiblock.GasPhysics.IGasCon;
import cd4017be.thermokin.multiblock.HeatReservoir;
import cd4017be.thermokin.physics.GasState;
import cd4017be.thermokin.physics.Substance;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.Gui.SlotItemType;
import cd4017be.lib.Gui.TileContainer;
import cd4017be.lib.templates.AutomatedTile;
import cd4017be.lib.templates.Inventory;
import cd4017be.lib.templates.SharedNetwork;
import cd4017be.lib.util.Utils;

public class SolidFuelHeater extends AutomatedTile implements IGuiData, IGasCon {

	private static final double V_LIMIT = 0x1p-6;
	public static double Size;
	public static float C0, R0;
	/**[molR/Ft] molar density of furnace fuel */
	public static float FuelDensity;
	/**[molR] minimum mount of fuel to store */
	public static float MinFuel;
	/**[J/molR] chemical energy stored in fuel */
	public static double BurnEnergy;
	/**[K] minimum temperature for self-ignition */
	public static float IgnitionTemp;
	/**[W/K] heat conductivity: flame -> casing */
	public static double FlameOutReact;
	/** */
	public static double ReactMult;

	private HeatReservoir casing;
	private GasContainer gasIn, gasOut;
	private float Tmin, Tmax, fuel, maxFuel, speed;
	private boolean flame = false;

	public SolidFuelHeater() {
		gasIn = new GasContainer(this, Size - V_LIMIT);
		gasOut = new GasContainer(this, V_LIMIT);
		gasIn.con = 0b000001;
		gasOut.con = 0b000010;
		casing = new HeatReservoir(C0, R0);
		inventory = new Inventory(2, 1, null).group(0, 0, 1, Utils.IN);

		Tmin = 300F; Tmax = 1200F;
	}

	@Override
	public void update() {
		super.update();
		if (worldObj.isRemote) return;
		casing.update(this);
		gasIn.network.updateTick(gasIn);
		gasOut.network.updateTick(gasOut);
		final GasState gas0 = gasIn.network.gas;
		final double inV = gasIn.V - V_LIMIT;
		exchangePressure(gas0, gasOut.network.gas, inV);
		if (fuel < MinFuel) consumeFuel();
		if (fuel <= 0) flame = false;
		else {
			double reactivity = inV / (Size - 2D * V_LIMIT);
			reactivity *= reactivity * gas0.T * Math.min(1, fuel / MinFuel) * ReactMult;
			if (reactivity > inV) reactivity = inV;
			reactivity *= gas0.nR / gas0.V * gas0.type.ox;
			double flameOut = (IgnitionTemp - casing.T) * FlameOutReact;
			if (reactivity < flameOut) flame = false;
			else if (flame) {
				//Thermostat control:
				float t = (Tmax - casing.T) / (Tmax - Tmin);
				if (t < 0) t = 0;
				else if (t > 1) t = 1;
				reactivity = Math.max((double)t * reactivity, flameOut);
				speed = (float)(reactivity / Math.max(FlameOutReact, flameOut));
				//Chemical reaction:
				if (reactivity > fuel) {
					reactivity = fuel;
					flame = false;
				}
				fuel -= reactivity; //consume fuel
				GasState g = gas0.remove(reactivity / gas0.type.ox, 0); //consume oxidizer
				g.T += BurnEnergy * g.type.ox; //add reaction energy
				outputGas(g);//TODO try adiabatic transfer
			} else if (flameOut < 0) flame = true;//self ignition
			else if (reactivity > 2 * flameOut) tryIgnition();
		}
		if (!flame) speed = 0;
	}

	private void exchangePressure(GasState gas0, GasState gas1, double inV) {
		double E0 = gas0.E(), E1 = gas1.E();
		double dV = (E1 * gas0.V - E0 * gas1.V) / (E0 + E1);
		if (dV > inV) {
			dV = inV;
			flame = false;
		} else if (gasOut.V + dV < V_LIMIT) {
			dV = V_LIMIT - gasOut.V;
		}
		if (dV != 0) {
			gasIn.V -= dV; gas0.V -= dV;
			gasOut.V += dV; gas1.V += dV;
		}
	}

	private void consumeFuel() {
		int n = TileEntityFurnace.getItemBurnTime(inventory.items[0]);
		if (n != 0) {
			fuel += FuelDensity * (float)n;
			maxFuel = fuel;
			inventory.extractItem(0, 1, false);
		}
	}

	private void tryIgnition() {
		if (fuel < MinFuel) return; //not enough fuel
		ItemStack item = inventory.items[1];
		if (item != null && item.getItem() == Items.FLINT_AND_STEEL) {
			int dmg = item.getItemDamage();
			if (dmg >= item.getMaxDamage()) inventory.items[1] = null;
			else item.setItemDamage(dmg + 1);
			flame = true;
		}
	}

	private void outputGas(GasState g) {
		//exchange heat with casing
		casing.T = (float)(g.T = ((double)(casing.T * casing.C) + g.E()) / (g.nR + (double)casing.C));
		//ensure that the output gas has correct type
		gasOut.network.gas.type = Objects.combustionWaste;
		gasOut.network.gas.add(g); //output combustion gas
	}

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing s) {
		if (cap == Objects.HEAT_CAP || (cap == Objects.GAS_CAP && (s == EnumFacing.DOWN || s == EnumFacing.UP))) return true;
		return super.hasCapability(cap, s);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing s) {
		if (cap == Objects.GAS_CAP) return (T)(s == EnumFacing.UP ? gasOut : s == EnumFacing.DOWN ? gasIn : null);
		if (cap == Objects.HEAT_CAP) return (T)casing.getCapability(this, s);
		return super.getCapability(cap, s);
	}

	@Override
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException {
		switch (cmd) {
		case 0:
			Tmax = dis.readFloat();
			if (Tmin > Tmax - 1) Tmin = Tmax - 1;
			break;
		case 1:
			Tmin = dis.readFloat();
			if (Tmax < Tmin + 1) Tmax = Tmin + 1;
			break;
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		casing.load(nbt, "heat");
		double V = nbt.getDouble("fillV");
		gasIn = GasContainer.readFromNBT(this, nbt, "in", V);
		gasOut = GasContainer.readFromNBT(this, nbt, "out", Size - V);
		gasIn.con = 0b000001;
		gasOut.con = 0b000010;
		Tmin = nbt.getFloat("minT");
		Tmax = nbt.getFloat("maxT");
		fuel = nbt.getInteger("fuel");
		maxFuel = fuel;
		
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		casing.save(nbt, "heat");
		gasIn.writeToNBT(nbt, "in");
		gasOut.writeToNBT(nbt, "out");
		nbt.setFloat("minT", Tmin);
		nbt.setFloat("maxT", Tmax);
		nbt.setFloat("fuel", fuel);
		return super.writeToNBT(nbt);
	}

	@Override
	public void onNeighborBlockChange(Block b) {
		casing.check = true;
	}

	@Override
	public void onNeighborTileChange(BlockPos pos) {
		casing.check = true;
		gasIn.updateCon = true;
		gasOut.updateCon = true;
	}

	@Override
	public void validate() {
		super.validate();
		long uid = SharedNetwork.ExtPosUID(pos, dimensionId);
		gasIn.setUID(uid);
		gasOut.setUID(uid^0x8000000000000000L);
		//gasOut.network.gas.type = Objects.combustionWaste;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (gasIn.network != null) gasIn.network.remove(gasIn);
		if (gasOut.network != null) gasOut.network.remove(gasOut);
	}

	@Override
	public void onChunkUnload() {
		super.onChunkUnload();
		if (gasIn.network != null) gasIn.network.remove(gasIn);
		if (gasOut.network != null) gasOut.network.remove(gasOut);
	}

	@Override
	public void initContainer(DataContainer cont) {
		TileContainer container = (TileContainer)cont;		
		container.addItemSlot(new SlotItemHandler(inventory, 0, 8, 16));
		container.addItemSlot(new SlotItemType(inventory, 1, 8, 34, new ItemStack(Items.FLINT_AND_STEEL)));
		container.addPlayerInventory(8, 68);
		
		cont.extraRef = new LastState();
	}

	@Override
	public boolean transferStack(ItemStack item, int s, TileContainer container) {
		if (s < container.invPlayerS) container.mergeItemStack(item, container.invPlayerS, container.invPlayerE, false);
		else container.mergeItemStack(item, 0, 2, true);
		return true;
	}

	@Override
	public boolean detectAndSendChanges(DataContainer cont, PacketBuffer dos) {
		LastState ls = (LastState)cont.extraRef;
		dos.writeFloat(casing.T);
		GasState gas0 = gasIn.network.gas;
		dos.writeFloat((float)gas0.T);
		dos.writeFloat((float)gas0.P());
		dos.writeFloat((float)((gasIn.V - V_LIMIT) / (Size - 2D * V_LIMIT)));
		int p = dos.writerIndex();
		byte chng = 0;
		dos.writeByte(chng);
		if (fuel != ls.fuel) {dos.writeFloat(ls.fuel = fuel); chng |= 1;}
		if (maxFuel != ls.maxFuel) {dos.writeFloat(ls.maxFuel = maxFuel); chng |= 2;}
		if (speed != ls.speed) {dos.writeFloat(ls.speed = speed); chng |= 4;}
		if (Tmin != ls.Tmin) {dos.writeFloat(ls.Tmin = Tmin); chng |= 8;}
		if (Tmax != ls.Tmax) {dos.writeFloat(ls.Tmax = Tmax); chng |= 16;}
		if (gas0.type != ls.in) {dos.writeInt(Substance.getId(ls.in = gas0.type)); chng |= 32;}
		if (chng != 0) dos.setByte(p, chng);
		return true;
	}

	@Override
	public void updateClientChanges(DataContainer cont, PacketBuffer dis) {
		LastState ls = (LastState)cont.extraRef;
		ls.temp = dis.readFloat();
		ls.Tin = dis.readFloat();
		ls.P = dis.readFloat();
		ls.dV = dis.readFloat();
		byte chng = dis.readByte();
		if ((chng & 1) != 0) ls.fuel = dis.readFloat();
		if ((chng & 2) != 0) ls.maxFuel = dis.readFloat();
		if ((chng & 4) != 0) ls.speed = dis.readFloat();
		if ((chng & 8) != 0) ls.Tmin = dis.readFloat();
		if ((chng & 16) != 0) ls.Tmax = dis.readFloat();
		if ((chng & 32) != 0) ls.in = Substance.REGISTRY.getObjectById(dis.readInt());
	}

	public static class LastState {
		public float fuel, maxFuel, speed, Tmin, Tmax, temp, dV, Tin, P;
		public Substance in = Substance.Default;
	}

	@Override
	public boolean conGas(byte side) {
		return side == 0 || side == 1;
	}

}
