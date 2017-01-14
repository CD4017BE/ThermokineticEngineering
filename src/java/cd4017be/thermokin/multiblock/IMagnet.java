package cd4017be.thermokin.multiblock;

import cd4017be.lib.ModTileEntity;

/**
 * 
 * @author CD4017BE
 */
public interface IMagnet {

	public float magneticStregth();

	public static class SimpleMagnet extends ShaftComponent implements IMagnet {

		public float B; 

		public SimpleMagnet(ModTileEntity shaft, float m, float B) {
			super(shaft, m);
			this.B = B;
		}

		@Override
		public float magneticStregth() {
			return B;
		}

	}

}
