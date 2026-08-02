package GameObject;

import java.awt.Graphics;

import javax.swing.JPanel;

import Logic.Vector2;
import Logic.Collision.Rectangle;

public abstract class GameTemplate {
	
	//get object from String input
	public static GameObject get_obj(String [] in, int id) {
		return null;
	}
	
	public void start() {} //do everything that you need to do upon startup
	//update every frame
	public void update() {}
	
	//whether or not a rectangle collides with the object
	public boolean collide_with(Rectangle in, boolean col_action) {
		return false;
	}
	
	//what to do upon a collision
	public void collision_action() {}
	
	//displace to resolve a collision
	public Vector2 displace_entity(Rectangle in, int direction) {
		return Vector2.zero;
	}
	
	//give momentum upon a jump
	public void give_jump_momentum() {}
	
	//scale (for save/load purposes)
	public void scale(double in) {}
	
	//draw
	public void draw_object(Graphics g, JPanel pane, double xin, double yin, String location) {}
	
	//move it (used in leveleditor & in mover logic)
	public void move(Vector2 in) {}
	
	//properties panel in leveleditor
	public boolean properties(int node_sel) {
		return false;
	}
	
	//print line
	public String toString() {
		return null;
	}
}
