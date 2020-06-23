package model.signal.filter;

public class TrackedObject {
    public double position;
    public double velocity;

    public TrackedObject(double velocity) {
        this.position = 0.0;
        this.velocity = velocity;
    }
}
