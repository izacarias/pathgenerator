package placement.core;

import java.text.DecimalFormat;

public class Service {

    public enum ServiceType {URLLC, EMBB, MMTC}

    int dmId;
    ServiceType dmClass;
    String dmDescription;
    double dmBandwidth;

    private static final DecimalFormat df = new DecimalFormat("0.0000000");

    public Service(int dmId, ServiceType dmClass, String dmDescription, double dmBandwidth) {
        this.dmId = dmId;
        this.dmClass = dmClass;
        this.dmDescription = dmDescription;
        this.dmBandwidth = dmBandwidth;
    }

    public int getDmId() {
        return dmId;
    }

    public String getDmClass() {
        switch (this.dmClass) {
            case URLLC:
                return "1";
            case EMBB:
                return "2";
            case MMTC:
                return "3";
            default:
                return "";
        }
    }

    public ServiceType getDmClassServiceType(){
        return this.dmClass;
    }

    public String getDmDescription() {
        return dmDescription;
    }

    public String getDmBandwidth() {
        if (dmBandwidth < 0.1){
            return df.format(dmBandwidth);
        } else {
            return String.valueOf(dmBandwidth);
        }
    }

    public Double getDmBandwidth(Boolean doubleType) {
        return this.dmBandwidth;
    }

    public void setDmBandwidth(double bandwidth){
        this.dmBandwidth = bandwidth;
    }

    public Service clone(){
        Service svc = null;
        try {
            svc = (Service) super.clone();
        } catch (CloneNotSupportedException e){
            svc = new Service(this.dmId, this.dmClass, this.dmDescription, this.dmBandwidth);
        }
        return svc;
    }

    public String toString(){
        return this.dmDescription + " with BW: " + String.valueOf(dmBandwidth);
    }
}