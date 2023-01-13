package placement.core;

import java.io.IOException;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.*;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphLoader {

    private static final Logger logger = LoggerFactory.getLogger(GraphLoader.class);
    private static Graph GRAPH_INSTANCE;

    private GraphLoader() {

    }

    public static Graph loadGraph(String file){
        if (GRAPH_INSTANCE == null) {
            GRAPH_INSTANCE = new DefaultGraph("mygraph");
        }
        logger.info("Loading graph from file {} ", file);
        FileSource fs;
        try {
            fs = FileSourceFactory.sourceFor(file);
            fs.addSink(GRAPH_INSTANCE);
            fs.readAll(file);
            logger.info("Graph loaded with {} nodes and {} edges", 
                GRAPH_INSTANCE.getNodeCount(), GRAPH_INSTANCE.getEdgeCount());
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return GRAPH_INSTANCE;
    }

    

    

}
