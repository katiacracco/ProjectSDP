package Network;

import PollutionSensor.Buffer;
import PollutionSensor.Measurement;

import java.util.*;

public class MeasBuffer implements Buffer {
    public List<Measurement> buffer = new ArrayList<>();

    public MeasBuffer() {
    }

    // filling the buffer up to 8 measurements
    @Override
    public synchronized void addMeasurement(Measurement m) {
        buffer.add(m);

        if (buffer.size() > 8) {
            notify();
        }
    }

    // sending the buffer to the drone and emptying it halfway (sliding window 50%)
    @Override
    public synchronized List<Measurement> readAllAndClean() {
        List<Measurement> copyBuffer;

        try {
            while (buffer.size() < 8)
                wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        copyBuffer = new ArrayList<>(buffer.subList(0, 8));
        buffer.subList(0,4).clear();

        return copyBuffer;
    }
}
