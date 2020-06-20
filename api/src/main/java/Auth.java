/*import com.auth0.jwt.algorithms.*;
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
//import io.jsonwebtoken.*;


import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

import java.util.Base64;

public class Auth extends Authenticator{

    public static final Algorithm algorithmHS = Algorithm.HMAC256("secret");
    public static final String SECRET_KEY= "1234";
    public static final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    private static final byte[] apiKeySecretBytes = Base64.getDecoder().decode(SECRET_KEY);
    public static Key KEY = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());;

    Auth(){

    }


    @Override
    public Authenticator.Result authenticate(HttpExchange httpExchange) {
        try{
            String token = httpExchange.getRequestHeaders().getFirst("token");
            if (token == null)
                throw new JwtException("Wrong token");
            Jwts.parser().setSigningKey(KEY).parseClaimsJws(token);
        }
        catch (JwtException ex){
            return new Authenticator.Failure(403);
        }
        return new Authenticator.Success(new HttpPrincipal("c0nst", "realm"));
    }
}
*/