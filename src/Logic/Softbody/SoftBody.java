package Logic.Softbody;

import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.FileReader;

import javax.swing.JPanel;

import Logic.Vector2;
import Logic.Collision.Line;
import Logic.Collision.Polygon;
import Logic.Collision.Rectangle;
import Main.Game;
import Player.Player;
import GameObject.Objects.Mover;

public class SoftBody {
	//ISSUES TO FIX:
		//squares collapsing in on each other
		//performance needs to be offloaded to a separate thread.
			//instead of storing vertices in 'Node' class, just use 3 arrays, pos, vel, force in SB class that handles this. should be significantly faster than Node
	//position
	/*
	float xpos, ypos;
	
	//base_rest_poses
	float[] base_rest_pos_x, base_rest_pos_y;
	float[] rest_pos_x, rest_pos_y;
	
	//nodes
	float[] xverts, yverts;
	float[] xvels, yvels;
	float[] xforce, yforce;
	
	//springs --> indices of spring.a and spring.b in the above 'node' arrays;
	//springs between nodes
	int[] aspr, bspr;
	
	//springs between node and vector
	float vec_spr_x, vec_spr_y; 
	boolean[] spr_hooked;
	float[] ks;//each spring has its own k value...*/
	
	//need to remove node class
	//need to remove spring class
	//need to remove polygon class?
	//need to remove vector2 class
	Vector2 pos;
	
	public Polygon pol;
	
	Color fill;
	
	//'base_rest_pos' is the very default non-rotated mesh that the shape has --> immutable
	//'rest_pos' is the rotated and moved mesh that the shape is actually pulled towards
	Vector2[] base_rest_pos, rest_pos;
	public Node[] nodes;
	int external_nodes;
	
	Spring[] springs;
	//simulation substeps
	final int num = 12, substeps = 1;
	public double angle, rest_volume, k = 100;
	
	public int id;
	
	
	//constructors
		//circle
	public SoftBody(String filename) {
		try {
			BufferedReader read = new BufferedReader(new FileReader(filename));
			
			String[] arr = read.readLine().split(" ");
			int N = Integer.parseInt(arr[0]);
			
			//always add the rest_pos springs as well.
			
			//reading in nodes
			this.nodes = new Node[N];
			
			for (int x = 0; x<N; x++) {
				arr = read.readLine().split(" ");
				
				
			}
			
			
		}catch (Exception e) {
			
		}
	}
	
