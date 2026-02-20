<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
/ -->

<script lang="ts">
import { message } from 'ant-design-vue'
import { defineComponent, reactive, ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import loginService from '@/services/login.service'
import { usePlaceholder } from '@/hooks/usePlaceholder'

interface FormState {
  username: string
  password: string
}

export default defineComponent({
  name: 'Login',
  setup() {
    const router = useRouter()
    const formState = reactive<FormState>({
      username: '',
      password: '',
    })
    const placeholder = reactive(usePlaceholder())

    // Password visibility toggle
    const passwordVisible = ref(false)

    // Button disabled state - use ref instead of computed to force update
    const disabled = ref(true)

    // Update disabled state when form fields change
    const updateDisabledState = () => {
      disabled.value = !(formState.username && formState.password)
    }

    // Handle browser autofill - listen to input events
    const handleInput = () => {
      updateDisabledState()
    }

    // Typing animation - matching demo exactly
    const keywords = ['Iceberg', 'Paimon', 'Lance', 'Multi-modal', 'Vector']
    const currentKeyword = ref('')
    const keywordIndex = ref(0)
    const charIndex = ref(0)
    const isDeleting = ref(false)
    let typingTimer: ReturnType<typeof setTimeout> | null = null

    function typeKeyword() {
      const current = keywords[keywordIndex.value]
      let delay = 150

      if (isDeleting.value) {
        currentKeyword.value = current.substring(0, charIndex.value - 1)
        charIndex.value--
        delay = 50
      }
      else {
        currentKeyword.value = current.substring(0, charIndex.value + 1)
        charIndex.value++
        delay = 100
      }

      if (!isDeleting.value && charIndex.value === current.length) {
        isDeleting.value = true
        delay = 2000
      }
      else if (isDeleting.value && charIndex.value === 0) {
        isDeleting.value = false
        keywordIndex.value = (keywordIndex.value + 1) % keywords.length
        delay = 300
      }

      typingTimer = setTimeout(typeKeyword, delay)
    }

    onMounted(() => {
      typeKeyword()
      // Delay to ensure autofill has completed
      setTimeout(() => {
        updateDisabledState()
      }, 100)
    })

    onUnmounted(() => {
      if (typingTimer) {
        clearTimeout(typingTimer)
      }
    })

    const onFinish = async () => {
      try {
        const res = await loginService.login({
          user: formState.username,
          password: formState.password,
        })
        if (res.code !== 200) {
          message.error(res.message)
          return
        }
        setTimeout(() => {
          window.location.href = '/'
        }, 100)
      }
      catch (error) {
        message.error((error as Error).message)
      }
    }

    const togglePassword = () => {
      passwordVisible.value = !passwordVisible.value
    }

    return {
      placeholder,
      formState,
      onFinish,
      disabled,
      passwordVisible,
      togglePassword,
      currentKeyword,
      handleInput,
    }
  },
})
</script>

<template>
  <div class="login-wrap">
    <!-- Background effects -->
    <div class="bg-grid" />
    <div class="particles">
      <div class="particle" />
      <div class="particle" />
      <div class="particle" />
      <div class="particle" />
      <div class="particle" />
    </div>

    <!-- Login form -->
    <div class="login-content">
      <div class="terminal-header">
        <img src="@/assets/images/logo-all1.svg" class="header-logo" alt="Amoro">
        <a href="https://amoro.apache.org" target="_blank" class="docs-link">
          <span class="docs-dot" />
          Docs
        </a>
      </div>

      <div class="img-logo">
        <div class="logo-icon">
          <div class="typing-line1">
            <span class="typing-prefix">></span>
            <span class="typing-fixed">Automatically and Continuously Tune</span>
          </div>
          <div class="typing-line2">
            <span class="typing-fixed2">Your&nbsp;</span>
            <span class="typing-wrapper">
              <span class="typing-text">{{ currentKeyword }}</span>
              <span class="typing-cursor">|</span>
            </span>
            <span class="typing-fixed2">&nbsp;Lakehouse</span>
          </div>
        </div>
      </div>

      <form class="login-form" @submit.prevent="onFinish">
        <div class="form-item" style="margin-bottom: 12px;">
          <div class="input-wrapper">
            <span class="input-prefix">▸</span>
            <input
                v-model="formState.username"
                type="text"
                class="form-input"
                :placeholder="placeholder.usernamePh"
                autocomplete="username"
                @input="handleInput"
                @change="handleInput"
            >
          </div>
        </div>

        <div class="form-item">
          <div class="input-wrapper">
            <span class="input-prefix">#</span>
            <input
                v-model="formState.password"
                :type="passwordVisible ? 'text' : 'password'"
                class="form-input"
                :placeholder="placeholder.passwordPh"
                autocomplete="current-password"
                @input="handleInput"
                @change="handleInput"
            >
            <span class="password-toggle" @click="togglePassword">
              <svg v-if="!passwordVisible" class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                <circle cx="12" cy="12" r="3" />
              </svg>
              <svg v-else class="eye-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                <line x1="1" y1="1" x2="23" y2="23" />
              </svg>
            </span>
          </div>
        </div>

        <button type="submit" class="login-button" :disabled="disabled">
          LOGIN
        </button>

        <div class="status-bar">
          <div class="status-item">
            <div class="license-dot" />
            <a href="https://www.apache.org/licenses/LICENSE-2.0" target="_blank" class="license-link">Apache License 2.0</a>
          </div>
          <div class="status-item">
            <a href="https://github.com/apache/incubator-amoro" target="_blank" class="github-link">
              <svg class="github-icon" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 0C5.374 0 0 5.373 0 12c0 5.302 3.438 9.8 8.207 11.387.599.111.793-.261.793-.577v-2.234c-3.338.726-4.033-1.416-4.033-1.416-.546-1.387-1.333-1.756-1.333-1.756-1.089-.745.083-.729.083-.729 1.205.084 1.839 1.237 1.839 1.237 1.07 1.834 2.807 1.304 3.492.997.107-.775.418-1.305.762-1.604-2.665-.305-5.467-1.334-5.467-5.931 0-1.311.469-2.381 1.236-3.221-.124-.303-.535-1.524.117-3.176 0 0 1.008-.322 3.301 1.23A11.509 11.509 0 0112 5.803c1.02.005 2.047.138 3.006.404 2.291-1.552 3.297-1.23 3.297-1.23.653 1.653.242 2.874.118 3.176.77.84 1.235 1.911 1.235 3.221 0 4.609-2.807 5.624-5.479 5.921.43.372.823 1.102.823 2.222v3.293c0 .319.192.694.801.576C20.566 21.797 24 17.3 24 12c0-6.627-5.373-12-12-12z" />
              </svg>
            </a>
          </div>
        </div>
      </form>
    </div>
  </div>
</template>

<style lang="less">
// Login page styles - matching demo EXACTLY
// All values from login-demo-geek-v3.html

.login-wrap {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  background: #ffffff;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.login-wrap .bg-grid {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image:
      linear-gradient(rgba(0, 54, 161, 0.03) 1px, transparent 1px),
      linear-gradient(90deg, rgba(0, 54, 161, 0.03) 1px, transparent 1px);
  background-size: 40px 40px;
  pointer-events: none;
  z-index: 0;
}

.login-wrap .particles {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;
}

.login-wrap .particle {
  position: absolute;
  width: 3px;
  height: 3px;
  background: rgba(0, 54, 161, 0.15);
  border-radius: 50%;
  animation: float 15s infinite;
}

.login-wrap .particle:nth-child(1) { left: 10%; animation-delay: 0s; }
.login-wrap .particle:nth-child(2) { left: 30%; animation-delay: 2s; }
.login-wrap .particle:nth-child(3) { left: 50%; animation-delay: 4s; }
.login-wrap .particle:nth-child(4) { left: 70%; animation-delay: 1s; }
.login-wrap .particle:nth-child(5) { left: 90%; animation-delay: 3s; }

@keyframes float {
  0% {
    transform: translateY(100vh) scale(0);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100px) scale(1);
    opacity: 0;
  }
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

// login-content: width 380px, padding 36px 32px (demo line 105-113)
.login-wrap .login-content {
  width: 380px;
  padding: 26px 32px 36px;
  background: rgba(255, 255, 255, 0.98);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 10;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-sizing: border-box;
}

.login-wrap .login-content::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg,
  transparent,
  rgba(0, 54, 161, 0.5),
  transparent);
}

