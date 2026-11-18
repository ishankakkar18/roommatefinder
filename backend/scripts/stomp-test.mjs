// Raw STOMP-over-WebSocket test. Node 25 has a global WebSocket, so no deps.
// usage: node stomp-test.mjs <bilalToken> <conversationId> <ashaToken>
//
// Order matters: a rejected SUBSCRIBE makes the server send ERROR and close the
// session, so the blocking test must run LAST.
const [, , bilalToken, cid, ashaToken] = process.argv;
const WS_URL = 'ws://localhost:8080/ws';
const HTTP = 'http://localhost:8080';

const frame = (cmd, headers = {}, body = '') =>
  cmd + '\n' +
  Object.entries(headers).map(([k, v]) => `${k}:${v}`).join('\n') +
  '\n\n' + body + '\0';

function open(token) {
  return new Promise((resolve) => {
    const ws = new WebSocket(WS_URL);
    const received = [];
    let connected = false;

    ws.onopen = () => {
      const headers = { 'accept-version': '1.2', host: 'localhost', 'heart-beat': '0,0' };
      if (token) headers.Authorization = `Bearer ${token}`;
      ws.send(frame('CONNECT', headers));
    };
    ws.onmessage = (ev) => {
      const data = String(ev.data);
      received.push(data);
      if (data.startsWith('CONNECTED')) connected = true;
    };
    ws.onerror = () => {};
    ws.onclose = () => {};

    setTimeout(() => resolve({ ws, received, connected }), 1500);
  });
}

const wait = (ms) => new Promise((r) => setTimeout(r, ms));
let failures = 0;
const check = (name, ok, extra = '') => {
  if (!ok) failures++;
  console.log(`${ok ? 'PASS' : 'FAIL'}  ${name}${extra ? '  ' + extra : ''}`);
};

// 1. CONNECT without a token must be rejected
const anon = await open(null);
await wait(600);
check('anonymous CONNECT rejected',
  !anon.connected || anon.received.some((m) => m.startsWith('ERROR')),
  `frames=${anon.received.length}`);
anon.ws?.close();

// 2. valid token connects
const me = await open(bilalToken);
check('authenticated CONNECT accepted', me.connected);

// 3. subscribing to your own conversation is allowed
me.ws.send(frame('SUBSCRIBE', { id: 'sub-own', destination: `/topic/conversations/${cid}` }));
await wait(500);
check('subscribe to own conversation allowed',
  !me.received.some((m) => m.startsWith('ERROR')));

// 4. a message sent over HTTP must arrive on the socket (AFTER_COMMIT broadcast)
const clientMsgId = crypto.randomUUID();
const posted = await fetch(`${HTTP}/api/conversations/${cid}/messages`, {
  method: 'POST',
  headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${ashaToken}` },
  body: JSON.stringify({ clientMsgId, body: 'realtime broadcast check' }),
});
check('HTTP send accepted', posted.status === 201, `status=${posted.status}`);

await wait(1200);
const delivered = me.received.find((m) => m.startsWith('MESSAGE') && m.includes(clientMsgId));
check('HTTP send delivered over STOMP', Boolean(delivered));

if (delivered) {
  const body = delivered.split('\n\n')[1]?.replace(/\0$/, '');
  try {
    const json = JSON.parse(body);
    check('broadcast payload matches the POSTed message',
      json.clientMsgId === clientMsgId && typeof json.id === 'number');
  } catch {
    check('broadcast payload is valid JSON', false, body?.slice(0, 80));
  }
}

// 5. LAST: subscribing to someone else's conversation must be rejected
const foreign = '11111111-2222-3333-4444-555555555555';
me.ws.send(frame('SUBSCRIBE', { id: 'sub-foreign', destination: `/topic/conversations/${foreign}` }));
await wait(900);
const rejected = me.received.find((m) => m.startsWith('ERROR') && m.includes(foreign));
check('subscribe to foreign conversation BLOCKED', Boolean(rejected));

me.ws?.close();
console.log(failures === 0 ? '\nALL STOMP CHECKS PASSED' : `\n${failures} STOMP CHECK(S) FAILED`);
process.exit(failures === 0 ? 0 : 1);
