import React from 'react';
import { Box, Typography } from '@mui/material';
import { keyframes } from '@emotion/react';

// Tier 1 (1–4): gentle wobble, no glow
const flicker1 = keyframes`
  0%, 100% { transform: scale(1) rotate(0deg); }
  35%       { transform: scale(1.05) rotate(1deg); }
  70%       { transform: scale(0.97) rotate(-0.5deg); }
`;

// Tier 2 (5–14): visible flicker, soft glow begins
const flicker2 = keyframes`
  0%, 100% { transform: scale(1) rotate(-0.5deg);
              filter: drop-shadow(0 0 3px rgba(251,146,60,0.35)); }
  30%       { transform: scale(1.09) rotate(1.5deg);
              filter: drop-shadow(0 0 9px rgba(251,146,60,0.7)); }
  65%       { transform: scale(0.95) rotate(-1deg);
              filter: drop-shadow(0 0 5px rgba(251,146,60,0.5)); }
`;

// Tier 3 (15–29): energetic, pulsing orange/red heat
const flicker3 = keyframes`
  0%, 100% { transform: scale(1) rotate(-1deg);
              filter: drop-shadow(0 0 6px rgba(251,146,60,0.75))
                      drop-shadow(0 0 14px rgba(239,68,68,0.3)); }
  25%       { transform: scale(1.12) rotate(2deg);
              filter: drop-shadow(0 0 16px rgba(251,146,60,1))
                      drop-shadow(0 0 7px rgba(239,68,68,0.55)); }
  50%       { transform: scale(0.94) rotate(-1.5deg);
              filter: drop-shadow(0 0 9px rgba(251,146,60,0.8))
                      drop-shadow(0 0 18px rgba(239,68,68,0.4)); }
  75%       { transform: scale(1.07) rotate(1deg);
              filter: drop-shadow(0 0 13px rgba(251,146,60,0.9))
                      drop-shadow(0 0 5px rgba(253,224,71,0.35)); }
`;

// Tier 4 (30+): inferno — fast, multi-layer, intense
const flicker4 = keyframes`
  0%, 100% { transform: scale(1) rotate(-1.5deg);
              filter: drop-shadow(0 0 10px rgba(251,146,60,0.95))
                      drop-shadow(0 0 20px rgba(239,68,68,0.65))
                      drop-shadow(0 0 30px rgba(253,224,71,0.2)); }
  20%       { transform: scale(1.18) rotate(2.5deg);
              filter: drop-shadow(0 0 22px rgba(251,146,60,1))
                      drop-shadow(0 0 12px rgba(239,68,68,0.85))
                      drop-shadow(0 0 36px rgba(253,224,71,0.45)); }
  40%       { transform: scale(0.91) rotate(-2deg);
              filter: drop-shadow(0 0 12px rgba(251,146,60,0.9))
                      drop-shadow(0 0 22px rgba(239,68,68,0.6))
                      drop-shadow(0 0 6px rgba(253,224,71,0.55)); }
  60%       { transform: scale(1.14) rotate(1.5deg);
              filter: drop-shadow(0 0 18px rgba(251,146,60,1))
                      drop-shadow(0 0 9px rgba(239,68,68,0.75))
                      drop-shadow(0 0 32px rgba(253,224,71,0.35)); }
  80%       { transform: scale(0.97) rotate(-1deg);
              filter: drop-shadow(0 0 14px rgba(251,146,60,0.88))
                      drop-shadow(0 0 26px rgba(239,68,68,0.55)); }
`;

// Heat haze backdrop — radial glow behind the emoji for tier 3+
const hazeT3 = keyframes`
  0%, 100% { opacity: 0.18; transform: scale(1); }
  50%       { opacity: 0.38; transform: scale(1.2); }
`;

const hazeT4 = keyframes`
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50%       { opacity: 0.58; transform: scale(1.35); }
`;


