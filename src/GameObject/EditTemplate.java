package GameObject;

import Logic.Vector2;
import Logic.Collision.Rectangle;

public abstract class EditTemplate extends Rectangle{
	
	public static GameObject get_obj(String[] in, int id) {
		return null;
	}
	
	public static GameObject default_object(Vector2 loc) {
		return null;
	}
	
	//update for rectangle
	public void update_nodes(Vector2 in, int place, int grid_size) {
		this.nodes[place] = in;
		switch(place) {
		case 0:
			this.nodes[3].x = in.x;
			this.nodes[1].y = in.y;
			break;
		case 1:
			this.nodes[0].y = in.y;
			this.nodes[2].x = in.x;
			break;
		case 2:
			this.nodes[1].x = in.x;
			this.nodes[3].y = in.y;
			break;
		case 3:
			this.nodes[2].y = in.y;
			this.nodes[0].x = in.x;
			break;
		}
		this.clip_nodes(grid_size);
		this.update_dimensions();
	}
	
	public void update_dimensions() {
		this.pos.x = (this.nodes[0].x + this.nodes[2].x) / 2;
		this.pos.y = (this.nodes[0].y + this.nodes[2].y) / 2;
		this.width = Math.abs(this.nodes[2].x - this.nodes[0].x);
		this.height = Math.abs(this.nodes[2].y - this.nodes[0].y);
		
	}
	
	
}
