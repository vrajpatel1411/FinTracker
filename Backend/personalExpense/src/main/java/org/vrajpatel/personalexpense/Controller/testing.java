package org.vrajpatel.personalexpense.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/personalexpense")
public class testing {

    @GetMapping("/")
    public String test(@RequestHeader("userEmail") String userEmail) {


        return "hello world " + userEmail;
    }
}
