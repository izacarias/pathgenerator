package placement;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import placement.core.GraphLoader;
import placement.core.PathFinder;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        Logger logger = LoggerFactory.getLogger(App.class);
        logger.info("Starting the App..." );
        
        // Set the graph filename 
        String fileName = "./src/main/resources/topologies/7nodes.dgs";
        Graph graph = GraphLoader.loadGraph(fileName);
        PathFinder pf = new PathFinder(graph);
        pf.computeAllPaths();
        pf.printAllPaths();
        // Finding paths


        // Displaying the Graph
        graph.setAttribute("ui.stylesheet", "node { fill-color: blue; text-size: 20; text-background-mode: plain;}");
        for (Node node : graph) {
            node.setAttribute("ui.label", node.getId());
        }
        System.setProperty("org.graphstream.ui", "swing");
        graph.display();
    }
}
