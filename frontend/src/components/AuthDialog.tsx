import React from 'react';
import { Box, Button, CircularProgress, Dialog, DialogContent, TextField, Typography } from '@mui/material';
import { AuthMode } from '../types';

interface AuthDialogProps {
  open: boolean;
  authMode: AuthMode;
  authPromptMessage: string;
  authError: string;
  authSubmitting: boolean;
  authForm: {
    identifier: string;
    username: string;
    email: string;
    password: string;
  };
  isDarkMode: boolean;
  pendingVerificationEmail: string;
  onClose: () => void;
  onExited: () => void;
  onSubmit: () => void;
  onChange: (field: 'identifier' | 'username' | 'email' | 'password', value: string) => void;
  onResendVerification: () => void;
}

function AuthDialog({
  open,
  authMode,
  authPromptMessage,
  authError,
  authSubmitting,
  authForm,
  isDarkMode,
  pendingVerificationEmail,
  onClose,
  onExited,
  onSubmit,
  onChange,
  onResendVerification,
}: AuthDialogProps) {

  const textFieldSx = {
    '& .MuiOutlinedInput-root': {
      borderRadius: '26px',
      backgroundColor: isDarkMode ? '#313130' : '#ffffff',
      color: isDarkMode ? '#f1f5f9' : '#2d3748',
      '& fieldset': {
        borderColor: isDarkMode ? '#404140' : '#e2e8f0',
      },
      '&:hover fieldset': {
        borderColor: isDarkMode ? '#5b5c5b' : '#cbd5e1',
      },
      '&.Mui-focused fieldset': {
        borderColor: isDarkMode ? '#7c828d' : '#cbd5e1',
        borderWidth: '1px',
      },
    },
    '& .MuiInputBase-input::placeholder': {
      color: isDarkMode ? '#94a3b8' : '#64748b',
      opacity: 1,
    },
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      TransitionProps={{
        onExited,
      }}
      fullWidth
      maxWidth="xs"
      disableScrollLock
      sx={{ '& .MuiDialog-paper': { maxWidth: 320 } }}
      slotProps={{ backdrop: { sx: { backdropFilter: 'blur(6px) brightness(0.45)', backgroundColor: 'rgba(0,0,0,0.55)' } } }}
      PaperProps={{
        elevation: 0,
        sx: {
          borderRadius: 5,
          backgroundColor: isDarkMode ? 'rgba(40,41,40,0.96)' : 'rgba(255,255,255,0.98)',
          border: isDarkMode ? '1px solid rgba(255,255,255,0.06)' : '1px solid rgba(15,23,42,0.06)',
          backdropFilter: 'blur(18px)',
          boxShadow: isDarkMode
            ? '0 24px 80px rgba(0,0,0,0.38)'
            : '0 24px 80px rgba(15,23,42,0.10)',
        },
      }}
    >
      <DialogContent sx={{ px: 3.25, pt: 3, pb: 3 }}>
        <Typography
          variant="h6"
          sx={{ fontWeight: 500, fontSize: '1.6rem', mb: 0.75 }}
        >
          <Box component="span" sx={{ color: isDarkMode ? '#f8fafc' : '#111827' }}>
            {authMode === 'verify-pending' ? 'Check your email' : authMode === 'login' ? 'Log in' : 'Sign up for free'}
          </Box>
        </Typography>

        <Typography
          variant="body2"
          sx={{ color: isDarkMode ? '#9ca3af' : '#6b7280', mb: 2, lineHeight: 1.6 }}
        >
          {authMode === 'verify-pending'
            ? <>We sent a verification link to <strong>{pendingVerificationEmail}</strong>. Click it to activate your account.</>
            : authMode === 'login'
              ? (authPromptMessage || 'Log in to save thoughts and get reflections.')
              : 'Create an account to save thoughts and get reflections.'}
        </Typography>

        {authMode === 'verify-pending' ? (
          <Box display="flex" flexDirection="column" gap={1.25}>
            {authError && (
              <Typography variant="body2" sx={{ color: isDarkMode ? '#fca5a5' : '#b91c1c', px: 0.25 }}>
                {authError}
              </Typography>
            )}
            <Typography variant="body2" sx={{ color: isDarkMode ? '#9ca3af' : '#6b7280' }}>
              Didn't receive it?
            </Typography>
            <Box display="flex" gap={1} mt={0.5}>
              <Button
                variant="text"
                onClick={onClose}
                sx={{
                  flex: 1,
                  borderRadius: 999,
                  textTransform: 'none',
                  color: isDarkMode ? '#d1d5db' : '#4b5563',
                  '&:hover': { backgroundColor: isDarkMode ? 'rgba(255,255,255,0.04)' : 'rgba(15,23,42,0.04)' },
                }}
              >
                Close
              </Button>
              <Button
                variant="contained"
                disableElevation
                onClick={onResendVerification}
                disabled={authSubmitting}
                sx={{
                  flex: 1,
                  minHeight: 42,
                  borderRadius: 999,
                  textTransform: 'none',
                  fontSize: '0.95rem',
                  color: isDarkMode ? '#111827' : '#f9fafb',
                  backgroundColor: isDarkMode ? '#f3f4f6' : '#111827',
                  '&:hover': { backgroundColor: isDarkMode ? '#e5e7eb' : '#1f2937' },
                }}
              >
                {authSubmitting ? <CircularProgress size={18} sx={{ color: isDarkMode ? '#111827' : '#f9fafb' }} /> : 'Resend email'}
              </Button>
            </Box>
          </Box>
        ) : (
          <Box display="flex" flexDirection="column" gap={1.25}>
            {authMode === 'register' && (
              <TextField
                fullWidth
                placeholder="Username"
                value={authForm.username}
                onChange={(event) => onChange('username', event.target.value)}
                sx={textFieldSx}
              />
            )}

            {authMode === 'register' && (
              <TextField
                fullWidth
                placeholder="Email"
                type="email"
                value={authForm.email}
                onChange={(event) => onChange('email', event.target.value)}
                sx={textFieldSx}
              />
            )}

            {authMode === 'login' && (
              <TextField
                fullWidth
                placeholder="Username or email"
                value={authForm.identifier}
                onChange={(event) => onChange('identifier', event.target.value)}
                sx={textFieldSx}
              />
            )}

            <TextField
              fullWidth
              placeholder="Password"
              type="password"
              value={authForm.password}
              onChange={(event) => onChange('password', event.target.value)}
              onKeyDown={(event) => {
                if (event.key === 'Enter') {
                  event.preventDefault();
                  onSubmit();
                }
              }}
              sx={textFieldSx}
            />

            {authError && (
              <Typography
                variant="body2"
                sx={{ color: isDarkMode ? '#fca5a5' : '#b91c1c', textAlign: 'left', px: 0.25 }}
              >
                {authError}
              </Typography>
            )}

            <Box display="flex" gap={1} mt={1.5}>
              <Button
                variant="text"
                onClick={onClose}
                sx={{
                  flex: 1,
                  borderRadius: 999,
                  textTransform: 'none',
                  color: isDarkMode ? '#d1d5db' : '#4b5563',
                  '&:hover': {
                    backgroundColor: isDarkMode ? 'rgba(255,255,255,0.04)' : 'rgba(15,23,42,0.04)',
                  },
                }}
              >
                Not now
              </Button>
              <Button
                variant="contained"
                disableElevation
                onClick={onSubmit}
                disabled={authSubmitting}
                sx={{
                  flex: 1,
                  minHeight: 42,
                  borderRadius: 999,
                  textTransform: 'none',
                  fontSize: '0.95rem',
                  color: isDarkMode ? '#111827' : '#f9fafb',
                  backgroundColor: isDarkMode ? '#f3f4f6' : '#111827',
                  '&:hover': {
                    backgroundColor: isDarkMode ? '#e5e7eb' : '#1f2937',
                  },
                }}
              >
                {authSubmitting ? (
                  <CircularProgress size={18} sx={{ color: isDarkMode ? '#111827' : '#f9fafb' }} />
                ) : authMode === 'login' ? 'Continue' : 'Create account'}
              </Button>
            </Box>
          </Box>
        )}
      </DialogContent>
    </Dialog>
  );
}

export default AuthDialog;

