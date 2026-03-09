import React, { useEffect, useRef, useState } from 'react';
import {
  Box,
  Fade,
  IconButton,
  Paper,
  Tooltip,
  Typography,
} from '@mui/material';
import {
  AutoAwesome as SparkleIcon,
  Send as SendIcon,
} from '@mui/icons-material';
import { ChatMessage } from '../types';

interface AiChatPanelProps {
  messages: ChatMessage[];
  loading: boolean;
  isDarkMode: boolean;
  onSendMessage: (content: string) => void;
}

const renderInlineFormatting = (text: string) => {
  const chunks = text.split(/(\*\*.*?\*\*)/g);
  return chunks.map((chunk, index) => {
    if (chunk.startsWith('**') && chunk.endsWith('**')) {
      return (
        <Box component="strong" key={index} sx={{ fontWeight: 600 }}>
          {chunk.slice(2, -2)}
        </Box>
      );
    }
    return <React.Fragment key={index}>{chunk}</React.Fragment>;
  });
};

const renderMessageContent = (text: string, isDarkMode: boolean) => {
  const paragraphs = text
    .split(/\n\s*\n/)
    .map((p) => p.trim())
    .filter(Boolean);

  const safeParagraphs = paragraphs.length > 0 ? paragraphs : [text];

  return safeParagraphs.map((paragraph, pIdx) => {
    const lines = paragraph.split('\n');
    return (
      <Typography
        key={pIdx}
        variant="body2"
        sx={{
          lineHeight: 1.8,
          color: isDarkMode ? '#cbd5e1' : '#4a5568',
          fontSize: '0.97rem',
          whiteSpace: 'normal',
          '&:not(:last-of-type)': { mb: 1.25 },
        }}
      >
        {lines.map((line, lIdx) => (
          <React.Fragment key={lIdx}>
            {renderInlineFormatting(line)}
            {lIdx < lines.length - 1 && <br />}
          </React.Fragment>
        ))}
      </Typography>
    );
  });
};

