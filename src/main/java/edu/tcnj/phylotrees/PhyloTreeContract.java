package edu.tcnj.phylotrees;

import edu.tcnj.phylotrees.algo.CubicTreeEnumerator;
import edu.tcnj.phylotrees.algo.EdgeContractor;
import edu.tcnj.phylotrees.data.CharacterList;
import edu.tcnj.phylotrees.data.Node;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PhyloTreeContract {

    public static void main(String[] args) {
        try {
            (new PhyloTreeContract()).enumerateCubicFromInput();
            (new PhyloTreeContract()).getTimingInfoFromInput();
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
            System.out.println(parser.toString(node, false));
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
        //TODO decide how these parameters will be set
        int numTrials = 3;
        int minTreeSize = 4;
        int maxTreeSize = 6;
        boolean multithreaded = true;

        List<String> rawSpecies = readSpecies();
        TreeTiming.runTiming(rawSpecies, numTrials, minTreeSize, maxTreeSize, multithreaded);
    }
}