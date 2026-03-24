package com.mindlog.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Getter
public enum Category {
    WORK_STRESS("Work Stress & Burnout",
        "Job pressure, overwhelming workload, deadlines, work-life balance struggles, exhaustion from work, " +
        "feeling overwhelmed by responsibilities, burnout symptoms, work anxiety."),
    
    WORKPLACE_RELATIONSHIPS("Workplace Relationships",
        "Difficult coworkers, office politics, team dynamics, conflicts with boss or colleagues, " +
        "workplace communication issues, feeling unsupported at work."),
    
    CAREER_DEVELOPMENT("Career Development",
        "Career growth, professional development, job searching, promotions, skill building, " +
        "career planning, networking, professional goals."),

    ANXIETY_OVERTHINKING("Anxiety & Overthinking",
        "Worry, anxious thoughts, overthinking, racing mind, stress about future, " +
        "catastrophic thinking, mental loops, anxiety symptoms, general stress, " +
        "feeling overwhelmed by life, too much to handle, pressure from multiple sources, " +
        "political stress, news anxiety, doomscrolling, world events causing distress."),

    DEPRESSION_LOW_MOOD("Depression & Low Mood",
        "Sadness, feeling down, depression symptoms, low energy, hopelessness, " +
        "emotional numbness, lack of motivation, persistent sadness."),

    SELF_DOUBT_UNCERTAINTY("Self-Doubt & Uncertainty",
        "Questioning decisions, uncertainty about future, imposter syndrome, " +
        "lack of confidence in choices, feeling lost or directionless."),

    SELF_ESTEEM_SELF_WORTH("Self-Esteem & Self-Worth",
        "Self-confidence issues, self-worth concerns, feeling inadequate, " +
        "comparing to others, self-image problems."),

    MOTIVATION_ENERGY_ISSUES("Motivation & Energy Issues",
        "Lack of motivation, low energy, procrastination, feeling stuck, " +
        "difficulty getting started, energy depletion."),

    ANGER_FRUSTRATION("Anger & Frustration",
        "Feeling angry, frustrated, irritated, resentful, rage, annoyance, " +
        "losing temper, feeling wronged, injustice, venting, aggression."),

    GRIEF_LOSS("Grief & Loss",
        "Losing someone, death, bereavement, mourning, grief, heartbreak from loss, " +
        "end of a relationship, losing a job, losing something meaningful, missing someone who is gone."),

    // Relationships - More specific
    ROMANTIC_RELATIONSHIPS("Romantic Relationships",
        "Dating, romantic partners, relationship issues, love, intimacy, " +
        "communication with partner, relationship conflicts."),
    
    FRIENDSHIPS_SOCIAL_CONNECTIONS("Friendships & Social Connections",
        "Friends, social interactions, social anxiety, social activities, " +
        "maintaining friendships, social dynamics."),
    
    FAMILY_RELATIONSHIPS("Family Relationships",
        "Family dynamics, parents, siblings, family conflicts, family support, " +
        "family obligations, generational differences."),
    
    LONELINESS_DISCONNECTION("Loneliness & Disconnection",
        "Feeling lonely, social isolation, disconnected from others, " +
        "lack of meaningful connections, social withdrawal."),

    // Physical Health & Lifestyle
    PHYSICAL_HEALTH_ILLNESS("Physical Health & Illness",
        "Health concerns, medical issues, illness, physical symptoms, " +
        "doctor visits, health anxiety, chronic conditions."),
    
    FITNESS_PHYSICAL_ACTIVITY("Fitness & Physical Activity",
        "Exercise, workouts, physical fitness, sports, movement, " +
        "fitness goals, physical training, active lifestyle."),
    
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
    
    LEARNING_SKILL_DEVELOPMENT("Learning & Skill Development",
        "Learning new skills, education, courses, reading, intellectual growth, " +
        "professional development, knowledge acquisition."),
    
    CREATIVITY_ARTISTIC_EXPRESSION("Creativity & Artistic Expression",
        "Creative projects, art, writing, music, creative blocks, " +
        "artistic pursuits, creative inspiration, self-expression."),
    
    SPIRITUALITY_LIFE_MEANING("Spirituality & Life Meaning",
        "Spiritual practices, meditation, life purpose, meaning, values, " +
        "philosophical thoughts, spiritual growth, existential questions."),

    MONEY_FINANCES("Money & Finances",
        "Money worries, financial pressure, debt concerns, budgeting stress, " +
        "financial insecurity, money-related anxiety, financial planning, saving goals, " +
        "investment decisions, budgeting, financial literacy, money management, financial future."),
    
    MAJOR_LIFE_TRANSITIONS("Major Life Transitions",
        "Life changes, moving, new job, relationship changes, major decisions, " +
        "transition anxiety, adapting to change, life milestones."),
    
    HOUSING_LIVING_SITUATION("Housing & Living Situation",
        "Living arrangements, housing issues, roommate problems, " +
        "home environment, living space concerns."),

    ENVIRONMENT_NATURE("Environment & Nature",
        "Weather, seasons, nature connection, environmental concerns, " +
        "outdoor activities, natural world appreciation."),

    JOY_POSITIVE_EMOTIONS("Joy & Positive Emotions",
        "Happiness, gratitude, excitement, accomplishments, positive experiences, " +
        "celebrations, joyful moments, appreciation."),
    
    DAILY_LIFE_OBSERVATIONS("Daily Life & Observations",
        "Random thoughts, daily observations, mundane moments, everyday experiences, " +
        "simple reflections, general thoughts, philosophical musings, stream of consciousness, " +
        "miscellaneous thoughts, screen time, social media, digital life, technology use.");

    private final String displayName;
    private final String keywords;

    Category(String displayName, String keywords) {
        this.displayName = displayName;
        this.keywords = keywords;
    }

    public static List<String> getAllCategoryNames() {
        return Arrays.stream(values())
                .map(Category::getDisplayName)
                .collect(Collectors.toList());
    }

    public static List<String> allLabels() {
        return Arrays.stream(values())
                .map(Category::getDisplayName)
                .collect(Collectors.toList());
    }

    public static List<String> allDescriptions() {
        return Arrays.stream(values())
                .map(Category::getKeywords)
                .collect(Collectors.toList());
    }

    public static String normalizeToLabelOrThrow(String input) {
        if (input == null) {
            throw new IllegalArgumentException("Subject cannot be null");
        }
        String trimmed = input.trim();
        String key = trimmed.toLowerCase(Locale.ROOT);
        for (Category c : values()) {
            if (c.getDisplayName().toLowerCase(Locale.ROOT).equals(key)) {
                return c.getDisplayName();
            }
        }
        throw new IllegalArgumentException("Unsupported subject: " + input);
    }

    public static Category fromDisplayName(String displayName) {
        return Arrays.stream(values())
                .filter(category -> category.getDisplayName().equals(displayName))
                .findFirst()
                .orElse(DAILY_LIFE_OBSERVATIONS);
    }

    public static Category fromString(String input) {
        if (input == null) return DAILY_LIFE_OBSERVATIONS;

        String normalizedInput = input.trim().toUpperCase(Locale.ROOT);

        // Try exact enum name match first
        try {
            return Category.valueOf(normalizedInput);
        } catch (IllegalArgumentException e) {
            // Try display name match
            return fromDisplayName(input);
        }
    }
}
