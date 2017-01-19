package cd4017be.thermokin.multiblock;

import cd4017be.lib.ModTileEntity;
import cd4017be.lib.util.ICachableInstance;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.recipe.Substances;
import cd4017be.thermokin.recipe.Substances.Environment;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HeatReservoir {

	public HeatReservoir(float C, float R) {
		this.C = C; this.T = -1F;
		Rc = new float[]{R, R, R, R, R, R};
	}

	/**	[J/K] heat capacity */
	public final float C;
	/** [K] temperature */
	public float T;

	public float envCond, envTemp;
	public final float[] Rc;
	public byte con;
	public boolean check = true;
	public Environment env;
	private IHeatReservoir[] ref = new IHeatReservoir[9];

	public void update(ModTileEntity tile) {
		if (check) {
			World world = tile.getWorld();
			BlockPos pos = tile.getPos();
			if (env == null) env = Substances.getEnvFor(world);
			envTemp = env.getTemp(world, pos);
			if (T < 0) T = envTemp;
			envCond = 0;
			for (EnumFacing s : EnumFacing.VALUES) {
				int i = s.ordinal();
				BlockPos pos1 = pos.offset(s);
				IBlockState state = world.getBlockState(pos1);
				TileEntity te;
				IHeatReservoir hr = null;
				if (!state.getBlock().hasTileEntity(state) || (te = world.getTileEntity(pos1)) == null || (hr = te.getCapability(Objects.HEAT_CAP, s.getOpposite())) == null)
					envCond += env.getCond(state, Rc[i]);
				if ((i & 1) == 0) ref[i >> 1] = hr;
			}
			check = false;
		}
		if (envCond > C) T = envTemp;
		else if (envCond > 0) T += (envTemp - T) * envCond / C;
		for (int i = 0; i < 3; i++) {
			IHeatReservoir H = ref[i];
			if (H == null) continue;
			if (H.invalid()) {ref[i] = null; check = true; continue;}
			float R = Rc[i * 2] + H.R();
			final float Rmin = 1F / C + 1F / H.C();
			final float dQ = (T - H.T()) / (R < Rmin ? Rmin : R);
			H.addHeat(dQ);
			T -= dQ / C;
		}
	}

	public void save(NBTTagCompound nbt, String k) {
		nbt.setFloat(k + "T", T);
	}

	public void load(NBTTagCompound nbt, String k) {
		T = nbt.getFloat(k + "T");
		check = true;
	}

	public static float exchangeHeat(IHeatReservoir Ha, IHeatReservoir Hb) {
		final float R = Ha.R() + Hb.R();
		final float Rmin = 1F / Ha.C() + 1F / Hb.C();
		final float dQ = (Ha.T() - Hb.T()) / (R < Rmin ? Rmin : R);
		Ha.addHeat(-dQ);
		Hb.addHeat(dQ);
		return dQ;
	}

	public static float exchangeHeat(IHeatReservoir H, float T, float R) {
		final float Rmin = 1F / H.C();
		final float dQ = (H.T() - T) / (R < Rmin ? Rmin : R);
		H.addHeat(dQ);
		return dQ;
	}

	public IHeatReservoir getCapability(ModTileEntity tile, EnumFacing side) {
		int i = side.ordinal();
		IHeatReservoir hr = ref[i + 3];
		if (hr == null) ref[i + 3] = hr = new Access(tile, i);
		return hr;
	}

	private class Access implements IHeatReservoir {
		final ICachableInstance tile;
		final int side;

		Access(ICachableInstance tile, int side) {
			this.tile = tile;
			this.side = side;
		}

		@Override
		public boolean invalid() {
			return tile.invalid();
		}

		@Override
		public float T() {
			return T;
		}

		@Override
		public float C() {
			return C;
		}

		@Override
		public float R() {
			return Rc[side];
		}

		@Override
		public void addHeat(float dQ) {
			T += dQ / C;
		}

	}

	public void addHeat(float dQ) {
		T += dQ / C;
	}

}
