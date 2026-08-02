package GameObject.EditorObjects;

import GameObject.GameObject;
import GameObject.Objects.SBTerrain;
import Logic.Vector2;

public class SBTerrainEdit extends SBTerrain{
	
	public SBTerrainEdit(double x, double y, double r, int id) {
		super(x, y, r, id);
	}

	public static GameObject get_obj(String[] in, int id) {
		return new SBTerrainEdit(Double.parseDouble(in[1]), Double.parseDouble(in[2]), Double.parseDouble(in[3]), id);
	}
	
	public static GameObject default_object(Vector2 loc) {
		System.out.println("DEFAULT_OBJECT SBTERRAIN: " + new SBTerrain(loc.x, loc.y, 20, 0));
		//System.exit(0);
		return new SBTerrainEdit(loc.x, loc.y, 20, 0);
	}
	
	public void update_nodes(Vector2 in, int place, int grid_size) {
		this.nodes[place] = in;
		
		
		this.clip_nodes(grid_size);
		this.update_dimensions();
	}
	
	public void update_dimensions() {
		this.pos = new Vector2(this.nodes[0]);
		this.radius = Vector2.dist(this.pos, this.nodes[1]);
	}
	
}
