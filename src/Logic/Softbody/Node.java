package Logic.Softbody;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import Logic.Vector2;

public class Node {
	String type; //node, hook, fixed
	public Vector2 pos = new Vector2();
	Vector2 last_pos;
	public Vector2 vel = new Vector2();
	public Vector2 force = new Vector2();
	double mass;
	boolean external = false;
	
	//constructor
			//position
		//pos, vel, force
	
	public Node(Vector2 in, double mass, String type) {
		//node & fixed -> .set
		//hook -> =
		
		this.type = type;
		if (this.type.equals("hook")) this.pos = in;
		else this.pos.set(in);
		this.last_pos = new Vector2(this.pos);
		this.vel = new Vector2();
		this.force = new Vector2();
		
		this.mass = mass;
	}
	
	public Node(String[] in, Vector2 pos, double mass) {
		//external purely used for sorting when saving the file.
		this.type = in[0];
		this.pos.set(Vector2.add(new Vector2(Double.parseDouble(in[1]), Double.parseDouble(in[2])), pos));
		//from here, only node and fixed, no hook from here
		if (in[0].equals("node")) {
			if (in.length == 3) this.mass = mass;
			else this.mass = Double.parseDouble(in[3]);
		}
		else this.mass = 0;
		
		this.vel = new Vector2();
		this.force = new Vector2();
	}
	
	//update (verlet integration or better)
	public void update(double dt) {
		
		double max_f = 50, v_drag = 0.85;
		
		if (this.type.equals("node")) {
			if (this.force.l() > max_f) this.force = this.force.norm()._mult(max_f);
			
			//euler integration
			this.vel.add(this.force._mult(1.0 / this.mass)._mult(dt));
			
			this.last_pos = new Vector2(this.pos);
			if (this.type.equals("node")) this.pos.add(this.vel._mult(v_drag));
		}
		else {
			this.vel.set(Vector2.zero);
			this.force.set(Vector2.zero);
		}
	}
	//TODO: fix
	public void draw_node(Graphics g, JPanel pane, double xpos, double ypos, String location) {
		
		this.pos.draw_node(g, pane, xpos, ypos, location, Color.magenta);
	}
	public String toString() {
		return "POS: " + this.pos + " VEL: " + this.vel + " FORCE: " + this.force;
	}
	
}
