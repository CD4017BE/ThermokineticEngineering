package cd4017be.thermokin.multiblock;

import cd4017be.lib.ModTileEntity;

public interface IGear {

	public float translationFactor();

	public static class SimpleGear extends ShaftComponent implements IGear {

		public float tf; 

		public SimpleGear(ModTileEntity shaft, float m, float f) {
			super(shaft, m);
			this.tf = f;
		}

		@Override
		public float translationFactor() {
			return tf;
		}

	}

}
