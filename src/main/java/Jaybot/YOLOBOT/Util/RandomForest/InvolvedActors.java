package Jaybot.YOLOBOT.Util.RandomForest;

/**
 * Created by Torsten on 06.06.2017.
 */
public class InvolvedActors {
    private static final int IRRELEVANT = -1;

    int iTypePlayer = IRRELEVANT;
    int iTypeOtherCollisionActor = IRRELEVANT;

    public InvolvedActors(int iTypePlayer, int iTypeOtherCollisionActor) {
        this.iTypePlayer = iTypePlayer;
        this.iTypeOtherCollisionActor = iTypeOtherCollisionActor;
    }

    public int getPlayerIType() {
        return iTypePlayer;
    }

    public int getOtherIType() {
        return iTypeOtherCollisionActor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InvolvedActors that = (InvolvedActors) o;

        if (iTypePlayer != that.iTypePlayer) return false;
        return iTypeOtherCollisionActor == that.iTypeOtherCollisionActor;
    }

    @Override
    public int hashCode() {
        int result = iTypePlayer;
        result = 31 * result + iTypeOtherCollisionActor;
        return result;
    }
}
