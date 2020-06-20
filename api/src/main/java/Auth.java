import com.sun.net.httpserver.HttpExchange;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;

public class Auth {

    public static final String SECRET_KEY = "1234";
    public static final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    public static final byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(Auth.SECRET_KEY);
    public static final Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

    public boolean authenticate(HttpExchange httpExchange) {
        try {

            String token = httpExchange.getRequestHeaders().getFirst("token");

            if (token == null)
                throw new JwtException("Wrong token");
            // Jwts.parser().setSigningKey(signingKey).parseClaimsJws(token);

            Claims claims = this.decodeJWT(token);

            return true;

        } catch (JwtException ex) {
            return false;
        }

    }

    public static Claims decodeJWT(String jwt) {
        //This line will throw an exception if it is not a signed JWS (as expected)
        Claims claims = Jwts.parser()
                .setSigningKey(signingKey)
                .parseClaimsJws(jwt).getBody();
        return claims;
    }
}
