package server.logic;

/** Chấm điểm một lượt: gần giá thực hơn +1, hòa mỗi bên +0,5. Không gửi giá (null) = thua lượt. */
public final class ScoreCalculator {
    private ScoreCalculator() {}

    /** @return {điểm người 1, điểm người 2} */
    public static double[] scoreRound(double actual, Double g1, Double g2) {
        if (g1 == null && g2 == null) return new double[]{0.5, 0.5};
        if (g1 == null) return new double[]{0, 1};
        if (g2 == null) return new double[]{1, 0};
        double d1 = Math.abs(g1 - actual);
        double d2 = Math.abs(g2 - actual);
        if (d1 < d2) return new double[]{1, 0};
        if (d2 < d1) return new double[]{0, 1};
        return new double[]{0.5, 0.5};
    }
}
