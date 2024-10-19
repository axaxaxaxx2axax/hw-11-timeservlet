package org.example;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.*;
import java.time.format.DateTimeFormatter;

@WebServlet(value = "/time")
public class TimeServlet extends HttpServlet {

    private TemplateEngine engine;

    @Override
    public void init() throws ServletException {
        engine = new TemplateEngine();

        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");

        // Отримуємо параметр timezone з URL
        String timezoneParam = req.getParameter("timezone");

        // Якщо timezone не передано, спробуємо отримати його з Cookie
        if (timezoneParam == null || timezoneParam.isBlank()) {
            timezoneParam = getTimezoneFromCookie(req);
        }

        // Якщо все ще немає timezone, використовуємо UTC
        if (timezoneParam == null || timezoneParam.isBlank()) {
            timezoneParam = "UTC";
        }

        // Отримуємо час для вказаного часового поясу
        String formattedTime = getTimeForTimeZone(timezoneParam);

        // Якщо часова зона валідна, зберігаємо її в Cookie
        if (!formattedTime.equals("Invalid timezone format")) {
            saveTimezoneInCookie(timezoneParam, resp);
        }

        // Відображаємо час у шаблоні
        Context context = new Context(req.getLocale());
        context.setVariable("time", formattedTime);

        engine.process("test", context, resp.getWriter());
        resp.getWriter().close();
    }

    // Метод для отримання часу для конкретного часового поясу
    private String getTimeForTimeZone(String timeZoneParam) {
        try {
            ZoneId zoneId = (timeZoneParam != null && !timeZoneParam.isBlank()) ? ZoneId.of(timeZoneParam.replace(" ", "+")) : ZoneId.of("UTC");
            ZonedDateTime currentTime = ZonedDateTime.now(zoneId);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");
            return currentTime.format(formatter);
        } catch (Exception e) {
            return "Invalid timezone format";
        }
    }

    // Метод для отримання часового поясу з Cookie
    private String getTimezoneFromCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("lastTimezone".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    // Метод для збереження часового поясу в Cookie
    private void saveTimezoneInCookie(String timeZoneParam, HttpServletResponse resp) throws UnsupportedEncodingException {
        Cookie cookie = new Cookie("lastTimezone", URLEncoder.encode(timeZoneParam, "UTF-8"));
        cookie.setMaxAge(120);  // Cookie зберігається на 1 рік
        resp.addCookie(cookie);
    }
}

