package cd4017be.thermokin.item;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.input.Keyboard;

import cd4017be.lib.item.BaseItem;
import cd4017be.lib.util.TooltipUtil;
import cd4017be.thermokin.Objects;
import cd4017be.thermokin.module.Part;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

/**
 * 
 * @author CD4017BE
 */
public class ItemPart extends BaseItem {

	public static int MAX_DUR = 250;

	public ItemPart(String id) {
		super(id);
		setMaxDamage(MAX_DUR);
		setCreativeTab(Objects.tabThermokin);
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

}
