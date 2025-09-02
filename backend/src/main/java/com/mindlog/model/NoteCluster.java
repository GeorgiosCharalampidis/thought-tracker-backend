package com.mindlog.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Getter
public enum NoteCluster {
    // Work & Career - More specific categories
    WORK_STRESS("Work Stress & Burnout",
        "Job pressure, overwhelming workload, deadlines, work-life balance struggles, exhaustion from work, " +
        "feeling overwhelmed by responsibilities, burnout symptoms, work anxiety."),
    
    WORK_RELATIONSHIPS("Workplace Relationships",
        "Difficult coworkers, office politics, team dynamics, conflicts with boss or colleagues, " +
        "workplace communication issues, feeling unsupported at work."),
    
    CAREER_GROWTH("Career Development",
        "Career aspirations, promotions, skills development, interviews, professional growth, " +
        "job searching, career transitions, professional goals and achievements."),

    // Financial - More specific categories
    FINANCIAL_STRESS("Financial Stress & Anxiety",
        "Money worries, financial anxiety, struggling to pay bills, debt stress, " +
        "fear of financial instability, money-related panic or overwhelm."),
    
    FINANCIAL_PLANNING("Financial Planning & Goals",
        "Budgeting, saving strategies, financial goals, investment planning, " +
        "retirement planning, financial decision-making, money management."),

    // Relationships - More granular
    ROMANTIC_RELATIONSHIPS("Romantic Relationships",
        "Dating experiences, relationship dynamics, intimacy, communication with partner, " +
        "love and affection, relationship conflicts, breakups, longing for romance."),
    
    FAMILY_DYNAMICS("Family Relationships",
        "Parents, siblings, children, family conflicts, family responsibilities, " +
        "generational differences, family support or lack thereof, caregiving."),
    
    FRIENDSHIPS("Friendships & Social Connections",
        "Friend relationships, social activities, building new friendships, " +
        "conflicts with friends, feeling supported or excluded socially."),
    
    SOCIAL_ANXIETY("Social Anxiety & Isolation",
        "Fear of social situations, feeling awkward in groups, social performance anxiety, " +
        "avoiding social events, difficulty connecting with others, social isolation."),

    // Mental Health - More specific
    ANXIETY_WORRY("Anxiety & Overthinking",
        "Worrying thoughts, fear of failure, overthinking situations, racing thoughts, " +
        "rumination, nervous tension, anticipatory anxiety, general unease."),
    
    DEPRESSION_MOOD("Depression & Low Mood",
        "Sadness, hopelessness, lack of motivation, emotional numbness, feeling empty, " +
        "loss of interest in activities, negative self-talk, despair."),
    
    STRESS_OVERWHELM("Stress & Feeling Overwhelmed",
        "Feeling overwhelmed by life, too much to handle, stress from multiple sources, " +
        "pressure from various areas of life, feeling stretched thin."),
    
    SELF_ESTEEM("Self-Esteem & Self-Worth",
        "Self-doubt, feeling inadequate, imposter syndrome, comparing self to others, " +
        "confidence issues, self-criticism, feelings of worthlessness or pride, " +
        "uncertainty about abilities, questioning decisions, feeling unsure about choices."),
    
    LONELINESS("Loneliness & Disconnection",
        "Feeling alone, disconnected from others, wanting companionship, " +
        "feeling misunderstood or invisible, emotional isolation."),
    
    POSITIVE_EMOTIONS("Joy & Positive Emotions",
        "Happiness, gratitude, excitement, contentment, satisfaction, achievement, " +
        "positive experiences, feeling blessed, moments of joy."),

    // Health & Wellness - More specific
    PHYSICAL_HEALTH("Physical Health & Illness",
        "Physical symptoms, illness, chronic conditions, doctor visits, health concerns, " +
        "medical tests, pain, physical discomfort, health anxiety."),
    
    BODY_IMAGE("Body Image & Appearance",
        "Concerns about physical appearance, body image issues, weight concerns, " +
        "self-perception of looks, appearance-related anxiety or confidence."),
    
