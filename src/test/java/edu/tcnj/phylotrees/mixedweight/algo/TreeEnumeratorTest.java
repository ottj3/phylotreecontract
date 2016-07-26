package edu.tcnj.phylotrees.mixedweight.algo;

import edu.tcnj.phylotrees.mixedweight.Parser;
import edu.tcnj.phylotrees.mixedweight.data.Node;

import java.util.ArrayList;
import java.util.List;

public abstract class TreeEnumeratorTest {
    public static List<String> testData = new ArrayList<>();

    static {
        testData.add("A:GAGGACCCCAGATATTACGCGGGTCGAACA");
        testData.add("B:GAAGATCCCAGATACTTTGCCGGAGAACAA");
        testData.add("C:GAGGATCCGCGTTACTTTAGCGGTATTCAA");
        testData.add("D:GAGGACCCCCGTTACTTTGCCGGCGAGGCC");
        testData.add("E:GAGGATCCCAGATATTTTGCGGGTGAGGCT");
        testData.add("F:GAAGACCCGCGCTACTTTGCCGGCACCGGC");
//      testData.add("G:GAAGATCC?CGTTTCTTCGCAGGAGAAGCA"); // true sequence with ? = A or G
//      testData.add("G:GAAGATCC{AG}CGTTTCTTCGCAGGAGAA"); // original sequence without last three chars
        testData.add("G:GAAGATCCCAGACGTTTCTTCGCAGGAGAA"); // Angela's replaced { with C and } with A
        testData.add("H:GAAGATCCACGCTACTATGCAGGACCTCAA");
        testData.add("I:GAAGACCCTCGCTATTACGCCGGTCCGCAA");
        testData.add("J:GAGGACCCACGATATTACGCGGGAGAAGGA");
        testData.add("K:GAGGATCCGCGCTACTTTGCCGGCCCGCAG");
        testData.add("L:GAAGACCCGCGATATTTTGCCGGAGAATCA");
        testData.add("M:GAAGATCCTCGATATTTTGCCGGTCCGCAA");
        testData.add("N:GAAGATCCTCGATATTTTGCCGGTCCGCAA");
        testData.add("O:GAAGACCCGCGTTATTTTGCCGGTACCAGC");
        testData.add("P:GAGGACCCGAGAATGTTCGCTGGCGTTGCC");
        testData.add("Q:GAGGATCCTAGGTTTTATGCGGGCGAGGGC");
        testData.add("R:GAAGACCCACGTTATTTCGCCGGCACCAGC");
        testData.add("S:GAGGACCCCAGATATTTTGCGGGTGAGGCT");
        testData.add("T:GAAGACCCGCGTTACTATGCGGGCACAGAT");
        testData.add("U:GAGGACCCGCGTTACTATGCGGGCACAGAC");
        testData.add("V:GAAGACCCGCGTTACTATGCGGGCACAGAT");
        testData.add("W:GAAGACCCGCGCTACTTTGCCGGCACCGGC");
        testData.add("X:AAGGACCCTTGTTATATTTCCGGCCCGCGT");
        testData.add("Y:GAGGACCCGCGCTACTTCGCGGGCGAAGGA");
        testData.add("Z:GAGGACCCGCGTTACTATGCGGGCACAGAT");
    }

    public double[][] weights = {
            {0, 1, 1, 1},
            {1, 0, 1, 1},
            {1, 1, 0, 1},
            {1, 1, 1, 0}
    };
    public List<Node> species = new ArrayList();

    public void getData(int dataSize) {
        List lines = new ArrayList();
        for (int i = 0; i < dataSize; i++) {
            lines.add(testData.get(i));
        }

        Parser parser = new Parser();
        species = parser.speciesList(lines);
    }

}
