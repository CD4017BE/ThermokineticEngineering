package cd4017be.thermokin.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import cd4017be.thermokin.multiblock.LiquidComponent;
import cd4017be.thermokin.multiblock.LiquidPhysics;
import cd4017be.thermokin.multiblock.LiquidPhysics.ILiquidCon;
import cd4017be.lib.templates.IPipe;
import cd4017be.lib.templates.MultiblockTile;
import cd4017be.lib.util.Utils;

public class LiquidTube extends MultiblockTile<LiquidComponent, LiquidPhysics> implements ILiquidCon, IPipe {

	private Cover cover = null;

	public LiquidTube() {
		comp = new LiquidComponent(this);
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
			if (te != null && te instanceof LiquidTube) {
				LiquidComponent pipe = ((LiquidTube)te).comp;
				pipe.setConnect(s, t);
				((LiquidTube)te).markUpdate();
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
		if (cover != null) cover.write(nbt, "cover");
		nbt.setByte("con", comp.con);
		return super.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		cover = Cover.read(nbt, "cover");
		comp.con = nbt.getByte("con");
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
		return te != null && te instanceof ILiquidCon && ((ILiquidCon)te).conLiquid((byte)(s^1)) ? 0 : -1;
	}

	@Override
	public Cover getCover() {
		return cover;
	}

	@Override
	public void breakBlock() {
		super.breakBlock();
		if (cover != null) this.dropStack(cover.item);
	}

	@Override
	public boolean conLiquid(byte side) {
		return comp.canConnect(side);
	}

}
