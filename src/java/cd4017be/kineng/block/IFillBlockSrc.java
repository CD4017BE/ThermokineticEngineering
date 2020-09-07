package cd4017be.kineng.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

public interface IFillBlockSrc {
	boolean supportsFill(IBlockState state, EnumFacing side);
}