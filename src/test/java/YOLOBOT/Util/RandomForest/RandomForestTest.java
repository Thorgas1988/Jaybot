package YOLOBOT.Util.RandomForest;

import Jaybot.YOLOBOT.Util.RandomForest.RandomForest;
import Jaybot.YOLOBOT.Util.Wissensdatenbank.YoloEvent;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Created by Torsten on 23.05.17.
 */
public class RandomForestTest {

    private static Map<byte[], YoloEvent> trainingData = new HashMap<>();
    private static Map<byte[], YoloEvent> validationData = new HashMap<>();

    private static RandomForest forest = new RandomForest(10, 100000);

    @BeforeClass
    public static void setUp() {
        long seed = 1;
        byte noChange = -1;
        int inventoryCount = 10;

        byte[][] inventoryClass1 = generateInventory(inventoryCount, 3, new byte[]{1, 1, -1}, 2, seed);
        byte[][] inventoryClass2 = generateInventory(inventoryCount, 5, new byte[]{-100, 50, -20, 20, 0}, 10, seed);
        byte[][] inventoryClass3 = generateInventory(inventoryCount, 2, new byte[]{10, 10}, 10, seed);
        byte[][] inventoryClass4 = generateInventory(inventoryCount, 4, new byte[]{0, 100, 0, -100}, 20, seed);

        YoloEvent defeat = new YoloEvent();
        defeat.setDefeat(true);

        YoloEvent blocked = new YoloEvent();
        blocked.setBlocked(true);

        YoloEvent victory = new YoloEvent();
        victory.setVictory(true);

        YoloEvent movedNoOtherChange = new YoloEvent();

        YoloEvent movedAndIncScore = new YoloEvent();
        movedAndIncScore.setScoreDelta((byte)10);

        YoloEvent other= new YoloEvent();
        other.setNewIType((byte)5);
        other.setScoreDelta((byte)1);
        other.setSpawns((byte)1);
        other.setAddInventorySlotItem((byte)3);

        // training for defeat class
        trainingData.put(inventoryClass1[0], movedNoOtherChange);
        for (int i=1; i < inventoryCount; i++) {
            trainingData.put(inventoryClass1[i], defeat);
        }

        // training for blocked class
        trainingData.put(inventoryClass2[0], movedNoOtherChange);
        trainingData.put(inventoryClass2[1], movedAndIncScore);
        for (int i=2; i < inventoryCount; i++) {
            trainingData.put(inventoryClass1[i], blocked);
        }

        // training for victory class
        trainingData.put(inventoryClass3[0], movedAndIncScore);
        trainingData.put(inventoryClass3[1], movedAndIncScore);
        for (int i=2; i < inventoryCount; i++) {
            trainingData.put(inventoryClass3[i], victory);
        }

        // training for other class
        trainingData.put(inventoryClass4[0], movedNoOtherChange);
        trainingData.put(inventoryClass4[1], movedAndIncScore);
        for (int i=2; i < inventoryCount; i++) {
            trainingData.put(inventoryClass4[i], other);
        }

        for (Entry<byte[], YoloEvent> entry : trainingData.entrySet()) {
            forest.train(entry.getKey(), entry.getValue());
        }
    }

    private static byte[][] generateInventory(int inventoryCount, int inventorySize, byte[] factor, int variation, long seed) {
        Random rnd = new Random(seed);

        if (factor.length == inventorySize) {
            byte[][] result = new byte[inventoryCount][inventorySize];

            for (int k=0; k<inventoryCount; k++) {

                for (int i = 0; i < inventorySize; i++) {
                    int var = rnd.nextInt(variation) - (variation / 2);
                    result[k][i] = (byte) (factor[i] + var);
                }

            }

            return result;

        } else {
            throw new IllegalArgumentException("The factor array must have the length of the parameter size!");
        }
    }

    @Test
    public void testOnTrainingData() {
        double totalCount = 0;
        double correct = 0;

        for (Entry<byte[], YoloEvent> entry : trainingData.entrySet()) {
            YoloEvent event = forest.getEvent(entry.getKey());

            if (event.equals(entry.getValue()))
                correct++;

            totalCount++;
        }

        System.out.println("Accuracy: " + correct / totalCount * 100);
        assertEquals(78.125, correct / totalCount * 100, 0.01);

    }
}
