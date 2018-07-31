package cd4017be.thermokin.item;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.input.Keyboard;

import cd4017be.lib.BlockItemRegistry;
import cd4017be.lib.item.BaseItem;
import cd4017be.lib.util.TooltipUtil;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.module.Part;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;

/**
 * 
 * @author CD4017BE
 */
public class ItemPart extends BaseItem {

	public static int MAX_DUR = 250;

	public HashMap<Integer, String> names = new HashMap<Integer, String>();

	public ItemPart(String id) {
		super(id);
		setHasSubtypes(true);
		setMaxDamage(MAX_DUR);
		setCreativeTab(Objects.tabThermokin);
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

	@Override
	public int getDamage(ItemStack stack) {
		return stack.hasTagCompound() ? stack.getTagCompound().getByte("dmg") & 0xff : 0;
	}

	@Override
	public void setDamage(ItemStack stack, int damage) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null)
			if (damage == 0) return;
			else stack.setTagCompound(nbt = new NBTTagCompound());
		nbt.setByte("dmg", (byte)damage);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return stack.hasTagCompound() && stack.getTagCompound().hasKey("dmg", Constants.NBT.TAG_BYTE);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return stack.hasTagCompound() ? (double)(stack.getTagCompound().getByte("dmg") & 0xff) / (double)MAX_DUR : 0;
	}

	@Override
	public void addInformation(ItemStack item, EntityPlayer player, List<String> list, boolean b) {
		super.addInformation(item, player, list, b);
		if (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)) {
			String sA = "item.thermokin.part.tipA";
			Part p = Part.getPart(item);
			String s1 = TooltipUtil.format(sA, (double)p.Lh * 20.0, p.Tmax, (float)Part.MAX_DUR / p.dmgH / 20F);
			if (!s1.equals(sA)) list.addAll(Arrays.asList(s1.split("\n")));
		} else {
			String hint = TooltipUtil.getAltHint();
			if (list.isEmpty() || !list.get(list.size() - 1).equals(hint)) list.add(hint);
		}
	}

	public void add(Part p, String name) {
		names.put(p.id, name);
		BlockItemRegistry.registerItemStack(p.item, getRegistryName().getResourcePath() + "." + name);
	}

	public ItemStack get(int id) {
		return new ItemStack(this, 1, id);
	}

}
