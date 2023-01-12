package ServerAmministratore;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@XmlRootElement
public class ComplexResult {
    List<Drone> drones = new ArrayList<>();
    int[] pos = new int[2];

    public ComplexResult() {}

    public ComplexResult(List<Drone> drones, int[] pos) {
            this.drones = drones;
            this.pos = pos;
    }

    public List<Drone> getDrones() {
        return drones;
    }

    public void setDrones(List<Drone> drones) {
        this.drones = drones;
    }

    public int[] getPos() {
        return pos;
    }

    public void setPos(int[] pos) {
        this.pos = pos;
    }

    @Override
    public String toString() {
        return "ComplexResult{" +
                "drones=" + drones.toString() +
                ", pos=" + Arrays.toString(pos) +
                '}';
    }
}



