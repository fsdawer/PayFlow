<template>
  <div class="input-group">
    <label v-if="label" :for="inputId" class="input-label">
      {{ label }}
      <span v-if="required" class="input-required">*</span>
    </label>
    
    <div class="input-wrapper">
      <input
        :id="inputId"
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :disabled="disabled"
        :required="required"
        :class="['input', { 'input-error': error }]"
        @input="handleInput"
        @blur="handleBlur"
        @focus="handleFocus"
      />
      
      <span v-if="error" class="input-icon-error">⚠</span>
    </div>
    
    <p v-if="error" class="input-error-message">{{ error }}</p>
    <p v-else-if="hint" class="input-hint">{{ hint }}</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  type: {
    type: String,
    default: 'text'
  },
  label: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: ''
  },
  error: {
    type: String,
    default: ''
  },
  hint: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
  required: {
    type: Boolean,
    default: false
  },
  id: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue', 'blur', 'focus'])

const inputId = computed(() => props.id || `input-${Math.random().toString(36).substr(2, 9)}`)

const handleInput = (event) => {
  emit('update:modelValue', event.target.value)
}

const handleBlur = (event) => {
  emit('blur', event)
}

const handleFocus = (event) => {
  emit('focus', event)
}
</script>

<style scoped>
.input-group {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-sm);
  width: 100%;
}

.input-label {
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-text-secondary);
}

.input-required {
  color: var(--color-error);
  margin-left: 2px;
}

.input-wrapper {
  position: relative;
  width: 100%;
}

.input {
  width: 100%;
  padding: var(--spacing-md);
  font-size: var(--font-size-base);
  font-family: var(--font-family);
  color: var(--color-text-primary);
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  outline: none;
  transition: all var(--transition-base);
}

.input::placeholder {
  color: var(--color-text-muted);
}

.input:focus {
  border-color: var(--color-primary);
  box-shadow: 0 0 0 3px var(--color-border-focus);
}

.input:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  background: var(--color-bg-tertiary);
}

.input-error {
  border-color: var(--color-error);
}

.input-error:focus {
  box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.2);
}

.input-icon-error {
  position: absolute;
  right: var(--spacing-md);
  top: 50%;
  transform: translateY(-50%);
  color: var(--color-error);
  pointer-events: none;
}

.input-error-message {
  font-size: var(--font-size-sm);
  color: var(--color-error);
  margin: 0;
}

.input-hint {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
  margin: 0;
}
</style>
