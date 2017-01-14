package cd4017be.thermokin.tileentity;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.physics.LiquidState;
import cd4017be.thermokin.multiblock.IGear;
import cd4017be.thermokin.multiblock.LiquidComponent;
import cd4017be.thermokin.multiblock.LiquidPhysics;
import cd4017be.thermokin.multiblock.LiquidPhysics.ILiquidCon;
import cd4017be.thermokin.multiblock.ShaftComponent;
import cd4017be.thermokin.multiblock.ShaftPhysics.IKineticComp;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.util.Utils;

public class LiquidPump extends ModTileEntity implements ITickable, IKineticComp, ILiquidCon, IGuiData {

	private ShaftComponent link;
	private LiquidComponent input, output;
	public static final float Amin = 0.0001F, Amax = 0.01F;
	private boolean updateCon = false, run = false;
	public int cfg;
	public float dA = Amin, speed, power;

	@Override
	public void update() {
		if (worldObj.isRemote) return;
		if (updateCon) {
			input = this.getConContainer(cfg & 0xf);
			output = this.getConContainer(cfg >> 4 & 0xf);
			if (link == null) {
				EnumFacing side = EnumFacing.VALUES[getOrientation()];
				ICapabilityProvider te = this.getTileOnSide(side);
				ShaftComponent comp = te != null ? te.getCapability(Objects.SHAFT_CAP, side.getOpposite()) : null;
				if (comp != null) comp.updateCon = true;
			}
			updateCon = false;
		}
	}

	private LiquidComponent getConContainer(int side) {
		if (side == 0) return null;
		side += this.getOrientation(); side %= 6;
		TileEntity te = Utils.getTileOnSide(this, (byte)side);
		return te != null ? te.getCapability(Objects.LIQUID_CAP, EnumFacing.VALUES[side^1]) : null;
	}

	@Override
	public byte getConSide() {
		return this.getOrientation();
	}

	@Override
	public ShaftComponent getShaft() {
		return link;
	}

	@Override
	public boolean setShaft(ShaftComponent shaft) {
		if (link == null && !(shaft instanceof IGear)) return false;//TODO make use of gear
		this.link = shaft;
		return true;
	}

	@Override
	public boolean valid() {
		return !tileEntityInvalid;
	}

	@Override
	public float estimatedForce(float ds) {
		if (input != null && ((TileEntity)input.tile).isInvalid()) {input = null; updateCon = true;}
		if (output != null && ((TileEntity)output.tile).isInvalid()) {output = null; updateCon = true;}
		if (input == null || output == null || ((cfg & 0x100) == 0 ^ ((cfg & 0x200) != 0 && worldObj.getStrongPower(pos) > 0))) {
			run = false; return 0;
		}
		LiquidPhysics in = input.network, out = output.network;
		if (in.content.type == null || (out.content.type != null && in.content.type != out.content.type)) {run = false; return 0;}
		LiquidState liq = new LiquidState(null, dA * ds, 0, 0);
		double P = in.drainLiquid(liq, false) / liq.Vmax;
		if (liq.V == 0) {run = false; return 0;}
		P += out.fillLiquid(liq, false) / liq.V;
		if (liq.V == 0) {run = false; return 0;}
		run = true;
		return -(float)P * dA;
	}

	@Override
	public float work(float ds, float v) {
		if (!run) return speed = power = 0;
		LiquidPhysics in = input.network, out = output.network;
		double Vmax = Math.min(in.content.V, out.content.Vrem());
		if (Vmax == 0 || (out.content.type != null && in.content.type != out.content.type)) return speed = power = 0;
		LiquidState liq = new LiquidState(null, Math.min(dA * ds, Vmax), 0, 0);
		speed = (float)liq.Vmax / ds;
		return power = -(float)(in.drainLiquid(liq, true) + out.fillLiquid(liq, true));
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		dA = nbt.getFloat("dA");
		cfg = nbt.getInteger("cfg");
		updateCon = true;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setFloat("dA", dA);
		nbt.setInteger("cfg", cfg);
		return super.writeToNBT(nbt);
	}

	@Override
	public void onNeighborBlockChange(Block b) {
		updateCon = true;
	}

	@Override
	public void onNeighborTileChange(BlockPos pos) {
		updateCon = true;
	}

	@Override
	public void onPlayerCommand(PacketBuffer dis, EntityPlayerMP player) {
		byte cmd = dis.readByte();
		if (cmd == 0) {
			dA = dis.readFloat();
			if (dA < Amin) dA = Amin;
			else if (dA > Amax) dA = Amax;
		} else if (cmd == 1) {
			cfg = dis.readInt();
			updateCon = true;
		}
	}

	public int getVscaled(int i) {
		return (int)(dA * (float)i / Amax);
	}

	@Override
	public boolean conLiquid(byte side) {
		side = (byte)((side + 6 - this.getOrientation()) % 6);
		return (cfg & 0xf) == side || (cfg >> 4 & 0xf) == side;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{cfg, Float.floatToIntBits(dA), Float.floatToIntBits(power), Float.floatToIntBits(speed)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: cfg = v; break;
		case 1: dA = Float.intBitsToFloat(v); break;
		case 2: power = Float.intBitsToFloat(v); break;
		case 3: speed = Float.intBitsToFloat(v); break;
		}
	}

}
