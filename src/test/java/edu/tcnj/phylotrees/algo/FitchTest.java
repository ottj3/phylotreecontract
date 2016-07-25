package edu.tcnj.phylotrees.algo;

import edu.tcnj.phylotrees.data.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FitchTest {

    private int handScored;
    private Node<Integer> testTree;
    private int chars = 0;

    @Before
    public void setup() {
        testTree = makeTestTree();
    }

    @Test
    public void testFitchScoring() {
        int score = Fitch.bottomUp(testTree, chars);
        assertEquals("Fitch score for test tree", handScored, score);
    }

    // TODO parse or generate a more complex tree for scoring
    private Node<Integer> makeTestTree() {
        Node<Integer> root = new Node<>("");
        Node<Integer> left = new Node<>("L");
        Node<Integer> right = new Node<>("R");

        root.children.add(left);
        root.children.add(right);
        left.parent = root;
        right.parent = root;

        chars = 10;
        left.root = Node.sets(chars);
        right.root = Node.sets(chars);
        for (int i = 0; i < chars / 2; i++) {
            left.root.get(i).add(1);
            right.root.get(i).add(2);
        }
        for (int i = chars / 2; i < chars; i++) {
            left.root.get(i).add(1);
            right.root.get(i).add(2);
        }
        handScored = 10;
        return root;
    }

}
