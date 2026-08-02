package GameObject;

import java.awt.Color;
import java.awt.Graphics;
import java.lang.reflect.Method;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import GameObject.Objects.Acid;
import GameObject.Objects.BezierTerrain;
import GameObject.Objects.Bouncer;
import GameObject.Objects.Circle;
import GameObject.Objects.EnemySpawner;
import GameObject.Objects.Mover;
import GameObject.Objects.OneWay;
import GameObject.Objects.Slope;
import GameObject.Objects.Sprite;
import LevelEdit.LevelEditor;
import LevelEdit.RoomEditor;
import Logic.Vector2;
import Logic.Collision.Rectangle;
import Logic.Utilities.Utility;
import Main.Start;
import UI.OptionPane;

public class EditObject extends GameObject{
	public int id;
	public boolean solid = true;
	public boolean vis_solid = true;
	public boolean sliceable = false;
	
	public int object_handle = -1;
	
	public static String sprite_name_default = "/object_textures/mouth.png";
	
	
	public String sprite_name = sprite_name_default;
	public boolean displaceable = true;
	
	
	public EditObject() {}
	
	public EditObject(double a, double b, double c, double d, double e, String sprite, int id) {
		super(a, b, c, d, e, sprite, id);
		/*this.pos = new Vector2(a, b);
		this.width = c;
		this.height = d;
		this.start_nodes();
		
		this.object_handle = (int)e;
		
		this.sprite_name = sprite;
		
		this.id = id;*/
	}
	
	public static GameObject get_obj(String[] in, int id) {
		return new EditObject(Double.parseDouble(in[1]), Double.parseDouble(in[2]), Double.parseDouble(in[3]), Double.parseDouble(in[4]), Double.parseDouble(in[5]), in[6], id);
	}
	
	public static GameObject create_game_object(String[] in, int id) {
		try {
			String class_name = in[0].equals("GameObject") ? "GameObject.EditObject" : "GameObject.EditorObjects." + in[0] + "Edit";
			Class c = Class.forName(class_name);
			
			Method meth = c.getMethod("get_obj", String[].class, int.class);
			
			return (GameObject)meth.invoke(null, (Object)in, id);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR: EditObject 73");
			System.exit(0);
		}
		
		return null;
	}
	
	public static GameObject create_default_game_object(String type, Vector2 loc) {
		
		try {
			String class_name = type.equals("GameObject") ? "GameObject.EditObject" : "GameObject.EditorObjects." + type + "Edit"; 
			Class c = Class.forName(class_name);
			
			Method meth = c.getMethod("default_object", Vector2.class);
			
			return (GameObject)meth.invoke(null, loc);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR: EditObject 91");
			System.exit(0);
		}
		
		return null;
	}
	
	public void draw_object(Graphics g, JPanel pane, double xin, double yin, String location) {
		this.draw(g, pane, xin, yin, location, true);
		this.draw_nodes(g, pane, xin, yin, location);
	}
	public static GameObject default_object(Vector2 loc) {
		return new EditObject(loc.x, loc.y, LevelEditor.grid_size, LevelEditor.grid_size, -1, sprite_name_default, 0);
	}
	
}
