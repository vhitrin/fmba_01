package sample;

import javafx.beans.property.*;

public class DataInputString {

    private final StringProperty D1;
    private final StringProperty P1;
    private final FloatProperty V1;
    private final FloatProperty V2;
    private final FloatProperty V3;

    public DataInputString() {
        this(null, null, 0, 0);
    }

    public DataInputString(String D1, String P1, float V1, float V2) {
        this.D1 = new SimpleStringProperty(D1);
        this.P1 = new SimpleStringProperty(P1);
        this.V1 = new SimpleFloatProperty(V1);
        this.V2 = new SimpleFloatProperty(V2);
        this.V3 = new SimpleFloatProperty((V2 / V1) * 100);
    }

    public DataInputString(String D1, String P1, float V1, float V2, float V3) {
        this.D1 = new SimpleStringProperty(D1);
        this.P1 = new SimpleStringProperty(P1);
        this.V1 = new SimpleFloatProperty(V1);
        this.V2 = new SimpleFloatProperty(V2);
        this.V3 = new SimpleFloatProperty(V3);
    }

    public String getD1() {
        return D1.get();
    }

    public StringProperty D1Property() {
        return D1;
    }

    public void setD1(String d1) {
        this.D1.set(d1);
    }

    public String getP1() {
        return P1.get();
    }

    public StringProperty P1Property() {
        return P1;
    }

    public void setP1(String p1) {
        this.P1.set(p1);
    }

    public float getV1() {
        return V1.get();
    }

    public FloatProperty V1Property() {
        return V1;
    }

    public void setV1(float v1) {
        this.V1.set(v1);
    }

    public float getV2() {
        return V2.get();
    }

    public FloatProperty V2Property() {
        return V2;
    }

    public void setV2(float v2) {
        this.V2.set(v2);
    }

    public float getV3() {
        return V3.get();
    }

    public FloatProperty V3Property() {
        return V3;
    }

    public void setV3(float v3) {
        this.V3.set(v3);
    }
}