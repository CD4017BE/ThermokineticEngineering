package cd4017be.thermokin.multiblock;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import cd4017be.lib.ModTileEntity;
import cd4017be.lib.templates.MultiblockComp;
import cd4017be.thermokin.Objects;

public class ShaftComponent extends MultiblockComp<ShaftComponent, ShaftPhysics> {

	public float m;
	public ItemStack type = null;
	public String model = "thermokin:models/tileentity/shaft";

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

	public void readFromNBT(NBTTagCompound nbt) {
		ShaftPhysics physics = network;
		if (physics == null) physics = new ShaftPhysics(this);
		physics.v = nbt.getFloat("rotVel");
		physics.s = nbt.getFloat("rotPos");
	}

	public void writeToNBT(NBTTagCompound nbt) {
		if (type != null) nbt.setTag("type", type.writeToNBT(new NBTTagCompound()));
		if (network != null) {
			nbt.setFloat("rotVel", network.v);
			nbt.setFloat("rotPos", network.s);
		}
	}

	public void onRemove() {
		((ModTileEntity)tile).dropStack(type);
	}

	@Override
	public Capability<ShaftComponent> getCap() {
		return Objects.SHAFT_CAP;
	}

}
