package GameObject.EditorObjects;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import GameObject.GameObject;
import GameObject.Objects.Acid;
import LevelEdit.LevelEditor;
import Logic.Vector2;

public class AcidEdit extends Acid{
	
	public AcidEdit() {}
	public AcidEdit(double a, double b, double c, double d, double e, String sprite, int id) {
		super(a, b, c, d, e, sprite, id);
	}
	
	public void draw_object(Graphics g, JPanel pane, double xin, double yin, String location) {
		this.draw(g, pane, xin, yin, location, true);
	}
	
	public static GameObject get_obj(String[] in, int id) {
		return new AcidEdit(Double.parseDouble(in[1]), Double.parseDouble(in[2]), Double.parseDouble(in[3]), Double.parseDouble(in[4]), Double.parseDouble(in[5]), in[6], id);
	}
	
	public static GameObject default_object(Vector2 loc) {
		return new AcidEdit(loc.x, loc.y, LevelEditor.grid_size, LevelEditor.grid_size, -1, sprite_name_default, 0);
	}
	
	public String toString() {
		return "Acid " + this.pos.x + " " + this.pos.y + " " + this.width + " " + this.height + " " + this.object_handle + " " + sprite_name_default + " ";
	}
	public String give_class() {
		return "acid";
	}
}
