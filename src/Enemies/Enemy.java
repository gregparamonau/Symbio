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
	public boolean damaged = false, airborn = false;
	public Vector2 momentum = new Vector2(), vel = new Vector2();
	
	public int health;
	public int damage_cooldown = 0;
	static int damage_cooldown_max = 5;
	public double mass = 1;
	
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
		//this.pos.add(Vector2.add(move, Vector2.add(this.momentum, this.vel)));
		
		//this.momentum.x = Utility.clamp(Utility.sign(this.momentum.x) * (Math.abs(this.momentum.x) - 1), this.momentum.x, 0);
		//this.apply_gravity();
		
		//if (this.momentum.length() != 0) this.momentum = Vector2.scale_to_length(this.momentum, Utility.clamp(this.momentum.length() - 1, 0, this.momentum.length()));
		
		this.displace();
	}
	public void apply_env_forces() {
		this.vel.y -= 1;
		if (this.vel.y < max_fall_speed) this.vel.y = max_fall_speed;
		this.momentum_damp();
		//this.momentum.y = -2;//Math.max(max_fall_speed, this.momentum.y - 0.2);
	}
	public void displace() {
		for (int x = 0; x<Game.current_room.objects.length; x++) {
			this.pos.add(Game.current_room.objects[x].displace_entity(this, 0));
		}
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
		this.damage_function();
		this.knockback(dir);
		
		return true;
	}
	public void damage_function() {
		
	}
	public void knockback(Vector2 dir) {
		this.momentum.set(dir._mult(1.0 / this.mass));
		this.vel.set(new Vector2());
	}
	
	public void momentum_damp() {
		double l = this.momentum.l();
		if (l < 1e-3) this.momentum.set(new Vector2());//return;
		this.momentum.mult(0.75);
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
