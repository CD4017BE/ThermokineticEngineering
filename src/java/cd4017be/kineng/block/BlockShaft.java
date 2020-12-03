package cd4017be.kineng.block;

import cd4017be.api.recipes.RecipeScriptContext.ConfigConstants;
import cd4017be.lib.block.AdvancedBlock;
import cd4017be.lib.util.TooltipUtil;
import static cd4017be.kineng.physics.Formula.J_cylinder;
import static cd4017be.kineng.physics.Formula.torsionStrength_circle;
import static net.minecraft.block.BlockRotatedPillar.AXIS;
import static net.minecraft.util.EnumFacing.Axis.*;
import java.util.List;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

/** @author CD4017BE */
public class BlockShaft extends AdvancedBlock implements IFillBlockSrc {

	private static final double DEFAULT_J_dens = J_cylinder(0.25, 1.0);
	private static final double DEFAULT_strength = torsionStrength_circle(0.25);

	public final ShaftMaterial shaftMat;
	public final double r;
	public double J_dens = DEFAULT_J_dens;
	public double strength = DEFAULT_strength;
	public double av_max = Double.NaN; 
	public int[] model;
	public boolean keepTex;

	public BlockShaft(String id, ShaftMaterial m, double r, Class<? extends TileEntity> tile) {
		super(id, m.material, m.blockSound, 3, tile);
		this.shaftMat = m;
		this.r = r;
		setDefaultState(blockState.getBaseState().withProperty(AXIS, Axis.Y));
		if (r >= 0.5) 
			boundingBox = new AxisAlignedBB[] {FULL_BLOCK_AABB, FULL_BLOCK_AABB, FULL_BLOCK_AABB};
		else {
			double min = 0.5 - r, max = 0.5 + r;
			boundingBox = new AxisAlignedBB[] {
				new AxisAlignedBB(0, min, min, 1, max, max),
				new AxisAlignedBB(min, 0, min, max, 1, max),
				new AxisAlignedBB(min, min, 0, max, max, 1),
			};
		}
		setLightOpacity(0);
	}

	public BlockShaft setShape(double r0, double l) {
		J_dens = J_cylinder(r0, 1.0 - l) + J_cylinder(r, l);
		strength = torsionStrength_circle(r0);
		return this;
	}

	public void setModel(int... modelArgs) {
		this.model = modelArgs;
		this.keepTex = true;
	}

	public double J(IBlockState state) {
		return shaftMat.density * J_dens;
	}

	public double maxM(IBlockState state) {
		return shaftMat.strength * strength;
	}

	public double maxAv(IBlockState blockState) {
		if (Double.isNaN(av_max)) av_max = Math.sqrt(3.0 * shaftMat.strength / shaftMat.density) / r;
		return av_max;
	}

	public int[] model(IBlockState state) {
		if (!keepTex) model[1] = shaftMat.texture;
		return model;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, AXIS);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(AXIS, Axis.values()[meta % 3]);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(AXIS).ordinal();
	}

	@SuppressWarnings("deprecation")
	@Override
	public IBlockState getStateForPlacement(
		World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta,
		EntityLivingBase placer
	) {
		return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(
			AXIS, facing.getAxis()
		);
	}

	@Override
	protected AxisAlignedBB getMainBB(IBlockState state, IBlockAccess world, BlockPos pos) {
		return boundingBox[state.getValue(AXIS).ordinal()];
	}

	public double radius(IBlockState state) {
		return r;
	}

	public AxisAlignedBB getSize(BlockPos pos, IBlockState state) {
		Axis ax = state.getValue(AXIS);
		double a = -0.01, r = radius(state) - 0.51;
		return new AxisAlignedBB(pos).grow(ax == X ? a : r, ax == Y ? a : r, ax == Z ? a : r);
	}

	@Override
	public boolean supportsFill(IBlockState state, EnumFacing side) {
		return radius(state) > 0.5;
	}

	public double getDebris(IBlockState state, List<ItemStack> items) {
		items.add(shaftMat.scrap);
		return radius(state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
		addInformation(getStateForPlacement(
			null, null, EnumFacing.NORTH, 0, 0, 0,
			stack.getItem().getMetadata(stack.getMetadata()), null
		), tooltip, advanced);
	}

	@SideOnly(Side.CLIENT)
	protected void addInformation(IBlockState state, List<String> tooltip, ITooltipFlag advanced) {
		tooltip.add(TooltipUtil.format("info.kineng.shaftstats", J(state) * 1000.0, maxM(state), maxAv(state)));
	}

	public static class ShaftMaterial {

		public final Material material;
		public final SoundType blockSound;
		public int texture;
		/** [kg/m³] density*/
		public double density;
		/** [1] friction coefficient */
		public double friction;
		/** [N/m²] mechanical robustness */
		public double strength;
		//real values: Lead = 10...15 MPa, Tin = 15 MPa, Aluminum Alloy = 200...640 MPa
		//Cast Iron = 100...350 MPa, Titanium Alloy = 290...1200(Ti-Al6-V4) MPa, Steel = 310...690 MPa
		//Steel Alloy = 1100...1300 MPa, Carbon Nanotubes = 63 GPa
		public ItemStack scrap = ItemStack.EMPTY;

		public ShaftMaterial(Material material, SoundType blockSound) {
			this.material = material;
			this.blockSound = blockSound;
		}

		public void setFromConfig(ConfigConstants c, String name, double... fallback) {
			fallback = c.getVect("mat_" + name, fallback);
			density = fallback[0];
			strength = fallback[1];
			friction = fallback[2];
			scrap = c.get("scrap_" + name, ItemStack.class, scrap);
		}

	}

}
