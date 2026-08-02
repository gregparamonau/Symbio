package Logic.Collision;

import java.util.ArrayList;
import java.util.List;

import Logic.Vector2;

//very useful class
public class Bezier {
	public Vector2[] control_points;
	public int[] fixed;
	public int num_seg;
	public Line[] lines;
	public Rectangle bounding_box;
	
	//this is set at 19 since that is a much less round number than 20, which helps reduce the amount of issues with the rendering
	public static final int res = 11;
	
	public Rectangle test;
	
	public Bezier(double num, double[] pnts) {
		this.control_points = new Vector2[(int)num];
		this.fixed = new int[(int)num];
		
		for (int x = 0; x<this.control_points.length; x++) {
			this.control_points[x] = new Vector2(pnts[3 * x], pnts[3 * x + 1]);
			this.fixed[x] = (int)pnts[3 * x + 2];
		}
		this.generate_lines();
		
		this.find_bounds();
	}
	
	public void generate_lines() {
		this.num_seg = 0;
		//have length - 1 sections on the Bezier, each of resolution res
		for (int x = 0; x<this.fixed.length; x++) this.num_seg += this.fixed[x];
		this.num_seg --;
		
		//System.out.println("num_seg: " + this.num_seg);
	    //System.out.println("fixed: " + java.util.Arrays.toString(this.fixed));
	    //System.out.println("control_points length: " + this.control_points.length);
	    
	    
	    
	    
		this.lines = new Line[res * (this.num_seg)];
		
		Vector2 prev_point = this.control_points[0];
		
		int start = 0, end = 0;
		for (int x = 0; x<this.num_seg; x++) {
			//find start
			for (int i = start; i < this.fixed.length; i++) if (this.fixed[i] == 1) {
				start = i;
				break;
			}
			for (int i = start + 1; i < this.fixed.length; i++) if (this.fixed[i] == 1) {
				end = i;
				break;
			}
			
			for (int i = 0; i < res; i++) {
				Vector2 newp = this.pnt((double)(i + 1) / res, start, end);
				lines[res * x + i] = new Line(prev_point, newp);
				
				prev_point = newp;
			}
			
			start = end;
			
		}
		
		//count sections first;
	}
	
	public Vector2[] to_polygon(boolean clockwise) {
		
		//01
		//32
		Vector2[] corners = new Vector2[] {
				new Vector2(this.bounding_box.pos.x - this.bounding_box.width / 2, this.bounding_box.pos.y + this.bounding_box.height / 2),
				new Vector2(this.bounding_box.pos.x + this.bounding_box.width / 2, this.bounding_box.pos.y + this.bounding_box.height / 2),
				new Vector2(this.bounding_box.pos.x + this.bounding_box.width / 2, this.bounding_box.pos.y - this.bounding_box.height / 2),
				new Vector2(this.bounding_box.pos.x - this.bounding_box.width / 2, this.bounding_box.pos.y - this.bounding_box.height / 2),

		};
		Vector2[] out = new Vector2[this.lines.length + 1];
		
		for (int x = 0; x<out.length - 1; x++) out[x] = this.lines[x].a;
		out[out.length - 1] = this.lines[lines.length - 1].b;
		
		double[] angles = new double[4];
		int[] ids = {0, 1, 2, 3};
		
		//if clockwise angle > 0, find smallest positive angle from all 4 corners
		for (int x = 0; x < 4; x++) {
			if (Vector2.dist(out[out.length - 1], corners[x]) < 1) {
				angles[x] = 0;
				continue;
			}
			
			angles[x] = Vector2.angle(Vector2.sub(corners[x], out[out.length - 1]), Vector2.sub(out[out.length - 2], out[out.length - 1]));
		}
		
		//sort according to direction
		boolean sorted = false;
		
		int count = 0;
		
		//if ccw, largest first VV
		//if cw smallest (negative too) first
		do {
			count++;
			sorted = true;
			for (int x = 1; x < 4; x++) {
				if ((clockwise ? -1 : 1) * angles[x] > (clockwise ? -1 : 1) * angles[x - 1]) {
					//change spots of angles
					double temp = angles[x];
					angles[x] = angles[x - 1];
					angles[x - 1] = temp;
					
					//change spots of ids
					int tempi = ids[x];
					ids[x] = ids[x - 1];
					ids[x - 1] = tempi;
					
					sorted = false;
				}
			}
		}while(!sorted && count < 200);
		
		if (count > 199) {
			System.out.println("ISSUE IN THE SORT METHOD");
		}
		
		count = 0;
		
		int target = ids[0];
		
		boolean on_side = this.on_side(out[0], out[out.length - 1], corners[target]);
		
		while(!on_side && count < 4) {
			
			count ++;
			out = Vector2.add_to_arr(out, corners[target]);
			
			target = (target + (clockwise ? 1 : -1) + 4) % 4;
			
			on_side = this.on_side(out[0], out[out.length - 1], corners[target]);
		}
		
		if (count > 4) System.out.println("ISSUE IN THE ADD CORNERS PART");
		
		for (int x = 0; x<out.length; x++) {
			System.out.println("out[" + x + "] = " + out[x]);
		}
		//System.exit(0);
		
		return out;
	}
	
	boolean on_side(Vector2 in, Vector2 a, Vector2 b) {
		
		return Vector2.dist(Line.find_node_on_line(new Line(a, b), in),in) < 1;
		//return (Math.abs(Vector2.angle(Vector2.sub(in, a), Vector2.sub(b, a))) < 1e-2);
	}
	
	public Line intersect_rect(Rectangle in) {
		
		for (int x = 0; x<this.lines.length; x++) {
			if (!Rectangle.intersect_line(in, this.lines[x].a, this.lines[x].b)) continue;
			System.out.println("LINE#: " + x);
			return this.lines[x];
		}
		return null;
	}
	
	public void find_bounds() {
		
		this.bounding_box = new Rectangle(this.lines[0].a, this.lines[0].b);
		
		for (int x = 1; x <this.lines.length; x++) {
			this.bounding_box.expand(this.lines[x].b);
		}
		//System.out.println(this.bounding_box);
		
		this.test = this.bounding_box;
		
	}
	
	//return the point of the Bezier at specified t value
	public Vector2 pnt(double t, int start, int end) {
		Vector2 out = new Vector2(0, 0);
		for (int x = start; x<end + 1; x++) {
			out.add(Vector2.mult(this.control_points[x], choose(end - start, x - start) * Math.pow(1 - t, end - x) * Math.pow(t, x - start)));
		}
		
		return out;
	}
	
	public static int choose(int n, int r) {
		return factorial(n) / factorial (r) / factorial(n - r);
	}
	
	public static int factorial(int a) {
		int out = 1;
		for (int x = a; x > 0; x--) {
			out *= x;
		}
		return out;
	}
}
