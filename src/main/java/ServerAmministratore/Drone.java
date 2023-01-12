package ServerAmministratore;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Drone {
    private int id = 0;
    private String ip = "";
    private int port = 0;
    boolean flagAvailable = true; // delivery
    private float battery = 100;

    public Drone() {}

    public Drone(int id, String ip, int port) {
        this.id = id;
        this.ip = ip;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean getFlagAvailable() {
        return flagAvailable;
    }

    public void setFlagAvailable(boolean flagAvailable) {
        this.flagAvailable = flagAvailable;
    }

    public float getBattery() {
        return battery;
    }

    public void setBattery(float battery) {
        this.battery = battery;
    }

    @Override
    public String toString() {
        return "Drone{" +
                "id=" + id +
                ", ip='" + ip + '\'' +
                ", port=" + port +
                ", flagAvailable=" + flagAvailable +
                '}';
    }
}
