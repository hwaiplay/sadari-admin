import { useEffect, useRef } from 'react'
import $ from 'jquery'
import 'summernote/dist/summernote-lite.css'
import 'summernote/dist/summernote-lite.min.js'
import { uploadNoticeImage } from '../api/noticeApi'

type SummernoteEditorProps = {
  value: string
  disabled: boolean
  onChange: (value: string) => void
  onError: (message: string) => void
}

/** 공지사항 HTML 작성과 공지 전용 이미지 업로드를 연결한다. */
export function SummernoteEditor({ value, disabled, onChange, onError }: SummernoteEditorProps) {
  const editorRef = useRef<HTMLDivElement | null>(null)
  const initialValueRef = useRef(value)
  const onChangeRef = useRef(onChange)
  const onErrorRef = useRef(onError)

  useEffect(() => {
    onChangeRef.current = onChange
    onErrorRef.current = onError
  }, [onChange, onError])

  useEffect(() => {
    if (!editorRef.current) return
    const editor = $(editorRef.current)
    editor.summernote({
      height: 420,
      placeholder: '공지 내용을 입력해 주세요.',
      callbacks: {
        onChange: (contents) => onChangeRef.current(contents),
        onImageUpload: (files) => {
          void (async () => {
            try {
              for (const file of files) {
                const url = await uploadNoticeImage(file)
                editor.summernote('insertImage', url)
              }
            } catch (error: unknown) {
              onErrorRef.current(error instanceof Error ? error.message : '공지 이미지 업로드에 실패했습니다.')
            }
          })()
        },
      },
    })
    editor.summernote('code', initialValueRef.current)
    return () => {
      editor.summernote('destroy')
    }
  }, [])

  useEffect(() => {
    if (!editorRef.current) return
    const editor = $(editorRef.current)
    if (editor.summernote('code') !== value) editor.summernote('code', value)
  }, [value])

  return <div className={disabled ? 'summernote-disabled' : ''} ref={editorRef} />
}
