package utils;

import java.util.ArrayList;
import java.util.Comparator;

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
        this.K = 5;
        Node n = graphCopy.getNode(0);
        Node m = graphCopy.getNode(3);
        this.generateKPathsNtoM(n, m, this.K);

    }

    private void generateKPathsNtoM(Node n, Node m, int K) {
        ArrayList<Path> a = new ArrayList<>();
        ArrayList<Path> b = new ArrayList<>();
        // show graph
        this.showGraph(graphCopy);
        // Determine the shortest path from the source to the sink.
        Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, "result", "weight");
        dijkstra.init(graphCopy);
        dijkstra.setSource(n);
        dijkstra.compute();
        Path a0 = dijkstra.getPath(m);
        a.add(a0);
        dijkstra.clear();
        logger.debug("Dijkstra shortest path from {} to {}: {}", n, m, a0);

        for (int k = 1; k < K; k++) {
            // The spur node ranges from the first node to the next to last node in the
            // previous k-shortest path.
            ArrayList<Edge> removedEdges = new ArrayList<>();
            ArrayList<Node> removedNodes = new ArrayList<>();
            logger.debug("Starting k={} of K={}. A={}", k, K, a);
            for (int i = 0; i < a.get(k - 1).getNodeCount() - 2; i++) {
                logger.debug("Starting sub-iteration {} from {} over path {}", i+1 , a.get(k - 1).getNodeCount() - 2 ,a.get(k - 1));
                // Spur node is retrieved from the previous k-shortest path, k − 1.
                Node spurNode = a.get(k - 1).getNodePath().get(i);
                logger.debug("Spur node is {}", spurNode.getId());

                // The sequence of nodes from the source to the spur node of the previous
                // k-shortest path.
                Path rootPath = this.copyPathToI(a.get(k - 1), i);
                logger.debug("Root path is {}", rootPath);
                for (int j = 0; j < a.size(); j++) {
                    Path p = a.get(j);
                
                    // Remove the links that are part of the previous shortest paths which share the
                    // same root path.
                    if (rootPath.equals(copyPathToI(p, i))) {
                        Node src, dst;
                        src = graphCopy.getNode(p.getNodePath().get(i).getId());
                        dst = graphCopy.getNode(p.getNodePath().get(i + 1).getId());
                        Edge toRemove = src.getEdgeBetween(dst);
                        logger.debug("RootPath {} is equals to {}. Removing edge {} from graph.", rootPath,
                                copyPathToI(p, i), toRemove);
                        removedEdges.add(toRemove);
                        graphCopy.removeEdge(toRemove);
                    }
                }

                for (int j = 0; j < rootPath.getNodeCount(); j++) {
                    Node rootPathNode = rootPath.getNodePath().get(j);
                    if (!spurNode.equals(rootPathNode)) {
                        logger.debug("Removing node {} from graph {}", rootPathNode, graphCopy);
                        removedNodes.add(rootPathNode);
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
                if (!b.contains(totalPath)) {
                    logger.debug("Adding path {}  to B", totalPath);
                    b.add(totalPath);
                }

                // TODO: remove
                sleep();

                logger.debug("Restoring the graph to the previous state.");
                for (Node node : removedNodes) {
                    graphCopy.addNode(node.getId());
                }
                for (Edge edge : removedEdges) {
                    graphCopy.addEdge(edge.getId(), edge.getNode0().getId() , edge.getNode1().getId() , true);
                }
                removedEdges.clear();
                removedNodes.clear();

            }

            if (b.isEmpty()) {
                logger.debug("B is empty! Breaking the iteration k={}", k);
                break;
            }

            logger.debug("Sorting B according to the path cost.");
            b.sort(new Comparator<Path>() {
                @Override
                public int compare(Path p1, Path p2) {
                    int costP1 = cost(p1);
                    int costP2 = cost(p2);
                    return costP1 - costP2;
                }

                private int cost(Path p){
                    int i = 0;
                    for (Edge edge : p.getEdgePath()) {
                        i = i + (int) edge.getAttribute("weight");
                    }
                    return i;
                }
            });

            logger.debug("Adding the lowest cost path {} to A", b.get(0));
            a.add(b.get(0));
            logger.debug("A={}", a);
            
            // In fact we should rather use shift since we are removing the first element
            b.remove(0);
            logger.debug("First element from B was removed: B={}", b);
        }
        System.out.println(a);
    }

    private Path concatenatePath(Path prefixPath, Path suffixPath) {
        logger.debug("Concatenating path {} with {}", prefixPath, suffixPath);
        Path path = new Path();
        ArrayList<Node> allNodes = new ArrayList<>();
        prefixPath.nodes().forEach(n -> allNodes.add(n));
        suffixPath.nodes().forEach(n -> allNodes.add(n));
        path.setRoot(allNodes.get(0));
        Node lastInserted = path.getRoot();
        for (int i = 1; i < allNodes.size(); i++) {
            Node currentNode = allNodes.get(i);
            if (!lastInserted.equals(currentNode)) {
                // path.add(currentNode.getEdgeFrom(lastInserted));
                path.getNodeSet().add(currentNode);
                lastInserted = currentNode;
            }
        }
        logger.debug("The joined path is N={}, E={}", path.getNodePath(), path.getEdgePath());
        return path;
    }

    // private Path computeDijkstra(Graph theGraph, Node srcNode, Node dstNode) {
    //     Dijkstra dijkstraTemp = new Dijkstra(Dijkstra.Element.EDGE, "result", "weight");
    //     dijkstraTemp.init(theGraph);
    //     dijkstraTemp.setSource(srcNode);
    //     dijkstraTemp.compute();
    //     Path path = dijkstraTemp.getPath(dstNode);
    //     dijkstraTemp.clear();
    //     return path;
    // }

    // private Path removeEdge(Path originalPath, Node src, Node dst) {
    //     logger.debug("Removing edge [{}, {}] from path {}.", src, dst, originalPath);
    //     Path newPath = new Path();
    //     originalPath.edges().forEach(e -> {
    //         if ((!e.getSourceNode().equals(src)) || (!e.getTargetNode().equals(dst))) {
    //             newPath.add(e.getSourceNode(), e);
    //         }
    //     });
    //     logger.debug("Updated path: {}", newPath);
    //     return newPath;
    // }

    private Path copyPathToI(Path pathToCopy, int i) {
        Path root = new Path();
        for (int j = 0; j <= i; j++) {
            root.getNodePath().add(pathToCopy.getNodePath().get(j));
        }
        return root;
    }

    // private boolean compareRootPath(Path rootPath, Path compareTo, int i) {
    //     boolean result = true;
    //     for (int j = 0; j <= i; j++) {
    //         Node rootPathNode = rootPath.getNodePath().get(j);
    //         Node compareToNode = compareTo.getNodePath().get(j);
    //         result = result && rootPathNode.equals(compareToNode);
    //     }
    //     logger.debug("Comparing RootPath {} with {}: {}", rootPath, compareTo, result);
    //     return result;
    // }

    private void showGraph(Graph graph) {
        String css = "";
        css = "node { " +
                "fill-mode: none; " +
                "stroke-mode: plain;" +
                "stroke-color: #5585b5;" +
                "stroke-width: 3;" +
                "text-size: 12; " +
                "text-background-mode: rounded-box; " +
                "text-background-color: #bbe4e9;" +
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

    protected void sleep() {
		try { Thread.sleep(1000); } catch (Exception e) {}
	}
}
