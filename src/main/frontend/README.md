# React + TypeScript + Vite

## 관리자 로그인과 홈 화면 테스트 케이스

로그인 후 기본 경로는 `/sadari/adm`이며, 사이드바와 상단 영역을 제외한 본문에는 화면을 표시하지 않습니다. 첫 번째 메뉴로 자동 이동하는 동작을 로그인 성공 조건으로 사용하지 않습니다.

| 케이스 | 실행 절차 | 기대 결과 |
| --- | --- | --- |
| 정상 로그인 | 접근 가능한 메뉴가 있는 관리자 계정으로 로그인 | `/sadari/adm`으로 이동하고 본문은 비어 있으며 특정 메뉴가 자동 선택되지 않음 |
| 접근 가능한 메뉴 없음 | 메뉴가 없는 관리자 계정으로 로그인 | 동일한 빈 홈 표시, 메뉴 권한 오류와 자동 이동 없음 |
| 홈 새로고침 | 로그인 후 홈에서 새로고침 | `/sadari/adm`과 빈 본문 유지 |
| 로그인 세션 복원 | 로그인 세션이 있는 상태에서 `/sadari/adm/login` 접근 | 특정 메뉴 대신 `/sadari/adm`으로 이동 |
| 메뉴 직접 선택 | 홈의 사이드바에서 접근 가능한 메뉴 선택 | 선택한 메뉴 화면 표시, 기존 메뉴 권한 검증 적용 |
| 메뉴 새로고침 | 선택한 메뉴 화면에서 새로고침 | 해당 메뉴 경로와 화면 유지 |
| 비로그인 접근 | 세션이 없는 상태에서 홈 접근 | 로그인 화면 표시 |

홈에서는 메뉴별 권한 조회와 업무 데이터 조회 요청이 발생하지 않는지도 확인합니다. 위 표는 회귀 검증 기준이며 실행 결과를 의미하지 않습니다.

This template provides a minimal setup to get React working in Vite with HMR and some ESLint rules.

Currently, two official plugins are available:

- [@vitejs/plugin-react](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react) uses [Oxc](https://oxc.rs)
- [@vitejs/plugin-react-swc](https://github.com/vitejs/vite-plugin-react/blob/main/packages/plugin-react-swc) uses [SWC](https://swc.rs/)

## React Compiler

The React Compiler is enabled on this template. See [this documentation](https://react.dev/learn/react-compiler) for more information.

Note: This will impact Vite dev & build performances.

## Expanding the ESLint configuration

If you are developing a production application, we recommend updating the configuration to enable type-aware lint rules:

```js
export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...

      // Remove tseslint.configs.recommended and replace with this
      tseslint.configs.recommendedTypeChecked,
      // Alternatively, use this for stricter rules
      tseslint.configs.strictTypeChecked,
      // Optionally, add this for stylistic rules
      tseslint.configs.stylisticTypeChecked,

      // Other configs...
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])

```

You can also install [eslint-plugin-react-x](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-x) and [eslint-plugin-react-dom](https://github.com/Rel1cx/eslint-react/tree/main/packages/plugins/eslint-plugin-react-dom) for React-specific lint rules:

```js
// eslint.config.js
import reactX from 'eslint-plugin-react-x'
import reactDom from 'eslint-plugin-react-dom'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...
      // Enable lint rules for React
      reactX.configs['recommended-typescript'],
      // Enable lint rules for React DOM
      reactDom.configs.recommended,
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])

```
