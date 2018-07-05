package NEAT.Simulations.FishMaze;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import NEAT.Display.DisplayObject;

public class GameBoard extends DisplayObject
{
	private int width,height;
	private int[] tileSize;
	private ArrayList<Tile> tiles;
	private ArrayList<Tile> collidableTiles;
	private Tile home;
	private Tile dest;
	public GameBoard(int[] tSize)
	{
		tileSize = tSize;
		home = null;
		dest = null;
		tiles = null;
		collidableTiles = null;
		width=0;
		height=0;
	}
	public void create(String fileName)
	{
		try
		{
			home = null;
			dest = null;
			tiles = new ArrayList<Tile>();
			collidableTiles = new ArrayList<Tile>();
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line = null;
			String delimiters = " ";
			
			int w = 0;
			int h = 0;
			
			while((line = reader.readLine()) != null)
			{
				h++;
				String[] splitLine = line.split(delimiters);
				if(splitLine.length>w){w = splitLine.length;}
				for(int i=0;i<splitLine.length;i++)
				{
					int repr = Integer.parseInt(splitLine[i]);
					if(repr == 2)
					{
						home = new Tile(tileSize[0]*i,tileSize[1]*(h-1),repr);
						tiles.add(home);
					}
					else if(repr == 3)
					{
						dest = new Tile(tileSize[0]*i,tileSize[1]*(h-1),repr);
						tiles.add(dest);
					}
					else
					{
						Tile newTile = new Tile(tileSize[0]*i,tileSize[1]*(h-1),repr);
						if(newTile.isCollidable()){collidableTiles.add(newTile);}
						tiles.add(newTile);
					}
				}
			}
			width=w;
			height=h;
			reader.close();
			scoreTiles();
		}
		catch(Exception e){e.printStackTrace();}
	}
	
	@Override
	public void update(double delta)
	{
		
	}
	@Override
	public void render(Graphics2D g)
	{
		for(int i=0,stop=tiles.size();i<stop;i++)
		{
			tiles.get(i).render(g);
		}
	}
	
	public Tile getTile(double[] pos)
	{
		
		int x = (int)pos[0];
		int y = (int)(pos[1]/tileSize[1]);
		
		int index = (y-1)*width;
		index+=x/tileSize[0];
		if(index<0 || index>=tiles.size()){return null;}
		
		return tiles.get(index);
	}
	
	public void scoreTiles()
	{
		double score = 0;
		double[] h = new double[]{home.getPosition()[0],home.getPosition()[1]};
		double[] d = new double[]{dest.getPosition()[0],dest.getPosition()[1]};
		double x = 0;
		double y = 0;
		for(int i=0;i<tiles.size();i++)
		{
			x = tiles.get(i).getPosition()[0];
			y = tiles.get(i).getPosition()[1];
			score = Math.sqrt(Math.pow(x-d[0],2)+Math.pow(y-d[1],2));//-Math.sqrt(Math.pow(x-h[0],2)+Math.pow(y-h[1],2));
			score = Math.abs(x-d[0])+Math.abs(y-d[1]);
			tiles.get(i).setScore(score);
		}
	}
	
	public ArrayList<Tile> getTiles() {return tiles;}
	public ArrayList<Tile> getCollidableTiles(){return collidableTiles;}
	public Tile getHome() {return home;}
	public Tile getDest() {return dest;}
	public int getWidth() {return width;}
	public int getHeight() {return height;}
	public int[] getTileSize() {return tileSize;}
	
	public void setWidth(int width) {this.width = width;}
	public void setHeight(int height) {this.height = height;}
	public void setTileSize(int[] tileSize) {this.tileSize = tileSize;}
	public void setTiles(ArrayList<Tile> tiles) {this.tiles = tiles;}
	public void setHome(Tile home) {this.home = home;}
	public void setDest(Tile dest) {this.dest = dest;}

}
