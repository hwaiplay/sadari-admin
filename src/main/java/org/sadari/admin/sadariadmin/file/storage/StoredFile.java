package org.sadari.admin.sadariadmin.file.storage;

/**
 * fileName       : StoredFile
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 저장소에서 조회한 파일 바이트와 MIME 유형을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 *
 * @param bytes 파일 바이트
 * @param contentType 파일 MIME 유형
 */
public record StoredFile(byte[] bytes, String contentType) {
}
