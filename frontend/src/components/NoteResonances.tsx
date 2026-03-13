import React, { useState, useCallback, useEffect } from 'react';
import {
  Box,
  Typography,
  Collapse,
  CircularProgress,
} from '@mui/material';
import FavoriteBorderIcon from '@mui/icons-material/FavoriteBorder';
import axios from 'axios';
import { AuthUser } from '../types';

interface ResonanceSnippet {
  bodyText: string;
  occurredAt: string;
}

interface NoteResonancesProps {
  noteId: number;
  userId: number;
  currentUser: AuthUser | null;
  isDarkMode: boolean;
}

function formatRelativeTime(iso: string): string {
  const diffMs = Date.now() - new Date(iso).getTime();
  const mins = Math.floor(diffMs / 60_000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins}m ago`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours}h ago`;
  return `${Math.floor(hours / 24)}d ago`;
}

function NoteResonances({ noteId, userId, currentUser, isDarkMode }: NoteResonancesProps) {
  const [open, setOpen] = useState(false);
  const [resonances, setResonances] = useState<ResonanceSnippet[]>([]);
  const [loaded, setLoaded] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (loaded || !currentUser) return;
    setLoading(true);
    setError(null);
    try {
      const res = await axios.get<ResonanceSnippet[]>(`/api/notes/${userId}/${noteId}/resonances`);
      setResonances(res.data);
      setLoaded(true);
    } catch {
      setError('Could not load resonances.');
    } finally {
      setLoading(false);
    }
  }, [noteId, userId, loaded, currentUser]);

  useEffect(() => {
    load();
  }, [load]);

  if (!currentUser) return null;

  const count = resonances.length;
  const mutedColor = isDarkMode ? '#94a3b8' : '#64748b';
  const textColor = isDarkMode ? '#e2e8f0' : '#374151';
  const borderColor = isDarkMode ? 'rgba(255,255,255,0.08)' : 'rgba(15,23,42,0.08)';
  const resonanceColor = isDarkMode ? '#34d399' : '#059669';

  return (
    <Box mt={1.5} borderTop={`1px solid ${borderColor}`} pt={1.5}>
      <Box
        display="flex"
        alignItems="center"
        gap={0.5}
        sx={{ cursor: 'pointer', width: 'fit-content' }}
        onClick={() => setOpen(prev => !prev)}
      >
        <FavoriteBorderIcon sx={{ fontSize: '0.95rem', color: count > 0 ? resonanceColor : mutedColor }} />
        <Typography variant="caption" sx={{ color: count > 0 ? resonanceColor : mutedColor, userSelect: 'none' }}>
          {loading ? '…' : count === 0 ? 'No resonances yet' : `${count} resonance${count === 1 ? '' : 's'}`}
        </Typography>
      </Box>

      <Collapse in={open} sx={{ overflow: 'hidden', '&.MuiCollapse-entered': { overflow: 'hidden' } }}>
        <Box mt={1.5}>
          {loading && (
            <Box display="flex" justifyContent="center" py={1}>
              <CircularProgress size={18} sx={{ color: resonanceColor }} />
            </Box>
          )}

          {!loading && resonances.length === 0 && loaded && (
            <Typography variant="caption" sx={{ color: mutedColor, display: 'block', mb: 1 }}>
              No one has resonated with this thought yet.
            </Typography>
          )}

          {resonances.map((r, idx) => (
            <Box key={idx} mb={1.5}>
              <Box display="flex" justifyContent="space-between" alignItems="baseline" mb={0.3}>
                <Typography variant="caption" sx={{ fontWeight: 600, color: resonanceColor }}>
                  Someone resonated
                </Typography>
                <Typography variant="caption" sx={{ color: mutedColor, fontSize: '0.72rem' }}>
                  {formatRelativeTime(r.occurredAt)}
                </Typography>
              </Box>
              <Typography variant="caption" sx={{ color: textColor, fontStyle: 'italic', lineHeight: 1.4, display: 'block' }}>
                "{r.bodyText}"
              </Typography>
            </Box>
          ))}

          {error && (
            <Typography variant="caption" sx={{ color: '#ef4444', display: 'block', mb: 1 }}>
              {error}
            </Typography>
          )}
        </Box>
      </Collapse>
    </Box>
  );
}

export default NoteResonances;
