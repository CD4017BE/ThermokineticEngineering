package cd4017be.kineng.physics;

import cd4017be.math.cplx.CplxD;

/**Describes a general mechanical force acting on a shaft structure.
 * Use a {@link ForceCon} to handle linking.
 * @author CD4017BE */
public interface IForce {

	/**Compute the torque to apply on the shaft during following tick.
	 * @param M torque as linear function of angular velocity:
	 * M(ω) = <b>M</b>{@link CplxD#r .r} + <b>M</b>{@link CplxD#i .i} * ω
	 * @param av0 pre-tick angular velocity = ω(t0) */
	ForceCon getM(CplxD M, double av0);

	/**Respond to shaft movement.
	 * @param dan angle moved: Δφ = φ(t1) - φ(t0)
	 * @param av_ average angular velocity = ∫ t0→t1 (ω(t) dφ(t)) / Δφ 
	 * @param av1 post-tick angular velocity = ω(t1) */
	void move(double dan, double av_, double av1);

	/**When the shaft connection changes.
	 * @param con new connection or null if disconnected */
	void connect(ForceCon con);

	/**When moved to a different shaft structure. */
	void onStructureChanged();

}
