package NEAT.Display;

import java.awt.Graphics2D;

public abstract class DisplayObject
{
	public static final int RENDER_IN_FRONT = 0;
	public static final int RENDER_LAST = RENDER_IN_FRONT;
	public static final int RENDER_FIRST = 9;
	public static final int RENDER_IN_BACK = RENDER_FIRST;
	
	public static final int UPDATE_FIRST = 9;
	public static final int UPDATE_LAST = 0;
	
	protected int renderPriority;
	protected int updatePriority;
	public abstract void update(double delta);
	public abstract void render(Graphics2D g);
	public DisplayObject(int _renderPriority, int _updatePriority)
	{
		renderPriority = _renderPriority;
		updatePriority = _updatePriority;
	}
	public DisplayObject()
	{
		renderPriority = RENDER_IN_FRONT;
		updatePriority = UPDATE_LAST;
	}
	public int getRenderPriority()
	{
		return renderPriority;
	}
	public int getUpdatePriority()
	{
		return updatePriority;
	}
	public void setRenderPriority(int priority) {renderPriority = priority;}
	public void setUpdatePriority(int priority) {updatePriority = priority;}
	

}
