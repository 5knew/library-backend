    package com.aues.library.service.impl;

    import com.aues.library.model.User;
    import com.aues.library.service.JWTService;
    import io.jsonwebtoken.Claims;
    import io.jsonwebtoken.Jwts;
    import io.jsonwebtoken.SignatureAlgorithm;
    import io.jsonwebtoken.io.Decoders;
    import io.jsonwebtoken.security.Keys;
    import org.springframework.security.core.authority.SimpleGrantedAuthority;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.stereotype.Service;

    import java.security.Key;
    import java.util.Date;
    import java.util.HashMap;
    import java.util.Map;
    import java.util.function.Function;

    @Service
    public class JWTServiceImpl implements JWTService {

        public String generateToken(UserDetails userDetails) {
            Map<String, Object> claims = new HashMap<>();

            // Assuming the role is stored as an authority in the UserDetails
            String role = userDetails.getAuthorities().stream()
                    .map(grantedAuthority -> ((SimpleGrantedAuthority) grantedAuthority).getAuthority())
                    .findFirst().orElse(null);

            // Add role to claims
            claims.put("role", role);

            // Cast userDetails to User to access getId()
            Long userId = ((User) userDetails).getId();
            claims.put("userId", userId);

            return Jwts.builder()
                    .setClaims(claims)
                    .setSubject(userDetails.getUsername())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // Token valid for 24 hours
                    .signWith(getSignKey(), SignatureAlgorithm.HS256)
                    .compact();
        }


        public String generateRefreshToken(Map<String, Object> extractClaims, UserDetails userDetails) {
            return Jwts.builder().setClaims(extractClaims).setSubject(userDetails.getUsername())
                    .setIssuedAt(new Date(System.currentTimeMillis()))
                    .setExpiration(new Date(System.currentTimeMillis() + 604800000))
                    .signWith(getSignKey(), SignatureAlgorithm.HS256)
                    .compact();
        }
        public String extractUserId(String token) {
            return extractClaim(token, claims -> {
                Object userIdObj = claims.get("userId");
                return userIdObj != null ? userIdObj.toString() : null;
            });
        }




        private  <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
            Claims claims = extractAllClaims(token);
            return claimsResolvers.apply(claims);
        }

        public String extractUserName(String token){
            return  extractClaim(token, Claims::getSubject);
        }

        private Claims extractAllClaims(String token) {
            return Jwts.parser().setSigningKey(getSignKey()).build().parseClaimsJws(token).getBody();
        }


        private Key getSignKey() {
            byte[] key = Decoders.BASE64.decode("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/");
            return Keys.hmacShaKeyFor(key);
        }

        public boolean isTokenValid(String token, UserDetails userDetails){
            final String username = extractUserName(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        }

        private boolean isTokenExpired(String token){
            return extractClaim(token, Claims::getExpiration).before(new Date());
        }
    }
