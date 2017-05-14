package cd4017be.thermokin.tileentity;

import java.io.IOException;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.physics.GasState;
import cd4017be.thermokin.multiblock.GasContainer;
import cd4017be.thermokin.multiblock.IGear;
import cd4017be.thermokin.multiblock.GasPhysics.IGasCon;
import cd4017be.thermokin.multiblock.ShaftComponent;
import cd4017be.thermokin.multiblock.ShaftPhysics.IKineticComp;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.templates.AutomatedTile;

/**
 * 
 * @author CD4017BE
 *
 */
public class PneumaticPiston extends AutomatedTile implements IKineticComp, IGasCon, IGuiData {

	public static float Amin, Amax;
	private ShaftComponent link;
	private GasContainer input, output;
	private boolean updateCon = false, run = false;
	private float x;
	public int cfg;
	public float Ain = Amin, Aout = Amin, power;

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

	private GasContainer getConContainer(int side) {
		return side == 0 ? null : getNeighborCap(Objects.GAS_CAP, EnumFacing.VALUES[(side + getOrientation()) % 6]);
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
		if (shaft instanceof IGear) {
			this.link = shaft;
			this.x = ((IGear)shaft).translationFactor();
			return true;
		} else {
			this.link = null;
			return false;
		}
	}

	private static final float LIMIT = 1e-18F;

	@Override
	public float estimatedForce(float ds) {
		run = (cfg & 0x100) != 0 ^ ((cfg & 0x200) != 0 && worldObj.getStrongPower(pos) > 0);
		if (input == null) run = false;
		else if (input.invalid()) {input = null; updateCon = true; run = false;}
		if (output == null) run = false;
		else if (output.invalid()) {output = null; updateCon = true; run = false;}
		if (!run) return 0;
		GasState in = input.network.gas, out = output.network.gas;
		if ((out.type != in.type && out.P() > LIMIT) || in.P() < LIMIT) {run = false; return 0;}
		else run = true;
		double x0 = Ain / in.V;
		x0 *= 2F - Ain / Aout;
		return (float)(in.E() * x0 - out.E() * Aout / out.V) * x;
		/*
		float x0;
		if (ds <= 0) {
			x0 = Ain / input.V;
			x0 *= 2F - Ain / Aout;
			return input.E() * x0 - output.E() * Aout / output.V;
		}
		float x1, x2, dV = ds * Vin;
		x0 = Vin / (input.V + dV);
		x1 = output.nR / (output.nR + input.nR * x0 * ds);
		x2 = 1F - Vin / Vout + x1 * dV / output.V;
		x2 *= input.V / (input.V + dV);
		x1 *= Vout / output.V;
		x0 *= 1F + x2;
		 */
	}

	@Override
	public float work(float ds, float v) {
		if (!run) return power = 0;
		ds *= x;
		GasState in = input.network.gas, out = output.network.gas;
		double E = in.E() + out.E();
		GasState s = in.extract(Ain * ds);
		double sqA = s.T * s.nR * Ain / out.P() / ds;
		if (sqA > Amax * Amax || Double.isNaN(sqA)) Aout = Amax;
		else if (sqA < Amin * Amin) Aout = Amin;
		else Aout = (float)Math.sqrt(sqA);
		s.adiabat(Aout * ds);
		out.inject(s);
		return power = (float)(E - in.E() - out.E());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		Ain = nbt.getFloat("Ain");
		Aout = nbt.getFloat("Aout");
		cfg = nbt.getInteger("cfg");
		updateCon = true;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setFloat("Ain", Ain);
		nbt.setFloat("Aout", Aout);
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
	protected void customPlayerCommand(byte cmd, PacketBuffer dis, EntityPlayerMP player) throws IOException {
		if (cmd == 0) {
			Ain = dis.readFloat();
			if (Ain < Amin) Ain = Amin;
			else if (Ain > Amax) Ain = Amax;
		} else if (cmd == 1) {
			cfg = dis.readInt();
			updateCon = true;
		}
	}

	public int getVscaled(int i) {
		return (int)(Ain * (float)i / Amax);
	}

	@Override
	public boolean conGas(byte side) {
		side = (byte)((side + 6 - this.getOrientation()) % 6);
		return (cfg & 0xf) == side || (cfg >> 4 & 0xf) == side;
	}

	@Override
	public int[] getSyncVariables() {
		return new int[]{cfg, Float.floatToIntBits(Ain), Float.floatToIntBits(Aout), Float.floatToIntBits(power)};
	}

	@Override
	public void setSyncVariable(int i, int v) {
		switch(i) {
		case 0: cfg = v; break;
		case 1: Ain = Float.intBitsToFloat(v); break;
		case 2: Aout = Float.intBitsToFloat(v); break;
		case 3: power = Float.intBitsToFloat(v); break;
		}
	}

}
