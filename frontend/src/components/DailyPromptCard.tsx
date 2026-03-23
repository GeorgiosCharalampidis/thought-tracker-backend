import React, { useRef, useState } from 'react';
import { Box, Button, Card, CardContent, CircularProgress, Collapse, Fade, Typography } from '@mui/material';
import { ExpandMore as ChevronIcon, PeopleOutline as PeopleIcon } from '@mui/icons-material';
import { AuthUser, DailyPrompt, PromptAnswerResponse } from '../types';
import PromptAnswerComments from './PromptAnswerComments';

interface DailyPromptCardProps {
  prompt: DailyPrompt;
  currentUser: AuthUser;
  isDarkMode: boolean;
  isMobile: boolean;
  onSubmitAnswer: (answerText: string) => Promise<void>;
  onLoadAnswers: () => Promise<PromptAnswerResponse[]>;
}

function DailyPromptCard({
  prompt,
  currentUser,
  isDarkMode,
  onSubmitAnswer,
  onLoadAnswers,
}: DailyPromptCardProps) {
  const answered = !!prompt.userAnswerText;
  const [inputOpen, setInputOpen] = useState(false);
  const [answerInput, setAnswerInput] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [othersOpen, setOthersOpen] = useState(false);
  const [otherAnswers, setOtherAnswers] = useState<PromptAnswerResponse[]>([]);
  const [answersLoading, setAnswersLoading] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const textMuted = isDarkMode ? '#64748b' : '#94a3b8';
  const textPrimary = isDarkMode ? '#f1f5f9' : '#1e293b';
  const cardBg = isDarkMode ? '#252626' : '#fafafa';
  const cardBorder = isDarkMode ? '1px solid #333434' : '1px solid #e8ecf0';

  const cardMaxWidth = (text: string) => {
    if (text.length < 60) return '220px';
    if (text.length < 130) return '290px';
    if (text.length < 230) return '360px';
    return '440px';
  };

  const handleSubmit = async () => {
    if (!answerInput.trim() || submitting) return;
    setSubmitting(true);
    try {
      await onSubmitAnswer(answerInput.trim());
      setAnswerInput('');
      setInputOpen(false);
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleOthers = async () => {
    if (!othersOpen && otherAnswers.length === 0) {
      setAnswersLoading(true);
      try {
        const answers = await onLoadAnswers();
        setOtherAnswers(answers);
      } finally {
        setAnswersLoading(false);
      }
    }
    setOthersOpen((prev) => !prev);
  };

  const handleOpenInput = () => {
    if (answered) return;
    setInputOpen((p) => !p);
    if (!inputOpen) setTimeout(() => textareaRef.current?.focus(), 150);
  };

  return (
    <Fade in timeout={600}>
      <Box sx={{ display: 'flex', justifyContent: 'center' }}>
        <Box sx={{ width: '100%', maxWidth: '800px', mb: 4 }}>

          {/* Label */}
          <Box display="flex" justifyContent="center" mb={1}>
            <Typography sx={{ fontSize: '0.7rem', fontWeight: 600, letterSpacing: '0.09em', textTransform: 'uppercase', color: '#667eea', opacity: 0.75 }}>
              Today's prompt
            </Typography>
          </Box>

          {/* Question */}
          <Box
            display="flex"
            justifyContent="center"
            alignItems="center"
            gap={0.5}
            mb={answered ? 1.25 : 0}
            onClick={handleOpenInput}
            sx={{ cursor: answered ? 'default' : 'pointer', '&:hover': answered ? {} : { opacity: 1 }, opacity: 0.85, transition: 'opacity 0.15s ease' }}
          >
            <Typography
              sx={{
                fontWeight: 400,
                fontSize: { xs: '0.97rem', sm: '1.1rem' },
                color: textPrimary,
                textAlign: 'center',
                lineHeight: 1.55,
              }}
            >
              {prompt.question}
            </Typography>
            {!answered && (
              <ChevronIcon sx={{ fontSize: 18, color: textMuted, flexShrink: 0, transform: inputOpen ? 'rotate(180deg)' : 'rotate(0deg)', transition: 'transform 0.2s ease' }} />
            )}
          </Box>

          {/* Unanswered: inline plain textarea */}
          {!answered && (
            <Collapse in={inputOpen}>
              <Box sx={{ mt: 2, display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1.5 }}>
                <textarea
                  ref={textareaRef}
                  value={answerInput}
                  onChange={(e) => setAnswerInput(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === 'Enter' && !e.shiftKey) {
                      e.preventDefault();
                      void handleSubmit();
                    }
                  }}
                  maxLength={500}
                  rows={3}
                  placeholder="Write your answer…"
                  style={{
                    width: '100%',
                    resize: 'none',
                    background: 'transparent',
                    border: 'none',
                    borderWidth: 0,
                    boxShadow: 'none',
                    outline: 'none',
                    color: textPrimary,
                    fontSize: '1rem',
                    lineHeight: '1.6',
                    fontFamily: 'inherit',
                    padding: '4px 0 8px',
                    appearance: 'none',
                  }}
                />
                <Button
                  size="small"
                  onClick={() => void handleSubmit()}
                  disabled={submitting || !answerInput.trim()}
                  sx={{
                    textTransform: 'none',
                    fontSize: '0.8rem',
                    borderRadius: 999,
                    px: 2,
                    py: 0.4,
                    color: '#ffffff',
                    backgroundColor: '#667eea',
                    minWidth: 0,
                    '&:hover': { backgroundColor: '#5a67d8' },
                    '&:disabled': { opacity: 0.4, color: '#ffffff', backgroundColor: '#667eea' },
                  }}
                >
                  {submitting ? <CircularProgress size={12} sx={{ color: '#ffffff' }} /> : 'Share'}
                </Button>
              </Box>
            </Collapse>
          )}

          {/* Answered: show user's answer + toggle others */}
          {answered && (
            <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1 }}>
              <Typography sx={{ fontSize: '0.95rem', color: textMuted, textAlign: 'center', lineHeight: 1.6, fontStyle: 'italic' }}>
                {prompt.userAnswerText}
              </Typography>

              <Button
                size="small"
                variant="text"
                onClick={handleToggleOthers}
                disabled={answersLoading}
                startIcon={<PeopleIcon sx={{ fontSize: '0.95rem !important' }} />}
                endIcon={<ChevronIcon sx={{ fontSize: '1rem !important', transform: othersOpen ? 'rotate(180deg)' : 'rotate(0deg)', transition: 'transform 0.2s ease' }} />}
                sx={{
                  textTransform: 'none',
                  fontSize: '0.88rem',
                  borderRadius: 999,
                  px: 1.5,
                  py: 0.4,
                  color: isDarkMode ? '#cbd5e1' : '#475569',
                  '&:hover': {
                    backgroundColor: 'transparent',
                    color: isDarkMode ? '#f1f5f9' : '#1e293b',
                  },
                }}
              >
                {answersLoading ? 'Loading…' : othersOpen ? "Hide others' answers" : 'See what others said'}
              </Button>

              <Collapse in={othersOpen} sx={{ width: '100%' }}>
                <Box sx={{ mt: 1 }}>
                  {otherAnswers.length === 0 ? (
                    <Typography sx={{ fontSize: '0.82rem', color: textMuted, fontStyle: 'italic', textAlign: 'center' }}>
                      No other answers yet.
                    </Typography>
                  ) : (
                    <Box display="flex" flexWrap="wrap" gap={2} justifyContent="center" alignItems="flex-start">
                      {otherAnswers.map((answer, i) => (
                        <Fade in timeout={400 + i * 150} key={answer.id}>
                          <Card
                            elevation={0}
                            sx={{
                              width: cardMaxWidth(answer.answerText),
                              borderRadius: 2.5,
                              backgroundColor: cardBg,
                              border: cardBorder,
                              cursor: 'default',
                              userSelect: 'text',
                              transition: 'border-color 0.15s ease',
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
                  )}
                </Box>
              </Collapse>
            </Box>
          )}

        </Box>
      </Box>
    </Fade>
  );
}

export default DailyPromptCard;
