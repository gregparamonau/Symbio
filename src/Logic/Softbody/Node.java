package Logic.Softbody;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import Logic.Vector2;

public class Node {
	public Vector2 pos;
	Vector2 last_pos;
	public Vector2 vel;
	public Vector2 force;
	double mass;
	
	//constructor
			//position
		//pos, vel, force
	
	public Node(double x, double y, double mass) {
		this.pos = new Vector2(x, y);
		this.last_pos = new Vector2(this.pos);
		this.vel = new Vector2();
		this.force = new Vector2();
		
		this.mass = mass;
	}
	
	//update (verlet integration or better)
	public void update(double dt) {
		
		double max_f = 50, v_drag = 0.85;
		
		//clamping force
		if (this.force.l() > max_f) this.force = this.force.norm()._mult(max_f);
		
		/*Vector2 acceleration = this.force.mult(1.0 / this.mass);
		Vector2 position_diff = Vector2.sub(this.pos, this.last_pos).mult(v_drag);
		Vector2 next_pos = Vector2.add(
			    Vector2.add(this.pos, position_diff), // P_t + (P_t - P_{t-dt}) * v_drag
			    acceleration // + a * dt^2 (assuming dt^2 is 1 or absorbed into Force/Mass scaling)
			);*/
		
		//euler integration
		this.vel.add(this.force._mult(1.0 / this.mass)._mult(dt));
		
		this.last_pos = new Vector2(this.pos);
		this.pos.add(this.vel._mult(v_drag));
		
		//this.force.set(new Vector2(0, 0));
		
		//intersection don't forget!
		
		//update force
		//update position
		//update last position
	}
	//TODO: fix
	public void draw_node(Graphics g, JPanel pane, double xpos, double ypos, String location) {
		
		this.pos.draw_node(g, pane, xpos, ypos, location, Color.magenta);
	}
	public String toString() {
		return "POS: " + this.pos + " VEL: " + this.vel + " FORCE: " + this.force;
	}
	
}
