package Evolution_Strategies.Environments.Snake.Workers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import Evolution_Strategies.Configs.Config;
import Evolution_Strategies.Environments.EnvironmentAgent;
import Evolution_Strategies.Environments.Snake.engine.SnakeLevel;
import Evolution_Strategies.Policies.FFNN.FFNetwork;
import Evolution_Strategies.Util.Rand;
import core.camera.Camera;
import core.map.TileMap;
import core.map.tiles.Tile;
import core.phys.PhysicsObject;

public class Snake extends EnvironmentAgent
{
	private int currentDirection = DOWN;
	private boolean champ;
	private double fitness;
	private ArrayList<SnakeSegment> segments;
	private ArrayList<double[]> positions;
	private int moveCounter = 0;
	private boolean movingToCell;
	private double[] nextCell;
	private double[] prevCell;
	private Pellet pellet;
	private int[] direction;
	private SnakeLevel level;
	private double[][][] inputVector;
	private double[] outputVector;
	private SnakeSegment head;
	private TileMap map;
	private int stepsSincePellet;
	private FFNetwork net;

	private double prevDist, dist;
	private double prevFitness;

	private int numInputs;
	private Rectangle2D[] sensors;

	public Snake(double[] startPos, double startAngle, Camera cam, SnakeLevel lev)
	{
		super(startPos, startAngle, cam);
		level = lev;
		map = level.getMap();
		pellet = lev.spawnPellet();
	}

	@Override
	public void init()
	{
		collidable = true;
		sensors = new Rectangle2D[3];
		numInputs = Config.FFNN_LAYER_INFO[0];
		dead = false;
		champ = false;
		direction = new int[2];
		inputVector = new double[numInputs][1][1];
		hasSelectiveCollisions = true;
		movingToCell = false;
		fitness = -3;
		nextCell = new double[2];
		prevCell = new double[2];
		acceleration[0] = 0;
		acceleration[1] = 0;
		velocity[0] = 0;
		velocity[1] = 0;
		stepsSincePellet = 0;
		prevFitness = 0;
		width = SEGMENT_SIZE;
		height = SEGMENT_SIZE;

		dist = Double.MAX_VALUE;
		currentDirection = 0;
		prevDist = Double.MAX_VALUE;

		segments = new ArrayList<SnakeSegment>();
		positions = new ArrayList<double[]>();
		head = new SnakeSegment(new double[] {position[0], position[1]}, 0, camera, null, true);
		double[] pos = new double[] {position[0], position[1]};

		net = null;
		positions.add(pos);
	}

	@Override
	public void handleCollision(PhysicsObject collider)
	{
		if(collider instanceof Tile)
		{
			kill();
		}
	}

	@Override
	public void eUpdate()
	{
		//System.out.println(fitness+" "+getState());
		if(dead){return;}

		checkSegmentCollisions();
		checkProgress();
		move();
		updateSegments();
		checkPellet();

	}
	public void takeNetAction()
	{
		double[][][] state = getState();
		double[] obs = new double[state.length];
		for(int i=0;i<state.length;i++)
		{
		    obs[i] = state[i][0][0];
		}
		double[] noise = new double[net.getNumParams()];
		if(state == null){return;}

		double[] action = net.activateNoisy(obs, noise);

		double max = action[0];
		int choice = 0;

		for(int i=0;i<action.length;i++)
		{
			if(action[i] > max)
			{
				max = action[i];
				choice = i;
			}
		}

		takeAction(choice);
	}

	public void checkProgress()
	{
		if(pellet != null)
		{
			dist = getDist(position, pellet.getPos());
			if(dist < prevDist)
			{
				prevDist = dist;
				//fitness+=0.1;
			}
			else
			{
				if(fitness > 0)
				{
					//fitness-=0.1;
				}

			}
		}

		if(previousPosition[0] == position[0] && previousPosition[1] == position[1])
		{
			moveCounter++;
		}
		else
		{
			moveCounter = 0;
		}
		if(moveCounter > 50 || stepsSincePellet > 250)
		{
			kill();
		}
	}

