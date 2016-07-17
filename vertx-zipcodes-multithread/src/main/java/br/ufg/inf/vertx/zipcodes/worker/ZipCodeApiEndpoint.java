package br.ufg.inf.vertx.zipcodes.worker;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.Map.Entry;

public class ZipCodeApiEndpoint extends AbstractVerticle{
	
	@Override
	public void start() throws Exception {
		try {
	        DeploymentOptions deployOpts = new DeploymentOptions().setWorker(true).setMultiThreaded(true).setInstances(10);
	        vertx.deployVerticle("vertx-zipcodes-multithread:br.ufg.inf.vertx.zipcodes.worker.ZipCodeApiWorker", deployOpts);
	        final DeliveryOptions opts = new DeliveryOptions();
            Router router = Router.router(vertx);
            router.get("/api/zipcodes")
            	.produces("application/json")
            	.handler(rc -> 
            	{
            		for (Entry<String, String> e : rc.request().params()) {
            			opts.addHeader(e.getKey(), e.getValue());
					}
            		vertx.eventBus().send("getAllZipCodes", null, opts, reply -> handleReplyGetZipCode(reply, rc));
            	});
            
            router.route("/api/zipcodes*").handler(BodyHandler.create());
            router.post("/api/zipcodes")
                    .produces("application/json")
                    .handler(rc -> {	        	                            
                        vertx.eventBus().send("addZipCodes", rc.getBodyAsJson(), opts, reply -> handleReplyAddZipCodes(reply, rc));
                    });
            router.get("/api/zipcodes/:id")
            	.produces("application/json")
            	.handler(rc -> {
            		opts.addHeader("id", rc.request().getParam("id"));
            		vertx.eventBus().send("getZipCode", null, opts, reply -> handleReplyGetZipCode(reply, rc));
            	});
            router.delete("/api/zipcodes/:id")
            	.produces("application/json")
            	.handler(rc  ->
            	{
            		opts.addHeader("id", rc.request().getParam("id"));
            		vertx.eventBus().send("deleteZipCodes", null, opts, reply -> handleReplyGetZipCode(reply, rc));
            	}
            	);
            router.put("/api/zipcodes/:id")
            	.produces("application/json")
            	.handler(rc ->
            	{
            		opts.addHeader("id", rc.request().getParam("id"));
            		vertx.eventBus().send("updateZipCodes", rc.getBodyAsJson(), opts, reply -> handleReplyGetZipCode(reply, rc));
            	}
            	);
            
            System.out.println("Configurando porta"+ config().getInteger("http.port", 8080));
            vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("http.port", 8080));
            System.out.println("ZipCodeApiEndpoint criado com sucesso!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    private void handleReplyAddZipCodes(AsyncResult<Message<Object>> reply, RoutingContext rc) {
        try {
			if (reply.succeeded()) {
			    Message<Object> replyMsg = reply.result();
			    Integer statusCode = Integer.valueOf(replyMsg.headers().get("STATUS"));
			    	if (replyMsg.headers().get("Location") != null) {
			    		rc.response()
			    		.setStatusMessage(replyMsg.headers().get("MESSAGE"))
			    		.setStatusCode(statusCode)                        
			    		.putHeader("Content-Type", "application/json")
			    		.putHeader("Location", replyMsg.headers().get("Location"))
			    		.end(replyMsg.body().toString());
					} else {
						rc.response()
			    		.setStatusMessage(replyMsg.headers().get("MESSAGE"))
			    		.setStatusCode(statusCode)                        
			    		.putHeader("Content-Type", "application/json")
			    		.end(replyMsg.body().toString());
					}
			} else {
				rc.response()
			    .setStatusCode(500)
			    .setStatusMessage("Server Error")
			    .end(reply.cause().getLocalizedMessage());
			}
		} catch (Exception e) {
			rc.response()
		    .setStatusCode(500)
		    .setStatusMessage("Server Error").end();
		}
    }
    
    private void handleReplyGetZipCode(AsyncResult<Message<Object>> reply, RoutingContext rc) {
        try {
		    if (reply.succeeded()) {
		    	Message<Object> replyMsg = reply.result();
		    	Integer statusCode = Integer.valueOf(replyMsg.headers().get("STATUS"));
		        rc.response()
		                .setStatusMessage(replyMsg.headers().get("MESSAGE"))
		                .setStatusCode(statusCode)                        
		                .putHeader("Content-Type", "application/json")
		                .end(replyMsg.body().toString());
		    } else {
		        rc.response()
		                .setStatusCode(500)
		                .setStatusMessage("Server Error")
		                .end(reply.cause().getLocalizedMessage());
		    }
		} catch (Exception e) {
			rc.response()
		    .setStatusCode(500)
		    .setStatusMessage("Server Error").end();
		}
    }
}
