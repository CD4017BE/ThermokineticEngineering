package cd4017be.kineng.tileentity;

import static net.minecraft.util.EnumFacing.AxisDirection.POSITIVE;
import java.util.*;
import cd4017be.kineng.block.BlockRotaryTool;
import cd4017be.kineng.physics.*;
import cd4017be.lib.block.AdvancedBlock.IInteractiveTile;
import cd4017be.lib.block.AdvancedBlock.INeighborAwareTile;
import cd4017be.lib.tileentity.BaseTileEntity.ITickableServerOnly;
import cd4017be.lib.util.TooltipUtil;
import cd4017be.math.cplx.CplxF;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.chunk.Chunk;

/** 
 * @author CD4017BE */
public class ManualPower extends KineticMachine implements IInteractiveTile, INeighborAwareTile, ITickableServerOnly {

	public static float EXHAUSTION_TICK, HURT_CHANCE = 0.5F;
	public static int CLICK_TICKS, CHECK_INTERVAL, MAX_TIME;
	public static final HashMap<Class<?>, CplxF> ENTITY_STRENGTH  = new HashMap<>();

	Worker force = new Worker();
	int t, remT;
	boolean hasFence;

	@Override
	public void update() {
		if (--t > 0) return;
		t = CHECK_INTERVAL;
		force.F = force.Fdv = 0.0;
		
		if (!hasFence) return;
		double v = shaft.av() * con.r;
		int n = 0;
		remT = 0;
		CplxF acc = new CplxF();
		Random rand = world.rand;
		double px = pos.getX() + 0.5, py = pos.getY() + 0.5, pz = pos.getZ() + 0.5;
		for (EntityLiving e : world.getEntitiesWithinAABB(EntityLiving.class,
			new AxisAlignedBB(px - 8.0, py - 1.5, pz - 8.0, px + 8.0, py + 1.5, pz + 8.0)
		)) {
			Entity lh = e.getLeashHolder();
			if (
				lh == null || Math.abs(py - lh.posY) >= 1.25
				|| Math.abs(px - lh.posX) + Math.abs(pz - lh.posZ) >= 0.25
				|| e.getHealth() < e.getMaxHealth()
			) continue;
			CplxF str = ENTITY_STRENGTH.get(e.getClass());
			if (str == null) continue;
			double F = Math.copySign(str.r, lh.posY - py), dv = v * F / str.i - 0.5;
			dv = Math.max(1.0 - 4.0 * dv * dv, 0);
			if (e instanceof EntityAgeable) {
				EntityAgeable ea = (EntityAgeable)e;
				int age = ea.getGrowingAge();
				if (age < -MAX_TIME) {
					F = 0;
					dv = 0;
				} else {
					if (age > 0) age = 0;
					remT += Math.max(age + MAX_TIME, 0);
				}
				ea.setGrowingAge(age - (int)(CHECK_INTERVAL * (1.0 + dv)));
				n++;
			} else if (rand.nextFloat() < dv)
				e.setHealth(e.getMaxHealth() * 0.5F);
			acc.add(str);
			force.F += F;
		}
		if (force.F != 0)
			force.Fdv = -Math.abs(force.F * acc.r / acc.i);
		if (n > 0) remT /= n;
	}

	@Override
	protected DynamicForce createForce(BlockRotaryTool block) {
		neighborBlockChange(block, pos);
		return force;
	}

	@Override
	public void neighborBlockChange(Block b, BlockPos src) {
		Chunk c = getChunk();
		hasFence = c.getBlockState(pos.down()).getBlock() instanceof BlockFence
			|| c.getBlockState(pos.up()).getBlock() instanceof BlockFence;
	}

	@Override
	public void neighborTileChange(TileEntity te, EnumFacing side) {}

	@Override
	public boolean onActivated(
		EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing s, float X, float Y, float Z
	) {
		if(world.isRemote) return true;
		double v = shaft.av() * con.r;
		if (!hasFence) {
			CplxF e = ENTITY_STRENGTH.getOrDefault(EntityPlayer.class, new CplxF());
			double v1 = Math.copySign(e.i / e.r, player.getPositionEyes(0).subtract(pos.getX() + X, pos.getY() + Y, pos.getZ() + Z)
				.crossProduct(new Vec3d(EnumFacing.getFacingFromAxis(POSITIVE, axis()).getDirectionVec()))
				.dotProduct(new Vec3d(X - 0.5, Y - 0.5, Z - 0.5)));
			force.updateF(e.r, v1);
			float t = (CLICK_TICKS - this.t) * EXHAUSTION_TICK;
			remT = 0;
			this.t = CLICK_TICKS;
			double dv = 1.0 - (v / v1) * 2.0;
			player.addExhaustion(Math.max(0, (float)(1.0 - dv * dv) * t));
		}
		player.sendStatusMessage(new TextComponentString(TooltipUtil.format(
			"\\%.3um/s * %.3uN [%.1fs]",
			v, force.F + force.Fdv * v, remT * 0.05
		)), true);
		return true;
	}

	@Override
	public void onClicked(EntityPlayer player) {}

	static class Worker extends DynamicForce {

		public void updateF(double F_max, double v_max) {
			this.F = Math.copySign(F_max, v_max);
			this.Fdv = -F_max / Math.abs(v_max);
		}

	}

}
