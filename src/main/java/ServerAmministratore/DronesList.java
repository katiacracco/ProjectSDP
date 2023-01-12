package ServerAmministratore;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DronesList {

    private List<Drone> drones ;
    private static DronesList instance;

    private DronesList() {
        drones = new ArrayList<Drone>();
    }

    // singleton
    public synchronized static DronesList getInstance() {
        if(instance==null)
            instance = new DronesList();

        return instance;
    }

    public synchronized List<Drone> getDrones() {
        return new ArrayList<>(drones);
    }

    public synchronized void add(Drone d) {
        drones.add(d);
    }

    public Drone getByID(int ID) {
        List<Drone> droneCopy = new ArrayList<>();
        synchronized(drones) {droneCopy.addAll(drones);}
        for(Drone d: droneCopy)
            if(d.getId() == ID)
                return d;
        return null;
    }

    public synchronized void remove(Drone d) {
        drones.remove(d);
    }

    @Override
    public String toString() {
        return "DronesList{" +
                "drones=" + drones +
                '}';
    }
}
