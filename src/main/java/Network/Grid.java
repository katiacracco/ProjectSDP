package Network;

import java.util.Arrays;

public class Grid {
    int id;
    int[] pos;

    public Grid() {
    }

    public Grid(int id, int[] pos) {
        this.id = id;
        this.pos = pos;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int[] getPos() {
        return pos;
    }

    public void setPos(int[] pos) {
        this.pos = pos;
    }

    @Override
    public String toString() {
        return "Grid{" +
                "id=" + id +
                ", pos=" + Arrays.toString(pos) +
                '}';
    }
}
