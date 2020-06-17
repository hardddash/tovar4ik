
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import entities.Good;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginHandler implements HttpHandler {
    protected Connection db;

    public LoginHandler(Connection db) {
        this.db = db;
    }
    public void login(HttpExchange ex) throws SQLException {

        if (this.db == null) throw new NullPointerException("Error: db can't be null");

        try {
            InputStream bodyStream = ex.getRequestBody();
           // byte[] stream = new byte[bodyStream.available()];
            //bodyStream.read(stream,0,bodyStream.available());

            int ch;
            StringBuilder sb = new StringBuilder();
            while((ch = bodyStream.read()) != -1)
                sb.append((char)ch);

            String bodyString = sb.toString();

            //JsonReader jsonReader = Json.createReader();
            //JsonObject object = jsonReader.readObject();
            //jsonReader.close();

            System.out.println("Trying to reach database");
            Statement st = this.db.createStatement();
            ResultSet rs = st.executeQuery("insert into users (id, name, description) values (nextval('goods_seq'), 'user1', '1111')");
            rs.close();
            st.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }



    public Map<String, String> queryToMap(String query) {
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        //Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        try {
            switch (method){
                case "POST":
                   login(exchange);
            }
        } catch (SQLException e) {

        } catch (NullPointerException e) {

        }

    }
}
