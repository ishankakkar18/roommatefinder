export default function Safety() {
  const rules = [
    ['Never pay before you visit', 'No deposit, no "booking amount", no token money for a room you have not stood inside. This is the most common scam and it is always irreversible.'],
    ['Visit in person, ideally with someone', 'Take a friend or family member. If the flatmate refuses a visit, that is the answer.'],
    ['Meet in a public place first', 'A café or a building lobby, not a private address, for the first meeting.'],
    ['Verify identity yourself', 'Ask for a college ID, work ID or Aadhaar last-4 at the visit. We do not verify documents for you.'],
    ['Never share OTPs, UPI PINs or bank details', 'Nobody legitimate needs your OTP. Ever.'],
    ['Urgency is a red flag', 'Pressure to decide today, "three other people want it", or a price far below the market is a script, not luck.'],
  ];

  const flags = [
    'Rent noticeably below the area average for similar rooms',
    'Photos that look like a hotel or a stock listing',
    'Owner "abroad" or "in the army" and cannot meet',
    'Asks you to move the conversation to WhatsApp immediately',
    'Wants a deposit before any visit or video walkthrough',
    'Refuses to video call or share a live photo of the room',
    'Requests money for "registration", "police verification" or "maintenance" upfront',
  ];

  return (
    <>
      <header className="page-head">
        <div>
          <h1>Safety</h1>
          <p className="sub">
            We connect people. We do not verify them — so treat everyone as a stranger until proven otherwise.
          </p>
        </div>
        <span className="chip chip-warn">Read before you meet</span>
      </header>

      <div className="stack">
        <div className="card" style={{ borderColor: 'rgba(245,181,68,0.3)', background: 'var(--warn-soft)' }}>
          <h3 style={{ color: 'var(--warn)' }}>The one rule that matters</h3>
          <p className="dim" style={{ marginTop: 6 }}>
            Money changes hands <strong>after</strong> you have visited the room, met the people living there,
            and checked the agreement. Anyone who wants money earlier is a scammer, however convincing.
          </p>
        </div>

        <section className="stack">
          <div className="section-title">Ground rules</div>
          <div className="safety-grid">
            {rules.map(([title, body], index) => (
              <div className="safety-item rise" key={title} style={{ ['--i' as string]: index }}>
                <span className="safety-num">{index + 1}</span>
                <div>
                  <h3>{title}</h3>
                  <p>{body}</p>
                </div>
              </div>
            ))}
          </div>
        </section>

        <section className="card stack">
          <div className="section-title">Red flags</div>
          <div className="flags">
            {flags.map((flag) => (
              <div className="flag" key={flag}>
                <span className="bullet">✕</span>
                <span>{flag}</span>
              </div>
            ))}
          </div>
        </section>

        <section className="card stack">
          <div className="section-title">What this platform does and does not do</div>
          <div className="detail-list">
            <div className="detail-item">
              <dt>We show</dt>
              <dd>Compatibility scores and the reasons behind them</dd>
            </div>
            <div className="detail-item">
              <dt>We gate</dt>
              <dd>Chat only opens after both people accept</dd>
            </div>
            <div className="detail-item">
              <dt>We do not</dt>
              <dd>Run background or police checks</dd>
            </div>
            <div className="detail-item">
              <dt>We do not</dt>
              <dd>Verify identity documents</dd>
            </div>
            <div className="detail-item">
              <dt>We do not</dt>
              <dd>Handle rent, deposits or payments</dd>
            </div>
            <div className="detail-item">
              <dt>We do not</dt>
              <dd>Guarantee anyone is who they say they are</dd>
            </div>
          </div>
        </section>

        <p className="muted">
          If someone behaves badly, stop replying and report them. Nothing here replaces meeting in person and
          trusting your own judgement.
        </p>
      </div>
    </>
  );
}
