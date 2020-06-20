
import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.xml.bind.DatatypeConverter;


import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.security.Key;

import java.util.Base64;

public class Auth extends Authenticator {

    public static final String SECRET_KEY = "1234";
    public static final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    public static final byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(Auth.SECRET_KEY);
    public static final Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

    @Override
    public Authenticator.Result authenticate(HttpExchange httpExchange) {
        try {
            InputStream bodyStream = httpExchange.getRequestBody();
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
            JsonObject jsonObject = jsonReader.readObject();
            String token = jsonObject.getString("bearer");

            if (token == null)
                throw new JwtException("Wrong token");
           // Jwts.parser().setSigningKey(signingKey).parseClaimsJws(token);

            Claims claims = this.decodeJWT(token);

            return new Authenticator.Success(new HttpPrincipal("c0nst", "realm"));

        } catch (JwtException ex) {
            return new Authenticator.Failure(403);
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
