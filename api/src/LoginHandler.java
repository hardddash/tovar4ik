
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import entities.Good;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.codehaus.jackson.map.ObjectMapper;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LoginHandler implements HttpHandler {
    protected Connection db;

    public LoginHandler(Connection db) {
        this.db = db;
    }

    private boolean checkUser(String username, String pass) {
        try {
            PreparedStatement preparedStatement = db.prepareStatement("SELECT * FROM users WHERE username = ? AND pass = ?");
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, pass);
            ResultSet res = preparedStatement.executeQuery();
            return res.next();
        } catch (SQLException e) {
            return false;
        }
    }

    public void login(HttpExchange ex) throws SQLException {

        if (this.db == null) throw new NullPointerException("Error: db can't be null");

        try {
            InputStream bodyStream = ex.getRequestBody();
            // byte[] stream = new byte[bodyStream.available()];
            //bodyStream.read(stream,0,bodyStream.available());

            int ch;
            StringBuilder sb = new StringBuilder();
            while ((ch = bodyStream.read()) != -1)
                sb.append((char) ch);

            String jsonString = sb.toString();
            JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
            JsonObject reply = jsonReader.readObject();
            String username = reply.getString("username");
            String password = reply.getString("password");
            if (username == null || password == null) {
                ex.sendResponseHeaders(401, 0);
                ex.getResponseBody().close();
                return;
            }


            System.out.println("Trying to reach database");
            Statement st = this.db.createStatement();


            if (checkUser(username, password)) {
                SignatureAlgorithm alg = Auth.signatureAlgorithm;
                Key key = Auth.KEY;
                String jwt = Jwts.builder().setSubject(username).signWith(alg, key).compact();
                ex.sendResponseHeaders(200, jwt.getBytes().length);
                ex.getResponseBody().write(jwt.getBytes());
            } else {

            }

            String token = "123";
            String response_json = Json.createObjectBuilder().add("token",token).build().toString();

            ex.sendResponseHeaders(200, response_json.length());
            OutputStream os = ex.getResponseBody();
            os.write(response_json.getBytes());

           // rs.close();
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

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method = exchange.getRequestMethod();

        //Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        try {
            switch (method) {
                case "POST":
                    login(exchange);
                    break;
                case "OPTIONS":
                    break;
            }
        } catch (SQLException e) {

        } catch (NullPointerException e) {
            System.err.println(e.getMessage());
        }

    }
}
