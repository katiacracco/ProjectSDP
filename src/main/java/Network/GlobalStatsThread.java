package Network;

public class GlobalStatsThread extends Thread {
    Node master;

    public GlobalStatsThread(Node n) {
        master = n;
    }

    // sending global statistics to the SA every 10 seconds
    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // sending global statistics to the SA if there are
            if (master.getDel() > 0) {
                master.sendGlobalStats();
            }
        }
    }

}
