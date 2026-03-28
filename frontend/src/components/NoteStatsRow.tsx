import React, { useCallback, useEffect, useState } from 'react';
import {
  Box,
  Button,
  CircularProgress,
  Collapse,
  IconButton,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material';
import ChatBubbleOutlineIcon from '@mui/icons-material/ChatBubbleOutline';
import FavoriteBorderIcon from '@mui/icons-material/FavoriteBorder';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import axios from 'axios';
import { AuthUser, Comment } from '../types';

interface ResonanceSnippet {
  bodyText: string;
  occurredAt: string;
}

interface NoteStatsRowProps {
  noteId: number;
  userId: number;
  commentCount: number;
  currentUser: AuthUser | null;
  isDarkMode: boolean;
}

function formatRelativeTime(iso: string): string {
  const diffMs = Date.now() - new Date(iso).getTime();
  const mins = Math.floor(diffMs / 60_000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins}m ago`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours}h ago`;
  return `${Math.floor(hours / 24)}d ago`;
}

function NoteStatsRow({ noteId, userId, commentCount, currentUser, isDarkMode }: NoteStatsRowProps) {
  const [commentsOpen, setCommentsOpen] = useState(false);
  const [resonancesOpen, setResonancesOpen] = useState(false);

  // Comments state
  const [comments, setComments] = useState<Comment[]>([]);
  const [commentsLoaded, setCommentsLoaded] = useState(false);
  const [commentsLoading, setCommentsLoading] = useState(false);
  const [draftText, setDraftText] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [commentError, setCommentError] = useState<string | null>(null);
  const [count, setCount] = useState(commentCount);

  // Resonances state
  const [resonances, setResonances] = useState<ResonanceSnippet[]>([]);
  const [resonancesLoaded, setResonancesLoaded] = useState(false);
  const [resonancesLoading, setResonancesLoading] = useState(false);
  const [resonanceError, setResonanceError] = useState<string | null>(null);

  const loadComments = useCallback(async () => {
    if (commentsLoaded) return;
    setCommentsLoading(true);
    try {
      const res = await axios.get<Comment[]>(`/api/notes/${noteId}/comments`);
      setComments(res.data);
      setCount(res.data.length);
      setCommentsLoaded(true);
    } catch {
      setCommentError('Could not load comments.');
    } finally {
      setCommentsLoading(false);
    }
  }, [noteId, commentsLoaded]);

  const loadResonances = useCallback(async () => {
    if (resonancesLoaded) return;
    setResonancesLoading(true);
    try {
      const res = await axios.get<ResonanceSnippet[]>(`/api/notes/${userId}/${noteId}/resonances`);
      setResonances(res.data);
      setResonancesLoaded(true);
    } catch {
      setResonanceError('Could not load resonances.');
    } finally {
      setResonancesLoading(false);
    }
  }, [noteId, userId, resonancesLoaded]);

  useEffect(() => { if (commentsOpen) loadComments(); }, [commentsOpen, loadComments]);
  useEffect(() => { loadResonances(); }, [loadResonances]);

  const handleSubmitComment = async () => {
    const text = draftText.trim();
    if (!text || submitting) return;
    setSubmitting(true);
    setCommentError(null);
    try {
      const res = await axios.post<Comment>(`/api/notes/${noteId}/comments`, { text });
      setComments(prev => [...prev, res.data]);
      setCount(prev => prev + 1);
      setDraftText('');
    } catch {
      setCommentError('Could not post comment.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDeleteComment = async (commentId: number) => {
    try {
      await axios.delete(`/api/comments/${commentId}`);
      setComments(prev => prev.filter(c => c.id !== commentId));
      setCount(prev => Math.max(0, prev - 1));
    } catch {
      setCommentError('Could not delete comment.');
    }
  };

  const resonanceCount = resonancesLoaded ? resonances.length : 0;

  const mutedColor = isDarkMode ? '#64748b' : '#94a3b8';
  const textColor = isDarkMode ? '#e2e8f0' : '#374151';
  const borderColor = isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(15,23,42,0.08)';
  const inputBg = isDarkMode ? 'rgba(255,255,255,0.04)' : 'rgba(15,23,42,0.02)';
  const resonanceColor = isDarkMode ? '#34d399' : '#059669';
  const commentActiveColor = isDarkMode ? '#a5b4fc' : '#4f46e5';

  return (
    <Box mt={1} pt={0}>
      {/* Icon row */}
      <Box display="flex" alignItems="center" gap={1.5}>
        <Box
          display="flex" alignItems="center" gap={0.4}
          onClick={() => setCommentsOpen(o => !o)}
          sx={{
            cursor: 'pointer',
            color: commentsOpen ? commentActiveColor : mutedColor,
            '&:hover': { color: commentsOpen ? commentActiveColor : mutedColor },
            '& svg': { transition: 'none' },
          }}
        >
          <ChatBubbleOutlineIcon sx={{ fontSize: '0.85rem', color: 'inherit' }} />
          <Typography variant="caption" sx={{ color: 'inherit', lineHeight: 1, userSelect: 'none' }}>
            {count > 0 ? `${count} comment${count === 1 ? '' : 's'}` : 'Add a comment'}
          </Typography>
        </Box>

        {resonancesLoaded && resonanceCount > 0 && (
          <Tooltip title="Someone else wrote something with a similar feeling to this thought." placement="top">
            <Box
              display="flex" alignItems="center" gap={0.4}
              onClick={() => setResonancesOpen(o => !o)}
              sx={{ cursor: 'pointer' }}
            >
              <FavoriteBorderIcon sx={{ fontSize: '0.85rem', color: resonanceColor }} />
              <Typography variant="caption" sx={{ color: resonanceColor, lineHeight: 1, userSelect: 'none' }}>
                {`${resonanceCount} resonances`}
              </Typography>
            </Box>
          </Tooltip>
        )}
      </Box>

      {/* Comments panel */}
      <Collapse in={commentsOpen}>
        <Box mt={1.5}>
          {commentsLoading && (
            <Box display="flex" justifyContent="center" py={1}>
              <CircularProgress size={16} />
            </Box>
          )}
          {comments.map(comment => (
            <Box key={comment.id} display="flex" justifyContent="space-between" alignItems="flex-start" mb={1}>
              <Box>
                <Typography variant="caption" sx={{ fontWeight: 600, color: commentActiveColor, mr: 0.75 }}>
                  {comment.username}
                </Typography>
                <Typography variant="caption" sx={{ color: textColor }}>
                  {comment.text}
                </Typography>
              </Box>
              {comment.own && (
                <IconButton size="small" onClick={() => handleDeleteComment(comment.id)}
                  sx={{ p: 0.25, color: mutedColor, '&:hover': { color: '#ef4444' } }}>
                  <DeleteOutlineIcon sx={{ fontSize: '0.9rem' }} />
                </IconButton>
              )}
            </Box>
          ))}
          {commentError && (
            <Typography variant="caption" sx={{ color: '#ef4444', display: 'block', mb: 1 }}>{commentError}</Typography>
          )}
          {currentUser ? (
            <Box display="flex" gap={1} alignItems="flex-end" mt={1}>
              <TextField
                value={draftText}
                onChange={e => setDraftText(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && !e.shiftKey && handleSubmitComment()}
                placeholder="Write a comment…"
                multiline maxRows={3} size="small" fullWidth
                inputProps={{ maxLength: 500 }}
                sx={{
                  '& .MuiOutlinedInput-root': {
                    fontSize: '0.82rem', backgroundColor: inputBg,
                    '& fieldset': { borderColor }, '&:hover fieldset': { borderColor }, '&.Mui-focused fieldset': { borderColor },
                  },
                  '& .MuiInputBase-input': { color: textColor, '&::placeholder': { color: mutedColor }, '&:focus::placeholder': { color: 'transparent' } },
                }}
              />
              <Button onClick={handleSubmitComment} disabled={!draftText.trim() || submitting}
                size="small" variant="text"
                sx={{ textTransform: 'none', color: commentActiveColor, minWidth: 0, px: 1, fontSize: '0.8rem', flexShrink: 0 }}>
                {submitting ? <CircularProgress size={14} /> : 'Post'}
              </Button>
            </Box>
          ) : (
            <Typography variant="caption" sx={{ color: mutedColor }}>Log in to comment.</Typography>
          )}
        </Box>
      </Collapse>

      {/* Resonances panel */}
      <Collapse in={resonancesOpen}>
        <Box mt={1.5}>
          {resonancesLoading && (
            <Box display="flex" justifyContent="center" py={1}>
              <CircularProgress size={16} sx={{ color: resonanceColor }} />
            </Box>
          )}
          {resonances.map((r, idx) => (
            <Box key={idx} mb={1.5}>
              <Box display="flex" justifyContent="space-between" alignItems="baseline" mb={0.3}>
                <Typography variant="caption" sx={{ fontWeight: 600, color: resonanceColor }}>
                  Someone resonated
                </Typography>
                <Typography variant="caption" sx={{ color: mutedColor, fontSize: '0.72rem' }}>
                  {formatRelativeTime(r.occurredAt)}
                </Typography>
              </Box>
              <Typography variant="caption" sx={{ color: textColor, fontStyle: 'italic', lineHeight: 1.4, display: 'block' }}>
                "{r.bodyText}"
              </Typography>
            </Box>
          ))}
          {resonanceError && (
            <Typography variant="caption" sx={{ color: '#ef4444', display: 'block' }}>{resonanceError}</Typography>
          )}
        </Box>
      </Collapse>
    </Box>
  );
}

export default NoteStatsRow;
