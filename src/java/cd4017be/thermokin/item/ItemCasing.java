package cd4017be.thermokin.item;

import java.util.Arrays;
import java.util.HashMap;

import cd4017be.lib.BlockItemRegistry;
import cd4017be.thermokin.module.Part;
import cd4017be.thermokin.module.Part.Type;
import cd4017be.thermokin.render.ModularModel;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * @author CD4017BE
 *
 */
public class ItemCasing extends ItemPart {

	public HashMap<Integer, String> names = new HashMap<Integer, String>();

	public ItemCasing(String id) {
		super(id);
		this.setHasSubtypes(true);
	}

	@Override
	protected void init() {}

	@Override
	public String getUnlocalizedName(ItemStack item) {
		String name = names.get(item.getMetadata());
		if (name == null || name.startsWith("$")) return super.getUnlocalizedName(item);
		return this.getUnlocalizedName() + ":" + name;
	}

	@Override
	public String getItemStackDisplayName(ItemStack item) {
		String name = names.get(item.getMetadata());
		if (name != null && name.startsWith("$")) return name.substring(1);
		return super.getItemStackDisplayName(item);
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, NonNullList<ItemStack> subItems) {
		int[] ids = new int[names.size()];
		int n = 0;
		for (int i : names.keySet()) ids[n++] = i;
		Arrays.sort(ids);
		for (int i : ids) subItems.add(new ItemStack(item, 1, i));
	}

	public Part addCasing(int id, String name, String texture, float Lh, float Tmax, float dmgH) {
		Part p = new Part(Type.CASING, id, new ItemStack(this, 1, id), Lh, Tmax, dmgH);
		names.put(id, name);
		BlockItemRegistry.registerItemStack(p.item, getRegistryName().getResourcePath() + "." + name);
		
		BlockRenderLayer layer = BlockRenderLayer.SOLID;
		int i = texture.lastIndexOf('@');
		if (i > 0) {
			String code = texture.substring(i+1).toLowerCase();
			texture = texture.substring(0, i);
			for (BlockRenderLayer l : BlockRenderLayer.values())
				if (l.toString().toLowerCase().startsWith(code)) {
					layer = l;
					break;
				}
		}
		p.opaque = layer == BlockRenderLayer.SOLID;
		if (FMLCommonHandler.instance().getSide().isClient())
			ModularModel.register(p, new ResourceLocation(texture), layer);
		return p;
	}

}
