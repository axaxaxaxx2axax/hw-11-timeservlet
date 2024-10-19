package org.example;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.TimeZone;

@WebFilter("/time")
public class TimezoneValidateFilter extends HttpFilter implements Filter {

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String timezoneParam = request.getParameter("timezone");

        if (timezoneParam != null && !isValidTimezone(timezoneParam)) {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("<html><body><h1>Invalid timezone</h1></body></html>");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isValidTimezone(String timezoneParam) {
        try {
            if (timezoneParam.startsWith("UTC")) {
                int offset = Integer.parseInt(timezoneParam.substring(3).trim());
                TimeZone.getTimeZone("UTC" + (offset >= 0 ? "+" : "") + offset);
                return true;
            }
        } catch (NumberFormatException e) {
            return false;
        }
        return false;
    }

}