package GameObject.Objects;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import GameObject.GameObject;
import LevelEdit.LevelEditor;
import Logic.Vector2;
import Logic.Collision.Polygon;
import Logic.Collision.Rectangle;
import Logic.Softbody.SoftBody;
import Main.Game;
import Player.Player;
import Rendering.Camera;

public class SBTerrain extends GameObject{
	public  double radius;
	
	public SBTerrain() {}
	
	public SBTerrain(double x, double y, double r, int id) {
		this.pos = new Vector2(x, y);
		this.radius = r;
		
		this.id = id;
		
		this.nodes = new Vector2[] {
				new Vector2(this.pos),
				new Vector2(this.pos.x + radius, this.pos.y)
		};
		
		this.sb = new SoftBody(new Vector2(x, y), r, id);
		
		this.start_nodes();
		
		this.width = 2 * this.radius; this.height = 2 * this.radius;
		
		this.solid = false;
		
		
		
	}
	
	
	public static GameObject get_obj(String [] in, int id) {
		return new SBTerrain(Double.parseDouble(in[1]), Double.parseDouble(in[2]), Double.parseDouble(in[3]), id);
	}
	
	public void start() {		
		this.sb = new SoftBody(new Vector2(this.pos), this.radius, this.id);
		this.pol = null;
	}
	
	public void update() {
		this.sb.pol.get_bounds();
		this.sb.update();
		
	}
	
	public void generate_sprite(GameObject[] objects, String in) {
		return;
	}
	
	public boolean collide_with(Rectangle in, boolean col_action) {
		//System.out.println("COLLIDED: " + in.get_pol().intersect(this.sb.pol) + " ID: " + Game.player.object_intersect_id + " " + this.id);
		return in.get_pol().intersect(this.sb.pol);
	}
	
	public void collision_action() {
		this.sb.displace_player();
	}
	
	public Vector2 displace_entity(Rectangle in, int direction) {
		
		//vector2 to move player by
		return Vector2.zero;
	}
	
	//public void give_jump_momentum() {}
	
	public void start_nodes() {
		if (!start) return;
		Vector2[] temp = {
				new Vector2(this.pos), 
				new Vector2(this.pos.x + this.radius, this.pos.y)
		};
		//System.out.println("rect " + this.nodes.length);
		this.nodes = temp;
		start = false;
	}
	
	public void move(int grid_size) {
		if (!LevelEditor.mouse_pressed) return;
		
		this.nodes = Vector2.move_arr(this.save_nodes, Vector2.sub(LevelEditor.mouse_pos, LevelEditor.click));
		this.clip_nodes(grid_size);
		
		this.pos = new Vector2(this.nodes[0]);
	
	}
	
	public void scale(double in) {
		this.pos.mult(in);
		this.nodes[0].mult(in);
		this.nodes[1].mult(in);
		this.radius *= in;
		this.width *= in;
		this.height *= in;
	}
	
	public void draw_object(Graphics g, JPanel pane, double xin, double yin, String location) {
		if (location.equals("game")) this.sb.draw(g, pane, xin, yin, location);
		if (location.equals("edit")) {
			Vector2 conv = Vector2.converted_pos(this.pos, pane, xin, yin, location);
			g.setColor(new Color(255, 0, 0, 64));
			g.fillOval((int)(conv.x - this.radius), (int)(conv.y - this.radius), (int)(2 * this.radius), (int)(2 * this.radius));
			
			this.pos.draw_node(g, pane, xin, yin, location, Color.red);
			this.nodes[1].draw_node(g, pane, xin, yin, location, Color.green);
		}
	}
	
	//public void move(Vector2 in) {}
	
	public boolean properties(int node_sel) {
		return false;
	}
	
	public String toString() {
		return "SBTerrain " + this.pos.x + " " + this.pos.y + " " + this.radius + " ";
	}
	
}
