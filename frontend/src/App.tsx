import React, { useEffect, useRef, useState } from 'react';
import {
  Container,
  Box,
  Typography,
  Button,
  CircularProgress,
  Grid,
  IconButton,
  Tooltip,
} from '@mui/material';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import {
  AutoAwesome as SparkleIcon,
  DarkMode as DarkModeIcon,
  LightMode as LightModeIcon,
  Logout as LogoutIcon,
  Menu as MenuIcon,
  TextFields as TextFieldsIcon,
} from '@mui/icons-material';
import axios from 'axios';
import AiChatPanel from './components/AiChatPanel';
import AuthDialog from './components/AuthDialog';
import SavedThoughtsSidebar from './components/SavedThoughtsSidebar';
import SimilarThoughtsSection from './components/SimilarThoughtsSection';
import ThoughtComposer from './components/ThoughtComposer';
import {
  AuthMode,
  AuthResponse,
  AuthUser,
  ChatMessage,
  Note,
  PendingAction,
  SimilarThoughtsResponse,
} from './types';

axios.defaults.withCredentials = true;

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

function App() {
  // Main screen state for the thought composer and response panels.
  const [note, setNote] = useState('');
  const [similarThoughts, setSimilarThoughts] = useState<Note[]>([]);
  const [ownSimilarThoughts, setOwnSimilarThoughts] = useState<Note[]>([]);
  const [categoryMessage, setCategoryMessage] = useState<string>('');
  const [validationMessage, setValidationMessage] = useState<string>('');
  const [showSimilarThoughts, setShowSimilarThoughts] = useState(false);
  const [isDarkMode, setIsDarkMode] = useState(() => {
    if (typeof window !== 'undefined') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches;
    }
    return false;
  });
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [chatLoading, setChatLoading] = useState(false);
  const [chatOpen, setChatOpen] = useState(false);
  const chatAbortRef = useRef<AbortController | null>(null);
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
  const [isSidebarClosing, setIsSidebarClosing] = useState(false);
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
  };

  const handleAuthDialogExited = () => {
    setPendingAction(null);
    setAuthPromptMessage('');
    setAuthError('');
  };

  const resetJournalState = () => {
    setSimilarThoughts([]);
    setOwnSimilarThoughts([]);
    setCategoryMessage('');
    setValidationMessage('');
    setShowSimilarThoughts(false);
    setChatMessages([]);
    setChatLoading(false);
    setChatOpen(false);
  };

  const applySimilarThoughtsResponse = (
    response: SimilarThoughtsResponse,
    options?: { clearComposer?: boolean },
  ) => {
    if (response.inputAccepted) {
      setSimilarThoughts(response.notes);
      setOwnSimilarThoughts(response.ownNotes ?? []);
      setCategoryMessage(response.categoryMessage);
      setValidationMessage('');
      if (options?.clearComposer !== false) {
        setNote('');
      }
      setShowSimilarThoughts(true);
      return;
    }

    setOwnSimilarThoughts([]);
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

  const sendChatMessageForUser = async (user: AuthUser, messages: ChatMessage[]) => {
    chatAbortRef.current?.abort();
    chatAbortRef.current = new AbortController();

    setChatLoading(true);
    try {
      const response = await axios.post<string>(
        `/api/notes/${user.id}/chat`,
        { messages },
        { signal: chatAbortRef.current.signal },
      );
      const aiMessage: ChatMessage = { role: 'assistant', content: response.data };
      setChatMessages((prev) => [...prev, aiMessage]);
    } catch (error) {
      if (axios.isCancel(error)) return;
      console.error('Error in chat:', error);
      if (axios.isAxiosError(error) && error.response?.status === 401) {
        setChatOpen(false);
        setChatMessages([]);
        openAuthPrompt('login', 'chat', 'Log in to chat with your journal AI.');
      } else {
        const errMessage: ChatMessage = {
          role: 'assistant',
          content: getErrorMessage(error, 'AI service is currently unavailable. Please try again later.'),
        };
        setChatMessages((prev) => [...prev, errMessage]);
      }
    } finally {
      setChatLoading(false);
    }
  };

  const openChatForUser = async (user: AuthUser) => {
    setChatOpen(true);
    setChatMessages([]);
    await sendChatMessageForUser(user, []);
  };

  const handleSubmit = async () => {
    if (!note.trim()) return;

    if (!currentUser) {
      await previewThought();
      return;
    }

    await submitNoteForUser(currentUser);
  };

  const openChat = async () => {
    if (!currentUser) {
      openAuthPrompt('login', 'chat', 'Log in to chat with your AI about your journal.');
      return;
    }
    await openChatForUser(currentUser);
  };

  const handleSendChatMessage = async (content: string) => {
    if (!currentUser) return;
    const userMessage: ChatMessage = { role: 'user', content };
    const updatedMessages = [...chatMessages, userMessage];
    setChatMessages(updatedMessages);
    await sendChatMessageForUser(currentUser, updatedMessages);
  };

  const handleAuthInputChange = (field: 'identifier' | 'username' | 'email' | 'password', value: string) => {
    setAuthForm((previous) => ({
      ...previous,
      [field]: value,
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
      } else if (actionToResume === 'chat') {
        await openChatForUser(authenticatedUser);
      }
    } catch (error) {
      console.error(`Error during ${authMode}:`, error);
      setAuthError(getErrorMessage(error, authMode === 'login' ? 'Login failed.' : 'Registration failed.'));
    } finally {
      setAuthSubmitting(false);
    }
  };

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
      setAuthPromptOpen(false);
      handleAuthDialogExited();
      resetJournalState();
      setNote('');
    }
  };

  const handleShareAnotherThought = () => {
    resetJournalState();
    setNote('');
  };

  const openSidebar = () => {
    setIsSidebarClosing(false);
    setIsSidebarOpen(true);
  };

  const closeSidebar = () => {
    if (!isSidebarOpen) {
      return;
    }
    setIsSidebarClosing(true);
    setIsSidebarOpen(false);
  };

  const handleSidebarTransitionEnd = () => {
    if (!isSidebarOpen && isSidebarClosing) {
      setIsSidebarClosing(false);
    }
  };

  const handlePrimaryLeftAction = () => {
    if (!isSidebarOpen && !isSidebarClosing) {
      openSidebar();
      return;
    }

    if (isSidebarOpen) {
      setIsAltHeading((previous) => !previous);
    }
  };

  const handleSavedNoteSelect = async (savedNote: Note) => {
    setNote(savedNote.content);
    setValidationMessage('');

    if (!currentUser) {
      setShowSimilarThoughts(false);
      return;
    }

    setLoading(true);
    try {
      const response = await axios.get<SimilarThoughtsResponse>(
        `/api/notes/${currentUser.id}/subject/${encodeURIComponent(savedNote.category)}/similar-to/${savedNote.id}`,
      );
      applySimilarThoughtsResponse(response.data, { clearComposer: false });
    } catch (error) {
      console.error('Error loading similar thoughts for saved note:', error);
      setOwnSimilarThoughts([]);
      setCategoryMessage('');
      setShowSimilarThoughts(false);
      if (axios.isAxiosError(error) && error.response?.status === 401) {
        openAuthPrompt('login', null, 'Log in again to explore related saved thoughts.');
      } else {
        setValidationMessage(getErrorMessage(error, 'Could not load related thoughts for this entry.'));
      }
    } finally {
      setLoading(false);
    }
  };

  const theme = createAppTheme(isDarkMode);
  const sidebarWidth = 284;
  const sidebarVisible = isSidebarOpen || isSidebarClosing;
  const mainContentOffset = isSidebarOpen ? 316 : 96;
  const headingText = isAltHeading ? 'Pause and notice' : 'Share a thought';
  const thoughtPlaceholder = currentUser
    ? 'Write a thought...'
    : 'Write a thought... sign in to save it.';

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
      <Box sx={{ flexGrow: 1, minHeight: '100vh', backgroundColor: isDarkMode ? '#202120' : '#f1f5f9', paddingTop: showSimilarThoughts ? '12vh' : '28vh' }}>
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
                pointerEvents: isSidebarClosing ? 'none' : 'auto',
                opacity: isSidebarClosing ? 0 : 1,
                color: isDarkMode ? '#cbd5e1' : '#475569',
                backgroundColor: isDarkMode ? '#202120' : '#f1f5f9',
                border: isDarkMode ? '1px solid rgba(255,255,255,0.08)' : '1px solid rgba(15,23,42,0.08)',
                transition: 'opacity 0.14s ease, background-color 0.2s ease',
                '&:hover': {
                  backgroundColor: isDarkMode ? '#262726' : '#eef2f7',
                },
              }}
            >
              {isSidebarOpen ? <TextFieldsIcon sx={{ fontSize: 18 }} /> : <MenuIcon sx={{ fontSize: 18 }} />}
            </IconButton>
          </Tooltip>

          <Tooltip
            title="Chat with your journal AI"
            placement="right"
            disableHoverListener={sidebarVisible}
            disableFocusListener={sidebarVisible}
            disableTouchListener={sidebarVisible}
          >
            <Box
              onClick={openChat}
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
                opacity: isSidebarClosing ? 0.72 : 1,
                transition: 'background-color 0.2s ease, color 0.2s ease, opacity 0.14s ease',
                backgroundColor: 'transparent',
                '&:hover': {
                  backgroundColor: isSidebarOpen
                    ? (isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(15,23,42,0.08)')
                    : 'transparent',
                },
              }}
            >
              <IconButton
                onClick={openChat}
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
                  Chat with your journal AI
                </Typography>
              )}
            </Box>
          </Tooltip>
        </Box>

        <SavedThoughtsSidebar
          currentUser={currentUser}
          savedNotes={savedNotes}
          notesLoading={notesLoading}
          notesError={notesError}
          isDarkMode={isDarkMode}
          isSidebarOpen={isSidebarOpen}
          isSidebarClosing={isSidebarClosing}
          sidebarWidth={sidebarWidth}
          onClose={closeSidebar}
          onSelectNote={handleSavedNoteSelect}
          onTransitionEnd={handleSidebarTransitionEnd}
          formatHistoryDate={formatHistoryDate}
        />

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
        <AuthDialog
          open={authPromptOpen}
          authMode={authMode}
          authPromptMessage={authPromptMessage}
          authError={authError}
          authSubmitting={authSubmitting}
          authForm={authForm}
          isDarkMode={isDarkMode}
          onClose={closeAuthPrompt}
          onExited={handleAuthDialogExited}
          onSubmit={handleAuthSubmit}
          onChange={handleAuthInputChange}
        />

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
                  <ThoughtComposer
                    headingText={headingText}
                    thoughtPlaceholder={thoughtPlaceholder}
                    note={note}
                    validationMessage={validationMessage}
                    loading={loading}
                    isDarkMode={isDarkMode}
                    onChange={(value) => {
                      setNote(value);
                      if (validationMessage) {
                        setValidationMessage('');
                      }
                    }}
                    onSubmit={handleSubmit}
                  />
                </Grid>
              )}

              {showSimilarThoughts && (
                <Grid item xs={12}>
                  <SimilarThoughtsSection
                    notes={similarThoughts}
                    ownNotes={ownSimilarThoughts}
                    categoryMessage={categoryMessage}
                    isDarkMode={isDarkMode}
                    onShareAnotherThought={handleShareAnotherThought}
                  />
                </Grid>
              )}

              {chatOpen && (
                <Grid item xs={12}>
                  <AiChatPanel
                    messages={chatMessages}
                    loading={chatLoading}
                    isDarkMode={isDarkMode}
                    onSendMessage={handleSendChatMessage}
                  />
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
