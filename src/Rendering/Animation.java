package Rendering;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import Logic.Vector2;
import Logic.Collision.Rectangle;
import Logic.Utilities.Utility;
import Main.Game;

public class Animation {
	public static Animation[] anims = new Animation[0];
	public BufferedImage[][] sprites;
	public int counter = -1, frame = 0;
	Vector2 pos;
	public boolean repeating = false, ended = false, flip = false;
	int frequency, length;
	public int id;
	//all animations are by default 10 frames long (and only that option) (so 10 frames per second)
	//fliph across horizontal
	//flipv across vertical
	public Animation(String file, Vector2 pos, boolean create_flip) {
		this.pos = pos;
		try {
			BufferedImage temp = ImageIO.read(getClass().getResource(file));
			
			//first column of sprite sheet contains info about frequency, and length of an animation.
			this.length = temp.getRGB(0, 0) & 16777215; // 00000000_11111111_11111111_11111111
			this.frequency = temp.getRGB(0, 1) & 16777215;
			this.repeating = temp.getRGB(0, 2) == Color.white.getRGB();
			
			this.sprites = new BufferedImage[(create_flip ? 2 : 1)][this.length];
			for (int x = 0; x<this.length; x++) {
				this.sprites[0][x] = temp.getSubimage(x * temp.getWidth() / this.length + 1, 0, (temp.getWidth() - 1) / this.length, temp.getHeight());
				if (create_flip) this.sprites[1][x] = Utility.flip(this.sprites[0][x], false, true);
			}
			
		
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
		new Rectangle(this.pos.x * (play_pos ? 0 : 1) + loc.x * (play_pos ? 1 : 0), this.pos.y * (play_pos ? 0 : 1) + loc.y * (play_pos ? 1 : 0), 0, 0).draw_with_sprite(g, pane, xin, yin, this.sprites[(flipv? 1 : 0)][this.frame], location);
		
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
	
	
	/*public static void add_new_animation(Animation in) {
		Animation[] temp = new Animation[anims.length + 1];
		for (int x = 0; x<anims.length; x++) {
			temp[x] = anims[x];
		}
		temp[anims.length] = in;
		temp[anims.length].id = anims.length;
		
		anims = temp;
	}
	public static void kill_animation(Animation in) {
		Animation[] temp = new Animation[anims.length - 1];
		
		for (int x = 0; x<temp.length; x++) {
			temp[x] = anims[(x < in.id ? x : x + 1)];
			temp[x].id = x;
		}
		
		anims = temp;
	}*/
}
