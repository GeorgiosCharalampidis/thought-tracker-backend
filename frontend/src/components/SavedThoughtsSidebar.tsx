import React from 'react';
import { Box, CircularProgress, IconButton, Paper, Tooltip, Typography } from '@mui/material';
import { MenuOpen as MenuOpenIcon } from '@mui/icons-material';
import { AuthUser, Note } from '../types';
import NoteComments from './NoteComments';

interface SavedThoughtsSidebarProps {
  currentUser: AuthUser | null;
  savedNotes: Note[];
  notesLoading: boolean;
  notesError: string;
  isDarkMode: boolean;
  isSidebarOpen: boolean;
  isSidebarClosing: boolean;
  sidebarWidth: number;
  onClose: () => void;
  onSelectNote: (note: Note) => void;
  onTransitionEnd: () => void;
  formatHistoryDate: (value: string) => string;
}

function SavedThoughtsSidebar({
  currentUser,
  savedNotes,
  notesLoading,
  notesError,
  isDarkMode,
  isSidebarOpen,
  isSidebarClosing,
  sidebarWidth,
  onClose,
  onSelectNote,
  onTransitionEnd,
  formatHistoryDate,
}: SavedThoughtsSidebarProps) {
  const sidebarBackground = isSidebarOpen
    ? (isDarkMode ? '#2c2d2c' : '#edf1f6')
    : (isDarkMode ? '#202120' : '#f1f5f9');

  const scrollTrackColor = isDarkMode ? '#202120' : '#edf1f6';

  return (
    <Box
      sx={{
        position: 'fixed',
        top: 0,
        left: 0,
        bottom: 0,
        width: sidebarWidth,
        zIndex: 1000,
        transform: isSidebarOpen ? 'translateX(0)' : `translateX(-${sidebarWidth}px)`,
        transition: 'transform 0.22s ease',
        overflow: 'hidden',
      }}
      onTransitionEnd={(event) => {
        if (event.target === event.currentTarget) {
          onTransitionEnd();
        }
      }}
    >
      <Paper
        elevation={0}
        sx={{
          position: 'relative',
          height: '100%',
          borderRadius: 0,
          backgroundColor: sidebarBackground,
          borderRight: isDarkMode ? '1px solid rgba(255,255,255,0.08)' : '1px solid rgba(15,23,42,0.08)',
          overflow: 'hidden',
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        <Box
          sx={{
            position: 'relative',
            height: '136px',
            flexShrink: 0,
            backgroundColor: sidebarBackground,
            borderBottom: isDarkMode ? '1px solid rgba(255,255,255,0.04)' : '1px solid rgba(15,23,42,0.04)',
          }}
        >
          <Tooltip title="Close sidebar" placement="left">
            <IconButton
              onClick={onClose}
              sx={{
                position: 'absolute',
                top: 18,
                right: 12,
                width: 36,
                height: 36,
                zIndex: 2,
                opacity: isSidebarClosing ? 0 : 1,
                pointerEvents: isSidebarClosing ? 'none' : 'auto',
                color: isDarkMode ? '#cbd5e1' : '#475569',
                backgroundColor: isDarkMode ? '#2c2d2c' : '#edf1f6',
                border: isDarkMode ? '1px solid rgba(255,255,255,0.08)' : '1px solid rgba(15,23,42,0.08)',
                transition: 'opacity 0.08s ease',
                '&:hover': {
                  backgroundColor: isDarkMode ? '#363736' : '#e3e8ef',
                },
              }}
            >
              <MenuOpenIcon sx={{ fontSize: 18 }} />
            </IconButton>
          </Tooltip>
        </Box>

        <Box
          sx={{
            flex: 1,
            minHeight: 0,
            px: 1.1,
            pb: 1.1,
            pt: 1.1,
            overflowY: 'auto',
            display: 'flex',
            flexDirection: 'column',
            gap: 0.8,
            scrollbarWidth: 'thin',
            scrollbarColor: isDarkMode ? '#4b5563 #202120' : '#cbd5e1 #edf1f6',
            '&::-webkit-scrollbar': {
              width: '10px',
            },
            '&::-webkit-scrollbar-track': {
              backgroundColor: scrollTrackColor,
            },
            '&::-webkit-scrollbar-thumb': {
              backgroundColor: isDarkMode ? '#4b5563' : '#cbd5e1',
              borderRadius: '999px',
              border: `2px solid ${scrollTrackColor}`,
            },
          }}
        >
          {currentUser && (
            <Typography
              sx={{
                px: 1.1,
                pt: 0,
                pb: 0.15,
                fontSize: '0.88rem',
                color: isDarkMode ? '#cbd5e1' : '#475569',
              }}
            >
              Your thoughts
            </Typography>
          )}

          {!currentUser ? (
            <Box sx={{ px: 1.1, py: 0.2 }}>
              <Typography sx={{ fontSize: '0.86rem', color: isDarkMode ? '#cbd5e1' : '#475569', lineHeight: 1.6 }}>
                Log in to browse your saved thoughts here.
              </Typography>
            </Box>
          ) : notesLoading ? (
            <Box display="flex" justifyContent="center" py={2}>
              <CircularProgress size={20} sx={{ color: isDarkMode ? '#cbd5e1' : '#667eea' }} />
            </Box>
          ) : notesError ? (
            <Typography sx={{ px: 1.1, py: 1.2, fontSize: '0.82rem', color: isDarkMode ? '#fca5a5' : '#b91c1c' }}>
              {notesError}
            </Typography>
          ) : savedNotes.length === 0 ? (
            <Typography sx={{ px: 1.1, py: 1.2, fontSize: '0.84rem', color: isDarkMode ? '#94a3b8' : '#64748b' }}>
              No saved thoughts yet.
            </Typography>
          ) : (
            savedNotes.map((savedNote) => (
              <Box
                key={savedNote.id}
                sx={{
                  px: 1.1,
                  py: 1,
                  borderRadius: 2.5,
                  backgroundColor: isDarkMode ? 'rgba(255,255,255,0.02)' : 'rgba(255,255,255,0.55)',
                  border: isDarkMode ? '1px solid rgba(255,255,255,0.05)' : '1px solid rgba(15,23,42,0.05)',
                }}
              >
                {/* Clickable header — selects the note */}
                <Box
                  onClick={() => onSelectNote(savedNote)}
                  sx={{
                    cursor: 'pointer',
                    borderRadius: 1.5,
                    '&:hover': {
                      backgroundColor: isDarkMode ? 'rgba(255,255,255,0.04)' : 'rgba(15,23,42,0.03)',
                    },
                  }}
                >
                  <Box display="flex" justifyContent="space-between" alignItems="center" mb={0.6}>
                    <Typography sx={{ fontSize: '0.73rem', color: isDarkMode ? '#94a3b8' : '#64748b' }}>
                      {formatHistoryDate(savedNote.date)}
                    </Typography>
                    <Typography sx={{ fontSize: '0.73rem', color: isDarkMode ? '#a8b0bd' : '#64748b' }}>
                      {savedNote.category}
                    </Typography>
                  </Box>
                  <Typography
                    sx={{
                      fontSize: '0.87rem',
                      lineHeight: 1.45,
                      color: isDarkMode ? '#e5e7eb' : '#1f2937',
                      display: '-webkit-box',
                      WebkitLineClamp: 3,
                      WebkitBoxOrient: 'vertical',
                      overflow: 'hidden',
                    }}
                  >
                    {savedNote.content}
                  </Typography>
                </Box>

                {/* Comments section */}
                <NoteComments
                  noteId={savedNote.id}
                  initialCount={savedNote.commentCount ?? 0}
                  currentUser={currentUser}
                  isDarkMode={isDarkMode}
                />
              </Box>
            ))
          )}
        </Box>
      </Paper>
    </Box>
  );
}

export default SavedThoughtsSidebar;

