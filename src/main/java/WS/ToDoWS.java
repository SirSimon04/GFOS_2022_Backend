package WS;

import EJB.AdresseEJB;
import EJB.BewerberEJB;
import EJB.BlacklistEJB;
import EJB.DateiEJB;
import EJB.FotoEJB;
import EJB.PersonalerEJB;
import EJB.ToDoEJB;
import Entitiy.Datei;
import Entitiy.Bewerber;
import Entitiy.Foto;
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
    private DateiEJB dateiEJB;

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
        System.out.println("WS.BewerberWS.verifyToken()");
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

                int existingTodoCount = dbPersonaler.getTodoList().size();

                Todo jsonTodo = parser.fromJson(daten, Todo.class);

                jsonTodo.setOrderid(existingTodoCount + 1);

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

                return response.build(200, parser.toJson(todos));
            }catch(Exception e){
                return response.buildError(500, "Es ist ein Fehler aufgetreten");
            }
        }
    }

}
