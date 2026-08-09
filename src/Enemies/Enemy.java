package Enemies;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

import Logic.Vector2;
import Logic.Collision.Rectangle;
import Logic.Utilities.Utility;
import Main.Game;
import Rendering.Animation;

public class Enemy extends Rectangle{
	
	public String code;
	public int id;
	public boolean damaged = false;
	public Vector2 vel = new Vector2();
	
	public int health;
	public int damage_cooldown = 0;
	static int damage_cooldown_max = 5;
	public double mass = 1;
	static double ground_drag = 0.5;
	
	public static final int enemy_knowckback = 0, contact_damage = 1, max_fall_speed = -10;
	
	public Enemy() {}
	public Enemy(double a, double b) {
		this.pos = new Vector2(a, b);
	}
	
	public void update() {
		//DAMAGE COOLDOWN IN ALL ENEMIES
	}
	
	public void move(Vector2 move) {
		this.pos.add(move);

		this.displace();
	}
	public void apply_env_forces() {
		//vertical
		this.vel.y -= 0.75;
		if (this.vel.y < max_fall_speed) this.vel.y = max_fall_speed;
		
		//horizontal
		this.vel.x = Math.signum(this.vel.x) * Math.max(0, Math.abs(this.vel.x) - ground_drag);
		//this.momentum.y = -2;//Math.max(max_fall_speed, this.momentum.y - 0.2);
	}
	public Vector2 displace() {
		for (int x = 0; x<Game.current_room.objects.length; x++) {
			Vector2 disp = Game.current_room.objects[x].displace_entity(this, 0);
			if (disp.x != 0 || disp.y != 0) {
				this.pos.add(disp);
				//this.vel.set(new Vector2());
				return disp;
			}
		}
		return Vector2.zero;
	}
	
	public Vector2 ai() {
		return new Vector2(0, 0);
	}
	
	public boolean damage(int damage, Vector2 dir) {
		if (this.damage_cooldown > 0) return false;
		this.health -= damage;
		this.damage_cooldown = damage_cooldown_max;
		this.damaged = true;
		if (this.health <= 0) this.die();
		this.damage_function(dir);
		this.knockback(dir);
		
		return true;
	}
	public void damage_function(Vector2 dir) {
		
	}
	public void knockback(Vector2 dir) {
		this.vel.set(dir._mult(1.0 / this.mass));
	}
	
	public void die() {
		//play kill animation
		//spawn background object as carcass
		Game.current_room.remove_enemy(this.id);
	}
	
	public void collision_action() {
		if (Rectangle.intersect(this, Game.player)) {
			Game.player.combat.damage(contact_damage, this);
		}
	}
	
	public void draw_enemy(Graphics g, JPanel pane, double xin, double yin, String location) {
		if (this.damaged) this.fill = Color.red;
		else this.fill = Color.gray;
		
		this.damaged = false;
		
		this.draw(g, pane, xin, yin, location, false);
		Vector2 temp = Vector2.converted_pos(this.pos, pane, xin, yin, location);
		
		g.drawString("health: " + this.health, (int)temp.x, (int)temp.y);
		
		//this.draw_with_sprite(g, Game.pane, Game.cam.pos.x, Game.cam.pos.y, this.sprite, "game");
	}
}
