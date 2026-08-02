package Player;

import Logic.Vector2;
import Logic.Collision.Rectangle;
import Logic.Utilities.Utility;
import Main.Game;

public class PlayerCombat {
	//attacks and health
	Player player;
	
	//attack
	public int slash_strength = 1;
	public Vector2 slash_dir;
	
	public Vector2 slash_bounds = new Vector2(35, 24);
	public Vector2 offset_slash = new Vector2(10, 12);
	
	
	//health system
	public Vector2 respawn_point = new Vector2(0, 0);
	public boolean respawn_set = false;
	public int health_full = 3;
	public int health = health_full;
	public int knockback = 6;
		
	public int invincibility_frames = 0;
	public int invincibility_frames_max = 60;
	
	
	public PlayerCombat(Player in) {
		this.player = in;
	}

	public void update() {
		
		//System.out.println("SLASH: " + this.player.slashing + " COUNT: " + this.slash_count);
		
		if (this.invincibility_frames > 0) this.invincibility_frames--;
		
		if (!this.respawn_set && this.player.object_intersect_id != -1 && this.player.collider.col_down && Game.current_room.objects[this.player.object_intersect_id].object_handle == -1) {
			this.respawn_point.set(this.player.pos);
			this.respawn_set = true;
		}
	}
	
	//ATTACK CODE

	
	public Vector2 find_slash_dir() {
		if (this.player.input.face_dir.y != 0 && !(this.player.input.face_dir.y < 0 && this.player.collider.col_down)) {
			return new Vector2(0, this.offset_slash.y * Utility.sign(this.player.input.face_dir.y));
		}
		//TODO: don't forget this
		return new Vector2(this.offset_slash.x * this.player.input.last_dir * 1/*(this.player.wall_slide ? -1 : 1)*/, 0);
	}
	
	
	
	
	//health
	public void damage(int damage, Rectangle source) {
		//return;
		
		if (this.invincibility_frames > 0) return;
		this.health -= damage;
		
		this.player.vel = Vector2.scale_to_length(Vector2.sub(this.player.pos, source.pos), this.knockback);
		/*if (this.player.movement.dashing) {
			this.player.movement.end_dash();
			this.player.momentum = new Vector2(0, 0);
		}*/
		this.player.momentum = Vector2.scale_to_length(Vector2.sub(this.player.pos, source.pos), this.player.momentum.l());
		
		this.invincibility_frames = this.invincibility_frames_max;
		
		if (this.health <= 0) this.death_respawn();
	}
	public void death_respawn() {
		//TODO: make this method
		this.health = this.health_full;
		this.player.set_position(this.respawn_point.x, this.respawn_point.y);
		this.player.vel = new Vector2(0, 0);
		this.player.momentum = new Vector2(0, 0);
	}
	public void hazard_respawn(boolean damage) {
		//TODO: finish this
		if (damage) this.health--;
		if (this.health == 0) {
			this.death_respawn();
			return;
		}
		
		this.player.set_position(this.respawn_point.x, this.respawn_point.y);
		this.player.vel = new Vector2(0, 0);
		this.player.momentum = new Vector2(0, 0);
	}
}
