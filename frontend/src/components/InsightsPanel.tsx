import React, { useMemo } from 'react';
import { Box, Dialog, DialogContent, IconButton, Typography } from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
import { Cell, Pie, PieChart, Tooltip } from 'recharts';
import { Note } from '../types';
import { DOMAIN_GROUPS } from '../utils/categoryColors';

interface InsightsPanelProps {
  open: boolean;
  onClose: () => void;
  notes: Note[];
  isDarkMode: boolean;
}

const toDateStr = (d: Date) => {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
};

const buildCalendarWeeks = (notes: Note[]) => {
  const countByDate = new Map<string, number>();
  for (const note of notes) {
    countByDate.set(note.date, (countByDate.get(note.date) ?? 0) + 1);
  }
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const dow = today.getDay();
  const daysFromMonday = dow === 0 ? 6 : dow - 1;
  const thisMonday = new Date(today);
  thisMonday.setDate(today.getDate() - daysFromMonday);
  const startDate = new Date(thisMonday);
  startDate.setDate(thisMonday.getDate() - 51 * 7);

  const weeks: Array<Array<{ date: string; count: number; isFuture: boolean }>> = [];
  const cur = new Date(startDate);
  for (let w = 0; w < 52; w++) {
    const week = [];
    for (let d = 0; d < 7; d++) {
      const dateStr = toDateStr(cur);
      week.push({ date: dateStr, count: countByDate.get(dateStr) ?? 0, isFuture: cur > today });
      cur.setDate(cur.getDate() + 1);
    }
    weeks.push(week);
  }
  return weeks.reverse();
};

const getMonthLabels = (weeks: ReturnType<typeof buildCalendarWeeks>) => {
  const labels: { label: string; col: number }[] = [];
  let lastMonth = -1;
  weeks.forEach((week, i) => {
    const month = new Date(`${week[0].date}T00:00`).getMonth();
    if (month !== lastMonth) {
      labels.push({
        label: new Date(`${week[0].date}T00:00`).toLocaleDateString(undefined, { month: 'short' }),
        col: i,
      });
      lastMonth = month;
    }
  });
  return labels;
};

const CELL_SIZE = 8;
const CELL_GAP = 2;
const CELL_STEP = CELL_SIZE + CELL_GAP;

