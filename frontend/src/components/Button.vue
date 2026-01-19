<template>
  <button
    :class="['btn', `btn-${variant}`, { 'btn-loading': loading, 'btn-block': block }]"
    :disabled="disabled || loading"
    @click="handleClick"
  >
    <span v-if="loading" class="btn-spinner"></span>
    <slot></slot>
  </button>
</template>

<script setup>
defineProps({
  variant: {
    type: String,
    default: 'primary',
    validator: (value) => ['primary', 'secondary', 'outline', 'ghost'].includes(value)
  },
  disabled: {
    type: Boolean,
    default: false
  },
  loading: {
    type: Boolean,
    default: false
  },
  block: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['click'])

const handleClick = (event) => {
  emit('click', event)
}
</script>

<style scoped>
.btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  padding: var(--spacing-md) var(--spacing-xl);
  font-size: var(--font-size-base);
  font-weight: var(--font-weight-semibold);
  line-height: 1;
  border: none;
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-base);
  white-space: nowrap;
  user-select: none;
  outline: none;
}

.btn:focus-visible {
  outline: 2px solid var(--color-primary-light);
  outline-offset: 2px;
}

/* Primary Button */
.btn-primary {
  background: var(--color-gradient-1);
  color: var(--color-text-primary);
  box-shadow: var(--shadow-md);
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: var(--shadow-glow);
}

.btn-primary:active:not(:disabled) {
  transform: translateY(0);
}

/* Secondary Button */
.btn-secondary {
  background: var(--color-bg-tertiary);
  color: var(--color-text-primary);
  border: 1px solid var(--color-border);
}

.btn-secondary:hover:not(:disabled) {
  background: var(--color-bg-secondary);
  border-color: var(--color-primary);
}

/* Outline Button */
.btn-outline {
  background: transparent;
  color: var(--color-primary-light);
  border: 2px solid var(--color-primary);
}

.btn-outline:hover:not(:disabled) {
  background: var(--color-primary);
  color: var(--color-text-primary);
}

/* Ghost Button */
.btn-ghost {
  background: transparent;
  color: var(--color-text-secondary);
}

.btn-ghost:hover:not(:disabled) {
  background: var(--color-bg-tertiary);
  color: var(--color-text-primary);
}

/* Disabled State */
.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none !important;
}

/* Loading State */
.btn-loading {
  pointer-events: none;
}

.btn-spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Block Button */
.btn-block {
  width: 100%;
}

/* Responsive */
@media (max-width: 768px) {
  .btn {
    padding: var(--spacing-sm) var(--spacing-lg);
    font-size: var(--font-size-sm);
  }
}
</style>
