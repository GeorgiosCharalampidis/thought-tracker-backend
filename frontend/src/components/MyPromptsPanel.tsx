import React, { useState } from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Collapse,
  Dialog,
  DialogContent,
  Fade,
  IconButton,
  Tooltip,
  Typography,
} from '@mui/material';
import { ChatBubbleOutline as CommentIcon, Close as CloseIcon, People as PeopleIcon } from '@mui/icons-material';
import { AnsweredPromptSummary, AuthUser } from '../types';
import PromptAnswerComments from './PromptAnswerComments';


interface MyPromptsPanelProps {
  open: boolean;
  onClose: () => void;
  prompts: AnsweredPromptSummary[];
  loading: boolean;
  currentUser: AuthUser;
  isDarkMode: boolean;
}

function MyPromptsPanel({ open, onClose, prompts, loading, currentUser, isDarkMode }: MyPromptsPanelProps) {
  const [expandedPrompts, setExpandedPrompts] = useState<Set<number>>(new Set());
  const [commentsOpenMap, setCommentsOpenMap] = useState<Record<number, boolean>>({});

  const toggleOthers = (promptIndex: number) => {
    setExpandedPrompts(prev => {
      const next = new Set(prev);
      if (next.has(promptIndex)) next.delete(promptIndex);
      else next.add(promptIndex);
      return next;
    });
  };

  const textPrimary = isDarkMode ? '#f1f5f9' : '#1e293b';
  const textMuted = isDarkMode ? '#64748b' : '#94a3b8';
  const cardBg = isDarkMode ? '#252626' : '#fafafa';
  const cardBorder = isDarkMode ? '1px solid #333434' : '1px solid #e8ecf0';
  const dialogBg = isDarkMode ? '#202120' : '#f1f5f9';

  const cardMaxWidth = (text: string) => {
    if (text.length < 60) return '220px';
    if (text.length < 130) return '290px';
    if (text.length < 230) return '360px';
    return '440px';
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      fullWidth
      maxWidth="md"
      disableScrollLock
      PaperProps={{
        sx: {
          backgroundColor: dialogBg,
          backgroundImage: 'none',
          borderRadius: 3,
          border: isDarkMode ? '1px solid rgba(255,255,255,0.06)' : '1px solid rgba(15,23,42,0.08)',
          maxHeight: '85vh',
        },
      }}
    >
      <DialogContent sx={{ p: 0 }}>
        {/* Close button — absolutely positioned so it takes no vertical space */}
        <IconButton
          size="small"
          onClick={onClose}
          sx={{ position: 'absolute', top: 8, right: 8, zIndex: 1, color: textMuted }}
        >
          <CloseIcon sx={{ fontSize: 18 }} />
        </IconButton>

        {/* Content */}
        <Box sx={{ px: 3, py: 2.5 }}>
          {loading ? (
            <Box display="flex" justifyContent="center" py={6}>
              <CircularProgress size={24} sx={{ color: '#667eea' }} />
            </Box>
          ) : prompts.length === 0 ? (
            <Typography sx={{ color: textMuted, fontSize: '0.9rem', textAlign: 'center', py: 6, fontStyle: 'italic' }}>
              You haven't answered any prompts yet.
            </Typography>
          ) : (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
              {prompts.map((item) => (
                <Fade in timeout={400} key={item.promptIndex}>
                  <Box>
                    {/* Question */}
                    <Typography
                      sx={{
                        fontSize: '0.7rem',
                        fontWeight: 600,
                        letterSpacing: '0.09em',
                        textTransform: 'uppercase',
                        color: '#667eea',
                        opacity: 0.75,
                        mb: 0.75,
                      }}
                    >
                      Prompt
                    </Typography>
                    <Typography
                      sx={{
                        fontSize: '1rem',
                        fontWeight: 400,
                        color: textPrimary,
                        lineHeight: 1.55,
                        mb: 1.25,
                      }}
                    >
                      {item.question}
                    </Typography>

                    {/* My answer */}
                    <Box sx={{ lineHeight: 1.6, mb: item.allAnswers.length > 0 ? 2 : 0 }}>
                      <Typography
                        component="span"
                        sx={{
                          fontSize: '0.95rem',
                          color: isDarkMode ? '#94a3b8' : '#475569',
                          fontStyle: 'italic',
                          lineHeight: 1.6,
                        }}
                      >
                        {item.myAnswer}
                      </Typography>
                        <Box
                          component="span"
                          display="inline-flex" alignItems="center" gap={0.25}
                          onClick={() => setCommentsOpenMap(prev => ({ ...prev, [item.promptIndex]: !prev[item.promptIndex] }))}
                          sx={{ cursor: 'pointer', ml: 1.5, px: 0.5, py: 0.4, color: commentsOpenMap[item.promptIndex] ? '#667eea' : (isDarkMode ? '#94a3b8' : '#64748b'), verticalAlign: 'middle', borderRadius: 999, '&:hover': { color: '#667eea', backgroundColor: isDarkMode ? 'rgba(102,126,234,0.15)' : 'rgba(102,126,234,0.1)' } }}
                        >
                          <CommentIcon sx={{ fontSize: '1rem' }} />
                          {item.myAnswerCommentCount > 0 && (
                            <Typography component="span" sx={{ fontSize: '0.72rem', lineHeight: 1, userSelect: 'none' }}>
                              {item.myAnswerCommentCount}
                            </Typography>
                          )}
                        </Box>
                    </Box>

                    <PromptAnswerComments
                      answerId={item.myAnswerId}
                      initialCount={item.myAnswerCommentCount}
                      currentUser={currentUser}
                      isDarkMode={isDarkMode}
                      open={commentsOpenMap[item.promptIndex] ?? false}
                      onToggle={() => setCommentsOpenMap(prev => ({ ...prev, [item.promptIndex]: !prev[item.promptIndex] }))}
                    />

                    {/* Others' answers */}
                    {item.allAnswers.length > 0 && (
                      <Box>
                        <Button
                          size="small"
                          startIcon={<PeopleIcon sx={{ fontSize: 15 }} />}
                          onClick={() => toggleOthers(item.promptIndex)}
                          sx={{
                            textTransform: 'none',
                            fontSize: '0.8rem',
                            color: textMuted,
                            px: 0,
                            minWidth: 0,
                            '&:hover': { backgroundColor: 'transparent', color: textPrimary },
                          }}
                        >
                          {expandedPrompts.has(item.promptIndex)
                            ? 'Hide others'
                            : `See what others said (${item.allAnswers.length})`}
                        </Button>
                        <Collapse in={expandedPrompts.has(item.promptIndex)}>
                          <Box display="flex" flexWrap="wrap" alignItems="flex-start" gap={2} mt={1.25}>
                            {item.allAnswers.map((answer, i) => (
                              <Fade in timeout={300 + i * 100} key={answer.id}>
                                <Card
                                  elevation={0}
                                  sx={{
                                    width: cardMaxWidth(answer.answerText),
                                    borderRadius: 2.5,
                                    backgroundColor: cardBg,
                                    border: cardBorder,
                                    cursor: 'default',
                                    userSelect: 'text',
                                    '&:hover': {
                                      borderColor: isDarkMode ? '#4a4b4a' : '#c8d0da',
                                    },
                                  }}
                                >
                                  <CardContent sx={{ p: 2.5, '&:last-child': { pb: 2.5 } }}>
                                    {answer.username && (
                                      <Typography sx={{ fontSize: '0.72rem', fontWeight: 600, color: isDarkMode ? '#a5b4fc' : '#4f46e5', mb: 0.75, opacity: 0.85 }}>
                                        {answer.username}
                                      </Typography>
                                    )}
                                    <Typography
                                      sx={{
                                        fontSize: '0.9rem',
                                        color: isDarkMode ? '#cbd5e1' : '#374151',
                                        lineHeight: 1.65,
                                        fontStyle: 'italic',
                                        opacity: 0.9,
                                      }}
                                    >
                                      {answer.answerText}
                                    </Typography>
                                    <PromptAnswerComments
                                      answerId={answer.id}
                                      initialCount={answer.commentCount}
                                      currentUser={currentUser}
                                      isDarkMode={isDarkMode}
                                    />
                                  </CardContent>
                                </Card>
                              </Fade>
                            ))}
                          </Box>
                        </Collapse>
                      </Box>
                    )}
                  </Box>
                </Fade>
              ))}
            </Box>
          )}
        </Box>
      </DialogContent>
    </Dialog>
  );
}

export default MyPromptsPanel;
