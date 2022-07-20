package placement.core;

import java.util.ArrayList;
import java.util.Stack;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathFinder {

    private static final Logger logger = LoggerFactory.getLogger(PathFinder.class);
    private Graph graph;
    ArrayList<Path> allPaths;
    Stack<Node> pathNodes;
    ArrayList<Node> onPath;

    public PathFinder(Graph graph) {
        this.graph = graph;
        this.allPaths = new ArrayList<>();
        pathNodes = new Stack<Node>();
        onPath = new ArrayList<Node>();
    }

    public void computeKShortestPaths(int K) {
        logger.debug("Computing {} shortest paths", K);
        graph.nodes().forEach(srcNode -> {
            graph.nodes().forEach(dstNode -> {
                if (!srcNode.equals(dstNode)) {
                    generatePaths(srcNode, dstNode, K);
                }
            });
        });
    }

    public ArrayList<Path> getAllPaths() {
        return allPaths;
    }

    private void generatePaths(Node srcNode, Node dstNode, int K) {
        pathNodes.push(srcNode);
        onPath.add(srcNode);

        if (!srcNode.equals(dstNode)) {
            srcNode.neighborNodes().forEach(currentNode -> {
                if (!onPath.contains(currentNode)) {
                    if (onPath.size() < K) {
                        generatePaths(currentNode, dstNode, K);
                    }
                }
            });
        } else {
            allPaths.add(generatePath(pathNodes));
        }
        pathNodes.pop();
        onPath.remove(srcNode);

    }

    private Path generatePath(Stack<Node> pathNodes) {
        ArrayList<Node> nodes = new ArrayList<>();
        ArrayList<Edge> edges = new ArrayList<>();
        for (int i = 0; i < pathNodes.size() - 1; i++) {
            Node node = pathNodes.get(i);
            nodes.add(node);
            for (int j = 0; j < node.edges().count(); j++) {
                Edge edge = (Edge) node.getEdge(j);
                if (edge.getOpposite(node).equals(pathNodes.get(i + 1))) {
                    edges.add(edge);
                    break;
                }
            }
        }
        Path path = new Path();
        for (int i = 0; i < nodes.size(); i++) {
            path.add((Node) nodes.get(i), (org.graphstream.graph.Edge) edges.get(i));
        }
        logger.info("Generated Path {}", path);
        return path;
    }

}
