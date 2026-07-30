import { useContext } from 'react'
import { MenuPermissionContext } from './menuPermissionContextValue'

/**
 * 현재 관리자 화면 경로에 연결된 메뉴 권한을 조회한다
 *
 * @author SeungHyeon.Kang
 * @return 현재 관리자의 조회와 쓰기 및 삭제 권한
 */
export function useMenuPermission() {
  // 가장 가까운 메뉴 권한 Provider가 조회한 현재 경로 권한을 반환한다
  return useContext(MenuPermissionContext)
}
