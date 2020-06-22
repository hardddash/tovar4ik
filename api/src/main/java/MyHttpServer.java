/*
 * Copyright (c) Daria Harashchuk
 * email: daria.harashchuk@gmail.com
 * github: https://github.com/hardddash
 * 2020.
 */

import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;

public class MyHttpServer {
    private static final String USER_NAME = "postgres";
    private static final String PASSWORD = "postgres";
    private static final String CONNECTION_URL = "jdbc:postgresql://localhost:5432/";

    public static void main(String[] args) throws Exception {

        Class.forName("org.postgresql.Driver");
        Connection db = DriverManager.getConnection(CONNECTION_URL, USER_NAME, PASSWORD);

        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(80), 0);

        System.out.println("Server started");

        server.createContext("/groups", new GroupsHandler(db));

        server.createContext("/goods", new GoodsHandler(db));

        server.createContext("/good", new GoodHandler(db));

        server.createContext("/statistics", new StatisticsHandler(db));

        server.createContext("/login", new LoginHandler(db));

        System.out.println("Routes created");

        server.setExecutor(null);
        server.start();

    }

}