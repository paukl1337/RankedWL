import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ThreadManager {
    private static RankedWL rwl = new RankedWL();
    private static ScheduledExecutorService scheduler;
    private static volatile boolean isClosing = false;

    public static void startPeriodicTask() {
        if (scheduler == null || scheduler.isShutdown()) {
            scheduler = Executors.newScheduledThreadPool(1);
            Runnable task = () -> {
                if (!isClosing) {
                    System.out.println("Task executed at: " + System.currentTimeMillis());
                    rwl.userInput = RankedWL.gui.name.getText();
                    rwl.doWork();
                    RankedWL.gui.ranking.setText(RankedWL.winsSes + "W" + " - " + RankedWL.lossesSes + "L");
                }
            };
            scheduler.scheduleAtFixedRate(task, 0, 30, TimeUnit.SECONDS);
        }
    }

    public static void stopPeriodicTask() {
        if (scheduler != null) {
            isClosing = true;
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
