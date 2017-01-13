package cd4017be.thermokin.tileentity;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import cd4017be.lib.Gui.DataContainer;
import cd4017be.lib.Gui.DataContainer.IGuiData;
import cd4017be.lib.templates.IPipe;
import cd4017be.lib.templates.MultiblockTile;
import cd4017be.lib.util.Utils;
import cd4017be.thermokin.multiblock.GasContainer;
import cd4017be.thermokin.multiblock.GasPhysics;
import cd4017be.thermokin.multiblock.GasPhysics.IGasCon;
import cd4017be.thermokin.multiblock.HeatReservoir;
import cd4017be.thermokin.multiblock.IHeatReservoir;
import cd4017be.thermokin.multiblock.IHeatReservoir.IHeatStorage;
import cd4017be.thermokin.physics.GasState;
import cd4017be.thermokin.physics.Substance;

public class GasPipe extends MultiblockTile<GasContainer, GasPhysics> implements IHeatStorage, IGasCon, IPipe, IGuiData {

	public static final float size = 0.25F;
	private Cover cover = null;

	public GasPipe() {
		comp = new GasContainer(this, size);
	}

	@Override
	public void update() {
		if (!worldObj.isRemote) super.update();
	}

	@Override
	public boolean onActivated(EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing dir, float X, float Y, float Z) {
		if (worldObj.isRemote) return true;
		if (!player.isSneaking() && item == null) return super.onActivated(player, hand, item, dir, X, Y, Z);
		if (cover != null) {
			if (player.isSneaking() && item == null) {
				
				this.dropStack(cover.item);
				cover = null;
				this.markUpdate();
				return true;
			}
			return false;
		}
		dir = this.getClickedSide(X, Y, Z);
		byte s = (byte)dir.ordinal();
		if (player.isSneaking() && item == null) {
			boolean t = !comp.canConnect(s);
			comp.setConnect(s, t);
			this.markUpdate();
			TileEntity te = Utils.getTileOnSide(this, s);
			if (te != null && te instanceof GasPipe) {
				GasContainer pipe = ((GasPipe)te).comp;
				pipe.setConnect(s, t);
				((GasPipe)te).markUpdate();
			}
			return true;
		} 
		if (player.isSneaking()) return false;
		if (item != null && (cover = Cover.create(item)) != null) {
			if (--item.stackSize <= 0) item = null;
			player.setHeldItem(hand, item);
			this.markUpdate();
			return true;
		} else return false;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		comp.writeToNBT(nbt, "gas");
		if (cover != null) cover.write(nbt, "cover");
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) 
	{
		super.readFromNBT(nbt);
		cover = Cover.read(nbt, "cover");
		comp = GasContainer.readFromNBT(this, nbt, "gas", size);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		cover = Cover.read(pkt.getNbtCompound(), "cover");
		comp.con = pkt.getNbtCompound().getByte("con");
		this.markUpdate();
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbt = new NBTTagCompound();
		if (cover != null) cover.write(nbt, "cover");
		nbt.setByte("con", comp.con);
		return new SPacketUpdateTileEntity(getPos(), -1, nbt);
	}

	@Override
	public int textureForSide(byte s) {
		if (s < 0 || s > 5) return 0;
		if (!comp.canConnect(s)) return -1;
		TileEntity te = Utils.getTileOnSide(this, s);
		return te != null && te instanceof IGasCon && ((IGasCon)te).conGas((byte)(s^1)) ? 0 : -1;
	}

	@Override
	public Cover getCover() 
	{
		return cover;
	}
	
	@Override
	public void breakBlock() 
	{
		super.breakBlock();
		if (cover != null) this.dropStack(cover.item);
	}

	@Override
	public boolean conGas(byte side) {
		return comp.canConnect(side);
	}

	@Override
	public void initContainer(DataContainer container) {
		if (worldObj.isRemote) container.extraRef = new GasState(Substance.Default, 0, 0, 0);
	}

	@Override
	public boolean detectAndSendChanges(DataContainer container, PacketBuffer dos) {
		if (comp.network == null) return false;
		GasState gas = comp.network.gas;
		dos.writeFloat((float)gas.V);
		dos.writeFloat((float)gas.nR);
		dos.writeFloat((float)gas.T);
		return true;
	}

	@Override
	public void updateClientChanges(DataContainer container, PacketBuffer dis) {
		GasState gas = (GasState)container.extraRef;
		gas.V = dis.readFloat();
		gas.nR = dis.readFloat();
		gas.T = dis.readFloat();
	}

	@Override
	public IHeatReservoir getHeat(byte side) {
		return comp;
	}

	@Override
	public float getHeatRes(byte side) {
		Float r = HeatReservoir.blocks.get(cover == null ? Material.AIR : cover.block.getMaterial());
		return r == null ? HeatReservoir.def_block : r.floatValue();
	}

}
