/**
 * The API sends LocalDateTime without a zone ("2026-11-17T13:45:32"), which
 * JavaScript would interpret as local time. The server stores UTC, so we
 * append Z before parsing. Without this, every timestamp is off by the
 * viewer's UTC offset.
 */
export function parseServerTime(value: string): Date {
  const hasZone = /[zZ]$|[+-]\d{2}:?\d{2}$/.test(value);
  return new Date(hasZone ? value : `${value}Z`);
}

export function formatTime(value: string): string {
  return parseServerTime(value).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
}

export function formatDate(value: string): string {
  return parseServerTime(value).toLocaleDateString();
}
