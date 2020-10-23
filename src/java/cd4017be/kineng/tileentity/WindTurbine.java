package cd4017be.kineng.tileentity;

import static cd4017be.kineng.block.BlockGear.DIAMETER;
import static java.lang.Double.*;
import static net.minecraft.util.EnumFacing.getFacingFromAxis;
import static net.minecraft.util.EnumFacing.AxisDirection.NEGATIVE;
import static net.minecraft.util.EnumFacing.AxisDirection.POSITIVE;
import java.util.List;
import java.util.Random;
import cd4017be.kineng.block.BlockTurbine;
import cd4017be.kineng.physics.*;
import cd4017be.lib.block.AdvancedBlock.IInteractiveTile;
import cd4017be.lib.block.AdvancedBlock.ITilePlaceHarvest;
import cd4017be.lib.tileentity.BaseTileEntity.ITickableServerOnly;
import cd4017be.lib.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

/** 
 * @author CD4017BE */
public class WindTurbine extends ShaftPart implements ITickableServerOnly, IInteractiveTile, ITilePlaceHarvest {

	/** [kg/m³] */
	public static double AIR_DENSITY = 1.29;
	public static int DT0 = 20, DT_RAND = 40;
	public static float WIND_SCALE = 1000F;

	ForceCon con;
	double a, v0;
	float obstr = 1F;
	int t;

	@Override
	public void update() {
		if (con == null) return;
		((Turbine)con.force).v_air += a;
		if (--t > 0) return;
		updateWindSpeed();
	}

	public void updateWindSpeed() {
		Random rand = world.rand;
		t = DT0 + rand.nextInt(DT_RAND);
		int r = (int)Math.ceil(block().radius(getBlockState()) * 2.0), d1, d2;
		do {
			d1 = rand.nextInt(r) - rand.nextInt(r);
			d2 = rand.nextInt(r) - rand.nextInt(r);
		} while(d1 == 0 && d2 == 0);
		Axis axis = axis();
		BlockPos pos0;
		switch(axis) {
		case X: pos0 = new BlockPos(pos.getX(), pos.getY() + d1, pos.getZ() + d2); break;
		case Y: pos0 = new BlockPos(pos.getX() + d1, pos.getY(), pos.getZ() + d2); break;
		default: pos0 = new BlockPos(pos.getX() + d1, pos.getY() + d2, pos.getZ());
		}
		boolean dir = rand.nextBoolean();
		EnumFacing face = getFacingFromAxis(dir ? POSITIVE : NEGATIVE, axis);
		obstr += (1F - obstr) * 0.0625F;
		for (int i = r; i >= 0; i--) {
			pos0 = pos0.offset(face);
			Chunk chunk = world.getChunkProvider().getLoadedChunk(pos0.getX() >> 4, pos0.getZ() >> 4);
			if (chunk == null) break;
			else if (chunk.getBlockState(pos0).getMaterial().blocksMovement()) {
				float d = 1F - (float)i / (float)r;
				d *= dir ^ v0 > 0 ? 2F : 8F;
				obstr -= 0.0625F / (d*d + 1F);
				break;
			}
		}
		float v = (1F + 0.5F * world.thunderingStrength * (rand.nextFloat() + 1F)) * obstr;
		a = (v * v0 - ((Turbine)con.force).v_air) / (double)t;
	}

	@Override
	public double setShaft(ShaftAxis shaft) {
		if (con == null && shaft != null) {
			BlockTurbine block = block();
			IBlockState state = getBlockState();
			double r = block.radius(state);
			con = new ForceCon(this, 2.5);
			con.link(new Turbine(r, AIR_DENSITY));
			con.maxF = block.maxF[state.getValue(DIAMETER)];
		}
		con.setShaft(shaft);
		return super.setShaft(shaft);
	}

	@Override
	protected void storeState(NBTTagCompound nbt, int mode) {
		super.storeState(nbt, mode);
		nbt.setFloat("v0", (float)v0);
		nbt.setFloat("cl", obstr);
	}

	@Override
	protected void loadState(NBTTagCompound nbt, int mode) {
		super.loadState(nbt, mode);
		v0 = nbt.getFloat("v0");
		obstr = nbt.getFloat("cl");
	}

