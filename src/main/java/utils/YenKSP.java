package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.Graphs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class YenKSP {

    /* A copy of the graph is used to apply the algorithm */
    private Graph graphCopy;
    /*
     * Used to restore the graph to the previous state (after removing nodes and
     * edges)
     */
    private Graph graphShadowing;
    private int K;
    private String lengthAttribute;
    private static final Logger logger = LoggerFactory.getLogger(YenKSP.class);

    
    public YenKSP(Graph graph) {
        this(graph, null);
    }

    public YenKSP(Graph graph, String lengthAttribute) {
        this.graphCopy = Graphs.clone(graph);
        this.graphShadowing = Graphs.clone(graph);
        this.lengthAttribute = lengthAttribute;
    }

    public List<List<Node>> generateKPaths(int K) {
        List<List<Node>> result = new ArrayList<>();
        this.K = K;
        graphCopy.nodes().forEach(n -> {
            graphCopy.nodes().forEach(m -> {
                if (!n.getId().equals(m.getId())) {
                    result.addAll((this.generateKPathsNtoM(n.getId(), m.getId(), this.K)));
                }
            });
        });
        return result;
    }

    private ArrayList<List<Node>> generateKPathsNtoM(String srcNodeId, String dstNodeId, int K) {

        logger.info("Generating K={} shortest path from {} to {}.", K, srcNodeId, dstNodeId);
        ArrayList<List<Node>> a = new ArrayList<>();

        // Initialize the set to store the potential kth shortest path.
        ArrayList<List<Node>> b = new ArrayList<>();

        // Determine the shortest path from the source to the sink.
        List<Node> a0 = this.computeDijkstra(graphCopy, srcNodeId, dstNodeId);
        a.add(a0);

        for (int k = 1; k < K; k++) {
            ArrayList<Edge> removedEdges = new ArrayList<>();
            ArrayList<Node> removedNodes = new ArrayList<>();
            logger.debug("Starting k={} of K={}. A={}", k, K, a);

            // The spur node ranges from the first node to the next to last node in the
            // previous k-shortest path.
            boolean done = false;
            for (int i = 0; i <= a.get(k - 1).size() - 2 && !done; i++) {
                logger.debug("Starting sub-iteration {} from {} over path {}", i + 1, a.get(k - 1).size() - 2,
                        a.get(k - 1));

                // Spur node is retrieved from the previous k-shortest path, k − 1.
                String spurNodeId = a.get(k - 1).get(i).getId();
                logger.debug("Spur node is {}", spurNodeId);

                // Root path is the sequence of nodes from the source to the spur node on
                // the the previous (k-1) shortest path
                List<Node> rootPath = this.subPath(a.get(k - 1), i);
                logger.debug("Root path is {}", rootPath);

                // For each path p in A
                for (int j = 0; j < a.size(); j++) {
                    List<Node> currPath = a.get(j);

                    if (rootPath.equals(subPath(currPath, i))) {
                        logger.debug("RootPath {} is equals to CurrentPath {}", rootPath, subPath(currPath, i));
                        // Remove the links that are part of the previous shortest paths which share the
                        // same root path.
                        String src = currPath.get(i).getId();
                        String dst = currPath.get(i + 1).getId();
                        Edge removeEdge = graphCopy.getNode(src).getEdgeToward(dst);
                        if (removeEdge != null) {
                            logger.debug("Removing edge {}", removeEdge);
                            // save edge attributes as well?
                            removedEdges.add(removeEdge);
                            graphCopy.removeEdge(removeEdge);
                        }
                    }
                }

                for (int j = 0; j < rootPath.size(); j++) {
                    String rootPathNodeId = rootPath.get(j).getId();
                    if (!spurNodeId.equals(rootPathNodeId)) {
                        logger.debug("Removing node {} from graph", rootPathNodeId);
                        removedNodes.add(graphCopy.getNode(rootPathNodeId));
                        graphCopy.removeNode(rootPathNodeId);
                    }
                }

                // Calculate the spur path from the spur node to the sink.
                List<Node> totalPath = new ArrayList<>();
                List<Node> spurPath = this.computeDijkstra(graphCopy, spurNodeId, dstNodeId);
                if (spurPath.size() > 0) {
                    totalPath = this.concatenatePath(rootPath, spurPath);
                    if (!b.contains(totalPath)) {
                        logger.debug("Adding path {} to B", totalPath);
                        b.add(totalPath);
                    }
                }

                logger.debug("Restoring the graph to the previous state.");
                graphCopy = Graphs.clone(this.graphShadowing);

            }

            if (!b.isEmpty()) {
                logger.debug("Adding first element of B(1)={} to A", b.get(0));
                // Sorting B according to path cost
                Collections.sort(b, new PathComparator(graphCopy, "weight"));
                a.add(b.get(0));
                b.remove(0);
                logger.debug("Value of A={}", a);
                logger.debug("Value of B={}", b);
            } else {
                logger.debug("B is empty! Breaking the iteration k={}", k);
                done = true;
            }

        }
        return a;
    }

    public List<Node> computeDijkstra(Graph g, String source, String destination) {
        logger.debug("Computing Dijkstra shortest path from node {} to node {}.", source, destination);
        Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, "result", this.lengthAttribute);
        dijkstra.init(g);
        dijkstra.setSource(g.getNode(source));
        dijkstra.compute();
        List<Node> result = new ArrayList<Node>();
        for (Node node : dijkstra.getPathNodes(g.getNode(destination))) {
            result.add(0, node);
        }
        dijkstra.clear();
        logger.debug("Shortest path from node {} to node {} is {}.", source, destination, result);
        return result;
    }

    private List<Node> subPath(List<Node> originalPath, int copyTo) {
        List<Node> subPath = new ArrayList<>();
        for (int i = 0; i <= copyTo && i < originalPath.size(); i++) {
            subPath.add(originalPath.get(i));
        }
        return subPath;
    }

    private List<Node> concatenatePath(List<Node> prefixPath, List<Node> suffixPath) {
        logger.debug("Concatenating path {} with {}", prefixPath, suffixPath);
        List<Node> newPath = new ArrayList<>(prefixPath);
        Node lastInserted = newPath.get(newPath.size() - 1);
        for (int i = 0; i < suffixPath.size(); i++) {
            Node currentNode = suffixPath.get(i);
            if (!lastInserted.getId().equals(currentNode.getId())) {
                newPath.add(currentNode);
                lastInserted = currentNode;
            }
        }
        logger.debug("The joined path is N={}", newPath);
        return newPath;
    }

    /* Private class to sort B */

    private class PathComparator implements Comparator<List<Node>> {
        private Graph graph;
        String attribute;

        public PathComparator(Graph g, String attribute) {
            this.graph = g;
            this.attribute = attribute;
        }

        @Override
        public int compare(List<Node> pathList1, List<Node> pathList2) {
            Path path1 = listToPath(pathList1, this.graph);
            Path path2 = listToPath(pathList2, this.graph);
            return (int) this.getPathCost(path1) - this.getPathCost(path2);
        }

        private Path listToPath(List<Node> pathList, Graph g) {
            Path path = new Path();
            // path.setRoot(pathList.get(0));
            for (int i = 0; i < pathList.size() - 1; i++) {
                Node srcNode = g.getNode(pathList.get(i).getId());
                Node dstNode = g.getNode(pathList.get(i + 1).getId());
                path.add(srcNode, srcNode.getEdgeToward(dstNode));
            }
            return path;
        }

        private int getPathCost(Path path) {
            int i = 0;
            for (Edge edge : path.getEdgePath()) {
                i = i + (int) edge.getAttribute(this.attribute);
            }
            return i;
        };

    }

}
