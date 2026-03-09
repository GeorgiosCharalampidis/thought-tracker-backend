import React from 'react';
import { Box, Button, Card, CardContent, Chip, Divider, Fade, Slide, Typography } from '@mui/material';
import { Note } from '../types';

interface SimilarThoughtsSectionProps {
  notes: Note[];
  ownNotes: Note[];
  categoryMessage: string;
  isDarkMode: boolean;
  onShareAnotherThought: () => void;
}

function formatRelativeDate(dateStr: string): string {
  const date = new Date(dateStr);
  if (isNaN(date.getTime())) return dateStr;

  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

  if (diffDays === 0) return 'today';
  if (diffDays === 1) return 'yesterday';
  if (diffDays < 7) return `${diffDays} days ago`;
  if (diffDays < 30) return `${Math.floor(diffDays / 7)} weeks ago`;
  if (diffDays < 365) return `${Math.floor(diffDays / 30)} months ago`;

  return date.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
}

function SimilarThoughtsSection({ notes, ownNotes, categoryMessage, isDarkMode, onShareAnotherThought }: SimilarThoughtsSectionProps) {
  const cardStyle = {
    minWidth: '200px',
    maxWidth: '400px',
    width: 'fit-content',
    borderRadius: 2,
    backgroundColor: isDarkMode ? '#2d2e2d' : '#ffffff',
    border: isDarkMode ? '1px solid #404140' : '1px solid #e2e8f0',
    userSelect: 'text' as const,
    cursor: 'default',
  };

  return (
    <Slide direction="up" in timeout={1000}>
      <Box sx={{ pb: 3 }}>
        <Box display="flex" flexDirection="column" alignItems="center" width="100%">
          <Button
            variant="text"
            onClick={onShareAnotherThought}
            sx={{
              mb: 3,
              borderRadius: 999,
              px: 1.25,
              py: 0.5,
              textTransform: 'none',
              color: isDarkMode ? '#cbd5e1' : '#475569',
              border: '1px solid',
              borderColor: isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(15,23,42,0.08)',
              '&:hover': {
                backgroundColor: isDarkMode ? 'rgba(255,255,255,0.04)' : 'rgba(15,23,42,0.03)',
              },
            }}
          >
            Share another thought
          </Button>

          {/* Section 1: Own past notes */}
          {ownNotes.length > 0 && (
            <Box sx={{ width: '100%', display: 'flex', flexDirection: 'column', alignItems: 'center', mb: 4 }}>
              <Typography
                variant="h5"
                sx={{
                  fontWeight: 400,
                  color: isDarkMode ? '#f1f5f9' : '#2d3748',
                  textAlign: 'center',
                  letterSpacing: '-0.01em',
                  mb: 3,
                }}
              >
                You've written about this before.
              </Typography>
              <Box display="flex" flexWrap="wrap" gap={3} justifyContent="center" alignItems="flex-start">
                {ownNotes.map((noteItem, index) => (
                  <Fade in timeout={600 + index * 200} key={noteItem.id}>
                    <Card elevation={1} sx={cardStyle}>
                      <CardContent sx={{ p: 3 }}>
                        <Typography
                          variant="caption"
                          sx={{
                            display: 'block',
                            mb: 1.5,
                            color: isDarkMode ? '#64748b' : '#94a3b8',
                            fontSize: '0.8rem',
                          }}
                        >
                          {formatRelativeDate(noteItem.date)}
                        </Typography>
                        <Typography
                          variant="body1"
                          sx={{
                            lineHeight: 1.6,
                            color: isDarkMode ? '#e2e8f0' : '#4a5568',
                            fontSize: '1rem',
                          }}
                        >
                          {noteItem.content}
                        </Typography>
                      </CardContent>
                    </Card>
                  </Fade>
                ))}
              </Box>
            </Box>
          )}

          {/* Divider between sections when both are present */}
          {ownNotes.length > 0 && (
            <Divider
              sx={{
                width: '100%',
                maxWidth: 480,
                mb: 4,
                borderColor: isDarkMode ? 'rgba(255,255,255,0.07)' : 'rgba(15,23,42,0.07)',
              }}
            />
          )}

          {/* Section 2: Community notes */}
          <Box
            sx={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              mb: 4,
              gap: 1,
            }}
          >
            <Typography
              variant="h5"
              sx={{
                fontWeight: 400,
                color: isDarkMode ? '#f1f5f9' : '#2d3748',
                textAlign: 'center',
                letterSpacing: '-0.01em',
              }}
            >
              {categoryMessage || "You're not alone."}
            </Typography>
            {notes.length > 0 && (
              <Typography
                variant="body2"
                sx={{
                  color: isDarkMode ? '#64748b' : '#94a3b8',
                  textAlign: 'center',
                  fontSize: '0.88rem',
                  mt: 0.25,
                }}
              >
                Here's what others have shared
              </Typography>
            )}
          </Box>

          <Box display="flex" flexWrap="wrap" gap={3} justifyContent="center" alignItems="flex-start">
            {notes.map((noteItem, index) => (
              <Fade in timeout={600 + index * 200} key={noteItem.id}>
                <Card elevation={1} sx={cardStyle}>
                  <CardContent sx={{ p: 3 }}>
                    <Box display="flex" justifyContent="flex-start" alignItems="flex-start" mb={2}>
                      <Box display="flex" flexDirection="row" gap={1} alignItems="center" flexWrap="wrap" sx={{ marginLeft: '-6px' }}>
                        <Chip
                          label={noteItem.category}
                          size="small"
                          sx={{
                            backgroundColor: isDarkMode ? '#f3f4f6' : '#111827',
                            color: isDarkMode ? '#111827' : '#f9fafb',
                            fontWeight: 500,
                            '& .MuiChip-label': {
                              px: 1.4,
                            },
                          }}
                        />
                        {noteItem.subCategory && (
                          <Chip
                            label={noteItem.subCategory}
                            size="small"
                            variant="outlined"
                            sx={{
                              borderColor: isDarkMode ? 'rgba(255,255,255,0.14)' : 'rgba(15,23,42,0.12)',
                              color: isDarkMode ? '#d1d5db' : '#475569',
                              fontWeight: 500,
                              fontSize: '0.75rem',
                              backgroundColor: isDarkMode ? 'rgba(255,255,255,0.03)' : 'rgba(15,23,42,0.02)',
                              '& .MuiChip-label': {
                                px: 1,
                              },
                            }}
                          />
                        )}
                      </Box>
                    </Box>
                    <Typography
                      variant="body1"
                      sx={{
                        lineHeight: 1.6,
                        color: isDarkMode ? '#e2e8f0' : '#4a5568',
                        fontSize: '1rem',
                      }}
                    >
                      {noteItem.content}
                    </Typography>
                  </CardContent>
                </Card>
              </Fade>
            ))}
          </Box>
        </Box>
      </Box>
    </Slide>
  );
}

export default SimilarThoughtsSection;
