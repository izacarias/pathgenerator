package placement.core;

import java.util.ArrayList;
import java.util.Iterator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PathFinder {
    
    private static final Logger logger = LoggerFactory.getLogger(PathFinder.class);
    private Graph graph;

    public PathFinder(Graph graph) {
        this.graph = graph;
    }

    public void computeAllPaths() {
        Node srcNode, dstNode;
        // for (int i = 0; i < graph.getNodeCount(); i++) {
        //     for (int j = 0; j < graph.getNodeCount(); j++) {
        //         srcNode = graph.getNode(i);
        //         dstNode = graph.getNode(j);
        //         this.findPaths(srcNode, dstNode);
        //     }
        // }
        srcNode = graph.getNode(0);
        dstNode = graph.getNode(3);
        this.findPaths(srcNode, dstNode, 5);
    }

    private void findPaths(Node srcNode, Node dstNode, int limit) {
        // if (limit == 0)
        //     return;
        // ArrayList<Node> path = new ArrayList<>();
        // if (! srcNode.equals(dstNode)) {
        //     Iterator<? extends Node> k = srcNode.getDepthFirstIterator();
        //     while (k.hasNext()) {
        //         Node nextK = k.next();
        //         path.add(nextK);
        //         if (nextK.equals(dstNode)) {
        //             System.out.println(path);
        //             return;
        //         }
        //         findPaths(nextK, dstNode, limit -1);
        //     }
        // }
    }

    public void printAllPaths() {
        logger.info("Operation not supported yet.");
    }
    
}
