package Rendering;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import Logic.Vector2;
import Logic.Collision.Rectangle;
import Logic.Utilities.Utility;
import Main.Game;

public class Animation {
	public static ArrayList<Animation> anims = new ArrayList<>();
	
	static BufferedImage[][] global_sprites = new BufferedImage[0][2];
	static Map<String, Integer[]> anim_dict = new HashMap<>();
	static Map<Integer, String> location_dict = new HashMap<>();
	
	//index 0 for normal anim, index 1 for flipped anim
	
	public int index_start;
	public int counter = -1, frame = 0;
	Vector2 pos;
	String file_name;
	public boolean repeating = false, ended = false, flip = false, persistent = false;
	int frequency, length;
	public int id;
	
	//TODO, instead of having a bajillion of the same animation, 
	//load the animation sprites into one array, and let every 
	//Animation object reference this global sprite array through indices.
	
	//this reduces ram usage
	//also allows for easier animation control
	//clear this array every time we go to a new room.
	//particle effects can thus also have animations attached
	//to them without needing a lot of memory
	
	
	public Animation(String file, Vector2 pos, boolean create_flip, boolean persistent) {
		this.pos = pos;
		this.persistent = persistent;
		this.file_name = file;
		try {
			BufferedImage temp = ImageIO.read(getClass().getResource(file));
			
			//first column of sprite sheet contains info about frequency, and length of an animation.
			this.length = temp.getRGB(0, 0) & 16777215; // 00000000_11111111_11111111_11111111
			this.frequency = temp.getRGB(0, 1) & 16777215;
			this.repeating = temp.getRGB(0, 2) == Color.white.getRGB();
			
			this.index_start = global_sprites.length;
			
			if (anim_dict.containsKey(file)) {
				this.index_start = anim_dict.get(file)[0];
				anims.add(this);
				return;
			}
			
			if (!anim_dict.containsKey(file)) {
				anim_dict.put(file, new Integer[] {this.index_start, this.length});
				location_dict.put(this.index_start, file);
				anims.add(this);
			}
			
			
			System.out.println("L: " + this.length + " F: " + this.frequency + " R: " + this.repeating);
			
			BufferedImage[][] barr = new BufferedImage[this.length][2];
			for (int x = 0; x<this.length; x++) {
				barr[x][0] = temp.getSubimage(x * temp.getWidth() / this.length + 1, 0, (temp.getWidth() - 1) / this.length, temp.getHeight());
				if (create_flip) barr[x][1] = Utility.flip(barr[x][0], false, true);
			}
			
			this.add_animation(barr);
			
		
		}catch(IOException e) {
			e.printStackTrace();
		}
	}
	public void start() {
		this.counter = 0;
		//add_new_animation(this);
	}
	public void play(boolean play_pos, Vector2 loc, boolean flipv, Graphics g, JPanel pane, double xin, double yin, String location) {
		if (this.counter == -1 || this.ended) return;
		new Rectangle(this.pos.x * (play_pos ? 0 : 1) + loc.x * (play_pos ? 1 : 0), this.pos.y * (play_pos ? 0 : 1) + loc.y * (play_pos ? 1 : 0), 0, 0).draw_with_sprite(g, pane, xin, yin, global_sprites[this.frame + this.index_start][(flipv? 1 : 0)], location);
		
		if (!this.repeating && (this.counter + 1 >= this.length * this.frequency || this.frame >= this.length)) {
			this.ended = true;
			return;
			//this.end();
		}
		
		this.counter = (this.counter + 1) % (this.length * this.frequency);
		this.frame = this.counter / this.frequency;

	}
	public void end() {
		//kill_animation(this);
	}
	public boolean interrupt() {
	    if (this.ended) return true;
	    if (this.counter % this.length == 0) return true;
	    return false;
	}
	
	public void add_animation(BufferedImage[][] in) {
		BufferedImage[][] out = new BufferedImage[global_sprites.length + in.length][2];
		
		System.arraycopy(global_sprites, 0, out, 0, global_sprites.length);
		System.arraycopy(in, 0, out, global_sprites.length, in.length);
		
		global_sprites = out;
	}
	
	public static void clear_animation(String filename, int start, int length) {
		
		BufferedImage[][] out = new BufferedImage[global_sprites.length - length][2];
		
		System.arraycopy(global_sprites, 0, out, 0, start);
		
		int rem = global_sprites.length - start - length;
		
		if (rem > 0) System.arraycopy(global_sprites, start + length, out, start, rem);
		
		global_sprites = out;
	}
	
	public static void clear_anims() {
		anim_dict.entrySet().removeIf(entry -> {
			String key = entry.getKey();
			int start = entry.getValue()[0], length = entry.getValue()[1];
			
			boolean cleared = anims.removeIf(anim -> {
				return !anim.persistent && anim.file_name.equals(key);
			});
			
			if (cleared) {
				clear_animation(key, start, length);
				update_anims(start, length);
			}
			return cleared;
		});
	}
	public static void update_anims(int start, int length) {
		for (String key: anim_dict.keySet()) {
			int temp = anim_dict.get(key)[0];
			if (temp > start) {
				anim_dict.put(key, new Integer[] {temp - length, anim_dict.get(key)[1]});
				location_dict.remove(temp);
				location_dict.put(temp - length, key);
			}	
		}
		
		for (Animation anim: anims) {
			if (anim_dict.containsKey(anim.file_name))
				anim.index_start = anim_dict.get(anim.file_name)[0];
		}
	}
	public String toString() {
		return "FILE: " + this.file_name + " START: " + this.index_start;
	}

}
