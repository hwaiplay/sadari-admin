import type { FormEvent } from 'react'

type LoginPageProps = {
  admnIdxx: string
  passWord: string
  submitting: boolean
  error: string | null
  onChangeAdmnIdxx: (value: string) => void
  onChangePassWord: (value: string) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
}

/**
 * 관리자 로그인 화면
 * @Author SeungHyeon.Kang
 * @param admnIdxx
 * @param passWord
 * @param submitting
 * @param error
 * @param onChangeAdmnIdxx
 * @param onChangePassWord
 * @param onSubmit
 * @return
 */
export function LoginPage({ admnIdxx, passWord, submitting, error, onChangeAdmnIdxx, onChangePassWord, onSubmit }: LoginPageProps) {
  return (
    <main className="login-page">
      <form className="login-panel" onSubmit={onSubmit}>
        <div>
          <p className="eyebrow">사다리 관리자</p>
          <h1>관리자 로그인</h1>
        </div>
        {error && <div className="error">{error}</div>}
        <label>
          <span>관리자 아이디</span>
          <input value={admnIdxx} onChange={(event) => onChangeAdmnIdxx(event.target.value)} required />
        </label>
        <label>
          <span>비밀번호</span>
          <input type="password" value={passWord} onChange={(event) => onChangePassWord(event.target.value)} required />
        </label>
        <button type="submit" disabled={submitting}>{submitting ? '로그인 중' : '로그인'}</button>
      </form>
    </main>
  )
}
