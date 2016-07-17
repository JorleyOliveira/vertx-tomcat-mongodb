package br.ufg.inf.tomcat.zipcodes.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Component;

import br.ufg.inf.tomcat.zipcodes.dto.ZipCode;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.util.JSON;

@Component
@Path(value="/api/zipcodes")
public class ZipCodeApi {
    private final String DATABASE = "vertx";
    private final String COLLECTION = "zips";

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @GET
    @Consumes(value={"application/json"})
    @Path(value="/{id}")
    public Response getZipCode(@PathParam(value="id") String id) {
        try {
            MongoClient mongo = new MongoClient("192.168.0.103", 27017);
            try {
                MongoDatabase db = mongo.getDatabase("vertx");
                FindIterable iterable = db.getCollection("zips").find((Bson)new Document("_id", new ObjectId(id)));
                if (iterable.first() != null) {
                    Response response = Response.ok(((Document)iterable.first()).toJson(), (String)"application/json").build();
                    return response;
                }
                Response response = Response.status(Status.NOT_FOUND).build();
                return response;
            }
            finally {
                mongo.close();
            }
        }
        catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @GET
    @Consumes(value={"application/json"})
    public Response getZipCode(@QueryParam(value="city") String city, @QueryParam(value="state") String state) {
        try {
            MongoClient mongo = new MongoClient("192.168.0.103", 27017);
            try {
                MongoDatabase db = mongo.getDatabase("vertx");
                List<Document> docs = new ArrayList<Document>();
                Document doc = new Document();
                if (city != null) {
                    doc.append("city", city);
                }
                if (state != null) {
                    doc.append("state", state);
                }
                Block<Document> printBlock = new Block<Document>() {
                    @Override
                    public void apply(final Document document) {
                    	docs.add(document);
                    }
               };
                db.getCollection("zips").find(doc).forEach(printBlock);
                if (!docs.isEmpty()) {
                    Response response = Response.ok(JSON.serialize(docs), "application/json").build();
                    return response;
                }
                Response response = Response.status(Status.NOT_FOUND).build();
                return response;
            }
            finally {
                mongo.close();
            }
        }
        catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @PUT
    @Consumes(value={"application/json"})
    @Path(value="/{id}")
    public Response atualizarZipCode(@PathParam(value="id") String id, ZipCode zipCode) {
        try {
            MongoClient mongo = new MongoClient("192.168.0.103", 27017);
            try {
                MongoDatabase db = mongo.getDatabase("vertx");
                if (id == null || zipCode == null) {
                    Response response = Response.status(Status.NOT_FOUND).build();
                    return response;
                }
                Document doc = new Document("city", zipCode.getCity()).append("loc", zipCode.getLoc()).append("pop", zipCode.getPop()).append("state", zipCode.getState());
                UpdateResult updateResult = db.getCollection("zips").updateOne(new Document("_id", new ObjectId(id)), new Document("$set", doc));
                if (updateResult.wasAcknowledged() && updateResult.isModifiedCountAvailable()) {
                    Response response = Response.ok().encoding("utf-8").build();
                    return response;
                }
                Response response = Response.status(Status.NOT_FOUND).build();
                return response;
            }
            finally {
                mongo.close();
            }
        }
        catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @DELETE
    @Consumes(value={"application/json"})
    @Path(value="/{id}")
    public Response removerZipCode(@PathParam(value="id") String id) {
        try {
            MongoClient mongo = new MongoClient("192.168.0.103", 27017);
            try {
                MongoDatabase db = mongo.getDatabase("vertx");
                if (id == null) {
                    Response response = Response.status(Status.NOT_FOUND).build();
                    return response;
                }
                DeleteResult delResult = db.getCollection("zips").deleteOne(new Document("_id", new ObjectId(id)));
                if (delResult.wasAcknowledged() && delResult.getDeletedCount() > 0) {
                    Response response = Response.ok().build();
                    return response;
                }
                Response response = Response.status(Status.NOT_FOUND).build();
                return response;
            }
            finally {
                mongo.close();
            }
        }
        catch (Exception e) {
            return Response.serverError().build();
        }
    }

    @POST
    @Consumes(value={"application/json"})
    public Response addZipCode(ZipCode zipCode) {
        try {
            MongoClient mongo = new MongoClient("192.168.0.103", 27017);
            try {
                MongoDatabase db = mongo.getDatabase("vertx");
                Document doc = new Document("city", zipCode.getCity()).append("loc", zipCode.getLoc()).append("pop", zipCode.getPop()).append("state", zipCode.getState());
                db.getCollection("zips").insertOne(doc);
                if (doc.getObjectId("_id") != null) {
                    Response response = Response.created((URI)new URI("http://localhost:8080/zipcodeapi/" + doc.getObjectId("_id"))).build();
                    return response;
                }
                Response response = Response.status(Status.CONFLICT).build();
                return response;
            }
            finally {
                mongo.close();
            }
        }
        catch (Exception e) {
            return Response.serverError().build();
        }
    }
}