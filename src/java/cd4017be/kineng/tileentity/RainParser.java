package cd4017be.kineng.tileentity;

import java.util.Arrays;
import it.unimi.dsi.fastutil.objects.Object2LongMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2LongRBTreeMap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.INBTSerializable;

public class RainParser implements INBTSerializable<NBTTagCompound> {

	final int x0, z0, y0;

	final Object2LongRBTreeMap<ChunkPos> pending = new Object2LongRBTreeMap<ChunkPos>(RainParser::compare);
	/** visited[z] >> x & 1 */
	final char[] visited = new char[16], visitedC = new char[16];
	/** posStack[i] = x | z << 4 */
	byte[] posStack = new byte[64];
	/** size of {@link #posStack} */
	int top;
	/** bits[0 + x]: z=0, bits[16 + x]: z=15, bits[32 + z]: x=0, bits[48 + z]: x=15 */
	long border;
	ChunkPos current;
	public float rain;

	public RainParser(BlockPos pos) {
		this.x0 = pos.getX();
		this.y0 = pos.getY() - 2;
		this.z0 = pos.getZ();
	}

	public RainParser initialize() {
		current = new ChunkPos(x0 >> 4, z0 >> 4);
		top = 0;
		int x = x0 & 15, z = z0 & 15;
		if (z > 0) posStack[top++] = (byte)(x | z - 1 << 4);
		if (z < 15) posStack[top++] = (byte)(x | z + 1 << 4);
		if (x > 0) posStack[top++] = (byte)(x - 1 | z << 4);
		if (x < 15) posStack[top++] = (byte)(x + 1 | z << 4);
		visitedC[7] |= 0x0080;
		return this;
	}

	/**@param world
	 * @return false if done */
	public boolean doStep(World world) {
		if (current != null) {
			parseChunk(world.getChunkFromChunkCoords(current.x, current.z));
			markChunk(current.x, current.z - 1, (border & 0xffffL) << 16);
			markChunk(current.x, current.z + 1, (border >> 16 & 0xffffL));
			markChunk(current.x - 1, current.z, (border & 0xffff_0000_0000L) << 16);
			markChunk(current.x + 1, current.z, (border >> 16 & 0xffff_0000_0000L));
			current = null;
		} else if (findNext()) {
			Chunk c = world.getChunkFromChunkCoords(current.x, current.z);
			//TODO check heights
			initChunk(
				c.getHeightMap(),
				world.getChunkFromChunkCoords(current.x, current.z - 1).getHeightMap(),
				world.getChunkFromChunkCoords(current.x, current.z + 1).getHeightMap(),
				world.getChunkFromChunkCoords(current.x - 1, current.z).getHeightMap(),
				world.getChunkFromChunkCoords(current.x + 1, current.z).getHeightMap()
			);
		} else return false;
		return true;
	}

	private void markChunk(int x, int z, long mask) {
		if (mask == 0) return;
		int x1 = x - (x0 >> 4) + 7, z1 = z - (z0 >> 4) + 7;
		if (x1 < 0 || x1 >= 15 || z1 < 0 || z1 >= 15) return;
		if ((visitedC[z1] >> x1 & 1) != 0) return;
		ChunkPos p = new ChunkPos(x, z);
		pending.put(p, pending.getLong(p) | mask);
	}

	private boolean findNext() {
		ChunkPos p0 = null;
		int x0 = (this.x0 << 1) - 15, z0 = (this.z0 << 1) - 15;
		int d0 = Integer.MAX_VALUE;
		for (Entry<ChunkPos> e : pending.object2LongEntrySet()) {
			ChunkPos p = e.getKey();
			int dx = (p.x << 5) - x0, dz = (p.z << 5) - z0;
			dx = dx*dx + dz*dz;
			if (dx < d0) {
				d0 = dx;
				p0 = p;
			}
		}
		if (p0 == null) return false;
		border = pending.removeLong(p0);
		z0 = p0.z - (this.z0 >> 4) + 7;
		x0 = p0.x - (this.x0 >> 4) + 7;
		if (z0 >= 0 && z0 < 15 && x0 >= 0 && x0 < 15) {
			current = p0;
			visitedC[z0] |= 1 << x0;
		} else current = null;
		return true;
	}

	private void initChunk(int[] map, int[] mapZ0, int[] mapZ1, int[] mapX0, int[] mapX1) {
		long b = border;
		for (int i = 0; i < 16; i++) {
			visited[i] = 0;
			long m;
			if ((b & (m = 1<<i)) != 0 && mapZ0[i    | 0xf0] > map[i   ]) b ^= m;
			if ((b & (m <<= 16)) != 0 && mapZ1[i   ] > map[i    | 0xf0]) b ^= m;
			if ((b & (m <<= 16)) != 0 && mapX0[i<<4 | 0x0f] > map[i<<4]) b ^= m;
			if ((b & (m <<= 16)) != 0 && mapX1[i<<4] > map[i<<4 | 0x0f]) b ^= m;
		}
		visited[0] = (char)b;
		visited[15] = (char)(b >> 16);
		border = b;
		int t = 0;
		for (int i = 0; i < 16; i++, b >>>= 1) {
			if ((b & 0x1) != 0)
				posStack[t++] = (byte)i;
			if ((b & 0x1_0000) != 0)
				posStack[t++] = (byte)(i | 0xf0);
			if ((b & 0x1_0000_0000L) != 0 && (visited[i] & 1) == 0) {
				visited[i] |= 0x1;
				posStack[t++] = (byte)(i << 4);
			}
			if ((b & 0x1_0000_0000_0000L) != 0 && (visited[i] & 0x8000) == 0) {
				visited[i] |= 0x8000;
				posStack[t++] = (byte)(i << 4 | 0xf);
			}
		}
		top = t;
	}

