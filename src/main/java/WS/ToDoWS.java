package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BlacklistEJB;
import EJB.PersonalerEJB;
import EJB.ToDoEJB;
import Entitiy.Bewerber;
import Entitiy.Personaler;
import Entitiy.Todo;
import Service.Antwort;
import Service.Hasher;
import Service.MailService;
import Service.Tokenizer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/todo")
@Stateless
@LocalBean
public class ToDoWS{

    @EJB
    private BlacklistEJB blacklistEJB;

    @EJB
    private BewerberEJB bewerberEJB;

    @EJB
    private PersonalerEJB personalerEJB;

    @EJB
    private ToDoEJB todoEJB;

    private final Antwort response = new Antwort();

    private final Gson parser = new Gson();

    private final MailService mail = new MailService();

    private final Hasher hasher = new Hasher();

    private Tokenizer tokenizer = new Tokenizer();

    public boolean verify(String token){
        if(tokenizer.isOn()){
            if(blacklistEJB.onBlacklist(token)){
                return false;
            }
            return tokenizer.verifyToken(token) != null;
        }else{
            return true;
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addTodo(String daten, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                int lastTodoCount;

                try{
                    lastTodoCount = dbPersonaler.getTodoList().get(dbPersonaler.getTodoList().size() - 1).getOrderid();
                }catch(Exception e){
                    //Wenn es kein Todo gibt
                    lastTodoCount = 0;
                }

                Todo jsonTodo = parser.fromJson(daten, Todo.class);

                jsonTodo.setOrderid(lastTodoCount + 1);

                Todo dbTodo = todoEJB.add(jsonTodo);

                dbPersonaler.getTodoList().add(dbTodo);
                dbTodo.setPersonaler(dbPersonaler);

                return response.build(200, parser.toJson(true));
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTodo(@HeaderParam("Authorization") String token, @PathParam("id") int todoId){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                Todo dbTodo = todoEJB.getById(todoId);
                if(dbTodo != null){

                    if(dbPersonaler.getTodoList().contains(dbTodo)){

                        dbPersonaler.getTodoList().remove(dbTodo);

                        todoEJB.remove(dbTodo);

                        return response.build(200, parser.toJson(true));
                    }else{
                        return response.buildError(403, "Das ist nicht ihr Todo");
                    }

                }else{
                    return response.buildError(404, "Das Todo wurde nicht gefunden");
                }

            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTodos(@HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{

                Personaler dbPersonaler = personalerEJB.getByToken(token);

                List<Todo> todos = new ArrayList<>();

                for(Todo todo : dbPersonaler.getTodoList()){
                    todos.add(todo.clone());
                }

                //Sorting by orderId
                todos.sort(new Comparator<Todo>(){
                    @Override
                    public int compare(Todo t1, Todo t2){
                        return t1.getOrderid().compareTo(t2.getOrderid());
                    }
                });

                //dont have to show orderid in json
                return response.build(200, parser.toJson(todos));
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response reorder(String daten, @HeaderParam("Authorization") String token){
        if(!verify(token)){
            return response.buildError(401, "Ungueltiges Token");
        }else{
            try{
                Personaler dbPersonaler = personalerEJB.getByToken(token);

                JsonObject jsonData = parser.fromJson(daten, JsonObject.class);

                //get Todoid from Json
                int jsonTodoId = parser.fromJson((jsonData.get("todo")), Integer.class);

                //get new Index from Json
                int newPos = parser.fromJson((jsonData.get("newPos")), Integer.class);

                Todo dbTodo = todoEJB.getById(jsonTodoId);

                if(dbTodo.getPersonaler().equals(dbPersonaler)){

                    List<Todo> personalerTodos = todoEJB.getSortedByPersonaler(dbPersonaler.getPersonalerid());

                    //orderId vom Todo, dass vorher auf der neuen Position war
                    int orderIdOldTodo = personalerTodos.get(newPos - 1).getOrderid();
                    //da minimal anders vorgegangen werden muss, je nachdem, ob das Todo nach vorne oder
                    //hinten verschoben wird, ist hier die Fallunterscheidung eingebaut
                    if(orderIdOldTodo < dbTodo.getOrderid()){
                        //Setze orderId des zu ändernden Todos auf vorherige Id + 1
                        dbTodo.setOrderid(orderIdOldTodo);
                        int orderIdNewTodo = dbTodo.getOrderid();

                        //Erhöhe die Id von allen nachfolgenden Todos um 1
                        for(Todo todo : personalerTodos){
                            int orderId = todo.getOrderid();
                            if(orderId >= orderIdNewTodo && !todo.equals(dbTodo)){
                                todo.setOrderid(orderId + 1);
                            }
                        }
                    }else if(orderIdOldTodo > dbTodo.getOrderid()){
                        //Setze orderId des zu ändernden Todos auf vorherige Id + 1
                        dbTodo.setOrderid(orderIdOldTodo + 1);
                        int orderIdNewTodo = dbTodo.getOrderid();

                        //Erhöhe die Id von allen nachfolgenden Todos um 1
                        for(Todo todo : personalerTodos){
                            int orderId = todo.getOrderid();
                            if(orderId >= orderIdNewTodo){
                                todo.setOrderid(orderId + 1);
                            }
                        }
                    }

                    return response.build(200, parser.toJson(true));
                }else{
                    return response.buildError(403, "Nicht Ihr Todo");
                }

            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }
}
