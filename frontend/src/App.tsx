import React, { useEffect, useRef, useState } from 'react';
import {
  AppBar,
  Backdrop,
  Container,
  Box,
  Divider,
  Typography,
  Button,
  CircularProgress,
  Grid,
  IconButton,
  Toolbar,
  Tooltip,
  useMediaQuery,
} from '@mui/material';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import {
  AutoAwesome as SparkleIcon,
  BarChart as InsightsIcon,
  Bookmark as MyPromptsIcon,
  DarkMode as DarkModeIcon,
  LightMode as LightModeIcon,
  Logout as LogoutIcon,
  Menu as MenuIcon,
  MenuOpen as MenuOpenIcon,
} from '@mui/icons-material';
import axios from 'axios';
import AiChatPanel from './components/AiChatPanel';
import AuthDialog from './components/AuthDialog';
import DailyPromptCard from './components/DailyPromptCard';
import InsightsPanel from './components/InsightsPanel';
import MyPromptsPanel from './components/MyPromptsPanel';
import NotificationBell from './components/NotificationBell';
import SavedThoughtsSidebar from './components/SavedThoughtsSidebar';
import SimilarThoughtsSection from './components/SimilarThoughtsSection';
import ThoughtComposer from './components/ThoughtComposer';
import OnThisDaySection from './components/OnThisDaySection';
import StreakBadge from './components/StreakBadge';
import {
  AnsweredPromptSummary,
  AuthMode,
  AuthResponse,
  AuthUser,
  ChatMessage,
  CommunityMoodEntry,
  DailyPrompt,
  Note,
  OnThisDayResponse,
  PendingAction,
  PromptAnswerResponse,
  RegisterResponse,
  SimilarThoughtsResponse,
} from './types';

axios.defaults.withCredentials = true;
axios.defaults.baseURL = process.env.REACT_APP_API_URL || '';

