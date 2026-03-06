# 全局错误处理使用指南

本文提供三套实现：
- 原生 JavaScript（无框架依赖）
- Vue3（已接入当前项目）
- React（可直接复用）

## 1. 原生 JavaScript 版本

```html
<script>
  (function setupGlobalErrorHandler() {
    var IS_DEV = location.hostname === 'localhost' || location.hostname === '127.0.0.1';
    var IGNORED_PATTERNS = [
      'ResizeObserver loop limit exceeded',
      'ResizeObserver loop completed with undelivered notifications'
    ];
    var RECENT = new Map();
    var DEDUPE_MS = 2500;

    // 基础过滤：忽略已知低风险错误，避免弹框泛滥。
    function shouldIgnore(message) {
      if (!message) return false;
      return IGNORED_PATTERNS.some(function (pattern) {
        return String(message).indexOf(pattern) !== -1;
      });
    }

    // 去重：短时间内相同错误仅提示一次。
    function isDuplicate(type, message, file, line, column) {
      var key = [type, message, file || '', line || 0, column || 0].join('|');
      var now = Date.now();
      var prev = RECENT.get(key);
      RECENT.set(key, now);
      return typeof prev === 'number' && now - prev < DEDUPE_MS;
    }

    // 统一提示：生产环境隐藏敏感信息，开发环境显示完整定位信息。
    function showFriendlyAlert(payload) {
      var location = payload.file
        ? payload.file + (payload.line ? ':' + payload.line : '') + (payload.column ? ':' + payload.column : '')
        : 'unknown';
      var text = IS_DEV
        ? [
            '抱歉，页面发生了一个异常。',
            '错误类型：' + payload.type,
            '错误信息：' + payload.message,
            '发生位置：' + location
          ].join('\n')
        : [
            '抱歉，页面遇到异常，部分功能可能不可用。',
            '请刷新后重试，若问题持续请联系管理员。',
            '错误类型：' + payload.type
          ].join('\n');

      alert(text);
    }

    function report(payload) {
      if (shouldIgnore(payload.message)) return;
      if (isDuplicate(payload.type, payload.message, payload.file, payload.line, payload.column)) return;

      // 保留原始堆栈，便于开发调试。
      if (payload.stack) {
        console.error('[GlobalErrorHandler]', payload, '\nStack:', payload.stack);
      } else {
        console.error('[GlobalErrorHandler]', payload);
      }

      // 只做提示，不主动 throw，保证页面不被我们的处理逻辑中断。
      showFriendlyAlert(payload);
    }

    // 1) 捕获 JS 运行时错误 + 资源加载失败（捕获阶段）。
    window.addEventListener(
      'error',
      function (event) {
        var isResourceError = event.target && event.target !== window;
        if (isResourceError) {
          var resourceUrl = event.target.src || event.target.href || 'unknown resource';
          report({
            type: 'RESOURCE_LOAD_ERROR',
            message: '资源加载失败：' + resourceUrl,
            file: resourceUrl
          });
          return;
        }

        report({
          type: 'JS_RUNTIME_ERROR',
          message: event.message || 'Unknown runtime error',
          file: event.filename,
          line: event.lineno,
          column: event.colno,
          stack: event.error && event.error.stack
        });

        event.preventDefault();
      },
      true
    );

    // 2) 捕获未处理的 Promise 异常。
    window.addEventListener('unhandledrejection', function (event) {
      var reason = event.reason;
      report({
        type: 'PROMISE_ERROR',
        message: reason && reason.message ? reason.message : String(reason || 'Unknown promise rejection'),
        stack: reason && reason.stack
      });

      event.preventDefault();
    });
  })();
</script>
```

## 2. Vue3 版本（当前项目）

当前项目已接入文件：
- `src/utils/globalErrorHandler.ts`
- `src/main.ts`

已在 `main.ts` 调用：

```ts
setupVueErrorHandler(app, {
  env: import.meta.env.DEV ? 'development' : 'production',
  useModal: true
})
```

说明：
- `setupGlobalErrorHandler` 负责监听 `window.error` 和 `unhandledrejection`。
- `setupVueErrorHandler` 额外接管 `app.config.errorHandler`，捕获组件渲染/生命周期错误。
- 默认使用 Element Plus `ElMessageBox.alert`，失败时自动降级到 `alert`。

## 3. React 版本（适配示例）

