package Network;

public class PrintThread extends Thread {
    Node n;
    int delTot = 0;
    double kmTot = 0.0;
    double power = 0;

    public PrintThread(Node n) {
        this.n = n;
    }

    @Override
    public void run() {
        // printing every 10 seconds
        while(true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            delTot = n.getNumDel();
            kmTot = n.getDelKm();
            power = n.getDrone().getBattery();

            System.out.println("\nTotal number of deliveries: " + delTot + "\nKilometers traveled: "
                    + String.format("%.2f", kmTot) + "\nRemaining power: " + String.format("%.2f", power) + "\n");
        }
    }
}
