package org.vrajpatel.personalexpense.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.vrajpatel.personalexpense.Service.AnalyticsService;
import org.vrajpatel.personalexpense.responseDto.AnalyticsDTO;

import java.time.LocalDate;
import java.util.Date;

@RestController
@RequestMapping("/personal/api")
public class AnalyticController {

    private final Logger log = LoggerFactory.getLogger(AnalyticController.class);

    private final AnalyticsService analyticsService;

    AnalyticController(AnalyticsService analyticsService) {
        this.analyticsService= analyticsService;
    }

    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsDTO> analytics(@RequestHeader("userEmail") String userEmail ,
                                                  @RequestHeader("userId") String userId, @RequestParam(name="date") String todayDate ) throws Exception {
        return ResponseEntity.ok(this.analyticsService.generateAnalytics(userEmail,userId, LocalDate.parse(todayDate)));
    }
}
