package org.sadari.admin.sadariadmin.notice.vo;

/**
 * fileName       : NoticeImageVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : Summernote 본문 이미지 업로드 결과를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 *
 * @param url 사용자와 관리자가 같은 저장소에서 조회할 이미지 URL
 */
public record NoticeImageVO(String url) {
}
