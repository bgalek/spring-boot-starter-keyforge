package com.github.bgalek.keyforge.spring;

import com.github.bgalek.keyforge.ApiKey;
import com.github.bgalek.keyforge.KeyForge;
import com.github.bgalek.keyforge.spring.KeyForgeAutoConfiguration.KeyForgeProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class KeyForgeAuthenticationFilter extends OncePerRequestFilter {

    private final KeyForgeProperties keyForgeProperties;
    private final KeyForge keyForge;

    public KeyForgeAuthenticationFilter(KeyForgeProperties keyForgeProperties, Clock clock) {
        this.keyForgeProperties = keyForgeProperties;
        this.keyForge = Optional.ofNullable(clock).map(KeyForge::new).orElseGet(KeyForge::new);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorization != null && authorization.toLowerCase(Locale.ROOT).startsWith("token ")) {
            String token = authorization.substring(6);
            try {
                ApiKey apiKey = keyForge.parse(token);
                if (keyForgeProperties.getKeys().contains(apiKey.toString())) {
                    Authentication auth = new UsernamePasswordAuthenticationToken(apiKey.getIdentifier(), apiKey.toString(), List.of());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception e) {
                logger.debug("Skipping malformed API key token", e);
            }
        }
        filterChain.doFilter(request, response);
    }
}
