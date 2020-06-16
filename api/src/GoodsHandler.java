import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import entities.Good;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.ArrayList;

public class GoodsHandler implements HttpHandler {

    protected Connection db;

    public GoodsHandler(Connection db) {
        this.db = db;
    }

    public void getGood(HttpExchange ex) throws SQLException {

        if (this.db == null) throw new NullPointerException("Error: db can't be null");
        ArrayList<Good> goods= new ArrayList<>();
        try {
            System.out.println("Trying to reach database");
            Statement st = this.db.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM goods");
            while (rs.next()) {
                int id = rs.getInt("id");
                int group_id=rs.getInt("group_id");
                String name=rs.getString("name");
                String description=rs.getString("description");
                String producer=rs.getString("producer");
                int quantity=rs.getInt("quantity");
                float price=rs.getFloat("price");
                goods.add(new Good(id,group_id,name,description,producer,quantity,price));
            }
            rs.close();
            st.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        

    }

    public void deleteGood(HttpExchange ex) {
        System.out.println("10 groups deleted");
    }

    public void createGood(HttpExchange ex) {
        System.out.println("1 group created");
    }

    public void changeGood(HttpExchange ex) {
        System.out.println("group is changed");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        try {
            switch (method) {
                case "GET":
                    getGood(exchange);
                    break;
                case "DELETE":
                    deleteGood(exchange);
                    break;
                case "PUT":
                    createGood(exchange);
                    break;
                case "POST":
                    changeGood(exchange);
            }
        } catch (SQLException e) {

        }  catch (NullPointerException e) {

        }


    }
}