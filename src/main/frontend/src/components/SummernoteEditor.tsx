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
  placeholder?: string
  uploadImage?: (file: File) => Promise<string>
  uploadErrorMessage?: string
}

/** HTML 작성 화면과 업무별 Summernote 이미지 업로드를 연결한다. */
export function SummernoteEditor({
  value,
  disabled,
  onChange,
  onError,
  placeholder = '공지 내용을 입력해 주세요.',
  uploadImage = uploadNoticeImage,
  uploadErrorMessage = '공지 이미지 업로드에 실패했습니다.',
}: SummernoteEditorProps) {
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
      placeholder,
      callbacks: {
        onChange: (contents) => onChangeRef.current(contents),
        onImageUpload: (files) => {
          void (async () => {
            try {
              for (const file of files) {
                const url = await uploadImage(file)
                editor.summernote('insertImage', url)
              }
            } catch (error: unknown) {
              onErrorRef.current(error instanceof Error ? error.message : uploadErrorMessage)
            }
          })()
        },
      },
    })
    editor.summernote('code', initialValueRef.current)
    return () => {
      editor.summernote('destroy')
    }
  }, [placeholder, uploadErrorMessage, uploadImage])

  useEffect(() => {
    if (!editorRef.current) return
    const editor = $(editorRef.current)
    if (editor.summernote('code') !== value) editor.summernote('code', value)
  }, [value])

  return <div className={disabled ? 'summernote-disabled' : ''} ref={editorRef} />
}
