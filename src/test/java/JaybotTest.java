import Jaybot.YOLOBOT.Agent;
import core.ArcadeMachine;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Random;

/**
 * Created by Torsten on 21.04.17.
 */
public class JaybotTest {
    private static final String gamesPath = "examples/gridphysics/";
    private static final String generateLevelPath = "examples/gridphysics/";

    private static final String[] games = new String[]{
            "aliens", "bait", "blacksmoke", "boloadventures", "boulderchase", "boulderdash", "brainman", "butterflies",
            "cakybaky", "camelRace", "catapults", "chase", "chipschallenge", "chopper", "cookmepasta", "crossfire",
            "defem", "defender", "digdug", "eggomania", "enemycitadel", "escape", "factorymanager", "firecaster",
            "firestorms", "frogs", "gymkhana", "hungrybirds", "iceandfire", "infection", "intersection", "jaws",
            "labyrinth", "lasers", "lasers2", "lemmings", "missilecommand", "modality", "overload", "pacman",
            "painter", "plants", "plaqueattack", "portals", "raceBet2", "realportals", "realsokoban", "roguelike",
            "seaquest", "sheriff", "sokoban", "solarfox", "superman", "surround", "survivezombies", "tercio",
            "thecitadel", "waitforbreakfast", "watergame", "whackamole", "zelda", "zenpuzzle"
    };

    private static final int seed = (new Random()).nextInt();
    private static final byte gameIdx = 61;//18//28//29//31
    private static final byte levelIdx = 1;
    private static String game = gamesPath + games[gameIdx] + ".txt";
    private static String level1 = gamesPath + games[gameIdx] + "_lvl" + levelIdx + ".txt";
    private static final String recordLevelFile = generateLevelPath + games[gameIdx] + "_glvl.txt";
    private static final Object recordActionsFile = null;

    @Test
    @Ignore("Activate to play the game by yourself")
    public void shouldPlayGame() {
        ArcadeMachine.playOneGame(game, level1, (String) recordActionsFile, seed);
    }

    @Test
    //@Ignore
    public void shouldRunGame() {
        ArcadeMachine.runOneGame(game, level1, true, Agent.class.getCanonicalName(), (String) recordActionsFile, seed, 0);
    }

    @Test
    @Ignore("Only activate if you want to test all games at once! Takes a while.")
    public void shouldRunAllGames() {
        for (int i = 0; i < games.length; i++) {
            game = gamesPath + games[i] + ".txt";
            level1 = gamesPath + games[i] + "_lvl" + levelIdx + ".txt";
            ArcadeMachine.runOneGame(game, level1, true, Agent.class.getCanonicalName(), (String) recordActionsFile, seed, 0);
        }
    }

}
