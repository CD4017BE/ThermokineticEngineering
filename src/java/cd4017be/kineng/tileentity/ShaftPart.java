package cd4017be.kineng.tileentity;

import static net.minecraft.block.BlockRotatedPillar.AXIS;
import static net.minecraft.util.EnumFacing.AxisDirection.*;
import cd4017be.kineng.block.BlockShaft;
import cd4017be.kineng.block.BlockShaft.ShaftMaterial;
import cd4017be.kineng.physics.*;
import cd4017be.lib.block.AdvancedBlock.IInteractiveTile;
import cd4017be.lib.tileentity.BaseTileEntity;
import cd4017be.lib.util.Orientation;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;

/** @author CD4017BE */
public class ShaftPart extends BaseTileEntity implements IShaftPart, IInteractiveTile {

	/** [m^5] moment of inertia per density */
	public static final double J_ρ = Formula.J_cylinder(0.25, 1.0);
	public static double BASE_STRENGHT = 1;
	protected ShaftAxis shaft;
	protected double vSave;

	protected ShaftMaterial material() {
		return ((BlockShaft)getBlockType()).shaftMat;
	}

	@Override
	public double J() {
		return material().ρ * J_ρ;
	}

	@Override
	public double maxTorque() {
		return material().strength * BASE_STRENGHT;
	}

	@Override
	public void onOverload(double F, double lim) {
		// world.createExplosion(null, pos.getX() + .5, pos.getY() + .5, pos.getZ() +
		// .5, 1F, true);
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
	public String model() {
		return "shaft " + material().texture.toString();
	}

	@Override
	public String capModel(boolean end) {
		return (end ? "cap1 " : "cap0 ") + material().texture.toString();
	}

	@Override
	protected void storeState(NBTTagCompound nbt, int mode) {
		if(shaft != null) {
			nbt.setDouble("v", shaft.ω());
			if(mode == SYNC) nbt.setDouble("s", shaft.φ());
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
	public boolean onActivated(
		EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing s, float X, float Y, float Z
	) {
		if(world.isRemote) return true;
		final double hitSpeed = 0.05, hitForce = 1.0;
		double d = player.getPositionEyes(0).subtract(pos.getX() + X, pos.getY() + Y, pos.getZ() + Z)
		.crossProduct(new Vec3d(EnumFacing.getFacingFromAxis(POSITIVE, axis()).getDirectionVec()))
		.normalize().dotProduct(new Vec3d(X - 0.5, Y - 0.5, Z - 0.5));
		double v = shaft.ω() / hitSpeed * d, f = 1 / (1.0 + v * v);
		shaft.pulse(hitForce * f * d);
		player.addExhaustion((float)Math.max(v * f * 2, 0));
		player.sendStatusMessage(new TextComponentString(String.format("v = %.3g r/t", shaft.ω())), true);
		return true;
	}

	@Override
	public void onClicked(EntityPlayer player) {}

}
