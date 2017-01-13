package cd4017be.thermokin.multiblock;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.templates.MultiblockComp;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.multiblock.ShaftPhysics.IKineticComp;

public class ShaftComponent extends MultiblockComp<ShaftComponent, ShaftPhysics> {

	public float m;
	public byte type = 0;

	public ShaftComponent(ModTileEntity shaft, float m) {
		super(shaft);
		this.m = m;
	}

	@Override
	public void setUID(long uid) {
		super.setUID(uid);
		if (network == null) new ShaftPhysics(this);
	}

	public byte getCon() {return con;}

	public void setCon(int i, boolean c) {
		if (c) con |= 1 << i;
		else con &= ~(1 << i);
	}

	@Override
	public boolean canConnect(byte side) {
		return side / 2 == ((TileEntity)tile).getBlockMetadata();
	}

	public static ShaftComponent readFromNBT(ModTileEntity tile, NBTTagCompound nbt) {
		ShaftComponent pipe = new ShaftComponent(tile, nbt.getFloat("mass"));
		ShaftPhysics physics = new ShaftPhysics(pipe);
		physics.v = nbt.getFloat("rotVel");
		physics.s = nbt.getFloat("rotPos");
		return pipe;
	}
	
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setFloat("mass", m);
		if (network != null) {
			nbt.setFloat("rotVel", network.v);
			nbt.setFloat("rotPos", network.s);
		}
	}
	
	public void remove() {
		if (network != null) network.remove(this);
	}

	public boolean supports(IKineticComp con2, byte i) {
		return true;//(con >> i & 1) == 0;
	}

	private static final double[] loss = {Math.sqrt(0.99F), Math.sqrt(0.999F), 1};
	
	public double getCoilLoss() {
		return loss[type >= 2 && type < 4 ? type - 2 : 2];
	}

	@Override
	public Capability<ShaftComponent> getCap() {
		return Objects.SHAFT_CAP;
	}

}
