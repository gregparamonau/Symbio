package Logic.Collision;

import java.awt.Graphics;

import Logic.Vector2;
import Logic.Softbody.Node;
import Logic.Softbody.SoftBody;
import Main.Game;
import Main.Start;

import java.awt.Color;


public class Polygon {
	public Vector2[] pnts;
	public Line[] sides;
	public Rectangle bounds; //every frame
	//Constructors
		//softbody parameter
	public Polygon(SoftBody in) {
		
		this.pnts = new Vector2[in.nodes.length];
		this.sides = new Line[in.nodes.length];
		
		for (int x = 0; x<this.pnts.length; x++) 
			this.pnts[x] = in.nodes[x].pos;
		for (int x = 0; x<this.sides.length; x++)
			this.sides[x] = new Line(this.pnts[x], this.pnts[(x + 1) % this.pnts.length]);
		
		
		this.get_bounds();
		//TODO: this
		//make Polygon that always follows around with the points of SoftBody
		
	}
		//vector2[] parameter
	public Polygon(Vector2[] in) {
		this.pnts = in;
		this.sides = new Line[in.length];
		
		for (int x = 0; x<this.sides.length; x++) {
			this.sides[x] = new Line(this.pnts[x], this.pnts[(x + 1) % this.pnts.length]);
		}
		
		this.get_bounds();
	}
	//displace
	
	public void displace(Vector2 pos, Node in, boolean reflect_velocity) {
		
		//TODO: make this into ONE big function, where we check every displaced position,
		//checking whether the position is a displacement AWAY or TOWARDS the center of the softbody
		//only set position equal if displacement TOWARDS center
		//use DOT product
		
		//try adding a timer to a node, where for the next 10 frames after its been displaced, its force is set to zero or smth
		
		//implement the interpolating math
		
		Vector2 spot = Line.find_node_on_line(this.sides[0], in.pos);
		double l = Vector2.dist(spot, in.pos);
		
		//if (l < 1e-3) return;
		
		int index = 0;
		
		for (int x = 1; x<this.sides.length; x++) {
			//if (Vector2.dot(this.sides[x].norm(), Vector2.sub(in.pos, pos)) > 0) continue;
			
			//System.out.println("X: " + x + " SIDE: " + this.sides[x] + " VEC: " + in.pos);
			
			Vector2 spot_temp = Line.find_node_on_line(this.sides[x], in.pos);
			
			if (Vector2.dot(Vector2.sub(spot_temp, in.pos), Vector2.sub(in.pos, pos)) > 0) continue;
			
			//System.out.println("SPOT_TEMP: " + spot_temp);
			
			//if (pos.l() > 1e-3 && Vector2.dot(Vector2.sub(spot_temp, in.pos), Vector2.sub(pos, in.pos)) < 0) continue;
			
			double dist = Vector2.dist(spot_temp, in.pos);
			
			if (dist < l) {
				l = dist;	
				index = x;
				spot = spot_temp;
			}
			
			//System.out.println("SPOT: " + spot);
		}
		
		Vector2 n = Vector2.sub(this.sides[index].a, this.sides[index].b).perp().norm();
		
		in.force = new Vector2();//Vector2.sub(Vector2.reflect(in.vel, n).mult(-1), in.vel).mult(in.mass);
		
		in.vel = new Vector2();//Vector2.reflect(in.vel, n).mult(-1);
		
		
		in.pos.set(Vector2.add(spot, n._mult(-l * 0.1)));
	}
	
	public double area() {
		double out = 0;
		for (int x = 0; x<this.sides.length; x++) {
			out += 0.5 * (this.sides[x].a.y + this.sides[x].b.y) * (this.sides[x].a.x - this.sides[x].b.x);
		}
		
		return out;
	}
	
	public boolean intersect_circle(Vector2 in, double r) {
		boolean out = false;
		
		for (int x = 0; x<this.pnts.length; x++) {
			out |= Vector2.dist(in, this.pnts[x]) < r;
		}
		return out;
	}
	//intersect (horizontal ray method

	//concept for intersection algorithm:
	//insert point randomly into array, and calculate volume
	//if point INSIDE polygon, area goes down
	//if points OUTSIDE, area goes up
	//very clever if no self-intersections involving new point
	/*public boolean intersect(Vector2 in) {
		//10000 is an arbitrarily large constant
		Line temp = new Line(in, new Vector2(in.x + 10000, in.y));
		
		int count = 0;
		
		for (int x = 0; x<this.sides.length; x++) {
			Vector2 bounds = this.sides[x].y_bounds();
			if (bounds.x > in.y || bounds.y < in.y) continue;
			
			count += Line.horiz_intersect(temp, this.sides[x]) ? 1 : 0;
		}
		
		return count % 2 == 0;
	}*/

	public boolean intersect(Vector2 in) {
		long start = System.nanoTime();
    	int windingNumber = 0;

    	for (int i = 0; i < pnts.length; i++) {
        	Vector2 a = pnts[i];
        	Vector2 b = pnts[(i + 1) % pnts.length];

        	if (a.y <= in.y) {
            	if (b.y > in.y && isLeft(a, b, in) > 0) {
                	windingNumber++;
            	}
        	} else {
            	if (b.y <= in.y && isLeft(a, b, in) < 0) {
                	windingNumber--;
            	}
        	}
    	}
    
    	long end = System.nanoTime();
    
    	//System.out.println("DT INTERSECT: " + (double)(end - start) / 1000000);

    	return windingNumber != 0;
	}
	
	public void get_bounds() {
		//set this bounds
		
		this.bounds = new Rectangle(this.pnts[0].x, this.pnts[0].y, 0, 0);
		
		//extend bounds
		
		for (int x = 1; x<this.pnts.length; x++) {
			this.bounds.expand(this.pnts[x]);
		}
		
	}

	private double isLeft(Vector2 a, Vector2 b, Vector2 p) {
		return (b.x - a.x) * (p.y - a.y) - (p.x - a.x) * (b.y - a.y);
	}
	
	public boolean intersect(Polygon in) {
		if (!Rectangle.intersect(this.bounds, in.bounds)) return false;
		
		for (int x = 0; x<this.pnts.length; x++) {
			if (in.intersect(this.pnts[x])) return true;
		}
		return false;
	}
	
	public void draw(Graphics g) {
		for (int x = 0; x<this.sides.length; x++) {
			//this.sides[x].draw_line(g, Color.green, Start.pane, Game.cam.pos.x, Game.cam.pos.y, "game", false);
		}
		
		//this.bounds.draw_border(g, Start.pane, Game.cam.pos.x, Game.cam.pos.y, "game");
	}
	//bounding box (returns rect)
	//TODO: this
	
	//
}
