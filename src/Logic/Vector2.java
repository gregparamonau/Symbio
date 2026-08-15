package Logic;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import Logic.Utilities.Utility;
import Rendering.Camera;

public class Vector2 {
	public double x, y;
	public static int radius = 3;
	public Color fill;
	
	public static final Vector2 up = new Vector2(0, 1);
	public static final Vector2 down = new Vector2(0, -1);
	public static final Vector2 left = new Vector2(-1, 0);
	public static final Vector2 right = new Vector2(1, 0);
	
	public static final Vector2 up_left = new Vector2(-0.7, 0.7);
	public static final Vector2 up_right = new Vector2(0.7, 0.7);
	public static final Vector2 down_left = new Vector2(-0.7, -0.7);
	public static final Vector2 down_right = new Vector2(0.7, -0.7);
	
	public static final Vector2 zero = new Vector2(0, 0);
	
	
	//organization
	//constructors & setters
	//add / sub / mult / div methods (instance & static)
	
	
	//bullets in symbio:
	
	//extra logic functions
	
	//constructors
	public Vector2() {
		this.x = 0;
		this.y = 0;
	}
	public Vector2(double a, double b) {
		this.x = a;
		this.y = b;
	}
	public Vector2(Vector2 in) {
		this.x = in.x;
		this.y = in.y;
	}
	//constructors same
	
