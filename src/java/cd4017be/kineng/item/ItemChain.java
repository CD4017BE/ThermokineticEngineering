package cd4017be.kineng.item;

import static net.minecraft.block.BlockDirectional.FACING;
import static net.minecraft.block.BlockRotatedPillar.AXIS;
import static net.minecraftforge.items.ItemHandlerHelper.copyStackWithSize;
import java.util.List;
import cd4017be.kineng.block.*;
import cd4017be.kineng.physics.GearLink;
import cd4017be.kineng.tileentity.IGear;
import cd4017be.lib.item.BaseItem;
import cd4017be.lib.util.TooltipUtil;
import cd4017be.lib.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;


/** 
 * @author CD4017BE */
public class ItemChain extends BaseItem {

	public final double[] maxF;
	public final double[] friction;

	public ItemChain(String id, int variants) {
		super(id);
		setHasSubtypes(true);
		this.maxF = new double[variants];
		this.friction = new double[variants];
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (!isInCreativeTab(tab)) return;
		for (int i = 0; i < maxF.length; i++)
			items.add(new ItemStack(this, 1, i));
	}

	@Override
	public EnumActionResult onItemUse(
		EntityPlayer player, World world, BlockPos pos, EnumHand hand,
		EnumFacing facing, float hitX, float hitY, float hitZ
	) {
		IBlockState state = world.getBlockState(pos);
		while (state.getBlock() instanceof BlockFillDirected)
			state = world.getBlockState(pos = pos.offset(state.getValue(FACING)));
		if (state.getBlock() instanceof BlockGear) {
			if (world.isRemote) return EnumActionResult.SUCCESS;
			int r = interact(player.getHeldItem(hand), world, pos, state, player.isCreative());
			player.sendStatusMessage(new TextComponentTranslation("msg.kineng.chain_link" + r), true);
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
	}

	private int interact(ItemStack stack, World world, BlockPos pos, IBlockState state, boolean creative) {
		NBTTagCompound nbt = stack.getTagCompound();
		int d = (int)(((BlockGear)state.getBlock()).radius(state) * 2.0);
		Axis ax = state.getValue(AXIS);
		if (nbt == null) {
			stack.setTagCompound(nbt = new NBTTagCompound());
			nbt.setInteger("x", pos.getX());
			nbt.setInteger("y", pos.getY());
			nbt.setInteger("z", pos.getZ());
			nbt.setInteger("d", d);
			nbt.setByte("a", (byte)ax.ordinal());
			return 0;
		} else {
			stack.setTagCompound(null);
			BlockPos pos0 = new BlockPos(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"));
			if (ax.ordinal() != nbt.getByte("a") || Utils.coord(pos0.subtract(pos), ax) != 0) return 1;
			d = chainLength(pos0, pos, d, nbt.getInteger("d"));
			if (creative && d < stack.getMaxStackSize()) d = 2;
			else if (d > stack.getCount()) return 2;
			TileEntity te0 = world.getTileEntity(pos0);
			TileEntity te1 = world.getTileEntity(pos);
			if (!(
				te0 instanceof IGear && te1 instanceof IGear
				&& IGear.link((IGear)te0, (IGear)te1, copyStackWithSize(stack, d))
			)) return 3;
			stack.shrink(d);
			return 4;
		}
	}

	public static int chainLength(BlockPos pos0, BlockPos pos1, int d0, int d1) {
		return MathHelper.ceil(
			(double)(d0 + d1) * Math.PI * 0.5
			+ Math.sqrt(pos0.distanceSq(pos1)) * 2.0
		);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
		if (player.isSneaking()) {
			ItemStack stack = player.getHeldItem(hand);
			stack.setTagCompound(null);
			return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
		}
		return new ActionResult<ItemStack>(EnumActionResult.PASS, player.getHeldItem(hand));
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
		if (!isSelected) return;
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) return;
		Vec3d start = entity.getPositionEyes(1);
		RayTraceResult rt = world.rayTraceBlocks(start, start.add(entity.getLook(1).scale(3.0)));
		BlockPos pos = rt == null ? entity.getPosition() : rt.getBlockPos();
		BlockPos pos1 = new BlockPos(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"));
		int d = nbt.getInteger("d");
		nbt.setInteger("n", chainLength(pos, pos1, d, d));
	}

	public void configureLink(ItemStack stack, GearLink con) {
		int m = stack.getMetadata();
		if (m < 0 || m >= maxF.length) return;
		con.maxF = Math.min(con.maxF, maxF[m]);
		con.fricD *= friction[m];
	}

	@Override
	public void addInformation(ItemStack item, World player, List<String> list, ITooltipFlag b) {
		super.addInformation(item, player, list, b);
		int m = item.getMetadata();
		if (m < 0 || m >= maxF.length) return;
		list.add(TooltipUtil.format("info.kineng.chain", maxF[m], friction[m]));
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return stack.getTagCompound() != null;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		return (double)nbt.getInteger("n") / (double)stack.getCount();
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return !oldStack.isItemEqual(newStack);
	}

}
