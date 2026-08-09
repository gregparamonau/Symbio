package GameObject.Objects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import Enemies.Enemy;
import GameObject.GameObject;
import GameObject.GameTemplate;
import LevelEdit.LevelEditor;
import Logic.Vector2;
import Logic.Collision.Bezier;
import Logic.Collision.Line;
import Logic.Collision.Polygon;
import Logic.Collision.Rectangle;
import Logic.Utilities.Utility;
import Main.Game;
import Main.Start;
import Player.Player;

public class BezierTerrain extends GameObject{
	public static int default_num = 3;
	protected static boolean default_start_full = false, default_render_padding = true;
	
	protected int num = 3;
	protected boolean start_full = false, render_padding = true;//, clockwise = true;
	
	public Bezier shape;
	
	Line intersect = new Line(new Vector2(0, 0), new Vector2(0, 0));
	
	protected static String texture_file = "/object_textures/mouth.png";
	
	public BezierTerrain(double num, double[] pnts, double object_handle, String start_full, String render_padding, String sprite, int id) {
		this.shape = new Bezier(num, pnts);
		this.num = (int)num;
		
		this.object_handle = (int)object_handle;
		
		this.start_full = (start_full.equals("true"));
		this.render_padding = (render_padding.equals("true"));
		
		this.sprite_name = sprite;
		
		this.vis_solid = true;
		
		this.id = id;
		
		this.terrain = true;
		
		this.assign_rect();
		
		this.start_nodes();
		
		this.nodes = this.shape.control_points;

	}
	
	public void start() {
		
		
		this.pol = new Polygon(this.shape.to_polygon(this.start_full));
		this.pol.get_bounds();
		this.pos.set(this.pol.bounds.pos);
		this.last_pos.set(this.pol.bounds.pos);
	}
	
	public Polygon get_pol() {
		Polygon out = new Polygon(this.shape.to_polygon(this.start_full));
		out.get_bounds();
		return out;
	}
	
	public static GameObject get_obj(String [] in, int id) {
		return new BezierTerrain(Double.parseDouble(in[1]), Utility.parse_array(Utility.sub_array(in, 2, in.length - 4)), Double.parseDouble(in[in.length - 4]), in[in.length - 3], in[in.length - 2], in[in.length - 1], id);
	}
	
	public void scale(double in) {
		this.pos.mult(in);;
		this.width *= in;
		this.height *= in;
		
		for (int x = 0; x<this.shape.control_points.length; x++) this.shape.control_points[x].mult(in);
		this.nodes = this.shape.control_points;
		
		this.shape.num_seg = 0;
		this.shape.generate_lines();
		this.shape.find_bounds();
	}
	
	
	//default_object(Vector2 loc)
			//update() [needed if the object does an action / changes sprite?]
			//collide_with(Rectangle in, boolean col_action)
			//collision_action()
			//displace_player(int direction)
			//4 momentum methods:
				//dash_jump
				//jump
				//dash_wall_jump
				//wall_jump
			//draw_object()
			//generate_sprite(GameObject[] objects, String in)
			//return_sprite_type(Vector2 in, GameObject[] objects)
			//return_sprite_array(String in)
			//toString()
			//give_class()
		
		//TODO: 
		//generate sprite
		//collision detection
		//print class
		//draw
	
	public boolean collide_with(Rectangle in, boolean col_action) {
		//TODO: rework collision code so that it always works, not just when 
		if (!Rectangle.intersect(this.shape.bounding_box, in)) return false;
		
		this.intersect = this.shape.intersect_rect(in);		
		return this.intersect != null;
		
	}
	
	public Polygon give_collider() {
		return new Polygon(this.shape.to_polygon(this.start_full));
	}
	
	public void move(int grid_size) {
		if (!LevelEditor.mouse_pressed) return;
		
		this.nodes = Vector2.move_arr(this.save_nodes, Vector2.sub(LevelEditor.mouse_pos, LevelEditor.click));
		this.clip_nodes(grid_size);
		
		this.pos = Vector2.mult(Vector2.add(this.nodes[0], this.nodes[2]), 0.5);
		
		this.shape.control_points = this.nodes;
	
	}
	
