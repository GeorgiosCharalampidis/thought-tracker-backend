import React from 'react';
import {
  Box,
  Card,
  CardContent,
  CircularProgress,
  Dialog,
  DialogContent,
  Fade,
  IconButton,
  Typography,
} from '@mui/material';
import { Close as CloseIcon } from '@mui/icons-material';
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
        {/* Header */}
        <Box
          sx={{
            position: 'sticky',
            top: 0,
            zIndex: 1,
            backgroundColor: dialogBg,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            px: 3,
            pt: 2.5,
            pb: 1.5,
            borderBottom: isDarkMode ? '1px solid rgba(255,255,255,0.06)' : '1px solid rgba(15,23,42,0.07)',
          }}
        >
          <Typography sx={{ fontSize: '1rem', fontWeight: 500, color: textPrimary }}>
            My Prompts
          </Typography>
          <IconButton size="small" onClick={onClose} sx={{ color: textMuted }}>
            <CloseIcon sx={{ fontSize: 18 }} />
          </IconButton>
        </Box>

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
                    <Typography
                      sx={{
                        fontSize: '0.95rem',
                        color: textMuted,
                        fontStyle: 'italic',
                        lineHeight: 1.6,
                        mb: item.allAnswers.length > 0 ? 2 : 0,
                      }}
                    >
                      {item.myAnswer}
                    </Typography>

                    {/* Others' answers */}
                    {item.allAnswers.length > 0 && (
                      <Box>
                        <Typography sx={{ fontSize: '0.72rem', fontWeight: 600, color: textMuted, letterSpacing: '0.06em', textTransform: 'uppercase', mb: 1.25 }}>
                          Others said
                        </Typography>
                        <Box display="flex" flexWrap="wrap" gap={2}>
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
