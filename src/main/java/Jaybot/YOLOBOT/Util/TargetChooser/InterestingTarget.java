package Jaybot.YOLOBOT.Util.TargetChooser;

import core.game.Observation;

/**
 * Datastructure for interesting targets used by the target chooser.
 */
public class InterestingTarget implements Comparable<InterestingTarget> {

    private Observation obs;
    private double priorityValue;
    private int distance;
    private boolean isWinCondition;
    private boolean useActionEffective;
    private boolean scoreIncrease;
    private boolean unseen;


    public InterestingTarget(Observation obs) {
        this.obs = obs;
        isWinCondition = false;
    }

    @Override
    public int compareTo(InterestingTarget o) {
        double diff = o.priorityValue - this.priorityValue;
        if (diff > 0) {
            return 1;
        } else if (diff < 0) {
            return -1;
        }
        return 0;
    }

    /**
     * @return the Observation object from the gvgai framework
     */
    public Observation getObs() {
        return obs;
    }

    /**
     * @return the calculated priority for this InterestingTarget object
     */
    public double getPriorityValue() {
        return priorityValue;
    }

    public void setPriorityValue(double priorityValue) {
        this.priorityValue = priorityValue;
    }

    /**
     * @return the distance from the player to this InterestingTarget object, calculated using the KnowledgeBasedAStar
     */
    int getDistance() {
        return distance;
    }

    void setDistance(int distance) {
        this.distance = distance;
    }

    /**
     * @return is this InterestingTarget necessary to win the game
     */
    public boolean isWinCondition() {
        return isWinCondition;
    }

    public void setWinCondition(boolean isWinCondition) {
        this.isWinCondition = isWinCondition;
    }

    /**
     * @return can the InterestingTarget be used by the USE action
     */
    public boolean isUseable() {
        return useActionEffective;
    }

    public void setIsUseable(boolean useActionEffective) {
        this.useActionEffective = useActionEffective;
    }

    /**
     * @return does the InterestingTarget increase the score
     */
    public boolean isScoreIncrease() {
        return scoreIncrease;
    }

    public void setScoreIncrease(boolean scoreIncrease) {
        this.scoreIncrease = scoreIncrease;
    }

    /**
     * @return was the InterestingTarget seen before, i.e. did a state advance up to a collision of
     * this InterestingTargets observation and the player avatar
     */
    boolean isUnseen() {
        return unseen;
    }

    void setUnseen(boolean unseen) {
        this.unseen = unseen;
    }

}
