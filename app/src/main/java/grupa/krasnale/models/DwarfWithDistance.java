package grupa.krasnale.models;

public class DwarfWithDistance {
    private DwarfModel dwarfModel;
    private Float distance;

    public DwarfWithDistance(DwarfModel dwarfModel, Float distance) {
        this.dwarfModel = dwarfModel;
        this.distance = distance;
    }

    public DwarfModel getDwarfModel() {
        return dwarfModel;
    }

    public Float getDistance() {
        return distance;
    }
}
