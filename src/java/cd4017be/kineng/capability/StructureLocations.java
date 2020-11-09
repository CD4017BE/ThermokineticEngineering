package cd4017be.kineng.capability;

import java.util.*;
import cd4017be.kineng.Main;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** 
 * @author CD4017BE */
public class StructureLocations implements ICapabilityProvider, INBTSerializable<NBTBase> {

	@CapabilityInject(StructureLocations.class)
	public static final Capability<StructureLocations> MULTIBLOCKS = null;

	public static void register() {}

	static {
		CapabilityManager.INSTANCE.register(
			StructureLocations.class,
			new BasicStorage<StructureLocations>(),
			StructureLocations::new
		);
		MinecraftForge.EVENT_BUS.register(StructureLocations.class);
	}

	@SubscribeEvent
	public static void attachCapabilities(AttachCapabilitiesEvent<Chunk> event) {
		event.addCapability(new ResourceLocation(Main.ID, "mbs"), new StructureLocations());
	}

	/** Multiblock entries (12 Byte/entry):<br>
	 * {core[0:7 X, 9:16 Y, 18:25 Z, 27:31 ID],
	 * start[0:7 X, 9:16 Y, 18:25 Z, 27:31 ID],
	 *  size[0:7 X, 9:16 Y, 18:25 Z]} */
	private int[] structures;
	/** element count * 3 */
	private int count;

	/**@param pos point to be contained in region
	 * @param id structure type to look for
	 * @return iterator over structures of type id containing pos */
	public Entry find(BlockPos pos, int id) {
		return new Entry(pos, id);
	}

	public void put(int c, int p0, int s) {
		for (int i = 0; i < count; i+=3)
			if (structures[i] == c) {
				structures[i+1] = p0;
				structures[i+2] = s;
				return;
			}
		int i = count; count += 3;
		if (structures == null) structures = new int[3];
		else if (structures.length < count)
			structures = Arrays.copyOf(structures, Math.max(count, structures.length << 1));
		structures[i  ] = c;
		structures[i+1] = p0;
		structures[i+2] = s;
	}

	public long get(int c) {
		for (int i = 0; i < count; i+=3)
			if (structures[i] == c)
				return (long)structures[i+1] & 0xffffffffL | (long)structures[i+2] << 32;
		return -1;
	}

	public void remove(int c) {
		for (int i = 0; i < count; i+=3)
			if (structures[i] == c) {
				count -= 3;
				structures[i  ] = structures[count  ];
				structures[i+1] = structures[count+1];
				structures[i+2] = structures[count+2];
				return;
			}
	}

	public static void setRange(TileEntity core, int id, int x0, int y0, int z0, int x1, int y1, int z1) {
		int c = remove(core, id);
		int p0 = (x0 & 0xff) | (y0 & 0xff) << 9 | (z0 & 0xff) << 18 | id << 27;
		int s = (x1 - x0 & 0xff) | (y1 - y0 & 0xff) << 9 | (z1 - z0 & 0xff) << 18;
		x0 >>= 4; z0 >>= 4; x1 = x1 + 15 >> 4; z1 = z1 + 15 >> 4;
		if (z1 - z0 > 15 || x1 - x0 > 15)
			throw new IllegalArgumentException("Multiblock region too large!");
		World world = core.getWorld();
		for (int x = x0; x < x1; x++)
			for (int z = z0; z < z1; z++) {
				Chunk chunk = world.getChunkFromChunkCoords(x, z);
				chunk.getCapability(MULTIBLOCKS, null).put(c, p0, s);
				chunk.markDirty();
			}
	}

	public static Entry find(World world, BlockPos pos, int id) {
		return world.getChunkFromBlockCoords(pos).getCapability(MULTIBLOCKS, null).find(pos, id);
	}

	public static int remove(TileEntity core, int id) {
		BlockPos pos = core.getPos();
		int c = (pos.getX() & 0xff) | (pos.getY() & 0xff) << 9 | (pos.getZ() & 0xff) << 18 | id << 27;
		World world = core.getWorld();
		long r = world.getChunkFromBlockCoords(pos).getCapability(MULTIBLOCKS, null).get(c);
		int p0 = (int)r, s = (int)(r >> 32);
		if (r < 0) return c;
		int x0 = pos.getX() - (p0       & 0xff) & 0xffffff00 | p0       & 0xff;
		int z0 = pos.getZ() - (p0 >> 18 & 0xff) & 0xffffff00 | p0 >> 18 & 0xff;
		int x1 = x0 + (s       & 0xff) + 15 >> 4; x0 >>=4;
		int z1 = z0 + (s >> 18 & 0xff) + 15 >> 4; z0 >>=4;
		for (int x = x0; x < x1; x++)
			for (int z = z0; z < z1; z++) {
				Chunk chunk = world.getChunkFromChunkCoords(x, z);
				chunk.getCapability(MULTIBLOCKS, null).remove(c);
				chunk.markDirty();
			}
		return c;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == MULTIBLOCKS;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == MULTIBLOCKS ? (T)this : null;
	}

	@Override
	public NBTBase serializeNBT() {
		int[] arr = structures;
		int l = count;
		if (l == 0) return new NBTTagByte((byte)0); //minimize memory footprint
		return new NBTTagIntArray(arr.length > l ? Arrays.copyOf(arr, l) : arr);
	}

	@Override
	public void deserializeNBT(NBTBase nbt) {
		if (nbt instanceof NBTTagIntArray) {
			structures = ((NBTTagIntArray)nbt).getIntArray();
			count = structures.length / 3 * 3;
		} else {
			structures = null;
			count = 0;
		}
	}

	public class Entry implements Iterator<Entry>, Iterable<Entry> {

		private final BlockPos pos;
		private final int k;
		private int i = 0, c, p0, s;

		private Entry(BlockPos pos, int id) {
			this.pos = pos;
			this.k = 0x4020100 | id << 27
				| (pos.getX() & 0xff)
				| (pos.getY() & 0xff) << 9
				| (pos.getZ() & 0xff) << 18;
		}

		@Override
		public boolean hasNext() {
			int[] str = structures;
			int i = this.i, n = count;
			for (int k = this.k;
				i < n && ((k - str[i+1] | 0x4020100) - str[i+2] & 0xfc020100) != 0;
			i+=3);
			return (this.i = i) < n;
		}

		@Override
		public Entry next() {
			int[] str = structures;
			int i = this.i;
			c = str[i];
			p0 = str[i+1];
			s = str[i+2];
			this.i = i + 3;
			return this;
		}

		public BlockPos core() {
			return new BlockPos(
				x0() + (c - p0 & 0xff), c >> 9 & 0xff,
				z0() + ((c >> 18) - (p0 >> 18) & 0xff)
			);
		}

		public final int x0() {
			int x = p0 & 0xff;
			return pos.getX() - x & 0xffffff00 | x;
		}
		public final int y0() {
			return p0 >> 9 & 0xff;
		}
		public final int z0() {
			int z = p0 >> 18 & 0xff;
			return pos.getZ() - z & 0xffffff00 | z;
		}
		public final int sx() {return s       & 0xff;}
		public final int sy() {return s >>  9 & 0xff;}
		public final int sz() {return s >> 18 & 0xff;}
		public final int x1() {return x0() + sx();}
		public final int y1() {return y0() + sy();}
		public final int z1() {return z0() + sz();}

		@Override
		public Iterator<Entry> iterator() {
			return this;
		}
	}

}
