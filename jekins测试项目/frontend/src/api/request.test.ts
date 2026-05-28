import { describe, it, expect } from 'vitest'

describe('sanitizeInput', () => {
  let sanitizeInput: (str: string) => string

  beforeAll(async () => {
    const mod = await import('./request')
    sanitizeInput = mod.sanitizeInput
  })

  it('should sanitize XSS dangerous characters', () => {
    expect(sanitizeInput('<script>alert("xss")</script>')).toBe(
      '&lt;script&gt;alert(&quot;xss&quot;)&lt;/script&gt;'
    )
  })

  it('should sanitize single quotes', () => {
    expect(sanitizeInput("test'or'1'='1")).toBe('test&#39;or&#39;1&#39;=&#39;1')
  })

  it('should sanitize backslashes', () => {
    expect(sanitizeInput('path\\to\\file')).toBe('path&#92;to&#92;file')
  })

  it('should sanitize ampersands', () => {
    expect(sanitizeInput('a&b')).toBe('a&amp;b')
  })

  it('should return original string if no dangerous characters', () => {
    expect(sanitizeInput('hello world')).toBe('hello world')
  })

  it('should handle empty string', () => {
    expect(sanitizeInput('')).toBe('')
  })

  it('should handle null/undefined gracefully', () => {
    expect(sanitizeInput(null as any)).toBeNull()
    expect(sanitizeInput(undefined as any)).toBeUndefined()
  })
})
