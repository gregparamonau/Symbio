package GameObject;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.reflect.Method;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import LevelEdit.LevelEditor;
import LevelEdit.RoomEditor;
import Logic.Vector2;
import Logic.Collision.Polygon;
import Logic.Collision.Rectangle;
import Logic.Softbody.SoftBody;
import Logic.Utilities.Utility;
import Main.Game;
import Main.Start;
import Rendering.Camera;
import UI.OptionPane;

public class GameObject extends Rectangle{
	public Polygon pol;
	public SoftBody sb;
	public int id;
	public boolean solid = true;
	public boolean vis_solid = true;
	public boolean sliceable = false;
	public boolean terrain = false;
	
	//public Color fill = Color.gray;
	
	public Vector2 last_pos = Vector2.zero;
	
	public int object_handle = -1;
	
	public static String sprite_name_default = "/object_textures/mouth.png";
	
	public static final double dash_jump_mom_mult = 2.5, dash_jump_mom_y_mult = 0.1;
	public static final double dash_wall_jump_mom_mult = 1.5, dash_wall_jump_x_mom_mult = 0.75;
	
	public String sprite_name = sprite_name_default;

	public boolean displaceable = true;
	
	public GameObject() {}
	
	public GameObject(double a, double b, double c, double d, double e, String sprite, int id) {
		this.pos = new Vector2(a, b);
		
		//System.out.println("CREATING OBJECT: " + c + " " + d);
		this.width = c;
		this.height = d;
		this.start_nodes();
		
		this.object_handle = (int)e;
		
		this.sprite_name = sprite;
		
		this.id = id;
		
		this.terrain = true;
	}
	
	public void start() {
		this.pol = this.get_pol();
	}
	
	public static GameObject get_obj(String[] in, int id) {
		return new GameObject(Double.parseDouble(in[1]), Double.parseDouble(in[2]), Double.parseDouble(in[3]), Double.parseDouble(in[4]), Double.parseDouble(in[5]), in[6], id);
	}
	
	public static GameObject create_game_object(String[] in, int id) {
		
		try {
			String class_name = in[0].equals("GameObject") ? "GameObject.GameObject" : "GameObject.Objects." + in[0];
			Class c = Class.forName(class_name);
			
			Method meth = c.getMethod("get_obj", String[].class, int.class);
			
			return (GameObject)meth.invoke(null, (Object)in, id);
			
		}catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR: GameObject 83");
			System.exit(0);
		}
		
