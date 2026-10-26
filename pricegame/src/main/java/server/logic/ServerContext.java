package server.logic;

import server.dao.*;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/** Gom các dịch vụ dùng chung của server. */
public class ServerContext {
    public interface ProductSource { List<ProductDAO.Product> random(int n) throws Exception; }
    public interface MatchSaver { void save(MatchDAO.MatchRecord m) throws Exception; }

    public final PlayerManager players = new PlayerManager();
    public final ChallengeManager challenges;
    public final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    public final ProductSource products;
    public final MatchSaver matches;
    public final UserDAO userDAO = new UserDAO();
    public final RankingDAO rankingDAO = new RankingDAO();
    public final MatchDAO matchDAO = new MatchDAO();

    /** Thời gian mỗi lượt (giây) và nghỉ giữa các lượt (ms); cho phép chỉnh khi test. */
    public int timeLimitSec = 15;
    public long nextRoundDelayMs = 3000;

    public ServerContext(ProductSource products, MatchSaver matches) {
        this.products = products;
        this.matches = matches;
        this.challenges = new ChallengeManager(this);
    }

    public static ServerContext withDatabase() {
        ProductDAO pd = new ProductDAO();
        MatchDAO md = new MatchDAO();
        return new ServerContext(pd::random, md::save);
    }
}
