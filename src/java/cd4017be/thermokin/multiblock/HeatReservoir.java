package cd4017be.thermokin.multiblock;

import java.util.HashMap;
import java.util.Map.Entry;

import cd4017be.lib.ModTileEntity;
import cd4017be.lib.util.Utils;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;

public class HeatReservoir implements IHeatReservoir {
	
	public HeatReservoir(float C) {
		this.C = C; this.T = -1F;
	}
	
	/**	[J/K] heat capacity */
	public final float C;
	/** [K] temperature */
	public float T;
	
	public float envCond, envTemp;
	public byte con;
	public boolean check = true;
	private IHeatStorage[] ref = new IHeatStorage[3];
	
	@Override
	public float T() {return T;}

	@Override
	public float C() {return C;}

	@Override
	public void addHeat(float dQ) {T += dQ / C;}
	
	public void update(ModTileEntity tile) {
		TileEntity te;
		if (check) {
			for (byte i = 0; i < 6; i++) {
				te = Utils.getTileOnSide(tile, i);
				if (te != null && te instanceof IHeatStorage) {
					if ((i & 1) == 0) ref[i >> 1] = (IHeatStorage)te;
					con &= ~(1 << i);
				} else {
					if ((i & 1) == 0) ref[i >> 1] = null;
					con |= 1 << i;
				}
			}
			envTemp = getEnvironmentTemp(tile.getWorld(), tile.getPos());
			envCond = getEnvHeatCond((IHeatStorage)tile, tile.getWorld(), tile.getPos(), con);
			if (T < 0) T = envTemp;
			check = false;
		}
		if (envCond > C) T = envTemp;
		else T += (envTemp - T) * envCond / C;
		for (int i = 0; i < 3; i++) {
			if (ref[i] == null) continue;
			if (((TileEntity)ref[i]).isInvalid()) {ref[i] = null; check = true; continue;}
			byte s = (byte)(i * 2 + 1);
			IHeatReservoir H = ref[i].getHeat(s);
			float R = ((IHeatStorage)tile).getHeatRes((byte)(i * 2)) + ref[i].getHeatRes(s);
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
	
	public static float exchangeHeat(IHeatReservoir Ha, IHeatReservoir Hb, float R) {
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
	
	public static float getEnvHeatCond(IHeatStorage tile, World world, BlockPos pos, byte sides) {//TODO use IHeatBlock and central registry
		float cond = 0;
		for (byte i = 0; i < 6; i++) 
			if ((sides >> i & 1) != 0) {
				Material m = world.getBlockState(pos.offset(EnumFacing.VALUES[i])).getMaterial();
				Float f = blocks_env.get(m);
				cond += 1F / (tile.getHeatRes(i) + (f == null ? def_block + def_env : f.floatValue()));
			}
		return cond;
	}
	
	public static float getEnvironmentTemp(World world, BlockPos pos) {//TODO use IHeatBlock and central registry
		if (world.provider.getDimensionType() == DimensionType.NETHER) return 380F; //107°C
		else if (world.provider.getDimensionType() == DimensionType.THE_END) return 250F; //-23°C
		else return 270F + world.getBiomeGenForCoords(pos).getFloatTemperature(pos) * 25F; 
		//coldTaiga= -18.1°C; icePlains= -3.1°C; plains= 11.9°C; jungle= 25.4°C; savanna= 32.9°C; desert= 56.9°C
	}
	
	/** Default Heat resistance for unlisted materials */
	public static float def_block;
	/** Default offset for environment heat resistance */
	public static float def_env;
	/** Default internal Heat resistance of machines for connected/disconnected sides */
	public static float def_con, def_discon;
	/** Heat resistance of block materials used as cover (no cover = air) */
	public static final HashMap<Material, Float> blocks = new HashMap<Material, Float>();
	/** Heat resistance of environment block materials */
	public static final HashMap<Material, Float> blocks_env = new HashMap<Material, Float>();
	static {
		def_block = 2.5F;
		def_env = 0.8F;
		blocks.put(Material.IRON, 0.01F);
		blocks.put(Material.GLASS, 1F);
		blocks.put(Material.ROCK, 1.2F);
		blocks.put(Material.CLAY, 1F);
		blocks.put(Material.GROUND, 1.5F);
		blocks.put(Material.GRASS, 2F);
		blocks.put(Material.SAND, 4F);
		blocks.put(Material.WOOD, 5F);
		blocks.put(Material.PACKED_ICE, 0.4F);
		blocks.put(Material.ICE, 0.5F);
		blocks.put(Material.CRAFTED_SNOW, 12F);
		blocks.put(Material.AIR, 25F);
		blocks.put(Material.CLOTH, 100F);
		//blocks.put(Objects.M_thermIns, 1000F);
		for (Entry<Material, Float> e : blocks.entrySet()) blocks_env.put(e.getKey(), e.getValue() + def_env);
		blocks_env.put(Material.AIR, 10F);
		blocks_env.put(Material.SNOW, 15F);
		blocks_env.put(Material.LAVA, 2.0F);
		blocks_env.put(Material.WATER, 1.2F);
		def_con = 0.004F;//heat conductivity of 1m³ copper
		//def_discon = blocks.get(Objects.M_thermIns);
	}
}
