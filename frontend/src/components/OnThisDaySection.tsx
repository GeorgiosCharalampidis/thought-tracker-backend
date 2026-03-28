import React, { useEffect, useState } from 'react';
import { Box, Divider, Typography } from '@mui/material';
import { History as HistoryIcon } from '@mui/icons-material';
import { Note, OnThisDayResponse } from '../types';

interface OnThisDaySectionProps {
  memories: OnThisDayResponse;
  isDarkMode: boolean;
  onSelectMemory: (note: Note) => void;
  skipAnimation?: boolean;
  onAnimated?: () => void;
}

interface MemoryGroup {
  label: string;
  accentColor: string;
  notes: Note[];
}

const truncate = (text: string, max: number) =>
  text.length <= max ? text : text.slice(0, max).trimEnd() + '…';

const formatDate = (dateStr: string) => {
  const d = new Date(`${dateStr}T00:00`);
  return d.toLocaleDateString(undefined, { month: 'long', day: 'numeric', year: 'numeric' });
};

function MemoryItem({
  note,
  label,
  accentColor,
  isDarkMode,
  index,
  skipAnimation,
  onClick,
}: {
  note: Note;
  label: string;
  accentColor: string;
  isDarkMode: boolean;
  index: number;
  skipAnimation?: boolean;
  onClick: () => void;
}) {
  const [expanded, setExpanded] = useState(false);
  const textMuted = isDarkMode ? '#64748b' : '#94a3b8';
  const textPrimary = isDarkMode ? '#e2e8f0' : '#1e293b';
  const isTruncated = note.content.length > 120;

  return (
    <Box
      onClick={onClick}
      sx={{
        cursor: 'pointer',
        borderRadius: 2,
        px: 2,
        py: 1.5,
        mx: -2,
        transition: 'background-color 0.15s ease',
        '&:hover': { backgroundColor: isDarkMode ? 'rgba(255,255,255,0.04)' : 'rgba(15,23,42,0.04)' },
        ...(skipAnimation ? {} : {
          animation: 'memItemIn 0.8s ease both',
          animationDelay: `${3.2 + index * 1.2}s`,
          '@keyframes memItemIn': { from: { opacity: 0 }, to: { opacity: 1 } },
        }),
      }}
    >
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
        <Typography sx={{ fontSize: '0.7rem', fontWeight: 700, color: accentColor, textTransform: 'uppercase', letterSpacing: '0.06em', lineHeight: 1 }}>
          {label}
        </Typography>
        <Typography sx={{ fontSize: '0.7rem', color: textMuted, lineHeight: 1 }}>
          {formatDate(note.date)}
        </Typography>
      </Box>
      <Typography className="memory-content" sx={{ fontSize: '0.85rem', color: textPrimary, lineHeight: 1.5, wordBreak: 'break-word' }}>
        {expanded ? note.content : truncate(note.content, 120)}
      </Typography>
      {isTruncated && (
        <Typography
          onClick={(e) => { e.stopPropagation(); setExpanded(v => !v); }}
          sx={{ fontSize: '0.75rem', color: textMuted, mt: 0.5, cursor: 'pointer', '&:hover': { color: textPrimary } }}
        >
          {expanded ? 'Show less' : 'Show more'}
        </Typography>
      )}
    </Box>
  );
}

export default function OnThisDaySection({ memories, isDarkMode, onSelectMemory, skipAnimation, onAnimated }: OnThisDaySectionProps) {
  const groups: MemoryGroup[] = [
    { label: 'A month ago', accentColor: '#8b5cf6', notes: memories.monthAgo ?? [] },
    { label: 'A year ago',  accentColor: '#3b82f6', notes: memories.yearAgo  ?? [] },
  ].filter(g => g.notes.length > 0);

  const textMuted = isDarkMode ? '#64748b' : '#94a3b8';
  const dividerDelay = skipAnimation ? 0 : 3.2 + (groups.length - 1) * 1.2;

  useEffect(() => {
    if (skipAnimation || !onAnimated || groups.length === 0) return;
    const totalDuration = (dividerDelay + 0.8) * 1000;
    const timer = setTimeout(onAnimated, totalDuration);
    return () => clearTimeout(timer);
  }, []);

  if (groups.length === 0) return null;

  const animSx = (delay: number) => skipAnimation
    ? {}
    : {
        animation: 'memItemIn 0.8s ease both',
        animationDelay: `${delay}s`,
        '@keyframes memItemIn': { from: { opacity: 0 }, to: { opacity: 1 } },
      };

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'center', mb: { xs: 2, sm: 4 }, ...animSx(2) }}>
        <Box sx={{ position: 'relative', display: 'inline-flex', alignItems: 'center' }}>
          <HistoryIcon sx={{ fontSize: 14, color: textMuted, position: 'absolute', right: '100%', mr: 0.8 }} />
          <Typography sx={{ fontSize: '0.75rem', color: textMuted, letterSpacing: '0.05em', textTransform: 'uppercase', fontWeight: 500, lineHeight: 1 }}>
            On this day
          </Typography>
        </Box>
      </Box>

      <Box sx={{ display: 'flex', flexDirection: 'row', flexWrap: 'wrap', gap: { xs: 2, sm: 6 }, justifyContent: 'center' }}>
        {groups.map(({ label, accentColor, notes }, groupIndex) =>
          notes.slice(0, 1).map(note => (
            <MemoryItem
              key={note.id}
              note={note}
              label={label}
              accentColor={accentColor}
              isDarkMode={isDarkMode}
              index={groupIndex}
              skipAnimation={skipAnimation}
              onClick={() => onSelectMemory(note)}
            />
          ))
        )}
      </Box>

      <Box sx={{ ...animSx(dividerDelay), mt: { xs: 3, sm: 6 }, mb: { xs: 3, sm: 6 } }}>
        <Divider sx={{ borderColor: isDarkMode ? 'rgba(255,255,255,0.18)' : 'rgba(15,23,42,0.18)' }} />
      </Box>
    </Box>
  );
}