```tsx
import React from 'react'

type ErrorPayload = {
  type: 'JS_RUNTIME_ERROR' | 'PROMISE_ERROR' | 'RESOURCE_LOAD_ERROR' | 'REACT_ERROR'
  message: string
  file?: string
  line?: number
  column?: number
  stack?: string
}

const IS_DEV = process.env.NODE_ENV !== 'production'
const IGNORED = ['ResizeObserver loop limit exceeded']
const RECENT = new Map<string, number>()

function shouldIgnore(message: string) {
  return IGNORED.some((item) => message.includes(item))
}

function isDuplicate(payload: ErrorPayload) {
  const key = `${payload.type}|${payload.message}|${payload.file ?? ''}|${payload.line ?? 0}|${payload.column ?? 0}`
  const now = Date.now()
  const prev = RECENT.get(key)
  RECENT.set(key, now)
  return typeof prev === 'number' && now - prev < 2500
}

function showFriendlyMessage(payload: ErrorPayload) {
  const location = payload.file
    ? `${payload.file}${payload.line ? `:${payload.line}` : ''}${payload.column ? `:${payload.column}` : ''}`
    : 'unknown'
  const text = IS_DEV
    ? `抱歉，页面发生了一个异常。\n错误类型：${payload.type}\n错误信息：${payload.message}\n发生位置：${location}`
    : `抱歉，页面遇到异常，部分功能可能不可用。\n请刷新后重试，若问题持续请联系管理员。\n错误类型：${payload.type}`
  alert(text)
}

function report(payload: ErrorPayload) {
  if (shouldIgnore(payload.message) || isDuplicate(payload)) return
  console.error('[GlobalErrorHandler]', payload, payload.stack ? `\nStack: ${payload.stack}` : '')
  showFriendlyMessage(payload)
}

export function setupGlobalErrorHandlerForReact() {
  window.addEventListener(
    'error',
    (event) => {
      const isResourceError = !!event.target && event.target !== window
      if (isResourceError) {
        const target = event.target as HTMLScriptElement | HTMLLinkElement | HTMLImageElement
        const resourceUrl = target.src || (target as HTMLLinkElement).href || 'unknown resource'
        report({
          type: 'RESOURCE_LOAD_ERROR',
          message: `资源加载失败：${resourceUrl}`,
          file: resourceUrl
        })
        return
      }

      report({
        type: 'JS_RUNTIME_ERROR',
        message: event.message || 'Unknown runtime error',
        file: event.filename,
        line: event.lineno,
        column: event.colno,
        stack: event.error?.stack
      })

      event.preventDefault()
    },
    true
  )

  window.addEventListener('unhandledrejection', (event) => {
    const reason = event.reason
    report({
      type: 'PROMISE_ERROR',
      message: reason?.message || String(reason || 'Unknown promise rejection'),
      stack: reason?.stack
    })
    event.preventDefault()
  })
}

export class GlobalErrorBoundary extends React.Component<
  { children: React.ReactNode },
  { hasError: boolean }
> {
  state = { hasError: false }

  static getDerivedStateFromError() {
    return { hasError: true }
  }

  componentDidCatch(error: Error, info: React.ErrorInfo) {
    report({
      type: 'REACT_ERROR',
      message: error.message + `（React 组件栈：${info.componentStack || 'unknown'}）`,
      stack: error.stack
    })
  }

  render() {
    // 不强制替换整个 UI，尽量减少“直接崩溃退出”体感。
    return this.props.children
  }
}
```

React 使用方式：

```tsx
import React from 'react'
import ReactDOM from 'react-dom/client'
import { GlobalErrorBoundary, setupGlobalErrorHandlerForReact } from './globalErrorHandler'
import App from './App'

setupGlobalErrorHandlerForReact()

ReactDOM.createRoot(document.getElementById('root')!).render(
  <GlobalErrorBoundary>
    <App />
  </GlobalErrorBoundary>
)
```

## 4. 使用注意事项

- 全局错误处理无法捕获所有跨域脚本报错详情，若出现 `Script error.`，需要给脚本资源配置 CORS 和 `crossorigin`。
- 不建议在错误处理器中执行复杂异步逻辑，避免“处理错误时再次报错”。
- 弹框策略建议线上限制频率（当前已做去重），否则连续错误会影响用户操作。
- 如果后续要接入 Sentry 等平台，可在 `report` 函数中统一上报，不影响现有提示逻辑。
