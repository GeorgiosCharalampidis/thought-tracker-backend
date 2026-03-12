import React from 'react';
import { Box, Button, Card, CardContent, Chip, Fade, Slide, Typography } from '@mui/material';
import { Note, AuthUser } from '../types';
import NoteComments from './NoteComments';

interface SimilarThoughtsSectionProps {
  notes: Note[];
  ownNotes: Note[];
  categoryMessage: string;
  isDarkMode: boolean;
  currentUser: AuthUser | null;
  onShareAnotherThought: () => void;
}


function SimilarThoughtsSection({ notes, ownNotes, categoryMessage, isDarkMode, currentUser, onShareAnotherThought }: SimilarThoughtsSectionProps) {
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

  const ownNoteCardStyle = {
    ...cardStyle,
    backgroundColor: isDarkMode ? 'rgba(102, 126, 234, 0.14)' : 'rgba(102, 126, 234, 0.08)',
    border: isDarkMode ? '1px solid rgba(129, 140, 248, 0.28)' : '1px solid rgba(102, 126, 234, 0.18)',
  };

  const renderNoteCards = (items: Note[], cardSx = cardStyle, showComments = false) => (
    <Box display="flex" flexWrap="wrap" gap={3} justifyContent="center" alignItems="flex-start">
      {items.map((noteItem, index) => (
        <Fade in timeout={600 + index * 200} key={noteItem.id}>
          <Card elevation={1} sx={cardSx}>
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
              {showComments && (
                <NoteComments
                  noteId={noteItem.id}
                  initialCount={noteItem.commentCount ?? 0}
                  currentUser={currentUser}
                  isDarkMode={isDarkMode}
                />
              )}
            </CardContent>
          </Card>
        </Fade>
      ))}
    </Box>
  );

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

          {/* Community notes */}
          <Box
            sx={{
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              mb: 4,
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
          </Box>

          {notes.length > 0 && (
            <Box sx={{ width: '100%', mb: ownNotes.length > 0 ? 4.5 : 0 }}>
              {renderNoteCards(notes, cardStyle, true)}
            </Box>
          )}

          {ownNotes.length > 0 && (
            <Box sx={{ width: '100%' }}>
              <Box sx={{ textAlign: 'center', mb: 2.2 }}>
                <Typography
                  variant="h6"
                  sx={{
                    fontWeight: 500,
                    color: isDarkMode ? '#e0e7ff' : '#3730a3',
                  }}
                >
                  Your related thoughts
                </Typography>
              </Box>
              {renderNoteCards(ownNotes, ownNoteCardStyle)}
            </Box>
          )}
        </Box>
      </Box>
    </Slide>
  );
}

export default SimilarThoughtsSection;
