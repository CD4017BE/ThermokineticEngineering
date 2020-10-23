package cd4017be.kineng.item;

import cd4017be.kineng.tileentity.WindTurbine;
import cd4017be.lib.item.BaseItem;
import cd4017be.lib.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;


/** 
 * @author CD4017BE */
public class ItemAerometer extends BaseItem {

	public ItemAerometer(String id) {
		super(id);
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		if (!(isSelected && entity instanceof EntityPlayer)) return;
		NBTTagCompound nbt = stack.getTagCompound();
		if (!world.isRemote) {
			if (nbt == null) stack.setTagCompound(nbt = new NBTTagCompound());
			updateMeasurement(nbt, world, entity.getPosition());
		} else if (nbt != null)
			updateDisplay(nbt, (EntityPlayer)entity);
	}

	private void updateMeasurement(NBTTagCompound nbt, World world, BlockPos pos) {
		int dx = pos.getX() - nbt.getInteger("px");
		int dy = pos.getY() - nbt.getInteger("py");
		int dz = pos.getZ() - nbt.getInteger("pz");
		if (dx*dx + dy*dy + dz*dz < 48) return;
		nbt.setInteger("px", pos.getX());
		nbt.setInteger("py", pos.getY());
		nbt.setInteger("pz", pos.getZ());
		Vec3d v = WindTurbine.baseWindSpeed(world, pos);
		nbt.setFloat("vx", (float)v.x);
		nbt.setFloat("vy", (float)v.y);
		nbt.setFloat("vz", (float)v.z);
	}

	private void updateDisplay(NBTTagCompound nbt, EntityPlayer player) {
		player.sendStatusMessage(new TextComponentString(
			String.format("%+.1f m/s", Utils.coord(
				nbt.getDouble("vx"), -nbt.getDouble("vy"), nbt.getDouble("vz"),
				Utils.getLookDirStrict(player)
			))
		), true);
	}

}