    FITNESS_EXERCISE("Fitness & Physical Activity",
        "Workouts, exercise routines, sports, physical training, fitness goals, " +
        "athletic performance, movement and physical activity, motivation for fitness, " +
        "exercise enthusiasm, active lifestyle choices."),
    
    SLEEP_FATIGUE("Sleep & Energy Issues",
        "Sleep problems, insomnia, fatigue, tiredness, sleep quality, " +
        "difficulty falling asleep, restless nights, energy levels."),
    
    NUTRITION_EATING("Nutrition & Eating",
        "Food choices, eating habits, nutrition concerns, diet, appetite changes, " +
        "relationship with food, meal planning, healthy eating."),

    // Personal Development - More specific
    HABITS_ROUTINE("Habits & Daily Routine",
        "Building habits, morning routines, productivity systems, time management, " +
        "routine establishment, habit tracking, daily structure."),
    
    LEARNING_GROWTH("Learning & Skill Development",
        "Learning new skills, education, courses, reading, intellectual growth, " +
        "knowledge acquisition, studying, personal development."),
    
    SPIRITUALITY_MEANING("Spirituality & Life Meaning",
        "Spiritual practices, meditation, mindfulness, searching for purpose, " +
        "existential questions, meaning of life, spiritual growth."),
    
    GOALS_ACHIEVEMENT("Goals & Achievement",
        "Setting goals, working toward objectives, achievement and success, " +
        "progress tracking, accomplishments, ambitions, life direction."),

    MOTIVATION_ENERGY("Motivation & Energy Issues",
        "Lack of motivation, losing interest in activities, don't feel like doing things anymore, " +
        "low energy for pursuits, giving up on activities, not wanting to continue, " +
        "feeling unmotivated, loss of drive, procrastination, avoiding activities."),

    SELF_DOUBT_UNCERTAINTY("Self-Doubt & Uncertainty",
        "Questioning decisions, feeling uncertain about choices, not sure about paths, " +
        "second-guessing yourself, uncertain about abilities, wavering on decisions, " +
        "feeling unsure about directions, indecisiveness, confusion about what to do."),

    // Life Events & Transitions
    MAJOR_LIFE_CHANGES("Major Life Transitions",
        "Moving, marriage, divorce, job changes, major life decisions, " +
        "significant life events, lifestyle changes, life transitions."),
    
    SEASONAL_WEATHER("Seasonal & Weather",
        "Seasonal changes, weather effects on mood, seasonal depression, " +
        "holiday feelings, seasonal activities, weather-related thoughts."),

    // Creative & Leisure
    CREATIVITY_ARTS("Creativity & Artistic Expression",
        "Art, writing, music, creative projects, artistic inspiration, " +
        "creative blocks, self-expression through art, creative pursuits."),
    
    HOBBIES_INTERESTS("Hobbies & Personal Interests",
        "Leisure activities, hobbies, personal interests, recreational pursuits, " +
        "passion projects, fun activities, entertainment."),
    
    TECHNOLOGY_DIGITAL("Technology & Digital Life",
        "Social media, screen time, digital overwhelm, technology frustrations, " +
        "online experiences, digital habits, tech-related thoughts."),

    // External World
    NEWS_POLITICS("News & Political Events",
        "Current events, politics, news consumption, political opinions, " +
        "world events, social issues, civic engagement, political stress."),
    
    ENVIRONMENT_NATURE("Environment & Nature",
        "Nature experiences, environmental concerns, outdoor activities, " +
        "connection with nature, weather appreciation, environmental anxiety."),

    // Catch-all
    DAILY_OBSERVATIONS("Daily Life & Observations",
        "Everyday experiences, mundane observations, daily routine thoughts, " +
        "simple life moments, ordinary experiences, casual reflections."),
    
    OPEN_REFLECTIONS("Open Reflections",
        "General musings, stray thoughts, daydreams, philosophical reflections, " +
        "abstract thinking, miscellaneous thoughts without specific theme.");

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
