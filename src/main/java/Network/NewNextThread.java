package Network;

import ServerAmministratore.Drone;
import java.util.List;

public class NewNextThread extends Thread {
    Node n;

    public NewNextThread(Node n) {
        this.n = n;
    }

    @Override
    public void run() {
        List<Drone> copyD = n.getDrones();
        List<Grid> copyG = n.getDronesPos();
        Drone dr = null;
        Grid gr = null;
        int[] newNext = new int[2];

        // removing the drone from the list of drones
        for(int i=0; i<copyD.size(); i++) {
            if (copyD.get(i).getId() == n.getDrone().getId()) {
                if (i == copyD.size()-1) {
                    dr = copyD.get(0);
                    newNext[0] = copyD.get(1).getId();
                    newNext[1] = copyD.get(1).getPort();
                } else if (i == copyD.size()-2) {
                    dr = copyD.get(i+1);
                    newNext[0] = copyD.get(0).getId();
                    newNext[1] = copyD.get(0).getPort();
                } else {
                    dr = copyD.get(i+1);
                    newNext[0] = copyD.get(i+2).getId();
                    newNext[1] = copyD.get(i+2).getPort();
                }
            }
        }
        copyD.remove(dr);

        // removing the position of the drone from the list of drones' positions
        for(int i=0; i<copyG.size(); i++) {
            if (copyG.get(i).getId() == n.getDrone().getId()) {
                if (i == copyG.size()-1) {
                    gr = copyG.get(0);
                } else {
                    gr = copyG.get(i+1);
                }
            }
        }
        copyG.remove(gr);

        n.setDrones(copyD);
        n.setDronesPos(copyG);

        // setting the new next drone
        n.setNext(newNext);
        System.out.println("Next: " + newNext[0]);
    }
}