	public void move(Vector2 in) {
		for (int x = 0; x < this.shape.lines.length; x++) this.shape.lines[x].b.add(in);
		this.shape.lines[0].a.add(in);
		
		this.pos.add(in);
		this.shape.bounding_box.pos.add(in);
	}
	//check for death, if position of player inside the polygon, then you're dead
	//more or less check if pos • normal < 0, then you know you're inside...
	public Vector2 displace_entity(Rectangle in, int direction) {
		//if (this.object_handle == -1) return;
		if (!Rectangle.intersect(in, this.shape.bounding_box)) return Vector2.zero;
		
		System.out.println("PLAYER: " + (in instanceof Player));
		if (!(in instanceof Player)) System.out.println(((Enemy)in).id + " " + in);
		
		Line ln = this.shape.intersect_rect(in);
		
		if (ln == null) return Vector2.zero;
		
		Vector2 out = new Vector2();
		
		//if (!Rectangle.intersect(in, this.shape.bounding_box)) return;
		for (int i = 0; i < 1; i++) {
			Line l = this.shape.intersect_rect(in);
			if (in instanceof Enemy) System.out.println(in + " " + l);
			if (l == null) continue;
			
			out.add(Line.disp_rect(ln, in, direction));
		}
		
		Vector2 out_F = Vector2.add(out, Vector2.sub(this.pos, this.last_pos));
		System.out.println("OUT: " + out + " vel: " + Vector2.sub(this.pos, this.last_pos) + " OUTF: " + out_F + " ID: " + this.id);
		return out_F;//Vector2.add(out, Vector2.sub(this.pos, this.last_pos));
		

	}
	
	//TODO: 4 momentum methods
	
	
	public void draw_object(Graphics g, JPanel pane, double xin, double yin, String location) {
		if (location.equals("edit")) {
			
			//System.out.println("lines.length: " + this.shape.lines.length);
			//System.out.println("lines[0].a: " + this.shape.lines[0].a);
			//System.out.println("lines[0].b: " + this.shape.lines[0].b);
			
			
			g.setColor(Color.black);
			for (int x = 0; x<this.shape.lines.length; x++) {
				
				Vector2 cent = Vector2.add(this.shape.lines[x].a, this.shape.lines[x].b)._mult(0.5);
				Vector2 norm = Vector2.add(this.shape.lines[x].norm()._mult(5), cent);
				
				
				Vector2 conv_a = Vector2.converted_pos(this.shape.lines[x].a, pane, xin, yin, location);
				Vector2 conv_b = Vector2.converted_pos(this.shape.lines[x].b, pane, xin, yin, location);
				
				g.drawLine((int)conv_a.x, (int)conv_a.y, (int)conv_b.x, (int)conv_b.y);
				
				conv_a = Vector2.converted_pos(cent, pane, xin, yin, location);
				conv_b = Vector2.converted_pos(norm, pane, xin, yin, location);
				
				g.drawLine((int)conv_a.x, (int)conv_a.y, (int)conv_b.x, (int)conv_b.y);
				
			}
			
			g.setColor(new Color(45, 223, 255, 128));
			this.fill_shape_edit(g, pane, xin, yin);
			
			
			
			for (int x = 0; x<this.shape.control_points.length; x++) {
				if (x == 0) {
					this.shape.control_points[x].draw_node(g, pane, xin, yin, location, Color.green);
					continue;
				}
				if (x == this.shape.control_points.length - 1) {
					this.shape.control_points[x].draw_node(g, pane, xin, yin, location, Color.red);
					continue;
				}
				
				this.shape.control_points[x].draw_node(g, pane, xin, yin, location, Color.magenta);


			}
			
		}
		if (location.equals("game")) {
			this.draw_with_sprite(g, pane, xin, yin, this.sprite, location);
			
			if (!Game.debug_mode) return;
			
			this.shape.bounding_box.draw_border(g, pane, xin, yin, location);
			//this.draw_border(g, pane, xin, yin, location);
			
			//this.shape.test.draw_border(g, pane, xin, yin, location);
			
			for (int x = 0; x<this.shape.lines.length; x++) {
				g.setColor(Color.red);
				if (x == 0) g.setColor(Color.cyan);
				
				Vector2 cent = Vector2.add(this.shape.lines[x].a, this.shape.lines[x].b)._mult(0.5);
				Vector2 norm = Vector2.add(this.shape.lines[x].norm()._mult(5), cent);
				
				
				Vector2 conv_a = Vector2.converted_pos(this.shape.lines[x].a, pane, xin, yin, location);
				Vector2 conv_b = Vector2.converted_pos(this.shape.lines[x].b, pane, xin, yin, location);
				
				g.drawLine((int)conv_a.x, (int)conv_a.y, (int)conv_b.x, (int)conv_b.y);
				this.shape.lines[x].a.draw_node(g, pane, xin, yin, location, fill);
				
				conv_a = Vector2.converted_pos(cent, pane, xin, yin, location);
				conv_b = Vector2.converted_pos(norm, pane, xin, yin, location);
				
				g.drawLine((int)conv_a.x, (int)conv_a.y, (int)conv_b.x, (int)conv_b.y);
				
				
			}
		}
	}
	
