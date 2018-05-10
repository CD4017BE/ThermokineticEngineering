package cd4017be.thermokin.render;

import cd4017be.lib.block.MultipartBlock.IModularTile;
import cd4017be.lib.property.PropertyByte;
import cd4017be.thermokin.module.Part;
import cd4017be.thermokin.module.Part.Type;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 
 * @author cd4017be
 */
@SideOnly(Side.CLIENT)
public class ModularItemImpl implements IModularTile {

	private final int[] modules;

	public ModularItemImpl(int[] modules) {
		this.modules = modules;
	}

	@Override
	public <T> T getModuleState(int m) {
		Part p = Part.getPart(Type.forSlot(m), modules[m]);
		return PropertyByte.cast(p.modelId);
	}

	@Override
	public boolean isModulePresent(int m) {
		Type t = Type.forSlot(m);
		return Part.getPart(t, modules[m]) != t.NULL();
	}

	@Override
	public boolean isOpaque() {
		for (int i = Type.CASING.slotS; i < Type.CASING.slotE; i++)
			if (!Part.getPart(Type.CASING, modules[i]).opaque) return false;
		return true;
	}

}
