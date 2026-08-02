package Logic.Softbody;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import Logic.Vector2;
import Logic.Collision.Line;

public class Spring {
	Node a, b;
	Vector2 hook;
	double rest_length, k;
	boolean hooked;
	double max_length = 1.2;
	//constructors
		//Node Node
		//Node Vector2 (hooked)
	
	public Spring(Node a, Node b, double k) {
		this.a = a;
		this.b = b;
		
		this.k = k;
		
		this.rest_length = this.length();
		
		this.hooked = false;
		
	}
	
	public Spring(Node a, Vector2 b, double k) {
		this.a = a;
		this.hook = b;
		
		this.k = k;
		
		this.hooked = true;
		
		this.rest_length = this.length();
	}
	
	
	//length()
	public double length() {
		if (this.hooked) return Vector2.dist(this.a.pos, hook);
		
		return Vector2.dist(this.a.pos, this.b.pos);
	}
	
	//update (apply forces to nodes)
	
	public void update() {
		if (this.hooked) {
			Vector2 a = Vector2.sub(this.hook, this.a.pos);
			if (a.l() < 1E-3) return;
			Vector2 dir = a.norm();
			
			double kd = 1.75;
			
			this.a.force.add(dir._mult((a.l() - this.rest_length) * this.k - Vector2.dot(dir, this.a.vel) * kd));
			return;
			//return;
		}
		
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
	}
	
	//total distance
	public void shorten(double distance) {
		if (distance < 0) return;
		
		if (this.hooked) {
			Vector2 temp = Vector2.sub(this.hook, this.a.pos).norm();
			this.a.pos.add(Vector2.mult(temp, distance));
			return;
		}
		
		Vector2 temp = Vector2.sub(this.b.pos, this.a.pos).norm();
		this.a.pos.add(Vector2.mult(temp, distance * 0.5));
		this.b.pos.add(Vector2.mult(temp, -distance * 0.5));
	}
	
	public void draw(Graphics g, JPanel pane, double xpos, double ypos, String location, boolean draw_nodes) {
		
		
		g.setColor(Color.white);
		Line out;
		
		if (this.hooked) {
			out = new Line(this.a.pos, this.hook);
		}
		
		else out = new Line(this.a.pos, this.b.pos);
		
		out.draw_line(g, Color.white, pane, xpos, ypos, location, draw_nodes);


	}
	//damping method?
		// Fd = (B - A)/|B - A| * (V_B - V_A) k_d
		//clamp force
}
