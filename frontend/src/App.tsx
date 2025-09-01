import React, { useState, useEffect } from 'react';
import {
  Container,
  Box,
  Typography,
  TextField,
  Button,
  Paper,
  AppBar,
  Toolbar,
  Grid,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Fade,
  Slide,
  Avatar,
  IconButton,
  Switch,
  FormControlLabel
} from '@mui/material';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import {
  Send as SendIcon,
  Psychology as PsychologyIcon,
  AutoAwesome as SparkleIcon,
  LightbulbOutlined as LightbulbIcon,
  DarkMode as DarkModeIcon,
  LightMode as LightModeIcon
} from '@mui/icons-material';
import axios from 'axios';

interface Note {
  id: number;
  text: string;
  date: string;
  subject: string;
}

interface AiInsight {
  text: string;
  loading: boolean;
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

function App() {
  const [note, setNote] = useState('');
  const [notes, setNotes] = useState<Note[]>([]);
  const [similarThoughts, setSimilarThoughts] = useState<Note[]>([]);
  const [showSimilarThoughts, setShowSimilarThoughts] = useState(false);
  
  // Auto-detect system theme preference
  const [isDarkMode, setIsDarkMode] = useState(() => {
    if (typeof window !== 'undefined') {
      return window.matchMedia('(prefers-color-scheme: dark)').matches;
    }
    return false;
  });
  const [aiInsight, setAiInsight] = useState<AiInsight>({ text: '', loading: false });
  const [loading, setLoading] = useState(false);

  // Listen for system theme changes
  useEffect(() => {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
    const handleChange = (e: MediaQueryListEvent) => {
      setIsDarkMode(e.matches);
    };

    mediaQuery.addEventListener('change', handleChange);
    return () => mediaQuery.removeEventListener('change', handleChange);
  }, []);

  const handleSubmit = async () => {
    if (!note.trim()) return;

    setLoading(true);
    try {
      // Create note
      const response = await axios.post('/api/notes/1', {
        text: note
      });

      if (response.data) {
        setNotes(prev => [...response.data]);
        setSimilarThoughts(response.data); // Set similar thoughts from response
        setShowSimilarThoughts(true); // Show similar thoughts section
        setNote('');

        /*
        // Get AI insight
        setAiInsight({ text: '', loading: true });
        const insightResponse = await axios.get('/api/notes/1/summary');
        setAiInsight({ text: insightResponse.data, loading: false });
        */
      }
    } catch (error) {
      console.error('Error creating note:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadNotes = async () => {
    try {
      const response = await axios.get('/api/notes/1');
      setNotes(response.data);
      // Don't show similar thoughts on initial load
      setShowSimilarThoughts(false);
    } catch (error) {
      console.error('Error loading notes:', error);
    }
  };

  useEffect(() => {
    loadNotes();
  }, []);

  const theme = createAppTheme(isDarkMode);

  return (
    <ThemeProvider theme={theme}>
      <Box sx={{
        flexGrow: 1,
        minHeight: '100vh',
        backgroundColor: isDarkMode ? '#202120' : '#f1f5f9',
        paddingTop: showSimilarThoughts ? '10vh' : '35vh',
        display: showSimilarThoughts ? 'block' : 'block',
        alignItems: showSimilarThoughts ? 'normal' : 'flex-start',
        justifyContent: showSimilarThoughts ? 'normal' : 'center',
      }}>
        {/* Dark Mode Toggle */}
        <Box sx={{ 
          position: 'fixed', 
          top: 24, 
          right: 24, 
          zIndex: 1000 
        }}>
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

          <Container maxWidth="lg">
            <Grid container spacing={4}>
              {/* Journal Entry */}
              <Grid item xs={12}>
                <Fade in timeout={800}>
                  <Box sx={{ mb: 4 }}>
                    <Box display="flex" alignItems="center" justifyContent="center" mb={3}>
                      <Typography
                          variant="h4"
                          sx={{
                            fontWeight: 400,
                            color: isDarkMode ? '#f1f5f9' : '#2d3748',
                          }}
                      >
                        Share a thought
                      </Typography>
                    </Box>

                    <Box display="flex" justifyContent="center">
                      <Box sx={{ position: 'relative', width: '100%', maxWidth: '800px' }}>
                        <TextField
                            fullWidth
                            multiline
                            maxRows={6}
                            variant="outlined"
                            placeholder="What's up?"
                            value={note}
                            onChange={(e) => setNote(e.target.value)}
                            onKeyDown={(e) => {
                              if (e.key === 'Enter' && !e.shiftKey) {
                                e.preventDefault();
                                handleSubmit();
                              }
                            }}
                            sx={{
                              mb: 3,
                              '& .MuiOutlinedInput-root': {
                                borderRadius: 12,
                                fontSize: '1.1rem',
                                lineHeight: 1.6,
                                paddingRight: '60px', // Make space for button + same padding as left
                                paddingLeft: '24px', // Move text further from rounded edge
                                minHeight: '56px', // Single line height
                                backgroundColor: isDarkMode ? '#313130' : 'transparent',
                                color: isDarkMode ? '#f1f5f9' : 'inherit',
                                '& fieldset': {
                                  borderColor: isDarkMode ? '#404140' : 'rgba(0, 0, 0, 0.23)',
                                },
                                '&:hover fieldset': {
                                  borderColor: isDarkMode ? '#404140' : 'rgba(0, 0, 0, 0.23)',
                                },
                                '&.Mui-focused fieldset': {
                                  borderColor: isDarkMode ? '#404140' : 'rgba(0, 0, 0, 0.23)',
                                  borderWidth: '1px', // Keep same width when focused
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
                            onClick={handleSubmit}
                            disabled={loading || !note.trim()}
                            sx={{
                              position: 'absolute',
                              right: '8px',
                              bottom: '32px', // Adjust based on your spacing
                              minWidth: '44px',
                              width: '44px',
                              height: '44px',
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
                              <CircularProgress size={20} sx={{ color: '#93c5fd' }} />
                          ) : (
                              <SendIcon sx={{ 
                                fontSize: 20,
                                transform: 'translateX(2px)'
                              }} />
                          )}
                        </Button>
                      </Box>
                    </Box>
                  </Box>
                </Fade>
              </Grid>

              {/* AI Insight */}
              {aiInsight.loading && (
                  <Grid item xs={12}>
                    <Fade in timeout={600}>
                      <Paper
                          elevation={6}
                          sx={{
                            p: 4,
                            borderRadius: 3,
                            background: 'linear-gradient(145deg, #f7fafc 0%, #edf2f7 100%)',
                            border: '1px solid rgba(255,255,255,0.3)',
                          }}
                      >
                        <Box display="flex" alignItems="center" mb={2}>
                          <SparkleIcon sx={{ mr: 2, color: '#667eea' }} />
                          <Typography
                              variant="h6"
                              sx={{
                                fontWeight: 600,
                                color: '#4a5568'
                              }}
                          >
                            AI Reflection
                          </Typography>
                        </Box>
                        <Box display="flex" alignItems="center" justifyContent="center" p={4}>
                          <CircularProgress sx={{ color: '#667eea' }} />
                          <Typography sx={{ ml: 2, color: '#718096', fontStyle: 'italic' }}>
                            Analyzing your thoughts...
                          </Typography>
                        </Box>
                      </Paper>
                    </Fade>
                  </Grid>
              )}

              {aiInsight.text && !aiInsight.loading && (
                  <Grid item xs={12}>
                    <Slide direction="up" in timeout={800}>
                      <Paper
                          elevation={6}
                          sx={{
                            p: 4,
                            borderRadius: 3,
                            background: 'linear-gradient(145deg, #f0fff4 0%, #e6fffa 100%)',
                            border: '1px solid #9ae6b4',
                          }}
                      >
                        <Box display="flex" alignItems="center" mb={2}>
                          <SparkleIcon sx={{ mr: 2, color: '#38a169' }} />
                          <Typography
                              variant="h6"
                              sx={{
                                fontWeight: 600,
                                color: '#2d3748'
                              }}
                          >
                            AI Reflection
                          </Typography>
                        </Box>
                        <Typography
                            variant="body1"
                            sx={{
                              lineHeight: 1.7,
                              color: '#4a5568',
                              fontSize: '1.1rem'
                            }}
                        >
                          {aiInsight.text}
                        </Typography>
                      </Paper>
                    </Slide>
                  </Grid>
              )}

              {/* Similar Thoughts - Only show after submitting */}
              {showSimilarThoughts && (
                  <Grid item xs={12}>
                    <Slide direction="up" in timeout={1000}>
                      <Box sx={{ pb: 3 }}>
                        <Box display="flex" alignItems="center" mb={3}>
                          <Typography
                              variant="h4"
                              sx={{
                                fontWeight: 400,
                                color: isDarkMode ? '#f1f5f9' : '#2d3748',
                              }}
                          >
                            You're not alone..
                          </Typography>
                        </Box>

                        <Grid container spacing={3}>
                          {similarThoughts.map((noteItem, index) => (
                              <Grid item xs={12} md={6} key={noteItem.id}>
                                <Fade in timeout={600 + index * 200}>
                                  <Card
                                      elevation={1}
                                      sx={{
                                        height: '100%',
                                        borderRadius: 2,
                                        backgroundColor: isDarkMode ? '#2d2e2d' : '#ffffff',
                                        border: isDarkMode ? '1px solid #404140' : '1px solid #e2e8f0',
                                        transition: 'all 0.2s ease',
                                        '&:hover': {
                                          transform: 'translateY(-2px)',
                                          boxShadow: isDarkMode ? '0 4px 12px rgba(0,0,0,0.3)' : '0 4px 12px rgba(0,0,0,0.1)',
                                        },
                                      }}
                                  >
                                    <CardContent sx={{ p: 3 }}>
                                      <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                                        <Typography
                                            variant="caption"
                                            sx={{
                                              color: isDarkMode ? '#94a3b8' : '#718096',
                                              fontWeight: 500,
                                              fontSize: '0.85rem'
                                            }}
                                        >
                                          {new Date(noteItem.date).toLocaleDateString('en-US', {
                                            year: 'numeric',
                                            month: 'long',
                                            day: 'numeric'
                                          })}
                                        </Typography>
                                        <Chip
                                            label={noteItem.subject}
                                            size="small"
                                            sx={{
                                              background: 'linear-gradient(45deg, #667eea, #764ba2)',
                                              color: 'white',
                                              fontWeight: 600,
                                              '& .MuiChip-label': {
                                                px: 1.5,
                                              },
                                            }}
                                        />
                                      </Box>
                                      <Typography
                                          variant="body1"
                                          sx={{
                                            lineHeight: 1.6,
                                            color: isDarkMode ? '#e2e8f0' : '#4a5568',
                                            fontSize: '1rem'
                                          }}
                                      >
                                        {noteItem.text}
                                      </Typography>
                                    </CardContent>
                                  </Card>
                                </Fade>
                              </Grid>
                          ))}
                        </Grid>
                      </Box>
                    </Slide>
                  </Grid>
              )}
            </Grid>
          </Container>
      </Box>
    </ThemeProvider>
  );
}

export default App;
