import type { AdminSession } from '../types/admin'
import type { Menu } from '../types/menu'
import type { ReactNode } from 'react'

type AdminLayoutProps = {
  admin: AdminSession
  menus: Menu[]
  currentPath: string
  activePath: string
  error: string | null
  onMovePath: (path: string) => void
  onLogout: () => void
  children: ReactNode
}

/**
 * 관리자 공통 레이아웃
 * @Author SeungHyeon.Kang
 * @param admin
 * @param menus
 * @param activePath
 * @param error
 * @param onMovePath
 * @param onLogout
 * @param children
 * @return
 */
export function AdminLayout({ admin, menus, activePath, error, onMovePath, onLogout, children }: AdminLayoutProps) {
  return (
    <div className="admin-layout">
      <aside className="sidebar">
        <div className="sidebar-title">사다리 관리자</div>
        <nav className="menu">
          {menus.map((menu) => (
            <button key={`${menu.menuNumb}-${menu.subxNumb}`} type="button" className={activePath === menu.menuUrlx ? 'menu-item active' : 'menu-item'} onClick={() => onMovePath(menu.menuUrlx)}>
              <span className={menu.subxNumb === '0' ? 'menu-label' : 'menu-label child'}>{menu.menuName}</span>
            </button>
          ))}
        </nav>
      </aside>
      <div className="content-shell">
        <header className="top-header">
          <div className="welcome">{admin.admnName}님 환영합니다.</div>
          <button type="button" className="logout-button" onClick={onLogout}>로그아웃</button>
        </header>
        <main className="content">
          {error && <div className="error">{error}</div>}
          {children}
        </main>
      </div>
    </div>
  )
}
