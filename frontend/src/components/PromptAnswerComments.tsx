import React, { useState, useCallback, useEffect } from 'react';
import {
  Box,
  Typography,
  TextField,
  Button,
  IconButton,
  Collapse,
  CircularProgress,
} from '@mui/material';
import ChatBubbleOutlineIcon from '@mui/icons-material/ChatBubbleOutline';
import DeleteOutlineIcon from '@mui/icons-material/DeleteOutline';
import axios from 'axios';
import { Comment, AuthUser } from '../types';

interface PromptAnswerCommentsProps {
  answerId: number;
  initialCount: number;
  currentUser: AuthUser | null;
  isDarkMode: boolean;
}

function PromptAnswerComments({ answerId, initialCount, currentUser, isDarkMode }: PromptAnswerCommentsProps) {
  const [open, setOpen] = useState(false);
  const [comments, setComments] = useState<Comment[]>([]);
  const [loaded, setLoaded] = useState(false);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [draftText, setDraftText] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [count, setCount] = useState(initialCount ?? 0);

  const loadComments = useCallback(async () => {
    if (loaded || !answerId) return;
    setLoading(true);
    setError(null);
    try {
      const res = await axios.get<Comment[]>(`/api/prompt-answers/${answerId}/comments`);
      setComments(res.data);
      setCount(res.data.length);
      setLoaded(true);
    } catch {
      setError('Could not load comments.');
    } finally {
      setLoading(false);
    }
  }, [answerId, loaded]);

  useEffect(() => {
    loadComments();
  }, [loadComments]);

  const handleToggle = () => setOpen(prev => !prev);

  const handleSubmit = async () => {
    const text = draftText.trim();
    if (!text || submitting || !answerId) return;
    setSubmitting(true);
    setError(null);
    try {
      const res = await axios.post<Comment>(`/api/prompt-answers/${answerId}/comments`, { text });
      setComments(prev => [...prev, res.data]);
      setCount(prev => prev + 1);
      setDraftText('');
    } catch {
      setError('Could not post comment. Please try again.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (commentId: number) => {
    try {
      await axios.delete(`/api/prompt-answer-comments/${commentId}`);
      setComments(prev => prev.filter(c => c.id !== commentId));
      setCount(prev => Math.max(0, prev - 1));
    } catch {
      setError('Could not delete comment.');
    }
  };

  const mutedColor = isDarkMode ? '#94a3b8' : '#64748b';
  const textColor = isDarkMode ? '#e2e8f0' : '#374151';
  const borderColor = isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(15,23,42,0.08)';
  const inputBg = isDarkMode ? 'rgba(255,255,255,0.04)' : 'rgba(15,23,42,0.02)';

  return (
    <Box mt={1} pt={0.5}>
      {!open && (
        <Box
          display="flex"
          alignItems="center"
          gap={0.5}
          sx={{ cursor: 'pointer', width: 'fit-content' }}
          onClick={() => setOpen(true)}
        >
          <ChatBubbleOutlineIcon sx={{ fontSize: '0.95rem', color: mutedColor }} />
          <Typography variant="caption" sx={{ color: mutedColor, userSelect: 'none' }}>
            {count === 0 ? 'Add a comment' : `${count} comment${count === 1 ? '' : 's'}`}
          </Typography>
        </Box>
      )}

      <Collapse in={open} sx={{ overflow: 'hidden', '&.MuiCollapse-entered': { overflow: 'hidden' } }}>
        <Box mt={1.5}>
          {loading && (
            <Box display="flex" justifyContent="center" py={1}>
              <CircularProgress size={18} />
            </Box>
          )}


          {comments.map(comment => (
            <Box
              key={comment.id}
              display="flex"
              justifyContent="space-between"
              alignItems="flex-start"
              mb={1}
            >
              <Box>
                <Typography
                  variant="caption"
                  sx={{ fontWeight: 600, color: isDarkMode ? '#a5b4fc' : '#4f46e5', mr: 0.75 }}
                >
                  {comment.username}
                </Typography>
                <Typography variant="caption" sx={{ color: textColor }}>
                  {comment.text}
                </Typography>
              </Box>
              {comment.own && (
                <IconButton
                  size="small"
                  onClick={() => handleDelete(comment.id)}
                  sx={{ p: 0.25, color: mutedColor, '&:hover': { color: '#ef4444' } }}
                >
                  <DeleteOutlineIcon sx={{ fontSize: '0.9rem' }} />
                </IconButton>
              )}
            </Box>
          ))}

          {error && (
            <Typography variant="caption" sx={{ color: '#ef4444', display: 'block', mb: 1 }}>
              {error}
            </Typography>
          )}

          {currentUser ? (
            <Box display="flex" gap={1} alignItems="flex-end" mt={1}>
              <TextField
                value={draftText}
                onChange={e => setDraftText(e.target.value)}
                onKeyDown={e => e.key === 'Enter' && handleSubmit()}
                placeholder="Write a comment…"
                size="small"
                fullWidth
                inputProps={{ maxLength: 500 }}
                sx={{
                  '& .MuiOutlinedInput-root': {
                    fontSize: '0.82rem',
                    backgroundColor: inputBg,
                    '& fieldset': { borderColor },
                    '&:hover fieldset': { borderColor },
                    '&.Mui-focused fieldset': { borderColor },
                  },
                  '& .MuiInputBase-input': {
                    color: textColor,
                    textOverflow: 'ellipsis',
                    '&::placeholder': { color: mutedColor },
                    '&:focus::placeholder': { color: 'transparent' },
                  },
                }}
              />
              <Button
                onClick={handleSubmit}
                disabled={!draftText.trim() || submitting}
                size="small"
                variant="text"
                sx={{
                  textTransform: 'none',
                  color: isDarkMode ? '#a5b4fc' : '#4f46e5',
                  minWidth: 0,
                  px: 1,
                  fontSize: '0.8rem',
                  flexShrink: 0,
                }}
              >
                {submitting ? <CircularProgress size={14} /> : 'Post'}
              </Button>
            </Box>
          ) : (
            <Typography variant="caption" sx={{ color: mutedColor }}>
              Log in to leave a comment.
            </Typography>
          )}
        </Box>
      </Collapse>
    </Box>
  );
}

export default PromptAnswerComments;
