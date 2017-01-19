package cd4017be.thermokin.multiblock;

import cd4017be.lib.ModTileEntity;

public interface IGear {

	public float translationFactor();

	public static class SimpleGear extends ShaftComponent implements IGear {

		public float tf; 

		public SimpleGear(ModTileEntity shaft, float m, float f, String model) {
			super(shaft, m);
			this.tf = f;
			this.model = model;
		}

		@Override
		public float translationFactor() {
			return tf;
		}

	}

}
