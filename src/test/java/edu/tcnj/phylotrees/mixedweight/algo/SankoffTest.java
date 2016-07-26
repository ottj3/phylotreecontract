package edu.tcnj.phylotrees.mixedweight.algo;

import edu.tcnj.phylotrees.mixedweight.data.Node;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;


public class SankoffTest {
    @Test
    public void testSankoff1() {
        int chars = 1;
        Node c1 = new Node("C1", "C");
        Node a1 = new Node("A1", "A");
        Node c2 = new Node("C2", "C");
        Node a2 = new Node("A2", "A");
        Node g1 = new Node("G1", "G");
        Node in1 = new Node("", 1);
        Node.linkNodes(in1, c1);
        Node.linkNodes(in1, a1);
        Node in2 = new Node("", 1);
        Node.linkNodes(in2, a2);
        Node.linkNodes(in2, g1);
        Node in3 = new Node("", 1);
        Node.linkNodes(in3, c2);
        Node.linkNodes(in3, in2);
        Node root = new Node("", 1);
        Node.linkNodes(root, in1);
        Node.linkNodes(root, in3);

        double[][] weights = {
                {0.0, 1.0, 2.5, 2.5},
                {1.0, 0.0, 2.5, 2.5},
                {2.5, 2.5, 0.0, 1.0},
                {2.5, 2.5, 1.0, 0.0}
        };

        assertEquals("Wrong score", 6, Sankoff.bottomUp(root, weights, chars), 0.01);
        List<List<Node>> zeroCost = Sankoff.topDown(root, weights, chars);
        assertEquals("Wrong edge list", 8, zeroCost.size());
        EdgeContractor edgeContractor = new EdgeContractor(weights, chars);
        Set<Node> compacted = edgeContractor.edgeContraction(root);
    }

    @Test
    public void testSankoff2() {
        int chars = 1;
        Node a = new Node("A", "A");
        Node c = new Node("C", "C");
        Node t = new Node("T", "T");
        Node g = new Node("G", "G");
        Node i1 = new Node("", 1);
        Node.linkNodes(i1, a);
        Node.linkNodes(i1, c);
        Node i2 = new Node("", 1);
        Node.linkNodes(i2, t);
        Node.linkNodes(i2, g);
        Node root = new Node("", 1);
        Node.linkNodes(root, i1);
        Node.linkNodes(root, i2);

        double[][] weights = {
                {0.0, 4.0, 3.0, 9.0},
                {4.0, 0.0, 2.0, 4.0},
                {3.0, 2.0, 0.0, 4.0},
                {9.0, 4.0, 4.0, 0.0}
        };
        assertEquals("Wrong score", 9, Sankoff.bottomUp(root, weights, chars), 0.01);
        List<List<Node>> zeroCost = Sankoff.topDown(root, weights, chars);
        assertEquals("Wrong edge list", 3, zeroCost.size());
        EdgeContractor edgeContractor = new EdgeContractor(weights, chars);
        Set<Node> compacted = edgeContractor.edgeContraction(root);
    }
}
