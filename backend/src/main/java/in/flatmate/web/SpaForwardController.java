package in.flatmate.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * The React build is served from static/, so deep links must fall back to
 * index.html or a refresh on /chat/xyz would 404.
 *
 * Routes are listed explicitly rather than using a catch-all: a catch-all would
 * also swallow /api, /ws and the Swagger resources.
 */
@Controller
public class SpaForwardController {

    @GetMapping({"/login", "/matches", "/requests", "/chats", "/safety", "/profile", "/chat/{id}"})
    public String forward() {
        return "forward:/index.html";
    }
}