	@Override
	public double takeAction(int actionNum)
	{
		//System.out.println(fitness+" | "+prevFitness);
		if(currentDirection == UP && actionNum == DOWN){return fitness-0.05;}
        if(currentDirection == DOWN && actionNum == UP){return fitness-0.05;}
        if(currentDirection == LEFT && actionNum == RIGHT){return fitness-0.05;}
        if(currentDirection == RIGHT && actionNum == LEFT){return fitness-0.05;}

		currentDirection = actionNum;
		return fitness;
	}

	@Override
	public double[][][] getState()
	{
		if(dead){return null;}

		loadInputs();
		return inputVector;
	}

	public void loadInputs()
	{
		/*inputVector[0][0][0] = velocity[0]/3d;
        inputVector[1][0][0] = velocity[1]/3d;
        inputVector[2][0][0] = (pellet.getX() - position[0]);
        inputVector[3][0][0] = (pellet.getY() - position[1]);
        inputVector[4][0][0] = position[0]/map.getPixelWidth();
        inputVector[5][0][0] = position[1]/map.getPixelHeight();
        inputVector[6][0][0] = checkNearestSegment();*/

		inputVector[0][0][0] = velocity[0]/3d;
		inputVector[1][0][0] = velocity[1]/3d;

		inputVector[2][0][0] = ((pellet.getX()) - (position[0]))/32d;
		inputVector[3][0][0] = ((pellet.getY()) - (position[1]))/32d;

		inputVector[4][0][0] = position[0]/map.getPixelWidth();
		inputVector[5][0][0] = position[1]/map.getPixelHeight();

		double[] sensorData = checkSensors();
		//System.out.println(nearest);
		inputVector[6][0][0] = (sensorData[0]);
		inputVector[7][0][0] = (sensorData[1]);
		inputVector[8][0][0] = (sensorData[2]);
	}

	public double[] checkSensors()
	{
	    updateSensors();
		double[] sensorOutputs = new double[]{1.1,1.1,1.1};
		int idx = 0;
		for(Rectangle2D sensor : sensors)
		{
			if(sensor == null)
			{
				break;
			}
			for(int i=0;i<segments.size();i++)
			{
				if(sensor.intersects(segments.get(i).getbbox()))
				{
					double[] c1 = new double[]{position[0]+width/2, position[1]+height/2};
					double[] c2 = new double[]{segments.get(i).getX()+segments.get(i).getWidth()/2, segments.get(i).getY()+segments.get(i).getHeight()/2};
					double dist = getDist(c2,c1)/77.0;
					sensorOutputs[idx] = dist;
					break;
				}
			}
			for(Tile t : map.getTiles())
			{
				if(!t.isCollidable()){continue;}
				
				if(sensor.intersects(t.getbbox()))
				{
					double[] c1 = new double[]{position[0]+width/2, position[1]+height/2};
					double[] c2 = new double[]{t.getX()+t.getWidth()/2, t.getY()+t.getHeight()/2};
					double dist = getDist(c2,c1)/86.0;
					if(dist < sensorOutputs[idx])
					{
						sensorOutputs[idx] = dist;
					}
				}
			}
			
			idx++;
		}
		return sensorOutputs;
	}
	public double[] checkFront()
	{
		double trigger = -1;
		double[] out = new double[2];

		double lookAhead = 5;
		double x1 = (position[0]+width/2);
		double y1 = (position[1]+height/2);
		double x2 = (position[0]+width/2+direction[0]*SEGMENT_SIZE*lookAhead);
		double y2 = (position[1]+height/2+direction[1]*SEGMENT_SIZE*lookAhead);

		Line2D line2 = new Line2D.Double(x1,y1,x2,y2);
		Line2D line1 = new Line2D.Double(position[0],position[1],
				(position[0]+direction[0]*SEGMENT_SIZE*lookAhead),
				(position[1]+direction[1]*SEGMENT_SIZE*lookAhead));

		for(Tile t : level.getMap().getTiles())
		{
			if(!t.isCollidable())
			{
				continue;
			}
			if(line2.intersects(t.getbbox()) || line1.intersects(t.getbbox()))
			{
				double dist = getDist(position,t.getPos());
				if(dist < trigger || trigger == -1)
				{
					trigger = dist;
				}
			}
		}
		out[0] = trigger;
		return out;
	}

