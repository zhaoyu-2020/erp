import type { App } from 'vue'
import { ElMessageBox } from 'element-plus'

type ErrorType = 'JS_RUNTIME_ERROR' | 'PROMISE_ERROR' | 'RESOURCE_LOAD_ERROR' | 'VUE_ERROR'

interface ErrorPayload {
  type: ErrorType
  message: string
  file?: string
  line?: number
  column?: number
  stack?: string
}

interface ErrorHandlerOptions {
  env?: 'development' | 'production'
  ignoredPatterns?: Array<string | RegExp>
  useModal?: boolean
  alertTitle?: string
}

const DEFAULT_IGNORED_PATTERNS: Array<string | RegExp> = [
  'ResizeObserver loop limit exceeded',
  'ResizeObserver loop completed with undelivered notifications',
  /Script error\.?/i,
  // axios HTTP 错误已由 request.ts 拦截器通过 ElMessage 处理，无需再弹窗
  /Request failed with status code \d+/i
]

const DEDUPE_WINDOW_MS = 2500
const recentErrors = new Map<string, number>()

function normalizeMessage(input: unknown): string {
  if (typeof input === 'string') return input
  if (input instanceof Error) return input.message || String(input)
  if (input === null || input === undefined) return 'Unknown error'
  try {
    return JSON.stringify(input)
  } catch {
    return String(input)
  }
}

function isHandledError(input: unknown): boolean {
  return !!(input && typeof input === 'object' && (input as any).__handled === true)
}

function shouldIgnore(message: string, patterns: Array<string | RegExp>): boolean {
  return patterns.some((pattern) => {
    if (typeof pattern === 'string') return message.includes(pattern)
    return pattern.test(message)
  })
}

function isDuplicate(payload: ErrorPayload): boolean {
  const key = `${payload.type}|${payload.message}|${payload.file ?? ''}|${payload.line ?? 0}|${payload.column ?? 0}`
  const now = Date.now()
  const last = recentErrors.get(key)
  recentErrors.set(key, now)

  if (typeof last === 'number' && now - last < DEDUPE_WINDOW_MS) {
    return true
  }
  return false
}

function getResourceUrl(target: EventTarget | null): string {
  if (!target || !(target instanceof HTMLElement)) return 'unknown resource'
  const element = target as HTMLElement & {
    src?: string
    href?: string
  }
  return element.src || element.href || 'unknown resource'
}

function resolveEnv(options?: ErrorHandlerOptions): 'development' | 'production' {
  if (options?.env) return options.env
  return import.meta.env.DEV ? 'development' : 'production'
}

function formatDisplayText(payload: ErrorPayload, env: 'development' | 'production'): string {
  const location = payload.file
    ? `${payload.file}${payload.line ? `:${payload.line}` : ''}${payload.column ? `:${payload.column}` : ''}`
    : 'unknown'

  if (env === 'development') {
    return [
      '抱歉，页面发生了一个异常。',
      `错误类型：${payload.type}`,
      `错误信息：${payload.message}`,
      `发生位置：${location}`
    ].join('\n')
  }

  return [
    '抱歉，页面遇到异常，部分功能可能不可用。',
    '请刷新后重试，若问题持续请联系管理员。',
    `错误类型：${payload.type}`
  ].join('\n')
}

function showFriendlyError(payload: ErrorPayload, options?: ErrorHandlerOptions): void {
  const env = resolveEnv(options)
  const message = formatDisplayText(payload, env)
  const title = options?.alertTitle ?? '系统提示'

  // 生产/开发环境都走统一提示入口，保证异常不会因为弹框逻辑再次抛错。
  try {
    if (options?.useModal !== false) {
      void ElMessageBox.alert(message, title, {
        confirmButtonText: '我知道了',
        type: 'error'
      })
      return
    }
  } catch {
    // Fallback 到浏览器原生 alert，避免依赖 UI 组件失败。
  }

  if (typeof window !== 'undefined' && typeof window.alert === 'function') {
    window.alert(`${title}\n\n${message}`)
  }
}

function reportError(payload: ErrorPayload, options?: ErrorHandlerOptions): void {
  const patterns = options?.ignoredPatterns ?? DEFAULT_IGNORED_PATTERNS
  if (shouldIgnore(payload.message, patterns)) return
  if (isDuplicate(payload)) return

  // 保留原始堆栈，便于开发调试与问题定位。
  if (payload.stack) {
    // eslint-disable-next-line no-console
    console.error('[GlobalErrorHandler]', payload, '\nStack:', payload.stack)
  } else {
    // eslint-disable-next-line no-console
    console.error('[GlobalErrorHandler]', payload)
  }

  showFriendlyError(payload, options)
}

export function setupGlobalErrorHandler(options?: ErrorHandlerOptions): void {
  window.addEventListener(
    'error',
    (event: ErrorEvent) => {
      // 使用捕获阶段可以拿到资源加载错误；普通 JS 运行时错误也会在这里被捕获。
      const target = event.target
      const isResourceError = !!target && target !== window

      if (isResourceError) {
        const resourceUrl = getResourceUrl(target)
        reportError(
          {
            type: 'RESOURCE_LOAD_ERROR',
            message: `资源加载失败：${resourceUrl}`,
            file: resourceUrl
          },
          options
        )
        return
      }

      reportError(
        {
          type: 'JS_RUNTIME_ERROR',
          message: normalizeMessage(event.message),
          file: event.filename,
          line: event.lineno,
          column: event.colno,
          stack: event.error?.stack
        },
        options
      )

      // 阻止浏览器默认错误弹层，避免重复打断用户（控制台日志已保留）。
      event.preventDefault()
    },
    true
  )

  window.addEventListener('unhandledrejection', (event: PromiseRejectionEvent) => {
    // 捕获所有未被 catch 的 Promise 异常。
    const reason = event.reason
    // 已由 request.ts 拦截器处理过的 HTTP 错误，直接忽略，不重复弹窗
    if (isHandledError(reason)) {
      event.preventDefault()
      return
    }
    reportError(
      {
        type: 'PROMISE_ERROR',
        message: normalizeMessage(reason),
        stack: reason instanceof Error ? reason.stack : undefined
      },
      options
    )

    // 防止默认重复报错输出；异常信息已通过统一上报和弹框提示。
    event.preventDefault()
  })
}

export function setupVueErrorHandler(app: App, options?: ErrorHandlerOptions): void {
  setupGlobalErrorHandler(options)

  app.config.errorHandler = (err, instance, info) => {
    // 已由 request.ts 拦截器处理过的 HTTP 错误，直接忽略，不重复弹窗
    if (isHandledError(err)) return

    const message = normalizeMessage(err)
    const componentName =
      (instance as { type?: { name?: string } } | null)?.type?.name || 'AnonymousComponent'

    reportError(
      {
        type: 'VUE_ERROR',
        message: `${message}（组件：${componentName}，生命周期：${info}）`,
        stack: err instanceof Error ? err.stack : undefined
      },
      options
    )
  }
}

