package GameObject.EditorObjects;

import javax.swing.JList;
import javax.swing.JOptionPane;

import GameObject.GameObject;
import GameObject.Objects.BezierTerrain;
import LevelEdit.LevelEditor;
import LevelEdit.RoomEditor;
import Logic.Vector2;
import Logic.Collision.Bezier;
import Logic.Collision.Rectangle;
import Logic.Utilities.Utility;
import Main.Start;
import UI.OptionPane;

public class BezierTerrainEdit extends BezierTerrain{
	
	public BezierTerrainEdit(double num, double[] pnts, double object_handle, String start_full, String render_padding, String sprite, int id) {
		super(num, pnts, object_handle, start_full, render_padding, sprite, id);
	}
	
	public void add_fix() {
		int[] out = new int[this.shape.fixed.length + 1];
		for (int x = 0; x<out.length - 1; x++) out[x] = this.shape.fixed[x];
		out[this.shape.fixed.length] = 1;
		out[this.shape.fixed.length - 1] = 0;
		
		this.shape.fixed = out;
	}
	
	public void del_fix(int ind) {
		int[] out = new int[this.shape.fixed.length - 1];
		for (int x = 0; x<out.length; x++) out[x] = this.shape.fixed[x >= ind ? x + 1 : x];
		
		if (ind == 0) out[0] = 1;
		if (ind == this.shape.fixed.length - 1) out [out.length - 1] = 1;
		
		this.shape.fixed = out;
	}
	
	public boolean properties(int node_sel) {
		System.out.println("PROPS BEZIER");
		//System.exit(0);

		String[] options = new String[] {"add node", "fix node", "delete node", "fill right", "render padding", "object handle"};
		
		Start.o_pane = new OptionPane(LevelEditor.click_save, options, new Runnable[] {
				() -> {
					System.out.println("ADDING POINT: " + node_sel + " CLICK: " + LevelEditor.click);
					
					this.add_fix();
					this.shape = new Bezier(this.shape.control_points.length + 1, Vector2.to_double_arr(Vector2.add_to_arr(this.shape.control_points, LevelEditor.click), this.shape.fixed));
					//Vector2.add_to_vec_arr(this.shape.control_points, LevelEditor.click);
					this.nodes = this.shape.control_points;
					
					this.num++;
					
					Start.o_pane.done();
					return;
				},
				() -> {
					System.out.println("FIXING POINT: " + node_sel + " CLICK: " + LevelEditor.click);
					this.shape.fixed[node_sel] = this.shape.fixed[node_sel] == 0 ? 1 : 0;
					
					Start.o_pane.done();
					return;
				},
				() -> {
					System.out.println("REMOVING POINT: " + node_sel + " CLICK: " + LevelEditor.click);
					if (node_sel == -1) {
						//return false;
					}
					
					this.del_fix(node_sel);
					this.shape = new Bezier(this.shape.control_points.length - 1, Vector2.to_double_arr(Vector2.remove_from_arr(this.shape.control_points, node_sel), this.shape.fixed));

					//Vector2.remove_from_vec_arr(this.shape.control_points, RoomEditor.object_select[1]);
					this.nodes = this.shape.control_points;
					
					this.num--;
					Start.o_pane.done();
					return;
				},
				() -> {
					Start.o_pane = new OptionPane(LevelEditor.click_save, "fill right", () -> {
						boolean input = Start.o_pane.input_text.equals("1");
						//true == 1
						
						default_start_full = input;
						this.start_full = input;
						
					});
					
				},
				() -> {
					Start.o_pane = new OptionPane(LevelEditor.click_save, "render padding", () -> {
						boolean input = Start.o_pane.input_text.equals("true");
						
						default_render_padding = input;
						this.render_padding = input;
					});
				},
				() -> {
					
					Start.o_pane = new OptionPane(LevelEditor.click_save, "object handle", () -> {
						int input = Integer.parseInt(Start.o_pane.input_text);
						if (input >= 0 && input < RoomEditor.room.objects.length) {
						    if (RoomEditor.room.objects[input].give_class().equals("mover")) {
						        RoomEditor.room.objects[RoomEditor.last_object_selected].object_handle = input;
						    } else {
						    	System.out.println("	SET ID -1");
						        RoomEditor.room.objects[RoomEditor.last_object_selected].object_handle = -1;
						    }
						}
						else RoomEditor.room.objects[RoomEditor.last_object_selected].object_handle = -1;
						
						System.out.println(RoomEditor.room.objects[RoomEditor.last_object_selected].object_handle);
					});
				}
		});
		
		return false;
		
		/*JList mods = new JList(new String[] {"Add Node", "Delete Node", "Start Full", "Object Handle"});
		
		JOptionPane.showMessageDialog(Start.frame, mods, "Select Modification", JOptionPane.PLAIN_MESSAGE);
		
		if (mods.getSelectedIndex() == 0) {
			System.out.println("ADDING POINT: " + RoomEditor.object_select[1] + " CLICK: " + LevelEditor.click);
			
			
			this.shape = new Bezier(this.shape.control_points.length + 1, Vector2.to_double_arr(Vector2.add_to_arr(this.shape.control_points, LevelEditor.click)));
			//Vector2.add_to_vec_arr(this.shape.control_points, LevelEditor.click);
			this.nodes = this.shape.control_points;
			
			this.num++;
		}
		if (mods.getSelectedIndex() == 1) {
			System.out.println("REMOVING POINT: " + RoomEditor.object_select[1] + " CLICK: " + LevelEditor.click);
			if (RoomEditor.object_select[1] == -1) return false;
			
			this.shape = new Bezier(this.shape.control_points.length - 1, Vector2.to_double_arr(Vector2.remove_from_arr(this.shape.control_points, RoomEditor.object_select[1])));

			//Vector2.remove_from_vec_arr(this.shape.control_points, RoomEditor.object_select[1]);
			this.nodes = this.shape.control_points;
			
			this.num--;
		}
		if (mods.getSelectedIndex() == 2) {
			JList sel = new JList(new String[] {"true", "false"});
			
			default_start_full = (sel.getSelectedIndex() == 0);
		}
		if (mods.getSelectedIndex() == 3) {
			this.object_handle = Integer.parseInt(JOptionPane.showInputDialog(Start.frame, "Attach to Object"));
			if (!RoomEditor.room.objects[this.object_handle].give_class().equals("mover")) this.object_handle = -1;
		}
		
		return false;*/
	}
	
