package server.logic;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ScoreCalculatorTest {
    @Test void closerWins() {
        assertArrayEquals(new double[]{1, 0}, ScoreCalculator.scoreRound(1000, 900.0, 500.0));
        assertArrayEquals(new double[]{0, 1}, ScoreCalculator.scoreRound(1000, 500.0, 1100.0));
    }

    @Test void equalDistanceIsDraw() {
        assertArrayEquals(new double[]{0.5, 0.5}, ScoreCalculator.scoreRound(1000, 900.0, 1100.0));
        assertArrayEquals(new double[]{0.5, 0.5}, ScoreCalculator.scoreRound(1000, 1000.0, 1000.0));
    }

    @Test void missingGuessLoses() {
        assertArrayEquals(new double[]{0, 1}, ScoreCalculator.scoreRound(1000, null, 1.0));
        assertArrayEquals(new double[]{1, 0}, ScoreCalculator.scoreRound(1000, 1.0, null));
        assertArrayEquals(new double[]{0.5, 0.5}, ScoreCalculator.scoreRound(1000, null, null));
    }
}
