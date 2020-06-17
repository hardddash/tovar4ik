import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import entities.Good;
import org.codehaus.jackson.map.ObjectMapper;

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

    public void getAllGoods(HttpExchange ex) throws SQLException {
        if (this.db == null) throw new NullPointerException("Error: db can't be null");
        ArrayList<Good> goods = new ArrayList<>();
        try {
            System.out.println("Trying to reach database");
            Statement st = this.db.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM goods");
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

            ex.sendResponseHeaders(200, response.length());
            OutputStream os = ex.getResponseBody();
            os.write(response.getBytes());
            os.close();

            rs.close();
            st.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void getGood(HttpExchange ex, int good_id) throws SQLException {
        if (this.db == null) throw new NullPointerException("Error: db can't be null");

        try {
            System.out.println("Trying to reach database");
            Statement st = this.db.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM goods WHERE id=good_id");
            while (rs.next()) {
                int id = rs.getInt("id");
                int group_id = rs.getInt("group_id");
                String name = rs.getString("name");
                String description = rs.getString("description");
                String producer = rs.getString("producer");
                int quantity = rs.getInt("quantity");
                float price = rs.getFloat("price");
                Good good = new Good(id, group_id, name, description, producer, quantity, price);
            }
            rs.close();
            st.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void findGoods(HttpExchange ex, String query) throws SQLException {
        if (this.db == null) throw new NullPointerException("Error: db can't be null");
        ArrayList<Good> goods = new ArrayList<>();
        try {
            System.out.println("Trying to reach database");
            Statement st = this.db.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM goods WHERE name LIKE '%"+query+"%'");
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

            rs.close();
            st.close();

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public void deleteGood(HttpExchange ex, int good_id) throws SQLException {
        if (this.db == null) throw new NullPointerException("Error: db can't be null");
        try {
            System.out.println("Trying to reach database");
            Statement st = this.db.createStatement();
            ResultSet rs = st.executeQuery("DELETE FROM goods WHERE id=" + good_id);
            System.out.println("Good with id = " + good_id + " is deleted");
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

    public void editGood(HttpExchange ex, int good_id) {

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
                    if (params.get("id") == null) getAllGoods(exchange);
                  //  else getGood(exchange, Integer.parseInt(params.get("id")));
                    else findGoods(exchange, params.get("query"));
                    break;
                case "DELETE":
                    deleteGood(exchange, Integer.parseInt(params.get("id")));
                    break;
                case "POST":
                    if (params.get("id") == null) createGood(exchange);
                    else editGood(exchange, Integer.parseInt(params.get("id")));
            }
        } catch (SQLException e) {

        } catch (NullPointerException e) {

        }

    }
}