import React from 'react';
import { Box, CircularProgress, Fade, Paper, Slide, Typography } from '@mui/material';
import { AutoAwesome as SparkleIcon } from '@mui/icons-material';

interface AiReflectionPanelProps {
  text: string;
  loading: boolean;
  isDarkMode: boolean;
}

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

function AiReflectionPanel({ text, loading, isDarkMode }: AiReflectionPanelProps) {
  if (loading) {
    return (
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
    );
  }

  if (!text) {
    return null;
  }

  return (
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
        {renderInsightContent(text, isDarkMode)}
      </Paper>
    </Slide>
  );
}

export default AiReflectionPanel;

