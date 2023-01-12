package ServerAmministratore;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.ArrayList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class StatsHistory {

    @XmlElement(name="statistic")
    private List<Stats> statsList;
    private static StatsHistory instance;

    private StatsHistory() {
        statsList = new ArrayList<Stats>();
    }

    // singleton
    public synchronized static StatsHistory getInstance() {
        if(instance==null)
            instance = new StatsHistory();
        return instance;
    }

    public List<Stats> getStats() {
        return new ArrayList<>(statsList);
    }

    public synchronized void add(Stats s) {
        statsList.add(s);
    }

    public Stats getByTimestamp(int t) {
        List<Stats> statsCopy = new ArrayList<>(statsList);

        for(Stats s: statsCopy)
            if(s.getTimestamp() == t)
                return s;
        return null;
    }

    public List<Stats> getLastNstats(int n) {
        List<Stats> statsCopy = new ArrayList<>(statsList);
        if(statsCopy.size() >= n) {
            List<Stats> lastNstats = statsCopy.subList(statsCopy.size()-n, statsCopy.size());
            return lastNstats;
        } else
            return statsCopy;
    }

    public float getDelBetweenTimestamp(long t1, long t2) {
        List<Stats> listS = StatsHistory.getInstance().getStats();
        float del = 0;
        int count = 0;

        for (Stats s: listS) {
            if (s.getTimestamp() >= t1 && s.getTimestamp() <= t2) {
                del += s.getDeliveries();
                count += 1;
            }
        }

        if (count == 0) {
            return 0;
        } else {
            return del / count;
        }
    }

    public float getKmBetweenTimestamp(long t1, long t2) {
        List<Stats> listS = StatsHistory.getInstance().getStats();
        float km = 0;
        int count = 0;

        for (Stats s: listS) {
            if (s.getTimestamp() >= t1 && s.getTimestamp() <= t2) {
                km += s.getKilometers();
                count += 1;
            }
        }

        return km / count;
    }

    @Override
    public String toString() {
        return "StatsHistory{" +
                "statsList=" + statsList +
                '}';
    }
}
