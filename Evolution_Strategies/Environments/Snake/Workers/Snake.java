package Evolution_Strategies.Environments.Snake.Workers;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import Evolution_Strategies.Configs.Config;
import Evolution_Strategies.Environments.EnvironmentAgent;
import Evolution_Strategies.Environments.Snake.engine.SnakeLevel;
import Evolution_Strategies.Policies.FFNN.Network;
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
    private Pellet pellet;
    private int[] direction;
    private SnakeLevel level;
    private double[][][] inputVector;
    private double[] outputVector;
    private SnakeSegment head;
    private TileMap map;
    private int stepsSincePellet;
    private Network net;

    private double prevDist, dist;
    private double prevFitness;
    
    private int numInputs;

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
        numInputs = Config.POLICY_INPUT_NODES;
        dead = false;
        champ = false;
        direction = new int[2];
        inputVector = new double[numInputs][1][1];
        hasSelectiveCollisions = true;
        movingToCell = false;
        fitness = 0;
        nextCell = new double[2];
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
        head = new SnakeSegment(new double[] {position[0], position[1]}, 0, camera, null);
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
            fitness-=20;
        }
    }

    @Override
    public void eUpdate()
    {
        prevFitness = fitness;
        if(dead){return;}     
        if(net != null)
        {
            double[] inp = getState();
            if(inp == null){kill();}
            net.activate(inp);
            double[] action = net.readOutputVector();
            int arg = 0;
            double max = 0;
            for(int i=0;i<action.length;i++)
            {
                if(action[i] > max)
                {
                    max = action[i];
                    arg = i;
                }
            }
            takeAction(arg);
        }
        checkProgress();
        move();
        updateSegments();
        checkPellet();
        
    }
    public void checkProgress()
    {
        if(pellet != null)
        {
            dist = getDist(position, pellet.getPos());
            if(dist < prevDist)
            {
                prevDist = dist;
                fitness+=0.1;
            }
            else
            {
                if(fitness > 0)
                {
                    fitness-=0.1;
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
        if(moveCounter > 50 || stepsSincePellet > 1500)
        {
            kill();
        }
    }
    
    @Override
    public double takeAction(int actionNum)
    {
        //System.out.println(fitness+" | "+prevFitness);
        double reward = fitness - prevFitness;
        /*
        if(currentDirection == UP && actionNum == DOWN){return -0.1;}
        if(currentDirection == DOWN && actionNum == UP){return -0.1;}
        if(currentDirection == LEFT && actionNum == RIGHT){return -0.1;}
        if(currentDirection == RIGHT && actionNum == LEFT){return -0.1;}*/
        
        currentDirection = actionNum;
        return reward;
    }

    @Override
    public double[] getState()
    {
        if(dead)
        {
            return null;
        }
        loadInputs();
        double[] state = new double[numInputs];
        for(int i=0;i<inputVector.length;i++)
        {
            state[i] = inputVector[i][0][0];
        }
        return state;
    }
    
    public void loadInputs()
    {
        inputVector[0][0][0] = velocity[0];
        inputVector[1][0][0] = velocity[1];
        inputVector[2][0][0] = ((pellet.getX()) - (position[0]));
        inputVector[3][0][0] = ((pellet.getY()) - (position[1]));
        inputVector[4][0][0] = checkFront()[0];
    }
    
    public void loadSensors()
    {
        angle-=Math.PI/2;
        double FOV = Math.PI/2;
        int itr = 0;

        //center sensor
        double o1 = Math.cos(angle);
        double o2 = Math.sin(angle);
        loadSensor(o1,o2,itr++);

        //up sensor
        o1 = Math.cos(angle - FOV/2);
        o2 = Math.sin(angle - FOV/2);
        loadSensor(o1,o2,itr++);

        //down sensor
        o1 = Math.cos(angle + FOV/2);
        o2 = Math.sin(angle + FOV/2);
        loadSensor(o1,o2,itr++);
        angle+=Math.PI/2;

    }

    public void loadSensor(double o1, double o2,int itr)
    {
        int x = 0;
        int y = 0;
        int r = 32;
        Tile t = null;
        int sensorRange = 3;
        double sx = (position[0]+width/2);
        double sy = (position[1]+height/2);
        double sw = o1*sensorRange*r;
        double sh = o2*sensorRange*r;
        Line2D sensorLine = new Line2D.Double(sx,sy,sw+sx,sh+sy);    
        double tileDist = 100;
        double segDist = 100;
        double pelletDist = 100;
        double checkDist = 0;
        synchronized(map)
        {
            for(double i=0;i<sensorRange;i+=0.5)
            {
                x = (int)(o1*r*(i) + position[0] + width/2);
                y = (int)(o2*r*(i) + position[1] + height/2);
                t = map.findTile(x,y);
                if(t == null) {continue;}
                if(t.isCollidable())
                {
                    Rectangle rect = new Rectangle((int)t.getPos()[0],(int)t.getPos()[1],(int)t.getWidth(),(int)t.getHeight());
                    if(sensorLine.intersects(rect))
                    {
                        checkDist = getDist(t.getPos(),position)/(r*sensorRange);
                        if(checkDist < tileDist)
                        {
                            tileDist = checkDist;
                        }
                    }   
                }
            }

            for(SnakeSegment s : segments)
            {
                if(sensorLine.intersects(s.getbbox()))
                {
                    checkDist = getDist(s.getPos(),position);
                    if(checkDist < segDist)
                    {
                        segDist = checkDist;
                    }
                }
            }
            if(pellet != null)
            {
                if(sensorLine.intersects(pellet.getbbox()))
                {
                    checkDist = getDist(pellet.getPos(),position);
                    if(checkDist < pelletDist)
                    {
                        pelletDist = checkDist;
                    }
                }
            }
        }
        
        double val = Math.min(tileDist, Math.min(segDist, pelletDist));
        if(val == tileDist || val == segDist)
        {
            val = -val;
        }
        
        inputVector[itr][0][0] = val;
    }

    public double[] checkFront()
    {
        double trigger = 100;
        double[] out = new double[2];

        double lookAhead = 32;
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
                trigger = dist;
                break;
            }
        }

        for(int i=0;i<segments.size()-1;i++)
        {
            if(line2.intersects(segments.get(i).getbbox()) || line1.intersects(segments.get(i).getbbox()))
            {
                double dist = getDist(position,segments.get(i).getPos());
                if(dist < trigger)
                {
                    trigger = dist;
                }
                break;
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
            angle = Math.PI/2;
            direction[0] = 0;
            direction[1] = -1;
            break;

        case DOWN:
            angle = Math.PI+Math.PI/2;
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
            nextCell[0] = position[0]+direction[0]*SEGMENT_SIZE;
            nextCell[1] = position[1]+direction[1]*SEGMENT_SIZE;
        }
        else
        {
            if(nextCell[0] <= position[0] && 3+nextCell[0]>position[0])
            {
                if(nextCell[1] <= position[1] && 3+nextCell[1]>position[1])
                {
                    movingToCell = false;
                    positions.add(new double[] {position[0], position[1]});
                    checkSegmentCollisions();
                    stepsSincePellet++;
                    //fitness+=0.001;
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
                fitness-=20;
                dead = true;
            }
        }
    }

    private void updateSegments()
    {
        if(positions.size() > segments.size())
        {
            positions.remove(0);
        }
        for(int i=0,stop=segments.size();i<stop;i++)
        {
            if(positions.size() <= i)
            {
                break;
            }
            segments.get(i).setPos(positions.get(i));
        }
        head.setPos(new double[] {position[0], position[1]});
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

        fitness+=10;
        dist = Double.MAX_VALUE;
        prevDist = Double.MAX_VALUE;

        stepsSincePellet = 0;

        double[] pos = new double[] {0,0};
        if(segments.size() > 0)
        {
            segments.add(new SnakeSegment(pos,0,camera,segments.get(segments.size()-1)));
        }
        else
        {
            segments.add(new SnakeSegment(pos,0,camera,head));
        }
        pellet = level.spawnPellet();
    }

    public void keyPressed(KeyEvent e)
    {

        /*if(e.getKeyChar() == 'w')
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
        }*/
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
        /*double lookAhead = 10;

        double x1 = scale[0]*(position[0]+width/2);
        double y1 = scale[1]*(position[1]+height/2);
        double x2 = (position[0]+width/2+direction[0]*SEGMENT_SIZE*lookAhead)*scale[0];
        double y2 = (position[1]+height/2+direction[1]*SEGMENT_SIZE*lookAhead)*scale[1];

        g.setColor(Color.YELLOW);
        Line2D line1 = new Line2D.Double(scale[0]*position[0],scale[1]*position[1],
                (position[0]+direction[0]*SEGMENT_SIZE*lookAhead)*scale[0],
                (position[1]+direction[1]*SEGMENT_SIZE*lookAhead)*scale[1]);
        Line2D line2 = new Line2D.Double(x1,y1,x2,y2);
        g.draw(line1);
        g.draw(line2);*/
    }
    public void setPolicy(Network n)
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
