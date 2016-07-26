package edu.tcnj.phylotrees.simpleweight;


import edu.tcnj.phylotrees.simpleweight.data.Node;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore
public class ParserTest {

    private Parser parser;
    private Node<Integer> genTree;

    @Before
    public void setup() {
        parser = new Parser();
        genTree = genTestTree();
    }

    @Ignore
    @Test
    public void testSpeciesInput() {
        List<String> lines = new ArrayList<>();
        lines.add("A:GAGGACCCCAGATATTACGCGGGTCGAACA");
        lines.add("B:GAAGATCCCAGATACTTTGCCGGAGAACAA");
        lines.add("C:GAGGATCCGCGTTACTTTAGCGGTATTCAA");
        lines.add("D:GAGGACCCCCGTTACTTTGCCGGCGAGGCC");
        lines.add("E:GAGGATCCCAGATATTTTGCGGGTGAGGCT");
        lines.add("F:GAAGACCCGCGCTACTTTGCCGGCACCGGC");
        lines.add("G:GAAGATCCCAGACGTTTCTTCGCAGGAGAA");
        lines.add("H:GAAGATCCACGCTACTATGCAGGACCTCAA");
        lines.add("I:GAAGACCCTCGCTATTACGCCGGTCCGCAA");
        lines.add("J:GAGGACCCACGATATTACGCGGGAGAAGGA");
        lines.add("K:GAGGATCCGCGCTACTTTGCCGGCCCGCAG");
        lines.add("L:GAAGACCCGCGATATTTTGCCGGAGAATCA");
        lines.add("M:GAAGATCCTCGATATTTTGCCGGTCCGCAA");
        lines.add("N:GAAGATCCTCGATATTTTGCCGGTCCGCAA");
        lines.add("O:GAAGACCCGCGTTATTTTGCCGGTACCAGC");
        lines.add("P:GAGGACCCGAGAATGTTCGCTGGCGTTGCC");
        lines.add("Q:GAGGATCCTAGGTTTTATGCGGGCGAGGGC");
        lines.add("R:GAAGACCCACGTTATTTCGCCGGCACCAGC");
        lines.add("S:GAGGACCCCAGATATTTTGCGGGTGAGGCT");
        lines.add("T:GAAGACCCGCGTTACTATGCGGGCACAGAT");
        lines.add("U:GAGGACCCGCGTTACTATGCGGGCACAGAC");
        lines.add("V:GAAGACCCGCGTTACTATGCGGGCACAGAT");
        lines.add("W:GAAGACCCGCGCTACTTTGCCGGCACCGGC");
        lines.add("X:AAGGACCCTTGTTATATTTCCGGCCCGCGT");
        lines.add("Y:GAGGACCCGCGCTACTTCGCGGGCGAAGGA");
        lines.add("Z:GAGGACCCGCGTTACTATGCGGGCACAGAT");
        List<Node<Character>> nodes = new ArrayList<>();
        List<Set<Character>> worldSet = new ArrayList<>();
        parser.speciesList(lines, nodes, worldSet);
        assertTrue("Species list size", nodes.size() == 26);

        // NOTE may no longer be true to do uninformative character removal
        assertTrue("World set size", worldSet.size() == 30);
        assertTrue("World set contents #0", worldSet.get(0).size() == 2);
        assertTrue("World set contents #11", worldSet.get(11).size() == 4);
    }

