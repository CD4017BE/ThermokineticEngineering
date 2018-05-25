package cd4017be.thermokin.block;

import cd4017be.lib.block.MultipartBlock;
import cd4017be.thermokin.tileentity.ModularMachine;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
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

	/**
	 * @param id
	 * @param m
	 * @param sound
	 * @param flags
	 * @param tile
	 */
	public BlockModularMachine(String id, Material m, SoundType sound, int flags, Class<? extends ModularMachine> tile) {
		super(id, m, sound, flags, PART_PROPS.length, tile);
		setMultilayer();
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
		return null;
	}

}
