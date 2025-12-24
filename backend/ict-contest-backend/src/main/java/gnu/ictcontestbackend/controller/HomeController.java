package gnu.ictcontestbackend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/gnu-weather")
public class HomeController {

    @GetMapping({"/",""})
    public String homeRedirect() {
        return home();
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }
}
