import 'jquery'

type SummernoteOptions = {
  height?: number
  placeholder?: string
  callbacks?: {
    onChange?: (contents: string) => void
    onImageUpload?: (files: File[]) => void
  }
}

declare global {
  interface JQuery {
    summernote(options: SummernoteOptions): JQuery
    summernote(method: 'code'): string
    summernote(method: 'code', value: string): JQuery
    summernote(method: 'destroy'): JQuery
    summernote(method: 'insertImage', url: string): JQuery
  }
}

declare module 'summernote/dist/summernote-lite.min.js'
