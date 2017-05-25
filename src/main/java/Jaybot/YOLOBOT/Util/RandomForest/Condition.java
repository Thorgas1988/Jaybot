package Jaybot.YOLOBOT.Util.RandomForest;

/**
 * Created by Torsten on 17.05.17.
 */
public class Condition {

    protected boolean lowerThan = false;
    protected byte limit = 0;

    protected Condition() {};

    public Condition(boolean lowerThan, byte limit) {
        this.lowerThan = lowerThan;
        this.limit = limit;
    }

    public boolean conditionIsTrue(byte testValue) {
        if (lowerThan) {
            return testValue < limit;
        }
        return testValue > limit;
    }

    public boolean conditionIsFalse(byte testValue) {
        return !conditionIsTrue(testValue);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (lowerThan)
            sb.append(" < ");
        else
            sb.append(" > ");
        sb.append(limit);
        return sb.toString();
    }
}
