package Entity;

import Logic.Vector2;
import Logic.Utilities.Utility;

public class EntityCollision {
	public Entity ent;
	public Vector2 pos;
	
	public boolean col_left, col_right, col_up, col_down;
	
	public EntityCollision(Entity in) {
		this.pos = in.pos;
		
	}
	
	public void squash_action() {
		
	}
}

//goals: 
//collision system:
	//generalised 'collider' class that acts as wrapper for all types of colliders:
		//circle
		//rectangle
		//polygon
	//'Entity' superclass which has a component called 'collider'
	//class called EntityCollision, EntityCombat, EntityMovement, Entity Render etc.
		//Player's version of these classes can extend from these respective versions
		//player can extend from 'Entity'
//Room has 'Entity[] ents' or something, which allows you to process all of them in one batch
//Player is always ents[0] (or separate, might be excluded from array)