	public SoftBody(Vector2 pos, double r, int id) {
		
		this.external_nodes = num;
		this.nodes = new Node[2 * num + 1];
		this.base_rest_pos = new Vector2[num];
		this.rest_pos = new Vector2[num];
		this.pos = pos;
		
		for (int x = 0; x<num; x++) {
			this.nodes[x] = new Node(new Vector2(this.pos.x + r * Math.cos(x * 2 * Math.PI / num), this.pos.y + r * Math.sin(x * 2 * Math.PI / num)), 0.75, "node");
			System.out.println(this.nodes[x]);
			this.rest_pos[x] = new Vector2(this.nodes[x].pos);
			this.base_rest_pos[x] = Vector2.sub(this.rest_pos[x], this.pos);
		}
		
		for (int x = 0; x < num; x++) {
			this.nodes[num + x] = new Node(this.rest_pos[x], 0.75, "hook");
		}
		
		this.nodes[2 * num] = new Node(this.pos, 0.75, "hook");
		
		
		this.pol = new Polygon(this);
		
		//how to arrange springs?
		
		this.springs = new Spring[4 * this.external_nodes];
		
		for (int x = 0; x < num; x++) {
			this.springs[x] = new Spring(this.nodes[x], this.nodes[(x + 1) % this.nodes.length], k / num);
			this.springs[this.external_nodes + x] = new Spring(this.nodes[x], this.nodes[(x + num / 4) % this.nodes.length], k / num);
			this.springs[2 * this.external_nodes + x] = new Spring(this.nodes[x], this.nodes[2 * num], k / num);
			//this.springs[2 * this.nodes.length + x] = new Spring(this.nodes[x], this.nodes[(x + num / 2 - 1) % this.nodes.length], k / num);
			this.springs[3 * this.external_nodes + x] = new Spring(this.nodes[x], this.nodes[x + num], k / num);
		}
		
		this.id = id;
		
		this.fill = Color.red;//new Color((int)(Math.random() * 255), (int)(Math.random() * 255), (int)(Math.random() * 255));
		
		//TODO: id & find_volume
	}
		//shape
	//update (nodes, springs, etc.)
	//rewrite this completely
	public void update() {
		this.apply_global_forces();
		
		this.apply_entity_forces();
		
		//System.out.println("DT GLOBAL FORCES: " + (b - a) / 1000000.0);
		
		this.apply_spring_forces();
		
		this.integrate_nodes(1.0 / Game.frame_rate);

		//intersect with other sbs
		for (int x = 0; x<Game.current_room.objects.length; x++) {
			if (Game.current_room.objects[x].sb == null) continue;
			if (x == this.id) continue;
			this.displace(Game.current_room.objects[x].sb);
		} //displacing from every other softbody
		//Game.player.pos = new Vector2();
		//for (int x = 0; x<Game.player.sb.pol.pnts.length; x++) Game.player.pos.add(Game.player.sb.pol.pnts[x]._mult(1.0 / Game.player.sb.pol.pnts.length));

		this.pol.get_bounds();
		this.displace_ground(); //displacing from ground
		//this.displace_player();
	
		this.configure();
		
		//System.out.println("DT Configure: " + (b - a) / 1000000.0);
		//System.out.println("TOTAL SB UPDATE: " + (total_end - total_start) / 1000000.0);
		
		//System.out.println(this.nodes[0]);
		
		//loop 10 times
		//update node gravity forces
		//update springs
		//move nodes
		//
	}
	public void apply_entity_forces() {
		//apply to entities in the game
		if (!Game.player.get_pol().intersect(this.pol)) return;
		
		int idx = this.closest_side();
		
		this.nodes[idx].force.add(new Vector2(0, -10 * Game.player.mass));
		this.nodes[(idx + this.nodes.length + 1) % this.nodes.length].force.add(new Vector2(0, -10 * Game.player.mass));
		
	}
	//apply_global_forces
	public void apply_global_forces() {
		for (int x = 0; x<this.nodes.length; x++) 
			this.nodes[x].force = new Vector2(0, -10 * this.nodes[x].mass);
	}
	//apply_spring_forces
	public void apply_spring_forces() {
		for (int x = 0; x<this.springs.length; x++) {
			System.out.println("SPRING: " + x + "/" + this.springs.length);
			this.springs[x].update();
		}
	}
	public void integrate_nodes(double dt) {
		for (int x = 0; x<this.nodes.length; x++) 
			this.nodes[x].update(dt);
	}
	
	public int closest_side() {
		double closest_dist = 100000;
		int idx = 0;
		for (int x = 0; x<this.pol.sides.length; x++) {
			double dist = Math.min(Vector2.dist(this.pol.sides[x].a, Game.player.pos), Vector2.dist(this.pol.sides[x].b, Game.player.pos));
			
			if (dist < closest_dist) {
				closest_dist = dist;
				idx = x;
			}
		}
		
		return idx;
	}
	
