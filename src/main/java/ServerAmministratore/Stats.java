package ServerAmministratore;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Stats {

    private float deliveries;
    private float kilometers;
    private float pollutionLev;
    private float batteryLev;
    private long timestamp;

    public Stats() {}

    public Stats(float deliveries, float km, float pollutionLev, float batteryLev, long timestamp) {
        this.deliveries = deliveries;
        this.kilometers = km;
        this.pollutionLev = pollutionLev;
        this.batteryLev = batteryLev;
        this.timestamp = timestamp;
    }

    public float getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(float deliveries) {
        this.deliveries = deliveries;
    }

    public float getKilometers() {
        return kilometers;
    }

    public void setKilometers(float kilometers) {
        this.kilometers = kilometers;
    }

    public float getPollutionLev() {
        return pollutionLev;
    }

    public void setPollutionLev(float pollutionLev) {
        this.pollutionLev = pollutionLev;
    }

    public float getBatteryLev() {
        return batteryLev;
    }

    public void setBatteryLev(float batteryLev) {
        this.batteryLev = batteryLev;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Stats{" +
                "deliveriesNum=" + String.format("%.2f", deliveries) +
                ", kilometers=" + String.format("%.2f", kilometers) +
                ", pollutionLev=" + String.format("%.2f", pollutionLev) +
                ", batteryLev=" + String.format("%.2f", batteryLev) +
                ", timestamp=" + timestamp +
                '}';
    }
}
