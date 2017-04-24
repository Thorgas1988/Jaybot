import core.ArcadeMachine;
import jaybot.Agent;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Created by Torsten on 24.04.17.
 */
public class JenkinsTest {
    private static final String gamesPath = "examples/gridphysics/";

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

    @Test
    public void shouldRunGame() throws IOException {
        String game;
        String gameName;
        String level;

        try (PrintStream out = new PrintStream(new FileOutputStream("./jenkinsTestResult.txt"))) {
            System.setOut(out);

            for (int gameIdx = 0; gameIdx < games.length; gameIdx++) {
                for (int levelIdx = 0; levelIdx < 5; levelIdx++) {
                    gameName = games[gameIdx];
                    game = gamesPath + gameName + ".txt";
                    level = gamesPath + games[gameIdx] + "_lvl" + levelIdx + ".txt";

                    out.write(("Testing Game #" + gameIdx + ": " + gameName + " - Level #" + levelIdx + "\n").getBytes(StandardCharsets.UTF_8));
                    ArcadeMachine.runOneGame(game, level, false, Agent.class.getCanonicalName(), null, seed, 0);
                }
            }

        }

    }
}
