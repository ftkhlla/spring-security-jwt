package ru.alishev.springcourse.FirstSecirityApp.config;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.alishev.springcourse.FirstSecirityApp.security.JWTUtil;
import ru.alishev.springcourse.FirstSecirityApp.services.PersonDetailService;

import java.io.IOException;

@Component
public class JWTFilter extends OncePerRequestFilter {
    private final JWTUtil jwtUtil;
    private final PersonDetailService personDetailService;

    @Autowired
    public JWTFilter(JWTUtil jwtUtil, PersonDetailService personDetailService) {
        this.jwtUtil = jwtUtil;
        this.personDetailService = personDetailService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if(authHeader != null && !authHeader.isBlank() && authHeader.startsWith("Bearer ")){
            String jwt = authHeader.substring(7);
            if(jwt.isBlank()){
                response.sendError(response.SC_BAD_REQUEST,"Ivalid JWT token in Bearer Header");
            }
            else {
                try {
                    String username = jwtUtil.validateTokenAndRetrieveClaim(jwt);
                    UserDetails userDetails =  personDetailService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, userDetails .getPassword(),userDetails.getAuthorities());
                    if(SecurityContextHolder.getContext().getAuthentication() == null){
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
                catch (JWTVerificationException e){
                    response.sendError(response.SC_BAD_REQUEST,"Invalid JWT token");
                }

            }

        }
        filterChain.doFilter(request,response);
    }
}
