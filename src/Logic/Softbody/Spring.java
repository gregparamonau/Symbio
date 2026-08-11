package Logic.Softbody;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import Logic.Vector2;
import Logic.Collision.Line;

public class Spring {
	Node a, b;
	double rest_length, k;
	double max_length = 1.2;
	//constructors
		//Node Node
		//Node Vector2 (hooked)
	
	public Spring(Node a, Node b, double k) {
		this.a = a;
		this.b = b;
		
		this.k = k;
		
		this.rest_length = this.length();
		
	}
	
	
	//length()
	public double length() {
		return Vector2.dist(this.a.pos, this.b.pos);
	}
	
	//update (apply forces to nodes)
	
	public void update() {
		if (this.a.type.equals("node") && this.b.type.equals("node")) {
			double ds = this.length() - this.rest_length, kd = 1.75;//kd = damping factor
			Vector2 dir = Vector2.sub(this.b.pos, this.a.pos).norm();
			//spring force
			Vector2 Fs = dir._mult(ds * k);
			//drag force
			//TBD
			Vector2 Fd = dir._mult(Vector2.dot(dir, Vector2.sub(this.b.vel, this.a.vel)) * kd);
			
			Vector2 F = Vector2.add(Fs, Fd);
			//Vector2 Fd = ;//Vector2.add(this.a.vel.mult(-), Fs)
			
			this.a.force.add(F);
			this.b.force.add(F._mult(-1));
			
			this.shorten(this.length() - this.rest_length * max_length);
			return;
		}
		
		Vector2 a = Vector2.sub(this.b.pos, this.a.pos);
		if (a.l() < 1e-3) return;
		
		Vector2 dir = a.norm();
		double kd = 1.75;
		
		if (!this.b.type.equals("node"))
			this.a.force.add(dir._mult((a.l() - this.rest_length) * this.k - Vector2.dot(dir, this.a.vel) * kd));
		
		if (!this.a.type.equals("node")) {
			dir.mult(-1);
			this.b.force.add(dir._mult((a.l() - this.rest_length) * this.k - Vector2.dot(dir, this.b.vel) * kd));
		}
	}
	
	//total distance
	public void shorten(double distance) {
		if (distance < 0) return;
		
		if (!this.b.type.equals("node")) {
			Vector2 temp = Vector2.sub(this.b.pos, this.a.pos).norm();
			this.a.pos.add(Vector2.mult(temp, distance));
			return;
		}
		
		if (!this.a.type.equals("node")) {
			Vector2 temp = Vector2.sub(this.a.pos, this.b.pos).norm();
			this.b.pos.add(Vector2.mult(temp, distance));
			return;
		}
		
		Vector2 temp = Vector2.sub(this.b.pos, this.a.pos).norm();
		this.a.pos.add(Vector2.mult(temp, distance * 0.5));
		this.b.pos.add(Vector2.mult(temp, -distance * 0.5));
	}
	
	public void draw(Graphics g, JPanel pane, double xpos, double ypos, String location, boolean draw_nodes) {
		
		
		g.setColor(!this.a.type.equals("node") || !this.b.type.equals("node") ? Color.green : Color.white);
		Line out = new Line(this.a.pos, this.b.pos);
		
		out.draw_line(g, Color.white, pane, xpos, ypos, location, draw_nodes);


	}
	
	public String toString() {
		return "A: " + this.a + " B: " + this.b + " K: " + this.k;
	}
	//damping method?
		// Fd = (B - A)/|B - A| * (V_B - V_A) k_d
		//clamp force
}
