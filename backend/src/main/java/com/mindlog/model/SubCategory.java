package com.mindlog.model;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum SubCategory {
    // Work Stress & Burnout sub-categories
    WORK_OVERWHELM("Work Overwhelm", "Work Stress & Burnout",
        "Feeling overwhelmed by workload, too much work, can't handle work pressure, work is too much"),
    WORK_BURNOUT("Work Burnout", "Work Stress & Burnout", 
        "Work exhaustion, burnt out from work, tired of job, work fatigue, emotional exhaustion"),
    WORK_DEADLINES("Work Deadlines", "Work Stress & Burnout",
        "Deadline stress, time pressure at work, rushing to meet deadlines, tight work schedules"),
    
    // Workplace Relationships sub-categories
    WORKPLACE_CONFLICTS("Workplace Conflicts", "Workplace Relationships",
        "Arguments with coworkers, conflict with boss, office drama, workplace tension"),
    WORKPLACE_ISOLATION("Workplace Isolation", "Workplace Relationships", 
        "Feeling alone at work, no work friends, isolated from team, workplace loneliness"),
    WORKPLACE_POLITICS("Office Politics", "Workplace Relationships",
        "Office politics, workplace games, favoritism at work, unfair treatment"),
    
    // Career Development sub-categories
    CAREER_ADVANCEMENT("Career Growth", "Career Development",
        "Promotion hopes, career advancement, climbing the ladder, professional progress"),
    CAREER_TRANSITION("Career Change", "Career Development",
        "Changing careers, new job search, career pivot, switching industries"),
    SKILL_DEVELOPMENT("Skill Building", "Career Development",
        "Learning new skills, professional development, training, building expertise"),

    // Financial Stress & Anxiety sub-categories
    MONEY_WORRIES("Money Worries", "Financial Stress & Anxiety",
        "Worried about money, financial anxiety, can't afford things, money stress"),
    DEBT_STRESS("Debt Stress", "Financial Stress & Anxiety",
        "Credit card debt, loan payments, owing money, debt burden, financial obligations"),
    BILL_STRUGGLES("Bill Payment Struggles", "Financial Stress & Anxiety",
        "Can't pay bills, rent stress, utility bills, struggling with expenses"),

    // Financial Planning & Goals sub-categories
    BUDGETING("Budgeting", "Financial Planning & Goals",
        "Creating budget, tracking expenses, managing money, financial planning"),
    SAVING_GOALS("Saving Goals", "Financial Planning & Goals",
        "Saving money, building emergency fund, financial goals, investment planning"),
    FINANCIAL_DECISIONS("Financial Decisions", "Financial Planning & Goals",
        "Making financial choices, investment decisions, spending decisions, money management"),

    // Romantic Relationships sub-categories
    ROMANTIC_COMMUNICATION("Communication Issues", "Romantic Relationships",
        "Problems talking with partner, relationship communication, misunderstandings with partner"),
    ROMANTIC_INTIMACY("Intimacy & Connection", "Romantic Relationships",
        "Physical intimacy, emotional connection, closeness with partner, romantic bonding"),
    ROMANTIC_CONFLICTS("Relationship Conflicts", "Romantic Relationships",
        "Fighting with partner, relationship arguments, partner disagreements, relationship problems"),

    // Family Relationships sub-categories
    FAMILY_CONFLICTS("Family Conflicts", "Family Relationships",
        "Arguments with family, family drama, family disagreements, family tension"),
    FAMILY_SUPPORT("Family Support", "Family Relationships",
        "Family helping, supportive family, family there for me, family love"),
    FAMILY_RESPONSIBILITIES("Family Duties", "Family Relationships",
        "Taking care of family, family obligations, helping family members, family caregiving"),

    // Friendships & Social Connections sub-categories
    FRIEND_CONFLICTS("Friend Conflicts", "Friendships & Social Connections",
        "Arguments with friends, friend drama, friend disagreements, social conflicts"),
    MAKING_FRIENDS("Making New Friends", "Friendships & Social Connections",
        "Meeting new people, building friendships, social networking, connecting with others"),
    SOCIAL_ACTIVITIES("Social Activities", "Friendships & Social Connections",
        "Hanging out with friends, social events, group activities, friend gatherings"),

    // Social Anxiety & Isolation sub-categories
    SOCIAL_FEAR("Social Fear", "Social Anxiety & Isolation",
        "Afraid of social situations, scared to socialize, nervous around people, social phobia"),
    SOCIAL_AVOIDANCE("Social Avoidance", "Social Anxiety & Isolation",
        "Avoiding social events, staying home, not wanting to socialize, social withdrawal"),
    SOCIAL_AWKWARDNESS("Social Awkwardness", "Social Anxiety & Isolation",
        "Feeling awkward socially, don't know what to say, uncomfortable in groups, social discomfort"),

    // Anxiety & Overthinking sub-categories
    OVERTHINKING("Overthinking", "Anxiety & Overthinking",
        "Can't stop thinking, ruminating, thoughts going in circles, mind won't quiet"),
    FUTURE_ANXIETY("Future Worries", "Anxiety & Overthinking",
        "Worried about future, anxious about what's coming, fear of unknown, anticipatory anxiety"),
    PERFORMANCE_ANXIETY("Performance Anxiety", "Anxiety & Overthinking",
        "Nervous about performance, fear of failure, anxiety about doing well, worried about results"),

    // Depression & Low Mood sub-categories
    SADNESS("Sadness", "Depression & Low Mood",
        "Feeling sad, down, melancholy, sorrowful, heavy heart, emotional pain"),
    HOPELESSNESS("Hopelessness", "Depression & Low Mood",
        "No hope, nothing will get better, feeling hopeless, no point, giving up hope"),
    EMPTINESS("Emotional Emptiness", "Depression & Low Mood",
        "Feeling empty inside, numb, void, hollow, emotionally drained, nothing inside"),

    // Stress & Feeling Overwhelmed sub-categories
    LIFE_OVERWHELM("Life Overwhelm", "Stress & Feeling Overwhelmed",
        "Too much going on, life is overwhelming, can't handle everything, too much stress"),
    MULTIPLE_PRESSURES("Multiple Pressures", "Stress & Feeling Overwhelmed",
        "Pressure from different areas, pulled in many directions, competing demands, stretched thin"),
    STRESS_SYMPTOMS("Stress Symptoms", "Stress & Feeling Overwhelmed",
        "Physical stress symptoms, stress affecting body, tension, stress-related issues"),

    // Self-Esteem & Self-Worth sub-categories
    CONFIDENCE_ISSUES("Confidence Issues", "Self-Esteem & Self-Worth",
        "Low confidence, don't believe in myself, lack of self-confidence, feeling insecure"),
    SELF_CRITICISM("Self-Criticism", "Self-Esteem & Self-Worth",
        "Being hard on myself, self-critical thoughts, negative self-talk, putting myself down"),
    COMPARISON("Comparing to Others", "Self-Esteem & Self-Worth",
        "Everyone else is better, comparing myself, others are more successful, feeling inferior"),
    
    SELF_PRIDE("Self Pride & Confidence", "Self-Esteem & Self-Worth",
        "Proud of myself, stood up for myself, feeling confident, self-respect, personal strength"),

    // Loneliness & Disconnection sub-categories
    EMOTIONAL_LONELINESS("Emotional Loneliness", "Loneliness & Disconnection",
        "Feeling emotionally alone, no one understands, disconnected from others, isolated feelings"),
    SOCIAL_LONELINESS("Social Loneliness", "Loneliness & Disconnection",
        "No friends around, alone socially, missing social connection, wanting companionship"),
    EXISTENTIAL_LONELINESS("Deep Loneliness", "Loneliness & Disconnection",
        "Alone in the world, fundamental loneliness, existential isolation, deep disconnection"),

    // Joy & Positive Emotions sub-categories
    HAPPINESS("Happiness", "Joy & Positive Emotions",
        "Feeling happy, joyful, cheerful, in good spirits, positive mood, contentment"),
    EXCITEMENT("Excitement", "Joy & Positive Emotions",
        "Excited about something, enthusiastic, looking forward, anticipation, thrilled"),
    GRATITUDE("Gratitude", "Joy & Positive Emotions",
        "Thankful, grateful, appreciative, recognizing blessings, feeling blessed"),

    // Fitness & Physical Activity sub-categories  
    FITNESS_MOTIVATION("Fitness Motivation", "Fitness & Physical Activity",
        "Don't want to work out, lost fitness motivation, no energy for exercise, avoiding gym"),
    FITNESS_PERFORMANCE("Fitness Achievement", "Fitness & Physical Activity", 
        "Great workout, fitness progress, exercise accomplishment, athletic achievement, fitness goals met"),
    FITNESS_PLANNING("Fitness Goals", "Fitness & Physical Activity",
        "Planning workouts, setting fitness goals, exercise schedule, training plans"),

    // Physical Health & Illness sub-categories
    ILLNESS_SYMPTOMS("Illness & Symptoms", "Physical Health & Illness",
        "Feeling sick, physical symptoms, health problems, medical issues, physical discomfort"),
    HEALTH_ANXIETY("Health Anxiety", "Physical Health & Illness",
        "Worried about health, health fears, medical anxiety, concerned about symptoms"),
    DOCTOR_VISITS("Medical Care", "Physical Health & Illness",
        "Doctor appointments, medical tests, healthcare, medical procedures, health checkups"),

    // Motivation & Energy Issues sub-categories
    LOST_INTEREST("Lost Interest", "Motivation & Energy Issues",
        "Don't care anymore, lost interest, nothing seems appealing, don't want to do anything"),
    PROCRASTINATION("Procrastination", "Motivation & Energy Issues",
        "Putting things off, avoiding tasks, can't get started, delaying everything"),
    LOW_ENERGY("Low Energy", "Motivation & Energy Issues", 
        "No energy, too tired to do things, energy depleted, feeling drained"),

    // Self-Doubt & Uncertainty sub-categories
    DECISION_UNCERTAINTY("Decision Uncertainty", "Self-Doubt & Uncertainty",
        "Not sure what to choose, uncertain about decisions, confused about options, indecisive"),
    PATH_UNCERTAINTY("Life Path Uncertainty", "Self-Doubt & Uncertainty",
        "Not sure about life direction, uncertain about future, questioning life choices, confused about path"),
    ABILITY_DOUBT("Ability Doubt", "Self-Doubt & Uncertainty",
        "Questioning my abilities, not sure if I can do it, doubting my skills, uncertain about capabilities"),

    // General catch-all for categories without specific sub-categories
    GENERAL_BODY("General", "Body Image & Appearance",
        "General body image thoughts, appearance concerns, looks-related thoughts"),
    
    SLEEP_QUALITY("Sleep Quality", "Sleep & Energy Issues",
        "Poor sleep, insomnia, sleep problems, trouble sleeping, restless nights"),
    FATIGUE("Fatigue & Tiredness", "Sleep & Energy Issues",
        "Feeling tired, exhausted, low energy, fatigue, worn out"),
    SLEEP_HABITS("Sleep Habits", "Sleep & Energy Issues",
        "Sleep schedule, bedtime routine, sleep patterns, trying to sleep earlier, " +
        "telling myself I'll sleep earlier, can't stick to sleep schedule, sleep discipline, " +
        "keep telling myself to sleep, never sleep early, sleep procrastination"),
    
    HEALTHY_EATING("Healthy Eating", "Nutrition & Eating",
        "Eating well, healthy diet, nutrition goals, staying consistent with diet"),
    EATING_HABITS("Eating Habits", "Nutrition & Eating",
        "Skipping meals, eating patterns, meal timing, food routine"),
    FOOD_STRESS("Food & Stress", "Nutrition & Eating",
        "Stress eating, comfort food, eating when stressed, food coping"),
    
    HABIT_BUILDING("Habit Building", "Habits & Daily Routine",
        "Creating new habits, building routines, habit consistency, daily structure"),
    HABIT_STRUGGLES("Habit Struggles", "Habits & Daily Routine",
        "Breaking bad habits, struggling with routine, can't stick to habits"),
    ROUTINE_PLANNING("Routine Planning", "Habits & Daily Routine",
        "Planning daily routine, organizing schedule, structuring day"),
    
    LEARNING_MOTIVATION("Learning Motivation", "Learning & Skill Development",
        "Want to learn something new, excited about learning, motivation to study"),
    LEARNING_STRUGGLES("Learning Challenges", "Learning & Skill Development",
        "Difficulty learning, learning obstacles, struggling with new skills"),
    SKILL_PROGRESS("Skill Progress", "Learning & Skill Development",
        "Making progress, improving skills, getting better, skill development"),
    
    SPIRITUAL_PEACE("Spiritual Peace", "Spirituality & Life Meaning",
        "Meditation helped, feeling peaceful, spiritual calm, inner peace, mindfulness"),
    LIFE_PURPOSE("Life Purpose", "Spirituality & Life Meaning",
        "Searching for meaning, life purpose, spiritual journey, existential questions"),
    SPIRITUAL_PRACTICE("Spiritual Practice", "Spirituality & Life Meaning",
        "Meditation, prayer, spiritual activities, mindfulness practice, spiritual growth"),
    
    GOAL_PROGRESS("Goal Progress", "Goals & Achievement",
        "Making progress on goals, achieving milestones, getting closer to goals"),
    GOAL_SETTING("Goal Setting", "Goals & Achievement",
        "Setting new goals, planning objectives, goal planning, future aspirations"),
    GOAL_STRUGGLES("Goal Struggles", "Goals & Achievement",
        "Struggling with goals, goal obstacles, difficulty achieving, goal setbacks"),
    
    LIFE_CHANGES("Life Changes", "Major Life Transitions",
        "Moving, big life changes, major transitions, life shifts, new chapter"),
    TRANSITION_ANXIETY("Transition Anxiety", "Major Life Transitions",
        "Nervous about changes, worried about transitions, change anxiety, uncertainty about future"),
    NEW_BEGINNINGS("New Beginnings", "Major Life Transitions",
        "Fresh start, new opportunities, starting over, new phase of life"),
    
    SEASONAL_MOOD("Seasonal Mood", "Seasonal & Weather",
        "Weather affecting mood, seasonal feelings, weather impact, seasonal changes"),
    NATURE_APPRECIATION("Nature Appreciation", "Seasonal & Weather",
        "Enjoying weather, appreciating seasons, weather bringing joy, seasonal beauty"),
    WEATHER_DISCOMFORT("Weather Discomfort", "Seasonal & Weather",
        "Weather bothering me, disliking weather, seasonal sadness, weather stress"),
    
    CREATIVE_INSPIRATION("Creative Inspiration", "Creativity & Artistic Expression",
        "Feeling inspired, creative ideas, artistic motivation, creative energy, artistic excitement"),
    CREATIVE_BLOCKS("Creative Blocks", "Creativity & Artistic Expression",
        "Creative wall, can't create, stuck creatively, no creative ideas, artistic frustration"),
    CREATIVE_ACHIEVEMENT("Creative Achievement", "Creativity & Artistic Expression",
        "Finished creative project, proud of art, creative accomplishment, artistic success"),
    
    HOBBY_ENJOYMENT("Hobby Enjoyment", "Hobbies & Personal Interests",
        "Enjoying hobbies, fun activities, recreational time, hobby satisfaction"),
    HOBBY_TIME("Hobby Time", "Hobbies & Personal Interests",
        "Want more time for hobbies, need hobby time, missing recreational activities"),
    HOBBY_EXPLORATION("Hobby Exploration", "Hobbies & Personal Interests",
        "Trying new hobbies, exploring interests, discovering new activities"),
    
    DIGITAL_OVERWHELM("Digital Overwhelm", "Technology & Digital Life",
        "Screen time, digital fatigue, technology stress, doomscrolling, social media draining, " +
        "internet addiction, can't stop scrolling"),
    DIGITAL_DETOX("Digital Detox", "Technology & Digital Life",
        "Want to disconnect, need break from technology, digital detox, unplugging"),
    SOCIAL_MEDIA("Social Media", "Technology & Digital Life",
        "Social media stress, online interactions, digital socializing, internet behavior"),
    
    NEWS_STRESS("News Stress", "News & Political Events",
        "News anxiety, political stress, current events overwhelming, world news worry, " +
        "tired of political arguments, online politics draining, political fatigue"),
    POLITICAL_ENGAGEMENT("Political Engagement", "News & Political Events",
        "Political involvement, civic engagement, political opinions, social issues"),
    WORLD_CONCERN("World Concern", "News & Political Events",
        "Worried about world, global issues, society problems, future of world"),
    
    NATURE_CONNECTION("Nature Connection", "Environment & Nature",
        "Enjoying nature, outdoor time, nature appreciation, environmental beauty"),
    ENVIRONMENTAL_CONCERN("Environmental Concern", "Environment & Nature",
        "Environmental worry, climate anxiety, nature concerns, environmental issues"),
    OUTDOOR_ACTIVITIES("Outdoor Activities", "Environment & Nature",
        "Outdoor adventures, nature activities, hiking, being outside"),
    
    DAILY_REFLECTIONS("Daily Reflections", "Daily Life & Observations",
        "Daily thoughts, everyday observations, routine reflections, ordinary moments"),
    LIFE_OBSERVATIONS("Life Observations", "Daily Life & Observations",
        "Observing life, people watching, noticing things, daily insights"),
    MUNDANE_MOMENTS("Mundane Moments", "Daily Life & Observations",
        "Simple moments, everyday experiences, ordinary life, routine observations"),
    
    PHILOSOPHICAL_THOUGHTS("Philosophical Thoughts", "Open Reflections",
        "Deep thoughts, philosophical questions, existential wondering, life contemplation, " +
        "wondering about life, what if scenarios, hypothetical thinking, deep reflection, " +
        "wonder what life would feel like, wondering if we know ourselves, existential questions"),
    RANDOM_THOUGHTS("Random Thoughts", "Open Reflections",
        "Random reflections, stray thoughts, miscellaneous thinking, wandering mind"),
    ABSTRACT_THINKING("Abstract Thinking", "Open Reflections",
        "Abstract ideas, conceptual thinking, imaginative thoughts, creative wondering");

    private final String label;
    private final String parentCategory;
    private final String description;

    SubCategory(String label, String parentCategory, String description) {
        this.label = label;
        this.parentCategory = parentCategory;
        this.description = description;
    }

    public static List<SubCategory> getSubCategoriesForParent(String parentCategory) {
        return Arrays.stream(values())
                .filter(sub -> sub.getParentCategory().equals(parentCategory))
                .collect(Collectors.toList());
    }

    public static List<String> getSubCategoryLabelsForParent(String parentCategory) {
        return getSubCategoriesForParent(parentCategory)
                .stream()
                .map(SubCategory::getLabel)
                .collect(Collectors.toList());
    }

    public static List<String> getSubCategoryDescriptionsForParent(String parentCategory) {
        return getSubCategoriesForParent(parentCategory)
                .stream()
                .map(SubCategory::getDescription)
                .collect(Collectors.toList());
    }
}
