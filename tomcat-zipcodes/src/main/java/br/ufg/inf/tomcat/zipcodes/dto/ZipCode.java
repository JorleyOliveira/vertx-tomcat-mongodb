package br.ufg.inf.tomcat.zipcodes.dto;

import java.util.ArrayList;
import java.util.List;

public class ZipCode {
    private String _id;
    private String city;
    private List<Double> loc = new ArrayList<Double>();
    private Integer pop;
    private String state;

    public ZipCode setId(String id) {
        this._id = id;
        return this;
    }

    public String get_id() {
        return this._id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getPop() {
        return this.pop;
    }

    public void setPop(Integer pop) {
        this.pop = pop;
    }

    public String getState() {
        return this.state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<Double> getLoc() {
        return this.loc;
    }

    public void setLoc(List<Double> loc) {
        this.loc = loc;
    }
}