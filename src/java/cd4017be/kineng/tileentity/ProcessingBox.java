package cd4017be.kineng.tileentity;

import static cd4017be.kineng.tileentity.IKineticLink.T_SHAPE;
import static cd4017be.kineng.tileentity.IKineticLink.T_TIER;
import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
import java.util.List;
import cd4017be.kineng.block.BlockProcessing;
import cd4017be.kineng.physics.DynamicForce;
import cd4017be.kineng.recipe.KineticProcess;
import cd4017be.lib.Gui.*;
import cd4017be.lib.block.AdvancedBlock.ITilePlaceHarvest;
import cd4017be.lib.network.*;
import cd4017be.lib.tileentity.BaseTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


/** 
 * @author CD4017BE */
public class ProcessingBox extends BaseTileEntity implements IForceProvider, IGuiHandlerTile, ITilePlaceHarvest {

	@Sync(tag = "dev")
	public KineticProcess machine = new KineticProcess(3);

	@Override
	public DynamicForce connect(IKineticLink link, EnumFacing side) {
		if (side != getOrientation().back) return null;
		if (link == null)
			return machine.setMode(Integer.MAX_VALUE);
		int type = link.type();
		int types = ((BlockProcessing)getBlockState().getBlock()).types;
		if ((types & 0x100 << (type >> 8)) == 0) return null;
		return machine.setMode(Math.min(type, types & T_TIER | type & T_SHAPE));
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == ITEM_HANDLER_CAPABILITY;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == ITEM_HANDLER_CAPABILITY ? (T)machine : null;
	}

	@Override
	protected void onUnload() {
		super.onUnload();
		machine.unload();
	}

	@Override
	public AdvancedContainer getContainer(EntityPlayer player, int id) {
		return machine.getContainer(player);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModularGui getGuiScreen(EntityPlayer player, int id) {
		return machine.getGuiScreen(player);
	}

	@Override
	public void onPlaced(EntityLivingBase entity, ItemStack item) {}

	@Override
	public List<ItemStack> dropItem(IBlockState state, int fortune) {
		List<ItemStack> list = makeDefaultDrops(null);
		machine.addItems(list);
		return list;
	}

}
