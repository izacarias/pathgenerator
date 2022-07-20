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

        // Setting weights for Shortest Path calculation and labels for visualizations
        graph.edges().forEach(e -> e.setAttribute("weight", 100));
        graph.nodes().forEach(n -> n.setAttribute("label", n.getId()));
        graph.edges().forEach(e -> e.setAttribute("label", "" + (int) e.getAttribute("weight")));

        PathFinder pf = new PathFinder(graph);
        pf.computeKShortestPaths(3);


        // Displaying the Graph
        // showGraph(graph);
    }

    /**
     * Displays the graph using the native Graphstream vistualization tool basen on
     * Java Swing.
     * @param graph
     */
    private static void showGraph(Graph graph) {
        String css = "";
        css = "node { " +
                "fill-mode: none; " +
                "stroke-mode: plain;" + 
                "stroke-color: #5585b5;" + 
                "stroke-width: 3;" +
                "text-size: 12; " +
                "text-background-mode: rounded-box; " +
                "text-background-color: #bbe4e9;"+
                "text-padding: 5px, 4px; " + 
                "text-offset: 0px, 5px;" +
                "text-alignment: under;" +
            "}";
        // setting CSS Style
        graph.setAttribute("ui.stylesheet", css);
        // configuring the rendering engine to swing
        System.setProperty("org.graphstream.ui", "swing");
        graph.display();
    }
}