	private void move()
	{
		moveToCell();
		if(movingToCell)
		{
			return;
		}
		switch(currentDirection)
		{
		case UP:
			angle = -Math.PI/2;
			direction[0] = 0;
			direction[1] = -1;
			break;

		case DOWN:
			angle = -(Math.PI+Math.PI/2);
			direction[0] = 0;
			direction[1] = 1;
			break;

		case LEFT:
			angle = Math.PI;
			direction[0] = -1;
			direction[1] = 0;
			break;

		case RIGHT:
			angle = 0;
			direction[0] = 1;
			direction[1] = 0;
			break;

		default:
			direction[0] = 0;
			direction[1] = 0;
		}
		velocity[0] = 3*direction[0];
		velocity[1] = 3*direction[1];
	}

	private void moveToCell()
	{
		if(velocity[0] == 0 && velocity[1] == 0) 
		{
			movingToCell = false;
			return;
		}

		if(!movingToCell)
		{
			movingToCell = true;         
			nextCell[0] = position[0]+direction[0]*(SEGMENT_SIZE);
			nextCell[1] = position[1]+direction[1]*(SEGMENT_SIZE);

			prevCell[0] = position[0];
			prevCell[1] = position[1];
		}
		else
		{
			if(nextCell[0] <= position[0] && 3+nextCell[0]>position[0])
			{
				if(nextCell[1] <= position[1] && 3+nextCell[1]>position[1])
				{
					movingToCell = false;
					positions.add(new double[] {prevCell[0], prevCell[1]});
					stepsSincePellet++;
					//updateSensors();

					if(net != null)
					{
						takeNetAction();
					}
				}
			}
		}
	}

	private void checkSegmentCollisions()
	{
		Rectangle2D bbox = head.getbbox();
		for(int i=0,stop=segments.size();i<stop;i++)
		{
			if(segments.get(i).getbbox().intersects(bbox))
			{
				//System.out.println("\nINTERSECTION!\nBBOX: "+bbox+"\nSEGMENT: "+segments.get(i).getbbox());
				kill();
			}
		}
	}

	private void updateSegments()
	{
		if(positions.size() > segments.size())
		{
			positions.remove(0);
		}
		head.setPos(new double[] {position[0], position[1]});
		for(int i=0,stop=segments.size();i<stop;i++)
		{
			if(positions.size() <= i)
			{
				break;
			}
			double[] prev = segments.get(i).getPos().clone();
			segments.get(i).setPos(positions.get(i));

			Rectangle2D r1 = head.getbbox();
			Rectangle2D r2 = segments.get(i).getbbox();
			if(r2.intersects(r1))
			{
				segments.get(i).setPos(prev);
				continue;
			}

			//System.out.println("SET SEGMENT TO POS: "+positions.get(i)[0]+","+positions.get(i)[1]);
			//System.out.println("HEAD POS: "+head.getPos()[0]+","+head.getPos()[1]);
			segments.get(i).setPos(positions.get(i));
		}
	}

	private void checkPellet()
	{
		if(pellet == null)
		{
			pellet = level.spawnPellet();
		}
		if(pellet.getbbox().intersects(head.getbbox()))
		{            
			handlePelletCollision();
		}
	}
	private void handlePelletCollision()
	{
		//level.freeSpawnPoint(pellet.getPos());
		pellet.kill();
		if(fitness < 0)
		{
			fitness=0;
		}
		fitness++;
		dist = Double.MAX_VALUE;
		prevDist = Double.MAX_VALUE;

		stepsSincePellet = 0;
		double[] pos = new double[]{0,0};
		if(segments.size() > 0)
		{
			segments.add(new SnakeSegment(pos,0,camera,segments.get(segments.size()-1), false));
		}
		else
		{
			segments.add(new SnakeSegment(pos,0,camera,head, false));
		}
		pellet = level.spawnPellet();
	}

	@Override
	public void kill()
	{
	    //System.out.println("agent died!");
		dead = true;
		fitness -= 1;
	}