	public void draw_border(Graphics g, JPanel in, double xin, double yin, String location) {
		
		for (int x = 0; x<this.shape.control_points.length; x++) {
			if (x == 0) {
				this.shape.control_points[x].draw_node(g, in, xin, yin, location, Color.green);
				continue;
			}
			if (x == this.shape.control_points.length - 1) {
				this.shape.control_points[x].draw_node(g, in, xin, yin, location, Color.red);
				continue;
			}
			
			this.shape.control_points[x].draw_node(g, in, xin, yin, location, Color.magenta);


		}
	}
	
	public String give_class() {
		return "bezier_terrain";
	}
	
	void assign_rect() {
		this.pos = this.shape.bounding_box.pos;
		this.last_pos = new Vector2(this.shape.bounding_box.pos);
		this.width = this.shape.bounding_box.width;
		this.height = this.shape.bounding_box.height;
	}
	
	public void start_nodes() {
		this.nodes = this.shape.control_points;
	}
	
	public void fill_shape(Graphics g, Rectangle bounds) {
		Vector2[] polygon = this.shape.to_polygon(this.start_full);
		
		for (int x = 0; x<polygon.length; x++) {
			Vector2 temp = polygon[x];
			
			polygon[x] = new Vector2(
						temp.x - bounds.pos.x + bounds.width / 2,
						bounds.height / 2 - temp.y + bounds.pos.y
					);
			//polygon[x] = (Vector2.add(Vector2.sub(temp, position), new Vector2(this.width / 2, this.height / 2)));
		}
		
		//polygon = Vector2.move_arr(polygon, Vector2.mult(position, -1));
		//polygon = Vector2.mult_vec_arr(polygon, -1);
		
		int[][] pnts = Vector2.to_double_arr_arr(polygon);
		
		g.fillPolygon(pnts[0], pnts[1], polygon.length);
	}
	
	public void fill_shape_edit(Graphics g, JPanel pane, double xin, double yin) {
	    Vector2[] polygon = this.shape.to_polygon(this.start_full);
	    
	    int[][] pnts = new int[2][polygon.length];
	    for (int x = 0; x < polygon.length; x++) {
	        Vector2 conv = Vector2.converted_pos(polygon[x], pane, xin, yin, "edit");
	        pnts[0][x] = (int)conv.x;
	        pnts[1][x] = (int)conv.y;
	    }
	    
	    g.fillPolygon(pnts[0], pnts[1], polygon.length);
	}


	
	public void draw_blank_object(Graphics g, Rectangle bounds) {
		this.fill_shape(g, bounds);
	}
	
	public boolean vis_intersect(Rectangle in) {
		if (this.sprite == null) return false;
		if (!Rectangle.intersect(this, in)) return false;
		
		Vector2 temp = new Vector2(this.pos.x - this.width / 2, this.pos.y + this.height / 2);
		
		Vector2 rel = Vector2.sub(temp, in.pos);
		rel.x *= -1;

		//System.out.println("REL: " + rel + " THIS: " + this); 
		return this.sprite.getRGB((int)Utility.clamp(rel.x, 0, this.sprite.getWidth() - 1), (int)Utility.clamp(rel.y, 0, this.sprite.getHeight() - 1)) != 0;
	}
	
	public double find_closest_dist(double[][] in, int x, int y) {
		double out = 10;
		
		for (int i = Math.max(0, x - 5); i < Math.min(in.length, x + 5); i++) {
			for (int j = 1; j<in[i].length - 1; j++) {
				double temp = Utility.dist(i, in[i][j], x, y);
				if (temp < out) {
					out = temp;
				}
			}
		}
		
		return out;
	}
	
	public static void write_image(String FILE, BufferedImage img) {
		
		System.out.println("WRITING IMAGE");
		try {
			File out = new File(FILE);
			ImageIO.write(img, "png", out);
		}catch(Exception e){
			e.printStackTrace();
			System.exit(0);
		}
	}
}
