package cd4017be.thermokin.block;

import java.lang.reflect.InvocationTargetException;

import cd4017be.lib.block.MultipartBlock;
import cd4017be.thermokin.tileentity.ModularMachine;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 
 * @author CD4017BE
 *
 */
public class BlockModularMachine extends MultipartBlock {

	public static final String[] PART_PROPS = {
			"cb", "ct", "cn", "cs", "cw", "ce",
			"mb", "mt", "mn", "ms", "mw", "me"
		};
	public static final PropertyInteger TYPE_PROP = PropertyInteger.create("v", 0, 15);

	@SuppressWarnings("unchecked")
	public final Class<? extends ModularMachine>[] tiles = new Class[16];

	/**
	 * @param id
	 * @param m
	 * @param sound
	 * @param flags
	 */
	public BlockModularMachine(String id, Material m, SoundType sound, int flags) {
		super(id, m, sound, flags, PART_PROPS.length, ModularMachine.class);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String moduleVariant(int i) {
		return PART_PROPS[i];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Class<?> moduleType(int i) {
		return Byte.class;
	}

	@Override
	protected PropertyInteger createBaseState() {
		return TYPE_PROP;
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return tiles[state.getValue(TYPE_PROP)] != null;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		Class<? extends ModularMachine> tc = tiles[state.getValue(TYPE_PROP)];
		if (tc == null) return null;
		try {
			try {
				return tc.getConstructor(IBlockState.class).newInstance(state);
			} catch (NoSuchMethodException e) {
				return tc.newInstance();
			}
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException ex) {
			ex.printStackTrace();
		}
		return null;
	}

}