	public void updateSensors()
	{
		double o1 = Math.cos(angle);
		double o2 = Math.sin(angle);
		double longer = 61;
		double shorter = 6;

		double sw = shorter + Math.abs(o1)*longer;
		double sh = shorter + Math.abs(o2)*longer;
		double sx = (position[0] + width*Math.max(0,o1) + (width/2)*Math.abs(o2));
		double sy = (position[1] + height*Math.max(0, o2)+ (height/2)*Math.abs(o1));
		Rectangle2D sensor = null;

		sensor = new Rectangle2D.Double((sx + sw*Math.min(0, o1) - (sw/2)*Math.abs(o2)),
				(sy + sh*Math.min(0, o2) - (sh/2)*Math.abs(o1)),sw,sh);

		sensors[0] = sensor;

		angle+=Math.PI/2;
		o1 = Math.cos(angle);
		o2 = Math.sin(angle);
		sw = shorter + Math.abs(o1)*longer;
		sh = shorter + Math.abs(o2)*longer;
		sx = (position[0] + width*Math.max(0,o1) + (width/2)*Math.abs(o2));  
		sy = (position[1] + height*Math.max(0, o2)+ (height/2)*Math.abs(o1));


		sensor = new Rectangle2D.Double((sx + sw*Math.min(0, o1) - (sw/2)*Math.abs(o2)),
				(sy + sh*Math.min(0, o2) - (sh/2)*Math.abs(o1)),sw,sh);

		sensors[1] = sensor;

		angle-=Math.PI;
		o1 = Math.cos(angle);
		o2 = Math.sin(angle);
		sw = shorter + Math.abs(o1)*longer;
		sh = shorter + Math.abs(o2)*longer;
		sx = (position[0] + width*Math.max(0,o1) + (width/2)*Math.abs(o2));  
		sy = (position[1] + height*Math.max(0, o2)+ (height/2)*Math.abs(o1));

		sensor = new Rectangle2D.Double((sx + sw*Math.min(0, o1) - (sw/2)*Math.abs(o2)),
				(sy + sh*Math.min(0, o2) - (sh/2)*Math.abs(o1)),sw,sh);

		sensors[2] = sensor;
		angle += Math.PI/2;
	}

	public void keyPressed(KeyEvent e)
	{

		if(e.getKeyChar() == 'w')
		{
			currentDirection = UP;
		}
		else if(e.getKeyChar() == 's')
		{
			currentDirection = DOWN;
		}
		else if(e.getKeyChar() == 'a')
		{
			currentDirection = LEFT;
		}
		else if(e.getKeyChar() == 'd')
		{
			currentDirection = RIGHT;
		}
	}

	@Override
	public void eRender(Graphics2D g, double interp)
	{
		if(dead){return;}
		if(pellet != null)
		{
			pellet.render(g, interp);
		}
		if(segments != null && segments.size() > 0)
		{
			for(SnakeSegment s : segments)
			{
				s.render(g, interp);
			}
		}
		head.render(g, interp);
		updateSensors();
		checkSensors();
		
		
		
		for(Rectangle2D sensor : sensors)
		{
			if(sensor == null)
			{
				break;
			}
			g.setColor(Color.RED);
			for(int i=0;i<segments.size();i++)
			{
				if(sensor.intersects(segments.get(i).getbbox()))
				{
					double[] c1 = new double[]{position[0]+width/2, position[1]+height/2};
					double[] c2 = new double[]{segments.get(i).getX()+segments.get(i).getWidth()/2, segments.get(i).getY()+segments.get(i).getHeight()/2};
					double dist = getDist(c2,c1)/77.0;
					g.setColor(Color.GREEN);
					break;
				}
			}
			Rectangle2D scaled = new Rectangle2D.Double(sensor.getX()*scale[0], sensor.getY()*scale[1], sensor.getWidth()*scale[0], sensor.getHeight()*scale[1]);
			g.draw(scaled);
		}

	}
	public void setPolicy(FFNetwork n)
	{
		net = n;
	}

	public void stop()
	{
	}


	public double getDist(double[] p1, double[] p2)
	{
		return Math.sqrt(Math.pow(p1[0]-p2[0],2)+Math.pow(p1[1]-p2[1], 2));
	}

	public double getFitness()
	{
		if(fitness == 0)
		{
			return Math.random();
		}
		return fitness;
	}

	public boolean isChamp()
	{
		return champ;
	}
	public ArrayList<SnakeSegment> getSegments()
	{
		return segments;
	}
	public static final int UP=0, DOWN=1, LEFT=2, RIGHT=3;
	public static final int SEGMENT_SIZE=10;



}