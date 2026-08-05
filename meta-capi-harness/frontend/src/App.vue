<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';

type DeliveryStatus = 'CONFIG_REQUIRED' | 'SENT' | 'FAILED';
type Delivery = { id:number; eventId:string; value:number; currency:string; status:DeliveryStatus; httpStatus:number; response:string; attemptedAt:string; payload:Record<string, unknown> };

const configured = ref(false);
const online = ref(true);
const loading = ref(false);
const events = ref<Delivery[]>([]);
const latest = ref<Delivery | null>(null);
const error = ref('');
const form = ref({ orderId:`order-${Date.now().toString().slice(-6)}`, value:49.90, currency:'USD', email:'buyer@example.com', phone:'+1 415 555 2671' });
const payload = computed(() => latest.value?.payload && Object.keys(latest.value.payload).length ? JSON.stringify(latest.value.payload, null, 2) : '{\n  "event_name": "Purchase",\n  "event_id": "generated from order ID",\n  "user_data": "SHA-256 values appear after submission"\n}');

async function refresh(){
  try {
    const [statusResponse, eventsResponse] = await Promise.all([fetch('/api/status'), fetch('/api/events')]);
    if(!statusResponse.ok || !eventsResponse.ok) throw new Error('API unavailable');
    configured.value=(await statusResponse.json()).configured;
    events.value=await eventsResponse.json(); online.value=true;
  } catch { online.value=false; }
}

async function submit(){
  loading.value=true; error.value='';
  try {
    const response=await fetch('/api/purchases',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify(form.value)});
    if(!response.ok) throw new Error(await response.text());
    latest.value=await response.json(); await refresh();
  } catch(e){ error.value=e instanceof Error ? e.message : 'Request failed'; }
  finally{ loading.value=false; }
}

async function resend(id:number){
  loading.value=true; error.value='';
  try{
    const response=await fetch(`/api/purchases/${id}/resend`,{method:'POST'});
    if(!response.ok) throw new Error(await response.text());
    latest.value=await response.json(); await refresh();
  }catch(e){error.value=e instanceof Error ? e.message : 'Request failed'}finally{loading.value=false}
}

onMounted(refresh);
</script>

<template><main>
  <nav><a class="brand"><span>IF</span> Integration Forensics</a><div class="nav-meta"><i :class="{off:!online}"></i>{{online?'api online':'api offline'}}<b>03 / 03</b></div></nav>
  <header class="hero"><div><p class="eyebrow">META CONVERSIONS API / TEST EVENTS</p><h1>Trace the sale<br><em>beyond the browser.</em></h1><p>A compact server-side conversion lab: normalize customer data, hash it before transport, keep a stable deduplication ID, and expose the provider response.</p></div><aside><span>DELIVERY MODE</span><b>{{configured?'LIVE TEST EVENTS':'PAYLOAD PREVIEW'}}</b><p>{{configured?'Requests go to Meta Test Events.':'Add local Meta credentials to enable external delivery.'}}</p></aside></header>

  <section class="workbench">
    <form @submit.prevent="submit"><p class="eyebrow">SIMULATED CHECKOUT</p><h2>Build a Purchase event</h2><div class="grid"><label>ORDER ID<input v-model="form.orderId" required pattern="[A-Za-z0-9._:-]{3,100}"></label><label>VALUE<input v-model.number="form.value" required type="number" min="0.01" step="0.01"></label><label>CURRENCY<input v-model="form.currency" required pattern="[A-Za-z]{3}" maxlength="3"></label><label>EMAIL<input v-model="form.email" required type="email"></label><label class="wide">PHONE<input v-model="form.phone" required type="tel"></label></div><button :disabled="loading||!online">{{loading?'WORKING…':'CREATE + SEND EVENT'}}<span>→</span></button><p v-if="error" class="error">{{error}}</p><small>Plaintext email and phone are accepted only for this request and are never stored. The ledger persists their SHA-256 hashes.</small></form>
    <article class="payload"><header><span>REQUEST PAYLOAD</span><b v-if="latest" :class="latest.status.toLowerCase()">{{latest.status.replace('_',' ')}}</b></header><pre>{{payload}}</pre><footer><span>action_source</span><b>website</b><span>dedup key</span><b>event_id + event_name</b></footer></article>
  </section>

  <section class="pipeline"><p class="eyebrow">PRIVACY + DELIVERY PIPELINE</p><ol><li><b>01</b><strong>Normalize</strong><span>trim + lowercase email<br>digits-only phone</span></li><li><b>02</b><strong>SHA-256</strong><span>hash before transport<br>never log plaintext PII</span></li><li><b>03</b><strong>Construct</strong><span>value + currency<br>website action source</span></li><li><b>04</b><strong>Deliver</strong><span>Graph API v25.0<br>Test Events code</span></li></ol></section>

  <section class="ledger"><header><div><p class="eyebrow">DELIVERY LEDGER</p><h2>Provider evidence</h2></div><button class="ghost" @click="refresh">↻ REFRESH</button></header><div class="table"><div class="table-head"><span>EVENT / TIME</span><span>VALUE</span><span>DECISION</span><span>PROVIDER RESPONSE</span><span>ACTION</span></div><div v-if="!events.length" class="empty">No purchase events recorded yet.</div><article v-for="event in events" :key="event.id"><div><b>{{event.eventId}}</b><time>{{new Date(event.attemptedAt).toLocaleString()}}</time></div><code>{{event.currency}} {{event.value}}</code><span class="badge" :class="event.status.toLowerCase()">{{event.status}}</span><code>{{event.httpStatus||'—'}} · {{event.response}}</code><button class="ghost" :disabled="loading" @click="resend(event.id)">same ID ↗</button></article></div></section>

  <section class="notes"><div><p class="eyebrow">WHY THESE FIELDS</p><h2>Every choice is inspectable.</h2></div><div><article><b>event_id</b><p>The order ID remains stable across retries, letting browser Pixel and server CAPI events describe the same conversion without double counting.</p></article><article><b>action_source</b><p>This lab simulates a website checkout, so the source is explicitly <code>website</code> rather than inferred.</p></article><article><b>test_event_code</b><p>External calls are restricted to Meta Test Events. Missing credentials produce CONFIG REQUIRED, never a fabricated success.</p></article></div></section>
  <footer><span>SPRING BOOT · MYSQL · VUE · META CAPI</span><span>No access tokens or plaintext customer data are rendered in the ledger.</span></footer>
</main></template>
