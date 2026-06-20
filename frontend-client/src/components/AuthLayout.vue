<template>
  <div class="auth-shell">
    <div class="auth-bg" aria-hidden="true">
      <div class="auth-bg-blob auth-bg-blob-a"></div>
      <div class="auth-bg-blob auth-bg-blob-b"></div>
    </div>

    <div class="auth-panel">
      <div class="auth-brand">
        <div class="brand-mark">🍜</div>
        <div>
          <p class="brand-kicker">OrderSys</p>
          <h1 class="brand-title">{{ title }}</h1>
          <p v-if="subtitle" class="brand-subtitle">{{ subtitle }}</p>
        </div>
      </div>

      <div class="auth-card card">
        <slot />
      </div>

      <p v-if="footer" class="auth-footer">{{ footer }}</p>
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  footer: { type: String, default: '' },
})
</script>

<style scoped>
.auth-shell {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px 20px;
  position: relative;
  overflow: hidden;
  background: var(--bg);
}

.auth-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.auth-bg-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.45;
}

.auth-bg-blob-a {
  width: 280px;
  height: 280px;
  background: var(--ticket);
  top: -80px;
  right: -40px;
}

.auth-bg-blob-b {
  width: 220px;
  height: 220px;
  background: #dbeafe;
  bottom: -60px;
  left: -30px;
}

.auth-panel {
  width: 100%;
  max-width: 420px;
  display: flex;
  flex-direction: column;
  gap: 20px;
  position: relative;
  z-index: 1;
}

.auth-brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-mark {
  width: 52px;
  height: 52px;
  border-radius: var(--radius);
  background: var(--ticket);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  box-shadow: var(--shadow-sm);
  flex-shrink: 0;
}

.brand-kicker {
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--ink-60);
  margin-bottom: 2px;
}

.brand-title {
  font-size: 24px;
  line-height: 1.15;
}

.brand-subtitle {
  margin-top: 4px;
  font-size: 14px;
  color: var(--ink-60);
}

.auth-card {
  padding: 28px 24px;
}

.auth-footer {
  text-align: center;
  font-size: 12px;
  color: var(--ink-30);
  font-family: var(--font-mono);
}
</style>
