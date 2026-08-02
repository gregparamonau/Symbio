package Logic.Utilities;
//handles all arrays adding / popping as needed

import Logic.Collision.Polygon;
import Logic.Softbody.SoftBody;

//static class
public class ArrayHandler {
	
	public static Object[] object_add(Object[] objs, Object in) {
		Object[] out = new Object[objs.length + 1];
		for (int x = 0; x<objs.length; x++) out[x] = objs[x];
		
		out[objs.length] = in;
		return out;
	}
	
	public static SoftBody[] sb_add(SoftBody[] sbs, SoftBody in) {
		SoftBody[] out = new SoftBody[sbs.length + 1];
		for (int x = 0; x<sbs.length; x++) out[x] = sbs[x];
		
		out[sbs.length] = in;
		return out;
	}
	
	public static Polygon[] pol_add(Polygon[] pols, Polygon in) {
		Polygon[] out = new Polygon[pols.length + 1];
		for (int x = 0; x<pols.length; x++) out[x] = pols[x];
		
		out[pols.length] = in;
		return out;
	}
}