	private void parseChunk(Chunk c) {
		int[] map = c.getHeightMap();
		int t = this.top;
		long b = 0;
		char[] visited = this.visited;
		byte[] stack = this.posStack;
		while(t > 0) {
			if (t + 3 > stack.length)
				stack = Arrays.copyOf(stack, stack.length << 1);
			int p = stack[--t] & 0xff;
			int h = map[p], x = p & 0xf, z = p >> 4;
			if (z == 0) b |= 1L << x;
			else if ((visited[z-1] >> x & 1) == 0 && map[p-16] >= h) {
				visited[z-1] |= 1 << x;
				stack[t++] = (byte)(p-16);
			}
			if (z == 15) b |= 1L << (x+16);
			else if ((visited[z+1] >> x & 1) == 0 && map[p+16] >= h) {
				visited[z+1] |= 1 << x;
				stack[t++] = (byte)(p+16);
			}
			if (x == 0) b |= 1L << (z+32);
			else if ((visited[z] >> (x-1) & 1) == 0 && map[p-1] >= h) {
				visited[z] |= 1 << (x-1);
				stack[t++] = (byte)(p-1);
			}
			if (x == 15) b |= 1L << (z+48);
			else if ((visited[z] >> (x+1) & 1) == 0 && map[p+1] >= h) {
				visited[z] |= 1 << (x+1);
				stack[t++] = (byte)(p+1);
			}
		}
		border = b;
		top = 0;
		int x0 = (c.x << 4) - this.x0, z0 = (c.z << 4) - this.z0;
		byte[] biomes = c.getBiomeArray();
		for (int z = 0; z < 16; z++)
			for (int x = 0, v = visited[z]; v != 0; v >>>= 1, x++)
				if ((v & 1) != 0) {
					int d1 = x + x0, d2 = z + z0;
					d1 = d1*d1 + d2*d2;
					d2 = (map[z << 4 | x] - y0) * 4;
					if (d2*d2 < d1) continue;
					Biome biome = Biome.getBiomeForId(biomes[z << 4 | x] & 0xff);
					if (biome != null) rain += MathHelper.clamp(biome.getRainfall(), 0F, 1F);
				}
	}

	@Override
	public NBTTagCompound serializeNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setFloat("r", rain);
		int[] arr = new int[8];
		for (int i = 0; i < 8; i++)
			arr[i] = visitedC[i<<1] | visitedC[i<<1|1] << 16;
		nbt.setIntArray("v", arr);
		NBTTagList list = new NBTTagList();
		for (Entry<ChunkPos> e : pending.object2LongEntrySet())
			list.appendTag(writePos(e.getKey(), e.getLongValue()));
		if (current != null)
			list.appendTag(writePos(current, border));
		nbt.setTag("p", list);
		return nbt;
	}

	private static NBTTagCompound writePos(ChunkPos p, long b) {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setLong("b", b);
		tag.setInteger("x", p.x);
		tag.setInteger("z", p.z);
		return tag;
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		rain = nbt.getFloat("r");
		int[] arr = nbt.getIntArray("v");
		for (int i = 0; i < 8 && i < arr.length; i++) {
			visitedC[i<<1] = (char)arr[i];
			visitedC[i<<1|1] = (char)(arr[i] >> 16);
		}
		pending.clear();
		current = null;
		NBTTagList list = nbt.getTagList("p", NBT.TAG_COMPOUND);
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			ChunkPos p = new ChunkPos(tag.getInteger("x"), tag.getInteger("z"));
			pending.put(p, tag.getLong("b"));
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(320);
		sb.append("rain=").append(rain).append('\n');
		boolean m = current == null;
		char d0 = m ? '^' : 'v', d1;
		sb.append('/');
		for (int x = 0; x < 16; x++)
			sb.append((border >> x & 1) != 0 ? d0 : '-');
		sb.append('\\').append('\n');
		d0 = m ? '<' : '>';
		d1 = m ? '>' : '<';
		for (int z = 0; z < 16; z++) {
			sb.append((border >> z + 32 & 1) != 0 ? d0 : '|');
			char v = visited[z];
			for (int x = 0; x < 16; x++, v >>= 1)
				sb.append((v & 1) != 0 ? 'x' : 'o');
			sb.append((border >> z + 48 & 1) != 0 ? d1 : '|');
			if (z < 15) {
				sb.append(' ');
				v = visitedC[z];
				for (int x = 0; x < 15; x++) {
					ChunkPos p = new ChunkPos(x + (x0 >> 4) - 7, z + (z0 >> 4) - 7);
					char c;
					if (p.equals(current)) c = 'x';
					else if ((v >> x & 1) != 0) c = x == 7 && z == 7 ? '+' : 'o';
					else c = pending.getLong(p) != 0 ? '!' : '?';
					sb.append(c);
				}
			}
			sb.append('\n');
		}
		d0 = m ? 'v' : '^';
		sb.append('\\');
		for (int x = 0; x < 16; x++)
			sb.append((border >> x + 16 & 1) != 0 ? d0 : '-');
		return sb.append('/').toString();
	}

	static int compare(ChunkPos a, ChunkPos b) {
		int c = a.z - b.z;
		return c == 0 ? a.x - b.x : c;
	}

}