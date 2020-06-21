import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import entities.Good;
import org.postgresql.util.PSQLException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;


public class GoodHandler implements HttpHandler {

    protected Connection db;

    public GoodHandler(Connection db) {
        this.db = db;
    }

    protected static ResultSet executeQuery(Statement c, String query) throws SQLException {
        try {
            return c.executeQuery(query);
        } catch (SQLException e) {
            switch (e.getSQLState()) {
                case "02000":
                    return null;
                default:
                    throw e;
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void getGood(HttpExchange ex) throws SQLException {

        Map<String, String> params = queryToMap(ex.getRequestURI().getQuery());

        if (this.db == null) throw new NullPointerException("Error: db can't be null");


        try {
            System.out.println("Trying to reach database");
            Statement st = this.db.createStatement();
            ResultSet rs;

            String good_id = params.get("id").toString();
            System.out.println("SELECT * FROM goods WHERE id =" + good_id);
            rs = executeQuery(st, "SELECT * FROM goods WHERE id = " + good_id);
            rs.next();
            int id = rs.getInt("id");
            int group_id = rs.getInt("group_id");
            String name = rs.getString("name");
            String description = rs.getString("description");
            String producer = rs.getString("producer");
            int quantity = rs.getInt("quantity");
            float price = rs.getFloat("price");
            Good good = new Good(id, group_id, name, description, producer, quantity, price);

            ObjectMapper mapper = new ObjectMapper();
            String response = mapper.writeValueAsString(good);

            ex.sendResponseHeaders(200, response.length());
            OutputStream os = ex.getResponseBody();
            os.write(response.getBytes());

            rs.close();
            st.close();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }

    public void deleteGood(HttpExchange ex) throws SQLException {
        Map<String, String> params = queryToMap(ex.getRequestURI().getQuery());

        if (this.db == null) throw new NullPointerException("Error: db can't be null");

        try {
            System.out.println("Trying to reach database");
            Statement st = this.db.createStatement();

            String good_id = params.get("id").toString();
            System.out.println("DELETE * FROM goods WHERE id =" + good_id);
            executeQuery(st, "DELETE FROM goods WHERE id = " + good_id);

            ex.sendResponseHeaders(200, -1);
            ex.getResponseBody().close();

            st.close();
        } catch (PSQLException e) {
            try {
                switch (e.getSQLState()) {
                    case "23505":
                        ex.sendResponseHeaders(409, -1);
                        ex.getResponseBody().close();
                        break;
                    default:
                        ex.sendResponseHeaders(400, -1);
                        ex.getResponseBody().close();
                }
            } catch (IOException exc) {
                System.err.println(exc.getMessage());
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void editGood(HttpExchange ex) {
        Map<String, String> params = queryToMap(ex.getRequestURI().getQuery());

        if (this.db == null) throw new NullPointerException("Error: db can't be null");

        InputStream bodyStream = ex.getRequestBody();
        StringBuilder sb = new StringBuilder();
        try {
            int ch;
            while (true) {
                if (!((ch = bodyStream.read()) != -1)) break;
                sb.append((char) ch);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String jsonString = sb.toString();
        JsonReader jsonReader = Json.createReader(new StringReader(jsonString));
        JsonObject reply = jsonReader.readObject();

        try {
            System.out.println("Trying to reach database");
            Statement st = this.db.createStatement();

            String good_id = params.get("id").toString();
            String dbRequest = "update goods set ";
            if (reply.containsKey("name")) dbRequest += "name = '" + reply.getString("name") + "',";
            if (reply.containsKey("description"))
                dbRequest += "description = '" + reply.getString("description") + "',";
            if (reply.containsKey("producer")) dbRequest += "producer = '" + reply.getString("producer") + "',";
            if (reply.containsKey("quantity")) dbRequest += "quantity = " + reply.getInt("quantity") + ",";
            if (reply.containsKey("price")) dbRequest += "price = " + reply.getJsonNumber("price") + ",";
            if (reply.containsKey("group_id")) dbRequest += "group_id = " + reply.getInt("group_id") + ",";

            if (dbRequest.charAt(dbRequest.length() - 1) == ',') {
                dbRequest = dbRequest.substring(0, dbRequest.length() - 1);
            }

            dbRequest += " WHERE id =" + good_id;
            System.out.println(dbRequest);

            executeQuery(st, dbRequest);
            System.out.println("Good is edited");
            st.close();
            ex.sendResponseHeaders(200, 0);
        } catch (PSQLException e) {
            try {
                switch (e.getSQLState()) {
                    case "02000":
                        ex.sendResponseHeaders(201, -1);
                        ex.getResponseBody().close();
                        break;
                    case "23505":
                        ex.sendResponseHeaders(409, -1);
                        ex.getResponseBody().close();
                        break;
                    default:
                        ex.sendResponseHeaders(400, -1);
                        ex.getResponseBody().close();
                }
            } catch (IOException exc) {
                System.err.println(exc.getMessage());
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                ex.getResponseBody().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ex.close();

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
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS, POST,DELETE, PUT");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization, token");
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        String method = exchange.getRequestMethod();
        if (!new Auth().authenticate(exchange)) {
            exchange.sendResponseHeaders(401, -1);
            exchange.getResponseBody().close();
        } else {
            try {
                switch (method) {
                    case "GET":
                        getGood(exchange);
                        break;
                    case "DELETE":
                        deleteGood(exchange);
                        break;
                    case "PUT":
                        editGood(exchange);
                }
            } catch (SQLException e) {

            } catch (NullPointerException e) {

            }

        }
    }
}