function InsightsPanel({ open, onClose, notes, isDarkMode }: InsightsPanelProps) {
  const domainData = useMemo(() =>
    DOMAIN_GROUPS
      .map(domain => ({
        name: domain.name,
        color: domain.color,
        count: notes.filter(n => domain.categories.includes(n.category)).length,
      }))
      .filter(d => d.count > 0),
    [notes]
  );

  const totalNotes = notes.length;
  const calendarWeeks = useMemo(() => buildCalendarWeeks(notes), [notes]);
  const monthLabels = useMemo(() => getMonthLabels(calendarWeeks), [calendarWeeks]);

  const bg = isDarkMode ? '#1e1f1e' : '#ffffff';
  const borderColor = isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(15,23,42,0.08)';
  const textPrimary = isDarkMode ? '#f1f5f9' : '#1e293b';
  const textMuted = isDarkMode ? '#64748b' : '#94a3b8';
  const emptyCellColor = isDarkMode ? 'rgba(255,255,255,0.07)' : 'rgba(15,23,42,0.08)';

  const getCellColor = (count: number, isFuture: boolean) => {
    if (isFuture || count === 0) return emptyCellColor;
    if (count === 1) return 'rgba(102,126,234,0.4)';
    if (count === 2) return 'rgba(102,126,234,0.65)';
    return 'rgba(102,126,234,0.9)';
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="sm"
      fullWidth
      slotProps={{ backdrop: { sx: { backdropFilter: 'blur(6px) brightness(0.45)', backgroundColor: 'rgba(0,0,0,0.55)' } } }}
      PaperProps={{
        sx: {
          backgroundColor: bg,
          backgroundImage: 'none',
          border: `1px solid ${borderColor}`,
          borderRadius: 3,
          overflow: 'hidden',
          position: 'relative',
        },
      }}
    >
      <IconButton
        size="small"
        onClick={onClose}
        sx={{
          position: 'absolute',
          top: 12,
          right: 12,
          zIndex: 1,
          color: textMuted,
          '&:hover': { color: textPrimary },
        }}
      >
        <CloseIcon sx={{ fontSize: 16 }} />
      </IconButton>

      <DialogContent sx={{ p: 3, pt: 3, overflow: 'hidden' }}>
        {notes.length === 0 ? (
          <Typography sx={{ color: textMuted, fontSize: '0.9rem', py: 4, textAlign: 'center' }}>
            Start journaling to see your insights here.
          </Typography>
        ) : (
          <>
            {/* Domain breakdown */}
            <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 3 }}>
              <PieChart width={140} height={140}>
                <Pie
                  data={domainData}
                  cx={65}
                  cy={65}
                  innerRadius={40}
                  outerRadius={62}
                  paddingAngle={2}
                  dataKey="count"
                  strokeWidth={0}
                >
                  {domainData.map((entry) => (
                    <Cell key={entry.name} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip
                  formatter={(value, name) => [`${value} entries`, name]}
                  contentStyle={{
                    backgroundColor: isDarkMode ? '#2d2e2d' : '#ffffff',
                    border: `1px solid ${borderColor}`,
                    borderRadius: 8,
                    fontSize: '0.75rem',
                    color: textPrimary,
                    boxShadow: 'none',
                  }}
                />
              </PieChart>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '4px 12px', mt: 1.5 }}>
                {domainData.map(domain => (
                  <Box key={domain.name} display="flex" alignItems="center" gap={0.6}>
                    <Box sx={{ width: 7, height: 7, borderRadius: '50%', backgroundColor: domain.color, flexShrink: 0 }} />
                    <Typography sx={{ fontSize: '0.78rem', color: textPrimary }}>{domain.name}</Typography>
                    <Typography sx={{ fontSize: '0.74rem', color: textMuted }}>
                      {Math.round((domain.count / totalNotes) * 100)}%
                    </Typography>
                  </Box>
                ))}
              </Box>
            </Box>

            {/* Scrollable wrapper for mobile */}
            <Box sx={{ overflowX: 'auto', pb: 0.5, display: 'flex', justifyContent: 'center' }}>
              <Box>
              {/* Month labels */}
              <Box sx={{ position: 'relative', height: 16, mb: 0.5, width: 52 * CELL_STEP }}>
                {monthLabels.map(({ label, col }) => (
                  <Typography
                    key={label + col}
                    sx={{ position: 'absolute', left: col * CELL_STEP, fontSize: '0.63rem', color: textMuted, userSelect: 'none', lineHeight: 1 }}
                  >
                    {label}
                  </Typography>
                ))}
              </Box>

              {/* Grid */}
              <Box sx={{ display: 'flex', gap: `${CELL_GAP}px` }}>
                {calendarWeeks.map((week, wi) => (
                  <Box key={wi} sx={{ display: 'flex', flexDirection: 'column', gap: `${CELL_GAP}px` }}>
                    {week.map((day, di) => (
                      <Box
                        key={di}
                        title={day.isFuture ? '' : `${day.date}${day.count > 0 ? ` · ${day.count} entr${day.count === 1 ? 'y' : 'ies'}` : ''}`}
                        sx={{ width: CELL_SIZE, height: CELL_SIZE, borderRadius: '2px', backgroundColor: getCellColor(day.count, day.isFuture), flexShrink: 0 }}
                      />
                    ))}
                  </Box>
                ))}
              </Box>
              </Box>
            </Box>

            {/* Heatmap legend */}
            <Box display="flex" alignItems="center" gap={0.6} mt={1.5} justifyContent="flex-end">
              <Typography sx={{ fontSize: '0.63rem', color: textMuted }}>Less</Typography>
              {[emptyCellColor, 'rgba(102,126,234,0.4)', 'rgba(102,126,234,0.65)', 'rgba(102,126,234,0.9)'].map((c, i) => (
                <Box key={i} sx={{ width: CELL_SIZE, height: CELL_SIZE, borderRadius: '2px', backgroundColor: c }} />
              ))}
              <Typography sx={{ fontSize: '0.63rem', color: textMuted }}>More</Typography>
            </Box>
          </>
        )}
      </DialogContent>
    </Dialog>
  );
}

export default InsightsPanel;