	@Override
	public void onPlaced(EntityLivingBase entity, ItemStack item) {
		if (world.isRemote) return;
		Vec3d v = baseWindSpeed(world, pos);
		switch(axis()) {
		case X: v0 = v.x; break;
		case Y: v0 = v.y; break;
		default: v0 = v.z; break;
		}
		if (entity instanceof EntityPlayer)
			((EntityPlayer)entity).sendStatusMessage(new TextComponentString(
				String.format("base wind speed: %.3g m/s", v0)
			), true);
	}

	@Override
	public List<ItemStack> dropItem(IBlockState state, int fortune) {
		return makeDefaultDrops(null);
	}

	@Override
	public boolean onActivated(
		EntityPlayer player, EnumHand hand, ItemStack item, EnumFacing s, float X, float Y, float Z
	) {
		if (con == null || world.isRemote) return true;
		double v = ((Turbine)con.force).v_air;
		if (Utils.coord(X, Y, Z, getFacingFromAxis(POSITIVE, axis())) > 0.5) v = -v;
		player.sendStatusMessage(new TextComponentString(
			String.format("current wind: %.1f m/s", v)
		), true);
		return true;
	}

	@Override
	public void onClicked(EntityPlayer player) {}

	static class Turbine extends DynamicForce {

		final double A;
		double v_air;

		public Turbine(double r, double d) {
			A = 0.5 * Math.PI * r * r * d;
		}

		@Override
		public void work(double dE, double ds, double v) {
			//Fdv = -abs((v_air - v) * A)
			Fdv = longBitsToDouble(doubleToRawLongBits((v_air - v) * A) | 0x80000000_00000000L);
			F = -v_air * Fdv;
		}

	}

	public static Vec3d baseWindSpeed(World world, BlockPos pos) {
		MutableBlockPos pos1 = new MutableBlockPos();
		final int r = 7;
		float[] arr = new float[(r+r+1) * (r+r+1) << 1];
		float y = 0, ud = 0;
		for (int i = 0, dx = -r; dx <= r; dx++)
			for (int dz = -r; dz <= r; dz++, i+=2) {
				y += getAtmParam(world, pos1.setPos(
					pos.getX() + (dx << 4), pos.getY(), pos.getZ() + (dz << 4)
				), arr, i);
				ud += arr[i];
			}
		y /= arr.length >> 1;
		ud /= arr.length >> 1;
		float vx = 0, vz = 0, vy = 0, c = 0, s = 0, d = 0;
		for (int i = 0, x = -r; x <= r; x++)
			for (int z = -r; z <= r; z++, i+=2) {
				float w = 1F / (x*x + z*z + y*y);
				float updraft = arr[i] * w;
				vx += updraft * x;
				vz += updraft * z;
				vy += (updraft - ud * w) * y;
				c += arr[i+1] * w;
				s += w;
				d += w * Math.abs(x);
			}
		d = WIND_SCALE * c / (s * d);
		s = WIND_SCALE * c / (s * s);
		vx *= d; vy *= s; vz *= d;
		return new Vec3d(
			Math.copySign(Math.sqrt(Math.abs(vx)), vx),
			Math.copySign(Math.sqrt(Math.abs(vy)), vy),
			Math.copySign(Math.sqrt(Math.abs(vz)), vz)
		);
	}

	/**@param world
	 * @param pos
	 * @param buffer [updraft, clearance]
	 * @param i buffer store base pointer */
	static float getAtmParam(World world, BlockPos pos, float[] buffer, int i) {
		Biome b = world.getBiome(pos);
		buffer[i] = b.getDefaultTemperature();
		float forest = 0.2F - 1F / (5F + b.decorator.treesPerChunk);
		float height = b.getBaseHeight() + forest * 0.5F;
		float var = b.getHeightVariation() + forest;
		if (height < var)
			var = height = Math.max((height + var) * 0.5F, 0);
		height = Math.max((pos.getY() - world.getSeaLevel()) / 32F - height, 0.5F);
		buffer[i+1] = height / (height + var);
		return height;
	}

}
