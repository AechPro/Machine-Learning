package NEAT.Population;

import java.awt.Graphics2D;

import NEAT.Display.DisplayObject;

public class Phenotype extends DisplayObject
{
	public Phenotype()
	{
		super();
	}
	public Phenotype(int _renderPriority, int _updatePriority)
	{
		super(_renderPriority, _updatePriority);
	}
	@Override
	public void update(double delta)
	{
	}
	@Override
	public void render(Graphics2D g) 
	{
	}
	public void saveAsImage(String dir)
	{
		
	}
}
