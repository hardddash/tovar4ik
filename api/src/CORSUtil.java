import com.sun.net.httpserver.Headers;

@SuppressWarnings("restriction")
public class CORSUtil
{
    public static void setCors(Headers requestHeaders, Headers responseHeaders)
    {

        String origin = requestHeaders.getFirst("Origin");

        if (origin == null)
            origin = "null";

        responseHeaders.set("Allow", "GET,PUT,POST,DELETE,OPTIONS");
        responseHeaders.set("Access-Control-Allow-Origin", origin);
        responseHeaders.set("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE,OPTIONS");
        responseHeaders.set("Access-Control-Allow-Headers", "Content-Type");
        responseHeaders.set("Access-Control-Request-Methods", "GET,PUT,POST,DELETE,OPTIONS");
        responseHeaders.set("Access-Control-Request-Headers", "Content-Type");
        responseHeaders.set("Access-Control-Max-Age", "3600");
    }
}