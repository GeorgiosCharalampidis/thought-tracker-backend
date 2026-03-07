import React, { useEffect, useState } from 'react';
import {
  Container,
  Box,
  Typography,
  TextField,
  Button,
  Paper,
  Grid,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Fade,
  Slide,
  IconButton,
  Dialog,
  DialogContent,
  Tooltip,
} from '@mui/material';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import {
  Send as SendIcon,
  AutoAwesome as SparkleIcon,
  DarkMode as DarkModeIcon,
  LightMode as LightModeIcon,
  Logout as LogoutIcon,
  Menu as MenuIcon,
  MenuOpen as MenuOpenIcon,
  TextFields as TextFieldsIcon,
} from '@mui/icons-material';
import axios from 'axios';

axios.defaults.withCredentials = true;

type AuthMode = 'login' | 'register';
type PendingAction = 'save' | 'reflection' | null;

interface Note {
  id: number;
  content: string;
  date: string;
  category: string;
  subCategory?: string;
}

interface AuthUser {
  id: number;
  username: string;
  email: string;
  authority: string;
}

interface AuthResponse {
  message: string;
  user: AuthUser;
}

interface AiInsight {
  text: string;
  loading: boolean;
}

interface SimilarThoughtsResponse {
  categoryMessage: string;
  notes: Note[];
  inputAccepted: boolean;
  validationMessage?: string;
}

// Create theme based on dark mode
const createAppTheme = (isDarkMode: boolean) => createTheme({
  palette: {
    mode: isDarkMode ? 'dark' : 'light',
    background: {
      default: isDarkMode ? '#202120' : '#f1f5f9',
      paper: isDarkMode ? '#2d2e2d' : '#ffffff',
    },
    text: {
      primary: isDarkMode ? '#f1f5f9' : '#2d3748',
      secondary: isDarkMode ? '#94a3b8' : '#4a5568',
    },
    primary: {
      main: '#667eea',
    },
  },
});

const getErrorMessage = (error: unknown, fallback: string) => {
  if (axios.isAxiosError(error)) {
    const responseMessage = error.response?.data;
    if (typeof responseMessage === 'string' && responseMessage.trim()) {
      return responseMessage;
    }
    const nestedMessage = error.response?.data?.message;
    if (typeof nestedMessage === 'string' && nestedMessage.trim()) {
      return nestedMessage;
    }
  }
  return fallback;
};

const renderInlineFormatting = (text: string) => {
  const chunks = text.split(/(\*\*.*?\*\*)/g);

  return chunks.map((chunk, index) => {
    if (chunk.startsWith('**') && chunk.endsWith('**')) {
      return <Box component="strong" key={`${chunk}-${index}`} sx={{ fontWeight: 600 }}>{chunk.slice(2, -2)}</Box>;
    }

    return <React.Fragment key={`${chunk}-${index}`}>{chunk}</React.Fragment>;
  });
};

const renderInsightContent = (text: string, isDarkMode: boolean) => {
  const paragraphs = text
    .split(/\n\s*\n/)
    .map((paragraph) => paragraph.trim())
    .filter(Boolean);

  const safeParagraphs = paragraphs.length > 0 ? paragraphs : [text];

  return safeParagraphs.map((paragraph, paragraphIndex) => {
    const lines = paragraph.split('\n');

    return (
      <Typography
        key={`paragraph-${paragraphIndex}`}
        variant="body2"
        sx={{
          lineHeight: 1.8,
          color: isDarkMode ? '#cbd5e1' : '#4a5568',
          fontSize: '0.98rem',
          whiteSpace: 'normal',
          '& strong': {
            color: isDarkMode ? '#f8fafc' : '#1f2937',
          },
          '&:not(:last-of-type)': {
            mb: 1.5,
          },
        }}
      >
        {lines.map((line, lineIndex) => (
          <React.Fragment key={`line-${paragraphIndex}-${lineIndex}`}>
            {renderInlineFormatting(line)}
            {lineIndex < lines.length - 1 && <br />}
          </React.Fragment>
        ))}
      </Typography>
    );
  });
};

