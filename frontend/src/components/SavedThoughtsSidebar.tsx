import React, { useState } from 'react';
import { Box, Button, CircularProgress, IconButton, Paper, TextField, Tooltip, Typography } from '@mui/material';
import { Edit as EditIcon, MenuOpen as MenuOpenIcon } from '@mui/icons-material';
import axios from 'axios';
import { AuthUser, Note } from '../types';
import { getCategoryColor } from '../utils/categoryColors';
import NoteStatsRow from './NoteStatsRow';

interface SavedThoughtsSidebarProps {
  currentUser: AuthUser | null;
  savedNotes: Note[];
  notesLoading: boolean;
  notesError: string;
  isDarkMode: boolean;
  isMobile: boolean;
  isSidebarOpen: boolean;
  isSidebarClosing: boolean;
  sidebarWidth: number;
  onClose: () => void;
  onSelectNote: (note: Note) => void;
  onTransitionEnd: () => void;
  formatHistoryDate: (value: string) => string;
  headerHeight: number;
  onNoteUpdated: (noteId: number, newContent: string) => void;
}


const toLocalDateStr = (d: Date) => {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
};

const groupNotesByDate = (notes: Note[]): { label: string; notes: Note[] }[] => {
  const today = toLocalDateStr(new Date());
  const yesterday = toLocalDateStr(new Date(Date.now() - 86400000));
  const currentYear = new Date().getFullYear();

  const groupMap = new Map<string, Note[]>();
  for (const note of notes) {
    if (!groupMap.has(note.date)) groupMap.set(note.date, []);
    groupMap.get(note.date)!.push(note);
  }

  return Array.from(groupMap.entries()).map(([date, groupNotes]) => {
    let label: string;
    if (date === today) {
      label = 'Today';
    } else if (date === yesterday) {
      label = 'Yesterday';
    } else {
      const d = new Date(`${date}T00:00:00`);
      label = d.toLocaleDateString(undefined, {
        month: 'short',
        day: 'numeric',
        ...(d.getFullYear() !== currentYear && { year: 'numeric' }),
      });
    }
    return { label, notes: groupNotes };
  });
};