	public void displace_player() {
		int count = 0;
		while (count <= 10 && Game.player.get_pol().intersect(this.pol)) {
			//get closest segment
			
			System.out.println("COUNT: " + count + " ID: " + this.id);
			
			
			int idx = this.closest_side();
			
			Vector2 normal = this.pol.sides[idx].norm();
			
			//Game.player.pos.add(normal._mult(2));
			Game.player.collider.move(normal._mult(2));
			this.pol.sides[idx].a.add(normal._mult(-1));
			this.pol.sides[idx].b.add(normal._mult(-1));
			
			//player inherits some velocity
			Vector2 vel_old = new Vector2(Game.player.vel);
			Game.player.vel.set(Vector2.add(this.nodes[idx].vel, this.nodes[(idx + this.nodes.length + 1) % this.nodes.length].vel)._mult(0.5));
			
			//update node velocities
			this.nodes[idx].vel.set(vel_old);
			this.nodes[(idx + this.nodes.length + 1) % this.nodes.length].vel.set(vel_old);
			
			
			
			
			count++;
		}
		/*while (Game.player.get_pol().intersect(this.pol)) {
			
			
			
			System.out.println("TRUE: " + this.id);
			//find closest line segment
			
			double closest_dist = 100000;
			int idx = 0;
			for (int x = 0; x<this.pol.sides.length; x++) {
				double dist = Math.min(Vector2.dist(this.pol.sides[x].a, Game.player.pos), Vector2.dist(this.pol.sides[x].b, Game.player.pos));
				
				if (dist < closest_dist) {
					closest_dist = dist;
					idx = x;
				}
			}
			
			
			
			Vector2 tempa = new Vector2(this.pol.sides[idx].a);
			Vector2 tempb = new Vector2(this.pol.sides[idx].b);
			
			double area = this.pol.area();
			
			Vector2 adja = Vector2.sub(Game.player.pos, this.pol.sides[idx].a);
			adja = adja.norm()._mult(6.5 + adja.l());
			
			Vector2 adjb = Vector2.sub(Game.player.pos, this.pol.sides[idx].b);
			adjb = adjb.norm()._mult(6.5 + 1 + adjb.l());
			
			Vector2 afin = Vector2.add(this.pol.sides[idx].b, adjb);
			Vector2 bfin = Vector2.add(this.pol.sides[idx].a, adja);
			
			this.pol.sides[idx].a.set(afin);
			this.nodes[idx].vel.set(Game.player.vel);
			
			this.pol.sides[idx].b.set(bfin);
			this.nodes[(idx + this.nodes.length + 1) % this.nodes.length].vel.set(Game.player.vel);
			
			if (this.pol.area() > area) {
				this.pol.sides[idx].a.set(tempa);
				this.pol.sides[idx].b.set(tempb);
			}
			
			
			
			Game.player.vel.set(new Vector2());
			Game.player.pos.add(Vector2.add(adja, adjb)._mult(-0.2));
			
			break;
			
			
		}*/
	}
	
	
	public void displace_ground() {
		
		for (int x = 0; x<Game.current_room.objects.length; x++) {
			if (x == this.id) continue;
			if (Game.current_room.objects[x].pol == null) continue;
			if (!Rectangle.intersect(this.pol.bounds, Game.current_room.objects[x].pol.bounds)) continue;
			
			for (int y = 0; y < this.nodes.length; y++) {
				if (Game.current_room.objects[x].pol.intersect(this.nodes[y].pos)) {
					Game.current_room.objects[x].pol.displace(this.pos, this.nodes[y], false);
					
					if (Game.current_room.objects[x].object_handle != -1) {
						this.pos.add(((Mover)Game.current_room.objects[Game.current_room.objects[x].object_handle]).move);
						
						this.nodes[y].vel.add(((Mover)Game.current_room.objects[Game.current_room.objects[x].object_handle]).move);
					}
						
				}
			}
		}
	}
	//collide:
		//with node
		//with softbody
	//displace:
		//node
		//softbody
	
	//shape memory section
	//find_pos
	public void find_pos() {
		Vector2 out = new Vector2();
		
		for (int x = 0; x<this.nodes.length; x++) out.add(this.nodes[x].pos);
		
		this.pos.set(out._mult(1.0 / this.nodes.length));
	}
	//find_orientation
	public void find_orientation() {
		//use dot product instead
		//find angle to displace by
		this.find_pos();
		
		//average dot
		
		double dot = 0.0;
		
		double angle = 0.0;
		
		for (int x = 0; x<this.external_nodes; x++) {
			Vector2 vec = Vector2.sub(this.nodes[x].pos, this.pos);
			double ang = Vector2.angle(this.base_rest_pos[x], vec);
			
			dot += Vector2.ndot(this.base_rest_pos[x], vec);
			
			angle += Vector2.angle(this.base_rest_pos[x], vec);
		}
		
		//this.angle = (this.id % 2 == 0 ? 1 : -1) * (double)(System.currentTimeMillis() % 5000) / 5000 * Math.PI * 2;

		this.angle = Math.signum(angle) * Math.acos(Vector2.clamp(dot / this.nodes.length, -1, 1));
		//this.angle = angle / this.nodes.length;
	}
	//change orientation direction of frame
	public void change_orientation() {
		//make sure to use .set since it alters the vector2 without reassigning memory
		for (int x = 0; x<this.rest_pos.length; x++) {
			this.rest_pos[x].set(Vector2.add(this.pos, this.base_rest_pos[x].rotate(this.angle)));
		}
	}
	//change location and rotation of rest_pos
	public void configure() {
		this.find_orientation();
		
		this.change_orientation();
	}
	
