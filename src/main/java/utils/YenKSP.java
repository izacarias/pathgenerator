package utils;

import java.util.ArrayList;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.Graphs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YenKSP {

    // private Graph graph;
    private Graph graphCopy;
    private int K;
    // private ArrayList<Path> paths;
    private static final Logger logger = LoggerFactory.getLogger(YenKSP.class);

    public void init(Graph graph) {
        // this.graph = graph;
        // cloning the graph to apply Yen's algorithm
        this.graphCopy = Graphs.clone(graph);
    }

    public void generateKPaths(int K) {
        this.K = K;

        // Get the shortest path using Dijkstra
        Node n = graphCopy.getNode(0);
        Node m = graphCopy.getNode(3);
        this.generateKPathsNtoM(n, m, K);

    }

    private void generateKPathsNtoM(Node n, Node m, int K) {
        ArrayList<Path> a = new ArrayList<>();
        ArrayList<Path<int>> b = new ArrayList<>();
        // Determine the shortest path from the source to the sink.
        Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, "result", "weight");
        dijkstra.init(graphCopy);
        dijkstra.setSource(n);
        dijkstra.compute();
        Path a0 = dijkstra.getPath(m);
        a.add(a0);
        dijkstra.clear();
        logger.debug("Dijkstra shortest path from {} to {}: {}", n, m, a0);

        for (int k = 1; k <= K; k++) {
            // The spur node ranges from the first node to the next to last node in the previous k-shortest path.
            logger.debug("Starting Yen iteration: {} of {}", k, K);
            for (int i = 0; i < a.get(k - 1).getNodeCount() - 2; i++) {
                // Spur node is retrieved from the previous k-shortest path, k − 1.
                Node spurNode = a.get(k - 1).getNodePath().get(i);
                logger.debug("Spur node is {}", spurNode.getId());
                
                // The sequence of nodes from the source to the spur node of the previous k-shortest path.
                Path rootPath = this.copyPathToI(a.get(k - 1), i);
                logger.debug("Root path is {}", rootPath);
                for (int j = 0; j < a.size(); j++) {
                    Path p = a.get(j);
                    // Remove the links that are part of the previous shortest paths which share the same root path.
                    if (rootPath.equals(copyPathToI(p, i))) {
                        Node src, dst;
                        src = p.getNodePath().get(i);
                        dst = p.getNodePath().get(i + 1);
                        logger.debug("RootPath {} is equals to {}. Should remove edge [{}, {}].", rootPath,
                                copyPathToI(p, i), src, dst);
                        //  = this.removeEdge(p, src, dst);
                        graphCopy.removeEdge(src.getId(), dst.getId());
                        graphCopy.removeEdge(dst.getId(), src.getId());

                    }
                }

                for (int j = 0; j < rootPath.getNodeCount(); j++) {
                    Node rootPathNode = rootPath.getNodePath().get(j);
                    if (!spurNode.equals(rootPathNode)) {
                        logger.debug("Removing node {} from graph {}", rootPath, graphCopy);
                        graphCopy.removeNode(rootPathNode);
                    } else {
                        logger.debug("Not removing node {}. Root path node and spur node are equals.", rootPathNode);
                    }
                }

                // Calculate the spur path from the spur node to the sink.
                // TODO: Consider also checking if any spurPath found
                Dijkstra dijkstraSpur = new Dijkstra(Dijkstra.Element.EDGE, null, "weight");
                dijkstraSpur.init(graphCopy);
                dijkstraSpur.setSource(spurNode);
                dijkstraSpur.compute();
                Path spurPath = dijkstraSpur.getPath(m);
                logger.debug("Dijkstra shortest path for spur node {} to {}: {}", spurNode, m, spurPath);
                dijkstraSpur.clear();

                // Entire path is made up of the root path and spur path.
                Path totalPath = this.concatenatePath(rootPath, spurPath);
                if (!b.contains(totalPath)){
                    b.add(totalPath);
                }
            }

            if (b.isEmpty()) {
                break;
            }

            // TODO: Remove, just to avoid IndexOutOfBounds error
            break; // temporary
            
        }
    }

    private Path concatenatePath(Path rootPath, Path spurPath) {
        return null;
    }

    private Path computeDijkstra(Graph theGraph, Node srcNode, Node dstNode) {
        Dijkstra dijkstraTemp = new Dijkstra(Dijkstra.Element.EDGE, "result", "weight");
        dijkstraTemp.init(theGraph);
        dijkstraTemp.setSource(srcNode);
        dijkstraTemp.compute();
        Path path = dijkstraTemp.getPath(dstNode);
        dijkstraTemp.clear();
        return path;
    }

    private Path removeEdge(Path originalPath, Node src, Node dst) {
        logger.debug("Removing edge [{}, {}] from path {}.", src, dst, originalPath);
        Path newPath = new Path();
        originalPath.edges().forEach(e -> {
            if ((!e.getSourceNode().equals(src)) || (!e.getTargetNode().equals(dst))) {
                newPath.add(e.getSourceNode(), e);
            }
        });
        logger.debug("Updated path: {}", newPath);
        return newPath;
    }

    private Path copyPathToI(Path pathToCopy, int i) {
        Path root = new Path();
        for (int j = 0; j <= i; j++) {
            root.getNodePath().add(pathToCopy.getNodePath().get(j));
        }
        return root;
    }

    private boolean compareRootPath(Path rootPath, Path compareTo, int i) {
        boolean result = true;
        for (int j = 0; j <= i; j++) {
            Node rootPathNode = rootPath.getNodePath().get(j);
            Node compareToNode = compareTo.getNodePath().get(j);
            result = result && rootPathNode.equals(compareToNode);
        }
        logger.debug("Comparing RootPath {} with {}: {}", rootPath, compareTo, result);
        return result;
    }

    private void showGraph(Graph graph) {
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
        graph.display(false);
    }
}
