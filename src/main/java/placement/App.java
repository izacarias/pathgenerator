package placement;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Stack;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import placement.core.GraphLoader;
import utils.YenKSP;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args)
    {   
        String filename = new String();
        int ksp = 0;
        
        if (args.length >= 1){
            
            if (Arrays.asList(args).contains("-f")){
                if (args[Arrays.asList(args).indexOf("-f") + 1] == null) {
                    System.out.println("Filename is missing. Please provide a dgs file with the -f <filename>.");
                    System.exit(100);
                } else {
                    filename = args[Arrays.asList(args).indexOf("-f") + 1];
                }

                if(Arrays.asList(args).contains("-n")){
                    if (args[Arrays.asList(args).indexOf("-n") + 1] == null){
                        System.out.println("Number of KSP missing. Please provide the number of KSP to generate with -n <number>.");
                        System.exit(100);
                    } else {
                        ksp = Integer.valueOf(args[Arrays.asList(args).indexOf("-n") + 1].trim());
                    }
                } else {
                    System.out.println("Number of KSP missing. Generating 2 KSPs.");
                    ksp = 2;
                }
                generateKsp(filename, ksp, false);
            }

            if (Arrays.asList(args).contains("-d")){
                System.out.println("Generating services definition.");
                int maxNodes = 100;
                int maxRep = 10;
                if(Arrays.asList(args).contains("-n")){
                    try {
                        maxNodes = Integer.valueOf(args[Arrays.asList(args).indexOf("-n") + 1].trim());
                    } catch (Exception e) {
                        System.out.println("You must provide a number for maximum number of nodes -n <number>");
                    }
                } else {
                    System.out.println("Maximum number of nodes not provided. Using default maxNodes = 100");
                }

                if(Arrays.asList(args).contains("-r")){
                    try {
                        maxRep = Integer.valueOf(args[Arrays.asList(args).indexOf("-r") + 1].trim());
                    } catch (Exception e) {
                        System.out.println("You must provide a number for maximum number of repetitions. Ex: -r <number>");
                    }
                } else {
                    System.out.println("Maximum number of repetitions not provided. Using default maxRep = 10");
                }


                GenerateServices genServicesClass = new GenerateServices();
                genServicesClass.execute(maxNodes, maxRep);
            }

            if (Arrays.asList(args).contains("-t")){
                GenerateServices genServicesClass = new GenerateServices();
                genServicesClass.execute2();
            }
            
        } else {
            System.out.println("Please provide at least one argument to the command line.");
            System.out.println( "-f <dgs file> \t Generate the paths for the specified topology");
            System.exit(101);
        }
        // Set the graph filename 
    }

    private static void generateKsp(String fileName, int num_ksp, boolean includeFakePaths) {
        Logger logger = LoggerFactory.getLogger(App.class);
        Graph graph = GraphLoader.loadGraph(fileName);

        logger.info("Setting path attributes.");
        graph.edges().forEach(e -> e.setAttribute("weight", 100));
        graph.nodes().forEach(n -> n.setAttribute("label", n.getId()));
        graph.edges().forEach(e -> e.setAttribute("label", "" + (int) e.getAttribute("weight")));

        YenKSP ksp = new YenKSP(graph);
        logger.info("Generating paths.");
        List<List<Node>> paths = ksp.generateKPathsTiers(num_ksp);
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
