import type { AdminSession } from '../types/admin'
import type { Menu } from '../types/menu'
import type { ReactNode } from 'react'
import { useState } from 'react'
import { MenuPermissionProvider } from '../contexts/MenuPermissionContext'

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
            // 부모 메뉴 아래 표시할 2뎁스 메뉴를 조회한다
            const childMenus = getChildMenus(menu.menuNumb)
            // 부모 메뉴가 자식 메뉴를 가지는지 구분한다
            const hasChildren = childMenus.length > 0
            // 현재 경로가 부모 메뉴의 자식 메뉴에 연결되었는지 확인한다
            const activeChild = childMenus.some((child) => activePath === child.menuUrlx)
            // 현재 경로가 부모 또는 자식 메뉴에 연결되면 메뉴 그룹을 활성화한다
            const active = activePath === menu.menuUrlx || activeChild
            // 활성 자식 메뉴가 속한 부모 그룹은 계층을 확인할 수 있도록 항상 펼친다
            const opened = openedMenuNumb === menu.menuNumb || activeChild
            // 자식 메뉴가 있는 활성 그룹은 부모와 자식을 하나의 선택 영역으로 표시한다
            const groupClassName = active
              ? `menu-group active${hasChildren ? ' has-children' : ''}`
              : 'menu-group'
            // 부모 메뉴와 연결된 2뎁스 메뉴 그룹을 반환한다
            return (
              <div key={`${menu.menuNumb}-${menu.subxNumb}`} className={groupClassName}>
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
          <MenuPermissionProvider key={activePath} menuUrlx={activePath}>{children}</MenuPermissionProvider>
        </main>
      </div>
    </div>
  )
}
