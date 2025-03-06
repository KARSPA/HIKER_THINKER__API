package fr.karspa.hiker_thinker.controller;

import fr.karspa.hiker_thinker.dtos.LoginDTO;
import fr.karspa.hiker_thinker.dtos.RegisterDTO;
import fr.karspa.hiker_thinker.services.auth.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/test")
public class TestController {

    @Autowired
    private AuthService authService;

    @GetMapping("/")
    public String index() {
        return "Index";
    }
    @GetMapping("/home")
    public String home() {
        return "Home";
    }
    @GetMapping("/admin")
    public String admin() {
        return "Admin";
    }
    @GetMapping("/client")
    public String client() {
        return "Client";
    }

}
