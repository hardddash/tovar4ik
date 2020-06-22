/*
 * Copyright (c) Daria Harashchuk
 * email: daria.harashchuk@gmail.com
 * github: https://github.com/hardddash
 * 2020.
 */

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
            Statement st = this.db.createStatement();
            ResultSet rs;
            if (params == null) {
                System.out.println("SQL request: SELECT * FROM goods order by goods.id");
                rs = st.executeQuery("SELECT * FROM goods order by goods.id");
            } else if (params.get("query") != null) {
                String query = params.get("query").toString();
                System.out.println("SQL request: SELECT * FROM goods WHERE name LIKE '%" + query + "%' ");
                rs = st.executeQuery("SELECT * FROM goods WHERE name LIKE '%" + query + "%'");
            } else {
                String id = params.get("group_id").toString();
                System.out.println("SQL request: SELECT * FROM goods WHERE group_id =" + id + " order by goods.id");
                rs = st.executeQuery("SELECT * FROM goods WHERE group_id =" + id + " order by goods.id");
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

            ex.sendResponseHeaders(200, response.length());
            OutputStream os = ex.getResponseBody();
            os.write(response.getBytes());

            rs.close();
            st.close();

        } catch (Exception e) {
            System.err.println(e.getMessage());

        }
    }

    public void createGood(HttpExchange ex) {

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
            String producer = reply.getString("producer");
            int quantity = reply.getInt("quantity");
            double price = reply.getJsonNumber("price").doubleValue();
            int group_id = reply.getInt("group_id");

            Statement st = this.db.createStatement();
            System.out.println("SQL request: insert into goods(id, name, description, producer, quantity, price, group_id) values (nextval('goods_sequence'),'"
                    + name + "','" + description + "','" + producer + "'," + quantity + "," + price + "," + group_id + ");");
            ResultSet rs = st.executeQuery("insert into goods(id, name, description, producer, quantity, price, group_id) values (nextval('goods_sequence'),'"
                    + name + "','" + description + "','" + producer + "'," + quantity + "," + price + "," + group_id + ");");

            rs.close();
            st.close();
            ex.sendResponseHeaders(201, 0);
        } catch (NumberFormatException e) {
            try {
                ex.sendResponseHeaders(406, -1);
            } catch (IOException exc) {
                exc.printStackTrace();
            }
        } catch (PSQLException e) {
            try {
                switch (e.getSQLState()) {
                    case "02000":
                        ex.sendResponseHeaders(201, -1);
                        break;
                    case "23505":
                        ex.sendResponseHeaders(409, -1);
                        break;
                    default:
                        ex.sendResponseHeaders(400, -1);
                }
            } catch (IOException exc) {
                System.err.println(exc.getMessage());
            }
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

        System.out.println(exchange.getRequestMethod() + " " + exchange.getRequestURI());

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "http://localhost:3000");
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
                        getGoods(exchange);
                        break;
                    case "POST":
                        createGood(exchange);
                }
            } catch (SQLException e) {

            } catch (NullPointerException e) {

            } finally {
                exchange.getResponseBody().close();
                exchange.close();
            }
        }
    }
}