package org.sadari.admin.sadariadmin.alim.service.impl;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.alim.mapper.AlimTempMapper;
import org.sadari.admin.sadariadmin.alim.service.AlimTempService;
import org.sadari.admin.sadariadmin.alim.vo.AlimTempVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 알림 템플릿 관리 서비스 구현체
 */
@Service
public class AlimTempServiceImpl implements AlimTempService {

    /** 템플릿 코드 입력 형식 */
    private static final String TEMP_CODE_PATTERN = "^[A-Z_]+$";

    /** 알림 템플릿 Mapper */
    private final AlimTempMapper alimTempMapper;

    /**
     * 알림 템플릿 관리 서비스 생성
     * @Author SeungHyeon.Kang
     * @param alimTempMapper
     * @return
     */
    public AlimTempServiceImpl(AlimTempMapper alimTempMapper) {
        this.alimTempMapper = alimTempMapper;
    }

    /**
     * 알림 템플릿 목록 조회
     * @Author SeungHyeon.Kang
     * @param admin
     * @return
     */
    @Override
    public List<AlimTempVO> getAlimTempList(AdminSessionVO admin) {
        checkLogin(admin);
        return alimTempMapper.getAlimTempList();
    }

    /**
     * 알림 템플릿 상세 조회
     * @Author SeungHyeon.Kang
     * @param alimSitu
     * @param tempCode
     * @param admin
     * @return
     */
    @Override
    public AlimTempVO getAlimTempDtl(String alimSitu, String tempCode, AdminSessionVO admin) {
        checkLogin(admin);
        AlimTempVO alimTemp = alimTempMapper.getAlimTempDtl(alimSitu, tempCode);
        // 복합키에 해당하는 알림 템플릿이 없으면 조회 결과 없음으로 분기한다
        if (StringUtil.isEmpty(alimTemp)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.ALIM_TEMP_NOT_FOUND);
        }
        return alimTemp;
    }

    /**
     * 알림 템플릿 중복 여부 조회
     * @Author SeungHyeon.Kang
     * @param alimSitu
     * @param tempCode
     * @param admin
     * @return
     */
    @Override
    public boolean getAlimTempDuplicate(String alimSitu, String tempCode, AdminSessionVO admin) {
        checkLogin(admin);
        validateKey(alimSitu, tempCode);
        return alimTempMapper.getAlimTempCnt(alimSitu, tempCode) > 0;
    }

    /**
     * 알림 템플릿 등록
     * @Author SeungHyeon.Kang
     * @param alimTemp
     * @param admin
     * @return
     */
    @Override
    @Transactional
    public AlimTempVO setAlimTemp(AlimTempVO alimTemp, AdminSessionVO admin) {
        checkLogin(admin);
        validateRequired(alimTemp);
        // 같은 알림 상황 안에서는 템플릿 코드를 중복 등록할 수 없으므로 저장 전에 중복을 확인한다
        if (alimTempMapper.getAlimTempCnt(alimTemp.getAlimSitu(), alimTemp.getTempCode()) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.ALIM_TEMP_DUPLICATE);
        }
        setAlimTempDefault(alimTemp, admin);
        alimTempMapper.setAlimTemp(alimTemp);
        return alimTempMapper.getAlimTempDtl(alimTemp.getAlimSitu(), alimTemp.getTempCode());
    }

    /**
     * 알림 템플릿 수정
     * @Author SeungHyeon.Kang
     * @param oldAlimSitu
     * @param oldTempCode
     * @param alimTemp
     * @param admin
     * @return
     */
    @Override
    @Transactional
    public AlimTempVO uptAlimTemp(String oldAlimSitu, String oldTempCode, AlimTempVO alimTemp, AdminSessionVO admin) {
        checkLogin(admin);
        validateKey(oldAlimSitu, oldTempCode);
        validateRequired(alimTemp);
        getAlimTempDtl(oldAlimSitu, oldTempCode, admin);

        boolean changedKey = !oldAlimSitu.equals(alimTemp.getAlimSitu()) || !oldTempCode.equals(alimTemp.getTempCode());
        // 기존 복합키가 바뀐 경우에는 변경될 복합키가 이미 존재하는지 확인한다
        if (changedKey && alimTempMapper.getAlimTempCnt(alimTemp.getAlimSitu(), alimTemp.getTempCode()) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.ALIM_TEMP_DUPLICATE);
        }

        setAlimTempDefault(alimTemp, admin);
        alimTempMapper.uptAlimTemp(oldAlimSitu, oldTempCode, alimTemp);
        return alimTempMapper.getAlimTempDtl(alimTemp.getAlimSitu(), alimTemp.getTempCode());
    }

    /**
     * 알림 템플릿 필수값 검증
     * @Author SeungHyeon.Kang
     * @param alimTemp
     * @return
     */
    private void validateRequired(AlimTempVO alimTemp) {
        // 저장 필수 항목 중 하나라도 비어 있으면 올바르지 않은 요청으로 분기한다
        if (StringUtil.isEmpty(alimTemp)
                || StringUtil.isEmpty(alimTemp.getAlimSitu())
                || StringUtil.isEmpty(alimTemp.getTempCode())
                || StringUtil.isEmpty(alimTemp.getTempTitl())
                || StringUtil.isEmpty(alimTemp.getTempCont())
                || StringUtil.isEmpty(alimTemp.getLinkUrlx())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }
        // 템플릿 코드는 영문 대문자와 밑줄만 허용한다
        if (!alimTemp.getTempCode().matches(TEMP_CODE_PATTERN)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }
    }

    /**
     * 알림 템플릿 복합키 검증
     * @Author SeungHyeon.Kang
     * @param alimSitu
     * @param tempCode
     * @return
     */
    private void validateKey(String alimSitu, String tempCode) {
        // 복합키를 구성하는 값이 비어 있으면 상세 조회와 중복 검사를 진행할 수 없어 요청 오류로 분기한다
        if (StringUtil.isEmpty(alimSitu) || StringUtil.isEmpty(tempCode)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }
    }

    /**
     * 알림 템플릿 기본값 설정
     * @Author SeungHyeon.Kang
     * @param alimTemp
     * @param admin
     * @return
     */
    private void setAlimTempDefault(AlimTempVO alimTemp, AdminSessionVO admin) {
        // 사용 여부가 없으면 사용 상태로 저장한다
        if (StringUtil.isEmpty(alimTemp.getUseeYsno())) {
            alimTemp.setUseeYsno(Constant.YES);
        }
        alimTemp.setRegiAdmn(admin.getAdmnNumb());
        alimTemp.setUpdtAdmn(admin.getAdmnNumb());
    }

    /**
     * 로그인 상태 확인
     * @Author SeungHyeon.Kang
     * @param admin
     * @return
     */
    private void checkLogin(AdminSessionVO admin) {
        // 인증 객체가 없으면 로그인하지 않은 요청으로 판단한다
        if (StringUtil.isEmpty(admin)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_REQUIRED_LOGIN);
        }
    }
}