function AiChatPanel({ messages, loading, isDarkMode, onSendMessage }: AiChatPanelProps) {
  const [input, setInput] = useState('');
  const [displayTexts, setDisplayTexts] = useState<Record<number, string>>({});
  const [animatingIdx, setAnimatingIdx] = useState<number>(-1);
  const [dots, setDots] = useState('');
  const animRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  // Animated dots while loading
  useEffect(() => {
    if (!loading) {
      setDots('');
      return;
    }
    const interval = setInterval(() => {
      setDots((d) => (d.length >= 3 ? '' : d + '.'));
    }, 380);
    return () => clearInterval(interval);
  }, [loading]);

  // Scroll to bottom on loading state change (shows thinking indicator)
  useEffect(() => {
    const timer = setTimeout(() => {
      window.scrollTo({ top: document.body.scrollHeight, behavior: 'smooth' });
    }, 80);
    return () => clearTimeout(timer);
  }, [loading]);

  // Chunk-reveal effect for new assistant messages
  useEffect(() => {
    // Complete any in-progress animation immediately when messages change
    if (animRef.current) {
      clearTimeout(animRef.current);
      animRef.current = null;
    }
    if (animatingIdx >= 0 && animatingIdx < messages.length) {
      setDisplayTexts((prev) => ({ ...prev, [animatingIdx]: messages[animatingIdx].content }));
      setAnimatingIdx(-1);
    }

    const lastIdx = messages.length - 1;
    if (lastIdx < 0) return;

    const lastMsg = messages[lastIdx];
    if (lastMsg.role !== 'assistant') return;

    const fullText = lastMsg.content;
    const words = fullText.split(' ');
    let wordIndex = 0;
    setAnimatingIdx(lastIdx);

    const tick = () => {
      const chunkSize = Math.floor(Math.random() * 16) + 12; // 12–27 words
      wordIndex = Math.min(wordIndex + chunkSize, words.length);
      setDisplayTexts((prev) => ({ ...prev, [lastIdx]: words.slice(0, wordIndex).join(' ') }));
      window.scrollTo({ top: document.body.scrollHeight });

      if (wordIndex < words.length) {
        const delay = Math.floor(Math.random() * 350) + 250; // 250–600ms
        animRef.current = setTimeout(tick, delay);
      } else {
        setAnimatingIdx(-1);
      }
    };

    animRef.current = setTimeout(tick, 0);
    return () => {
      if (animRef.current) clearTimeout(animRef.current);
    };
  }, [messages]);

  const handleSend = () => {
    const trimmed = input.trim();
    if (!trimmed) return;
    setInput('');
    onSendMessage(trimmed);
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  const isEmpty = messages.length === 0;

  return (
    <Fade in timeout={250}>
      <Paper
        elevation={0}
        sx={{
          borderRadius: 0,
          backgroundColor: 'transparent',
          border: 'none',
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        {/* Header */}
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            px: 0,
            pt: 2,
            pb: 1.5,
          }}
        >
          <Box display="flex" alignItems="center" gap={1.25}>
            <SparkleIcon sx={{ fontSize: 18, color: isDarkMode ? '#94a3b8' : '#64748b' }} />
            <Typography
              variant="subtitle1"
              sx={{ fontWeight: 500, color: isDarkMode ? '#e2e8f0' : '#2d3748' }}
            >
              Chat
            </Typography>
          </Box>
        </Box>

        {/* Message list */}
        <Box
          sx={{
            px: 0,
            py: 2,
            display: 'flex',
            flexDirection: 'column',
            gap: 2,
          }}
        >
          {/* Initial loading state */}
          {isEmpty && loading && (
            <Box display="flex" alignItems="center" gap={1.25} py={0.5}>
              <Typography
                sx={{
                  color: isDarkMode ? '#94a3b8' : '#64748b',
                  fontStyle: 'italic',
                  fontSize: '0.93rem',
                }}
              >
                Reflecting on your thoughts{dots}
              </Typography>
            </Box>
          )}

          {messages.map((msg, idx) => {
            if (msg.role === 'assistant') {
              const displayed = displayTexts[idx] ?? msg.content;
              const isCurrentlyAnimating = animatingIdx === idx;
              return (
                <Fade in timeout={300} key={idx}>
                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                    {renderMessageContent(displayed, isDarkMode)}
                    {isCurrentlyAnimating && (
                      <Box
                        component="span"
                        sx={{
                          display: 'inline-block',
                          width: '2px',
                          height: '1.1em',
                          backgroundColor: isDarkMode ? '#94a3b8' : '#64748b',
                          ml: 0.25,
                          verticalAlign: 'text-bottom',
                          animation: 'cursorBlink 0.9s step-end infinite',
                          '@keyframes cursorBlink': {
                            '0%, 100%': { opacity: 1 },
                            '50%': { opacity: 0 },
                          },
                        }}
                      />
                    )}
                  </Box>
                </Fade>
              );
            }
            return (
              <Fade in timeout={200} key={idx}>
                <Box sx={{ display: 'flex', justifyContent: 'flex-end' }}>
                  <Box
                    sx={{
                      maxWidth: '78%',
                      px: 1.75,
                      py: 1,
                      borderRadius: '16px 16px 4px 16px',
                      backgroundColor: isDarkMode ? 'rgba(102,126,234,0.18)' : 'rgba(102,126,234,0.12)',
                      border: isDarkMode
                        ? '1px solid rgba(102,126,234,0.25)'
                        : '1px solid rgba(102,126,234,0.2)',
                    }}
                  >
                    <Typography
                      variant="body2"
                      sx={{
                        fontSize: '0.97rem',
                        lineHeight: 1.7,
                        color: isDarkMode ? '#e2e8f0' : '#2d3748',
                        whiteSpace: 'pre-wrap',
                      }}
                    >
                      {msg.content}
                    </Typography>
                  </Box>
                </Box>
              </Fade>
            );
          })}

          {/* Follow-up loading indicator */}
          {loading && !isEmpty && (
            <Fade in timeout={300}>
              <Typography
                sx={{
                  color: isDarkMode ? '#94a3b8' : '#64748b',
                  fontStyle: 'italic',
                  fontSize: '0.87rem',
                }}
              >
                {dots || '.'}
              </Typography>
            </Fade>
          )}
        </Box>

        {/* Input area — sticky so it stays visible without manual scrolling */}
        <Box
          sx={{
            position: 'sticky',
            bottom: 0,
            borderTop: isDarkMode ? '1px solid rgba(255,255,255,0.08)' : '1px solid rgba(0,0,0,0.08)',
            backgroundColor: isDarkMode ? '#202120' : '#f1f5f9',
            px: 0,
            py: 1.25,
            display: 'flex',
            alignItems: 'flex-end',
            gap: 1,
          }}
        >
          <Box
            component="textarea"
            ref={textareaRef}
            value={input}
            onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Reply..."
            rows={1}
            sx={{
              flex: 1,
              resize: 'none',
              border: 'none',
              outline: 'none',
              background: 'transparent',
              fontFamily: 'inherit',
              fontSize: '0.95rem',
              lineHeight: 1.6,
              color: isDarkMode ? '#e2e8f0' : '#2d3748',
              '&::placeholder': {
                color: isDarkMode ? '#64748b' : '#94a3b8',
              },
              overflowY: 'auto',
              maxHeight: 96,
              py: 0.5,
            }}
          />
          <Tooltip title="Send (Enter)" placement="top">
            <span>
              <IconButton
                onClick={handleSend}
                disabled={!input.trim()}
                size="small"
                sx={{
                  color: !input.trim()
                    ? (isDarkMode ? '#4a5568' : '#cbd5e1')
                    : '#667eea',
                  transition: 'color 0.15s ease',
                  mb: 0.25,
                }}
              >
                <SendIcon sx={{ fontSize: 18 }} />
              </IconButton>
            </span>
          </Tooltip>
        </Box>
      </Paper>
    </Fade>
  );
}

export default AiChatPanel;
