
public class Main {
	
	final static Integer DIST_MAX = 10; /* 
								  		 * considering this number as the maximum 
								  		 * distance of relation between actors
								  		 */
	final static Integer FACTOR = 100;  /*
										 * the part of the actors that will be 
										 * chosen for each percentage based on 
										 * their popularity
										 */
	
	public static void main(String[] args) {
		// read in data and initialize graph
        String filename = args[0];
        Graph G = new Graph(filename, "/");
        
        // create popularity data structure
        ST<String, Double> act_popularity = new ST<String, Double>();
        // create distances data structure
        ST<Double, SET<String>> act_distances = new ST<Double, SET<String>>();
        
        // run breadth first search
        String s = "Bacon, Kevin";
        PathFinder finder = new PathFinder(G, s);
        
        // calculate the popularity and distance of each actor
        for (String actor : G.vertices()) {
        	Double dist = (double)finder.distanceTo(actor);
        	Double popularity = G.popularity(actor);
            if (dist % 2 != 0) continue;  // it's a movie vertex
            
            act_popularity.put(actor, popularity);
            
            if (actor.equals(s)) continue;  // it's the same actor
            if (!act_distances.contains(dist/2)) {
            	act_distances.put(dist/2, new SET<String>());
            }
            act_distances.get(dist/2).add(actor);
        }

        // convert distances to percent
        Integer max = act_distances.max().intValue();
        for (Integer d=1; d<=max; d++) {
        	Double percent;
        	if (d < DIST_MAX) {
        		percent = (1- (double)d/(double)DIST_MAX) * 100;
        	}else {
        		percent = 0.0;
        	}
        	
        	act_distances.put(percent, act_distances.get((double)d));
        	act_distances.remove((double)d);
        }
        
        // Trazas para comprobar datos de popularidad
        /*
        for (String act: act_popularity.keys())
            StdOut.println(act + " " + act_popularity.get(act));*/
        
        // Trazas para comprobar datos de distancias y porcentajes
        /*
        StringBuilder str = new StringBuilder();
        for (Double d : act_distances) {
            str.append(d + "%: ");
            for (String act : act_distances.get(d)) {
                str.append(act + " ");
            }
            str.append('\n');
        }
        StdOut.println(str.toString())*/
        
        StringBuilder str = new StringBuilder();
        for (Double p : act_distances) {
            str.append(p + "%: ");
            int numb_act = (int) Math.ceil((double)act_distances.get(p).size()/FACTOR);
            for (int i=1; i<=numb_act; i++) {
            	String actor = "";
            	double pop = 0.0;
            	for (String act : act_distances.get(p)) {
	                if (act_popularity.get(act) > pop) {
	                	pop = act_popularity.get(act);
	                	actor = act;
	                }
	            }
            	str.append(actor + "; ");
            	act_distances.get(p).delete(actor);
            }
            str.append('\n');
        }
        StdOut.println(str.toString());
	}
}