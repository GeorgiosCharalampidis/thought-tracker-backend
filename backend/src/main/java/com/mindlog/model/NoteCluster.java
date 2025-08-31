package com.mindlog.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Getter
public enum NoteCluster {
    WORK_AND_CAREER("Work & Career",
        "Job-related stress, deadlines, workload, difficult coworkers, office politics, burnout, " +
        "career aspirations, promotions, skills development, interviews, professional growth, career transitions."),

    MONEY_AND_FINANCES("Money & Finances",
        "Budgeting, expenses, saving, debt repayment, loans, rent, bills, financial goals, income stability, " +
        "financial anxiety, money-related stress, planning for financial decisions."),

    RELATIONSHIPS_ROMANTIC("Romantic Relationships",
        "Dating, intimacy, communication with a partner, love, trust issues, breakups, emotional closeness, " +
        "longing for companionship, relationship conflicts."),

    RELATIONSHIPS_FAMILY("Family",
        "Parents, siblings, children, caregiving, family conflicts, family responsibilities, " +
        "feeling supported or unsupported by family."),

    RELATIONSHIPS_FRIENDS("Friendships & Social Life",
        "Friends, socializing, building connections, conflicts with friends, peer pressure, support networks, " +
        "feeling included or excluded in social circles."),

    MENTAL_HEALTH_ANXIETY("Anxiety & Worry",
        "Worrying thoughts, fear of failure, overthinking, nervous tension, racing thoughts, rumination, " +
        "panic-like feelings, general unease without clear cause."),

    MENTAL_HEALTH_DEPRESSION("Depression & Low Mood",
        "Hopelessness, lack of motivation, fatigue, emptiness, negative self-talk, " +
        "loss of interest in activities, sadness, apathy."),

    MENTAL_HEALTH_LONELINESS("Loneliness & Isolation",
        "Feeling alone, disconnected from others, social isolation, wanting companionship, " +
        "feeling misunderstood or invisible."),

    MENTAL_HEALTH_POSITIVE("Positive Emotions",
        "Gratitude, joy, excitement, anticipation, contentment, satisfaction, " +
        "recognizing blessings, positive reflections and experiences."),

    HEALTH_PHYSICAL("Physical Health",
        "Physical illness, symptoms, chronic conditions, doctor visits, health tests, " +
        "personal well-being, nutrition, body image concerns."),

    FITNESS_AND_EXERCISE("Fitness & Exercise",
        "Workouts, running, gym, training plans, exercise routines, healthy habits, physical activity, sports."),

    SLEEP_AND_REST("Sleep & Rest",
        "Insomnia, trouble falling asleep, waking up at night, poor sleep quality, fatigue, restorative rest."),

    PERSONAL_GROWTH("Personal Growth & Meaning",
        "Self-improvement, habits, productivity, discipline, routines, learning, spirituality, " +
        "mindfulness, meditation, searching for purpose and meaning."),

    LIFE_TRANSITIONS("Life Transitions",
        "Major commitments, moving, marriage, career change, education decisions, lifestyle shifts, " +
        "long-term choices that shape life direction."),

    CREATIVITY_AND_HOBBIES("Creativity & Hobbies",
        "Hobbies, passions, art, writing, music, coding projects, creative blocks, artistic inspiration, " +
        "learning for fun, self-expression."),

    EXTERNAL_WORLD("External World & Media",
        "News, politics, global issues, economy, wars, disasters, climate change, social media fatigue, doomscrolling."),

    OPEN_REFLECTIONS("Open Reflections",
        "General musings, stray thoughts, daydreams, philosophical reflections, and notes without a strong theme; fallback category.");

    private final String label;
    private final String description;

    NoteCluster(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public static List<String> allLabels() {
        return Arrays.stream(values())
                .map(NoteCluster::getLabel)
                .collect(Collectors.toList());
    }

    public static List<String> allDescriptions() {
        return Arrays.stream(values())
                .map(NoteCluster::getDescription)
                .collect(Collectors.toList());
    }

    public static String normalizeToLabelOrThrow(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Subject cannot be null");
        }
        String trimmed = input.trim();
        String key = trimmed.toLowerCase(Locale.ROOT);
        for (NoteCluster c : values()) {
            if (c.getLabel().toLowerCase(Locale.ROOT).equals(key)) {
                return c.getLabel();
            }
        }
        throw new IllegalArgumentException("Unsupported subject: " + input);
    }
}
