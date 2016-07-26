package edu.tcnj.phylotrees.simpleweight;

import edu.tcnj.phylotrees.simpleweight.algo.CubicTreeEnumerator;
import edu.tcnj.phylotrees.simpleweight.algo.EdgeContractor;
import edu.tcnj.phylotrees.simpleweight.data.CharacterList;
import edu.tcnj.phylotrees.simpleweight.data.Node;

import java.io.*;
import java.util.*;

public class SimpleWeightPhyloTrees {

    public static void main(String[] args) {
        try {
            (new SimpleWeightPhyloTrees()).enumerateCubicFromInput();
            (new SimpleWeightPhyloTrees()).getTimingInfoFromInput();
        } catch (IOException e) {
            System.out.println("There was an error reading the input file.");
            e.printStackTrace();
        }
    }

    private Parser parser = new Parser();

    private void enumerateCubicFromInput() throws IOException {
        List<String> rawSpecies = readSpecies();
        List<Node<Character>> species = new ArrayList<>();
        List<Set<Character>> worldSet0 = new ArrayList<>();
        parser.speciesList(rawSpecies, species, worldSet0);
        CharacterList<Character> worldSet = new CharacterList<>(worldSet0);

        System.out.println("Now enumerating cubic trees and contracting them to find"
                + " the most parsimonious, most compact mixed-labelled tree. Note that"
                + " some trees may be duplicates or re-rooted versions of others.");
        System.out.println("(this may take some time)");

        runCubic(species, worldSet);
    }

    private List<String> readSpecies() throws IOException {
        System.out.println("Reading species input from file \"input.txt\".");
        File file = new File("input.txt");

        FileInputStream fis = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line;
        List<String> rawSpecies = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            rawSpecies.add(line);
        }
        System.out.println("Read " + rawSpecies.size() + " species from file.");
        return rawSpecies;
    }

    private void runCubic(List<Node<Character>> species, CharacterList<Character> worldSet) {
        int chars = species.iterator().next().root.size();
        long before = System.currentTimeMillis();
        CubicTreeEnumerator<Character> treeEnumerator = new CubicTreeEnumerator<>(species, chars);
        Set<Node<Character>> mostParsimonious = treeEnumerator.fitchEnumerate();
        List<Node<Character>> mostCompact = compactCubic(mostParsimonious, worldSet, chars);

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
        for (Node<Character> node : mostCompact) {
            System.out.println(parser.toString(node));
        }
    }

    private List<Node<Character>> compactCubic(Set<Node<Character>> mostParsimonious,
                                               CharacterList<Character> worldSet, int chars) {
        List<Node<Character>> mostCompact = new ArrayList<>();
        int mostCompactSize = Integer.MAX_VALUE;
        for (Node<Character> tree : mostParsimonious) {
            EdgeContractor<Character> edgeContractor = new EdgeContractor<>(worldSet, chars);
            for (Node<Character> compactTree : edgeContractor.edgeContraction(tree)) {
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
        TreeTiming.runTiming(rawSpecies, numTrials, minTreeSize, maxTreeSize, multithreaded);
    }
}