package Dronazon;

import java.util.Arrays;

public class Order {
    private int id;
    private int[] pickupPoint;
    private int[] deliveryPoint;

    public Order() {}

    public Order(int id, int[] pickupPoint, int[] deliveryPoint) {
        this.id = id;
        this.pickupPoint = pickupPoint;
        this.deliveryPoint = deliveryPoint;
    }

    public void setID(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setPickupPoint(int[] pickupPoint) {
        this.pickupPoint = pickupPoint;
    }

    public int[] getPickupPoint() {
        return pickupPoint;
    }

    public void setDeliveryPoint(int[] deliveryPoint) {
        this.deliveryPoint = deliveryPoint;
    }

    public int[] getDeliveryPoint() {
        return deliveryPoint;
    }

    @Override
    public String toString() {
        return "Order{" +
                "ID=" + id +
                ", pickupPoint=" + Arrays.toString(pickupPoint) +
                ", deliveryPoint=" + Arrays.toString(deliveryPoint) +
                '}';
    }
}
