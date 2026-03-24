package com.mindlog.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum SubCategory {
    // Work Stress & Burnout sub-categories
    WORK_OVERWHELM("Work Overwhelm", Category.WORK_STRESS,
        "Feeling overwhelmed by workload, too much work, can't handle work pressure, work is too much"),
    WORK_BURNOUT("Work Burnout", Category.WORK_STRESS, 
        "Work exhaustion, burnt out from work, tired of job, work fatigue, emotional exhaustion"),
    WORK_DEADLINES("Work Deadlines", Category.WORK_STRESS,
        "Deadline stress, time pressure at work, rushing to meet deadlines, tight work schedules"),
    
    // Workplace Relationships sub-categories
    WORKPLACE_CONFLICTS("Workplace Conflicts", Category.WORKPLACE_RELATIONSHIPS,
        "Conflicts with coworkers, office politics, difficult colleagues, workplace drama"),
    WORKPLACE_ISOLATION("Workplace Isolation", Category.WORKPLACE_RELATIONSHIPS, 
        "Feeling isolated at work, no work friends, excluded from workplace social activities"),
    WORKPLACE_POLITICS("Office Politics", Category.WORKPLACE_RELATIONSHIPS,
        "Navigating office dynamics, workplace favoritism, political tensions at work"),

    // Career Development sub-categories
    CAREER_GROWTH("Career Growth", Category.CAREER_DEVELOPMENT,
        "Career advancement, promotions, professional development, career goals"),
    CAREER_TRANSITION("Career Change", Category.CAREER_DEVELOPMENT,
        "Job searching, career changes, switching careers, new job opportunities"),
    SKILL_DEVELOPMENT("Skill Building", Category.CAREER_DEVELOPMENT,
        "Learning new skills, professional training, competency development, certifications"),

    // Financial Stress & Anxiety sub-categories
    MONEY_WORRIES("Money Worries", Category.MONEY_FINANCES,
        "Financial anxiety, money stress, worried about finances, financial insecurity"),
    DEBT_STRESS("Debt Stress", Category.MONEY_FINANCES,
        "Debt burden, loan payments, credit card debt, financial obligations"),
    BILL_STRUGGLES("Bill Payment Struggles", Category.MONEY_FINANCES,
        "Can't pay bills, rent stress, utility bills, struggling with expenses"),

    // Financial Planning & Goals sub-categories
    BUDGETING("Budgeting", Category.MONEY_FINANCES,
        "Creating budget, tracking expenses, managing money, financial planning"),
    SAVING_GOALS("Saving Goals", Category.MONEY_FINANCES,
        "Saving money, building emergency fund, financial goals, investment planning"),
    FINANCIAL_DECISIONS("Financial Decisions", Category.MONEY_FINANCES,
        "Making financial choices, investment decisions, spending decisions, money management"),

    // Romantic Relationships sub-categories
    COMMUNICATION_ISSUES("Communication Issues", Category.ROMANTIC_RELATIONSHIPS,
        "Communication problems, misunderstandings, not being heard, relationship talks"),
    INTIMACY_CONNECTION("Intimacy & Connection", Category.ROMANTIC_RELATIONSHIPS,
        "Physical intimacy, emotional connection, closeness, romantic bonding"),
    RELATIONSHIP_CONFLICTS("Relationship Conflicts", Category.ROMANTIC_RELATIONSHIPS,
        "Arguments, fights, relationship problems, partner disagreements"),

    // Family Relationships sub-categories
    FAMILY_CONFLICTS("Family Conflicts", Category.FAMILY_RELATIONSHIPS,
        "Family arguments, conflicts with parents, sibling disputes, family drama"),
    FAMILY_SUPPORT("Family Support", Category.FAMILY_RELATIONSHIPS,
        "Family help, supportive family, family love, family encouragement"),
    FAMILY_RESPONSIBILITIES("Family Duties", Category.FAMILY_RELATIONSHIPS,
        "Family obligations, caring for family, family responsibilities, family caregiving"),

    // Friendships & Social Connections sub-categories
    FRIEND_CONFLICTS("Friend Conflicts", Category.FRIENDSHIPS_SOCIAL_CONNECTIONS,
        "Friendship problems, conflicts with friends, friend drama, social disputes"),
    MAKING_FRIENDS("Making New Friends", Category.FRIENDSHIPS_SOCIAL_CONNECTIONS,
        "Meeting new people, building friendships, social connections, networking"),
    SOCIAL_ACTIVITIES("Social Activities", Category.FRIENDSHIPS_SOCIAL_CONNECTIONS,
        "Hanging out with friends, social events, group activities, social gatherings"),

    // Loneliness & Disconnection sub-categories
    EMOTIONAL_LONELINESS("Emotional Loneliness", Category.LONELINESS_DISCONNECTION,
        "Feeling lonely, isolated, disconnected, alone, lacking companionship"),
    SOCIAL_ISOLATION("Social Isolation", Category.LONELINESS_DISCONNECTION,
        "Cut off from others, withdrawn, avoiding social contact, feeling excluded"),
    MISSING_CONNECTION("Missing Connection", Category.LONELINESS_DISCONNECTION,
        "Longing for connection, wanting deeper relationships, craving intimacy"),

    // Anxiety & Overthinking sub-categories
    GENERAL_ANXIETY("General Anxiety", Category.ANXIETY_OVERTHINKING,
        "Anxious feelings, worry, nervous, anxious thoughts, general anxiety"),
    OVERTHINKING("Overthinking", Category.ANXIETY_OVERTHINKING,
        "Racing thoughts, can't stop thinking, mental loops, overthinking everything"),
    PERFORMANCE_ANXIETY("Performance Anxiety", Category.ANXIETY_OVERTHINKING,
        "Fear of failure, performance pressure, test anxiety, presentation nerves"),

    // Depression & Low Mood sub-categories
    SADNESS("Sadness", Category.DEPRESSION_LOW_MOOD,
        "Feeling sad, down, melancholy, blue, sorrowful, heavy heart"),
    HOPELESSNESS("Hopelessness", Category.DEPRESSION_LOW_MOOD,
        "No hope, nothing will get better, feeling hopeless, no point, giving up hope"),
    EMPTINESS("Emotional Emptiness", Category.DEPRESSION_LOW_MOOD,
        "Feeling empty inside, numb, void, hollow, emotionally drained, nothing inside"),

    // Stress & Feeling Overwhelmed sub-categories
    LIFE_OVERWHELM("Life Overwhelm", Category.ANXIETY_OVERTHINKING,
        "Too much going on, life is overwhelming, can't handle everything, too much stress"),
    MULTIPLE_PRESSURES("Multiple Pressures", Category.ANXIETY_OVERTHINKING,
        "Pressure from different areas, pulled in many directions, competing demands, stretched thin"),
    STRESS_SYMPTOMS("Stress Symptoms", Category.ANXIETY_OVERTHINKING,
        "Physical stress symptoms, stress affecting body, tension, stress-related issues"),

    // Self-Esteem & Self-Worth sub-categories
    CONFIDENCE_ISSUES("Confidence Issues", Category.SELF_ESTEEM_SELF_WORTH,
        "Low confidence, don't believe in myself, lack of self-confidence, feeling insecure"),
    SELF_CRITICISM("Self-Criticism", Category.SELF_ESTEEM_SELF_WORTH,
        "Being hard on myself, self-judgment, negative self-talk, self-blame"),
    SELF_PRIDE_CONFIDENCE("Self Pride & Confidence", Category.SELF_ESTEEM_SELF_WORTH,
        "Proud of myself, feeling confident, self-accomplishment, positive self-regard"),

    // Self-Doubt & Uncertainty sub-categories
    ABILITY_DOUBT("Ability Doubt", Category.SELF_DOUBT_UNCERTAINTY,
        "Doubting my abilities, questioning my skills, imposter syndrome, not good enough"),
    LIFE_PATH_UNCERTAINTY("Life Path Uncertainty", Category.SELF_DOUBT_UNCERTAINTY,
        "Don't know direction, uncertain about future, questioning life choices"),
    DECISION_PARALYSIS("Decision Paralysis", Category.SELF_DOUBT_UNCERTAINTY,
        "Can't make decisions, paralyzed by choices, afraid of wrong choice"),

    // Motivation & Energy Issues sub-categories
    LOW_ENERGY("Low Energy", Category.MOTIVATION_ENERGY_ISSUES,
        "Tired, exhausted, drained, low energy, physically depleted"),
    LACK_MOTIVATION("Lack of Motivation", Category.MOTIVATION_ENERGY_ISSUES,
        "No motivation, don't want to do anything, unmotivated, lacking drive"),
    PROCRASTINATION("Procrastination", Category.MOTIVATION_ENERGY_ISSUES,
        "Putting things off, delaying tasks, avoiding work, procrastinating"),

    // Physical Health & Illness sub-categories
    HEALTH_ANXIETY("Health Anxiety", Category.PHYSICAL_HEALTH_ILLNESS,
        "Worried about health, health concerns, medical anxiety, fear of illness"),
    CHRONIC_ILLNESS("Chronic Illness", Category.PHYSICAL_HEALTH_ILLNESS,
        "Living with chronic condition, managing illness, health struggles"),
    MEDICAL_CONCERNS("Medical Concerns", Category.PHYSICAL_HEALTH_ILLNESS,
        "Doctor visits, medical tests, health problems, medical issues"),

    // Fitness & Physical Activity sub-categories
    FITNESS_ACHIEVEMENT("Fitness Achievement", Category.FITNESS_PHYSICAL_ACTIVITY,
        "Workout success, exercise accomplishment, fitness goals achieved, physical progress"),
    FITNESS_MOTIVATION("Fitness Motivation", Category.FITNESS_PHYSICAL_ACTIVITY,
        "Motivated to exercise, fitness inspiration, workout enthusiasm"),
    FITNESS_STRUGGLES("Fitness Struggles", Category.FITNESS_PHYSICAL_ACTIVITY,
        "Hard to exercise, fitness challenges, workout difficulties, physical limitations"),

    // Sleep & Energy Issues sub-categories
    SLEEP_PROBLEMS("Sleep Problems", Category.SLEEP_FATIGUE,
        "Can't sleep, insomnia, sleep difficulties, restless sleep, poor sleep quality"),
    SLEEP_HABITS("Sleep Habits", Category.SLEEP_FATIGUE,
        "Sleep routine, bedtime habits, sleep schedule, keep telling myself to sleep, never sleep early, sleep procrastination"),
    FATIGUE("Fatigue", Category.SLEEP_FATIGUE,
        "Tired all the time, exhausted, fatigue, low energy, feeling drained"),

    // Nutrition & Eating sub-categories
    HEALTHY_EATING("Healthy Eating", Category.NUTRITION_EATING,
        "Eating well, nutritious food, healthy diet, good food choices"),
    FOOD_STRESS("Food & Stress", Category.NUTRITION_EATING,
        "Stress eating, emotional eating, food and emotions, eating when stressed"),
    EATING_HABITS("Eating Habits", Category.NUTRITION_EATING,
        "Food routines, meal patterns, eating behaviors, relationship with food"),

    // Habits & Daily Routine sub-categories
    HABIT_BUILDING("Habit Building", Category.HABITS_ROUTINE,
        "Creating new habits, building routines, habit formation, consistency"),
    ROUTINE_STRUGGLES("Routine Struggles", Category.HABITS_ROUTINE,
        "Can't stick to routine, inconsistent habits, routine challenges"),
    PRODUCTIVITY("Productivity", Category.HABITS_ROUTINE,
        "Getting things done, being productive, time management, efficiency"),

    // Learning & Skill Development sub-categories
    LEARNING_MOTIVATION("Learning Motivation", Category.LEARNING_SKILL_DEVELOPMENT,
        "Want to learn, motivated to study, excited about learning, intellectual curiosity"),
    LEARNING_STRUGGLES("Learning Challenges", Category.LEARNING_SKILL_DEVELOPMENT,
        "Difficulty learning, learning obstacles, study challenges, academic stress"),
    SKILL_PROGRESS("Skill Progress", Category.LEARNING_SKILL_DEVELOPMENT,
        "Making progress, skill improvement, learning success, mastering skills"),

    // Creativity & Artistic Expression sub-categories
    CREATIVE_INSPIRATION("Creative Inspiration", Category.CREATIVITY_ARTISTIC_EXPRESSION,
        "Feeling creative, artistic inspiration, creative energy, artistic motivation"),
    CREATIVE_BLOCKS("Creative Blocks", Category.CREATIVITY_ARTISTIC_EXPRESSION,
        "Creative block, can't create, artistic struggles, lack of inspiration"),
    CREATIVE_ACHIEVEMENT("Creative Achievement", Category.CREATIVITY_ARTISTIC_EXPRESSION,
        "Creative success, artistic accomplishment, finished creative project, proud of art"),

    // Spirituality & Life Meaning sub-categories
    SPIRITUAL_PEACE("Spiritual Peace", Category.SPIRITUALITY_LIFE_MEANING,
        "Inner peace, spiritual calm, meditation benefits, mindfulness, spiritual connection"),
    SPIRITUAL_SEEKING("Spiritual Seeking", Category.SPIRITUALITY_LIFE_MEANING,
        "Spiritual journey, seeking meaning, spiritual exploration, faith questions"),
    PHILOSOPHICAL_THOUGHTS("Philosophical Thoughts", Category.SPIRITUALITY_LIFE_MEANING,
        "Deep thoughts, philosophical questions, existential thinking, wonder what life would feel like, wondering if we know ourselves, existential questions"),

    // Major Life Transitions sub-categories
    TRANSITION_ANXIETY("Transition Anxiety", Category.MAJOR_LIFE_TRANSITIONS,
        "Nervous about change, transition stress, anxiety about new phase, change worries"),
    TRANSITION_EXCITEMENT("Transition Excitement", Category.MAJOR_LIFE_TRANSITIONS,
        "Excited about change, looking forward to transition, positive about new chapter"),
    ADAPTATION_STRUGGLES("Adaptation Struggles", Category.MAJOR_LIFE_TRANSITIONS,
        "Struggling with change, hard to adapt, adjustment difficulties, transition challenges"),

    // Housing & Living Situation sub-categories
    HOUSING_STRESS("Housing Stress", Category.HOUSING_LIVING_SITUATION,
        "Housing problems, living situation stress, home environment issues"),
    ROOMMATE_ISSUES("Roommate Issues", Category.HOUSING_LIVING_SITUATION,
        "Roommate conflicts, living with others, shared space problems"),
    HOME_COMFORT("Home Comfort", Category.HOUSING_LIVING_SITUATION,
        "Love my home, comfortable living space, home as sanctuary"),

    // News & Political Events sub-categories
    NEWS_STRESS("News Stress", Category.ANXIETY_OVERTHINKING,
        "News anxiety, overwhelmed by current events, political stress, world events worry"),
    POLITICAL_ENGAGEMENT("Political Engagement", Category.ANXIETY_OVERTHINKING,
        "Political involvement, civic engagement, political discussions, activism"),
    SOCIAL_ISSUES("Social Issues", Category.ANXIETY_OVERTHINKING,
        "Social justice, inequality concerns, societal problems, community issues"),

    // Technology & Digital Life sub-categories
    DIGITAL_OVERWHELM("Digital Overwhelm", Category.DAILY_LIFE_OBSERVATIONS,
        "Screen time, digital fatigue, technology stress, doomscrolling, social media draining, " +
        "internet addiction, can't stop scrolling"),
    DIGITAL_DETOX("Digital Detox", Category.DAILY_LIFE_OBSERVATIONS,
        "Want to disconnect, need break from technology, digital detox, unplugging"),
    SOCIAL_MEDIA("Social Media", Category.DAILY_LIFE_OBSERVATIONS,
        "Social media stress, online interactions, digital socializing, internet behavior"),

    // Environment & Nature sub-categories
    NATURE_CONNECTION("Nature Connection", Category.ENVIRONMENT_NATURE,
        "Love nature, outdoor time, natural beauty, connection with environment"),
    WEATHER_MOOD("Weather & Mood", Category.ENVIRONMENT_NATURE,
        "Weather affecting mood, seasonal feelings, climate impact on emotions"),
    ENVIRONMENTAL_CONCERNS("Environmental Concerns", Category.ENVIRONMENT_NATURE,
        "Climate change worry, environmental anxiety, nature conservation concerns"),

    // Joy & Positive Emotions sub-categories
    HAPPINESS("Happiness", Category.JOY_POSITIVE_EMOTIONS,
        "Feeling happy, joyful, content, pleased, delighted, cheerful"),
    GRATITUDE("Gratitude", Category.JOY_POSITIVE_EMOTIONS,
        "Thankful, grateful, appreciation, counting blessings, feeling blessed"),
    EXCITEMENT("Excitement", Category.JOY_POSITIVE_EMOTIONS,
        "Excited, enthusiastic, looking forward, anticipation, thrilled"),

    // Daily Life & Observations sub-categories
    MUNDANE_MOMENTS("Mundane Moments", Category.DAILY_LIFE_OBSERVATIONS,
        "Everyday observations, routine moments, ordinary experiences, daily life thoughts"),
    PERSONAL_INSIGHTS("Personal Insights", Category.DAILY_LIFE_OBSERVATIONS,
        "Self-discovery, personal realizations, insights about myself, learning about myself"),
    LIFE_REFLECTIONS("Life Reflections", Category.DAILY_LIFE_OBSERVATIONS,
        "Thinking about life, life observations, general reflections, pondering"),

    // Open Reflections sub-categories
    RANDOM_THOUGHTS("Random Thoughts", Category.DAILY_LIFE_OBSERVATIONS,
        "Random ideas, scattered thoughts, miscellaneous thinking, various topics"),
    STREAM_CONSCIOUSNESS("Stream of Consciousness", Category.DAILY_LIFE_OBSERVATIONS,
        "Stream of thoughts, unfiltered thinking, mental wandering, free-flowing ideas"),
    GENERAL_MUSINGS("General Musings", Category.DAILY_LIFE_OBSERVATIONS,
        "General thoughts, pondering, wondering, casual reflections, thinking out loud");

    private final String label;
    private final Category parentCategory;
    private final String description;

    SubCategory(String label, Category parentCategory, String description) {
        this.label = label;
        this.parentCategory = parentCategory;
        this.description = description;
    }

    public static List<SubCategory> getSubCategoriesForParent(String parentCategoryName) {
        return Arrays.stream(values())
                .filter(sub -> sub.getParentCategory().getDisplayName().equals(parentCategoryName))
                .collect(Collectors.toList());
    }

    public static List<SubCategory> getSubCategoriesForParent(Category parentCategory) {
        return Arrays.stream(values())
                .filter(sub -> sub.getParentCategory().equals(parentCategory))
                .collect(Collectors.toList());
    }

    public static List<String> getAllSubCategoryLabels() {
        return Arrays.stream(values())
                .map(SubCategory::getLabel)
                .collect(Collectors.toList());
    }

    public static SubCategory fromLabel(String label) {
        return Arrays.stream(values())
                .filter(subCategory -> subCategory.getLabel().equals(label))
                .findFirst()
                .orElse(RANDOM_THOUGHTS);
    }
}
