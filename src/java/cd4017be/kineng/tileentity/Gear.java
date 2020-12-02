package cd4017be.kineng.tileentity;

import static cd4017be.kineng.block.BlockGear.*;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import cd4017be.kineng.physics.*;
import cd4017be.lib.network.Sync;
import cd4017be.lib.util.ItemFluidUtil;

/** @author CD4017BE */
public class Gear extends ShaftPart implements IGear, ISelfAwareTile {

	public static double F_FRICTION = 0, A_CONTACT;
	private final GearLink[] cons = new GearLink[4];

	@Sync(to = 0x23, tag = "cs") public ItemStack chainStack;
	@Sync(to = 0x23, tag = "cl") public BlockPos chainLink;
	@Sync public void chainLink(BlockPos pos) {
		if (shaft != null && world.isRemote && (chainLink != null || pos != null)) {
			GearLink con = getCon(null), con1 = null;
			if (pos != null) {
				TileEntity te = world.getTileEntity(pos);
				if (te instanceof IGear)
					con1 = ((IGear)te).getCon(null);
			}
			con.connect(con1);
			model = null;
		}
		chainLink = pos;
	}

	@Override
	public double setShaft(ShaftAxis shaft, double v0) {
		v0 = IGear.super.setShaft(shaft, vSave);
		this.shaft = shaft;
		return v0;
	}

	@Override
	public GearLink getCon(EnumFacing side) {
		Axis a = axis();
		if (side == null)
			side = a == Axis.Y ? EnumFacing.NORTH : EnumFacing.DOWN;
		int d = (1 - (side.getAxis().ordinal() - a.ordinal() + 4) % 3)
			* side.getAxisDirection().getOffset();
		if (d == 0) return null;
		int i = side.ordinal();
		if (i >= 2 && a != Axis.X) i -= 2;
		GearLink con = cons[i];
		if (con != null) return con;
		con = new GearLink(this, diameter() * d * 0.5);
		ShaftMaterial mat = material();
		con.maxF = mat.strength * A_CONTACT;
		con.fricD = mat.friction;
		con.fricS = mat.friction * F_FRICTION;
		return cons[i] = con;
	}

	@Override
	public int diameter() {
		return getBlockState().getValue(DIAMETER);
	}

	@Override
	public BlockPos chainLink() {
		return chainLink;
	}

	@Override
	public void linkChain(BlockPos pos1, ItemStack stack) {
		BlockPos ocl = chainLink;
		if (chainStack != null)
			ItemFluidUtil.dropStack(chainStack, world, pos);
		chainStack = stack;
		chainLink = pos1;
		markDirty(0x20);
		if (ocl == null) return;
		getCon(null).connect(null);
		TileEntity te = world.getTileEntity(ocl);
		if (te instanceof IGear) {
			IGear g = (IGear)te;
			if (pos.equals(g.chainLink()))
				g.linkChain(null, null);
		}
	}

	@Override
	public void breakBlock() {
		linkChain(null, null);
	}

	@SideOnly(Side.CLIENT)
	public int[] model;

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		AxisAlignedBB box = super.getRenderBoundingBox();
		if (chainLink != null)
			box = box.union(new AxisAlignedBB(chainLink));
		return box;
	}

}
