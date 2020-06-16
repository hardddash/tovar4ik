import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class GroupsHandler implements HttpHandler {
    public void getGroup(HttpExchange ex){
        System.out.println("We have 10 groups");
    }
    public void deleteGroup(HttpExchange ex){
        System.out.println("10 groups deleted");
    }
    public void createGroup(HttpExchange ex){
        System.out.println("1 group created");
    } public void changeGroup(HttpExchange ex){
        System.out.println("group is changed");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        switch (method){
            case "GET":
                getGroup(exchange);
                break;
            case "DELETE":
                deleteGroup(exchange);
                break;
            case "PUT":
                createGroup(exchange);
                break;
            case "POST":
                changeGroup(exchange);

        }
    }
}
