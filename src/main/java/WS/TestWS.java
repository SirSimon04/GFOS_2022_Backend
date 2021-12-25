/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package WS;

import EJB.TestEJB;
import Entities.Test;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author simon
 */
@Path("/test")
@Stateless
@LocalBean
public class TestWS{

    @EJB
    private TestEJB testEJB;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getAll(){
        Gson parser = new Gson();
        List<Test> l = testEJB.getAll();
        return parser.toJson(l);
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String get(@PathParam("id") int id){
        Gson parser = new Gson();
        return parser.toJson(testEJB.get(id));
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public boolean delete(@PathParam("id") int id){
        return testEJB.delete(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public boolean create(String jsonStr){
        Gson parser = new Gson();
        try{
            Test t = parser.fromJson(jsonStr, Test.class);
            testEJB.add(t);
            return true;
        }catch(JsonSyntaxException e){
            return false;
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public boolean update(String jsonStr){
        Gson parser = new Gson();
        Test aktTest = parser.fromJson(jsonStr, Test.class);
        return testEJB.update(aktTest);
    }

}
