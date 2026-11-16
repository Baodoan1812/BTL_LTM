package server.logic;

/**
 * Chấm điểm một lượt: đoán cao hơn giá thật thì thua người đoán thấp hơn (hoặc bằng);
 * cùng không cao hơn thì ai chênh ít hơn thắng (+1), bằng nhau mỗi bên +0,5.
 * Cả hai cùng đoán cao hơn: hòa. Không gửi giá (null) = thua lượt.
 */
public final class ScoreCalculator {
    private ScoreCalculator() {}

    /** @return {điểm người 1, điểm người 2} */
    public static double[] scoreRound(double actual, Double g1, Double g2) {
        if (g1 == null && g2 == null) return new double[]{0.5, 0.5};
        if (g1 == null) return new double[]{0, 1};
        if (g2 == null) return new double[]{1, 0};
        boolean over1 = g1 > actual;
        boolean over2 = g2 > actual;
        if (over1 && over2) return new double[]{0.5, 0.5};
        if (over1) return new double[]{0, 1};
        if (over2) return new double[]{1, 0};
        double d1 = actual - g1;
        double d2 = actual - g2;
        if (d1 < d2) return new double[]{1, 0};
        if (d2 < d1) return new double[]{0, 1};
        return new double[]{0.5, 0.5};
    }
}