	//to_polygon (maybe make memory-assigned polygon that stays in touch w Nodes)
	//expand volume (only for spheres)
	
	
	//intersect
	
	public boolean intersect(SoftBody in) {
		return this.pol.intersect(in.pol);
	}
	
	/*public void displace(SoftBody in) {
		
		if (!in.pol.intersect(this.pol)) return;
		//displacing our nodes out from the softbody in
		for (int x = 0; x<this.nodes.length; x++) {
			if (!in.pol.intersect(this.nodes[x].pos)) continue;
			
			int index = 0;
			double pb = 99999999;
			
			Vector2 p = new Vector2();
			
			
			for (int i = 0; i<in.pol.sides.length; i++) {
				Vector2 pnt = Line.find_node_on_line(in.pol.sides[i], this.nodes[x].pos);
				//using sign filter out any points outside the polygon
				//acc irrelevant
				
				
				
				
				
				//if (Vector2.dot(Vector2.sub(, pnt)))
				
				//if (Vector2.dot(Vector2.sub(pnt, this.nodes[x].pos), Vector2.sub(this.nodes[x].pos, in.pos)) < 0) continue;
				
				double temp = Vector2.dist(this.nodes[x].pos, pnt);

				//double temp = Math.signum(Vector2.dot(in, in)) * Vector2.dist(in, Line.find_node_on_line(this.sides[x], in));
				if (temp < pb) {
					p = pnt;
					index = x;
					pb = temp;
				}
			}
			
			Line line = in.pol.sides[index];
			
			Vector2 n = Vector2.sub(p, this.nodes[x].pos).norm();
			
			double t = Vector2.dist(line.a, p) / Vector2.dist(line.a, line.b);
			
			if (Vector2.dist(line.a, line.b) < 1e-3) t = 0;
			
			double displacement_constant = 2.0 / 3;
			
			double da = (1 - t) * displacement_constant * pb;
			double dc = t * displacement_constant * pb;
			double db = pb - da - t * (dc - da);
			
			final double FDAMP = 1, VDAMP = 0.60;
			
			long time_a = System.nanoTime();
			
			Vector2 ve1 = n._mult(db), ve2 = n._mult(-da), ve3 = n._mult(-dc);
			
			this.nodes[x].force = new Vector2(0, 0);
			in.nodes[index].force = new Vector2(0, 0);
			in.nodes[(index + 1) % in.nodes.length].force = new Vector2(0, 0);
			
			//displace positions
			//this.nodes[x].pos.set(p);
			
			this.nodes[x].pos.set(p);
			//in.nodes[index].pos.add(ve2);//.set(Vector2.add(in.nodes[index].pos, ve2));
			//in.nodes[(index + 1) % in.nodes.length].pos.add(ve3);//.set(Vector2.add(in.nodes[(index + 1) % in.nodes.length].pos, ve3));
			//this.nodes[x].pos.add(ve1);//.set(Vector2.add(this.nodes[x].pos, ve1));
			
			//calculate new velocities
			
			final double ELASTICITY = 0.3; 
			Vector2 V1 = this.nodes[x].vel;

			// Calculate the relative velocity along the normal (V_rel_n = V1_n)
			double vn = Vector2.dot(V1, n);

			// If the penetrating node is still moving INTO the other body (negative velocity):
			if (vn < 0) {
			    // Calculate the impulse magnitude (J) needed to reverse the velocity
			    // J = (-(1 + e) * V_rel_n) / (1/m1 + 1/m2)
			    // Assuming m2 (the segment mass) is large/infinite for simplicity, m2 drops out.
			    // J = -(1 + e) * vn * m1 
			    
			    // Instead of full impulse, just apply damped reflection to the normal component.
			    Vector2 V_normal = n._mult(vn);
			    
			    // V_new_normal = -E * V_old_normal
			    Vector2 V_new_normal = V_normal._mult(-ELASTICITY); 
			    
			    // Total impulse applied to V1: V_new_normal - V_old_normal
			    Vector2 impulse = Vector2.sub(V_new_normal, V_normal);
			    
			    // Apply impulse to the penetrating node's velocity
			    this.nodes[x].vel.add(impulse);

			    // Apply damping to stop excessive sliding/energy transfer
			    this.nodes[x].vel = this.nodes[x].vel._mult(VDAMP);
			    
			    // Since the segment nodes (in.nodes[index], in.nodes[index+1]) are part of another soft body, 
			    // simply damping them slightly is often enough to prevent secondary explosion.
			    in.nodes[index].vel = in.nodes[index].vel._mult(0.9);
			    in.nodes[(index + 1) % in.nodes.length].vel = in.nodes[(index + 1) % in.nodes.length].vel._mult(0.9);
			}
			
			long time_b = System.nanoTime();
			
			//System.out.println("DT COL: " + (time_b - time_a) / 1000000.0);
			
			
			
		}
	}*/
	public void displace(SoftBody in) {

	    if (!in.pol.intersect(this.pol)) return;

	    for (int x = 0; x < this.nodes.length; x++) {
	        if (!in.pol.intersect(this.nodes[x].pos)) continue;

	        // --- find nearest point on in's boundary, same as before ---
	        int index = 0;
	        double pb = 99999999;
	        Vector2 p = new Vector2();

	        for (int i = 0; i < in.pol.sides.length; i++) {
	            Vector2 pnt = Line.find_node_on_line(in.pol.sides[i], this.nodes[x].pos);
	            double temp = Vector2.dist(this.nodes[x].pos, pnt);
	            if (temp < pb) {
	                p = pnt;
	                index = i;      // NOTE: this was a bug in the original -- it stored x, not i
	                pb = temp;
	            }
	        }

	        Line line = in.pol.sides[index];
	        int i1 = index;
	        int i2 = (index + 1) % in.nodes.length;

	        Vector2 n = Vector2.sub(p, this.nodes[x].pos).norm();
	        double t = Vector2.dist(line.a, p) / Vector2.dist(line.a, line.b);
	        if (Vector2.dist(line.a, line.b) < 1e-3) t = 0;

	        // --- mass-weighted position correction, split between both bodies ---
	        // Treat 'in's edge as a single effective point mass, blended by t
	        // between its two endpoint masses.
	        double m1 = this.nodes[x].mass;
	        double m2a = in.nodes[i1].mass;
	        double m2b = in.nodes[i2].mass;
	        double m2 = (1 - t) * m2a + t * m2b;

	        double totalMass = m1 + m2;
	        double w1 = m2 / totalMass; // fraction of correction 'this' node absorbs
	        double w2 = m1 / totalMass; // fraction the 'in' edge absorbs

	        double penetration = pb;

	        // this.nodes[x] moves back along n by w1 * penetration
	        Vector2 correctionThis = n._mult(w1 * penetration);
	        this.nodes[x].pos.add(correctionThis);

	        // in's edge nodes move forward along n by w2 * penetration,
	        // split between the two endpoints by (1 - t) / t weighting
	        Vector2 correctionIn = n._mult(-w2 * penetration);
	        in.nodes[i1].pos.add(correctionIn._mult(1 - t));
	        in.nodes[i2].pos.add(correctionIn._mult(t));

	        // --- clear forces (as before) ---
	        this.nodes[x].force = new Vector2(0, 0);
	        in.nodes[i1].force = new Vector2(0, 0);
	        in.nodes[i2].force = new Vector2(0, 0);

	        // --- proper impulse-based velocity resolution ---
	        final double ELASTICITY = 0.3;
	        final double VDAMP = 0.60;

	        Vector2 v1 = this.nodes[x].vel;
	        Vector2 v2 = Vector2.add(in.nodes[i1].vel._mult(1 - t), in.nodes[i2].vel._mult(t));

	        Vector2 vRel = Vector2.sub(v1, v2);
	        double vn = Vector2.dot(vRel, n);

	        if (vn < 0) {
	            // J = -(1 + e) * vn / (1/m1 + 1/m2)
	            double invM1 = 1.0 / m1;
	            double invM2 = 1.0 / m2;
	            double j = -(1 + ELASTICITY) * vn / (invM1 + invM2);

	            Vector2 impulse = n._mult(j);

	            // this node gains +impulse/m1
	            this.nodes[x].vel.add(impulse._mult(1.0 / m1));
	            this.nodes[x].vel = this.nodes[x].vel._mult(VDAMP);

	            // in's edge loses impulse, split back out by (1 - t)/t onto the two endpoints
	            Vector2 inImpulse = impulse._mult(-1.0 / m2);
	            in.nodes[i1].vel.add(inImpulse._mult(1 - t));
	            in.nodes[i2].vel.add(inImpulse._mult(t));
	            in.nodes[i1].vel = in.nodes[i1].vel._mult(VDAMP);
	            in.nodes[i2].vel = in.nodes[i2].vel._mult(VDAMP);
	        }
	    }
	}
	
	
	//draw TODO: make this work with proper texture rendering
	public void draw(Graphics g, JPanel pane, double xpos, double ypos, String location) {
		//if (!(this.id == 2 || this.id == 3 || this.id == 4 || this.id == 6 || this.id == 14)) return;
		
		int[][] temp = this.to_polygon(pane, xpos, ypos, location);
		g.setColor(this.fill);		
		g.fillPolygon(temp[0], temp[1], this.external_nodes);
		
		g.setColor(Color.blue);		
		g.drawPolygon(temp[0], temp[1], this.external_nodes);
		
		temp = this.to_polygon(this.rest_pos, pane, xpos, ypos, location);
		
		g.setColor(Color.orange);
		
		//g.drawPolygon(temp[0], temp[1], this.rest_pos.length);
		
		for (int x = 0; x<this.nodes.length; x++) {
			//this.nodes[x].pos.draw_node(g, pane, xpos, ypos, location, Color.blue);
		}
		
		
		/*for (int x = 0; x<this.pol.sides.length; x++) {
			Vector2 cent = Vector2.add(this.pol.sides[x].a, this.pol.sides[x].b)._mult(0.5);
			Vector2 norm = Vector2.add(this.pol.sides[x].norm()._mult(5), cent);
			
			Vector2 conv_a = Vector2.converted_pos(cent, pane, xpos, ypos, location);
			Vector2 conv_b = Vector2.converted_pos(norm, pane, xpos, ypos, location);
			
			g.drawLine((int)conv_a.x, (int)conv_a.y, (int)conv_b.x, (int)conv_b.y);
		}*/
		
		//this.pol.bounds.draw_border(g, pane, xpos, ypos, location);
		
		//Vector2 tempA = Vector2.add(this.pos, new Vector2(Base.screen_width / 2, Base.screen_height / 2));
		
		//g.drawString(this.id + "", (int)tempA.x, (int)tempA.y);
		
		/*for (int x = 0; x<this.nodes.length; x++) {
			Vector2 tempB = Vector2.add(this.nodes[x].pos, new Vector2(Base.screen_width / 2, Base.screen_height / 2));
			
			g.drawString(x + "", (int)tempB.x, (int)tempB.y);
			
			this.nodes[x].draw_node(g);
		}*/
		
		
		//for (int x = 0; x<this.springs.length; x++) this.springs[x].draw(g, pane, xpos, ypos, location, false);
		//for (int x = this.nodes.length * (this.nodes.length - 1) / 2; x<this.springs.length; x++) this.springs[x].draw(g);
	
	}
	
	public int[][] to_polygon(JPanel pane, double xpos, double ypos, String location) {
		int[][] out = new int[2][this.external_nodes];
		
		for (int x = 0; x<out[0].length; x++) {
			Vector2 temp = Vector2.converted_pos(this.nodes[x].pos, pane, xpos, ypos, location);
			out[0][x] = (int)temp.x;
			out[1][x] = (int)temp.y;

		}
		
		return out;
	}
	
	public int[][] to_polygon(Vector2[] in, JPanel pane, double xpos, double ypos, String location) {
		int[][] out = new int[2][this.external_nodes];
		
		for (int x = 0; x<out[0].length; x++) {
			Vector2 temp = Vector2.converted_pos(in[x], pane, xpos, ypos, location);
			out[0][x] = (int)temp.x;
			out[1][x] = (int)temp.y;

		}
		
		return out;
	}
	
	
	
}
