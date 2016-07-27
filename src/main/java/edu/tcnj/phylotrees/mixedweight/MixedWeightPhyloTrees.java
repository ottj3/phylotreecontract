package edu.tcnj.phylotrees.mixedweight;

import edu.tcnj.phylotrees.mixedweight.algo.CubicTreeEnumerator;
import edu.tcnj.phylotrees.mixedweight.algo.EdgeContractor;
import edu.tcnj.phylotrees.mixedweight.data.Node;

import java.io.*;
import java.util.*;

//
public class MixedWeightPhyloTrees {

    public static void main(String[] args) {
        try {
            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.println("What would you like to do?");
                System.out.println("1. Find the best tree from a set of input species.");
                System.out.println("2. Compact existing MP trees to their most compact form.");
                System.out.println("3. Enumerate mixed-labelled/multifurcating trees AND cubic trees, and compare times.");
                String ln = sc.nextLine();
                if (ln.matches("1.*")) {
                    (new MixedWeightPhyloTrees()).enumerateCubicFromInput();
                    break;
                } else if (ln.matches("2.*")) {
                    (new MixedWeightPhyloTrees()).onlyContractCubics();
                    break;
                } else if (ln.matches("3.*")) {
                    (new MixedWeightPhyloTrees()).getTimingInfoFromInput();
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("There was an error reading the input file.");
            e.printStackTrace();
        }
    }

//    private Parser parser = new Parser();

    private void enumerateCubicFromInput() throws IOException {
        List<String> rawSpecies = readSpecies();
        List<Node> species = Parser.speciesList(rawSpecies);
        double[][] weights = readWeights();
        System.out.println("Now enumerating cubic trees and contracting them to find"
                + " the most parsimonious, most compact mixed-labelled tree. Note that"
                + " some trees may be duplicates or re-rooted versions of others.");
        System.out.println("(this may take some time)");

        runCubic(species, weights);
    }

    private List<String> readSpecies() throws IOException {
        System.out.println("Reading species input from file \"species.txt\".");
        File file = new File("species.txt");

        FileInputStream fis = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line;
        List<String> rawSpecies = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            rawSpecies.add(line);
        }
        br.close();
        System.out.println("Read " + rawSpecies.size() + " species from file.");
        return rawSpecies;
    }

    private double[][] readWeights() throws IOException {
        System.out.println("Reading weight input from file \"weights.txt\".");
        File file = new File("weights.txt");

        FileInputStream fis = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line;
        double[][] weights = new double[4][4];
        //TODO check validity of file contents?
        for (int i = 0; i < 4; i++) {
            line = br.readLine();
            String[] split = line.split(" ");
            for (int j = 0; j < 4; j++) {
                weights[i][j] = Double.valueOf(split[j]);
            }
        }
        br.close();
        return weights;
    }

    private void runCubic(List<Node> species, double[][] weights) {
        int chars = species.get(0).data.size();
        long before = System.currentTimeMillis();
        CubicTreeEnumerator treeEnumerator = new CubicTreeEnumerator(species, weights, chars);
        Set<Node> mostParsimonious = treeEnumerator.sankoffEnumerate();
        List<Node> mostCompact = compactCubic(mostParsimonious, weights, chars);

        long time = System.currentTimeMillis() - before;
        int mostCompactSize = mostCompact.get(0).size();
        int cubicSize = mostParsimonious.iterator().next().size();
        System.out.println("Cubic Tree Contraction:\n"
                + "Species: " + species.size() + "\n"
                + "Time taken: " + time + "ms\n"
                + "Number of most parsimonious cubic trees: " + mostParsimonious.size() + "\n"
                + "Number of most compacted mixed-labelled trees: " + mostCompact.size() + "\n"
                + "Size of most compact mixed-labelled trees: " + mostCompact.get(0).size()
                + " (" + (cubicSize - mostCompactSize) + " contractions)"
        );
        System.out.println("List of best trees (structure only): ");
        for (Node node : mostCompact) {
            System.out.println(Parser.toString(node));
        }
    }


    private void onlyContractCubics() throws IOException {
        System.out.println("Reading tree input from file \"trees.txt\".");
        File file = new File("trees.txt");

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        String line;
        Set<Node> inTrees = new HashSet<>();
        while ((line = br.readLine()) != null) {
            inTrees.add(Parser.fromString(line));
        }
        br.close();
        List<String> rawSpecies = readSpecies();
        List<Node> species = Parser.speciesList(rawSpecies);
        for (Node inTree : inTrees) {
            Parser.fillNodes(inTree, rawSpecies);
        }

        compactCubic(inTrees, readWeights(), species.get(0).data.size());
    }

    private List<Node> compactCubic(Set<Node> mostParsimonious,
                                               double[][] weights, int chars) {
        List<Node> mostCompact = new ArrayList<>();
        int mostCompactSize = Integer.MAX_VALUE;
        for (Node tree : mostParsimonious) {
            EdgeContractor edgeContractor = new EdgeContractor(weights, chars);
            for (Node compactTree : edgeContractor.edgeContraction(tree)) {
                int thisSize = compactTree.size();
                if (thisSize <= mostCompactSize) {
                    if (compactTree.size() < mostCompactSize) {
                        mostCompact.clear();
                    }
                    mostCompact.add(compactTree);
                    mostCompactSize = thisSize;
                }
            }
        }
        return mostCompact;
    }

    private void getTimingInfoFromInput() throws IOException {
        Scanner sc = new Scanner(System.in);
        int numTrials = 0, minTreeSize = 0, maxTreeSize = 0;
        boolean multithreaded;
        do {
            System.out.print("Enter the number of trials to be run: ");
            try {
                numTrials = sc.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Wrong input format.");
                sc.nextLine();
            }
        } while (numTrials <= 0);
        do {
            System.out.print("Enter the minimum number of species per tree: ");
            try {
                minTreeSize = sc.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Wrong input format.");
                sc.nextLine();
            }
        } while (minTreeSize <= 0);
        do {
            System.out.print("Enter the maximum number of species per tree: ");
            try {
                maxTreeSize = sc.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("Wrong input format.");
                sc.nextLine();
            }
        } while (maxTreeSize < minTreeSize);
        System.out.print("Should this be run on multiple threads? (y or n): ");
        multithreaded = (sc.next().equals("y"));
        System.out.println("Running " + numTrials + " trials for trees from " + minTreeSize + " to " + maxTreeSize + " input species with" + (multithreaded ? "" : "out") + " multithreading.");
        List<String> rawSpecies = readSpecies();
        double[][] weights = readWeights();
        TreeTiming.runTiming(rawSpecies, weights, numTrials, minTreeSize, maxTreeSize, multithreaded);
    }
}