.login-wrap .login-content:hover {
  box-shadow: 0 6px 32px rgba(0, 0, 0, 0.12);
}

// header-logo: height 24px
.login-wrap .header-logo {
  height: 24px !important;
  width: auto !important;
  border: 0;
  vertical-align: middle;
}

// docs-link: font-size 12px (demo line 146-160)
// Push docs down more to match demo top: 42px
.login-wrap .docs-link {
  color: #666;
  text-decoration: none !important;
  font-size: 12px !important;
  letter-spacing: 0.5px;
  transition: color 0.3s ease;
  display: flex;
  align-items: center;
  gap: 6px;
  line-height: 1;
  margin-top: 20px;
}

.login-wrap .docs-link:hover {
  color: #0036A1;
}

// docs-dot: 6px (demo line 162-167)
.login-wrap .docs-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #febc2e;
  flex-shrink: 0;
}

// terminal-header: margin-bottom 28px, padding 16px 0 (demo line 173-182)
// Remove terminal-header padding-top to reduce top margin, use login-content padding instead
.login-wrap .terminal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 28px;
  padding-top: 5px;
  padding-bottom: 11px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

// img-logo: margin +2px top/bottom
.login-wrap .img-logo {
  margin-top: 2px;
  margin-bottom: 26px;
}

