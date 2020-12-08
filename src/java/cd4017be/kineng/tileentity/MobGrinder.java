package cd4017be.kineng.tileentity;

import static cd4017be.kineng.recipe.KineticProcess.FRICTION_V0;
import static cd4017be.lib.network.Sync.Type.I16;
import static java.lang.Float.floatToIntBits;
import static net.minecraft.enchantment.EnchantmentHelper.getEnchantmentLevel;
import static net.minecraft.init.Enchantments.*;
import java.util.*;
import com.mojang.authlib.GameProfile;
import cd4017be.kineng.block.BlockRotaryTool;
import cd4017be.kineng.physics.DynamicForce;
import cd4017be.lib.block.AdvancedBlock.ITilePlaceHarvest;
import cd4017be.lib.network.Sync;
import cd4017be.lib.tileentity.BaseTileEntity.ITickableServerOnly;
import cd4017be.lib.util.SaferFakePlayer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.WorldServer;
import net.minecraftforge.items.ItemHandlerHelper;

/** 
 * @author CD4017BE */
public class MobGrinder extends KineticMachine implements ITickableServerOnly, ITilePlaceHarvest {

	public static float MIN_HP = 1, DMG_J = 0.001F;
	public static double F_BASE = 1000;
	public static int T_CHECK = 50;

	Work work = new Work();
	@Sync ItemStack item = ItemStack.EMPTY;
	@Sync(type = I16) int t;
	EntityDamageSource damageSource;
	GameProfile owner = new GameProfile(new UUID(0, 0), "???");

	@Override
	protected DynamicForce createForce(BlockRotaryTool block) {
		con.maxF *= 1 + getEnchantmentLevel(UNBREAKING, item);
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
		float dmg_J = (getEnchantmentLevel(SHARPNESS, item) * 0.25F + 1F) * DMG_J;
		int fire = getEnchantmentLevel(FIRE_ASPECT, item) << 2;
		for (EntityLivingBase e : list) {
			double F = torque(e), E = F * v;
			float dmg = (float)E * dmg_J;
			if (dmg < 1F) work.Eacc += E;
			else if (!e.attackEntityFrom(damageSource, dmg)) {
				work.F1 -= F;
				work.Eacc += E;
			} else if (fire > 0) e.setFire(fire);
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
			p.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, item);
			damageSource = new EntityDamageSource("kineng.grinder", p);
		}
		return damageSource;
	}

	@Override
	protected void storeState(NBTTagCompound nbt, int mode) {
		super.storeState(nbt, mode);
		if (mode == SAVE) {
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
		this.item = ItemHandlerHelper.copyStackWithSize(item, 1);
	}

	@Override
	public List<ItemStack> dropItem(IBlockState state, int fortune) {
		List<ItemStack> list = new ArrayList<>();
		list.add(item);
		this.item = ItemStack.EMPTY;
		return list;
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
