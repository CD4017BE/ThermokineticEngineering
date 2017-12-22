package cd4017be.thermokin.block;

import java.lang.reflect.InvocationTargetException;

import cd4017be.lib.block.AdvancedBlock;
import cd4017be.lib.property.PropertyBoolean;
import cd4017be.lib.property.PropertyWrapObj;
import cd4017be.thermokin.module.Part;
import cd4017be.thermokin.tileentity.ModularMachine;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import scala.actors.threadpool.Arrays;

public class BlockModularMachine extends AdvancedBlock {

	public static final IUnlistedProperty<short[]> parts_prop = new PropertyWrapObj<short[]>("parts", short[].class);
	public static final IUnlistedProperty<Boolean> opaque_prop = new PropertyBoolean("opaque");
	public static final IProperty<Integer> type_prop = PropertyInteger.create("type", 0, 15);

	@SuppressWarnings("unchecked")
	public final Class<? extends ModularMachine>[] tiles = new Class[16];

	protected BlockModularMachine(String id, Material m, SoundType sound, int flags) {
		super(id, m, sound, flags, ModularMachine.class);
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this, new IProperty[] {type_prop}, new IUnlistedProperty[] {opaque_prop, parts_prop});
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(type_prop, meta);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(type_prop);
	}

	@Override
	public boolean hasTileEntity(IBlockState state) {
		return tiles[state.getValue(type_prop)] != null;
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state) {
		Class<? extends ModularMachine> tc = tiles[state.getValue(type_prop)];
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

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		IExtendedBlockState eState = (IExtendedBlockState)state;
		boolean opaque = true;
		short[] parts;
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof ModularMachine) {
			ModularMachine m = (ModularMachine)te;
			int n = 0;
			parts = new short[15];
			for (int i = 0; i < 15; i++) {
				Part p = m.components[i];
				if (p != null) {
					short id = p.getModelId(i);
					if (id >= 0) {
						parts[n++] = id;
						continue;
					}
				}
				if (i < 6) opaque = false;
			}
			if (n < 15) parts = Arrays.copyOf(parts, n);
		} else parts = null;
		return eState.withProperty(opaque_prop, opaque).withProperty(parts_prop, parts);
	}

}
