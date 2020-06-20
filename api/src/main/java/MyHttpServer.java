import com.sun.net.httpserver.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.DriverManager;

public class MyHttpServer {
    private static final String USER_NAME = "postgres";
    private static final String PASSWORD = "example";
    private static final String CONNECTION_URL = "jdbc:postgresql://localhost:5432/";

    public static void main(String[] args) throws Exception {

        Class.forName("org.postgresql.Driver");
        Connection db = DriverManager.getConnection(CONNECTION_URL, USER_NAME, PASSWORD);

        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(80), 0);

        System.out.println("Server started");

       // HttpContext context = server.createContext("/", new EchoHandler());
       // context.setAuthenticator(new Auth());

        server.createContext("/groups", new GroupsHandler(db));

        GoodsHandler goodsHandler = new GoodsHandler(db);
        server.createContext("/goods/:id", new GoodsHandler(db));
        server.createContext("/goods", new GoodsHandler(db));

        server.createContext("/good", new GoodHandler(db));

        server.createContext("/statistics", new StatisticsHandler(db));

        server.createContext("/login", new LoginHandler(db));

        System.out.println("Routes created");

        //db.close();

        server.setExecutor(null);
        server.start();

    }

    static class EchoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            StringBuilder builder = new StringBuilder();

            builder.append("<h1>URI: ").append(exchange.getRequestURI()).append("</h1>");

            Headers headers = exchange.getRequestHeaders();
            for (String header : headers.keySet()) {
                builder.append("<p>").append(header).append("=")
                        .append(headers.getFirst(header)).append("</p>");
            }

            byte[] bytes = builder.toString().getBytes();
            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }

    static class Auth extends Authenticator {
        @Override
        public Result authenticate(HttpExchange httpExchange) {
            if ("/forbidden".equals(httpExchange.getRequestURI().toString()))
                return new Failure(403);
            else
                return new Success(new HttpPrincipal("c0nst", "realm"));
        }
    }
}