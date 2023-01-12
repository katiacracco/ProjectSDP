package Network;

import Dronazon.Order;
import ServerAmministratore.Drone;
import ServerAmministratore.Stats;
import Util.PostRequest;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Node {
    private Drone drone;
    private boolean flagMaster = false;
    private int[] pos = new int[2];
    private int[] next = {100,0}; //id, port
    private int[] master = new int[2]; //id, port
    private boolean flagParticipant = false; // election
    private boolean flagElection = false;
    private long delTimestamp = 0;
    private int numDel = 0;
    private float delKm = 0;
    private List<Float> pollAvgs = new ArrayList<>();
    private List<Drone> drones = new ArrayList<>();
    private boolean flagClosure = false;
    private boolean flagRecharge = false;
    private boolean wantRecharge = false;
    private long RechargeTs = 0;
    private Object lockDelivery = new Object(); // wait for a drone to do the delivery
    private Object lockWaitOrders = new Object();
    private Object lockEndDelivery = new Object(); // waiting the drone to end its delivery
    private Object lockMqtt = new Object();
    private Object lockEmpty = new Object();
    private Object lockRecharge = new Object();
    private Object lockElection = new Object();
    private Object lockPing = new Object();
    private Object lockAsk = new Object();
    Object l1 = new Object();
    Object l2 = new Object();
    Object l3 = new Object();
    Object l4 = new Object();
    Object l5 = new Object();
    Object l6 = new Object();
    Object l7 = new Object();
    Object l8 = new Object();
    Object l9 = new Object();
    Object l10 = new Object();
    Object l11 = new Object();
    Object l12 = new Object();
    Object l13 = new Object();
    Object l14 = new Object();
    Object l15 = new Object();


    // needed only if the node is the master
    private List<Grid> dronesPos = new ArrayList<>();
    private List<Order> orders = new ArrayList<>();
    private int del = 0;
    private float km = 0;
    private float poll = 0;
    private float pow = 0;


    public Node() {}

    public Node(Drone drone) {
        this.drone = drone;
    }


    public Drone getDrone() {
        return drone;
    }

    public void setDrone(Drone drone) {
        this.drone = drone;
    }

    public void modifyAvailabilty(boolean flag) {
        synchronized (drone) {
            drone.setFlagAvailable(flag);
        }
    }

    public void modifyBattery(float bat) {
        synchronized (drone) {
            drone.setBattery(bat);
        }
    }

    public boolean getFlagMaster() {
        synchronized (l1) {
            return flagMaster;
        }
    }

    public void setFlagMaster(boolean flagMaster) {
        synchronized (l1) {
            this.flagMaster = flagMaster;
        }
    }

    public int[] getPos() {
        synchronized (l2) {
            return pos;
        }
    }

    public void setPos(int[] pos) {
        synchronized (l2) {
            this.pos = pos;
        }
    }

    public int[] getNext() {
        synchronized (l3) {
            return next;
        }
    }

    public void setNext(int[] next) {
        synchronized (l3) {
            this.next = next;
        }
    }

    public int[] getMaster() {
        synchronized (l4) {
            return master;
        }
    }

    public void setMaster(int[] master) {
        synchronized (l4) {
            this.master = master;
        }
    }

    public boolean getFlagParticipant() {
        synchronized (l5) {
            return flagParticipant;
        }
    }

    public void setFlagParticipant(boolean flagParticipant) {
        synchronized (l5) {
            this.flagParticipant = flagParticipant;
        }
    }

    public boolean getFlagElection() {
        return flagElection;
    }

    public void setFlagElection(boolean flagElection) {
        synchronized (l15) {
            this.flagElection = flagElection;
        }
    }

    public long getDelTimestamp() {
        return delTimestamp;
    }

    public void setDelTimestamp(long delTimestamp) {
        this.delTimestamp = delTimestamp;
    }

    public int getNumDel() {
        synchronized (l11) {
            return numDel;
        }
    }

    public void setNumDel(int nd) {
        this.numDel = nd;
    }

    public void addNumDel(int d) {
        synchronized (l11) {
            this.numDel += d;
        }
    }

    public float getDelKm() {
        synchronized (l12) {
            return delKm;
        }
    }

    public void setDelKm(float delKm) {
        this.delKm = delKm;
    }

    public void addDelKm(float delKm) {
        synchronized (l12) {
            this.delKm += delKm;
        }
    }

    public List<Float> getPollAvgs() {
        synchronized (pollAvgs) {
            return pollAvgs;
        }
    }

    public void setPollAvgs(List<Float> pollAvgs) {
        this.pollAvgs = pollAvgs;
    }

    public void addAvgs(float avg) {
        synchronized (pollAvgs) {
            pollAvgs.add(avg);
        }
    }

    public void emptyAvgs() {
        synchronized (pollAvgs) {
            pollAvgs.clear();
        }
    }

    public List<Drone> getDrones() {
        synchronized (drones) {
            return drones;
        }
    }

    public void setDrones(List<Drone> drones) {
        synchronized (this.drones) {
            this.drones = drones;
            Collections.sort(this.drones, (d1, d2) -> {
                return d1.getId() - d2.getId();
            });
        }
    }

    public void addDrones(Drone d) {
        synchronized (drones) {
            drones.add(d);
            Collections.sort(drones, (d1, d2) -> {
                return d1.getId() - d2.getId();
            });
        }
    }

    public boolean getFlagClosure() {
        synchronized (l6) {
            return flagClosure;
        }
    }

    public void setFlagClosure(boolean flagClosure) {
        synchronized (l6) {
            this.flagClosure = flagClosure;
        }
    }

    public boolean getFlagRecharge() {
        synchronized (l13) {
            return flagRecharge;
        }
    }

    public void setFlagRecharge(boolean flagRecharge) {
        synchronized (l13) {
            this.flagRecharge = flagRecharge;
        }
    }

    public boolean getWantRecharge() {
        synchronized (l14) {
            return wantRecharge;
        }
    }

    public void setWantRecharge(boolean wantRecharge) {
        synchronized (l14) {
            this.wantRecharge = wantRecharge;
        }
    }

    public long getRechargeTs() {
        return RechargeTs;
    }

    public void setRechargeTs(long rechargeTs) {
        RechargeTs = rechargeTs;
    }

    public Object getLockDelivery() {
        return lockDelivery;
    }

    public void setLockDelivery(Object lockDelivery) {
        this.lockDelivery = lockDelivery;
    }

    public Object getLockWaitOrders() {
        return lockWaitOrders;
    }

    public void setLockWaitOrders(Object lockWaitOrders) {
        this.lockWaitOrders = lockWaitOrders;
    }

    public Object getLockEndDelivery() {
        return lockEndDelivery;
    }

    public void setLockEndDelivery(Object lockEndDelivery) {
        this.lockEndDelivery = lockEndDelivery;
    }

    public Object getLockMqtt() {
        return lockMqtt;
    }

    public void setLockMqtt(Object sharedLock) {
        this.lockMqtt = sharedLock;
    }

    public Object getLockEmpty() {
        return lockEmpty;
    }

    public void setLockEmpty(Object lockEmpty) {
        this.lockEmpty = lockEmpty;
    }

    public Object getLockRecharge() {
        return lockRecharge;
    }

    public void setLockRecharge(Object lockRecharge) {
        this.lockRecharge = lockRecharge;
    }

    public Object getLockElection() {
        return lockElection;
    }

    public void setLockElection(Object lockElection) {
        this.lockElection = lockElection;
    }

    public Object getLockPing() {
        return lockPing;
    }

    public void setLockPing(Object lockPing) {
        this.lockPing = lockPing;
    }

    public Object getLockAsk() {
        return lockAsk;
    }

    public void setLockAsk(Object lockAsk) {
        this.lockAsk = lockAsk;
    }

    // master

    public List<Grid> getDronesPos() {
        synchronized (dronesPos) {
            return dronesPos;
        }
    }

    public void setDronesPos(List<Grid> dronesPos) {
        synchronized (dronesPos) {
            this.dronesPos = dronesPos;
        }
    }

    public void addDronesPos(Grid g) {
        synchronized (dronesPos) {
            dronesPos.add(g);
            Collections.sort(dronesPos, (d1, d2) -> {
                return d1.getId() - d2.getId();
            });
        }
    }

    public List<Order> getOrders() {
        synchronized (orders) {
            return orders;
        }
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public void addOrder(Order o) {
        synchronized (orders) {
            orders.add(o);
        }
    }

    public void removeOrder(Order o) {
        synchronized (orders) {
            orders.remove(o);
        }
    }

    public int getDel() {
        synchronized (l7) {
            return del;
        }
    }

    public void setDel(int del) {
        this.del = del;
    }

    public void addDel(int d) {
        synchronized (l7) {
            del += d;
        }
    }

    public void removeDel() {
        synchronized (l7) {
            del = 0;
        }
    }

    public float getKm() {
        synchronized (l8) {
            return km;
        }
    }

    public void setKm(float km) {
        this.km = km;
    }

    public void addKm(float k) {
        synchronized (l8) {
            km += k;
        }
    }

    public void removeKm() {
        synchronized (l8) {
            km = 0;
        }
    }

    public float getPoll() {
        synchronized (l9) {
            return poll;
        }
    }

    public void setPoll(float poll) {
        synchronized (l9) {
            this.poll = poll;
        }
    }

    public void addPoll(List<Float> p) {
        synchronized (l9) {
            float sum = 0;
            for (Float m : p) {
                sum += m;
            }
            float avg = sum / p.size();
            if (Double.isNaN(avg)) {
                avg = 0;
            }
            poll += Math.round((avg - avg * 0.1f) * 100.0f) / 100.0f;
        }

    }

    public void removePoll() {
        synchronized (l9) {
            poll = 0;
        }
    }

    public float getPow() {
        synchronized (l10) {
            return pow;
        }
    }

    public void setPow(float pow) {
        this.pow = pow;
    }

    public void addPow(float po) {
        synchronized (l10) {
            pow += po;
        }
    }

    public void removePow() {
        synchronized (l10) {
            pow = 0;
        }
    }

    public void removeStats() {
        removeDel();
        removeKm();
        removePoll();
        removePow();
    }

    public void sendGlobalStats() {
        if (!getFlagMaster()) {
            System.out.println("Not sending global statistics because I'm not the master");
            return;
        }

        float numDrones = (float) this.getDrones().size();
        float d = (float) this.getDel();
        float deliveries = d;
        float km = this.getKm() / d;
        float pollution = this.getPoll() / d;
        float power = this.getPow() / d;
        long timestamp = System.currentTimeMillis();

        if (Double.isNaN(pollution)) {
            pollution = 0;
        }
        this.removeStats();
        Stats gs = new Stats(deliveries, km, pollution, power, timestamp);

        // sending global statistics to the SA
        System.out.println("\nSending global statistics...");
        System.out.println("Del " + String.format("%.2f", deliveries) + "\nKm " + String.format("%.2f", km) + "\nPollution " + String.format("%.2f", pollution) + "\nPower " + String.format("%.2f", power));
        String serverAddress = "http://localhost:1234";
        String postPath = "/drone/add/stats";
        Client client = Client.create();
        ClientResponse response = null;
        response = PostRequest.postStats(client,serverAddress+postPath,gs);
        if (response.getStatus() != 200) {
            System.out.println("Something went wrong! " + response.getStatus());
        }
    }
}
