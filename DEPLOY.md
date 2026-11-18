# Deploy for free (permanent URL)

Two options. **A** needs no credit card but sleeps when idle. **B** is always on
but asks for a card at signup (never charged).

---

## Option A — Render (app) + Aiven (MySQL) · ₹0, no card

### 1. Database (5 min)

1. https://aiven.io → sign up (free plan, no card)
2. **Create service → MySQL** → plan **Free** → region closest to you (e.g. `google-asia-south1` for India)
3. Wait for *Running*, then copy from **Overview → Connection information**:
   - Host, Port, User (`avnadmin`), Password, Database (`defaultdb`)
4. Build the JDBC URL — note `sslMode=REQUIRED`:

```
jdbc:mysql://HOST:PORT/defaultdb?sslMode=REQUIRED&connectionTimeZone=UTC&forceConnectionTimeZoneToSession=true&characterEncoding=utf8
```

### 2. App (10 min)

1. Push this repo to GitHub (private is fine)
2. https://render.com → **New → Blueprint** → pick the repo → it reads `render.yaml`
3. Paste the three secrets:
   - `DB_URL` = the JDBC URL above
   - `DB_USER` = `avnadmin`
   - `DB_PASSWORD` = from Aiven
4. Deploy. First build takes ~6–8 min (Maven + Docker).

You get `https://flatmate-api-xxxx.onrender.com` — the **whole app**, not just an API.

### Know the trade-off

Render free sleeps after 15 minutes idle. The first visitor waits ~40 s, then it
is fast. Fine for a resume link; not fine for real users.

---

## Option B — Oracle Cloud Always Free · ₹0 forever, always on

1. https://cloud.oracle.com/free → sign up (card for identity check, never charged)
2. **Create instance** → shape `VM.Standard.A1.Flex` (Ampere ARM, up to 4 OCPU / 24 GB free)
3. Image: **Ubuntu 22.04**, add your SSH key
4. Open ports 80 and 443 in the VCN security list
5. On the VM:

```bash
sudo apt update && sudo apt install -y openjdk-21-jre mysql-server caddy git
sudo mysql -e "CREATE DATABASE flatmate CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"
sudo mysql -e "CREATE USER 'flatmate'@'localhost' IDENTIFIED BY 'STRONG_PASSWORD';"
sudo mysql -e "GRANT ALL ON flatmate.* TO 'flatmate'@'localhost';"

# copy the jar up
scp backend/target/flatmate-api-0.1.0.jar ubuntu@VM_IP:~

# run it
nohup java -jar flatmate-api-0.1.0.jar > app.log 2>&1 &
```

6. Caddy for HTTPS in one line (`/etc/caddy/Caddyfile`):

```
yourdomain.com {
    reverse_proxy localhost:8080
}
```

`sudo systemctl reload caddy` — Caddy gets the certificate automatically.

Point a cheap domain (or a free DuckDNS subdomain) at the VM IP.

---

## Both options: build the jar

```bash
cd backend && mvn -DskipTests package
# target/flatmate-api-0.1.0.jar  — includes the whole React app
```

## Environment variables the app understands

| Variable | Meaning |
|---|---|
| `DB_URL` | full JDBC URL (default: local MySQL) |
| `DB_USER` / `DB_PASSWORD` | database credentials |
| `DB_POOL` | Hikari pool size — set `3` on a 512 MB instance |
| `JWT_SECRET` | ≥32 chars; **must** change in production |
| `PORT` | injected by Render; defaults to 8080 |

## After deploying

1. Open the URL — the login screen should render
2. Register an account, complete the profile
3. Check `https://YOUR-URL/actuator/health` returns `{"status":"UP"}`

Flyway runs the six migrations automatically on first boot.
