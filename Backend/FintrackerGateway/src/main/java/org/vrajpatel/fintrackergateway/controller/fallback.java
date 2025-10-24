package org.vrajpatel.fintrackergateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class fallback {

    @RequestMapping("/fallback")
    public ResponseEntity<String> getFallback() {

        return ResponseEntity.ok("fallback");
    }
}
