import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import entities.Group;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupsHandler implements HttpHandler {

    protected Connection db;

    public GroupsHandler(Connection db) {
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

    public void getGroup(HttpExchange ex) throws SQLException {
        Map<String, String> params = null;
        try {
            params = queryToMap(ex.getRequestURI().getQuery());
        } catch (Exception e) {
        }


        if (this.db == null) throw new NullPointerException("Error: db can't be null");

        ArrayList<Group> groups = new ArrayList<>();

        try {
            System.out.println("Trying to reach database");
            Statement st = this.db.createStatement();
            ResultSet rs;
            if (params == null) {
                rs = st.executeQuery("SELECT * FROM groups order by groups.id");
            } else {
                String group_id = params.get("id").toString();
                rs = st.executeQuery("SELECT * FROM groups WHERE id =" + group_id + " order by groups.id");
            }
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                groups.add(new Group(id, name, description));
            }

            ObjectMapper mapper = new ObjectMapper();
            String response = mapper.writeValueAsString(groups);

            ex.sendResponseHeaders(200, response.length());
            OutputStream os = ex.getResponseBody();
            os.write(response.getBytes());

            rs.close();
            st.close();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void deleteGroup(HttpExchange ex) throws SQLException {
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

    public void createGroup(HttpExchange ex) throws SQLException {
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
            String name = reply.getString("name");
            String description = reply.getString("description");
            System.out.println("Trying to reach database");
            Statement st = this.db.createStatement();
            ResultSet rs = st.executeQuery("insert into groups(id, name, description) values (nextval('groups_seq'),'"
                    + name + "','" + description + "');");
            System.out.println("New group is created");
            rs.close();
            st.close();
            ex.sendResponseHeaders(201, 0);
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
        }
    }

    public void editGroup(HttpExchange ex) throws SQLException {
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

            String group_id = params.get("id").toString();
            String dbRequest = "update groups set ";
            if (reply.containsKey("name")) dbRequest += "name = '" + reply.getString("name") + "',";
            if (reply.containsKey("description"))
                dbRequest += "description = '" + reply.getString("description") + "',";

            if (dbRequest.charAt(dbRequest.length() - 1) == ',') {
                dbRequest = dbRequest.substring(0, dbRequest.length() - 1);
            }

            dbRequest += " WHERE id =" + group_id;
            System.out.println(dbRequest);

            executeQuery(st, dbRequest);
            System.out.println("Group is edited");
            ex.sendResponseHeaders(200, 0);
            st.close();
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
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS, POST, PUT, DELETE");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization,token");
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
                        getGroup(exchange);
                        break;
                    case "DELETE":
                        deleteGroup(exchange);
                        break;
                    case "POST":
                        createGroup(exchange);
                        break;
                    case "PUT":
                        editGroup(exchange);

                }
            } catch (SQLException e) {

            } catch (NullPointerException e) {

            }

        }
    }
}
