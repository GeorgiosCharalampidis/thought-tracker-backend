import React, { useCallback, useEffect, useRef, useState } from 'react';
import {
  Badge,
  Box,
  Button,
  IconButton,
  Popover,
  Tooltip,
  Typography,
} from '@mui/material';
import NotificationsNoneIcon from '@mui/icons-material/NotificationsNone';
import axios from 'axios';
import { Notification } from '../types';

interface NotificationBellProps {
  isDarkMode: boolean;
  isMobile?: boolean;
  onNoteClick: (noteId: number) => void;
}

const POLL_INTERVAL_MS = 30_000;

function formatRelativeTime(iso: string): string {
  const diffMs = Date.now() - new Date(iso).getTime();
  const mins = Math.floor(diffMs / 60_000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins}m ago`;
  const hours = Math.floor(mins / 60);
  if (hours < 24) return `${hours}h ago`;
  return `${Math.floor(hours / 24)}d ago`;
}

function NotificationBell({ isDarkMode, isMobile, onNoteClick }: NotificationBellProps) {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [filter, setFilter] = useState<'ALL' | 'COMMENT' | 'RESONANCE'>('ALL');
  const [clearedAt, setClearedAt] = useState<number>(() =>
    parseInt(localStorage.getItem('notificationsClearedAt') || '0', 10)
  );
  const [anchorEl, setAnchorEl] = useState<HTMLButtonElement | null>(null);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const visibleNotifications = notifications.filter(n =>
    !n.seen || new Date(n.occurredAt).getTime() > clearedAt
  );
  const unseenCount = visibleNotifications.filter(n => !n.seen).length;
  const filteredNotifications = filter === 'ALL' ? visibleNotifications : visibleNotifications.filter(n => n.type === filter);

  const fetchNotifications = useCallback(async () => {
    try {
      const res = await axios.get<Notification[]>('/api/notifications');
      setNotifications(res.data);
    } catch {
      // Silently ignore — network hiccup or session expired
    }
  }, []);

  useEffect(() => {
    fetchNotifications();
    intervalRef.current = setInterval(fetchNotifications, POLL_INTERVAL_MS);
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [fetchNotifications]);

  const handleOpen = (e: React.MouseEvent<HTMLButtonElement>) => {
    setAnchorEl(e.currentTarget);
  };

  const handleClose = () => setAnchorEl(null);

  const handleClearAll = async () => {
    const now = Date.now();
    setClearedAt(now);
    localStorage.setItem('notificationsClearedAt', String(now));
    try {
      await axios.delete('/api/notifications');
    } catch {
      // Badge will correct on next poll
    }
  };

  const handleItemClick = async (n: Notification) => {
    if (!n.seen) {
      setNotifications(prev =>
        prev.map(item => item.type === n.type && item.id === n.id ? { ...item, seen: true } : item)
      );
      try {
        await axios.post(`/api/notifications/${n.id}/mark-seen?type=${n.type}`);
      } catch {
        // Badge will correct itself on next poll
      }
    }
    handleClose();
    onNoteClick(n.noteId);
  };

  const open = Boolean(anchorEl);

  const iconColor = isDarkMode ? '#cbd5e1' : '#475569';
  const bgColor = isDarkMode ? '#2d2e2d' : '#ffffff';
  const borderColor = isDarkMode ? '#404140' : '#e2e8f0';
  const mutedColor = isDarkMode ? '#94a3b8' : '#64748b';
  const textColor = isDarkMode ? '#e2e8f0' : '#374151';
  const previewColor = isDarkMode ? '#cbd5e1' : '#4b5563';
  const hoverBg = isDarkMode ? 'rgba(255,255,255,0.05)' : 'rgba(15,23,42,0.04)';
  const unseenBg = isDarkMode ? 'rgba(165,180,252,0.14)' : 'rgba(79,70,229,0.07)';
  const unseenAccent = isDarkMode ? '#818cf8' : '#4f46e5';
  const seenTextColor = isDarkMode ? '#94a3b8' : '#9ca3af';
  const resonanceAccent = isDarkMode ? '#34d399' : '#059669';

  return (
    <>
      <Tooltip title="Notifications">
        <IconButton
          onClick={handleOpen}
          size={isMobile ? 'small' : 'medium'}
          sx={isMobile ? {
            color: iconColor,
          } : {
            backgroundColor: 'transparent',
            color: iconColor,
            border: '1px solid transparent',
            '&:hover': {
              backgroundColor: isDarkMode ? '#404140' : '#f7fafc',
              borderColor: borderColor,
            },
            transition: 'background-color 0.2s ease, border-color 0.2s ease',
          }}
        >
          <Badge
            badgeContent={unseenCount}
            max={99}
            sx={{
              '& .MuiBadge-badge': {
                backgroundColor: '#ef4444',
                color: '#fff',
                fontSize: '0.68rem',
                minWidth: 16,
                height: 16,
              },
            }}
          >
            <NotificationsNoneIcon fontSize="small" />
          </Badge>
        </IconButton>
      </Tooltip>

      <Popover
        open={open}
        anchorEl={anchorEl}
        onClose={handleClose}
        disableScrollLock
        anchorOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        transformOrigin={{ vertical: 'top', horizontal: 'right' }}
        PaperProps={{
          sx: {
            mt: 1,
            width: 320,
            maxHeight: 420,
            overflowY: 'auto',
            backgroundColor: bgColor,
            border: `1px solid ${borderColor}`,
            borderRadius: 2,
            boxShadow: isDarkMode ? '0 8px 24px rgba(0,0,0,0.5)' : '0 8px 24px rgba(0,0,0,0.10)',
            '&::-webkit-scrollbar': { width: 4 },
            '&::-webkit-scrollbar-track': { background: 'transparent' },
            '&::-webkit-scrollbar-thumb': {
              background: isDarkMode ? 'rgba(255,255,255,0.12)' : 'rgba(15,23,42,0.12)',
              borderRadius: 4,
            },
            '&::-webkit-scrollbar-thumb:hover': {
              background: isDarkMode ? 'rgba(255,255,255,0.22)' : 'rgba(15,23,42,0.22)',
            },
          },
        }}
      >
        {visibleNotifications.length === 0 ? (
          <Box px={2} py={2}>
            <Typography variant="body2" sx={{ color: mutedColor }}>
              You're all caught up.
            </Typography>
          </Box>
        ) : (
          <Box px={2} pt={1.5} pb={1} display="flex" justifyContent="space-between" alignItems="center" sx={{ borderBottom: `1px solid ${borderColor}` }}>
            <Box display="flex" gap={0.75}>
              {(['ALL', 'COMMENT', 'RESONANCE'] as const).map(f => (
                <Box
                  key={f}
                  onClick={() => setFilter(f)}
                  sx={{
                    px: 1.2,
                    py: 0.3,
                    borderRadius: 999,
                    fontSize: '0.72rem',
                    fontWeight: filter === f ? 600 : 400,
                    cursor: 'pointer',
                    userSelect: 'none',
                    backgroundColor: filter === f
                      ? (f === 'RESONANCE' ? resonanceAccent : f === 'COMMENT' ? unseenAccent : (isDarkMode ? 'rgba(255,255,255,0.1)' : 'rgba(15,23,42,0.08)'))
                      : 'transparent',
                    color: filter === f
                      ? (f === 'RESONANCE' || f === 'COMMENT' ? '#fff' : textColor)
                      : mutedColor,
                    transition: 'all 0.15s ease',
                    '&:hover': { color: textColor },
                  }}
                >
                  {f === 'ALL' ? 'All' : f === 'COMMENT' ? 'Comments' : 'Resonances'}
                </Box>
              ))}
            </Box>
            <Button
              size="small"
              onClick={handleClearAll}
              sx={{
                textTransform: 'none',
                fontSize: '0.75rem',
                color: mutedColor,
                minWidth: 0,
                px: 0,
                '&:hover': { backgroundColor: 'transparent', color: textColor },
              }}
            >
              Clear all
            </Button>
          </Box>
        )}

        {filteredNotifications.length === 0 && visibleNotifications.length > 0 && (
          <Box px={2} py={1.5}>
            <Typography variant="body2" sx={{ color: mutedColor }}>
              No {filter === 'COMMENT' ? 'comments' : 'resonances'} yet.
            </Typography>
          </Box>
        )}

        {filteredNotifications.map((n, idx) => (
          <Box
            key={`${n.type}-${n.id}`}
            px={2}
            py={1.5}
            onClick={() => handleItemClick(n)}
            sx={{
              cursor: 'pointer',
              backgroundColor: n.seen ? 'transparent' : unseenBg,
              borderTop: idx === 0 ? `1px solid ${borderColor}` : 'none',
              borderBottom: `1px solid ${borderColor}`,
              borderLeft: n.seen ? '3px solid transparent' : `3px solid ${n.type === 'RESONANCE' ? resonanceAccent : unseenAccent}`,
              '&:hover': { backgroundColor: hoverBg },
              transition: 'background-color 0.15s ease',
            }}
          >
            <Box display="flex" justifyContent="space-between" alignItems="baseline" mb={0.4}>
              <Typography
                variant="caption"
                sx={{
                  fontWeight: n.seen ? 400 : 700,
                  color: n.seen ? seenTextColor : n.type === 'RESONANCE' ? resonanceAccent : (isDarkMode ? '#a5b4fc' : '#4f46e5'),
                }}
              >
                {n.type === 'RESONANCE' ? 'Resonance' : n.actorUsername}
              </Typography>
              <Typography variant="caption" sx={{ color: mutedColor, fontSize: '0.72rem' }}>
                {formatRelativeTime(n.occurredAt)}
              </Typography>
            </Box>
            <Typography
              variant="caption"
              sx={{ color: n.seen ? mutedColor : previewColor, fontStyle: 'italic', lineHeight: 1.4, display: 'block', mb: 0.5 }}
            >
              your thought: {n.notePreview}
            </Typography>
            {n.type === 'RESONANCE' ? (
              <Typography
                variant="caption"
                sx={{ color: n.seen ? mutedColor : previewColor, lineHeight: 1.4, display: 'block' }}
              >
                their thought: {n.bodyText}
              </Typography>
            ) : (
              <Typography variant="body2" sx={{ color: n.seen ? seenTextColor : textColor }}>
                {n.bodyText}
              </Typography>
            )}
          </Box>
        ))}
      </Popover>
    </>
  );
}

export default NotificationBell;
