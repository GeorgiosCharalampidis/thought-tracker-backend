package com.rumino.controller;

import com.rumino.dto.CommunityMoodDto;
import com.rumino.service.CommunityMoodService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/community")
public class CommunityController {

    private final CommunityMoodService communityMoodService;

    public CommunityController(CommunityMoodService communityMoodService) {
        this.communityMoodService = communityMoodService;
    }

    @GetMapping("/mood")
    public ResponseEntity<List<CommunityMoodDto>> getCommunityMood(Authentication authentication) {
        return ResponseEntity.ok(communityMoodService.getCommunityMood());
    }
}
