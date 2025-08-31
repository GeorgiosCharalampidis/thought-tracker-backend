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
  CircularProgress
} from '@mui/material';
import { Send as SendIcon, Psychology as PsychologyIcon } from '@mui/icons-material';
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

function App() {
  const [note, setNote] = useState('');
  const [notes, setNotes] = useState<Note[]>([]);
  const [aiInsight, setAiInsight] = useState<AiInsight>({ text: '', loading: false });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async () => {
    if (!note.trim()) return;

    setLoading(true);
    try {
      // Create note
      const response = await axios.post('/api/notes/1', {
        text: note,
        date: new Date().toISOString().split('T')[0]
      });

      if (response.data) {
        setNotes(prev => [...response.data]);
        setNote('');
        
        // Get AI insight
        setAiInsight({ text: '', loading: true });
        const insightResponse = await axios.get('/api/notes/1/summary');
        setAiInsight({ text: insightResponse.data, loading: false });
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
    } catch (error) {
      console.error('Error loading notes:', error);
    }
  };

  useEffect(() => {
    loadNotes();
  }, []);

  return (
    <Box sx={{ flexGrow: 1 }}>
      <AppBar position="static">
        <Toolbar>
          <PsychologyIcon sx={{ mr: 2 }} />
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            MindLog
          </Typography>
        </Toolbar>
      </AppBar>

      <Container maxWidth="md" sx={{ mt: 4 }}>
        <Grid container spacing={3}>
          {/* Journal Entry */}
          <Grid item xs={12}>
            <Paper sx={{ p: 3 }}>
              <Typography variant="h5" gutterBottom>
                How are you feeling today?
              </Typography>
              <TextField
                fullWidth
                multiline
                rows={4}
                variant="outlined"
                placeholder="Write about your day, thoughts, feelings..."
                value={note}
                onChange={(e) => setNote(e.target.value)}
                sx={{ mb: 2 }}
              />
              <Button
                variant="contained"
                endIcon={<SendIcon />}
                onClick={handleSubmit}
                disabled={loading || !note.trim()}
                sx={{ mb: 2 }}
              >
                {loading ? <CircularProgress size={20} /> : 'Save & Get AI Insight'}
              </Button>
            </Paper>
          </Grid>

          {/* AI Insight */}
          {aiInsight.loading && (
            <Grid item xs={12}>
              <Paper sx={{ p: 3 }}>
                <Typography variant="h6" gutterBottom>
                  AI Reflection
                </Typography>
                <Box display="flex" alignItems="center" justifyContent="center" p={3}>
                  <CircularProgress />
                  <Typography sx={{ ml: 2 }}>Analyzing your thoughts...</Typography>
                </Box>
              </Paper>
            </Grid>
          )}

          {aiInsight.text && !aiInsight.loading && (
            <Grid item xs={12}>
              <Paper sx={{ p: 3, backgroundColor: '#f8f9fa' }}>
                <Typography variant="h6" gutterBottom>
                  AI Reflection
                </Typography>
                <Typography variant="body1">
                  {aiInsight.text}
                </Typography>
              </Paper>
            </Grid>
          )}

          {/* Recent Notes */}
          <Grid item xs={12}>
            <Typography variant="h5" gutterBottom>
              Recent Entries
            </Typography>
            {notes.map((noteItem) => (
              <Card key={noteItem.id} sx={{ mb: 2 }}>
                <CardContent>
                  <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                    <Typography variant="caption" color="text.secondary">
                      {new Date(noteItem.date).toLocaleDateString()}
                    </Typography>
                    <Chip label={noteItem.subject} size="small" color="primary" />
                  </Box>
                  <Typography variant="body1">
                    {noteItem.text}
                  </Typography>
                </CardContent>
              </Card>
            ))}
          </Grid>
        </Grid>
      </Container>
    </Box>
  );
}

export default App;