	//setters
	public void set(Vector2 in) {
		this.x = in.x;
		this.y = in.y;
	}
	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}
	//set vector to zero
	public void zero() {
		this.x = 0;
		this.y = 0;
	}
	
	
	//add et al.
	public static Vector2 mult(Vector2 in, double a) {
		return new Vector2(in.x * a, in.y * a);
	}
	public static Vector2 add(Vector2 a, Vector2 b) {
		return new Vector2(a.x + b.x, a.y + b.y);
	}
	
	public static Vector2 sub(Vector2 a, Vector2 b) {
		return new Vector2(a.x - b.x, a.y - b.y);
	}
	public void add(Vector2 in) {
		this.x += in.x;
		this.y += in.y;
	}
	public void sub(Vector2 in) {
		this.x -= in.x;
		this.y -= in.y;
	}
	public void mult(double in) {
		this.x *= in;
		this.y *= in;
	}
	public Vector2 _mult(double in) {
		return new Vector2(this.x * in, this.y * in);
	}
	
	
	
	
	
	//utility methods
	public double l() {
		return Math.sqrt(this.x * this.x + this.y * this.y);
	}
	public Vector2 norm() {
		double l = this.l();
		
		if (l < 1e-6) return new Vector2(0, 0);
		return new Vector2(this.x / l, this.y / l);
	}
	public static Vector2 normalize(Vector2 in) {
		if (in.equals(Vector2.zero)) return in;
		return new Vector2(in.x / in.l(), in.y / in.l());
	}
	//normalized dot
	public static double ndot(Vector2 a, Vector2 b) {
		Vector2 temp_a = normalize(a), temp_b = normalize(b);
		return (temp_a.x * temp_b.x + temp_a.y * temp_b.y);
	}
	//normal dot product (non-normalized)
	public static double dot(Vector2 a, Vector2 b) {
		return a.x * b.x + a.y * b.y;
	}
	public static Vector2 invert_x(Vector2 in) {
		return new Vector2(-in.x, in.y);
	}
	public static Vector2 invert_y(Vector2 in) {
		return new Vector2(in.x, -in.y);
	}
	public static double dist(Vector2 a, Vector2 b) {
		return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y));
	}
	public Vector2 perp() {	
		
		if (this.l() < 1e-3) return new Vector2(0, 0);
		Vector2 temp = Vector2.mult(this, (double)1 / this.l());
		
		if (Double.isNaN(temp.x) || Double.isNaN((temp.y))) return new Vector2(0, 0);
		
		return new Vector2(temp.y, -temp.x);
	}
	/*public Vector2 perp() {
		return new Vector2(-this.y, this.x);
	}*/
	public void max_mag(Vector2 in) {
		this.x = Math.abs(in.x) > Math.abs(this.x) ? in.x : this.x;
		this.y = Math.abs(in.y) > Math.abs(this.y) ? in.y : this.y;
	}
	public static Vector2 scale_to_length(Vector2 in, double length) {
		double mult = length/in.l();
		return new Vector2(in.x * mult, in.y * mult);
	}
	public void scale_to_length(double length) {
		double l = this.l();
		if (l == 0) return;
		double mult = length / l;
		
		this.x *= mult;
		this.y *= mult;
	}
	public static Vector2[] move_arr(Vector2[] in, Vector2 add) {
		Vector2[] out = new Vector2[in.length];
		for (int x = 0; x<out.length; x++) out[x] = Vector2.add(in[x], add);
		return out;
	}
	public static Vector2 converted_pos(Vector2 pos, JPanel in, double xin, double yin, String location) {
		JPanel pane = in;
		
		//if (location.equals("edit")) pane = rescale(in, 1);
		//else if (location.equals("game")) ;
		pane = rescale(in, (double)1 / Camera.pixel_size);
		
		return new Vector2((int)Math.round(pos.x + pane.getWidth()/2 - xin), (int)Math.round(pane.getHeight()/2 - pos.y + yin));
	}
	public void draw_node(Graphics g, JPanel in, double xin, double yin, String location, Color fill) {
		g.setColor(fill);
		
		JPanel pane = in;
		pane = rescale(in, (double)1 / Camera.pixel_size);
		
		//(int)(this.pos.y + yin - pane.getHeight() / 2 - this.height / 2)
		if (location.equals("edit")) g.drawOval((int)(this.x + pane.getWidth()/2 - xin - radius), (int)(pane.getHeight() / 2 - this.y + yin - radius), radius * 2, radius * 2);
		else if (location.equals("game")) g.drawOval((int)(this.x + pane.getWidth()/2 - xin - radius), (int)(pane.getHeight() / 2 - this.y + yin - radius), radius * 2, radius * 2);
	}
	public void draw_vector(Graphics g, JPanel in, double xin, double yin, String location, Color fill, Vector2 start) {
		g.setColor(fill);
		
		Vector2 end = add(this, start);
		
		Vector2 conv_start = converted_pos(start, in, xin, yin, location);
		Vector2 conv_end = converted_pos(end, in, xin, yin, location);
		
		g.drawLine((int)conv_start.x, (int)conv_start.y, (int)conv_end.x, (int)conv_end.y);
	}
	public static double angle(Vector2 a, Vector2 b) {
		
		if (a.l() < 1e-3 || b.l() < 1e-3) return 0;
		
		double theta = Math.acos(clamp(Vector2.dot(a, b) / a.l() / b.l(), -1, 1));
		
		if (Math.acos(clamp(Vector2.dot(a.rotate(0.00000000001), b) / a.l() / b.l(), -1, 1)) > theta) {
			return -theta;
		}
		return theta;
	}
	public static Vector2 reflect(Vector2 in, Vector2 norm) {
		if (norm.l() < 1e-3) return in._mult(-1);
		return Vector2.sub(Vector2.mult(norm, 2 * Vector2.dot(in, norm)), in);
	}
	
	public Vector2 rotate(double theta) {
	    double cos = Math.cos(theta);
	    double sin = Math.sin(theta);
	    return new Vector2(this.x * cos - this.y * sin,
	                       this.x * sin + this.y * cos);
	}
	public static Vector2 swap_axis(Vector2 in) {
		return new Vector2(in.y, in.x);
	}
	public Vector2 round(int decimal_place) {
		return new Vector2(Utility.round(this.x, decimal_place), Utility.round(this.y, decimal_place));
	}
	public static double clamp(double in, double min, double max) {
		return Math.max(min, Math.min(in, max));
	}
	public void clip_node(int grid_size) {
		this.x = (Math.round(this.x / grid_size)) * grid_size;
		this.y = (Math.round(this.y / grid_size)) * grid_size;
	}
	//void versions
	public static void add_to_vec_arr(Vector2[] in, Vector2 add) {		
		Vector2[] out = new Vector2[in.length + 1];
		
		for (int x = 0; x<in.length; x++) {
			out[x] = in[x];
		}
		
		out[out.length - 1] = add;
		
		in = out;		
	}
	public static void remove_from_vec_arr(Vector2[] in, int index) {
		Vector2[] out = new Vector2[in.length - 1];
		
		for (int x = 0; x<out.length; x++) {
			out[x] = in[x < index ? x : x + 1];
		}
		
		in = out;
	}
	
	//vector[] versions
	public static Vector2[] add_to_arr(Vector2[] in, Vector2 add) {		
		Vector2[] out = new Vector2[in.length + 1];
		
		for (int x = 0; x<in.length; x++) {
			out[x] = in[x];
		}
		
		out[out.length - 1] = add;
		
		return out;
	}
	public static Vector2[] merge_arr(Vector2[] a, Vector2[] b) {
		Vector2[] out = new Vector2[a.length + b.length];
		
		for (int x = 0; x< out.length; x++) {
			out[x] = (x < a.length ? a[x] : b[x - a.length]);
		}
		return out;
	}
	public static Vector2[] remove_from_arr(Vector2[] in, int index) {
		Vector2[] out = new Vector2[in.length - 1];
		
		for (int x = 0; x<out.length; x++) {
			out[x] = in[x < index ? x : x + 1];
		}
		
		return out;
	}
	
	public static Vector2[] mult_vec_arr(Vector2[] in, double mult) {
		Vector2[] out = new Vector2[in.length];
		for (int x = 0; x<out.length; x++) out[x] = Vector2.mult(in[x], mult);
		
		return out;
	}
	
	public static int[][] to_double_arr_arr(Vector2[] in) {
		int[][] out = new int[2][in.length];
		
		for (int x = 0; x<in.length; x++) {
			out[0][x] = (int)in[x].x;
			out[1][x] = (int)in[x].y;
		}
		
		return out;
	}
	
	public static double[] to_double_arr(Vector2[] in, int[] fix) {
		double[] out = new double[in.length * 3];
		
		for (int x =0; x<in.length; x++) {
			out[3 * x] = in[x].x;
			out[3 * x + 1] = in[x].y;
			out[3 * x + 2] = fix[x];
		}
		return out;
	}
	public static Vector2 ave(Vector2[] in) {
		Vector2 out = new Vector2();
		for (int x = 0; x<in.length; x++) out.add(in[x]);
		
		return out._mult(1.0 / in.length);
	}
	
	public static JPanel rescale(JPanel in, double scale) {
		JPanel out = new JPanel();
		out.setSize((int)(in.getWidth() * scale), (int)(in.getHeight() * scale));
		return out;
	}
	public String toString() {
		return "x: " + this.x + " y: " + this.y;
	}
}
