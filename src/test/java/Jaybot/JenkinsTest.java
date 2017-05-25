package Jaybot;

import Jaybot.Agent;
import core.ArcadeMachine;
import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by Torsten on 24.04.17.
 */
public class JenkinsTest {
    private static final String endIterations = "=";
    private static final String endGame = "===";
    private static final String endLevel = "==";
    private static final String exception = "=EXCEPTION";
    private static final String game = "=GAME";
    private static final byte[] endIterationsBytes = (endIterations + "\n").getBytes(StandardCharsets.UTF_8);
    private static final byte[] endLevelBytes = (endLevel + "\n").getBytes(StandardCharsets.UTF_8);
    private static final byte[] endGameBytes = (endGame + "\n").getBytes(StandardCharsets.UTF_8);
    private static final byte[] exceptionBytes = (exception + "\n").getBytes(StandardCharsets.UTF_8);
    private static final byte[] newLineBytes = "\n".getBytes(StandardCharsets.UTF_8);

    private static final String SCORE_PREFIX = "SCORE:";
    private static final String INFO_PREFIX = "INFO:";
    private static final String SEED_PREFIX = "SEED:";

    private static final String plainFile = "./jenkinsTestResult.txt";
    private static final String csvFile = "./jenkinsTestResult.csv";

    private static final String gamesPath = "examples/gridphysics/";

    private static final String[] games = new String[]{
            "aliens", "blacksmoke", "boloadventures", "cookmepasta", "dungeon", "enemycitadel",
            "jaws", "pacman", "racebet", "racebet2", "zelda", "zenpuzzle"
    };

    private static int seed;

    @Test
    public void shouldRunGame() throws IOException {
        // Disable System Out for this test
        System.setOut(new PrintStream(new OutputStream() {
            @Override
            public void write(int arg0) throws IOException {
            }
        }));

        try (PrintStream out = new PrintStream(new FileOutputStream(plainFile))) {

            for (int gameIdx = 0; gameIdx < games.length; gameIdx++) {
                for (int levelIdx = 0; levelIdx < 5; levelIdx++) {
                    for (int iteration = 0; iteration < 3; iteration++) {
                        if (!runTestGame(gameIdx, levelIdx, iteration, out)) {
                            continue;
                        }
                        out.write(endIterationsBytes);
                    }
                    out.write(endLevelBytes);
                }
                out.write(endGameBytes);
            }

        }

        createCSV();
    }

    private boolean runTestGame(int gameIdx, int levelIdx, int iterationIdx, PrintStream out) throws IOException {
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

        out.write((INFO_PREFIX + "Game-Name=" + gameName + "#Level-Nr=" + levelIdx + "#Iteration=" + iterationIdx + "\n").getBytes(StandardCharsets.UTF_8));
        seed = (new Random()).nextInt();
        out.write((SEED_PREFIX + seed + "\n").getBytes(StandardCharsets.UTF_8));

        try {
            double[] result = ArcadeMachine.runOneGame(game, level, false, Agent.class.getCanonicalName(), null, seed, 0);

            String strResult;
            if (result != null && result.length > 0) {
                strResult = SCORE_PREFIX + Double.toString(result[0]);
                out.write(strResult.getBytes(StandardCharsets.UTF_8));
            } else {
                strResult = SCORE_PREFIX + "0";
                out.write(strResult.getBytes(StandardCharsets.UTF_8));
            }
            out.write(newLineBytes);
        } catch (Throwable t) {
            out.write(exceptionBytes);
            t.printStackTrace(out);
            out.write(newLineBytes);
        }
        return true;
    }

    private void createCSV() {
        String line;
        Double score = null;
        String seed = null;
        List<String> gameInfo = null;
        List<String> seeds = new LinkedList<>();
        List<Double> scores = new LinkedList<>();


        try (BufferedReader br = new BufferedReader(new FileReader(new File(plainFile)))) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(csvFile)))) {

                bw.write("Game;Level;Seed1;Seed2;Seed3;Score1;Score2;Score3;MinScore;MaxScore;MeanScore;Result1;Result2;Result3;WinPercentage\n");

                while ((line = br.readLine()) != null) {

                    if (line.equals(endIterations)) {
                        if (score != null) {
                            scores.add(score);
                        }
                        if (seed != null) {
                            seeds.add(seed);
                        }
                    } else if (line.equals(endLevel)) {
                        String csvLine = createCsvLine(gameInfo, seeds, scores);
                        bw.write(csvLine.toString());
                        bw.flush();
                        scores = new LinkedList<>();
                        seeds = new LinkedList<>();
                        gameInfo = null;
                    } else if (line.equals(endGame)) {
                        // nothing to do
                    } else if (line.startsWith(SCORE_PREFIX)) {
                        score = parseScoreLine(line);
                    } else if (line.startsWith(SEED_PREFIX)) {
                        seed = parseSeedLine(line);
                    } else if (line.startsWith(INFO_PREFIX)) {
                        List<String> tmpGameInfo = parseInfoLine(line);

                        if (gameInfo == null) {
                            gameInfo = tmpGameInfo;
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> parseInfoLine(String line) {
        List<String> result = new LinkedList<>();
        String[] properties = line.split("#");

        for (int i = 0; i < properties.length - 1; i++) {
            String[] keyVal = properties[i].split("=");
            result.add(keyVal[1]);
        }

        return result;
    }

    private String createCsvLine(List<String> gameInfo, List<String> seeds, List<Double> scores) {
        StringBuilder sb = new StringBuilder();

        for (String info : gameInfo) {
            sb.append(info).append(";");
        }

        for (String seed : seeds) {
            sb.append(seed).append(";");
        }

        double totalScore = 0;
        double minScore = Double.MAX_VALUE;
        double maxScore = Double.MIN_VALUE;
        for (Double score : scores) {
            if (score > maxScore)
                maxScore = score;
            if (score < minScore)
                minScore = score;

            sb.append(score).append(";");
            totalScore += score;
        }
        sb.append(minScore).append(";");
        sb.append(maxScore).append(";");
        sb.append(totalScore / scores.size()).append(";");

        double totalWins = 0;
        for (Double score : scores) {
            if (score > 0) {
                totalWins++;
                sb.append("VICTORY");
            } else {
                sb.append("DEFEAT");
            }
            sb.append(";");
        }
        sb.append(100 * totalWins / scores.size());
        sb.append("\n");

        return sb.toString();
    }

    private Double parseScoreLine(String line) {
        try {
            double score = Double.parseDouble(line.substring(SCORE_PREFIX.length()));
            return score;
        } catch (Exception e) {
            return null;
        }
    }

    private String parseSeedLine(String line) {
        return line.substring(SEED_PREFIX.length());
    }
}
