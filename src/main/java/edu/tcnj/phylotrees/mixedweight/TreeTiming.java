package edu.tcnj.phylotrees.mixedweight;


import edu.tcnj.phylotrees.mixedweight.algo.CubicTreeEnumerator;
import edu.tcnj.phylotrees.mixedweight.algo.EdgeContractor;
import edu.tcnj.phylotrees.mixedweight.algo.MixedTreeEnumerator;
import edu.tcnj.phylotrees.mixedweight.data.Node;

import java.util.*;
import java.util.concurrent.*;

public class TreeTiming {
    private static Parser parser = new Parser();

    public static void runTiming(List<String> testData, double[][] weights, int numTrials, int minTreeSize, int maxTreeSize, boolean multithreaded) {
        ExecutorService executorService = (multithreaded) ?
                Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())
                : Executors.newSingleThreadExecutor();
        List<Callable<long[]>> callables = new ArrayList<>();
        List<Future<long[]>> futures;

        List<List<String>> dataPerTrial = new ArrayList<>();
        for (int i = 0; i < numTrials; i++) {
            Collections.shuffle(testData);
            dataPerTrial.add(new ArrayList<>(testData));
        }

        for (int i = minTreeSize; i <= maxTreeSize; i++) {
            final int treeSize = i;
            for (int j = 0; j < numTrials; j++) {
                final int trialNum = j;

                final List<Node> species = parser.speciesList(dataPerTrial.get(j).subList(0, treeSize));
                callables.add(new Callable<long[]>() {
                    @Override
                    public long[] call() throws Exception {
                        return runMixed(species, weights, trialNum);
                    }
                });

                final List<Node> species0 = parser.speciesList(dataPerTrial.get(j).subList(0, treeSize));
                callables.add(new Callable<long[]>() {
                    @Override
                    public long[] call() throws Exception {
                        return runCubic(species0, weights, trialNum);
                    }
                });
            }

        }

        try {
            futures = executorService.invokeAll(callables);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("n\tmixed time\tcubic time\t# compact mixed\t# mp cubic\t# compact\t# contractions");
        int numTreeSizes = maxTreeSize - minTreeSize + 1;
        long[][] totalResults = new long[numTreeSizes][7];
        long[][][] deviation = new long[numTreeSizes][numTrials][2];
        for (int i = 0; i < futures.size(); i += 2) {
            long[] res1;
            long[] res2;
            try {
                res1 = futures.get(i).get(); //{n, mixed time, # compact mixed}
                res2 = futures.get(i + 1).get(); //{cubic time, # mp cubic, # compact, # contractions}
                int treeSize = (int) res1[0];
                int index = treeSize - minTreeSize;

                //I'm so sorry.
                totalResults[index][0] += treeSize; //n
                totalResults[index][1] += res1[1]; //mixed time
                deviation[index][(i / 2) % numTrials][0] = res1[1];
                totalResults[index][2] += res2[0]; //cubic time
                deviation[index][(i / 2) % numTrials][1] = res2[0];
                totalResults[index][3] += res1[2]; //# compact mixed
                totalResults[index][4] += res2[1]; //# mp cubic
                totalResults[index][5] += res2[2]; //# compact
                totalResults[index][6] += res2[3]; //# contractions
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < deviation.length; i++) {
            long mixedDeviation = 0;
            long cubicDeviation = 0;
            //            System.out.println("Size: " + (i+treeSizes[0]));
            for (int j = 0; j < deviation[i].length; j++) {
                //                System.out.print(deviation[i][j][0] + " " + deviation[i][j][1] + "\n");
                deviation[i][j][0] -= totalResults[i][1] / numTrials;
                mixedDeviation += Math.pow(deviation[i][j][0], 2);
                deviation[i][j][1] -= totalResults[i][2] / numTrials;
                cubicDeviation += Math.pow(deviation[i][j][1], 2);
            }
            mixedDeviation /= numTrials;
            cubicDeviation /= numTrials;
            deviation[i][0][0] = (long) Math.sqrt(mixedDeviation);
            deviation[i][0][1] = (long) Math.sqrt(cubicDeviation);
        }
        for (int i = 0; i < totalResults.length; i++) {
            System.out.print(Math.round((float) totalResults[i][0] / numTrials) + "\t");
            for (int i1 = 1; i1 < 3; i1++) {
                System.out.print(Math.round((float) totalResults[i][i1] / numTrials) + "\t");
                System.out.print(deviation[i][0][i1 - 1] + "\t");
            }
            for (int i1 = 3; i1 < totalResults[i].length; i1++) {
                System.out.print(((float) totalResults[i][i1] / numTrials) + "\t");
            }
            System.out.println();
        }
    }

    public static long[] runMixed(List<Node> species, double[][] weights, int trialNum) {
        int chars = species.get(0).data.size();
        long before = System.currentTimeMillis();
        MixedTreeEnumerator treeEnumerator = new MixedTreeEnumerator(species, weights, chars);
        Set<Node> mostParsimonious = treeEnumerator.sankoffEnumerate();
        Set<Node> mostCompact = compactMixed(mostParsimonious);


        long time = System.currentTimeMillis() - before;
        System.out.println("Mixed #" + species.size() + "-" + trialNum + "\t" + species.size() + "\t" + time + "\t" + mostCompact.size());
        return new long[]{species.size(), time, mostCompact.size()};
    }

    private static Set<Node> compactMixed(Set<Node> mostParsimonious) {
        Set<Node> mostCompact = new HashSet<>();
        int mostCompactSize = Integer.MAX_VALUE;
        for (Node tree : mostParsimonious) {
            int thisSize = tree.size();
            if (thisSize <= mostCompactSize) {
                if (tree.size() < mostCompactSize) {
                    mostCompact.clear();
                }
                mostCompact.add(tree);
                mostCompactSize = thisSize;
            }
        }
        return mostCompact;
    }

    public static long[] runCubic(List<Node> species, double[][] weights, int trialNum) {
        int chars = species.get(0).data.size();
        long before = System.currentTimeMillis();
        CubicTreeEnumerator treeEnumerator = new CubicTreeEnumerator(species, chars);
        Set<Node> mostParsimonious = treeEnumerator.sankoffEnumerate();
        Set<Node> mostCompact = compactCubic(mostParsimonious, weights, chars);
        int initialSize = mostParsimonious.iterator().next().size();
        int numContractions = initialSize - mostCompact.iterator().next().size();
        long time = System.currentTimeMillis() - before;
        System.out.println("Cubic #" + species.size() + "-" + trialNum + "\t" + species.size() + "\t" + time + "\t" + mostParsimonious.size() +
                "\t" + mostCompact.size() + "\t" + numContractions);
        return new long[]{time, mostParsimonious.size(), mostCompact.size(), numContractions};
    }

    private static Set<Node> compactCubic(Set<Node> mostParsimonious, double[][] weights, int chars) {
        Set<Node> mostCompact = new HashSet<>();
        int mostCompactSize = Integer.MAX_VALUE;
        for (Node tree : mostParsimonious) {
            EdgeContractor edgeContractor = new EdgeContractor(weights, chars);
            Set<Node> compactTrees = edgeContractor.edgeContraction(tree);
            int thisSize = compactTrees.iterator().next().size();
            if (thisSize <= mostCompactSize) {
                if (compactTrees.size() < mostCompactSize) {
                    mostCompact.clear();
                }
                mostCompact.addAll(compactTrees);
                mostCompactSize = thisSize;
            }
        }
        return mostCompact;
    }
}