const createAppTheme = (isDarkMode: boolean, isMobile: boolean) => createTheme({
  palette: {
    mode: isDarkMode ? 'dark' : 'light',
    background: {
      default: isDarkMode ? (isMobile ? '#000000' : '#202120') : '#f1f5f9',
      paper: isDarkMode ? (isMobile ? '#0d0d0d' : '#2d2e2d') : '#ffffff',
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

const toLocalDateStr = (d: Date) => {
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
};

const computeStreak = (notes: Note[]): number => {
  const dates = new Set(notes.map(n => n.date));
  const today = new Date();
  const todayStr = toLocalDateStr(today);
  const yesterday = new Date(today);
  yesterday.setDate(yesterday.getDate() - 1);
  const yesterdayStr = toLocalDateStr(yesterday);

  const current = dates.has(todayStr) ? new Date(today)
    : dates.has(yesterdayStr) ? new Date(yesterday)
    : null;
  if (!current) return 0;

  let streak = 0;
  while (dates.has(toLocalDateStr(current))) {
    streak++;
    current.setDate(current.getDate() - 1);
  }
  return streak;
};

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
  const [similarThoughtsKey, setSimilarThoughtsKey] = useState(0);
  const [submittedNote, setSubmittedNote] = useState('');
  const [isDarkMode, setIsDarkMode] = useState(() => {
    if (typeof window !== 'undefined') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches;
    }
    return false;
  });
  const [chatMessages, setChatMessages] = useState<ChatMessage[]>([]);
  const [chatLoading, setChatLoading] = useState(false);
  const [chatOpen, setChatOpen] = useState(false);
  const [insightsOpen, setInsightsOpen] = useState(false);
  const [myPromptsOpen, setMyPromptsOpen] = useState(false);
  const [myPrompts, setMyPrompts] = useState<AnsweredPromptSummary[]>([]);
  const [myPromptsLoading, setMyPromptsLoading] = useState(false);
  const chatAbortRef = useRef<AbortController | null>(null);
  const floatingRailRef = useRef<HTMLDivElement>(null);
  const [loading, setLoading] = useState(false);
  const [authMode, setAuthMode] = useState<AuthMode>('login');
  const [currentUser, setCurrentUser] = useState<AuthUser | null>(null);
  const [authLoading, setAuthLoading] = useState(true);
  const [authPromptOpen, setAuthPromptOpen] = useState(false);
  const [pendingAction, setPendingAction] = useState<PendingAction>(null);
  const [authPromptMessage, setAuthPromptMessage] = useState('');
  const [authSubmitting, setAuthSubmitting] = useState(false);
  const [authError, setAuthError] = useState('');
  const [pendingVerificationEmail, setPendingVerificationEmail] = useState('');
  const [savedNotes, setSavedNotes] = useState<Note[]>([]);
  const [notesLoading, setNotesLoading] = useState(false);
  const [notesError, setNotesError] = useState('');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [isSidebarClosing, setIsSidebarClosing] = useState(false);
  const [authForm, setAuthForm] = useState({
    identifier: '',
    username: '',
    email: '',
    password: '',
  });
  const [dailyPrompt, setDailyPrompt] = useState<DailyPrompt | null>(null);
  const [communityMood, setCommunityMood] = useState<CommunityMoodEntry[]>([]);
  const [communityMoodLoading, setCommunityMoodLoading] = useState(false);
  const [onThisDay, setOnThisDay] = useState<OnThisDayResponse | null>(null);
  const [onThisDayLoading, setOnThisDayLoading] = useState(false);
  const [onThisDayAnimated, setOnThisDayAnimated] = useState(false);

  useEffect(() => {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    const handleChange = (e: MediaQueryListEvent) => setIsDarkMode(e.matches);

    mediaQuery.addEventListener('change', handleChange);
    return () => mediaQuery.removeEventListener('change', handleChange);
  }, []);

  // Rail has: menu (hidden when open), chat, insights, my prompts, dark mode — each 40px with 8px gaps, starting at top 18px
  const railButtonCount = 5;
  const sidebarHeaderHeight = 18 + railButtonCount * 40 + (railButtonCount - 1) * 8 + 20;

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
    setAuthMode('login');
    setPendingVerificationEmail('');
  };

  const resetJournalState = () => {
    setSimilarThoughts([]);
    setOwnSimilarThoughts([]);
    setCategoryMessage('');
    setValidationMessage('');
    setShowSimilarThoughts(false);
    setSubmittedNote('');
    setChatMessages([]);
    setChatLoading(false);
    setChatOpen(false);
  };

  const applySimilarThoughtsResponse = (
    response: SimilarThoughtsResponse,
    options?: { clearComposer?: boolean; submittedContent?: string },
  ) => {
    if (response.inputAccepted) {
      setSimilarThoughts(response.notes);
      setOwnSimilarThoughts(response.ownNotes ?? []);
      setCategoryMessage(response.categoryMessage);
      setValidationMessage('');
      const content = options?.submittedContent ?? note;
      if (options?.clearComposer !== false) {
        setSubmittedNote(content);
        setNote('');
      } else {
        setSubmittedNote(content);
      }
      setSimilarThoughtsKey(k => k + 1);
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
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    if (token) {
      window.history.replaceState({}, '', '/');
      axios.get(`/api/auth/verify-email?token=${token}`)
        .then(() => {
          setAuthMode('login');
          setAuthPromptMessage('Your email has been verified! You can now log in.');
          setAuthPromptOpen(true);
        })
        .catch((error) => {
          setAuthMode('login');
          setAuthError(getErrorMessage(error, 'Verification failed. The link may have expired.'));
          setAuthPromptOpen(true);
        });
    }
  }, []);

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

  const loadDailyPrompt = async (authenticated: boolean) => {
    try {
      const url = authenticated ? '/api/daily-prompt' : '/api/daily-prompt/public';
      const response = await axios.get<DailyPrompt>(url);
      setDailyPrompt(response.data);
    } catch (error) {
      console.error('Error loading daily prompt:', error);
    }
  };

  const handleSubmitPromptAnswer = async (answerText: string) => {
    if (!dailyPrompt) return;
    await axios.post(`/api/daily-prompt/${dailyPrompt.promptIndex}/answer`, { answerText });
    setDailyPrompt((prev) => prev ? { ...prev, userAnswerText: answerText } : prev);
  };

  const handleEditPromptAnswer = async (answerText: string) => {
    if (!dailyPrompt) return;
    await axios.put(`/api/daily-prompt/${dailyPrompt.promptIndex}/answer`, { answerText });
    setDailyPrompt((prev) => prev ? { ...prev, userAnswerText: answerText } : prev);
  };

  const handleLoadPromptAnswers = async (): Promise<PromptAnswerResponse[]> => {
    if (!dailyPrompt) return [];
    const response = await axios.get<PromptAnswerResponse[]>(`/api/daily-prompt/${dailyPrompt.promptIndex}/answers`);
    return response.data;
  };

  const loadMyPrompts = async () => {
    setMyPromptsLoading(true);
    try {
      const res = await axios.get<AnsweredPromptSummary[]>('/api/daily-prompt/my-answers');
      setMyPrompts(res.data);
    } catch (err) {
      console.error('Error loading my prompts:', err);
    } finally {
      setMyPromptsLoading(false);
    }
  };

  const loadOnThisDay = async (userId: number) => {
    try {
      const response = await axios.get<OnThisDayResponse>(`/api/notes/${userId}/on-this-day`);
      const { weekAgo, monthAgo, yearAgo } = response.data;
      const hasAny = weekAgo?.length || monthAgo?.length || yearAgo?.length;
      setOnThisDay(hasAny ? response.data : null);
    } catch (error) {
      console.error('Error loading on-this-day memories:', error);
    } finally {
      setOnThisDayLoading(false);
    }
  };

  const loadCommunityMood = async () => {
    setCommunityMoodLoading(true);
    try {
      const response = await axios.get<CommunityMoodEntry[]>('/api/community/mood');
      setCommunityMood(response.data);
    } catch (error) {
      console.error('Error loading community mood:', error);
    } finally {
      setCommunityMoodLoading(false);
    }
  };

  useEffect(() => {
    if (currentUser) {
      setOnThisDayLoading(true);
      void loadNotes(currentUser.id);
      void loadDailyPrompt(true);
      void loadOnThisDay(currentUser.id);
      return;
    }

    void loadDailyPrompt(false);
    setSavedNotes([]);
    setOnThisDay(null);
    setOnThisDayLoading(false);
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
      if (authMode === 'register') {
        const response = await axios.post<RegisterResponse>('/api/auth/register', {
          username: authForm.username,
          email: authForm.email,
          password: authForm.password,
        });
        if (response.data.message === 'VERIFICATION_SENT') {
          setPendingVerificationEmail(response.data.email);
          setAuthMode('verify-pending');
          setAuthForm({ identifier: '', username: '', email: '', password: '' });
        }
        return;
      }

      const response = await axios.post<AuthResponse>('/api/auth/login', {
        identifier: authForm.identifier,
        password: authForm.password,
      });
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
      const message = getErrorMessage(error, authMode === 'login' ? 'Login failed.' : 'Registration failed.');
      if (message.startsWith('EMAIL_NOT_VERIFIED:')) {
        setPendingVerificationEmail(message.replace('EMAIL_NOT_VERIFIED:', ''));
        setAuthMode('verify-pending');
        return;
      }
      setAuthError(message);
    } finally {
      setAuthSubmitting(false);
    }
  };

  const handleResendVerification = async () => {
    setAuthSubmitting(true);
    setAuthError('');
    try {
      await axios.post('/api/auth/resend-verification', { email: pendingVerificationEmail });
    } catch (error) {
      setAuthError(getErrorMessage(error, 'Could not resend verification email.'));
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
    }
  };

  const handleSavedNoteSelect = async (savedNote: Note) => {
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
      applySimilarThoughtsResponse(response.data, { clearComposer: false, submittedContent: savedNote.content });
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

  const isMobile = useMediaQuery('(max-width: 600px)');
  const streak = computeStreak(savedNotes);

  const theme = createAppTheme(isDarkMode, isMobile);
  const sidebarWidth = isMobile ? Math.min(window.innerWidth * 0.85, 320) : 284;
  const sidebarVisible = isSidebarOpen || isSidebarClosing;
  // On mobile the sidebar overlays content — never push the main content
  const mainContentOffset = isMobile ? 0 : (isSidebarOpen ? 316 : 96);
  const headingText = 'Share a thought';
  const thoughtPlaceholder = (currentUser || isMobile)
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
            backgroundColor: isDarkMode ? (isMobile ? '#000000' : '#202120') : '#f1f5f9',
          }}
        >
          <CircularProgress size={28} sx={{ color: '#667eea' }} />
        </Box>
      </ThemeProvider>
    );
  }

  return (
    <ThemeProvider theme={theme}>
      {/* Mobile top AppBar — menu toggle left, account actions right */}
      {isMobile && (
        <AppBar position="fixed" elevation={0} sx={{ backgroundColor: isDarkMode ? '#000000' : '#f1f5f9', zIndex: 1300, transition: 'background-color 0.22s ease' }}>
          <Toolbar variant="dense" sx={{ gap: 0.5, px: 2, py: 2 }}>
            <IconButton edge="start" size="small" onClick={handlePrimaryLeftAction} sx={{ color: isDarkMode ? '#cbd5e1' : '#475569' }}>
              {isSidebarOpen ? <MenuOpenIcon sx={{ fontSize: 20 }} /> : <MenuIcon sx={{ fontSize: 20 }} />}
            </IconButton>
            <Box sx={{ flexGrow: 1 }} />
            {currentUser ? (
              <>
                <NotificationBell isDarkMode={isDarkMode} onNoteClick={async (noteId) => {
                  if (!currentUser) return;
                  setLoading(true);
                  try {
                    const response = await axios.get<SimilarThoughtsResponse>(`/api/notes/${currentUser.id}/similar-to/${noteId}`);
                    applySimilarThoughtsResponse(response.data, { clearComposer: false });
                  } catch (error) {
                    console.error('Error loading note from notification:', error);
                  } finally {
                    setLoading(false);
                  }
                }} />
                <Button size="small" variant="text" onClick={handleLogout} startIcon={<LogoutIcon sx={{ fontSize: 15 }} />} sx={{ borderRadius: 999, px: 1, py: 0.5, textTransform: 'none', fontSize: '0.82rem', color: isDarkMode ? '#cbd5e1' : '#475569', minWidth: 0, '&:hover': { backgroundColor: isDarkMode ? 'rgba(255,255,255,0.06)' : 'rgba(15,23,42,0.05)' } }}>
                  Log out
                </Button>
              </>
            ) : (
              <>
                <Button size="small" variant="text" onClick={() => openAuthPrompt('login', null, 'Log in to save thoughts and unlock AI reflections.')} sx={{ borderRadius: 999, px: 1.2, py: 0.5, textTransform: 'none', fontSize: '0.82rem', color: isDarkMode ? '#111827' : '#ffffff', backgroundColor: isDarkMode ? '#ffffff' : '#111827', border: isDarkMode ? '1px solid rgba(255,255,255,0.10)' : '1px solid rgba(15,23,42,0.08)', '&:hover': { backgroundColor: isDarkMode ? '#f8fafc' : '#222222' } }}>
                  Log in
                </Button>
                <Button size="small" variant="text" onClick={() => openAuthPrompt('register', null, 'Create an account to save thoughts and unlock AI reflections.')} sx={{ borderRadius: 999, px: 1.2, py: 0.5, textTransform: 'none', fontSize: '0.82rem', color: isDarkMode ? '#f3f4f6' : '#111827', backgroundColor: isDarkMode ? 'rgba(255,255,255,0.08)' : '#ffffff', border: isDarkMode ? '1px solid rgba(255,255,255,0.10)' : '1px solid rgba(15,23,42,0.08)', '&:hover': { backgroundColor: isDarkMode ? 'rgba(255,255,255,0.12)' : '#f8fafc' } }}>
                  Sign up
                </Button>
              </>
            )}
          </Toolbar>
        </AppBar>
      )}

      {/* Backdrop for mobile sidebar */}
      {isMobile && (
        <Backdrop open={isSidebarOpen} onClick={closeSidebar} sx={{ zIndex: 1199 }} />
      )}

      {currentUser && streak > 0 && (
        <StreakBadge streak={streak} isDarkMode={isDarkMode} isMobile={isMobile} />
      )}

      {/* Page background and top-level layout wrapper for the app. */}
      <Box sx={{
        flexGrow: 1,
        minHeight: '100vh',
        backgroundColor: isDarkMode
          ? (isMobile ? (isSidebarOpen ? '#202120' : '#000000') : '#202120')
          : (isMobile ? (isSidebarOpen ? '#edf1f6' : '#f1f5f9') : '#f1f5f9'),
        transition: 'background-color 0.22s ease',
        paddingTop: isMobile ? '72px' : (showSimilarThoughts ? '12vh' : (onThisDay ? '10vh' : '28vh')),
        // On mobile, vertically center the composer when nothing else is shown
        ...(isMobile && !showSimilarThoughts && !chatOpen && {
          display: 'flex',
          flexDirection: 'column',
          justifyContent: 'center',
        }),
      }}>
        {/* Left floating action rail: sidebar toggle and AI reflection shortcut. */}
        <Box
          sx={{
            position: 'fixed',
            top: 18,
            left: 16,
            zIndex: 1100,
            display: { xs: 'none', sm: 'flex' },
            flexDirection: 'column',
            gap: 1,
            pointerEvents: 'none',
          }}
        >
          <Tooltip title="Open sidebar" placement="right">
            <IconButton
              onClick={handlePrimaryLeftAction}
              disableRipple
              sx={{
                width: 40,
                height: 40,
                pointerEvents: isSidebarOpen || isSidebarClosing ? 'none' : 'auto',
                opacity: isSidebarOpen || isSidebarClosing ? 0 : 1,
                color: isDarkMode ? '#cbd5e1' : '#475569',
                backgroundColor: 'transparent',
                border: '1px solid transparent',
                transition: 'opacity 0.14s ease',
                '&:hover': {
                backgroundColor: isDarkMode ? '#262726' : '#eef2f7',
                borderColor: isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(15,23,42,0.08)',
              },
              }}
            >
              <MenuIcon sx={{ fontSize: 18 }} />
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
                width: isSidebarOpen ? `${sidebarWidth - 24}px` : 'fit-content',
                height: 40,
                pointerEvents: 'auto',
                pl: 0,
                pr: isSidebarOpen ? 1 : 0,
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
                  backgroundColor: 'transparent',
                  border: '1px solid transparent',
                  transition: 'background-color 0.2s ease, border-color 0.2s ease',
                  '&:hover': {
                    backgroundColor: isDarkMode ? '#262726' : '#eef2f7',
                    borderColor: isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(15,23,42,0.08)',
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

          {(
            <Tooltip
              title="My Insights"
              placement="right"
              disableHoverListener={sidebarVisible}
              disableFocusListener={sidebarVisible}
              disableTouchListener={sidebarVisible}
            >
              <Box
                onClick={() => { if (!currentUser) { openAuthPrompt('login', null, 'Log in to view your personal insights.'); return; } setInsightsOpen(true); void loadCommunityMood(); }}
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 1,
                  width: isSidebarOpen ? `${sidebarWidth - 24}px` : 'fit-content',
                  height: 40,
                  pointerEvents: 'auto',
                  pl: 0,
                  pr: isSidebarOpen ? 1 : 0,
                  py: 0,
                  borderRadius: 999,
                  cursor: 'pointer',
                  opacity: isSidebarClosing ? 0.72 : 1,
                  transition: 'background-color 0.2s ease, opacity 0.14s ease',
                  backgroundColor: 'transparent',
                  '&:hover': {
                    backgroundColor: isSidebarOpen
                      ? (isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(15,23,42,0.08)')
                      : 'transparent',
                  },
                  '&:hover .insights-icon-btn': {
                    backgroundColor: isDarkMode ? '#262726' : '#eef2f7',
                    borderColor: isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(15,23,42,0.08)',
                  },
                }}
              >
                <IconButton
                  className="insights-icon-btn"
                  sx={{
                    width: 40,
                    height: 40,
                    pointerEvents: 'none',
                    color: isDarkMode ? '#cbd5e1' : '#475569',
                    backgroundColor: 'transparent',
                    border: '1px solid transparent',
                    transition: 'background-color 0.2s ease, border-color 0.2s ease',
                  }}
                >
                  <InsightsIcon sx={{ fontSize: 18 }} />
                </IconButton>
                {isSidebarOpen && (
                  <Typography sx={{ pr: 0.15, fontSize: '0.86rem', color: isDarkMode ? '#cbd5e1' : '#475569', whiteSpace: 'nowrap', lineHeight: 1 }}>
                    My Insights
                  </Typography>
                )}
              </Box>
            </Tooltip>
          )}

          {(
            <Tooltip
              title="My Prompts"
              placement="right"
              disableHoverListener={sidebarVisible}
              disableFocusListener={sidebarVisible}
              disableTouchListener={sidebarVisible}
            >
              <Box
                onClick={() => { if (!currentUser) { openAuthPrompt('login', null, 'Log in to view your saved prompts.'); return; } setMyPromptsOpen(true); void loadMyPrompts(); }}
                sx={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 1,
                  width: isSidebarOpen ? `${sidebarWidth - 24}px` : 'fit-content',
                  height: 40,
                  pointerEvents: 'auto',
                  pl: 0,
                  pr: isSidebarOpen ? 1 : 0,
                  py: 0,
                  borderRadius: 999,
                  cursor: 'pointer',
                  opacity: isSidebarClosing ? 0.72 : 1,
                  transition: 'background-color 0.2s ease, opacity 0.14s ease',
                  backgroundColor: 'transparent',
                  '&:hover': {
                    backgroundColor: isSidebarOpen
                      ? (isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(15,23,42,0.08)')
                      : 'transparent',
                  },
                  '&:hover .myprompts-icon-btn': {
                    backgroundColor: isDarkMode ? '#262726' : '#eef2f7',
                    borderColor: isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(15,23,42,0.08)',
                  },
                }}
              >
                <IconButton
                  className="myprompts-icon-btn"
                  sx={{
                    width: 40,
                    height: 40,
                    pointerEvents: 'none',
                    color: isDarkMode ? '#cbd5e1' : '#475569',
                    backgroundColor: 'transparent',
                    border: '1px solid transparent',
                    transition: 'background-color 0.2s ease, border-color 0.2s ease',
                  }}
                >
                  <MyPromptsIcon sx={{ fontSize: 18 }} />
                </IconButton>
                {isSidebarOpen && (
                  <Typography sx={{ pr: 0.15, fontSize: '0.86rem', color: isDarkMode ? '#cbd5e1' : '#475569', whiteSpace: 'nowrap', lineHeight: 1 }}>
                    My Prompts
                  </Typography>
                )}
              </Box>
            </Tooltip>
          )}

          <Tooltip title={isDarkMode ? 'Switch to light mode' : 'Switch to dark mode'} placement="right" disableHoverListener={sidebarVisible} disableFocusListener={sidebarVisible} disableTouchListener={sidebarVisible}>
            <Box
              onClick={() => setIsDarkMode(!isDarkMode)}
              sx={{
                display: 'flex',
                alignItems: 'center',
                gap: 1,
                width: isSidebarOpen ? `${sidebarWidth - 24}px` : 'fit-content',
                height: 40,
                pointerEvents: 'auto',
                pl: 0,
                pr: isSidebarOpen ? 1 : 0,
                py: 0,
                borderRadius: 999,
                cursor: 'pointer',
                transition: 'background-color 0.2s ease, opacity 0.14s ease',
                backgroundColor: 'transparent',
                '&:hover': {
                  backgroundColor: isSidebarOpen
                    ? (isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(15,23,42,0.08)')
                    : 'transparent',
                },
                '&:hover .darkmode-icon-btn': {
                  backgroundColor: isDarkMode ? '#262726' : '#eef2f7',
                  borderColor: isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(15,23,42,0.08)',
                },
              }}
            >
              <IconButton
                className="darkmode-icon-btn"
                sx={{
                  width: 40,
                  height: 40,
                  pointerEvents: 'none',
                  color: isDarkMode ? '#cbd5e1' : '#475569',
                  backgroundColor: 'transparent',
                  border: '1px solid transparent',
                  transition: 'background-color 0.2s ease, border-color 0.2s ease',
                }}
              >
                {isDarkMode ? <LightModeIcon sx={{ fontSize: 18 }} /> : <DarkModeIcon sx={{ fontSize: 18 }} />}
              </IconButton>
              {isSidebarOpen && (
                <Typography sx={{ pr: 0.15, fontSize: '0.86rem', color: isDarkMode ? '#cbd5e1' : '#475569', whiteSpace: 'nowrap', lineHeight: 1 }}>
                  {isDarkMode ? 'Light mode' : 'Dark mode'}
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
          isMobile={isMobile}
          isSidebarOpen={isSidebarOpen}
          isSidebarClosing={isSidebarClosing}
          sidebarWidth={sidebarWidth}
          onClose={closeSidebar}
          onSelectNote={handleSavedNoteSelect}
          onTransitionEnd={handleSidebarTransitionEnd}
          formatHistoryDate={formatHistoryDate}
          headerHeight={sidebarHeaderHeight}
          onNoteUpdated={(noteId, newContent) => {
            setSavedNotes(prev => prev.map(n => n.id === noteId ? { ...n, content: newContent } : n));
          }}
          onNoteDeleted={(noteId) => {
            setSavedNotes(prev => prev.filter(n => n.id !== noteId));
          }}
          mobileNav={isMobile ? (
            <Box sx={{ display: 'flex', flexDirection: 'column' }}>
              {[
                { icon: <SparkleIcon sx={{ fontSize: 18 }} />, label: 'Chat', onClick: () => { openChat(); closeSidebar(); } },
                { icon: <InsightsIcon sx={{ fontSize: 18 }} />, label: 'My Insights', onClick: () => { if (!currentUser) { openAuthPrompt('login', null, 'Log in to view your personal insights.'); closeSidebar(); return; } setInsightsOpen(true); void loadCommunityMood(); closeSidebar(); } },
                { icon: <MyPromptsIcon sx={{ fontSize: 18 }} />, label: 'My Prompts', onClick: () => { if (!currentUser) { openAuthPrompt('login', null, 'Log in to view your saved prompts.'); closeSidebar(); return; } setMyPromptsOpen(true); void loadMyPrompts(); closeSidebar(); } },
                { icon: isDarkMode ? <LightModeIcon sx={{ fontSize: 18 }} /> : <DarkModeIcon sx={{ fontSize: 18 }} />, label: isDarkMode ? 'Light mode' : 'Dark mode', onClick: () => setIsDarkMode(d => !d) },
              ].map(({ icon, label, onClick }) => (
                <Box
                  key={label}
                  onClick={onClick}
                  sx={{
                    display: 'flex', alignItems: 'center', gap: 1.5,
                    px: 0.5, py: 1, borderRadius: 2, cursor: 'pointer',
                    color: isDarkMode ? '#cbd5e1' : '#475569',
                    '&:hover': { backgroundColor: isDarkMode ? 'rgba(255,255,255,0.06)' : 'rgba(15,23,42,0.05)' },
                  }}
                >
                  {icon}
                  <Typography sx={{ fontSize: '0.9rem', lineHeight: 1 }}>{label}</Typography>
                </Box>
              ))}
            </Box>
          ) : undefined}
        />

        {/* Top-right account actions and dark mode toggle. */}
        <Box
          sx={{
            position: 'fixed',
            top: 24,
            right: 24,
            zIndex: 1000,
            display: { xs: 'none', sm: 'flex' },
            alignItems: 'center',
            gap: 1,
          }}
        >
          {currentUser && (
            <NotificationBell
              isDarkMode={isDarkMode}
              onNoteClick={async (noteId) => {
                if (!currentUser) return;
                setLoading(true);
                try {
                  const response = await axios.get<SimilarThoughtsResponse>(
                    `/api/notes/${currentUser.id}/similar-to/${noteId}`,
                  );
                  applySimilarThoughtsResponse(response.data, { clearComposer: false });
                } catch (error) {
                  console.error('Error loading note from notification:', error);
                } finally {
                  setLoading(false);
                }
              }}
            />
          )}

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
                backgroundColor: 'transparent',
                border: '1px solid transparent',
                transition: 'background-color 0.2s ease, border-color 0.2s ease',
                '&:hover': {
                  backgroundColor: isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(255,255,255,0.95)',
                  borderColor: isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(0,0,0,0.06)',
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

        </Box>

        <InsightsPanel
          open={insightsOpen}
          onClose={() => setInsightsOpen(false)}
          notes={savedNotes}
          isDarkMode={isDarkMode}
          communityMood={communityMood}
          communityMoodLoading={communityMoodLoading}
        />

        {currentUser && (
          <MyPromptsPanel
            open={myPromptsOpen}
            onClose={() => setMyPromptsOpen(false)}
            prompts={myPrompts}
            loading={myPromptsLoading}
            currentUser={currentUser}
            isDarkMode={isDarkMode}
          />
        )}

        {/* Authentication dialog used for login and sign-up flows. */}
        <AuthDialog
          open={authPromptOpen}
          authMode={authMode}
          authPromptMessage={authPromptMessage}
          pendingVerificationEmail={pendingVerificationEmail}
          onResendVerification={handleResendVerification}
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
            mr: isMobile ? 0 : (isSidebarOpen ? 0 : '96px'),
            px: isMobile ? 1 : 0,
            transition: 'margin-left 0.22s ease, margin-right 0.22s ease',
          }}
        >
          <Container maxWidth="lg">
            <Grid container spacing={2}>
              {onThisDayLoading ? null : !showSimilarThoughts && (
                <Grid item xs={12}>
                  {currentUser && onThisDay && (
                    <OnThisDaySection
                      memories={onThisDay}
                      isDarkMode={isDarkMode}
                      onSelectMemory={handleSavedNoteSelect}
                      skipAnimation={onThisDayAnimated}
                      onAnimated={() => setOnThisDayAnimated(true)}
                    />
                  )}
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
                  {dailyPrompt && (
                    <>
                      <Divider sx={{ my: { xs: 3, sm: 6 }, borderColor: isDarkMode ? 'rgba(255,255,255,0.18)' : 'rgba(15,23,42,0.18)' }} />
                      <DailyPromptCard
                        prompt={dailyPrompt}
                        currentUser={currentUser}
                        isDarkMode={isDarkMode}
                        isMobile={isMobile}
                        isToday={true}
                        onSubmitAnswer={handleSubmitPromptAnswer}
                        onEditAnswer={handleEditPromptAnswer}
                        onLoadAnswers={handleLoadPromptAnswers}
                        onLoginRequired={() => openAuthPrompt('login', null, 'Log in to respond to today\'s prompt.')}
                      />
                    </>
                  )}
                </Grid>
              )}

              {showSimilarThoughts && (
                <Grid item xs={12}>
                  <SimilarThoughtsSection
                    key={similarThoughtsKey}
                    notes={similarThoughts}
                    ownNotes={ownSimilarThoughts}
                    categoryMessage={categoryMessage}
                    isDarkMode={isDarkMode}
                    currentUser={currentUser}
                    submittedNote={submittedNote}
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