    @Test
    public void countSize() {
        String[] treeStrings = {
                "((((((((K:0,((H:0,I:0):0,(N:0)M:0):0):0,C:0):0,(F:0,O:0):0):0,D:0):0,E:0):0,((B:0,G:0):0,L:0):0):0,J:0):0)A:0;",
                "((((((((((H:0,I:0):0,(M:0,N:0):0):0,K:0):0,C:0):0,(F:0,O:0):0):0,D:0):0,((B:0,G:0):0,L:0):0):0,E:0):0,J:0):0)A:0;",
                "(((((((F:0,(((((I:0,(M:0,N:0):0):0,K:0):0,H:0):0,C:0):0,O:0):0):0,D:0):0,L:0):0,(B:0,G:0):0):0,E:0):0,J:0):0)A:0;",
                "((((((((((H:0,I:0):0,(M:0)N:0):0,K:0):0,C:0):0,(F:0,O:0):0):0,D:0):0,((B:0,G:0):0,L:0):0):0,E:0):0,J:0):0)A:0;",
                "((((((((((I:0,(M:0,N:0):0):0,H:0):0,K:0):0,C:0):0,(F:0,O:0):0):0,D:0):0,E:0):0,((B:0,G:0):0,L:0):0):0,J:0):0)A:0;",
                "(((((((((((D:0,(F:0,O:0):0):0,C:0):0,K:0):0,H:0):0,(I:0,(N:0)M:0):0):0,L:0):0,B:0):0,G:0):0,E:0):0,J:0):0)A:0;",
                "(((((((((((D:0,(F:0,O:0):0):0,C:0):0,K:0):0,H:0):0,((I:0,N:0):0)M:0):0,L:0):0,B:0):0,G:0):0,E:0):0,J:0):0)A:0;",
                "(((((((((((D:0,(F:0,O:0):0):0,C:0):0,K:0):0,H:0):0,(I:0,N:0)M:0):0,L:0):0,B:0):0,G:0):0,E:0):0,J:0):0)A:0;",
                "(((((((((H:0,(I:0,(N:0)M:0):0):0,K:0):0,C:0):0,(F:0,O:0):0):0,D:0):0,((B:0,G:0):0,L:0):0):0,E:0):0,J:0):0)A:0;",
                "(((((((((((D:0,(F:0,O:0):0):0,C:0):0,K:0):0,H:0):0,(I:0,(M:0,N:0):0):0):0,L:0):0,B:0):0,G:0):0,E:0):0,J:0):0)A:0;",
                "(((((((F:0,((((K:0,(I:0,(N:0)M:0):0):0,H:0):0,C:0):0,O:0):0):0,D:0):0,L:0):0,(B:0,G:0):0):0,E:0):0,J:0):0)A:0;",
                "((((((((((I:0,(M:0,N:0):0):0,H:0):0,K:0):0,C:0):0,(F:0,O:0):0):0,D:0):0,((B:0,G:0):0,L:0):0):0,E:0):0,J:0):0)A:0;",
                "(((((((((((D:0,(F:0,O:0):0):0,C:0):0,K:0):0,H:0):0,((I:0)M:0,N:0):0):0,L:0):0,B:0):0,G:0):0,E:0):0,J:0):0)A:0;",
                "((((((((((H:0,I:0):0,(M:0,N:0):0):0,K:0):0,C:0):0,(F:0,O:0):0):0,D:0):0,E:0):0,((B:0,G:0):0,L:0):0):0,J:0):0)A:0;",
                "((((((((((H:0,I:0):0,(M:0)N:0):0,K:0):0,C:0):0,(F:0,O:0):0):0,D:0):0,E:0):0,((B:0,G:0):0,L:0):0):0,J:0):0)A:0;",
                "(((((((((((D:0,(F:0,O:0):0):0,C:0):0,K:0):0,H:0):0,((I:0)M:0)N:0):0,L:0):0,B:0):0,G:0):0,E:0):0,J:0):0)A:0;",
                "(((((((((((D:0,(F:0,O:0):0):0,C:0):0,K:0):0,H:0):0,(I:0,(M:0)N:0):0):0,L:0):0,B:0):0,G:0):0,E:0):0,J:0):0)A:0;",
                "(((((((((H:0,(I:0,(N:0)M:0):0):0,K:0):0,C:0):0,(F:0,O:0):0):0,D:0):0,E:0):0,((B:0,G:0):0,L:0):0):0,J:0):0)A:0;",
                "(((((((F:0,(((((I:0,(M:0)N:0):0,K:0):0,H:0):0,C:0):0,O:0):0):0,D:0):0,L:0):0,(B:0,G:0):0):0,E:0):0,J:0):0)A:0;",
                "(((((((((((D:0,(F:0,O:0):0):0,C:0):0,K:0):0,H:0):0,(M:0,(I:0,N:0):0):0):0,L:0):0,B:0):0,G:0):0,E:0):0,J:0):0)A:0;",
                "(((((((((((D:0,(F:0,O:0):0):0,C:0):0,K:0):0,H:0):0,(I:0,M:0,N:0):0):0,L:0):0,B:0):0,G:0):0,E:0):0,J:0):0)A:0;",
                "(((((((((((D:0,(F:0,O:0):0):0,C:0):0,K:0):0,H:0):0,((I:0,M:0):0,N:0):0):0,L:0):0,B:0):0,G:0):0,E:0):0,J:0):0)A:0;",
                "((((((((K:0,((H:0,I:0):0,(N:0)M:0):0):0,C:0):0,(F:0,O:0):0):0,D:0):0,((B:0,G:0):0,L:0):0):0,E:0):0,J:0):0)A:0;",
                "(((((((((((D:0,(F:0,O:0):0):0,C:0):0,K:0):0,H:0):0,(M:0,(I:0)N:0):0):0,L:0):0,B:0):0,G:0):0,E:0):0,J:0):0)A:0;",
                "(((((((((((D:0,(F:0,O:0):0):0,C:0):0,K:0):0,H:0):0,((I:0,M:0):0)N:0):0,L:0):0,B:0):0,G:0):0,E:0):0,J:0):0)A:0;",
                "((((((((((I:0,(M:0)N:0):0,H:0):0,K:0):0,C:0):0,(F:0,O:0):0):0,D:0):0,((B:0,G:0):0,L:0):0):0,E:0):0,J:0):0)A:0;",
                "(((((((((((D:0,(F:0,O:0):0):0,C:0):0,K:0):0,H:0):0,(I:0,M:0)N:0):0,L:0):0,B:0):0,G:0):0,E:0):0,J:0):0)A:0;",
                "(((((((((((D:0,(F:0,O:0):0):0,C:0):0,K:0):0,H:0):0,((I:0)N:0)M:0):0,L:0):0,B:0):0,G:0):0,E:0):0,J:0):0)A:0;",
                "((((((((((I:0,(M:0)N:0):0,H:0):0,K:0):0,C:0):0,(F:0,O:0):0):0,D:0):0,E:0):0,((B:0,G:0):0,L:0):0):0,J:0):0)A:0;"
        };

        int bestSize = Integer.MAX_VALUE;
        List<Node<?>> bestTrees = new ArrayList<>();
        for (String treeString : treeStrings) {
            Node<?> root = parser.fromString(treeString);
            int size = root.size();
            if (size < bestSize) {
                bestSize = size;
                bestTrees.clear();
                bestTrees.add(root);
            } else if (size == bestSize) {
                bestTrees.add(root);
            }
        }
        for (Node<?> tree : bestTrees) {
            System.out.println(parser.toString(tree));
        }
        System.out.println(bestSize);
    }