		return null;
	}
	
	
	//needed methods in every class for 'game'
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
	
	//needed methods for 'edit'
	//public void update_nodes(Vector2 in, int place, int grid_size)
	//public void move(int grid_size)
	//public void update_dimensions()
	//public void start_nodes()
	//public void scale(double in)
	
	
	public void update() {
		if (this.object_handle == -1) return;
		
		this.last_pos = new Vector2(this.pos);
		
		this.move(Vector2.sub(Game.current_room.objects[this.object_handle].pos, Game.current_room.objects[this.object_handle].last_pos));
		//write code here
		
		if (Game.player.object_intersect_id == this.id) {
			Vector2 temp = new Vector2(Game.player.pos);
			Game.player.pos.add(Vector2.sub(this.pos, this.last_pos));
			
			Game.player.collider.displace(0);
			
		}
	}
	//whether you are physically colliding with the platform for movement & physics
	public boolean collide_with(Rectangle in, boolean col_action) {
		
		return Rectangle.intersect(this, in);
	}
	public void collision_action() {
		//write code here
	}
	public Vector2 displace_entity(Rectangle in, int direction) {
		//direction: 0 = any, 1 = vertical, 2 = horizontal
		if (!this.solid || !Rectangle.intersect(this, in) || !this.displaceable) return Vector2.zero;
		
		double outx = 0, outy = 0;
		Rectangle temp = Rectangle.intersect_area(this, in);
		if (temp.height <= temp.width) {
			outy += Utility.sign(in.pos.y - this.pos.y) * (temp.height + 1);
		}
		else if (temp.height > temp.width) {
			outx += Utility.sign(in.pos.x - this.pos.x) * (temp.width + 1);
		}
		
		return new Vector2(outx, outy);
	}
	
	//4 momentum methods??? or 1 single one
	public void give_jump_momentum() {
		//nothing here for normal object
	}
	
	public Polygon give_collider() {
		return new Polygon(this.to_polygon());
	}
	
	public void scale(double in) {
		this.pos = Vector2.mult(this.pos, in);
		this.width *= in;
		this.height *= in;
		
		this.start_nodes();
		if (this.nodes != null) for (int x = 0; x<this.nodes.length; x++) this.nodes[x] = Vector2.mult(this.nodes[x], in);
	}
	public void draw_object(Graphics g, JPanel pane, double xin, double yin, String location) {
		if (location.equals("game")) this.draw_with_sprite(g, pane, xin, yin, this.sprite, location);
		if (location.equals("edit")) this.draw(g, pane, xin, yin, location, true);
	}
	public void draw_blank_object(Graphics g, Rectangle bounds) {
		Vector2 temp_pos = new Vector2(
					this.pos.x - bounds.pos.x + bounds.width / 2,
					bounds.height / 2 - this.pos.y + bounds.pos.y
				);
		
		g.fillRect((int)(temp_pos.x - this.width / 2), (int)(temp_pos.y - this.height / 2), (int)this.width, (int)this.height);
	}
	public void generate_sprite(GameObject[] objects, String in) {
		BufferedImage[] sprites_temp = return_sprite_array(in);
		
		this.sprite = new BufferedImage((int)this.width, (int)this.height, BufferedImage.TYPE_INT_ARGB);
		Graphics g = this.sprite.getGraphics();
		
		for (int y = 0; y < this.height / Camera.tile_size; y++) {
			for (int x = 0; x<this.width / Camera.tile_size; x++) {
				g.drawImage(sprites_temp[this.return_sprite_type(new Vector2(this.pos.x - this.width/2 + Camera.tile_size * (x + 0.5), this.pos.y + this.height/2 - Camera.tile_size * (y + 0.5)), objects)], x * Camera.tile_size, y * Camera.tile_size, null);
			}
		}
	}
	public int return_sprite_type(Vector2 in, GameObject[] objects) {
		//where there are tiles
		boolean l = objects_intersect(objects, new Rectangle(in.x - Camera.tile_size, in.y, 0, 0));
		boolean r = objects_intersect(objects, new Rectangle(in.x + Camera.tile_size, in.y, 0, 0));
		boolean u = objects_intersect(objects, new Rectangle(in.x, in.y + Camera.tile_size, 0, 0));
		boolean d = objects_intersect(objects, new Rectangle(in.x, in.y - Camera.tile_size, 0, 0));
		
		boolean ul = objects_intersect(objects, new Rectangle(in.x - Camera.tile_size, in.y + Camera.tile_size, 0, 0));
		boolean ur = objects_intersect(objects, new Rectangle(in.x + Camera.tile_size, in.y + Camera.tile_size, 0, 0));
		boolean dl = objects_intersect(objects, new Rectangle(in.x - Camera.tile_size, in.y - Camera.tile_size, 0, 0));
		boolean dr = objects_intersect(objects, new Rectangle(in.x + Camera.tile_size, in.y - Camera.tile_size, 0, 0));
				
		//one line with 2 corners
		if (!l && r && u && d && !ur && !dr) return 35;
		if (l && r && !u && d && !dl && !dr) return 36;
		if (l && r && u && !d && !ul && !ur) return 42;
		if (l && !r && u && d && !ul && !dl) return 43;
		
		//one line with 1 corner (left)
		if (!l && r && u && d && ur && !dr) return 37;
		if (l && r && !u && d && !dl && dr) return 38;
		if (l && r && u && !d && ul && !ur) return 44;
		if (l && !r && u && d && !ul && dl) return 45;
		//one line with 1 corner (right)
		if (l && r && !u && d && dl && !dr) return 39;
		if (l && !r && u && d && ul && !dl) return 40;
		if (!l && r && u && d && !ur && dr) return 46;
		if (l && r && u && !d && !ul && ur) return 47;
		
		//single wide ones
		
		//vertical line
		if (!l && !r && !u && d) return 3;
		if (!l && !r && u && d) return 10;
		if (!l && !r && u && !d) return 17;
		
		//horizontal line
		if (!l && r && !u && !d) return 4;
		if (l && r && !u && !d) return 5;
		if (l && !r && !u && !d) return 6;
		
		//4 single corners
		if (!l && r && !u && d && !dr) return 11;
		if (l && !r && !u && d && !dl) return 12;
		if (!l && r && u && !d && !ur) return 18;
		if (l && !r && u && !d && !ul) return 19;
		
		//one single block
		if (!l && !r && !u && !d) return 13;
		
		
		//all the odd corners
		
		//4 only corners
		if (l && r && u && d && ul && ur && dl && !dr) return 21;
		if (l && r && u && d && ul && ur && !dl && dr) return 22;
		if (l && r && u && d && ul && !ur && dl && dr) return 28;
		if (l && r && u && d && !ul && ur && dl && dr) return 29;
		
		//2 adjacent corner pieces
		if (l && r && u && d && ul && !ur && dl && !dr) return 23;
		if (l && r && u && d && ul && ur && !dl && !dr) return 24;
		if (l && r && u && d && !ul && !ur && dl && dr) return 30;
		if (l && r && u && d && !ul && ur && !dl && dr) return 31;
		
		//2 diagonal corner pieces
		if (l && r && u && d && !ul && ur && dl && !dr) return 25;
		if (l && r && u && d && ul && !ur && !dl && dr) return 32;
		
		//3 corner pieces
		if (l && r && u && d && ul && !ur && !dl && !dr) return 26;
		if (l && r && u && d && !ul && ur && !dl && !dr) return 27;
		if (l && r && u && d && !ul && !ur && dl && !dr) return 33;
		if (l && r && u && d && !ul && !ur && !dl && dr) return 34;
		
		//4 corner piece
		if (l && r && u && d && !ul && !ur && !dl && !dr) return 20;
		
		
		//9 converntional pieces
		if (!l && r && !u && d) return 0;
		if (l && r && !u && d) return 1;
		if (l && !r && !u && d) return 2;
		if (!l && r && u && d) return 7;
		if (l && !r && u && d) return 9;
		if (!l && r && u && !d) return 14;
		if (l && r && u && !d) return 15;
		if (l && !r && u && !d) return 16;
		return 8;
		
	}
	//TODO: REMAKE THIS FUCNTION SO THAT ITS MORE ACCURATE TO WHATS RENDERED
	public boolean vis_intersect(Rectangle in) {
		return Rectangle.intersect(this, in);
	}
	
	public boolean objects_intersect(GameObject[] objects, Rectangle pos) {
		for (int x = 0; x<objects.length; x++) {
			if ((!objects[x].vis_solid || objects[x].object_handle != -1) && x != this.id) continue;
			if (objects[x].vis_intersect(pos)) {
				return true;
			}
		}
		return false;
	}
	public BufferedImage[] return_sprite_array(String in) {
		
		//System.out.println(in);
		BufferedImage[] out = new BufferedImage[49];
		try {
			BufferedImage source = ImageIO.read(getClass().getResource(in));
			for (int x = 0; x<out.length; x++) {
				out[x] = source.getSubimage((x % 7) * Camera.tile_size, (x / 7) * Camera.tile_size, Camera.tile_size, Camera.tile_size);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out;
	}
	public void move(Vector2 in) {
		for (int x = 0; x<this.nodes.length; x++) this.nodes[x].add(in);
		this.pos.add(in);
	}
	
	public static GameObject[] add_to_arr(GameObject[] in, GameObject add) {		
		GameObject[] out = new GameObject[in.length + 1];
		
		for (int x = 0; x<in.length; x++) {
			out[x] = in[x];
		}
		
		out[out.length - 1] = add;
		
		return out;
	}
	
	public static GameObject[] merge_arrays(GameObject[] a, GameObject[] b) {
		GameObject[] out = new GameObject[a.length + b.length];
		
		for (int x = 0; x<out.length; x++) {
			out[x] = (x < a.length ? a[x] : b[x - a.length]);
		}
		
		return out;
	}
	
	public boolean properties(int node_sel) {
		
		Start.o_pane = new OptionPane(LevelEditor.click_save, new String[] {"attach to object "}, new Runnable[] {
				() -> {
					//TODO: make this method & functionalioty to input text
					Start.o_pane = new OptionPane(LevelEditor.click_save, "object handle", () -> {
						int input = Integer.parseInt(Start.o_pane.input_text);
						if (input >= 0 && input < RoomEditor.room.objects.length) {
						    if (RoomEditor.room.objects[input].give_class().equals("mover")) {
						        RoomEditor.room.objects[RoomEditor.last_object_selected].object_handle = input;
						    } else {
						    	//System.out.println("	SET ID -1");
						        RoomEditor.room.objects[RoomEditor.last_object_selected].object_handle = -1;
						    }
						}
						/*int index = Integer.parseInt(Start.o_pane.input_text);
						if (index >= 0 && index < RoomEditor.room.objects.length) {
							RoomEditor.room.objects[RoomEditor.last_object_selected].object_handle = index;
						}
						if (RoomEditor.room.objects[RoomEditor.last_object_selected])
						
						RoomEditor.room.objects[RoomEditor.last_object_selected].object_handle = Integer.parseInt(Start.o_pane.input_text);
						if (!RoomEditor.room.objects[RoomEditor.room.objects[RoomEditor.last_object_selected].object_handle].give_class().equals("mover")) RoomEditor.room.objects[RoomEditor.last_object_selected].object_handle = -1;
						System.out.println("OBJECT PROPERTIES");*/
						
						//System.out.println(RoomEditor.room.objects[RoomEditor.last_object_selected].object_handle);
					});
					
					//this.object_handle = Integer.parseInt(JOptionPane.showInputDialog(Start.frame, "Attach to Object"));
					//if (!RoomEditor.room.objects[this.object_handle].give_class().equals("mover")) this.object_handle = -1;
					
					//Start.o_pane.done();
					//return;
				}
		});
		return false;
	}
	
	public String give_class() {
		return "object";
	}
	public String toString() {
		return "GameObject " + this.pos.x + " " + this.pos.y + " " + this.width + " " + this.height + " " + this.object_handle + " " + sprite_name_default + " ";
	}
}