.login-wrap .logo-icon {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.login-wrap .typing-line1 {
  display: flex;
  align-items: center;
  line-height: 1.4;
}

// typing-prefix: 15px bold (demo line 210-214)
.login-wrap .typing-prefix {
  color: #666;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', 'source-code-pro', monospace !important;
  font-size: 15px !important;
  font-weight: 700;
  line-height: 1.4;
}

// typing-fixed: 13px, weight 600 (demo line 216-222)
.login-wrap .typing-fixed {
  color: #666;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', 'source-code-pro', monospace !important;
  font-size: 13px !important;
  font-weight: 600;
  letter-spacing: 0.5px;
  padding-left: 10px;
  line-height: 1.4;
}

.login-wrap .typing-line2 {
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1.4;
}

// typing-fixed2: 13px, weight 600 (demo line 230-235)
.login-wrap .typing-fixed2 {
  color: #666;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', 'source-code-pro', monospace !important;
  font-size: 13px !important;
  font-weight: 600;
  letter-spacing: 0.5px;
  line-height: 1.4;
}

.login-wrap .typing-wrapper {
  display: inline-flex;
  align-items: center;
}

// typing-text: 13px, color #0036A1 (demo line 242-247)
.login-wrap .typing-text {
  color: #0036A1;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', 'source-code-pro', monospace !important;
  font-size: 13px !important;
  font-weight: 600;
  letter-spacing: 0.5px;
  line-height: 1.2;
}

// typing-cursor: 13px (demo line 249-254)
.login-wrap .typing-cursor {
  color: #0036A1;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', 'source-code-pro', monospace !important;
  font-size: 13px !important;
  font-weight: 600;
  line-height: 1.2;
  animation: blink 1s step-end infinite;
}

.login-wrap .login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.login-wrap .form-item {
  position: relative;
}

// input-wrapper: border 1px (demo line 301-308)
.login-wrap .input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
  background: #ffffff;
  border: 1px solid rgba(0, 54, 161, 0.2);
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.login-wrap .input-wrapper:focus-within {
  border-color: #0036A1;
  box-shadow: 0 0 0 3px rgba(0, 54, 161, 0.1);
}

// input-prefix: padding 0 14px, font-size 18px (demo line 315-321)
// Global reset.less uses "font: 14px/1.42858" shorthand which overrides font-size
// Must use "font" shorthand to override, not font-size alone
.login-wrap .input-prefix {
  padding: 0 14px;
  color: #666;
  font: 18px/1 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', 'source-code-pro', monospace !important;
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

// form-input: height 44px, font-size 14px (demo line 323-333)
// Use default content page font (Helvetica from reset.less), but font-size from demo
// Must use "font" shorthand to override global font setting
.login-wrap .form-input {
  flex: 1;
  height: 44px !important;
  line-height: 44px !important;
  border: none !important;
  outline: none !important;
  font: 14px/44px !important;
  color: #0a0a0a !important;
  background: transparent !important;
  padding: 0 14px 0 0 !important;
  margin: 0 !important;
  box-sizing: border-box;
  box-shadow: none !important;
}

// Override browser autofill background and text color
.login-wrap .form-input:-webkit-autofill,
.login-wrap .form-input:-webkit-autofill:hover,
.login-wrap .form-input:-webkit-autofill:focus,
.login-wrap .form-input:-webkit-autofill:active {
  background: #ffffff !important;
  color: #0a0a0a !important;
  -webkit-box-shadow: 0 0 0 1000px #ffffff inset !important;
  -webkit-text-fill-color: #0a0a0a !important;
}

.login-wrap .form-input::placeholder {
  color: #999;
  font-size: 13px;
}

.login-wrap .form-input:focus {
  color: #0a0a0a;
  box-shadow: none !important;
}

// password-toggle: right 14px (demo line 345-356)
.login-wrap .password-toggle {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  cursor: pointer;
  color: #999;
  font-size: 16px;
  user-select: none;
  transition: color 0.3s ease;
  opacity: 0.7;
  line-height: 1;
}

.login-wrap .password-toggle:hover {
  color: #0036A1;
  opacity: 1;
}

// eye-icon: 16x16 (demo line 363-373)
.login-wrap .eye-icon {
  width: 16px !important;
  height: 16px !important;
  stroke: currentColor;
  stroke-width: 2;
  fill: none;
  transition: all 0.2s ease;
  display: block;
}

.login-wrap .password-toggle:hover .eye-icon {
  stroke: #0036A1;
}

// login-button: height 48px, font-size 14px (demo line 436-451)
// Use default content page font, not monospace
.login-wrap .login-button {
  margin-top: 22px;
  height: 48px !important;
  line-height: 48px !important;
  border: none !important;
  background: #0036A1;
  color: #ffffff !important;
  font-size: 14px !important;
  font-weight: 700;
  letter-spacing: 2px;
  text-transform: uppercase;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s ease;
  padding: 0 !important;
  box-sizing: border-box;
  border-radius: 0 !important;
}

.login-wrap .login-button:hover {
  background: #002a80;
  box-shadow: 0 4px 12px rgba(0, 54, 161, 0.3);
}

.login-wrap .login-button:active {
  transform: translateY(0);
}

.login-wrap .login-button:disabled {
  background: #e0e0e0;
  color: #999 !important;
  cursor: not-allowed;
  box-shadow: none;
}

// status-bar: font-size 10px (demo line 470-480)
// Use default content page font, not monospace
.login-wrap .status-bar {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.08);
  font-size: 10px !important;
  color: #999;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.login-wrap .status-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

// license-dot: 6px (demo line 488-493)
.login-wrap .license-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #28c840;
  flex-shrink: 0;
}

.login-wrap .license-link {
  color: #666;
  text-decoration: none !important;
  transition: color 0.3s ease;
}

.login-wrap .license-link:hover {
  color: #0036A1;
}

.login-wrap .github-link {
  display: flex;
  align-items: center;
  color: #666;
  text-decoration: none !important;
  transition: color 0.3s ease;
}

.login-wrap .github-link:hover {
  color: #0036A1;
}

// github-icon: 18x18 (demo line 517-521)
.login-wrap .github-icon {
  width: 18px !important;
  height: 18px !important;
  fill: currentColor;
}

/* Responsive */
@media (max-width: 500px) {
  .login-wrap .login-content {
    width: 90%;
    padding: 32px 24px;
  }

  // demo uses height: 20px for mobile
  .login-wrap .header-logo {
    height: 20px !important;
  }
}
</style>
