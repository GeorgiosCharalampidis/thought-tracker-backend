import React, { useEffect, useRef, useState } from 'react';
import {
  Box,
  CircularProgress,
  Fade,
  IconButton,
  Paper,
  Tooltip,
  Typography,
} from '@mui/material';
import {
  AutoAwesome as SparkleIcon,
  Close as CloseIcon,
  Send as SendIcon,
} from '@mui/icons-material';
import { ChatMessage } from '../types';

interface AiChatPanelProps {
  messages: ChatMessage[];
  loading: boolean;
  isDarkMode: boolean;
  onSendMessage: (content: string) => void;
  onClose: () => void;
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

function AiChatPanel({ messages, loading, isDarkMode, onSendMessage, onClose }: AiChatPanelProps) {
  const [input, setInput] = useState('');
  const scrollContainerRef = useRef<HTMLDivElement>(null);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  // Scroll within the container only — never touches the page scroll
  useEffect(() => {
    const el = scrollContainerRef.current;
    if (el) {
      el.scrollTop = el.scrollHeight;
    }
  }, [messages, loading]);

  const handleSend = () => {
    const trimmed = input.trim();
    if (!trimmed || loading) return;
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
          borderRadius: 2,
          backgroundColor: isDarkMode ? 'rgba(255,255,255,0.04)' : 'rgba(0,0,0,0.02)',
          border: isDarkMode ? '1px solid rgba(255,255,255,0.08)' : '1px solid rgba(0,0,0,0.08)',
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
        }}
      >
        {/* Header */}
        <Box
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            px: 2.5,
            pt: 2,
            pb: 1.5,
            borderBottom: isDarkMode ? '1px solid rgba(255,255,255,0.06)' : '1px solid rgba(0,0,0,0.06)',
          }}
        >
          <Box display="flex" alignItems="center" gap={1.25}>
            <SparkleIcon sx={{ fontSize: 18, color: isDarkMode ? '#a0aec0' : '#718096' }} />
            <Typography
              variant="subtitle1"
              sx={{ fontWeight: 500, color: isDarkMode ? '#e2e8f0' : '#2d3748' }}
            >
              AI Chat
            </Typography>
          </Box>
          <Tooltip title="Close" placement="left">
            <IconButton
              onClick={onClose}
              size="small"
              sx={{
                color: isDarkMode ? '#94a3b8' : '#718096',
                '&:hover': { color: isDarkMode ? '#e2e8f0' : '#2d3748' },
              }}
            >
              <CloseIcon sx={{ fontSize: 18 }} />
            </IconButton>
          </Tooltip>
        </Box>

        {/* Message list — scrolls internally, never touches the page */}
        <Box
          ref={scrollContainerRef}
          sx={{
            overflowY: 'auto',
            overflowX: 'hidden',
            px: 2.5,
            py: 2,
            maxHeight: 480,
            display: 'flex',
            flexDirection: 'column',
            gap: 2,
            // Thin, subtle scrollbar that blends with the app style
            '&::-webkit-scrollbar': { width: '4px' },
            '&::-webkit-scrollbar-track': { background: 'transparent' },
            '&::-webkit-scrollbar-thumb': {
              background: isDarkMode ? 'rgba(255,255,255,0.12)' : 'rgba(0,0,0,0.12)',
              borderRadius: '4px',
            },
            '&::-webkit-scrollbar-thumb:hover': {
              background: isDarkMode ? 'rgba(255,255,255,0.22)' : 'rgba(0,0,0,0.22)',
            },
          }}
        >
          {/* Initial loading state — only shown before first message arrives */}
          {isEmpty && loading && (
            <Box display="flex" alignItems="center" gap={1.25} py={0.5}>
              <CircularProgress size={16} sx={{ color: '#667eea' }} />
              <Typography
                sx={{
                  color: isDarkMode ? '#a0aec0' : '#718096',
                  fontStyle: 'italic',
                  fontSize: '0.93rem',
                }}
              >
                Analyzing your thoughts...
              </Typography>
            </Box>
          )}

          {messages.map((msg, idx) => {
            if (msg.role === 'assistant') {
              return (
                <Fade in timeout={400} key={idx}>
                  <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
                    {renderMessageContent(msg.content, isDarkMode)}
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

          {/* Follow-up loading indicator — only shown after at least one message */}
          {loading && !isEmpty && (
            <Fade in timeout={300}>
              <Box display="flex" alignItems="center" gap={1}>
                <CircularProgress size={13} sx={{ color: '#667eea' }} />
                <Typography
                  sx={{
                    color: isDarkMode ? '#a0aec0' : '#718096',
                    fontStyle: 'italic',
                    fontSize: '0.87rem',
                  }}
                >
                  Thinking...
                </Typography>
              </Box>
            </Fade>
          )}
        </Box>

        {/* Input area */}
        <Box
          sx={{
            borderTop: isDarkMode ? '1px solid rgba(255,255,255,0.06)' : '1px solid rgba(0,0,0,0.06)',
            px: 2,
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
                color: isDarkMode ? '#4a5568' : '#a0aec0',
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
                disabled={!input.trim() || loading}
                size="small"
                sx={{
                  color: !input.trim() || loading
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
