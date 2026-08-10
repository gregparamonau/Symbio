package Player;

import Logic.Vector2;
import Logic.Collision.Rectangle;
import Logic.Utilities.Utility;
import Main.Game;

public class PlayerCombat {
	//attacks and health
	Player player;
	
	//attack
	public int slash_strength = 5;
	public Vector2 slash_dir = new Vector2();
	public int slash_direction_buffer = 0, slash_count = 0, slash_cooldown, slash_buffer;
	static int slash_direction_buffer_max = 2, slash_length = 10, slash_cooldown_max = 10, slash_buffer_max = 5;
	public boolean slashing = false, slash_held = false, pogo = false;
	
	public double slash_h_knockback = 6, slash_v_knockback = 8;
	
	public Vector2 slash_bounds = new Vector2(36, 24);
	public Vector2 offset_slash = new Vector2(10, 12);
	
	
	//health system
	public Vector2 respawn_point = new Vector2(0, 0);
	public boolean respawn_set = false;
	public int health_full = 7;
	public int health = health_full;
	public int knockback = 8;
	public int enemy_knockback_horizontal = 3, enemy_knockback_vertical = 10;
		
	public int invincibility_frames = 0;
	public int invincibility_frames_max = 60;
	
	
	public PlayerCombat(Player in) {
		this.player = in;
	}

	public void update() {
		if (this.player.input.slash) this.slash_buffer = slash_buffer_max;
		if ((this.player.input.slash || this.slash_buffer > 0) && this.slash_cooldown == 0 && this.slash_direction_buffer == 0 && !this.slashing && !this.slash_held) this.start_slash();
		
		//System.out.println("SLASH: " + this.player.slashing + " COUNT: " + this.slash_count);
		
		if (this.invincibility_frames > 0) this.invincibility_frames--;
		if (this.slash_cooldown > 0) this.slash_cooldown--;
		if (this.slash_buffer > 0) this.slash_buffer--;
		
		if (!this.respawn_set && this.player.object_intersect_id != -1 && this.player.collider.col_down && Game.current_room.objects[this.player.object_intersect_id].object_handle == -1) {
			this.respawn_point.set(this.player.pos);
			this.respawn_set = true;
		}
		if (this.slash_direction_buffer > 0 || this.slash_count > 0) this.slash();
		this.slashing = this.slash_count > 0;
		this.pogo = this.slash_cooldown > 0 && this.slash_dir.y < 0;
		this.slash_held = this.player.input.slash && (this.slashing || this.slash_held);
	}
	
	//ATTACK CODE
	public void start_slash() {
		this.slash_direction_buffer = slash_direction_buffer_max;
		System.out.println("START_SLASH");
		//X frame window to choose slash direction (maybe not necessary)
		//decide direction
		//loop through enemies & other things we can attack & attack them as appropriate
		//give every thing a Y frame cooldown, where it cannnot be attacked again in that time
		//
	}
	
	public void slash() {
		System.out.println("SLASHING");
		if (this.slash_direction_buffer > 0) {
			this.slash_dir.set(this.find_slash_dir());
			this.slash_direction_buffer--;
			if (this.slash_direction_buffer == 0) this.slash_count = slash_length;
			System.out.println("SLASH BUFFER");
			return;
		}
		
		Rectangle hb = new Rectangle(this.player.pos.x + this.slash_dir.x, this.player.pos.y + this.slash_dir.y, (this.slash_dir.x != 0 ? 1 : 0) * this.slash_bounds.x + (this.slash_dir.x == 0 ? 1: 0) * this.slash_bounds.y, (this.slash_dir.y != 0 ? 1 : 0) * this.slash_bounds.x + (this.slash_dir.y == 0 ? 1: 0) * this.slash_bounds.y);
		
		for (int x = 0; x<Game.current_room.enemies.length; x++) {
			if (Rectangle.intersect(hb, Game.current_room.enemies[x])) {
				if (Game.current_room.enemies[x].damage(this.slash_strength, this.slash_dir.norm()._mult(this.slash_dir.x != 0 ? enemy_knockback_horizontal : enemy_knockback_vertical))) {
					this.slash_effects();
				}
			}
		}
		System.out.println("SLASH");
		
		if (this.slash_count > 0) this.slash_count--;
		if (this.slash_count == 0) this.slash_cooldown = slash_cooldown_max;
		
	}
	
	public void slash_effects() {
		this.player.movement.knockback(this.slash_dir._mult(-1).norm());
		this.player.movement.dash_num = this.player.movement.dash_keep;
	}
	
	
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
