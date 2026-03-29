package com.rumino.dto;

public record CommunityMoodDto(
        String domainName,
        String color,
        long count,
        int percentage
) {
}
