import React from 'react';
import { Box, Button, CircularProgress, Fade, TextField, Typography } from '@mui/material';
import { Send as SendIcon } from '@mui/icons-material';

interface ThoughtComposerProps {
  headingText: string;
  thoughtPlaceholder: string;
  note: string;
  validationMessage: string;
  loading: boolean;
  isDarkMode: boolean;
  onChange: (value: string) => void;
  onSubmit: () => void;
}

function ThoughtComposer({
  headingText,
  thoughtPlaceholder,
  note,
  validationMessage,
  loading,
  isDarkMode,
  onChange,
  onSubmit,
}: ThoughtComposerProps) {
  return (
    <Fade in timeout={800}>
      <Box sx={{ mb: 4 }}>
        <Box display="flex" alignItems="center" justifyContent="center" mb={4.5}>
          <Typography
            variant="h4"
            sx={{
              fontWeight: 400,
              color: isDarkMode ? '#f1f5f9' : '#2d3748',
            }}
          >
            {headingText}
          </Typography>
        </Box>

        <Box display="flex" justifyContent="center">
          <Box sx={{ position: 'relative', width: '100%', maxWidth: '800px', mb: 3 }}>
            <TextField
              fullWidth
              multiline
              maxRows={6}
              variant="outlined"
              placeholder={thoughtPlaceholder}
              value={note}
              onChange={(event) => onChange(event.target.value)}
              onKeyDown={(event) => {
                if (event.key === 'Enter' && !event.shiftKey) {
                  event.preventDefault();
                  onSubmit();
                }
              }}
              sx={{
                '& .MuiOutlinedInput-root': {
                  borderRadius: '28px !important',
                  fontSize: '1.1rem',
                  lineHeight: 1.6,
                  paddingRight: '60px',
                  paddingLeft: '24px',
                  minHeight: '56px',
                  backgroundColor: isDarkMode ? '#313130' : 'transparent',
                  color: isDarkMode ? '#f1f5f9' : 'inherit',
                  '& fieldset': {
                    borderColor: isDarkMode ? '#404140' : 'rgba(0, 0, 0, 0.23)',
                    borderRadius: '28px !important',
                  },
                  '&:hover fieldset': {
                    borderColor: isDarkMode ? '#404140' : 'rgba(0, 0, 0, 0.23)',
                  },
                  '&.Mui-focused fieldset': {
                    borderColor: isDarkMode ? '#404140' : 'rgba(0, 0, 0, 0.23)',
                    borderWidth: '1px',
                  },
                },
                '& .MuiInputBase-input': {
                  color: isDarkMode ? '#f1f5f9' : 'inherit',
                  '&::placeholder': {
                    opacity: 0.7,
                    fontStyle: 'italic',
                    color: isDarkMode ? '#888888' : 'inherit',
                  },
                },
              }}
            />

            <Button
              variant="contained"
              onClick={onSubmit}
              disabled={loading || !note.trim()}
              sx={{
                position: 'absolute',
                right: '12px',
                bottom: '12px',
                minWidth: '36px',
                width: '36px',
                height: '36px',
                borderRadius: '48px',
                backgroundColor: '#667eea',
                padding: 0,
                '&:hover': {
                  backgroundColor: '#5a67d8',
                },
                '&:disabled': {
                  backgroundColor: '#e2e8f0',
                  color: '#a0aec0',
                },
                transition: 'all 0.2s ease',
              }}
            >
              {loading ? (
                <CircularProgress size={16} sx={{ color: '#93c5fd' }} />
              ) : (
                <SendIcon sx={{ fontSize: 16, transform: 'translateX(1px)' }} />
              )}
            </Button>
          </Box>
        </Box>

        <Box display="flex" justifyContent="center" mt={1} minHeight="32px">
          {validationMessage && (
            <Typography
              variant="body2"
              sx={{
                color: isDarkMode ? '#a0a0a0' : '#666666',
                fontSize: '0.875rem',
                textAlign: 'center',
                maxWidth: '800px',
                fontStyle: 'italic',
              }}
            >
              {validationMessage}
            </Typography>
          )}
        </Box>
      </Box>
    </Fade>
  );
}

export default ThoughtComposer;

