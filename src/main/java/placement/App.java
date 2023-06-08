package placement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Stack;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.ah.A;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import placement.core.GraphLoader;
import utils.YenKSP;

public class App {

    @Parameters(separators = "=", commandDescription = "Generate paths for a given topology.")
    public static class CommandPaths {
        @Parameter(names = {"--topology", "-t"}, description = "The toplogy file")
        private String fileName;

        @Parameter(names = {"--kshortestpaths", "-k"}, description = "How many k shortest paths to generate")
        private Integer ksp;

        @Parameter(names = {"--fakepaths", "-f"}, description = "Include fake paths?")
        private Boolean fakePaths = false;

        @Parameter(names = {"--srcTier", "-b"}, description = "Indicate the layer where all paths should begin")
        Integer srcTier = 4;

        @Parameter(names = {"--dstTier", "-e"}, description = "Indicate the layer where all paths should end")
        Integer dstTier = 0;
    }

    @Parameters(separators = "=", commandDescription = "Generate demands to be placed in nsp4j.")
    public static class CommandDemands{
        
        @Parameter(names = {"--demands", "-d"}, description = "How many demands to generate")
        Integer nDemands = 100;

        @Parameter(names = {"--repetitions", "-r"}, description = "How many repetitions of the experiment")
        Integer nRepetitions = 10;

        @Parameter(names = {"--scaling", "-s"}, description = "Indicate whether to scale the demands")
        Boolean scaling = false;
    }


    public static void main(String[] args) {   

        App app = new App();
        CommandPaths cp = new CommandPaths();
        CommandDemands cd = new CommandDemands();
        
        JCommander jc = JCommander.newBuilder()
            .addObject(app)
            .addCommand("paths", cp)
            .addCommand("demands", cd)
            .build();

        jc.parse(args);

        // paths
        if (jc.getParsedCommand().equals("paths")) {
            // generateKsp(filename, ksp, false);
            generateKsp(cp.fileName, cp.ksp, cp.fakePaths, cp.srcTier, cp.dstTier);
        }

        // demands
        if (jc.getParsedCommand().equals("demands")) {
            GenerateServices genServicesClass = new GenerateServices();
            if (!cd.scaling) {
                genServicesClass.execute(cd.nDemands, cd.nRepetitions);
            } else {
                genServicesClass.executeScaling(cd.nDemands, cd.nRepetitions);
            }
        }
    }

    private static void generateKsp(String fileName, int num_ksp, boolean includeFakePaths, int srcTier, int dstTier) {
        Logger logger = LoggerFactory.getLogger(App.class);
        Graph graph = GraphLoader.loadGraph(fileName);

        logger.info("Setting path attributes.");
        graph.edges().forEach(e -> e.setAttribute("weight", 100));
        graph.nodes().forEach(n -> n.setAttribute("label", n.getId()));
        graph.edges().forEach(e -> e.setAttribute("label", "" + (int) e.getAttribute("weight")));

        YenKSP ksp = new YenKSP(graph);
        logger.info("Generating paths.");
        List<List<Node>> paths = ksp.generateKPathsTiers(num_ksp, srcTier, dstTier);
        LinkedHashSet<List<Node>> fakePaths = new LinkedHashSet<List<Node>>();

        if (includeFakePaths) {
            logger.info("Adding the fake destination node for each path.");
            for (List<Node> path : paths) {
                Stack<Node> pathSt = new Stack<Node>();
                pathSt.addAll(path);
                List<Node> currPath = new ArrayList<Node>();
                while (!pathSt.empty()) {
                    currPath.add(pathSt.pop());
                    List<Node> aPath = new ArrayList<Node>(currPath);
                    aPath.add(graph.getNode("999"));

                    if (!pathContained(fakePaths, aPath)) {
                        fakePaths.add(aPath);
                    }
                }
            }
        } else {
            for (List<Node> path : paths) {
                Collections.reverse(path);
                fakePaths.add(path);
            }
        }
        // printPaths(fakePaths);
        logger.info("Saving paths to file.");
        saveToFile(fakePaths);
    }

    private static void saveToFile(LinkedHashSet<List<Node>> fakePaths) {
        Logger logger = LoggerFactory.getLogger(App.class);
        String outputFileName = "fakepaths.txt";
        try {
            File genFile = new File(outputFileName);
            genFile.setWritable(true);
            genFile.setReadable(true);
            FileWriter outputFile = new FileWriter(genFile);
            for (List<Node> path : fakePaths) {
                outputFile.write(path.toString() + System.lineSeparator());
            }            
            String absPath = genFile.getAbsolutePath();
            outputFile.close();
            logger.info("Paths written to {}", absPath);
        } catch (IOException e) {
            System.out.println("Not possible to write the output file.");
            e.printStackTrace();
        }
    }

    private static boolean pathContained(LinkedHashSet<List<Node>> fakePaths, List<Node> aPath) {
        Boolean contains = false;
        for (List<Node> path : fakePaths) {
            contains = contains || comparePaths(path, aPath);
        }
        return contains;
    }

    private static Boolean comparePaths(List<Node> pathA, List<Node> pathB) {
        Boolean equal = true;
        for (int i = 0; i < pathA.size(); i++) {
            equal = equal && (pathA.get(i).getId().compareTo(pathB.get(i).getId()) == 0);
        }
        return equal;
    }

    private static void printPaths(LinkedHashSet<List<Node>> paths) {
        for (List<Node> path : paths) {
            System.out.println(path);
        }
    }

    /**
     * Displays the graph using the native Graphstream vistualization tool basen on
     * Java Swing.
     * 
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
                "text-background-color: #bbe4e9;" +
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
