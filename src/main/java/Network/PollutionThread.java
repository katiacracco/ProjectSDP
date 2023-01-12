package Network;

import PollutionSensor.Measurement;
import PollutionSensor.PM10Simulator;
import java.util.ArrayList;
import java.util.List;

public class PollutionThread extends Thread {
    Node n;

    public PollutionThread(Node n) {
        this.n = n;
    }

    @Override
    public void run() {
        List<Measurement> buffer;
        MeasBuffer b = new MeasBuffer();

        // starting the sensor to detect the level of pollution
        PM10Simulator sensor = new PM10Simulator(b);
        sensor.start();

        // reading the values in the buffer
        while(true) {
            buffer = new ArrayList<>(b.readAllAndClean());
            float sum = 0;

            // computing the average of the buffer
            for (Measurement m: buffer) {
                sum += m.getValue();
            }
            float avg = sum / 8;

            n.addAvgs(avg);
        }
    }
}
