package br.ufg.inf.vertx.zipcodes.worker;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.List;
import java.util.stream.Collectors;

import br.ufg.inf.vertx.zipcodes.dto.ZipCode;

public class ZipCodeApiVerticle extends AbstractVerticle {

  public static final String COLLECTION_ZIPCODES = "zips";
  private MongoClient mongo;

  @Override
  public void start(Future<Void> fut) {
    // Create a Mongo client
    mongo = MongoClient.createShared(vertx, config());

    Router router = Router.router(vertx);
    router.get("/api/zipcodes").handler(this::getAllZipCodes);
    router.route("/api/zipcodes*").handler(BodyHandler.create());
    router.post("/api/zipcodes").handler(this::addZipCodes);
    router.get("/api/zipcodes/:id").handler(this::getZipCode);
    router.put("/api/zipcodes/:id").handler(this::updateZipCodes);
    router.delete("/api/zipcodes/:id").handler(this::deleteZipCodes);
    
    vertx.createHttpServer().requestHandler(router::accept)
    	 .listen(config().getInteger("http.port", 8080));
  
  }

  @Override
  public void stop() throws Exception {
    mongo.close();
  }


  private void addZipCodes(RoutingContext routingContext) {
    final ZipCode zipCode = new ZipCode(routingContext.getBodyAsJson());
    mongo.insert(COLLECTION_ZIPCODES, zipCode.toJson(), r ->
    	 {
    		if (r.succeeded()) {
    			routingContext.response()
                .setStatusCode(201)
                .putHeader("content-type", "application/json; charset=utf-8")
                .putHeader("Location", "http://localhost:8082/zipcodeapi/"+r.result())
                .end();
    			return;
    		} else {
    			routingContext.response()
                .setStatusCode(409)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(r.cause().getMessage()));
    			return;
			} 		
		});
        
  }
  
	private void getZipCode(RoutingContext routingContext) {
		final String id = routingContext.request().getParam("id");
		if (id == null) {
			routingContext.response().setStatusCode(400).end();
		} else {
			mongo.findOne(COLLECTION_ZIPCODES, new JsonObject().put("_id", new JsonObject().put("$oid", id)), null,		
					ar -> {
						if (ar.succeeded()) {
							if (ar.result() == null) {
								routingContext.response().setStatusCode(404).end();
								return;
							}
							ZipCode zipcode = new ZipCode(ar.result());
							routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
										  .end(Json.encodePrettily(zipcode));
						} else {
							routingContext.response().setStatusCode(404).end();
						}
					});
		}
	}
  
  private void deleteZipCodes(RoutingContext routingContext) {
    String id = routingContext.request().getParam("id");
    if (id == null) {
      routingContext.response().setStatusCode(404).end();
    } else {
      mongo.removeOne(COLLECTION_ZIPCODES, new JsonObject().put("_id", new JsonObject().put("$oid", id)),
          ar -> {
        	  if (ar.succeeded()) {
        		  routingContext.response().setStatusCode(200).end();
        		  return;
			  } else {
				routingContext.response().setStatusCode(404).end();
				return;
			  }
          });
      		
    }
  }

  private void updateZipCodes(RoutingContext routingContext) {
	  final String id = routingContext.request().getParam("id");
	  JsonObject json = routingContext.getBodyAsJson();
	  if (id == null || json == null) {
	     routingContext.response().setStatusCode(404).end();
	  } else {
	      mongo.update(COLLECTION_ZIPCODES,
	    	  new JsonObject().put("_id", new JsonObject().put("$oid", id)),	  
	          new JsonObject().put("$set", json),
	          v -> {
	            if (v.failed()) {
	              routingContext.response().setStatusCode(404).end();
	              return;
	            } else {
	              routingContext.response().setStatusCode(200).putHeader("content-type", "application/json; charset=utf-8")
	              		.end(Json.encodePrettily(new ZipCode(id, json.getString("city"), json.getJsonArray("loc"), json.getInteger("pop"), json.getString("state"))));
	            }
	          });
	    }
  }
  
   private void getAllZipCodes(RoutingContext routingContext) {
	   MultiMap params = routingContext.request().params();
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
 	 	      List<ZipCode> zips = objects.stream().map(ZipCode::new).collect(Collectors.toList());
 	 	      routingContext.response()
 	 	          .putHeader("content-type", "application/json; charset=utf-8")
 	 	          .end(Json.encodePrettily(zips));
 	 	    });
	   } else {
	       mongo.find(COLLECTION_ZIPCODES, new JsonObject(), results -> {
	 	      List<JsonObject> objects = results.result();
	 	      List<ZipCode> zips = objects.stream().map(ZipCode::new).collect(Collectors.toList());
	 	      routingContext.response()
	 	          .putHeader("content-type", "application/json; charset=utf-8")
	 	          .end(Json.encodePrettily(zips));
	 	    });
	   }

   }
}