    @Test
    public void parseSimpleTree() {
        String[] treeStrings = {";", "A;", "(B,C)A;", "(B,C);", "(((D:1)B:1)C:1)A:0;", "((C:1,(B:1,D:1):0):1)A:0;", "((C:0,(E:0,F:0)D:0)B:0)A:0;"};

        for (String treeString : treeStrings) {
            Node<?> root = parser.fromString(treeString);
            String out = parser.toString(root);
            assertEquals("Simple tree re-parsing", treeString, out);
        }

    }

    @Test
    public void parseGenTree() {
        String genOut = parser.toString(genTree);

        Node<?> root = parser.fromString(genOut);
        String genOutAgain = parser.toString(root);

        System.out.println(leaves + " " + genOutAgain);
        assertEquals("Generated tree re-parsing (leaves: " + leaves + ")", genOut, genOutAgain);
    }

    private static Node<Integer> genTestTree() {
        int chars = 5;
        Node<Integer> root = new Node<>("");
        genRecursive(root, chars);
        return root;
    }

    private static int leaves = 0;
    private static final int MAX_LEAVES = 1000;
    private static final double DECAY = 0.5;
    private static void genRecursive(Node<Integer> current, int chars) {
        if (leaves > MAX_LEAVES) return;
        if (Math.random() < DECAY) {
            Node<Integer> left = new Node<>("");
            current.children.add(left);
            left.parent = current;
            genRecursive(left, chars);
        }
        if (Math.random() < DECAY) {
            Node<Integer> right = new Node<>("");
            current.children.add(right);
            right.parent = current;
            genRecursive(right, chars);
        }
        if (current.children.isEmpty()) {
            current.label = String.valueOf(leaves++);
            current.root = Node.sets(chars);
            for (int i = 0; i < chars; i++) {
                current.root.get(i).add((int) (Math.random() * 3));
            }
        }
    }
}