function SavedThoughtsSidebar({
  currentUser,
  savedNotes,
  notesLoading,
  notesError,
  isDarkMode,
  isMobile,
  isSidebarOpen,
  isSidebarClosing,
  sidebarWidth,
  onClose,
  onSelectNote,
  onTransitionEnd,
  headerHeight,
  onNoteUpdated,
}: SavedThoughtsSidebarProps) {
  const [editingNoteId, setEditingNoteId] = useState<number | null>(null);
  const [editDraft, setEditDraft] = useState('');
  const [editSaving, setEditSaving] = useState(false);
  const [editError, setEditError] = useState('');

  const todayStr = toLocalDateStr(new Date());

  const startEditing = (note: Note) => {
    setEditingNoteId(note.id);
    setEditDraft(note.content);
    setEditError('');
  };

  const cancelEditing = () => {
    setEditingNoteId(null);
    setEditDraft('');
    setEditError('');
  };

  const saveEdit = async (note: Note) => {
    if (!currentUser || !editDraft.trim() || editDraft.trim() === note.content) {
      cancelEditing();
      return;
    }
    setEditSaving(true);
    setEditError('');
    try {
      await axios.put(`/api/notes/${currentUser.id}/${note.id}`, { content: editDraft.trim() });
      onNoteUpdated(note.id, editDraft.trim());
      setEditingNoteId(null);
      setEditDraft('');
    } catch (error) {
      if (axios.isAxiosError(error)) {
        const msg = error.response?.data;
        setEditError(typeof msg === 'string' && msg.trim() ? msg : 'Could not save changes.');
      } else {
        setEditError('Could not save changes.');
      }
    } finally {
      setEditSaving(false);
    }
  };

  const groups = groupNotesByDate(savedNotes);

  const sidebarBackground = isMobile
    ? (isDarkMode ? '#000000' : '#f1f5f9')
    : isSidebarOpen
      ? (isDarkMode ? '#161716' : '#edf1f6')
      : (isDarkMode ? '#202120' : '#f1f5f9');

  const scrollTrackColor = isDarkMode ? '#202120' : '#edf1f6';
  const mutedColor = isDarkMode ? '#64748b' : '#94a3b8';
  const textColor = isDarkMode ? '#e5e7eb' : '#1f2937';
  const borderColor = isDarkMode ? 'rgba(255,255,255,0.05)' : 'rgba(15,23,42,0.05)';
  const accentOpacity = isDarkMode ? 'bb' : '99'; // hex alpha: ~73% / ~60%

  return (
    <Box
      sx={{
        position: 'fixed',
        top: isMobile ? '64px' : 0,
        left: 0,
        bottom: 0,
        width: sidebarWidth,
        zIndex: isMobile ? 1200 : 1000,
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
            height: isMobile ? '0px' : `${headerHeight}px`,
            display: isMobile ? 'none' : 'block',
            flexShrink: 0,
            backgroundColor: sidebarBackground,
            borderBottom: 'none',
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
            gap: 0,
            scrollbarWidth: 'thin',
            scrollbarColor: isDarkMode ? '#4b5563 #202120' : '#cbd5e1 #edf1f6',
            '&::-webkit-scrollbar': { width: '10px' },
            '&::-webkit-scrollbar-track': { backgroundColor: scrollTrackColor },
            '&::-webkit-scrollbar-thumb': {
              backgroundColor: isDarkMode ? '#4b5563' : '#cbd5e1',
              borderRadius: '999px',
              border: `2px solid ${scrollTrackColor}`,
            },
          }}
        >
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
            groups.map(({ label, notes: groupNotes }) => (
              <Box key={label} sx={{ mb: 1.5 }}>
                {/* Date group header */}
                <Box display="flex" alignItems="center" gap={1} sx={{ px: 0.5, mb: 0.8 }}>
                  <Typography sx={{
                    fontSize: '0.7rem',
                    fontWeight: 600,
                    letterSpacing: '0.06em',
                    textTransform: 'uppercase',
                    color: mutedColor,
                    whiteSpace: 'nowrap',
                    flexShrink: 0,
                  }}>
                    {label}
                  </Typography>
                  <Box sx={{ flex: 1, height: '1px', backgroundColor: borderColor }} />
                </Box>

                {/* Notes in this group */}
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.8 }}>
                  {groupNotes.map((savedNote) => {
                    const isToday = savedNote.date === todayStr;
                    const isEditing = editingNoteId === savedNote.id;

                    return (
                      <Box
                        key={savedNote.id}
                        sx={{
                          pl: 1.1,
                          pr: 1.1,
                          py: 1,
                          borderRadius: 2.5,
                          backgroundColor: isDarkMode ? 'rgba(255,255,255,0.02)' : 'rgba(255,255,255,0.55)',
                          border: `1px solid ${borderColor}`,
                          borderLeftColor: `${getCategoryColor(savedNote.category)}${accentOpacity}`,
                          borderLeftWidth: '3px',
                          transition: 'background-color 0.12s ease',
                          ...(!isEditing && {
                            cursor: 'pointer',
                            '&:hover': {
                              backgroundColor: isDarkMode ? 'rgba(255,255,255,0.06)' : 'rgba(255,255,255,0.85)',
                            },
                          }),
                        }}
                      >
                        {isEditing ? (
                          <Box>
                            <TextField
                              value={editDraft}
                              onChange={e => setEditDraft(e.target.value)}
                              onKeyDown={e => {
                                if (e.key === 'Escape') cancelEditing();
                                if (e.key === 'Enter' && (e.metaKey || e.ctrlKey)) saveEdit(savedNote);
                              }}
                              multiline
                              fullWidth
                              autoFocus
                              minRows={3}
                              maxRows={8}
                              inputProps={{ maxLength: 2000 }}
                              sx={{
                                mb: 1,
                                '& .MuiOutlinedInput-root': {
                                  fontSize: '0.87rem',
                                  backgroundColor: isDarkMode ? 'rgba(255,255,255,0.03)' : 'rgba(15,23,42,0.02)',
                                  '& fieldset': { borderColor },
                                  '&:hover fieldset': { borderColor },
                                  '&.Mui-focused fieldset': { borderColor: '#667eea' },
                                },
                                '& .MuiInputBase-input': { color: textColor },
                              }}
                            />
                            {editError && (
                              <Typography sx={{ fontSize: '0.78rem', color: '#ef4444', mb: 0.75 }}>
                                {editError}
                              </Typography>
                            )}
                            <Box display="flex" gap={1} alignItems="center">
                              <Button
                                size="small"
                                onClick={() => saveEdit(savedNote)}
                                disabled={editSaving || !editDraft.trim()}
                                sx={{ textTransform: 'none', fontSize: '0.8rem', color: '#667eea', minWidth: 0, px: 0.5, py: 0.25 }}
                              >
                                {editSaving ? <CircularProgress size={12} /> : 'Save'}
                              </Button>
                              <Button
                                size="small"
                                onClick={cancelEditing}
                                disabled={editSaving}
                                sx={{ textTransform: 'none', fontSize: '0.8rem', color: mutedColor, minWidth: 0, px: 0.5, py: 0.25 }}
                              >
                                Cancel
                              </Button>
                            </Box>
                          </Box>
                        ) : (
                          <Box onClick={() => onSelectNote(savedNote)}>
                            <Box display="flex" justifyContent="space-between" alignItems="center" mb={0.5}>
                              <Typography sx={{ fontSize: '0.72rem', color: mutedColor, letterSpacing: '0.01em' }}>
                                {savedNote.category}
                              </Typography>
                              {isToday && (
                                <Tooltip title="Edit" placement="top">
                                  <IconButton
                                    size="small"
                                    onClick={e => { e.stopPropagation(); startEditing(savedNote); }}
                                    sx={{ p: 0.3, color: mutedColor, '&:hover': { color: '#667eea' } }}
                                  >
                                    <EditIcon sx={{ fontSize: '0.85rem' }} />
                                  </IconButton>
                                </Tooltip>
                              )}
                            </Box>
                            <Typography
                              sx={{
                                fontSize: '0.87rem',
                                fontStyle: 'italic',
                                lineHeight: 1.5,
                                color: textColor,
                                display: '-webkit-box',
                                WebkitLineClamp: 3,
                                WebkitBoxOrient: 'vertical',
                                overflow: 'hidden',
                              }}
                            >
                              {savedNote.content}
                            </Typography>
                          </Box>
                        )}

                        {currentUser && !isEditing && (
                          <NoteStatsRow
                            noteId={savedNote.id}
                            userId={currentUser.id}
                            commentCount={savedNote.commentCount ?? 0}
                            currentUser={currentUser}
                            isDarkMode={isDarkMode}
                          />
                        )}
                      </Box>
                    );
                  })}
                </Box>
              </Box>
            ))
          )}
        </Box>
      </Paper>
    </Box>
  );
}

export default SavedThoughtsSidebar;
