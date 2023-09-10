package sw.capstone.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RootRestController {

    @GetMapping("/health")
    public String healthAPi(){
        return "i'm healthy";
    }
}
