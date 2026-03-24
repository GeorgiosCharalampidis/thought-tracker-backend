export interface DomainGroup {
  name: string;
  color: string;
  categories: string[];
}

export const DOMAIN_GROUPS: DomainGroup[] = [
  {
    name: 'Work & Career',
    color: '#f59e0b',
    categories: ['Work Stress & Burnout', 'Workplace Relationships', 'Career Development'],
  },
  {
    name: 'Mental Health',
    color: '#8b5cf6',
    categories: [
      'Anxiety & Overthinking',
      'Depression & Low Mood',
      'Self-Doubt & Uncertainty',
      'Self-Esteem & Self-Worth',
      'Motivation & Energy Issues',
      'Anger & Frustration',
      'Grief & Loss',
    ],
  },
  {
    name: 'Relationships',
    color: '#f43f5e',
    categories: [
      'Romantic Relationships',
      'Friendships & Social Connections',
      'Family Relationships',
      'Loneliness & Disconnection',
    ],
  },
  {
    name: 'Physical Health',
    color: '#10b981',
    categories: [
      'Physical Health & Illness',
      'Fitness & Physical Activity',
      'Sleep & Energy Issues',
      'Nutrition & Eating',
    ],
  },
  {
    name: 'Personal Development',
    color: '#0ea5e9',
    categories: [
      'Habits & Daily Routine',
      'Learning & Skill Development',
      'Creativity & Artistic Expression',
      'Spirituality & Life Meaning',
    ],
  },
  {
    name: 'Finance',
    color: '#f97316',
    categories: ['Money & Finances'],
  },
  {
    name: 'Joy',
    color: '#facc15',
    categories: ['Joy & Positive Emotions'],
  },
  {
    name: 'Life & World',
    color: '#64748b',
    categories: [
      'Major Life Transitions',
      'Housing & Living Situation',
      'Environment & Nature',
      'Daily Life & Observations',
    ],
  },
];

const CATEGORY_COLOR: Record<string, string> = {};
for (const domain of DOMAIN_GROUPS) {
  for (const cat of domain.categories) {
    CATEGORY_COLOR[cat] = domain.color;
  }
}

export const getCategoryColor = (category: string): string =>
  CATEGORY_COLOR[category] ?? '#667eea';
