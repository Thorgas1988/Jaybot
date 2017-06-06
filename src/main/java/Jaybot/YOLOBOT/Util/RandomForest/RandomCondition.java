package Jaybot.YOLOBOT.Util.RandomForest;

import java.util.Random;

/**
 * Created by Torsten on 17.05.17.
 */
public class RandomCondition extends Condition {

    private static final Random rand = new Random(System.currentTimeMillis());

    public RandomCondition(boolean onlyPositive) {
        super();
        lowerThan = rand.nextBoolean();

        byte[] b = new byte[1];
        rand.nextBytes(b);
        limit = b[0];

        if (onlyPositive && limit < 0) {
            limit *= -1;
        }
    }
}