interface Tier {
  minStreak: number;
  emoji: string;
  emojiSize: string;
  animation: ReturnType<typeof keyframes>;
  duration: string;
  hazeAnimation: ReturnType<typeof keyframes> | null;
  hazeDuration: string;
  hazeColor: string;
  labelColor: (dark: boolean) => string;
}

const TIERS: Tier[] = [
  {
    minStreak: 30,
    emoji: '🔥',
    emojiSize: '2.1rem',
    animation: flicker4,
    duration: '3s',
    hazeAnimation: hazeT4,
    hazeDuration: '2.2s',
    hazeColor: 'radial-gradient(ellipse, rgba(251,146,60,0.55) 0%, rgba(239,68,68,0.25) 50%, transparent 75%)',
    labelColor: (dark) => dark ? '#f1f5f9' : '#1e293b',
  },
  {
    minStreak: 15,
    emoji: '🔥',
    emojiSize: '1.8rem',
    animation: flicker3,
    duration: '2.2s',
    hazeAnimation: hazeT3,
    hazeDuration: '3s',
    hazeColor: 'radial-gradient(ellipse, rgba(251,146,60,0.4) 0%, rgba(239,68,68,0.15) 55%, transparent 75%)',
    labelColor: (dark) => dark ? '#f1f5f9' : '#1e293b',
  },
  {
    minStreak: 5,
    emoji: '🔥',
    emojiSize: '1.55rem',
    animation: flicker2,
    duration: '1.5s',
    hazeAnimation: null,
    hazeDuration: '',
    hazeColor: '',
    labelColor: (dark) => dark ? '#f1f5f9' : '#1e293b',
  },
  {
    minStreak: 1,
    emoji: '🔥',
    emojiSize: '1.25rem',
    animation: flicker1,
    duration: '2.2s',
    hazeAnimation: null,
    hazeDuration: '',
    hazeColor: '',
    labelColor: (dark) => dark ? '#f1f5f9' : '#1e293b',
  },
];

interface StreakBadgeProps {
  streak: number;
  isDarkMode: boolean;
  isMobile: boolean;
}

export default function StreakBadge({ streak, isDarkMode, isMobile }: StreakBadgeProps) {
  if (streak <= 0) return null;

  const tier = TIERS.find(t => streak >= t.minStreak)!;

  return (
    <Box
      sx={{
        position: 'fixed',
        top: isMobile ? 70 : 22,
        left: '50%',
        transform: 'translateX(calc(-50% - 1.2rem))',
        zIndex: 1400,
        display: 'flex',
        alignItems: 'center',
        gap: 0.6,
        pointerEvents: 'none',
        userSelect: 'none',
        whiteSpace: 'nowrap',
      }}
    >
      {/* Flame emoji — fixed slot so text never shifts regardless of tier size */}
      <Box sx={{ width: '2.4rem', height: '2.4rem', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0, position: 'relative' }}>
        {/* Heat haze lives inside the slot so it's always centered on the flame */}
        {tier.hazeAnimation && (
          <Box sx={{
            position: 'absolute',
            inset: '-12px',
            background: tier.hazeColor,
            borderRadius: '50%',
            animation: `${tier.hazeAnimation} ${tier.hazeDuration} ease-in-out infinite`,
            pointerEvents: 'none',
          }} />
        )}
        <Box
          component="span"
          sx={{
            fontSize: tier.emojiSize,
            lineHeight: 1,
            display: 'inline-flex',
            alignItems: 'center',
            position: 'relative',
            zIndex: 1,
            animation: `${tier.animation} ${tier.duration} ease-in-out infinite`,
          }}
        >
          {tier.emoji}
        </Box>
      </Box>

      {/* Label */}
      <Typography
        sx={{
          fontSize: '0.9rem',
          fontWeight: 500,
          color: tier.labelColor(isDarkMode),
          letterSpacing: '0.01em',
          lineHeight: 1,
          position: 'relative',
          zIndex: 1,
        }}
      >
        {streak}-day streak
      </Typography>
    </Box>
  );
}
