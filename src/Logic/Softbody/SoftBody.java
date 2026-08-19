package Logic.Softbody;

import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JPanel;

import Logic.Vector2;
import Logic.Collision.Line;
import Logic.Collision.Polygon;
import Logic.Collision.Rectangle;
import Main.Game;
import Player.Player;
import GameObject.Objects.Mover;

public class SoftBody {
	Vector2[] poss;
	double[] angles;
	
	public Polygon pol;
	
	Color fill;
	
	//'base_rest_pos' is the very default non-rotated mesh that the shape has --> immutable
	//'rest_pos' is the rotated and moved mesh that the shape is actually pulled towards
	Vector2[][] base_rest_pos, rest_pos;
	public Node[] nodes;
	public int external_nodes;
	public int[][] frame_inds;
	
	boolean fixed = false;
	
	Spring[] springs;
	final int num = 12;
	public double rest_volume, k = 100;
	
	public int id, substeps = 1;
	
	
	//constructors
		//circle
	public SoftBody(Vector2 pos, String filename, int id) {
		double max_k = 0;
		try {
			BufferedReader read = new BufferedReader(new FileReader(filename));
			//file structure
			//[# Nodes] [# external] [#frames] [# springs] [default mass] [default k]
			double[] header = Arrays.stream(read.readLine().split(" ")).mapToDouble(Double::parseDouble).toArray();
	
			int NN = (int)header[0], NF = (int)header[2], NS = (int)header[3];
			this.external_nodes = (int)header[1];
			double default_mass = header[4];
			this.k = header[5];
			
			//initialise all arrays
			this.poss = new Vector2[NF];
			Node[] core_nodes = new Node[NN]; //copy later into new one
			Spring[] core_springs = new Spring[NS];
			this.base_rest_pos = new Vector2[NF][];
			this.rest_pos = new Vector2[NF][];
			this.frame_inds = new int[NF][];
			this.angles = new double[NF];
			
			//read in base nodes
			String[] arr;
			for (int x = 0; x<NN; x++) {
				arr = read.readLine().split(" ");
				core_nodes[x] = new Node(arr, pos, default_mass);
			}
			
			ArrayList<Node> nodes_t = new ArrayList<>();
			ArrayList<Spring> springs_t = new ArrayList<>();
			
			//read in the frames
			int[] inds;
			for (int x = 0; x<NF; x++) {
				inds = Arrays.stream(read.readLine().split(" ")).mapToInt(Integer::parseInt).toArray();
				this.base_rest_pos[x] = new Vector2[inds.length];
				this.rest_pos[x] = new Vector2[inds.length];
				this.frame_inds[x] = inds;
				
				for (int i = 0; i < inds.length; i++) {
					this.rest_pos[x][i] = new Vector2(core_nodes[inds[i]].pos);
					Node temp = new Node(this.rest_pos[x][i], 0, "hook");
					nodes_t.add(temp);
					springs_t.add(new Spring(core_nodes[inds[i]], temp, this.k));
				}
				max_k = Math.max(max_k, this.k);
				
				//make base_rest_pos
				this.poss[x] = Vector2.ave(this.rest_pos[x]);
				for (int i = 0; i < inds.length; i++) 
					this.base_rest_pos[x][i] = Vector2.sub(this.rest_pos[x][i], this.poss[x]);
				
				//done creating nodes and structures
			}
			
			//create springs specified in fil	
			//springs
			
			//create base springs
			for (int x = 0; x<NS; x++) {
				arr = read.readLine().split(" ");
				if (arr.length == 2)
					core_springs[x] = new Spring(core_nodes[Integer.parseInt(arr[0])], core_nodes[Integer.parseInt(arr[1])], this.k);
				else core_springs[x] = new Spring(core_nodes[Integer.parseInt(arr[0])], core_nodes[Integer.parseInt(arr[1])], Double.parseDouble(arr[2]));
				max_k = Math.max(core_springs[x].k, max_k);
			}
			
			
			//merge core and actual into final  everything
			
			this.nodes = new Node[NN + nodes_t.size()];
			this.springs = new Spring[NS + springs_t.size()];
			
			System.arraycopy(core_nodes, 0, this.nodes, 0, NN);
			System.arraycopy(nodes_t.toArray(), 0, this.nodes, NN, nodes_t.size());
			
			System.arraycopy(core_springs, 0, this.springs, 0, NS);
			System.arraycopy(springs_t.toArray(), 0, this.springs, NS, springs_t.size());
			
			
			this.substeps = Math.max((int)(max_k / 25), 1);
			
			this.id = id;
			
			this.fill = Color.red;
			
			this.pol = new Polygon(this);
			
			read.close();
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
		//shape
	//update (nodes, springs, etc.)
	//rewrite this completely
	public void update() {		
		
		for (int x = 0; x<this.substeps; x++) {
			this.apply_global_forces(); 
			this.apply_entity_forces();
			this.apply_spring_forces();
			this.integrate_nodes(1.0 / Game.frame_rate / substeps);
		}
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
		this.nodes[(idx + this.external_nodes + 1) % this.external_nodes].force.add(new Vector2(0, -10 * Game.player.mass));
		
	}
	//apply_global_forces
	public void apply_global_forces() {
		for (int x = 0; x<this.nodes.length; x++) 
			if (this.nodes[x].type.equals("node")) this.nodes[x].force = new Vector2(0, -10 * this.nodes[x].mass);
	}
	//apply_spring_forces
	public void apply_spring_forces() {
		for (int x = 0; x<this.springs.length; x++) {
	        if (this.springs[x] == null) continue;

			this.springs[x].update(this.substeps);
		}
	}
	public void integrate_nodes(double dt) {
		for (int x = 0; x<this.nodes.length; x++) 
			this.nodes[x].update(dt, this.substeps);
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
	
	/*public void displace_player() {
		int count = 0;
		while (count <= 10 && Game.player.get_pol().intersect(this.pol)) {
			//get closest segment			
			
			int idx = this.closest_side();
			
			Vector2 normal = this.pol.sides[idx].norm();
			
			//Game.player.pos.add(normal._mult(2));
			Game.player.collider.move(normal._mult(1));
			int total = -1;
			if (this.nodes[idx].type.equals("node")) {
				this.pol.sides[idx].a.add(normal._mult(this.nodes[(idx + this.external_nodes + 1) % this.external_nodes].type.equals("node") ? -0.5 : -1));
				total += 0.5;
			}
			if (this.nodes[(idx + this.external_nodes + 1) % this.external_nodes].type.equals("node"))
				this.pol.sides[idx].b.add(normal._mult(total));
			
			//player inherits some velocity
			Vector2 vel_old = new Vector2(Game.player.vel);
			Game.player.vel.set(Vector2.add(this.nodes[idx].vel, this.nodes[(idx + this.external_nodes + 1) % this.external_nodes].vel)._mult(0.5));
			
			//update node velocities
			this.nodes[idx].vel.set(vel_old);
			this.nodes[(idx + this.external_nodes + 1) % this.external_nodes].vel.set(vel_old);
			
			
			
			
			count++;
		}
	}*/
	
	public void displace_player() {
	    if (!Game.player.get_pol().intersect(this.pol)) return;

	    int count = 0;
	    while (count <= 10 && Game.player.get_pol().intersect(this.pol)) {
	        int idx = this.closest_side();

	        Node na = this.nodes[idx];
	        Node nb = this.nodes[(idx + 1) % this.external_nodes];

	        Vector2 normal = this.pol.sides[idx].norm();

	        // push player out
	        Game.player.collider.move(normal._mult(1));

	        // --- masses ---
	        double mp = Game.player.mass;
	        double mna = na.type.equals("node") ? na.mass : Double.MAX_VALUE;
	        double mnb = nb.type.equals("node") ? nb.mass : Double.MAX_VALUE;
	        // t = 0.5: player hits middle of edge
	        double t = 0.5;
	        double me = (1 - t) * mna + t * mnb; // effective edge mass

	        // --- velocities ---
	        // player total velocity (vel + momentum), in player's frame units (pixels/frame)
	        Vector2 vp = Vector2.add(Game.player.vel, Game.player.momentum);
	        Vector2 ve = Vector2.add(na.vel._mult(1 - t), nb.vel._mult(t));

	        Vector2 vRel = Vector2.sub(vp, ve);
	        double vn = Vector2.dot(vRel, normal);

	        if (vn < 0) {
	            final double ELASTICITY = 0.3;

	            double invMp = 1.0 / mp;
	            double invMe = (me == Double.MAX_VALUE) ? 0.0 : 1.0 / me;
	            double j = -(1 + ELASTICITY) * vn / (invMp + invMe);

	            Vector2 impulse = normal._mult(j);

	            // apply to player — split between vel and momentum proportionally
	            double vp_len = vp.l();
	            double vel_frac = vp_len > 1e-3 ? Game.player.vel.l() / vp_len : 1.0;
	            double mom_frac = 1.0 - vel_frac;

	            Game.player.vel.add(impulse._mult(invMp * vel_frac));
	            Game.player.momentum.add(impulse._mult(invMp * mom_frac));

	            // apply to edge nodes
	            if (me != Double.MAX_VALUE) {
	                Vector2 nodeImpulse = impulse._mult(-invMe);
	                if (na.type.equals("node")) na.vel.add(nodeImpulse._mult(1 - t));
	                if (nb.type.equals("node")) nb.vel.add(nodeImpulse._mult(t));
	            }
	        }

	        count++;
	    }
	}
	
	
	public void displace_ground() {
		
		for (int x = 0; x<Game.current_room.objects.length; x++) {
			if (x == this.id) continue;
			if (Game.current_room.objects[x].pol == null) continue;
			if (!Rectangle.intersect(this.pol.bounds, Game.current_room.objects[x].pol.bounds)) continue;
			
			for (int y = 0; y < this.external_nodes; y++) {
				if (Game.current_room.objects[x].pol.intersect(this.nodes[y].pos)) {
					Game.current_room.objects[x].pol.displace(Vector2.ave(this.poss), this.nodes[y], false);
					
					if (Game.current_room.objects[x].object_handle != -1) {
						for (int i = 0; i<this.frame_inds.length; i++)
							this.poss[i].add(((Mover)Game.current_room.objects[Game.current_room.objects[x].object_handle]).move);
						
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
		
		for (int x = 0; x<this.frame_inds.length; x++) {
			Vector2 temp = new Vector2();
			for (int i = 0; i < this.frame_inds[x].length; i++) {
				temp.add(this.nodes[this.frame_inds[x][i]].pos);
			}
			this.poss[x].set(temp._mult(1.0 / this.frame_inds[x].length));
		}
	}
	//find_orientation
	public void find_orientation() {
		//use dot product instead
		//find angle to displace by
		this.find_pos();
		
		//average dot
		
		for (int x = 0; x<this.angles.length; x++) {
			double dot = 0.0;
			double angle = 0.0;
			
			for (int i = 0; i < this.frame_inds[x].length; i++) {
				Vector2 vec = Vector2.sub(this.nodes[this.frame_inds[x][i]].pos, this.poss[x]);
				
				dot += Vector2.ndot(this.base_rest_pos[x][i], vec);
				
				angle += Vector2.angle(this.base_rest_pos[x][i], vec);
			}
			
			this.angles[x] = Math.signum(angle) * Math.acos(Vector2.clamp(dot / this.frame_inds[x].length, -1, 1));
		}
	}
	//change orientation direction of frame
	public void change_orientation() {
		if (this.fixed) return;
		//make sure to use .set since it alters the vector2 without reassigning memory
		for (int x = 0; x<this.frame_inds.length; x++) {
			
			for (int i = 0; i < this.frame_inds[x].length; i++) {
				this.rest_pos[x][i].set(Vector2.add(this.poss[x], this.base_rest_pos[x][i].rotate(this.angles[x])));
			}
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

	public void displace(SoftBody in) {

	    if (!in.pol.intersect(this.pol)) return;

	    for (int x = 0; x < this.external_nodes; x++) {
	    	if (this.nodes[x].type.equals("fixed")) continue;
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
	        int i2 = (index + 1) % in.external_nodes;

	        Vector2 n = Vector2.sub(p, this.nodes[x].pos).norm();
	        double t = Vector2.dist(line.a, p) / Vector2.dist(line.a, line.b);
	        if (Vector2.dist(line.a, line.b) < 1e-3) t = 0;

	        // --- mass-weighted position correction, split between both bodies ---
	        // Treat 'in's edge as a single effective point mass, blended by t
	        // between its two endpoint masses.
	        double m1 = this.nodes[x].mass;
	        double m2a = in.nodes[i1].type.equals("node") ? in.nodes[i1].mass : Double.MAX_VALUE;
	        double m2b = in.nodes[i2].type.equals("node") ? in.nodes[i2].mass : Double.MAX_VALUE;
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
	        if (in.nodes[i1].type.equals("node")) in.nodes[i1].pos.add(correctionIn._mult(1 - t));
	        if (in.nodes[i2].type.equals("node")) in.nodes[i2].pos.add(correctionIn._mult(t));

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
	            if (in.nodes[i1].type.equals("node")) {
	            	in.nodes[i1].vel.add(inImpulse._mult(1 - t));
	            	in.nodes[i1].vel = in.nodes[i1].vel._mult(VDAMP);
	            }
	            
	            if (in.nodes[i2].type.equals("node")) {
	            	in.nodes[i2].vel.add(inImpulse._mult(t));
		            in.nodes[i2].vel = in.nodes[i2].vel._mult(VDAMP);
	            }
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
		
		if (!Game.debug_mode) return;
		
		for (int x = 0; x<this.frame_inds.length; x++) {
			temp = this.to_polygon(this.rest_pos[x], pane, xpos, ypos, location);
			
			g.setColor(Color.orange);
			
			g.drawPolygon(temp[0], temp[1], this.rest_pos[x].length);
		}
		
		for (int x = 0; x<this.nodes.length; x++) {
			this.nodes[x].pos.draw_node(g, pane, xpos, ypos, location, Color.blue);
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
		
		
		for (int x = 0; x<this.springs.length; x++) {
			if (this.springs[x] == null) continue;
			this.springs[x].draw(g, pane, xpos, ypos, location, false);
		}
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
	    int[][] out = new int[2][in.length];
	    
	    for (int x = 0; x < in.length; x++) {
	        Vector2 temp = Vector2.converted_pos(in[x], pane, xpos, ypos, location);
	        out[0][x] = (int)temp.x;
	        out[1][x] = (int)temp.y;
	    }
	    return out;
	}
	
	
	
}
