package org.vrajpatel.personalexpense.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.vrajpatel.personalexpense.Service.PersonalExpenseService;

@RestController
@RequestMapping("/personalexpense")
@RequiredArgsConstructor
public class testing {

    private final PersonalExpenseService personalExpenseService;

    @GetMapping("/")
    public String test(@RequestHeader("userEmail") String userEmail, @RequestHeader("userId") String userId) {
        return "hello world " + userEmail +" User Id : "+ userId + " Categories :" + personalExpenseService.findAll().toString();
    }
}
