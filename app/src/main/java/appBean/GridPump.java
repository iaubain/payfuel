package appBean;

import java.util.List;

/**
 * Created by Owner on 5/18/2016.
 */
public class GridPump {
    private int pumpId;
    private String pumpName;
    private List<GridNozzle> nozzles;

    public int getPumpId() {
        return pumpId;
    }

    public void setPumpId(int pumpId) {
        this.pumpId = pumpId;
    }

    public String getPumpName() {
        return pumpName;
    }

    public void setPumpName(String pumpName) {
        this.pumpName = pumpName;
    }

    public List<GridNozzle> getNozzles() {
        return nozzles;
    }

    public void setNozzles(List<GridNozzle> nozzles) {
        this.nozzles = nozzles;
    }
}
