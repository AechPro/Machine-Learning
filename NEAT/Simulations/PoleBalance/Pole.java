package NEAT.Simulations.PoleBalance;

public class Pole 
{
	private final double GRAVITY=9.8;
	private final double MASSCART=1.0;
	private final double MASSPOLE=0.1;
	private final double TOTAL_MASS=(MASSPOLE + MASSCART);
	private final double LENGTH=1.0;	  /* actually half the pole's length */
	private final double POLEMASS_LENGTH=(MASSPOLE * LENGTH);
	private final double FORCE_MAG=10.0;
	private final double TAU=0.02;	  /* seconds between state updates */
	private final double FOURTHIRDS=1.3333333333333;
	public Pole() 
	{
		
	}
	public double[] step()
	{
		force = (y>0)? FORCE_MAG : -FORCE_MAG;
		
		costheta = Math.cos(theta);
		sintheta = Math.sin(theta);

		temp = (force + POLEMASS_LENGTH * theta_dot * theta_dot * sintheta)
				/ TOTAL_MASS;

		thetaacc = (GRAVITY * sintheta - costheta * temp)
				   / (LENGTH * (FOURTHIRDS - MASSPOLE * costheta * costheta
				   / TOTAL_MASS));

		xacc  = temp - POLEMASS_LENGTH * thetaacc * costheta / TOTAL_MASS;
		x  += TAU * x_dot;
		x_dot += TAU * xacc;
		theta += TAU * theta_dot;
		if(Math.random()<0.2) {thetaacc+=thetaacc*rng.nextGaussian();}
		theta_dot += TAU * thetaacc;
		
		polePos[0] = pos[0] + (int)(Math.round(poleRadius*Math.cos(theta + Math.PI/2)));
		polePos[1] = pos[1] - (int)(Math.round(poleRadius*Math.sin(theta + Math.PI/2)));
		
		
		if (x < -2.4 || x > 2.4  || theta < -twelve_degrees 
				|| theta > twelve_degrees || steps>maxSteps){done=true;}
	}
}
