#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# Start Flatmate locally and expose it on a public HTTPS URL.
#
#   ./run.sh          start API + tunnel (uses the already-built UI)
#   ./run.sh --build  rebuild the UI first, then start
#
# The built React app lives in backend/src/main/resources/static, so the API
# serves the whole app on one origin — no CORS, and wss:// just works.
# ---------------------------------------------------------------------------
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="${TMPDIR:-/tmp}"
# an already-running tunnel may have been started from a different shell
TUNNEL_LOG="$LOG_DIR/tunnel.log"
[[ -f "$TUNNEL_LOG" ]] || TUNNEL_LOG="/tmp/tunnel.log"

if [[ "${1:-}" == "--build" ]]; then
  echo "→ building UI"
  (cd "$ROOT/frontend" && npm run build >/dev/null)
  rm -rf "$ROOT/backend/src/main/resources/static"
  mkdir -p "$ROOT/backend/src/main/resources/static"
  cp -R "$ROOT/frontend/dist/"* "$ROOT/backend/src/main/resources/static/"
  echo "✓ UI copied into the API"
fi

if curl -sf -m 2 http://localhost:8080/actuator/health >/dev/null 2>&1; then
  echo "✓ API already running on :8080"
else
  echo "→ starting API (Flyway runs on boot)"
  (cd "$ROOT/backend" && nohup mvn -B spring-boot:run > "$LOG_DIR/flatmate-run.log" 2>&1 &)
  for _ in $(seq 1 90); do
    curl -sf -m 2 http://localhost:8080/actuator/health >/dev/null 2>&1 && break
    sleep 1
  done
  curl -sf -m 2 http://localhost:8080/actuator/health >/dev/null || {
    echo "✗ API failed to start — see $LOG_DIR/flatmate-run.log"; exit 1;
  }
  echo "✓ API up"
fi

if pgrep -f "cloudflared tunnel --url http://localhost:8080" >/dev/null 2>&1; then
  echo "✓ tunnel already running"
  URL=$(grep -oE 'https://[a-z0-9-]+\.trycloudflare\.com' "$TUNNEL_LOG" 2>/dev/null | tail -1 || true)
else
  echo "→ opening public tunnel"
  TUNNEL_LOG="$LOG_DIR/tunnel.log"
  nohup cloudflared tunnel --url http://localhost:8080 --no-autoupdate --protocol http2 > "$TUNNEL_LOG" 2>&1 &
  for _ in $(seq 1 40); do
    URL=$(grep -oE 'https://[a-z0-9-]+\.trycloudflare\.com' "$TUNNEL_LOG" 2>/dev/null | head -1 || true)
    [[ -n "${URL:-}" ]] && break
    sleep 1
  done
fi

echo
echo "  Local   http://localhost:8080"
echo "  Public  ${URL:-<tunnel did not start — see $TUNNEL_LOG>}"
echo
echo "  Demo accounts (password: secret123)"
echo "    asha@example.com    bilal@example.com    esha@example.com"
echo "    farhan@example.com  gita@example.com     chetan@example.com"
echo
echo "  The public URL lives only while this machine and cloudflared stay awake."
echo "  If the URL does not open on THIS Mac, flush the stale DNS cache:"
echo "    sudo dscacheutil -flushcache; sudo killall -HUP mDNSResponder"
