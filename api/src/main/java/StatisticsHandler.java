/*
 * Copyright (c) Daria Harashchuk
 * email: daria.harashchuk@gmail.com
 * github: https://github.com/hardddash
 * 2020.
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class StatisticsHandler implements HttpHandler {

    protected Connection db;

    public StatisticsHandler(Connection db) {
        this.db = db;
    }

    public void statistics(HttpExchange ex) throws SQLException {

        Map<String, String> params = null;
        try {
            params = queryToMap(ex.getRequestURI().getQuery());
        } catch (Exception e) {
        }

        if (this.db == null) throw new NullPointerException("Error: db can't be null");

        try {
            System.out.println("Trying to reach database");
            Statement st = this.db.createStatement();
            ResultSet rs;
            if (params == null) {
                rs = st.executeQuery("select sum(total) as total_price from(select price*quantity as total from goods) as counted limit 1");
            } else {
                String id = params.get("group_id").toString();
                rs = st.executeQuery("select sum(total) as total_price from(select price*quantity as total from goods where group_id=" + id + ") as counted limit 1");
            }
            rs.next();
            Double total_price = rs.getDouble("total_price");
            System.out.println(total_price);

            ObjectMapper mapper = new ObjectMapper();
            String response = mapper.writeValueAsString(total_price);

            ex.sendResponseHeaders(200, response.length());
            OutputStream os = ex.getResponseBody();
            os.write(response.getBytes());

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

        System.out.println(exchange.getRequestMethod() + " " + exchange.getRequestURI());

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
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
                        statistics(exchange);
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
