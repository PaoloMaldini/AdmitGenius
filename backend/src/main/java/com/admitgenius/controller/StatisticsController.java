package com.admitgenius.controller;

import com.admitgenius.service.ForumService;
import com.admitgenius.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private UserService userService;

    @Autowired
    private ForumService forumService;

    @GetMapping("/community")
    public ResponseEntity<Map<String, Long>> getCommunityStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", userService.countTotalUsers());
        stats.put("totalPosts", forumService.countTotalPosts());
        // Placeholder for other stats, can be expanded later
        // stats.put("activeToday", 0L); // Example placeholder
        return ResponseEntity.ok(stats);
    }

    // Future methods for hot topics, active users etc. can be added here
}