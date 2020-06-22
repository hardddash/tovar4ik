import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.*;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class LoginHandler implements HttpHandler {
    protected Connection db;

    public LoginHandler(Connection db) {
        this.db = db;
    }

    private boolean checkUser(String username, String pass) {
        try {
            System.out.println("SQL request: SELECT * FROM users WHERE username = " + username + " AND pass = " + pass);
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

            int ch;
            StringBuilder sb = new StringBuilder();
            while ((ch = bodyStream.read()) != -1)
                sb.append((char) ch);

            String jsonString = sb.toString();
            JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
            JsonObject reply = jsonReader.readObject();
            String username = reply.getString("username");
            System.out.println("trying to login user: " + username);
            String password = reply.getString("password");
            if (username == null || password == null) {
                ex.sendResponseHeaders(401, 0);
                ex.getResponseBody().close();
                return;
            }

            Statement st = this.db.createStatement();

            Date expiration_time = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(expiration_time);
            calendar.add(Calendar.HOUR, 1);
            expiration_time = calendar.getTime();


            if (checkUser(username, password)) {
                System.out.println("logged in user: " + username);
                JwtBuilder builder = Jwts.builder()
                        .setIssuedAt(expiration_time)
                        .setSubject(username)
                        .signWith(Auth.signatureAlgorithm, Auth.signingKey);
                String jwt = builder.compact();

                ex.sendResponseHeaders(200, jwt.getBytes().length);
                ex.getResponseBody().write(jwt.getBytes());

            } else {
                ex.sendResponseHeaders(401, 0);
            }
            st.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                ex.getResponseBody().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
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

        System.out.println(exchange.getRequestMethod() + " " + exchange.getRequestURI());

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization,token");
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method = exchange.getRequestMethod();

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
        } finally {
            exchange.getResponseBody().close();
            exchange.close();
        }

    }
}
