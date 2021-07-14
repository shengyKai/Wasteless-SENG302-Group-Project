package org.seng302.leftovers.middleware;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@ControllerAdvice
public class CookieRefresher implements ResponseBodyAdvice<Object> {

    private static final String AUTH_TOKEN_NAME = "AUTHTOKEN";
    private static Logger logger = LogManager.getLogger(CookieRefresher.class.getName());


    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        ServletServerHttpRequest req = (ServletServerHttpRequest)request;
        ServletServerHttpResponse res = (ServletServerHttpResponse)response;
        extendSession(req.getServletRequest(), res.getServletResponse());
        return body;
    }

    /**
     * Attempts to extent the current session time
     * Only valid for existing sessions with current authentication tokens.
     * @param request The HTTP request
     * @param response The HTTP response
     */
    private void extendSession(HttpServletRequest request,
                               HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();

        HttpSession session = request.getSession();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().contentEquals(AUTH_TOKEN_NAME) &&
                        cookie.getValue().equals(session.getAttribute(AUTH_TOKEN_NAME))) {
                    cookie.setPath("/");
                    cookie.setHttpOnly(true);
                    cookie.setMaxAge(60 * 60); // extend the cookie by an hour
                    response.addCookie(cookie);

                    logger.debug("Extending Session with token: {}", cookie.getValue());
                    break;
                }
            }
        }
    }

}
