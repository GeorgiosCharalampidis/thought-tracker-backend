import React, { useCallback, useEffect, useRef, useState } from 'react';
import {
  Badge,
  Box,
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

function NotificationBell({ isDarkMode, onNoteClick }: NotificationBellProps) {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [anchorEl, setAnchorEl] = useState<HTMLButtonElement | null>(null);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const unseenCount = notifications.filter(n => !n.seen).length;

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

  const handleItemClick = async (n: Notification) => {
    // Mark this specific notification as seen
    if (!n.seen) {
      try {
        await axios.post(`/api/notifications/${n.commentId}/mark-seen`);
        setNotifications(prev =>
          prev.map(item => item.commentId === n.commentId ? { ...item, seen: true } : item)
        );
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

  return (
    <>
      <Tooltip title="Notifications">
        <IconButton
          onClick={handleOpen}
          sx={{
            backgroundColor: bgColor,
            color: iconColor,
            border: `1px solid ${borderColor}`,
            '&:hover': { backgroundColor: isDarkMode ? '#404140' : '#f7fafc' },
            transition: 'all 0.2s ease',
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
          },
        }}
      >
        <Box px={2} pt={2} pb={1}>
          <Typography variant="subtitle2" sx={{ fontWeight: 600, color: textColor, mb: 0.5 }}>
            Notifications
          </Typography>
        </Box>

        {notifications.length === 0 && (
          <Box px={2} pb={2.5}>
            <Typography variant="body2" sx={{ color: mutedColor }}>
              You're all caught up.
            </Typography>
          </Box>
        )}

        {notifications.map((n, idx) => (
          <Box
            key={n.commentId}
            px={2}
            py={1.5}
            onClick={() => handleItemClick(n)}
            sx={{
              cursor: 'pointer',
              backgroundColor: n.seen ? 'transparent' : unseenBg,
              borderTop: idx === 0 ? `1px solid ${borderColor}` : 'none',
              borderBottom: `1px solid ${borderColor}`,
              borderLeft: n.seen ? '3px solid transparent' : `3px solid ${unseenAccent}`,
              '&:hover': { backgroundColor: hoverBg },
              transition: 'background-color 0.15s ease',
            }}
          >
            <Box display="flex" justifyContent="space-between" alignItems="baseline" mb={0.4}>
              <Typography
                variant="caption"
                sx={{ fontWeight: n.seen ? 400 : 700, color: n.seen ? seenTextColor : (isDarkMode ? '#a5b4fc' : '#4f46e5') }}
              >
                {n.commenterUsername}
              </Typography>
              <Typography variant="caption" sx={{ color: mutedColor, fontSize: '0.72rem' }}>
                {formatRelativeTime(n.commentedAt)}
              </Typography>
            </Box>
            <Typography variant="body2" sx={{ color: n.seen ? seenTextColor : textColor, mb: 0.5 }}>
              {n.commentText}
            </Typography>
            <Typography
              variant="caption"
              sx={{ color: n.seen ? mutedColor : previewColor, fontStyle: 'italic', lineHeight: 1.4, display: 'block' }}
            >
              on: {n.notePreview}
            </Typography>
          </Box>
        ))}
      </Popover>
    </>
  );
}

export default NotificationBell;
