package com.example.EliteCart.Filter;

import com.example.EliteCart.Service.CustomUserDetailsService;
import com.example.EliteCart.Util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 🔹 Debug Logs
        System.out.println("--------------------------------------------------");
        System.out.println(">>> Incoming Request: " + request.getMethod() + " " + request.getRequestURI());
        System.out.println(">>> Servlet Path: " + request.getServletPath());

        // Skip /api/auth endpoints (register, login)
        if (request.getServletPath().startsWith("/api/auth")) {
            System.out.println(">>> Skipping JWT filter for auth endpoint (public route)");
            filterChain.doFilter(request, response);
            System.out.println(">>> Exiting JWT filter (public route handled)");
            System.out.println("--------------------------------------------------");
            return;
        }

        // Extract Authorization header
        final String authHeader = request.getHeader("Authorization");
        System.out.println(">>> Authorization Header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println(">>> No Bearer token found. Skipping authentication.");
            filterChain.doFilter(request, response);
            System.out.println(">>> Exiting JWT filter (no token)");
            System.out.println("--------------------------------------------------");
            return;
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        System.out.println(">>> Extracted Username: " + username);

        // If user is not already authenticated
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            boolean valid = jwtUtil.validateToken(token, userDetails);
            System.out.println(">>> Token Valid: " + valid);
            System.out.println(">>> User Roles: " + userDetails.getAuthorities());

            if (valid) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println(">>> Security context updated successfully for user: " + username);
            } else {
                System.out.println(">>> Token validation failed!");
            }
        } else {
            System.out.println(">>> Username null or already authenticated.");
        }

        filterChain.doFilter(request, response);
        System.out.println(">>> Exiting JWT filter after processing");
        System.out.println("--------------------------------------------------");
    }
}
