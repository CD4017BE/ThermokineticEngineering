package cd4017be.kineng.item;

import java.util.*;
import cd4017be.api.recipes.RecipeAPI.IRecipeHandler;
import cd4017be.lib.item.BaseItem;
import cd4017be.lib.script.Parameters;
import cd4017be.lib.util.ItemFluidUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


/** 
 * @author CD4017BE */
public class ItemBreakRecipe extends BaseItem implements IRecipeHandler {

	public final HashMap<Block, Recipe> RECIPES = new HashMap<>();

	public ItemBreakRecipe(String id) {
		super(id);
	}

	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state) {
		Recipe r = matchRecipe(state);
		return r == null ? 0F : r.speed;
	}

	@Override
	public boolean onBlockStartBreak(ItemStack stack, BlockPos pos, EntityPlayer player) {
		World world = player.world;
		if (world.isRemote) return true;
		Recipe r = matchRecipe(world.getBlockState(pos));
		if (r == null) return false;
		world.setBlockToAir(pos);
		ItemFluidUtil.dropStack(r.drop.copy(), world, pos);
		stack.damageItem(1, player);
		return true;
	}

	public Recipe matchRecipe(IBlockState state) {
		Block block = state.getBlock();
		int m = 1 << block.getMetaFromState(state);
		Recipe r = RECIPES.get(block);
		while (r != null) {
			if ((r.metaMask & m) != 0)
				return r;
			r = r.next;
		}
		return r;
	}

	@Override
	public void addRecipe(Parameters param) {
		Block block = Block.REGISTRY.getObject(new ResourceLocation(param.getString(1)));
		if (block == null) return;
		Recipe r = new Recipe(param.getIndex(2), (float)param.getNumber(3), param.get(4, ItemStack.class));
		RECIPES.merge(block, r, Recipe::append);
	}

	public static class Recipe {
		Recipe next;
		final int metaMask;
		final float speed;
		final ItemStack drop;

		public Recipe(int metaMask, float speed, ItemStack drop) {
			this.metaMask = metaMask;
			this.speed = speed;
			this.drop = drop;
		}

		public Recipe append(Recipe r) {
			r.next = this;
			return r;
		}
	}

}
