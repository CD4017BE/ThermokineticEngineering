package cd4017be.kineng.tileentity;

import static net.minecraft.block.BlockRotatedPillar.AXIS;
import static net.minecraft.util.EnumFacing.AxisDirection.*;
import java.util.ArrayList;
import java.util.Random;
import cd4017be.kineng.block.BlockShaft;
import cd4017be.kineng.block.BlockShaft.ShaftMaterial;
import cd4017be.kineng.physics.*;
import cd4017be.kineng.render.PartModels;
import cd4017be.lib.tileentity.BaseTileEntity;
import cd4017be.lib.util.Orientation;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemHandlerHelper;

/** @author CD4017BE */
public class ShaftPart extends BaseTileEntity implements IShaftPart {

	protected ShaftAxis shaft;
	protected double vSave;

	@SuppressWarnings("unchecked")
	public <T extends BlockShaft> T block() {
		return (T)getBlockState().getBlock();
	}

	protected ShaftMaterial material() {
		return block().shaftMat;
	}

	@Override
	public double J() {
		return block().J(getBlockState());
	}

	@Override
	public double maxTorque() {
		return block().maxM(getBlockState());
	}

	@Override
	public double maxSpeed() {
		return block().maxAv(getBlockState());
	}

	@Override
	public void handleOverload() {
		EnumFacing dir = EnumFacing.getFacingFromAxis(AxisDirection.POSITIVE, axis());
		Orientation o = Orientation.fromFacing(dir);
		double E = shaft != null ? shaft.av() : 0;
		Vec3d vel = new Vec3d(dir.getDirectionVec()).scale(MathHelper.clamp(E * Ticking.dt, -1, 1));
		E = MathHelper.clamp(Math.log(E * E * J() * 0.001), 0.5, 16.0);
		ArrayList<ItemStack> items = new ArrayList<>();
		double r = block().getDebris(getBlockState(), items);
		world.setBlockToAir(pos);
		double x0 = pos.getX() + 0.5, y0 = pos.getY() + 0.5, z0 = pos.getZ() + 0.5;
		world.createExplosion(null, x0, y0, z0, (float)E, false);
		Random rand = world.rand;
		for (ItemStack stack : items) {
			int n = stack.getCount();
			while(n > 0) {
				Vec3d vec = new Vec3d(rand.nextDouble() * 2.0 - 1.0, rand.nextFloat() * 2.0 - 1.0, 0.0);
				if (vec.lengthSquared() > 1.0) continue;
				vec = o.rotate(vec).scale(r);
				EntityItem ei = new EntityItem(world);
				ei.setItem(ItemHandlerHelper.copyStackWithSize(stack, 1));
				ei.setPosition(vec.x + x0, vec.y + y0, vec.z + z0);
				vec = vec.crossProduct(vel);
				ei.motionX = vec.x;
				ei.motionY = vec.y;
				ei.motionZ = vec.z;
				world.spawnEntity(ei);
				n--;
			}
		}
	}

	@Override
	public Axis axis() {
		return getBlockState().getValue(AXIS);
	}

	@Override
	public IShaftPart next(boolean dir) {
		Axis axis = axis();
		TileEntity te = Utils.neighborTile(
			this, EnumFacing.getFacingFromAxis(dir ? POSITIVE : NEGATIVE, axis)
		);
		return te instanceof IShaftPart && ((IShaftPart)te).axis() == axis ? (IShaftPart)te : null;
	}

	@Override
	public ShaftAxis getShaft() {
		return shaft;
	}

	@Override
	public double setShaft(ShaftAxis shaft) {
		double v = setShaft(shaft, vSave);
		this.shaft = shaft;
		return v;
	}

	@Override
	public void onLoad() {
		super.onLoad();
		Ticking.of(this).updateCon.add(this);
	}

	@Override
	protected void onUnload() {
		super.onUnload();
		vSave = setShaft(null);
	}

	@Override
	public void syncToClient() {
		markDirty(SYNC);
	}

	@Override
	public Orientation getOrientation() {
		return Orientation.fromFacing(EnumFacing.getFacingFromAxis(NEGATIVE, axis()));
	}

	@Override
	protected void storeState(NBTTagCompound nbt, int mode) {
		if(shaft != null) {
			nbt.setDouble("v", shaft.av());
			if(mode == SYNC) nbt.setDouble("s", shaft.ang());
		} else nbt.setDouble("v", vSave);
	}

	@Override
	protected void loadState(NBTTagCompound nbt, int mode) {
		if(mode < SYNC)
			vSave = nbt.getDouble("v");
		else if(shaft != null)
			shaft.onSpeedSync(nbt.getDouble("v"), nbt.getDouble("s"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int[] model() {
		return block().model(getBlockState());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int[] capModel(boolean end) {
		return new int[] {PartModels.SHAFT_CAPS, material().texture, end ? 4 : -4};
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		return block().getSize(pos, getBlockState());
	}

}
