package br.ufg.inf.vertx.zipcodes.dto;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class ZipCode {

	private String _id;
	
	private String city;
	
	private List<Double> loc = new ArrayList<Double>();
	
	private Integer pop;
	
	private String state;
	
	public JsonObject toJson() {
	    JsonObject json = new JsonObject()
	        .put("city", city)
	        .put("pop", pop)
	        .put("state", state)
	        .put("loc", new JsonArray(loc));
	    if (_id != null && !_id.isEmpty()) {
	      json.put("_id", _id);
	    }
	    return json;
	  }

	public ZipCode(String _id, String city, JsonArray loc, Integer pop, String state) {
		this._id = _id;
		this.city = city;
		this.pop = pop;
		for (int i = 0; i < loc.size(); i++) {
			this.loc.add(loc.getDouble(i));
		}
		this.state = state;
				
	}

	public ZipCode(JsonObject json) {
		if (json.getValue("_id") instanceof JsonObject) {
			this._id = json.getJsonObject("_id").getString("$oid");
		} else {
			this._id = json.getString("_id");
		}
	    this.city = json.getString("city");
	    this.pop = json.getInteger("pop");
	    JsonArray jsonArray = json.getJsonArray("loc");
	    for (int i = 0; i < jsonArray.size(); i++) {
			this.loc.add(jsonArray.getDouble(i));
		}
	    this.state = json.getString("state");
	  }
	
	public ZipCode setId(String id) {
	    this._id = id;
	    return this;
	  }
	
	public String get_id() {
		return _id;
	}

	public void set_id(String _id) {
		this._id = _id;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public Integer getPop() {
		return pop;
	}

	public void setPop(Integer pop) {
		this.pop = pop;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public List<Double> getLoc() {
		return loc;
	}

	public void setLoc(List<Double> loc) {
		this.loc = loc;
	}
	
}
