package br.ufg.inf.vertx.zipcodes.worker;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.List;

import br.ufg.inf.vertx.zipcodes.dto.ZipCode;

public class ZipCodeApiWorker extends AbstractVerticle {

	  public static final String COLLECTION_ZIPCODES = "zips";
	  private MongoClient mongo;

	  @Override
	  public void start(Future<Void> fut) {
		config().put("http.port", 8082);
		config().put("db_name", "vertx");
		config().put("connection_string", "mongodb://192.168.0.103:27017");
	    mongo = MongoClient.createShared(vertx, config());
	    vertx.eventBus().consumer("addZipCodes").handler(this::addZipCodes);
	    vertx.eventBus().consumer("getZipCode").handler(this::getZipCode);
	    vertx.eventBus().consumer("deleteZipCodes").handler(this::deleteZipCodes);
	    vertx.eventBus().consumer("updateZipCodes").handler(this::updateZipCodes);
	    vertx.eventBus().consumer("getAllZipCodes").handler(this::getAllZipCodes);
	  }

	  @Override
	  public void stop() throws Exception {
	    mongo.close();
	  }
	  
	  private void getAllZipCodes(Message<Object> msg) {
		  try {
			   DeliveryOptions opts = new DeliveryOptions();
	 	       opts.addHeader("STATUS", "200");
			   opts.addHeader("MESSAGE", "OK");
			   MultiMap params = msg.headers();
			   if (params.size() > 0 && params.contains("state") || params.contains("city")) {
				   JsonObject matcher = new JsonObject();
			       if (params.contains("state")) {
			    	   matcher.put("state", params.get("state"));
			       }
			       if (params.contains("city")) {
			    	   matcher.put("city", params.get("city"));	
			       }
			       mongo.find(COLLECTION_ZIPCODES, matcher, results -> {
			 	      List<JsonObject> objects = results.result();
			 	      msg.reply(Json.encodePrettily(objects), opts);
			 	    });
			   } else {
			       mongo.find(COLLECTION_ZIPCODES, new JsonObject(), results -> {
			 	      List<JsonObject> objects = results.result();
					  msg.reply(Json.encodePrettily(objects), opts);
			 	    });
			   }
		} catch (Exception e) {
			e.printStackTrace();
			msg.fail(1, e.getMessage());
		}
	   }
	  
	  private void updateZipCodes(Message<Object> msg) {
		  try {
			DeliveryOptions opts = new DeliveryOptions();
			  final String id = msg.headers().get("id");
			  JsonObject json = (JsonObject) msg.body();
			  if (id == null || json == null) {
				  opts.addHeader("STATUS", "404");
				  opts.addHeader("MESSAGE", "Not Found");
				  msg.reply("", opts);
			  } else {
			      mongo.update(COLLECTION_ZIPCODES,
			    	  new JsonObject().put("_id", new JsonObject().put("$oid", id)),	  
			          new JsonObject().put("$set", json),
			          v -> {
			            if (v.failed()) {
			            	opts.addHeader("STATUS", "404");
							opts.addHeader("MESSAGE", "Not Found");
							msg.reply("", opts);
			            } else {
			            	opts.addHeader("STATUS", "200");
							opts.addHeader("MESSAGE", "OK");
							msg.reply("", opts);
			            }
			          });
			    }
		} catch (Exception e) {
			e.printStackTrace();
			msg.fail(1, e.getMessage());
		}
	  }
	  
	  private void deleteZipCodes(Message<Object> msg) {
		  try {
			DeliveryOptions opts = new DeliveryOptions();
			 String id = msg.headers().get("id");
			if (id == null) {
				opts.addHeader("STATUS", "404");
				opts.addHeader("MESSAGE", "Not Found");
				msg.reply("", opts);
			} else {
			  mongo.removeOne(COLLECTION_ZIPCODES, new JsonObject().put("_id", new JsonObject().put("$oid", id)),
			      ar -> {
			    	  if (ar.succeeded()) {
			    		  opts.addHeader("STATUS", "200");
						  opts.addHeader("MESSAGE", "OK");
						  msg.reply("", opts);
					  } else {
						  opts.addHeader("STATUS", "404");
						  opts.addHeader("MESSAGE", "Not Found");
						  msg.reply("", opts);
					  }
			      });
			}
		} catch (Exception e) {
			e.printStackTrace();
			msg.fail(1, e.getMessage());
		}
	  }
	  
	  
	  private void getZipCode(Message<Object> msg) {
		    try {
		    	DeliveryOptions opts = new DeliveryOptions();
				final String id = msg.headers().get("id");
				if (id == null) {
					opts.addHeader("STATUS", "404");
				} else {
					mongo.findOne(COLLECTION_ZIPCODES, new JsonObject().put("_id", new JsonObject().put("$oid", id)), null,
							ar -> {
								if (ar.succeeded()) {
									if (ar.result() == null) {
										opts.addHeader("STATUS", "404").addHeader("MESSAGE", "Not Found");
										msg.reply("", opts);
									} else {
										ZipCode zipcode = new ZipCode(ar.result());
										opts.addHeader("STATUS", "200").addHeader("MESSAGE", "OK");
										msg.reply(Json.encodePrettily(zipcode), opts);
									}
								} else {
									opts.addHeader("STATUS", "404").addHeader("MESSAGE", "Not Found");
									msg.reply("", opts);
								}
							});
				}
			} catch (Exception e) {
				e.printStackTrace();
				msg.fail(1, e.getMessage());
			}
		}
	  
	  private void addZipCodes(Message<Object> msg) {
		  try {
			  DeliveryOptions opts = new DeliveryOptions();
			    mongo.insert(COLLECTION_ZIPCODES, (JsonObject) msg.body(), r ->
			    	 {
			    		if (r.succeeded()) {
			    			opts.addHeader("Location", "http://localhost:8082/api/zipcodes/"+r.result());
			    			opts.addHeader("STATUS", "201");
			    			opts.addHeader("MESSAGE", "Created");
			    			msg.reply("", opts);
			    		} else {
			    			opts.addHeader("STATUS", "409");
			    			opts.addHeader("MESSAGE", "Conflict");
			    			msg.reply("", opts);
						} 		
					});
		} catch (Exception e) {
			e.printStackTrace();
			msg.fail(1, e.getMessage());
		}
	  }
	}