function App() {
  // Main screen state for the thought composer and response panels.
  const [note, setNote] = useState('');
  const [similarThoughts, setSimilarThoughts] = useState<Note[]>([]);
  const [categoryMessage, setCategoryMessage] = useState<string>('');
  const [validationMessage, setValidationMessage] = useState<string>('');
  const [showSimilarThoughts, setShowSimilarThoughts] = useState(false);
  const [isDarkMode, setIsDarkMode] = useState(() => {
    if (typeof window !== 'undefined') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches;
    }
    return false;
  });
  const [aiInsight, setAiInsight] = useState<AiInsight>({ text: '', loading: false });
  const [loading, setLoading] = useState(false);
  const [authMode, setAuthMode] = useState<AuthMode>('login');
  const [currentUser, setCurrentUser] = useState<AuthUser | null>(null);
  const [authLoading, setAuthLoading] = useState(true);
  const [authPromptOpen, setAuthPromptOpen] = useState(false);
  const [pendingAction, setPendingAction] = useState<PendingAction>(null);
  const [authPromptMessage, setAuthPromptMessage] = useState('');
  const [authSubmitting, setAuthSubmitting] = useState(false);
  const [authError, setAuthError] = useState('');
  const [savedNotes, setSavedNotes] = useState<Note[]>([]);
  const [notesLoading, setNotesLoading] = useState(false);
  const [notesError, setNotesError] = useState('');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [isAltHeading, setIsAltHeading] = useState(false);
  const [authForm, setAuthForm] = useState({
    identifier: '',
    username: '',
    email: '',
    password: '',
  });

  useEffect(() => {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    const handleChange = (e: MediaQueryListEvent) => setIsDarkMode(e.matches);

    mediaQuery.addEventListener('change', handleChange);
    return () => mediaQuery.removeEventListener('change', handleChange);
  }, []);

  const openAuthPrompt = (mode: AuthMode, action: PendingAction, message: string) => {
    setAuthMode(mode);
    setPendingAction(action);
    setAuthPromptMessage(message);
    setAuthError('');
    setAuthPromptOpen(true);
  };

  const closeAuthPrompt = () => {
    setAuthPromptOpen(false);
    setPendingAction(null);
    setAuthPromptMessage('');
    setAuthError('');
  };

  const resetJournalState = () => {
    setSimilarThoughts([]);
    setCategoryMessage('');
    setValidationMessage('');
    setShowSimilarThoughts(false);
    setAiInsight({ text: '', loading: false });
  };

  const applySimilarThoughtsResponse = (response: SimilarThoughtsResponse) => {
    if (response.inputAccepted) {
      setSimilarThoughts(response.notes);
      setCategoryMessage(response.categoryMessage);
      setValidationMessage('');
      setNote('');
      setShowSimilarThoughts(response.notes.length > 0);
      return;
    }

    setValidationMessage(response.validationMessage || 'Please try writing something more meaningful.');
  };

  const loadNotes = async (userId: number) => {
    setNotesLoading(true);
    setNotesError('');

    try {
      const response = await axios.get<Note[]>(`/api/notes/${userId}`);
      const sortedNotes = [...response.data].sort((first, second) => {
        const dateDelta = new Date(second.date).getTime() - new Date(first.date).getTime();
        return dateDelta !== 0 ? dateDelta : second.id - first.id;
      });
      setSavedNotes(sortedNotes);
    } catch (error) {
      console.error('Error loading notes:', error);
      setSavedNotes([]);
      if (axios.isAxiosError(error) && error.response?.status === 401) {
        setCurrentUser(null);
      } else {
        setNotesError(getErrorMessage(error, 'Could not load saved thoughts.'));
      }
    } finally {
      setNotesLoading(false);
    }
  };

  useEffect(() => {
    const bootstrapAuth = async () => {
      try {
        const response = await axios.get<AuthUser>('/api/auth/me');
        setCurrentUser(response.data);
      } catch {
        setCurrentUser(null);
      } finally {
        setAuthLoading(false);
      }
    };

    bootstrapAuth();
  }, []);

  useEffect(() => {
    if (currentUser) {
      void loadNotes(currentUser.id);
      return;
    }

    setSavedNotes([]);
    setNotesError('');
    setNotesLoading(false);
    resetJournalState();
  }, [currentUser]);

  const submitNoteForUser = async (user: AuthUser) => {
    setLoading(true);
    try {
      const response = await axios.post<SimilarThoughtsResponse>(`/api/notes/${user.id}`, {
        content: note,
      });

      applySimilarThoughtsResponse(response.data);
      await loadNotes(user.id);
    } catch (error) {
      console.error('Error creating note:', error);
      if (axios.isAxiosError(error) && error.response?.status === 401) {
        openAuthPrompt('login', 'save', 'Log in to save this thought and keep it in your journal.');
      } else {
        setValidationMessage(getErrorMessage(error, 'Could not save your thought right now.'));
      }
    } finally {
      setLoading(false);
    }
  };

  const previewThought = async () => {
    setLoading(true);
    try {
      const response = await axios.post<SimilarThoughtsResponse>('/api/notes/preview', {
        content: note,
      });

      applySimilarThoughtsResponse(response.data);
    } catch (error) {
      console.error('Error previewing note:', error);
      setValidationMessage(getErrorMessage(error, 'Could not analyze this thought right now.'));
    } finally {
      setLoading(false);
    }
  };

  const fetchAiReflectionForUser = async (user: AuthUser) => {
    try {
      setAiInsight({ text: '', loading: true });
      const response = await axios.get<string>(`/api/notes/${user.id}/summary`);
      setAiInsight({ text: response.data, loading: false });
    } catch (error) {
      console.error('Error fetching AI reflection:', error);
      if (axios.isAxiosError(error) && error.response?.status === 401) {
        setAiInsight({ text: '', loading: false });
        openAuthPrompt('login', 'reflection', 'Log in to get an AI reflection on your journal.');
      } else {
        setAiInsight({ text: getErrorMessage(error, 'AI service is currently unavailable. Please try again later.'), loading: false });
      }
    }
  };

  const handleSubmit = async () => {
    if (!note.trim()) return;

    if (!currentUser) {
      await previewThought();
      return;
    }

    await submitNoteForUser(currentUser);
  };

  const fetchAiReflection = async () => {
    if (!currentUser) {
      openAuthPrompt('login', 'reflection', 'Log in to get an AI reflection on your saved thoughts.');
      return;
    }

    await fetchAiReflectionForUser(currentUser);
  };

  const handleAuthInputChange = (field: keyof typeof authForm) => (event: React.ChangeEvent<HTMLInputElement>) => {
    setAuthForm((previous) => ({
      ...previous,
      [field]: event.target.value,
    }));

    if (authError) {
      setAuthError('');
    }
  };

  const handleAuthSubmit = async () => {
    setAuthSubmitting(true);
    setAuthError('');

    try {
      const endpoint = authMode === 'login' ? '/api/auth/login' : '/api/auth/register';
      const payload = authMode === 'login'
        ? {
            identifier: authForm.identifier,
            password: authForm.password,
          }
        : {
            username: authForm.username,
            email: authForm.email,
            password: authForm.password,
          };

      const response = await axios.post<AuthResponse>(endpoint, payload);
      const actionToResume = pendingAction;
      const authenticatedUser = response.data.user;

      setCurrentUser(authenticatedUser);
      setAuthForm({ identifier: '', username: '', email: '', password: '' });
      setAuthPromptOpen(false);
      setPendingAction(null);
      setAuthPromptMessage('');
      setAuthError('');

      if (actionToResume === 'save') {
        await submitNoteForUser(authenticatedUser);
      } else if (actionToResume === 'reflection') {
        await fetchAiReflectionForUser(authenticatedUser);
      }
    } catch (error) {
      console.error(`Error during ${authMode}:`, error);
      setAuthError(getErrorMessage(error, authMode === 'login' ? 'Login failed.' : 'Registration failed.'));
    } finally {
      setAuthSubmitting(false);
    }
  };

  const getAuthPromptTitle = () => authMode === 'login' ? 'Log in' : 'Sign up for free';

  const getAuthPromptFallbackMessage = () => authMode === 'login'
    ? 'Log in to save thoughts and get reflections.'
    : 'Create an account to save thoughts and get reflections.';

  const handleLogout = async () => {
    try {
      await axios.post('/api/auth/logout');
    } catch (error) {
      console.error('Error logging out:', error);
    } finally {
      setCurrentUser(null);
      setAuthError('');
      setSavedNotes([]);
      setNotesError('');
      setAuthForm({ identifier: '', username: '', email: '', password: '' });
      closeAuthPrompt();
      resetJournalState();
      setNote('');
    }
  };

  const handleShareAnotherThought = () => {
    resetJournalState();
    setNote('');
  };

  const handlePrimaryLeftAction = () => {
    if (!isSidebarOpen) {
      setIsSidebarOpen(true);
      return;
    }

    setIsAltHeading((previous) => !previous);
  };

  const theme = createAppTheme(isDarkMode);
  const sidebarWidth = 284;
  const mainContentOffset = isSidebarOpen ? 316 : 96;

  const headingText = isAltHeading ? 'Pause and notice' : 'Share a thought';

  const thoughtPlaceholder = currentUser
    ? 'Write a thought...'
    : 'Write a thought... sign in to save it.';

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

  const formatHistoryDate = (value: string) => {
    const parsed = new Date(value);
    if (Number.isNaN(parsed.getTime())) {
      return value;
    }
    return parsed.toLocaleDateString(undefined, {
      month: 'short',
      day: 'numeric',
    });
  };

  if (authLoading) {
    return (
      <ThemeProvider theme={theme}>
        {/* Full-screen loading state while the app checks the current session. */}
        <Box
          sx={{
            minHeight: '100vh',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            backgroundColor: isDarkMode ? '#202120' : '#f1f5f9',
          }}
        >
          <CircularProgress size={28} sx={{ color: '#667eea' }} />
        </Box>
      </ThemeProvider>
    );
  }

  return (
    <ThemeProvider theme={theme}>
      {/* Page background and top-level layout wrapper for the app. */}
      <Box sx={{
        flexGrow: 1,
        minHeight: '100vh',
        backgroundColor: isDarkMode ? '#202120' : '#f1f5f9',
        paddingTop: showSimilarThoughts ? '12vh' : '28vh',
      }}>
        {/* Left floating action rail: sidebar toggle and AI reflection shortcut. */}
        <Box
          sx={{
            position: 'fixed',
            top: 18,
            left: 16,
            zIndex: 1100,
            display: 'flex',
            flexDirection: 'column',
            gap: 1,
            pointerEvents: 'none',
          }}
        >
          <Tooltip title={isSidebarOpen ? 'Change heading' : 'Open sidebar'} placement="right">
            <IconButton
              onClick={handlePrimaryLeftAction}
              sx={{
                width: 40,
                height: 40,
                pointerEvents: 'auto',
                color: isDarkMode ? '#cbd5e1' : '#475569',
                backgroundColor: isDarkMode ? '#202120' : '#f1f5f9',
                border: isDarkMode ? '1px solid rgba(255,255,255,0.08)' : '1px solid rgba(15,23,42,0.08)',
                '&:hover': {
                  backgroundColor: isDarkMode ? '#262726' : '#eef2f7',
                },
              }}
            >
              {isSidebarOpen ? <TextFieldsIcon sx={{ fontSize: 18 }} /> : <MenuIcon sx={{ fontSize: 18 }} />}
            </IconButton>
          </Tooltip>

          <Tooltip
            title="Reflect on your thoughts"
            placement="right"
            disableHoverListener={isSidebarOpen}
            disableFocusListener={isSidebarOpen}
            disableTouchListener={isSidebarOpen}
          >
            <Box
              onClick={fetchAiReflection}
              sx={{
                display: 'flex',
                alignItems: 'center',
                gap: 1,
                width: 'fit-content',
                height: 40,
                pointerEvents: 'auto',
                pl: 0,
                pr: isSidebarOpen ? 0.9 : 0,
                py: 0,
                borderRadius: 999,
                cursor: 'pointer',
                transition: 'background-color 0.2s ease, color 0.2s ease',
                backgroundColor: 'transparent',
                '&:hover': {
                  backgroundColor: isSidebarOpen
                    ? (isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(15,23,42,0.08)')
                    : 'transparent',
                },
              }}
            >
              <IconButton
                onClick={fetchAiReflection}
                sx={{
                  width: 40,
                  height: 40,
                  color: isDarkMode ? '#cbd5e1' : '#475569',
                  backgroundColor: isDarkMode ? '#202120' : '#f1f5f9',
                  border: isDarkMode ? '1px solid rgba(255,255,255,0.08)' : '1px solid rgba(15,23,42,0.08)',
                  '&:hover': {
                    backgroundColor: isDarkMode ? '#262726' : '#eef2f7',
                  },
                }}
              >
                <SparkleIcon sx={{ fontSize: 18 }} />
              </IconButton>

              {isSidebarOpen && (
                <Typography
                  sx={{
                    pr: 0.15,
                    fontSize: '0.86rem',
                    color: isDarkMode ? '#cbd5e1' : '#475569',
                    whiteSpace: 'nowrap',
                    lineHeight: 1,
                  }}
                >
                  Reflect on your thoughts
                </Typography>
              )}
            </Box>
          </Tooltip>
        </Box>

        {/* Slide-out sidebar that shows the saved thought history. */}
        <Box
          sx={{
            position: 'fixed',
            top: 0,
            left: 0,
            bottom: 0,
            width: sidebarWidth,
            zIndex: 1000,
            transform: isSidebarOpen ? 'translateX(0)' : `translateX(-${sidebarWidth}px)`,
            transition: 'transform 0.22s ease',
            overflow: 'hidden',
          }}
        >
          <Paper
            elevation={0}
            sx={{
              position: 'relative',
              height: '100%',
              borderRadius: 0,
              backgroundColor: isSidebarOpen
                ? (isDarkMode ? '#2c2d2c' : '#edf1f6')
                : (isDarkMode ? '#202120' : '#f1f5f9'),
              borderRight: isDarkMode ? '1px solid rgba(255,255,255,0.08)' : '1px solid rgba(15,23,42,0.08)',
              overflow: 'hidden',
              display: 'flex',
              flexDirection: 'column',
            }}
          >
            <Tooltip title="Close sidebar" placement="left">
              <IconButton
                onClick={() => setIsSidebarOpen(false)}
                sx={{
                  position: 'absolute',
                  top: 18,
                  right: 12,
                  width: 36,
                  height: 36,
                  zIndex: 2,
                  color: isDarkMode ? '#cbd5e1' : '#475569',
                  backgroundColor: isDarkMode
                    ? '#2c2d2c'
                    : '#edf1f6',
                  border: isDarkMode ? '1px solid rgba(255,255,255,0.08)' : '1px solid rgba(15,23,42,0.08)',
                  '&:hover': {
                    backgroundColor: isDarkMode ? '#363736' : '#e3e8ef',
                  },
                }}
              >
                <MenuOpenIcon sx={{ fontSize: 18 }} />
              </IconButton>
            </Tooltip>

            <Box
              sx={{
                pt: '136px',
                px: 1.1,
                pb: 1.1,
                overflowY: 'auto',
                display: 'flex',
                flexDirection: 'column',
                gap: 0.8,
                scrollbarWidth: 'thin',
                scrollbarColor: isDarkMode ? '#4b5563 #202120' : '#cbd5e1 #edf1f6',
                '&::-webkit-scrollbar': {
                  width: '10px',
                },
                '&::-webkit-scrollbar-track': {
                  backgroundColor: isDarkMode ? '#202120' : '#edf1f6',
                },
                '&::-webkit-scrollbar-thumb': {
                  backgroundColor: isDarkMode ? '#4b5563' : '#cbd5e1',
                  borderRadius: '999px',
                  border: `2px solid ${isDarkMode ? '#202120' : '#edf1f6'}`,
                },
              }}
            >
              {currentUser && (
                <Typography
                  sx={{
                    px: 1.1,
                    pt: 0,
                    pb: 0.15,
                    fontSize: '0.88rem',
                    color: isDarkMode ? '#cbd5e1' : '#475569',
                  }}
                >
                  Your thoughts
                </Typography>
              )}

              {!currentUser ? (
                <Box sx={{ px: 1.1, py: 0.2 }}>
                  <Typography sx={{ fontSize: '0.86rem', color: isDarkMode ? '#cbd5e1' : '#475569', lineHeight: 1.6 }}>
                    Log in to browse your saved thoughts here.
                  </Typography>
                </Box>
              ) : notesLoading ? (
                <Box display="flex" justifyContent="center" py={2}>
                  <CircularProgress size={20} sx={{ color: isDarkMode ? '#cbd5e1' : '#667eea' }} />
                </Box>
              ) : notesError ? (
                <Typography sx={{ px: 1.1, py: 1.2, fontSize: '0.82rem', color: isDarkMode ? '#fca5a5' : '#b91c1c' }}>
                  {notesError}
                </Typography>
              ) : savedNotes.length === 0 ? (
                <Typography sx={{ px: 1.1, py: 1.2, fontSize: '0.84rem', color: isDarkMode ? '#94a3b8' : '#64748b' }}>
                  No saved thoughts yet.
                </Typography>
              ) : (
                savedNotes.map((savedNote) => (
                  <Button
                    key={savedNote.id}
                    variant="text"
                    onClick={() => {
                      setNote(savedNote.content);
                      setValidationMessage('');
                      setShowSimilarThoughts(false);
                    }}
                    sx={{
                      justifyContent: 'flex-start',
                      alignItems: 'flex-start',
                      px: 1.1,
                      py: 1,
                      borderRadius: 2.5,
                      textTransform: 'none',
                      backgroundColor: isDarkMode ? 'rgba(255,255,255,0.02)' : 'rgba(255,255,255,0.55)',
                      border: isDarkMode ? '1px solid rgba(255,255,255,0.05)' : '1px solid rgba(15,23,42,0.05)',
                      '&:hover': {
                        backgroundColor: isDarkMode ? 'rgba(255,255,255,0.05)' : 'rgba(255,255,255,0.75)',
                      },
                    }}
                  >
                    <Box sx={{ width: '100%', textAlign: 'left' }}>
                      <Box display="flex" justifyContent="space-between" alignItems="center" mb={0.6}>
                        <Typography sx={{ fontSize: '0.73rem', color: isDarkMode ? '#94a3b8' : '#64748b' }}>
                          {formatHistoryDate(savedNote.date)}
                        </Typography>
                        <Typography sx={{ fontSize: '0.73rem', color: isDarkMode ? '#a8b0bd' : '#64748b' }}>
                          {savedNote.category}
                        </Typography>
                      </Box>
                      <Typography
                        sx={{
                          fontSize: '0.87rem',
                          lineHeight: 1.45,
                          color: isDarkMode ? '#e5e7eb' : '#1f2937',
                          display: '-webkit-box',
                          WebkitLineClamp: 3,
                          WebkitBoxOrient: 'vertical',
                          overflow: 'hidden',
                        }}
                      >
                        {savedNote.content}
                      </Typography>
                    </Box>
                  </Button>
                ))
              )}
            </Box>
          </Paper>
        </Box>

        {/* Top-right account actions and dark mode toggle. */}
        <Box
          sx={{
            position: 'fixed',
            top: 24,
            right: 24,
            zIndex: 1000,
            display: 'flex',
            alignItems: 'center',
            gap: 1,
          }}
        >
          {currentUser ? (
            <Button
              variant="text"
              onClick={handleLogout}
              startIcon={<LogoutIcon sx={{ fontSize: 17 }} />}
              sx={{
                borderRadius: 999,
                px: 1.25,
                py: 0.65,
                textTransform: 'none',
                fontSize: '0.92rem',
                color: isDarkMode ? '#cbd5e1' : '#475569',
                backgroundColor: isDarkMode ? 'rgba(255,255,255,0.04)' : 'rgba(255,255,255,0.72)',
                border: isDarkMode ? '1px solid rgba(255,255,255,0.08)' : '1px solid rgba(0,0,0,0.06)',
                '&:hover': {
                  backgroundColor: isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(255,255,255,0.95)',
                },
              }}
            >
              Log out
            </Button>
          ) : (
            <>
              <Button
                variant="text"
                onClick={() => openAuthPrompt('login', null, 'Log in to save thoughts and unlock AI reflections.')}
                sx={{
                  borderRadius: 999,
                  px: 1.25,
                  py: 0.65,
                  textTransform: 'none',
                  fontSize: '0.92rem',
                  color: '#111827',
                  backgroundColor: '#ffffff',
                  border: '1px solid rgba(15,23,42,0.08)',
                  '&:hover': {
                    backgroundColor: '#f8fafc',
                  },
                }}
              >
                Log in
              </Button>
              <Button
                variant="text"
                onClick={() => openAuthPrompt('register', null, 'Create an account to save thoughts and unlock AI reflections.')}
                sx={{
                  borderRadius: 999,
                  px: 1.4,
                  py: 0.65,
                  textTransform: 'none',
                  fontSize: '0.92rem',
                  color: isDarkMode ? '#f3f4f6' : '#111827',
                  backgroundColor: isDarkMode ? 'rgba(255,255,255,0.08)' : '#ffffff',
                  border: isDarkMode ? '1px solid rgba(255,255,255,0.10)' : '1px solid rgba(15,23,42,0.08)',
                  '&:hover': {
                    backgroundColor: isDarkMode ? 'rgba(255,255,255,0.12)' : '#f8fafc',
                  },
                }}
              >
                Sign up for free
              </Button>
            </>
          )}

          <IconButton
            onClick={() => setIsDarkMode(!isDarkMode)}
            sx={{
              backgroundColor: isDarkMode ? '#2d2e2d' : '#ffffff',
              color: isDarkMode ? '#f1f5f9' : '#2d3748',
              border: isDarkMode ? '1px solid #404140' : '1px solid #e2e8f0',
              '&:hover': {
                backgroundColor: isDarkMode ? '#404140' : '#f7fafc',
              },
              transition: 'all 0.2s ease',
            }}
          >
            {isDarkMode ? <LightModeIcon /> : <DarkModeIcon />}
          </IconButton>
        </Box>

        {/* Authentication dialog used for login and sign-up flows. */}
        <Dialog
          open={authPromptOpen}
          onClose={closeAuthPrompt}
          fullWidth
          maxWidth="xs"
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
          <DialogContent sx={{ p: 3.25 }}>
            <Typography
              variant="h6"
              sx={{
                fontWeight: 400,
                color: isDarkMode ? '#f8fafc' : '#111827',
                mb: 0.75,
              }}
            >
              {getAuthPromptTitle()}
            </Typography>
            <Typography
              variant="body2"
              sx={{
                color: isDarkMode ? '#9ca3af' : '#6b7280',
                mb: 2,
                lineHeight: 1.6,
              }}
            >
              {authPromptMessage || getAuthPromptFallbackMessage()}
            </Typography>

            <Box display="flex" flexDirection="column" gap={1.25}>
              {authMode === 'register' && (
                <TextField
                  fullWidth
                  placeholder="Username"
                  value={authForm.username}
                  onChange={handleAuthInputChange('username')}
                  sx={textFieldSx}
                />
              )}

              {authMode === 'register' && (
                <TextField
                  fullWidth
                  placeholder="Email"
                  type="email"
                  value={authForm.email}
                  onChange={handleAuthInputChange('email')}
                  sx={textFieldSx}
                />
              )}

              {authMode === 'login' && (
                <TextField
                  fullWidth
                  placeholder="Username or email"
                  value={authForm.identifier}
                  onChange={handleAuthInputChange('identifier')}
                  sx={textFieldSx}
                />
              )}

              <TextField
                fullWidth
                placeholder="Password"
                type="password"
                value={authForm.password}
                onChange={handleAuthInputChange('password')}
                onKeyDown={(event) => {
                  if (event.key === 'Enter') {
                    event.preventDefault();
                    handleAuthSubmit();
                  }
                }}
                sx={textFieldSx}
              />

              {authError && (
                <Typography
                  variant="body2"
                  sx={{
                    color: isDarkMode ? '#fca5a5' : '#b91c1c',
                    textAlign: 'left',
                    px: 0.25,
                  }}
                >
                  {authError}
                </Typography>
              )}

              <Box display="flex" gap={1} mt={1}>
                <Button
                  variant="text"
                  onClick={closeAuthPrompt}
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
                  onClick={handleAuthSubmit}
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
          </DialogContent>
        </Dialog>

        {/* Main content area that shifts right when the sidebar is open. */}
        <Box
          sx={{
            ml: `${mainContentOffset}px`,
            mr: 2,
            transition: 'margin-left 0.22s ease',
          }}
        >
          <Container maxWidth="lg">
            <Grid container spacing={2}>
              {!showSimilarThoughts && (
                <Grid item xs={12}>
                  <Fade in timeout={800}>
                    <Box sx={{ mb: 4 }}>
                      {/* Main heading above the thought input. */}
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
                        {/* Thought composer: text box plus the send button. */}
                        <Box sx={{ position: 'relative', width: '100%', maxWidth: '800px', mb: 3 }}>
                          <TextField
                            fullWidth
                            multiline
                            maxRows={6}
                            variant="outlined"
                            placeholder={thoughtPlaceholder}
                            value={note}
                            onChange={(e) => {
                              setNote(e.target.value);
                              if (validationMessage) {
                                setValidationMessage('');
                              }
                            }}
                            onKeyDown={(e) => {
                              if (e.key === 'Enter' && !e.shiftKey) {
                                e.preventDefault();
                                handleSubmit();
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

                          {/* Submit button for the current thought. */}
                          <Button
                            variant="contained"
                            onClick={handleSubmit}
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

                      {/* Validation or input feedback message under the composer. */}
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
                </Grid>
              )}

              {/* Loading card shown while the AI reflection is being generated. */}
              {aiInsight.loading && (
                <Grid item xs={12}>
                  <Fade in timeout={600}>
                    <Paper
                      elevation={0}
                      sx={{
                        p: 2.5,
                        borderRadius: 2,
                        backgroundColor: isDarkMode ? 'rgba(255,255,255,0.04)' : 'rgba(0,0,0,0.02)',
                        border: isDarkMode ? '1px solid rgba(255,255,255,0.08)' : '1px solid rgba(0,0,0,0.08)',
                      }}
                    >
                      <Box display="flex" alignItems="center" gap={1.25}>
                        <SparkleIcon sx={{ fontSize: 18, color: isDarkMode ? '#a0aec0' : '#718096' }} />
                        <Typography variant="subtitle1" sx={{ fontWeight: 500, color: isDarkMode ? '#e2e8f0' : '#4a5568' }}>
                          AI Reflection
                        </Typography>
                      </Box>
                      <Box display="flex" alignItems="center" gap={1.25} mt={1.5}>
                        <CircularProgress size={18} sx={{ color: '#667eea' }} />
                        <Typography sx={{ color: isDarkMode ? '#a0aec0' : '#718096', fontStyle: 'italic', fontSize: '0.95rem' }}>
                          Analyzing your thoughts...
                        </Typography>
                      </Box>
                    </Paper>
                  </Fade>
                </Grid>
              )}

              {/* Final AI reflection panel shown after loading completes. */}
              {aiInsight.text && !aiInsight.loading && (
                <Grid item xs={12}>
                  <Slide direction="up" in timeout={800}>
                    <Paper
                      elevation={0}
                      sx={{
                        p: 2.5,
                        borderRadius: 2,
                        backgroundColor: isDarkMode ? 'rgba(255,255,255,0.04)' : 'rgba(0,0,0,0.02)',
                        border: isDarkMode ? '1px solid rgba(255,255,255,0.08)' : '1px solid rgba(0,0,0,0.08)',
                      }}
                    >
                      <Box display="flex" alignItems="center" gap={1.25} mb={1.5}>
                        <SparkleIcon sx={{ fontSize: 18, color: isDarkMode ? '#a0aec0' : '#718096' }} />
                        <Typography variant="subtitle1" sx={{ fontWeight: 500, color: isDarkMode ? '#e2e8f0' : '#2d3748' }}>
                          AI Reflection
                        </Typography>
                      </Box>
                      {renderInsightContent(aiInsight.text, isDarkMode)}
                    </Paper>
                  </Slide>
                </Grid>
              )}

              {/* Similar thoughts results shown after a successful submission/preview. */}
              {showSimilarThoughts && (
                <Grid item xs={12}>
                  <Slide direction="up" in timeout={1000}>
                    <Box sx={{ pb: 3 }}>
                      <Box display="flex" flexDirection="column" alignItems="center" width="100%">
                        <Button
                          variant="text"
                          onClick={handleShareAnotherThought}
                          sx={{
                            mb: 2.5,
                            borderRadius: 999,
                            px: 1.25,
                            py: 0.5,
                            textTransform: 'none',
                            color: isDarkMode ? '#cbd5e1' : '#475569',
                            border: '1px solid',
                            borderColor: isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(15,23,42,0.08)',
                            '&:hover': {
                              backgroundColor: isDarkMode ? 'rgba(255,255,255,0.04)' : 'rgba(15,23,42,0.03)',
                            },
                          }}
                        >
                          Share another thought
                        </Button>

                        <Typography
                          variant="h5"
                          sx={{
                            fontWeight: 400,
                            color: isDarkMode ? '#f1f5f9' : '#2d3748',
                            mb: 3,
                            textAlign: 'center',
                          }}
                        >
                          {categoryMessage || "You're not alone.."}
                        </Typography>

                        <Box display="flex" flexWrap="wrap" gap={3} justifyContent="center" alignItems="flex-start">
                          {similarThoughts.map((noteItem, index) => (
                            <Fade in timeout={600 + index * 200} key={noteItem.id}>
                              <Card
                                elevation={1}
                                sx={{
                                  minWidth: '200px',
                                  maxWidth: '400px',
                                  width: 'fit-content',
                                  borderRadius: 2,
                                  backgroundColor: isDarkMode ? '#2d2e2d' : '#ffffff',
                                  border: isDarkMode ? '1px solid #404140' : '1px solid #e2e8f0',
                                  userSelect: 'text',
                                  cursor: 'default',
                                }}
                              >
                                <CardContent sx={{ p: 3 }}>
                                  <Box display="flex" justifyContent="flex-start" alignItems="flex-start" mb={2}>
                                    <Box display="flex" flexDirection="row" gap={1} alignItems="center" flexWrap="wrap" sx={{ marginLeft: '-6px' }}>
                                      <Chip
                                        label={noteItem.category}
                                        size="small"
                                        sx={{
                                          backgroundColor: isDarkMode ? '#f3f4f6' : '#111827',
                                          color: isDarkMode ? '#111827' : '#f9fafb',
                                          fontWeight: 500,
                                          '& .MuiChip-label': {
                                            px: 1.4,
                                          },
                                        }}
                                      />
                                      {noteItem.subCategory && (
                                        <Chip
                                          label={noteItem.subCategory}
                                          size="small"
                                          variant="outlined"
                                          sx={{
                                            borderColor: isDarkMode ? 'rgba(255,255,255,0.14)' : 'rgba(15,23,42,0.12)',
                                            color: isDarkMode ? '#d1d5db' : '#475569',
                                            fontWeight: 500,
                                            fontSize: '0.75rem',
                                            backgroundColor: isDarkMode ? 'rgba(255,255,255,0.03)' : 'rgba(15,23,42,0.02)',
                                            '& .MuiChip-label': {
                                              px: 1,
                                            },
                                          }}
                                        />
                                      )}
                                    </Box>
                                  </Box>
                                  <Typography
                                    variant="body1"
                                    sx={{
                                      lineHeight: 1.6,
                                      color: isDarkMode ? '#e2e8f0' : '#4a5568',
                                      fontSize: '1rem',
                                    }}
                                  >
                                    {noteItem.content}
                                  </Typography>
                                </CardContent>
                              </Card>
                            </Fade>
                          ))}
                        </Box>
                      </Box>
                    </Box>
                  </Slide>
                </Grid>
              )}
            </Grid>
          </Container>
        </Box>
      </Box>
    </ThemeProvider>
  );
}

export default App;
