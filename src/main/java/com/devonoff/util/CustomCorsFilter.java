package com.devonoff.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class CustomCorsFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {
    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
    System.out.println(request.getHeader("Origin"));
//    System.out.println(request.getHeader("Access-Control-Allow-Origin"));
//    System.out.println(request.getHeaderNames());
//    System.out.println(request.getServerName());
//    System.out.println(request.getRequestURL());
    System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");

    response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
//    response.setHeader("Access-Control-Allow-Origin", "https://devonoff-develop-test.vercel.app");
    response.setHeader("Access-Control-Allow-Credentials", "true");
    response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
    response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type");
    response.setHeader("Host", "https://doanything.shop");
    response.setHeader("Server", "https://doanything.shop");
    filterChain.doFilter(request, response);
  }
}