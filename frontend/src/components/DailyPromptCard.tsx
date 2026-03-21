import React, { useRef, useState } from 'react';
import { Box, Button, CircularProgress, Collapse, Fade, Typography } from '@mui/material';
import { ExpandMore as ChevronIcon } from '@mui/icons-material';
import { AuthUser, DailyPrompt } from '../types';

interface DailyPromptCardProps {
  prompt: DailyPrompt;
  currentUser: AuthUser;
  isDarkMode: boolean;
  isMobile: boolean;
  onSubmitAnswer: (answerText: string) => Promise<void>;
  onLoadAnswers: () => Promise<string[]>;
}

function DailyPromptCard({
  prompt,
  isDarkMode,
  onSubmitAnswer,
  onLoadAnswers,
}: DailyPromptCardProps) {
  const answered = !!prompt.userAnswerText;
  const [inputOpen, setInputOpen] = useState(false);
  const [answerInput, setAnswerInput] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [othersOpen, setOthersOpen] = useState(false);
  const [otherAnswers, setOtherAnswers] = useState<string[]>([]);
  const [answersLoading, setAnswersLoading] = useState(false);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const textMuted = isDarkMode ? '#64748b' : '#94a3b8';
  const textPrimary = isDarkMode ? '#f1f5f9' : '#1e293b';

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

          {/* Answered: plain answer text + others toggle */}
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
                sx={{ textTransform: 'none', fontSize: '0.75rem', color: textMuted, p: 0, minWidth: 0, opacity: 0.7, '&:hover': { color: textPrimary, backgroundColor: 'transparent', opacity: 1 } }}
              >
                {answersLoading ? 'Loading…' : othersOpen ? "Hide others' answers" : 'See what others said'}
              </Button>

              <Collapse in={othersOpen} sx={{ width: '100%' }}>
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.75, mt: 0.5 }}>
                  {otherAnswers.length === 0 ? (
                    <Typography sx={{ fontSize: '0.82rem', color: textMuted, fontStyle: 'italic', textAlign: 'center' }}>
                      No other answers yet.
                    </Typography>
                  ) : (
                    otherAnswers.map((answer, i) => (
                      <Typography key={i} sx={{ fontSize: '0.88rem', color: textMuted, textAlign: 'center', lineHeight: 1.55, opacity: 0.75 }}>
                        {answer}
                      </Typography>
                    ))
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
