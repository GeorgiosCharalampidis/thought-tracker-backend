package com.mindlog.service;

import com.mindlog.dto.CommunityMoodDto;
import com.mindlog.repository.NoteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CommunityMoodService {

    private record DomainDef(String name, String color, List<String> categories) {}

    private static final List<DomainDef> DOMAIN_GROUPS = List.of(
            new DomainDef("Work & Career", "#f59e0b", List.of(
                    "Work Stress & Burnout", "Workplace Relationships", "Career Development")),
            new DomainDef("Mental Health", "#8b5cf6", List.of(
                    "Anxiety & Overthinking", "Depression & Low Mood", "Self-Doubt & Uncertainty",
                    "Stress & Feeling Overwhelmed", "Self-Esteem & Self-Worth", "Motivation & Energy Issues")),
            new DomainDef("Relationships", "#f43f5e", List.of(
                    "Romantic Relationships", "Friendships & Social Connections",
                    "Family Relationships", "Loneliness & Disconnection")),
            new DomainDef("Physical Health", "#10b981", List.of(
                    "Physical Health & Illness", "Fitness & Physical Activity",
                    "Sleep & Energy Issues", "Nutrition & Eating", "Environment & Nature")),
            new DomainDef("Personal Development", "#0ea5e9", List.of(
                    "Habits & Daily Routine", "Learning & Skill Development",
                    "Creativity & Artistic Expression", "Spirituality & Life Meaning")),
            new DomainDef("Finance", "#f97316", List.of(
                    "Financial Stress & Anxiety", "Financial Planning & Goals")),
            new DomainDef("Joy", "#facc15", List.of("Joy & Positive Emotions")),
            new DomainDef("Life & World", "#64748b", List.of(
                    "Major Life Transitions", "Housing & Living Situation", "News & Political Events",
                    "Technology & Digital Life", "Daily Life & Observations", "Open Reflections"))
    );

    private final NoteRepository noteRepository;

    public CommunityMoodService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public List<CommunityMoodDto> getCommunityMood() {
        LocalDate since = LocalDate.now().minusDays(7);
        List<Object[]> rows = noteRepository.countByCategoryAndDateAfter(since);

        Map<String, Long> categoryCounts = new java.util.HashMap<>();
        for (Object[] row : rows) {
            String category = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            categoryCounts.put(category, count);
        }

        long total = categoryCounts.values().stream().mapToLong(Long::longValue).sum();

        List<CommunityMoodDto> result = new ArrayList<>();
        for (DomainDef domain : DOMAIN_GROUPS) {
            long domainCount = domain.categories().stream()
                    .mapToLong(cat -> categoryCounts.getOrDefault(cat, 0L))
                    .sum();
            if (domainCount > 0) {
                int percentage = total > 0 ? (int) Math.round((domainCount * 100.0) / total) : 0;
                result.add(new CommunityMoodDto(domain.name(), domain.color(), domainCount, percentage));
            }
        }

        result.sort((a, b) -> Long.compare(b.count(), a.count()));
        return result;
    }
}