	public static GameObject get_obj(String[] in, int id) {
		return new BezierTerrainEdit(Double.parseDouble(in[1]), Utility.parse_array(Utility.sub_array(in, 2, in.length - 4)), Double.parseDouble(in[in.length - 4]), in[in.length - 3], in[in.length - 2], in[in.length - 1], id);
	}
	
	public static GameObject default_object(Vector2 loc) {
		
		double[] vec = new double[3 * default_num];
		
		for (int x = 0; x<default_num; x++) {
			vec[3 * x] = loc.x + LevelEditor.grid_size + LevelEditor.grid_size * x;
			vec[3 * x + 1] = loc.y;
			vec[3 * x + 2] = (x == 0 || x == default_num - 1) ? 1 : 0;
		}
		
		return new BezierTerrainEdit(default_num, vec, -1, (default_start_full ? "true" : "false"), (default_render_padding ? "true" : "false"), texture_file, 0);
	}
	
	
	
	public void update_nodes(Vector2 in, int place, int grid_size) {
		this.nodes[place] = in;
		this.clip_nodes(grid_size);
		
		this.shape.control_points[place] = this.nodes[place];
		
		this.update_dimensions();
	}
	public void update_dimensions() {
		
		this.shape.generate_lines();
		this.shape.find_bounds();
		
		this.pos = this.shape.bounding_box.pos;
		
		this.width = this.shape.bounding_box.width;
		this.height = this.shape.bounding_box.height;
	}
	public String toString() {
		String out = "BezierTerrain " + this.num + " ";
		
		for (int x = 0; x<num; x++) {
			out += this.nodes[x].x + " " + this.nodes[x].y + " " + this.shape.fixed[x] + " ";
		}
		
		out += this.object_handle + " " + (this.start_full ? "true " : "false ") + (this.render_padding ? "true " : "false ") + this.sprite_name + " ";
		//TODO: determine other attributes to include
		return out;
	}
}
