package cd4017be.kineng.tileentity;

import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
import java.util.List;
import cd4017be.kineng.block.BlockRotaryTool;
import cd4017be.kineng.physics.*;
import cd4017be.kineng.recipe.KineticProcess;
import cd4017be.lib.Gui.*;
import cd4017be.lib.block.AdvancedBlock.ITilePlaceHarvest;
import cd4017be.lib.network.*;
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
public class ProcessingShaft extends KineticMachine implements IGuiHandlerTile, ITilePlaceHarvest {

	@Sync(tag = "dev")
	public KineticProcess machine = new KineticProcess(3);

	@Override
	protected DynamicForce createForce(BlockRotaryTool block) {
		if (!world.isRemote) machine.setMode(block.type);
		return machine;
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
	public void onLoad() {
		super.onLoad();
		machine.mode &= Integer.MAX_VALUE;
	}

	@Override
	protected void onUnload() {
		super.onUnload();
		machine.mode |= Integer.MIN_VALUE;
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
