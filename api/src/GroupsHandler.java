import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import entities.Good;
import entities.Group;

import java.io.IOException;
import java.io.OutputStream;
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

    public void getAllGroups(HttpExchange ex) throws SQLException {
        if (this.db == null) throw new NullPointerException("Error: db can't be null");
        ArrayList<Group> groups = new ArrayList<>();
        try {
            System.out.println("Trying to reach groups database");
            Statement st = this.db.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM groups");
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");

                groups.add(new Group(id, name, description));
            }
            rs.close();
            st.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void getGroup(HttpExchange ex, int group_id) throws SQLException {
        if (this.db == null) throw new NullPointerException("Error: db can't be null");

        try {
            System.out.println("Trying to reach groups database");
            Statement st = this.db.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM groups WHERE id=" + group_id);
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                Group group = new Group(id, name, description);
            }
            rs.close();
            st.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void deleteGroup(HttpExchange ex, int group_id) throws SQLException {
        if (this.db == null) throw new NullPointerException("Error: db can't be null");
        try {
            System.out.println("Trying to reach groups database");
            Statement st = this.db.createStatement();
            ResultSet rs = st.executeQuery("DELETE FROM goods WHERE id=" + group_id);
            System.out.println("Good with id = " + group_id + " is deleted");
            rs.close();
            st.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void createGroup(HttpExchange ex) {
        if (this.db == null) throw new NullPointerException("Error: db can't be null");
        try {
            System.out.println("Trying to reach database");
            Statement st = this.db.createStatement();
            ResultSet rs = st.executeQuery("insert into groups(id, name, description) values (nextval('goods_seq'), 'Socks', 'Beautiful socks')");
            System.out.println("New group is created");
            rs.close();
            st.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void editGroup(HttpExchange ex, int group_id) {

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
        Map<String, String> params = queryToMap(exchange.getRequestURI().getQuery());
        try {
            switch (method) {
                case "GET":
                    if (params.get("id") == null) getAllGroups(exchange);
                    else getGroup(exchange, Integer.parseInt(params.get("id")));
                    break;
                case "DELETE":
                    deleteGroup(exchange, Integer.parseInt(params.get("id")));
                    break;
                case "POST":
                    if (params.get("id") == null) createGroup(exchange);
                    else editGroup(exchange, Integer.parseInt(params.get("id")));
            }
        } catch (SQLException e) {

        } catch (NullPointerException e) {

        }

    }
}
