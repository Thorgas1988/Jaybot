import Jaybot.Agent;
import core.ArcadeMachine;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * Created by Torsten on 24.04.17.
 */
public class JenkinsTest {
    private static final String endIterations = "=\n";
    private static final String endGame = "==\n";
    private static final String endLevel = "===\n";
    private static final String exception = "=EXCEPTION\n";
    private static final String game = "=GAME\n";
    private static final byte[] endIterationsBytes = endIterations.getBytes(StandardCharsets.UTF_8);
    private static final byte[] endLevelBytes = endLevel.getBytes(StandardCharsets.UTF_8);
    private static final byte[] endGameBytes = endGame.getBytes(StandardCharsets.UTF_8);
    private static final byte[] exceptionBytes = exception.getBytes(StandardCharsets.UTF_8);
    private static final byte[] gameBytes = game.getBytes(StandardCharsets.UTF_8);
    private static final byte[] newLineBytes = "\n".getBytes(StandardCharsets.UTF_8);

    private static final String SCORE_PREFIX = "SCORE:";
    private static final String INFO_PREFIX = "INFO:";

    private static final String plainFile = "./jenkinsTestResult.txt";
    private static final String csvFile = "./jenkinsTestResult.csv";

    private static final String gamesPath = "examples/gridphysics/";

    private static final String[] games = new String[]{
            "aliens", "bait" /*, "blacksmoke", "boloadventures", "boulderchase", "boulderdash", "brainman", "butterflies",
            "cakybaky", "camelRace", "catapults", "chase", "chipschallenge", "chopper", "cookmepasta", "crossfire",
            "defem", "defender", "digdug", "eggomania", "enemycitadel", "escape", "factorymanager", "firecaster",
            "firestorms", "frogs", "gymkhana", "hungrybirds", "iceandfire", "infection", "intersection", "jaws",
            "labyrinth", "lasers", "lasers2", "lemmings", "missilecommand", "modality", "overload", "pacman",
            "painter", "plants", "plaqueattack", "portals", "raceBet2", "realportals", "realsokoban", "roguelike",
            "seaquest", "sheriff", "sokoban", "solarfox", "superman", "surround", "survivezombies", "tercio",
            "thecitadel", "waitforbreakfast", "watergame", "whackamole", "zelda", "zenpuzzle" */
    };

    private static final int seed = (new Random()).nextInt();

    @Test
    //@Ignore("Currently a hell lot of data is written into the System.out. But as the results are written into the System.out as well we have a problem...")
    public void shouldRunGame() throws IOException {
        // Disable System Out for this test
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int arg0) throws IOException {}
        }));

        try (PrintStream out = new PrintStream(new FileOutputStream(plainFile))) {

            for (int gameIdx = 0; gameIdx < games.length; gameIdx++) {
                for (int levelIdx = 0; levelIdx < 5; levelIdx++) {
                    for (int iteration = 0; iteration < 3; iteration++) {
                        if (!runTestGame(gameIdx, levelIdx, iteration, out)) {
                            continue;
                        }
                        out.write(endLevelBytes);
                    }
                    out.write(endGameBytes);
                }
                out.write(endIterationsBytes);
            }

        }
    }

    private boolean runTestGame (int gameIdx, int levelIdx, int iterationIdx, PrintStream out) throws IOException {
        String gameName = games[gameIdx];
        String game = gamesPath + gameName + ".txt";
        String level = gamesPath + games[gameIdx] + "_lvl" + levelIdx + ".txt";

        File gameFile = new File(game);
        if (!gameFile.exists()) {
            return false;
        }
        File levelFile = new File(level);
        if (!levelFile.exists()) {
            return false;
        }

        out.write(gameBytes);
        out.write((INFO_PREFIX + "Game-Nr=" + gameIdx + "#Game-Name=" + gameName + "#Level-Nr=" + levelIdx + "#Iteration=" + iterationIdx + "\n").getBytes(StandardCharsets.UTF_8));
        try {
            double[] result = ArcadeMachine.runOneGame(game, level, false, Agent.class.getCanonicalName(), null, seed, 0);

            for (int player = 0; player < result.length; player++) {
                String strResult = SCORE_PREFIX + "Player" + player + "=" + Double.toString(result[player]);
                out.write(strResult.getBytes(StandardCharsets.UTF_8));
                out.write(newLineBytes);
            }
        } catch (Throwable t) {
            out.write(exceptionBytes);
            t.printStackTrace(out);
            out.write(newLineBytes);
        }
        return true;
    }

    private void createCSV() {
        StringBuilder csvLine = new StringBuilder();
        int scoreNumber;
        double totalScore;
        String line;
        
        try (BufferedReader br = new BufferedReader(new FileReader(new File(plainFile)))){
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(csvFile)))) {
                line = br.readLine();

                if (line.equals(endIterations)) {

                } else if (line.equals(endLevel)) {

                } else if(line.equals(endGame)) {

                } else if (line.startsWith(SCORE_PREFIX)) {

                } else if (line.startsWith(INFO_PREFIX)) {

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
