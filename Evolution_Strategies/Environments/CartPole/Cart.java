package Evolution_Strategies.Environments.CartPole;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

import Evolution_Strategies.Configs.Config;
import Evolution_Strategies.Environments.EnvironmentAgent;
import Evolution_Strategies.Util.Rand;
import core.camera.Camera;

public class Cart extends EnvironmentAgent
{
    private double[][][] inputs;
    private double[] outputs;
    private boolean victory;
    private int width,height,poleRadius;
    private int maxSteps;
    private double prevX;
    private int timeSinceMovement;
    private int[] pos, polePos;
    private double fitness;
    private int numTrials;
    private boolean renderPhenotype;
    private int numResets;
    
    private double x,       /* cart position, meters */
    xDot,           /* cart velocity */
    theta1,
    theta2,         /* pole angle, radians */
    thetaDot;       /* pole angular velocity */
    private int steps,y;
    private Random rng;
    private boolean randomStart;
    private boolean done;
    private double twelve_degrees;
    private double xAcc,thetaAcc,force,costheta,sintheta,temp;
    private Color color;

    private final double GRAVITY=9.8;
    private final double MASSCART=1.0;
    private final double MASSPOLE=0.1;
    private final double TOTAL_MASS=(MASSPOLE + MASSCART);
    private final double LENGTH=1.0;      /* actually half the pole's length */
    private final double POLEMASS_LENGTH=(MASSPOLE * LENGTH);
    private final double FORCE_MAG=10.0;
    private final double TAU=0.02/4;      /* seconds between state updates */
    private final double FOURTHIRDS=1.3333333333333;
    
    private double prevFitness;
    
    private Pole pole1;
    
    public Cart(double[] startPos, double startAngle, Camera cam)
    {
        super(startPos, startAngle, cam);
        maxSteps = 100000;
        randomStart = false;
        numTrials=0;
        pole1 = new Pole(width,height,300,1.0,Math.toRadians(1),randomStart);
        //pole2 = new Pole(w,h,300,0.1,0,randomStart);
        rng = Rand.rand;
        reset();
    }
    public void reset()
    {
        done = false;
        steps = 0;
        timeSinceMovement = 0;
        twelve_degrees = Math.PI/2;//0.2094384;
        poleRadius = (int)(Math.round(300*LENGTH));
        inputs = new double[4][1][1];
        outputs = new double[2];
        pos = new int[] {100,720/2};
        polePos = new int[] {100,720/2};
        y = 0;
        x = 0.0;
        xAcc = 0.0;
        force = 0.0;
        costheta = 0.0;
        sintheta = 0.0;
        temp = 0.0;
        if (randomStart) 
        {
            /*set up random start state*/
            x = ((Integer.MAX_VALUE*Math.random())%4800)/1000.0 - 2.4;
            xDot = ((Integer.MAX_VALUE*Math.random())%2000)/1000.0 - 1;
        }
        prevX = x;
    }

    @Override
    public double takeAction(int actionNum)
    {
        if(actionNum == 0) {y = 0;}
        else {y = 1;}
        return fitness - prevFitness;
    }

    @Override
    public double[] getState()
    {
        if(done)
        {
            return null;
        }
        double[] state = new double[Config.POLICY_INPUT_NODES];
        state[0]=(x + 2.4) / 4.8;
        state[1]=(theta1 + 1.0) / 2.0;
        state[2]=(theta2 + 1.0) / 2.0;
        return state;
    }

    @Override
    public void init()
    {
    }

    @Override
    public void eUpdate()
    {
        prevFitness = fitness;
        if (x < -2.4 
                || x > 2.4  
                || theta1 < -twelve_degrees 
                || theta1 > twelve_degrees 
                || theta2 < -twelve_degrees 
                || theta2 > twelve_degrees
                || steps>maxSteps){done=true;}
        if(timeSinceMovement > 100)
        {
            System.out.println("\n!!!FAILURE TO MOVE DETECTED!!!\n"+timeSinceMovement);
            done=true;
        }
        if(done) 
        {
            numTrials++;
            if(numTrials<numResets) {reset();}
            return;
        }
        if(x == prevX) {timeSinceMovement++;}
        else {timeSinceMovement=0;}
        
        theta1 = pole1.getTheta();
        //theta2 = pole2.getTheta();
        
        pos[0] = (int)(Math.round((1280-width)*(x+2.4)/4.8));
        //System.out.println(x+" "+theta);
        
        xDot = pole1.step(y,pos);
        x  += TAU * 0.75*(xDot);
        
        steps++;
        fitness++;
        
    }

    @Override
    public void eRender(Graphics2D g, double interp)
    {
    }

}
