package placement.core;

import java.util.ArrayList;
import java.util.Stack;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PathFinder {

    private static final Logger logger = LoggerFactory.getLogger(PathFinder.class);
    private Graph graph;

    public PathFinder(Graph graph) {
        this.graph = graph;
    }

    public void computeKShortestPaths(int K) {
        logger.debug("Computing {} shortest paths", K);
        ArrayList<Path> paths = new ArrayList<>();
        graph.nodes().forEach(srcNode -> {
            graph.nodes().forEach(dstNode -> {
                if (!srcNode.equals(dstNode)){
                    generatePaths(srcNode, dstNode);
                }
            });
        });
    }

    private void generatePaths(Node srcNode, Node dstNode) {
        logger.debug("Generating paths from {} to {}", srcNode.getId(), dstNode.getId());

        Stack<Node> pathNodes = new Stack<Node>();
        ArrayList<Node> onPath = new ArrayList<Node>();
        // Iterator<? extends Node> k = srcNode.;
        srcNode.getNe
    }

}
