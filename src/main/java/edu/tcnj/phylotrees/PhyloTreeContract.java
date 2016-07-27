package edu.tcnj.phylotrees;

import edu.tcnj.phylotrees.mixedweight.MixedWeightPhyloTrees;
import edu.tcnj.phylotrees.simpleweight.SimpleWeightPhyloTrees;

import java.util.Scanner;

public class PhyloTreeContract {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        do {
            System.out.println("Use Simple weights or Mixed weights?");
            String ln = sc.nextLine();
            if (ln.matches("(s|S)(imple)?.*")) {
                SimpleWeightPhyloTrees.main(args);
                break;
            } else if (ln.matches("(m|M)(ixed)?.*")) {
                MixedWeightPhyloTrees.main(args);
                break;
            }
        } while (true);
    }
}
