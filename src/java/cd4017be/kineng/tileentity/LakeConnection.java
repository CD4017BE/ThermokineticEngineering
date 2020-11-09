package cd4017be.kineng.tileentity;

import static cd4017be.kineng.capability.StructureLocations.MULTIBLOCKS;
import java.util.List;
import cd4017be.kineng.capability.StructureLocations.Entry;
import cd4017be.lib.block.AdvancedBlock.ITilePlaceHarvest;
import cd4017be.lib.tileentity.BaseTileEntity;
import cd4017be.lib.tileentity.BaseTileEntity.ITickableServerOnly;
import cd4017be.lib.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;


/** 
 * @author CD4017BE */
public abstract class LakeConnection extends BaseTileEntity
implements ITickableServerOnly, ITilePlaceHarvest {

	protected BlockPos core;
	protected StorageLake lake;

	@Override
	public void update() {
		if (lake != null && !lake.invalid() || getLake())
			tickLakeInteract();
	}

	protected abstract void tickLakeInteract();

	protected float relLiquidLvl() {
		return (float)(core.getY() - pos.getY() + (lake.level >> 1)) + lake.partLevel();
	}

	protected boolean getLake() {
		if (core == null) return false;
		TileEntity te = Utils.getTileAt(world, core);
		if (te instanceof StorageLake) {
			lake = (StorageLake)te;
			return true;
		} else return false;
	}

	@Override
	public void onLoad() {
		super.onLoad();
		BlockPos pos = this.pos.offset(getOrientation().back);
		for (Entry e : getChunk().getCapability(MULTIBLOCKS, null).find(pos, 0)) {
			pos = e.core();
			if (core == null || pos.getY() > core.getY())
				core = pos;
		}
	}

	@Override
	public void onPlaced(EntityLivingBase entity, ItemStack item) {
		if (world.isRemote || !(entity instanceof EntityPlayer)) return;
		ITextComponent msg;
		if (getLake()) {
			int h = pos.getY() - core.getY();
			lake.updateLayers(h);
			msg = new TextComponentString(String.format("§2Connected with lake at level %d", h));
		} else msg = new TextComponentString("§cNo Storage Lake near by!");
		((EntityPlayer)entity).sendStatusMessage(msg, true);
	}

	@Override
	public List<ItemStack> dropItem(IBlockState state, int fortune) {
		return makeDefaultDrops(null);
	}

}
