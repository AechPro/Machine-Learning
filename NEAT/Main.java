package NEAT;

import java.awt.Color;
import java.util.ArrayList;

import javax.swing.JFrame;

import NEAT.Display.*;
import NEAT.Population.Phenotype;

public class Main
{
	private static final int width = 1920, height = 1080;
	private ArrayList<Phenotype> phenotypes;
	private ArrayList<DisplayObject> objectsToDisplay;
	public Main()
	{
		init();
	}
	public void init()
	{
		phenotypes = new ArrayList<Phenotype>();
		JFrame f = new JFrame("NEAT");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setContentPane(new Window(width,height,60,objectsToDisplay));
		f.setSize(width,height);
		f.setVisible(true);
	}
	public static void main(String[] args){new Main();}
}
