package ktu.kaganndemirr.util;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomNumberGenerator {
    private final int MIN_WEIGHT;
    private final int MAX_WEIGHT;

    public RandomNumberGenerator() {
        this.MIN_WEIGHT = 100;
        this.MAX_WEIGHT = Integer.MAX_VALUE;
    }

    public RandomNumberGenerator(int edgeNumber) {
        this.MIN_WEIGHT = 1;
        this.MAX_WEIGHT = edgeNumber;
    }

    public int generateRandomWeightWithWMAX() {
        return ThreadLocalRandom.current().nextInt(MAX_WEIGHT - MIN_WEIGHT + 1) + MIN_WEIGHT;
    }

    public int generateRandomWeightWithINTMAX(){
        int count = MAX_WEIGHT / MIN_WEIGHT;
        int randomIndex = ThreadLocalRandom.current().nextInt(count) + 1;

        return randomIndex * MIN_WEIGHT;
    }

    public int generateRandomWeightWithHeadsOrTails(){
        boolean isHeads = ThreadLocalRandom.current().nextBoolean();

        if (isHeads){
            return MIN_WEIGHT;
        }
        else {
            return MAX_WEIGHT;
        }
    }

    public static List<Double> generateRandomWeightsAVBTTLength(){
        List<Double> possibleValues = new ArrayList<>();
        for (double i = 0; i <= 1.0; i += 0.01) {
            possibleValues.add(i);
        }

        double first = possibleValues.get(ThreadLocalRandom.current().nextInt(possibleValues.size()));

        double remainingTotal = 1.0 - first;

        ArrayList<Double> remainingValues = new ArrayList<>();
        for (double value : possibleValues) {
            if (value <= remainingTotal) {
                remainingValues.add(value);
            }
        }

        double second = remainingValues.get(ThreadLocalRandom.current().nextInt(remainingValues.size()));
        double third = 1.0 - first - second;

        List<Double> weightList = new ArrayList<>();
        weightList.add(first);
        weightList.add(second);
        weightList.add(third);

        return weightList;
    }

    public static List<Double> generateRandomWeightsAVBTTLengthUtil() {
        List<Double> possibleValues = new ArrayList<>();
        for (double i = 0; i <= 1.0; i += 0.01) {
            possibleValues.add(i);
        }

        double first = possibleValues.get(ThreadLocalRandom.current().nextInt(possibleValues.size()));

        double remainingTotal = 1.0 - first;

        ArrayList<Double> remainingValues1 = new ArrayList<>();
        for (double value : possibleValues) {
            if (value <= remainingTotal) {
                remainingValues1.add(value);
            }
        }

        double second = remainingValues1.get(ThreadLocalRandom.current().nextInt(remainingValues1.size()));
        remainingTotal -= second;

        ArrayList<Double> remainingValues2 = new ArrayList<>();
        for (double value : possibleValues) {
            if (value <= remainingTotal) {
                remainingValues2.add(value);
            }
        }

        double third = remainingValues2.get(ThreadLocalRandom.current().nextInt(remainingValues2.size()));
        double fourth = 1.0 - first - second - third;

        List<Double> weightList = new ArrayList<>();
        weightList.add(first);
        weightList.add(second);
        weightList.add(third);
        weightList.add(fourth);

        return weightList;
    }

    public static List<Double> generateRandomWeightsSecureRandomAVBTTLengthSecureRandom() {
        SecureRandom secureRandom = new SecureRandom();

        List<Double> possibleValues = new ArrayList<>();
        for (double i = 0; i <= 1.0; i += 0.01) {
            possibleValues.add(i);
        }

        double first = possibleValues.get(secureRandom.nextInt(possibleValues.size()));

        double remainingTotal = 1.0 - first;

        ArrayList<Double> remainingValues1 = new ArrayList<>();
        for (double value : possibleValues) {
            if (value <= remainingTotal) {
                remainingValues1.add(value);
            }
        }

        double second = remainingValues1.get(secureRandom.nextInt(remainingValues1.size()));
        remainingTotal -= second;

        ArrayList<Double> remainingValues2 = new ArrayList<>();
        for (double value : possibleValues) {
            if (value <= remainingTotal) {
                remainingValues2.add(value);
            }
        }

        double third = remainingValues2.get(secureRandom.nextInt(remainingValues2.size()));
        double fourth = 1.0 - first - second - third;

        List<Double> weightList = new ArrayList<>();
        weightList.add(first);
        weightList.add(second);
        weightList.add(third);
        weightList.add(fourth);

        return weightList;
    }

    public static List<Double> generateRandomWeightsSecureRandomAVBTTLength(){
        SecureRandom secureRandom = new SecureRandom();

        List<Double> possibleValues = new ArrayList<>();
        for (double i = 0; i <= 1.0; i += 0.01) {
            possibleValues.add(i);
        }

        double first = possibleValues.get(secureRandom.nextInt(possibleValues.size()));

        double remainingTotal = 1.0 - first;

        ArrayList<Double> remainingValues = new ArrayList<>();
        for (double value : possibleValues) {
            if (value <= remainingTotal) {
                remainingValues.add(value);
            }
        }

        double second = remainingValues.get(secureRandom.nextInt(remainingValues.size()));
        double third = 1.0 - first - second;

        List<Double> weightList = new ArrayList<>();
        weightList.add(first);
        weightList.add(second);
        weightList.add(third);

        return weightList;
    }

    public static int generateRandomWeightWithHeadsOrTailsWithXORShift(int minWeight, int maxWeight){
        long seed = System.nanoTime();

        seed ^= (seed << 21);
        seed ^= (seed >>> 35);
        seed ^= (seed << 4);

        Random random = new Random(seed);

        boolean isHeads = random.nextBoolean();

        if (isHeads){
            return minWeight;
        }
        else {
            return maxWeight;
        }
    }

    public static int generateRandomWeightWithHeadsOrTailsWithSecureRandom(int minWeight, int maxWeight){
        SecureRandom secureRandom = new SecureRandom();

        boolean isHeads = secureRandom.nextBoolean();

        if (isHeads){
            return minWeight;
        }
        else {
            return maxWeight;
        }
    }

    public static int rouletteWheelDistribution(int size) {
        double r = ThreadLocalRandom.current().nextDouble();
        int index = 0;
        double val = 0.5;
        while (!(r > val) && index < size - 1) {
            index++;
            val = val / 2.0;
        }
        return index;
    }


}
