import type { AdminSession } from '../types/admin'
import type { Menu } from '../types/menu'
import type { ReactNode } from 'react'
import { useState } from 'react'

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
  const [openedMenuNumb, setOpenedMenuNumb] = useState<string | null>(null)
  const parentMenus = menus.filter((menu) => menu.subxNumb === '0')
  const getChildMenus = (menuNumb: string) => menus.filter((menu) => menu.menuNumb === menuNumb && menu.subxNumb !== '0')

  /**
   * 메뉴 URL 이동
   * @Author SeungHyeon.Kang
   * @param menuUrlx
   * @return
   */
  const handleMenuMove = (menuUrlx: string) => {
    if (menuUrlx === '#') return
    onMovePath(menuUrlx)
  }

  /**
   * 부모 메뉴 클릭 처리
   * @Author SeungHyeon.Kang
   * @param menu
   * @return
   */
  const handleParentMenuClick = (menu: Menu) => {
    const childMenus = getChildMenus(menu.menuNumb)
    if (childMenus.length > 0) {
      setOpenedMenuNumb(openedMenuNumb === menu.menuNumb ? null : menu.menuNumb)
      return
    }
    handleMenuMove(menu.menuUrlx)
  }

  return (
    <div className="admin-layout">
      <aside className="sidebar">
        <div className="sidebar-title">사다리 관리자</div>
        <nav className="menu">
          {parentMenus.map((menu) => {
            const childMenus = getChildMenus(menu.menuNumb)
            const opened = openedMenuNumb === menu.menuNumb
            const active = activePath === menu.menuUrlx || childMenus.some((child) => activePath === child.menuUrlx)
            return (
              <div key={`${menu.menuNumb}-${menu.subxNumb}`} className="menu-group">
                <button type="button" className={active ? 'menu-item active' : 'menu-item'} onClick={() => handleParentMenuClick(menu)}>
                  <span className="menu-label">{menu.menuName}</span>
                  {childMenus.length > 0 && <span className={opened ? 'menu-arrow opened' : 'menu-arrow'} aria-hidden="true" />}
                </button>
                {opened && childMenus.map((child) => (
                  <button key={`${child.menuNumb}-${child.subxNumb}`} type="button" className={activePath === child.menuUrlx ? 'menu-item child active' : 'menu-item child'} onClick={() => handleMenuMove(child.menuUrlx)}>
                    <span className="menu-label child">{child.menuName}</span>
                  </button>
                ))}
              </div>
            )
          })}
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
