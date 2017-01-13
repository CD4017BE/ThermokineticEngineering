package cd4017be.thermokin;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

/**
 *
 * @author CD4017BE
 */
public class CreativeTabThermokin extends CreativeTabs {

	public CreativeTabThermokin(String name) {
		super(name);
	}

	@Override
	public Item getTabIconItem() {
		return Item.getItemFromBlock(Objects.liqPipe);
	}

}
