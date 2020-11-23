package cd4017be.kineng.tileentity;

import static cd4017be.kineng.recipe.KineticProcess.FRICTION_V0;
import static java.lang.Float.floatToIntBits;
import java.util.List;
import java.util.UUID;
import com.mojang.authlib.GameProfile;
import cd4017be.kineng.block.BlockRotaryTool;
import cd4017be.kineng.physics.DynamicForce;
import cd4017be.lib.block.AdvancedBlock.ITilePlaceHarvest;
import cd4017be.lib.tileentity.BaseTileEntity.ITickableServerOnly;
import cd4017be.lib.util.SaferFakePlayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.WorldServer;

/** 
 * @author CD4017BE */
public class MobGrinder extends KineticMachine implements ITickableServerOnly, ITilePlaceHarvest {

	public static float MIN_HP = 1, DMG_J = 0.001F;
	public static double F_BASE = 1000;
	public static int T_CHECK = 50;

	Work work = new Work();
	int t;
	EntityDamageSource damageSource;
	GameProfile owner = new GameProfile(new UUID(0, 0), "???");

	@Override
	protected DynamicForce createForce(BlockRotaryTool block) {
		return work;
	}

	@Override
	public void update() {
		if (--t > 0) return;
		work.F1 = 0;
		List<EntityLivingBase> list = world.getEntitiesWithinAABB(
			EntityLivingBase.class, block().getSize(pos, getBlockState()),
			(e) -> !e.isDead && !e.isEntityInvulnerable(source())
		);
		if (list.isEmpty()) {
			t = T_CHECK;
			return;
		}
		t = 5;
		for (EntityLivingBase e : list) work.F1 += torque(e);
		double v = work.Eacc / work.F1;
		work.Eacc = 0;
		if (v <= 0) return;
		for (EntityLivingBase e : list) {
			double F = torque(e), E = F * v;
			if (!e.attackEntityFrom(damageSource, (float)E * DMG_J)) {
				work.F1 -= F;
				work.Eacc += E;
			}
			t = Math.max(t, e.hurtResistantTime);
		}
	}

	private static double torque(EntityLivingBase e) {
		//cheap log base 2
		int ln2 = floatToIntBits(e.getMaxHealth()) - floatToIntBits(MIN_HP);
		return ln2 <= 0 ? F_BASE : ((double)ln2 * 0x1p-23 + 1.0) * F_BASE;
	}

	private DamageSource source() {
		if (damageSource == null) {
			SaferFakePlayer p = new SaferFakePlayer((WorldServer)world, owner);
			p.setPosition(pos.getX()+ 0.5, pos.getY() + 10.5, pos.getZ() + 0.5);
			damageSource = new EntityDamageSource("kineng.grinder", p);
		}
		return damageSource;
	}

	@Override
	protected void storeState(NBTTagCompound nbt, int mode) {
		super.storeState(nbt, mode);
		if (mode == SAVE) {
			nbt.setShort("t", (short)t);
			nbt.setDouble("E", work.Eacc);
			nbt.setDouble("F", work.F1);
			nbt.setUniqueId("FPuuid", owner.getId());
			nbt.setString("FPname", owner.getName());
		}
	}

	@Override
	protected void loadState(NBTTagCompound nbt, int mode) {
		super.loadState(nbt, mode);
		if (mode == SAVE) {
			t = nbt.getShort("t");
			work.Eacc = nbt.getDouble("E");
			work.F1 = nbt.getDouble("F");
			owner = new GameProfile(nbt.getUniqueId("FPuuid"), nbt.getString("FPname"));
		}
	}

	@Override
	public void onPlaced(EntityLivingBase entity, ItemStack item) {
		if (entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer)entity;
			owner = player.getGameProfile();
		}
	}

	@Override
	public List<ItemStack> dropItem(IBlockState state, int fortune) {
		return makeDefaultDrops(null);
	}

	static class Work extends DynamicForce {

		double Eacc, F1;

		@Override
		public void work(double dE, double ds, double v) {
			Eacc -= dE;
			Fdv = -F1 / (Math.abs(v) + FRICTION_V0);
		}

	}

}
