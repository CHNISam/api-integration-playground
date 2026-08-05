<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';

type Session = { authenticated: boolean; providerConfigured: boolean; login?: string; displayName?: string; avatarUrl?: string };
type Trace = { time: string; label: string; detail: string; status: 'ok' | 'warn' | 'idle' };

const session = ref<Session>({ authenticated: false, providerConfigured: false });
const loading = ref(true);
const labBusy = ref(false);
const traces = ref<Trace[]>([{ time: 'READY', label: 'Browser session', detail: 'Waiting for an authorization attempt', status: 'idle' }]);
const oauthResult = new URLSearchParams(location.search).get('oauth');
const resultMessage = computed(() => ({
  success: 'GitHub identity verified and local session established.',
  denied: 'Authorization was cancelled. No local session was created.',
  invalid_state: 'Callback rejected: the state value was missing, expired, or changed.',
  provider_error: 'GitHub returned an error while exchanging the code or loading the profile.',
}[oauthResult ?? '']));

async function loadSession() {
  loading.value = true;
  try {
    session.value = await fetch('/api/session', { credentials: 'include' }).then(r => r.json());
  } finally { loading.value = false; }
}

function startLogin() { location.href = '/api/oauth/github/start'; }

async function logout() {
  await fetch('/api/logout', { method: 'POST', credentials: 'include' });
  traces.value.unshift({ time: now(), label: 'Session cleared', detail: 'Server-side session invalidated', status: 'ok' });
  await loadSession();
}

async function runRefreshLab() {
  labBusy.value = true;
  await fetch('/api/token-lab/seed-expired', { method: 'POST', credentials: 'include' });
  traces.value.unshift({ time: now(), label: 'Expired token loaded', detail: 'Protected resource would return 401 without refresh', status: 'warn' });
  const result = await fetch('/api/token-lab/resource', { credentials: 'include' }).then(r => r.json());
  traces.value.unshift({ time: now(), label: 'Resource returned', detail: `Automatic refresh: ${result.refreshed ? 'executed' : 'not needed'} · ${result.accessTokenPreview}`, status: 'ok' });
  labBusy.value = false;
}

function now() { return new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' }); }
onMounted(loadSession);
</script>

<template>
  <main>
    <nav><a class="brand" href="/"><span>IF</span> Integration Forensics</a><div class="nav-meta"><i></i> lab online <b>01 / 03</b></div></nav>

    <section class="hero">
      <div class="hero-copy">
        <p class="eyebrow">OAUTH 2.0 / AUTHORIZATION CODE</p>
        <h1>Trust the callback.<br><em>Verify everything.</em></h1>
        <p class="lede">A working GitHub sign-in flow with single-use CSRF state, graceful provider failures, durable browser sessions, and an observable token-refresh lab.</p>
        <div class="actions" v-if="!loading">
          <button v-if="!session.authenticated" class="primary" :disabled="!session.providerConfigured" @click="startLogin"><span>Continue with GitHub</span><b>↗</b></button>
          <button v-else class="primary profile"><img v-if="session.avatarUrl" :src="session.avatarUrl" alt=""><span>{{ session.displayName }}<small>@{{ session.login }}</small></span></button>
          <button v-if="session.authenticated" class="secondary" @click="logout">End session</button>
        </div>
        <p v-if="!loading && !session.providerConfigured" class="config-note"><span>CONFIG</span> Add GitHub credentials to the backend `.env` to enable the real provider flow.</p>
        <p v-if="resultMessage" class="result" :class="oauthResult"><b>{{ oauthResult?.replace('_', ' ') }}</b>{{ resultMessage }}</p>
      </div>
      <div class="flow-card">
        <div class="card-head"><span>REQUEST TRACE</span><span>github.com</span></div>
        <ol>
          <li class="active"><b>01</b><div><strong>Issue state</strong><small>256-bit random · 10 min TTL</small></div><i>LOCAL</i></li>
          <li><b>02</b><div><strong>Authorize</strong><small>Minimal identity scopes</small></div><i>302</i></li>
          <li><b>03</b><div><strong>Validate callback</strong><small>Constant-time · single use</small></div><i>GATE</i></li>
          <li><b>04</b><div><strong>Exchange + profile</strong><small>Secret remains server-side</small></div><i>API</i></li>
          <li><b>05</b><div><strong>Persist session</strong><small>HttpOnly · SameSite=Lax</small></div><i>DONE</i></li>
        </ol>
      </div>
    </section>

    <section class="diagnostics">
      <header><div><p class="eyebrow">FAILURE ATLAS</p><h2>What breaks, and where to look.</h2></div><p>Each case maps the visible symptom to the control that catches it.</p></header>
      <div class="failure-grid">
        <article><span class="code">E-REDIRECT</span><h3>Redirect URI mismatch</h3><p>Provider rejects before callback. Compare the registered callback character-for-character with <code>GITHUB_REDIRECT_URI</code>.</p><footer><i class="amber"></i> Provider boundary</footer></article>
        <article><span class="code">E-STATE</span><h3>State changed or missing</h3><p>Callback returns to the UI as <code>invalid_state</code>. The issued value is consumed even when validation fails.</p><footer><i class="red"></i> Request rejected</footer></article>
        <article><span class="code">E-TOKEN</span><h3>Expired access token</h3><p>A protected request would fail with 401. The refresh lab replaces the expired token, then retries with a safe preview.</p><footer><i class="green"></i> Recoverable</footer></article>
        <article><span class="code">E-DENIED</span><h3>User cancels consent</h3><p>The provider's <code>error</code> callback becomes a clear UI message—never a blank page or an exposed stack trace.</p><footer><i class="amber"></i> Graceful exit</footer></article>
      </div>
    </section>

    <section class="token-lab">
      <div><p class="eyebrow">CONTROLLED EXPERIMENT</p><h2>Watch an expired token recover.</h2><p>This local provider exists because standard GitHub OAuth tokens do not expose a normal refresh-token flow. It isolates the refresh algorithm without pretending GitHub supplied the token.</p><button class="primary" :disabled="labBusy" @click="runRefreshLab"><span>{{ labBusy ? 'Running trace…' : 'Run expiry → refresh' }}</span><b>→</b></button></div>
      <div class="terminal"><div class="terminal-head"><span></span><span></span><span></span><b>token-lifecycle.log</b></div><ul><li v-for="trace in traces" :key="trace.time + trace.label" :class="trace.status"><time>{{ trace.time }}</time><div><b>{{ trace.label }}</b><small>{{ trace.detail }}</small></div></li></ul></div>
    </section>

    <footer class="page-footer"><span>SPRING BOOT · VUE 3 · MYSQL</span><span>No provider secrets cross the browser boundary.</span></footer>
  </main>
</template>
