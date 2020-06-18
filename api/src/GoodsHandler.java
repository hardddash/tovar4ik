import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import entities.Good;
import org.codehaus.jackson.map.ObjectMapper;

import javax.json.*;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class GoodsHandler implements HttpHandler {

    protected Connection db;

    public GoodsHandler(Connection db) {
        this.db = db;
    }


    public void getGoods(HttpExchange ex) throws SQLException {
        Map<String, String> params = null;
        try {
            params = queryToMap(ex.getRequestURI().getQuery());
        } catch (Exception e) {
        }


        if (this.db == null) throw new NullPointerException("Error: db can't be null");

        ArrayList<Good> goods = new ArrayList<>();

        try {
            System.out.println("Trying to reach database");
            Statement st = this.db.createStatement();
            ResultSet rs;
            if (params == null) {
                rs = st.executeQuery("SELECT * FROM goods");
            } else if (params.get("query") != null) {
                String query = params.get("query").toString();
                System.out.println("SELECT * FROM goods WHERE name LIKE '%" + query + "%'");
                rs = st.executeQuery("SELECT * FROM goods WHERE name LIKE '%" + query + "%'");
            } else {
                String id = params.get("group_id").toString();
                rs = st.executeQuery("SELECT * FROM goods WHERE group_id =" + id);
            }
            while (rs.next()) {
                int id = rs.getInt("id");
                int group_id = rs.getInt("group_id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                String producer = rs.getString("producer");
                int quantity = rs.getInt("quantity");
                float price = rs.getFloat("price");
                goods.add(new Good(id, group_id, name, description, producer, quantity, price));
            }
            ObjectMapper mapper = new ObjectMapper();
            String response = mapper.writeValueAsString(goods);
            System.out.println(response);

            rs.close();
            st.close();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void deleteGood(HttpExchange ex) throws SQLException {
        if (this.db == null) throw new NullPointerException("Error: db can't be null");
        try {
            System.out.println("Trying to reach database");
            Statement st = this.db.createStatement();
            ResultSet rs = st.executeQuery("DELETE FROM goods WHERE id=" + 1);
            System.out.println("Good with id = " + 1 + " is deleted");
            rs.close();
            st.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void createGood(HttpExchange ex) {
        if (this.db == null) throw new NullPointerException("Error: db can't be null");
        try {
            System.out.println("Trying to reach database");
            Statement st = this.db.createStatement();
            ResultSet rs = st.executeQuery("insert into goods(id, name, description, producer, quantity, price, group_id) values (nextval('goods_seq'), 'Red socks', 'Beautiful red socks', 'Zhitomyr', 10, 15.5, 1);");
            System.out.println("New good is created");
            rs.close();
            st.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void editGood(HttpExchange ex) {

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

        try {
            switch (method) {
                case "GET":
                    getGoods(exchange);
                    break;
                case "DELETE":
                    deleteGood(exchange);
                    break;
                case "POST":
                    createGood(exchange);
                case "PUT":
                    editGood(exchange);
            }
        } catch (SQLException e) {

        } catch (NullPointerException e) {

        }

    }
}