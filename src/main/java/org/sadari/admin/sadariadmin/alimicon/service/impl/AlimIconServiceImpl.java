package org.sadari.admin.sadariadmin.alimicon.service.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.imageio.ImageIO;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.alimicon.mapper.AlimIconMapper;
import org.sadari.admin.sadariadmin.alimicon.service.AlimIconService;
import org.sadari.admin.sadariadmin.alimicon.vo.AlimIconSearchVO;
import org.sadari.admin.sadariadmin.alimicon.vo.AlimIconVO;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.common.pagination.PageRequest;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * fileName       : AlimIconServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-08-12
 * description    : 알림 아이콘 SVG 및 PNG 검증과 직접 수정을 처리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-12        SeungHyeon.Kang    최초 생성
 * 2026-08-12        SeungHyeon.Kang    알림 상황 식별 구조로 전환
 */
@Service
@Transactional(readOnly = true)
public class AlimIconServiceImpl implements AlimIconService {

    // 관리자가 업로드할 수 있는 아이콘 원본 최대 크기
    private static final long MAX_ICON_FILE_SIZE = 200 * 1024L;

    // 알림 아이콘 코드 허용 형식
    private static final String ALIM_SITU_PATTERN = "^[A-Z][A-Z0-9_]{1,49}$";

    // 알림센터 렌더링 안정성을 위한 최소 한 변 길이
    private static final int MIN_ICON_PIXEL = 16;

    // 과도한 원본 저장을 막기 위한 최대 한 변 길이
    private static final int MAX_ICON_PIXEL = 256;

    // 알림 아이콘 데이터 접근 객체
    private final AlimIconMapper alimIconMapper;

    /**
     * 알림 아이콘 관리 서비스를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param alimIconMapper 알림 아이콘 Mapper
     */
    public AlimIconServiceImpl(AlimIconMapper alimIconMapper) {
        this.alimIconMapper = alimIconMapper;
    }

    /** {@inheritDoc} */
    @Override
    public PageData<AlimIconVO> getAlimIconList(AlimIconSearchVO search, AdminSessionVO admin) {

        checkLogin(admin);
        // 누락된 검색 조건은 첫 페이지 조건으로 보정한다
        AlimIconSearchVO safeSearch = StringUtil.isEmpty(search) ? new AlimIconSearchVO() : search;
        // 현재 페이지의 시작과 종료 행을 계산한다
        PageRequest pageRequest = new PageRequest(safeSearch.getPage());
        // 목록 조회 시작 행을 검색 조건에 설정한다
        safeSearch.setStartRow(pageRequest.getStartRow());
        // 목록 조회 종료 행을 검색 조건에 설정한다
        safeSearch.setEndRow(pageRequest.getEndRow());
        // 아이콘 목록과 전체 건수로 페이지 응답을 반환한다
        return PageData.of(alimIconMapper.getAlimIconList(safeSearch), alimIconMapper.getAlimIconListCount(safeSearch), pageRequest);
    }

    /** {@inheritDoc} */
    @Override
    public AlimIconVO getAlimIconDtl(String alimSitu, AdminSessionVO admin) {

        checkLogin(admin);
        // 알림 상황이 없으면 공통코드와 아이콘 상세를 특정할 수 없어 요청 오류로 분기한다
        if (StringUtil.isEmpty(alimSitu)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }
        // ALIM_SITU 공통코드와 선택적 아이콘 메타데이터를 조회한다
        AlimIconVO icon = alimIconMapper.getAlimIconDtl(alimSitu.trim());
        // 요청한 ALIM_SITU 공통코드가 없으면 조회 결과 없음으로 분기한다
        if (StringUtil.isEmpty(icon)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.ALIM_ICON_NOT_FOUND);
        }
        // 아이콘 미등록 상태를 포함한 알림 상황 상세를 반환한다
        return icon;
    }

    /** {@inheritDoc} */
    @Override
    public AlimIconVO getAlimIconImage(String alimSitu, AdminSessionVO admin) {

        // ALIM_SITU 공통코드 존재 여부와 관리자 로그인을 함께 확인한다
        getAlimIconDtl(alimSitu, admin);
        // 관리자 미리보기에 사용할 아이콘 이미지 원본을 조회한다
        AlimIconVO icon = alimIconMapper.getAlimIconImage(alimSitu.trim());
        // 해당 알림 상황에 아직 이미지가 없으면 미등록 상태로 응답한다
        if (StringUtil.isEmpty(icon)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.ALIM_ICON_NOT_FOUND);
        }
        // 등록된 아이콘 이미지 원본을 반환한다
        return icon;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public AlimIconVO saveAlimIcon(String alimSitu, MultipartFile file
                                 , AdminSessionVO admin) throws IOException {

        checkLogin(admin);
        // 알림 상황은 공통코드 존재 여부를 확인하기 전에 필수 형식을 검증한다
        validateAlimSitu(alimSitu);
        // 임의 코드로 아이콘 행이 생성되지 않도록 ALIM_SITU 공통코드만 허용한다
        if (alimIconMapper.getAlimSituCodeCount(alimSitu.trim()) != 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.ALIM_ICON_SITU_INVALID);
        }
        // SVG 또는 PNG 원본을 형식별 보안 정책으로 검증한다
        AlimIconVO icon = normalizeIcon(file);
        // 공통코드와 감사정보를 아이콘 저장 값에 설정한다
        icon.setAlimSitu(alimSitu.trim());
        icon.setRegiAdmn(admin.getAdmnNumb());
        icon.setUpdtAdmn(admin.getAdmnNumb());
        // 알림 상황 PK를 기준으로 신규 등록 또는 이미지 교체를 수행한다
        alimIconMapper.saveAlimIcon(icon);
        // 저장 직후 공통코드명이 포함된 최신 상세를 반환한다
        return alimIconMapper.getAlimIconDtl(icon.getAlimSitu());
    }

    /** 업로드 형식에 맞춰 SVG를 검사하거나 PNG를 정규화한다. */
    private AlimIconVO normalizeIcon(MultipartFile file) throws IOException {

        // 비어 있거나 제한 크기를 넘는 원본은 형식 판별 전에 거부한다
        if (StringUtil.isEmpty(file) || file.isEmpty() || file.getSize() > MAX_ICON_FILE_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.ALIM_ICON_INVALID);
        }
        // 업로드 바이트를 한 번만 읽어 형식 검증과 저장에 공통 사용한다
        byte[] uploaded = file.getBytes();
        // PNG 고정 시그니처가 있으면 래스터 이미지 검증 경로를 사용한다
        if (hasPngSignature(uploaded)) {
            // 메타데이터를 제거한 안전한 PNG 저장 값을 반환한다
            return normalizePng(uploaded);
        }

        // PNG가 아닌 원본은 안전한 SVG 문서인지 검사한 결과를 반환한다
        return normalizeSvg(uploaded);
    }

    /** PNG 이미지와 크기를 검증하고 안전한 PNG 바이트로 정규화한다. */
    private AlimIconVO normalizePng(byte[] uploaded) throws IOException {

        // 이미지 디코딩 전에 IHDR 크기를 제한해 작은 압축 파일이 과도한 메모리를 할당하지 못하게 한다
        long headerWidth = readPngDimension(uploaded, 16);
        // PNG 헤더의 높이도 같은 방식으로 읽어 정사각형 및 최대 크기 정책을 먼저 검증한다
        long headerHeight = readPngDimension(uploaded, 20);
        // 허용 범위를 벗어난 헤더는 ImageIO가 픽셀 버퍼를 생성하기 전에 차단한다
        if (headerWidth != headerHeight || headerWidth < MIN_ICON_PIXEL || headerWidth > MAX_ICON_PIXEL) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.ALIM_ICON_INVALID);
        }
        // 실제 이미지 디코더로 PNG 내용을 읽어 확장자 위장 파일을 차단한다
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(uploaded));
        // PNG로 해석되지 않거나 정사각형 및 허용 픽셀 범위를 벗어나면 저장하지 않는다
        if (StringUtil.isEmpty(image) || image.getWidth() != image.getHeight()
                || image.getWidth() < MIN_ICON_PIXEL || image.getWidth() > MAX_ICON_PIXEL) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.ALIM_ICON_INVALID);
        }
        // 메타데이터가 제거된 표준 PNG로 다시 인코딩한다
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        // 기본 PNG Writer가 이미지를 기록하지 못한 경우 잘못된 이미지로 처리한다
        if (!ImageIO.write(image, "png", output)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.ALIM_ICON_INVALID);
        }
        // 정규화한 PNG 바이트를 저장 메타데이터 구성에 사용한다
        byte[] normalized = output.toByteArray();
        // 저장할 이미지 바이너리와 크기 정보를 구성한다
        AlimIconVO icon = new AlimIconVO();
        icon.setMimeType("image/png");
        icon.setIconData(normalized);
        icon.setFileSize((long) normalized.length);
        icon.setPixlWdth(image.getWidth());
        icon.setPixlHght(image.getHeight());
        // 검증과 정규화를 마친 아이콘 저장 값을 반환한다
        return icon;
    }

    /** 실행 코드와 외부 리소스가 없는 정사각형 SVG 원본을 검증한다. */
    private AlimIconVO normalizeSvg(byte[] uploaded) {

        try {
            // 잘못된 UTF-8을 대체 문자로 저장하지 않도록 엄격한 디코더로 SVG 문자열을 읽는다
            String svgText = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(uploaded)).toString();
            // 문서형 선언과 엔티티는 외부 개체 및 확장 공격 가능성이 있어 파싱 전에 차단한다
            if (svgText.matches("(?is).*<!\\s*(DOCTYPE|ENTITY).*")) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.ALIM_ICON_INVALID);
            }
            // 외부 개체와 엔티티 확장을 비활성화한 XML 파서를 구성한다
            DocumentBuilderFactory factory = createSecureSvgFactory();
            // 보안 설정이 끝난 파서로 SVG DOM을 생성한다
            Document document = factory.newDocumentBuilder().parse(new ByteArrayInputStream(uploaded));
            // 최상위 요소와 모든 하위 요소의 안전성을 검증한다
            Element root = document.getDocumentElement();
            // SVG 루트 및 네임스페이스가 아니면 이미지 형식 위장으로 처리한다
            if (StringUtil.isEmpty(root) || !"svg".equalsIgnoreCase(root.getLocalName())
                    || !"http://www.w3.org/2000/svg".equals(root.getNamespaceURI())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.ALIM_ICON_INVALID);
            }
            validateSvgElement(root);
            // width와 height가 없으면 viewBox 크기를 사용해 표시 크기를 계산한다
            double[] size = getSvgSize(root);
            // 알림 레이아웃을 유지하도록 정사각형과 허용 크기를 검증한다
            if (Math.abs(size[0] - size[1]) > 0.01 || size[0] < MIN_ICON_PIXEL || size[0] > MAX_ICON_PIXEL) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.ALIM_ICON_INVALID);
            }
            // 검증한 SVG 원본과 표시 크기를 저장 값으로 구성한다
            AlimIconVO icon = new AlimIconVO();
            icon.setMimeType("image/svg+xml");
            icon.setIconData(uploaded);
            icon.setFileSize((long) uploaded.length);
            icon.setPixlWdth((int) Math.round(size[0]));
            icon.setPixlHght((int) Math.round(size[1]));
            // 안전성 검증을 통과한 SVG 저장 값을 반환한다
            return icon;
        }
        // XML 파서 설정, 문법 또는 UTF-8이 잘못된 원본은 동일한 업로드 오류로 변환한다
        catch (ParserConfigurationException | SAXException | CharacterCodingException exception) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.ALIM_ICON_INVALID);
        }
        // DOM 파싱 중 입출력 오류도 내부 경로를 노출하지 않고 업로드 오류로 변환한다
        catch (IOException exception) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.ALIM_ICON_INVALID);
        }
    }

    /** SVG 외부 개체와 엔티티 확장을 차단하는 XML 파서를 구성한다. */
    private DocumentBuilderFactory createSecureSvgFactory() throws ParserConfigurationException {

        // SVG 네임스페이스를 정확히 확인할 수 있는 DOM 팩토리를 생성한다
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        // 외부 자원을 읽지 않는 SVG DOM 팩토리를 반환한다
        return factory;
    }

    /** SVG 요소 트리에서 실행 요소, 이벤트 속성 및 외부 참조를 차단한다. */
    private void validateSvgElement(Element element) {

        // 실행 또는 외부 문서를 포함할 수 있는 요소는 아이콘에서 허용하지 않는다
        String elementName = element.getLocalName();
        if (StringUtil.isEmpty(elementName)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.ALIM_ICON_INVALID);
        }
        elementName = elementName.toLowerCase();
        if (List.of("script", "style", "foreignobject", "iframe", "object", "embed", "image", "audio", "video")
                .contains(elementName)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.ALIM_ICON_INVALID);
        }
        // 현재 요소의 속성을 순회하며 이벤트와 외부 URI를 검사한다
        NamedNodeMap attributes = element.getAttributes();
        for (int index = 0; index < attributes.getLength(); index++) {
            Node attribute = attributes.item(index);
            String name = attribute.getNodeName().toLowerCase();
            String value = attribute.getNodeValue().trim().toLowerCase();
            // SVG와 선택적 xlink 표준 네임스페이스 선언만 외부 URI 검사에서 제외한다
            if (("xmlns".equals(name) && "http://www.w3.org/2000/svg".equals(value))
                    || ("xmlns:xlink".equals(name) && "http://www.w3.org/1999/xlink".equals(value))) {
                continue;
            }
            // 이벤트 처리기와 스크립트 및 네트워크 참조는 이미지 렌더링에 필요하지 않아 차단한다
            if (name.startsWith("on") || value.contains("javascript:") || value.contains("@import")
                    || value.contains("expression(") || value.contains("http:") || value.contains("https:")
                    || value.contains("data:") || value.contains("//") || hasExternalSvgUrl(value)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.ALIM_ICON_INVALID);
            }
            // href 계열 속성은 같은 SVG 내부의 fragment 참조만 허용한다
            if (("href".equals(name) || name.endsWith(":href")) && !value.startsWith("#")) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.ALIM_ICON_INVALID);
            }
        }
        // 모든 자식 요소에 같은 보안 정책을 재귀적으로 적용한다
        NodeList children = element.getChildNodes();
        for (int index = 0; index < children.getLength(); index++) {
            Node child = children.item(index);
            // 텍스트 노드는 제외하고 실제 SVG 요소만 검사한다
            if (child instanceof Element childElement) {
                validateSvgElement(childElement);
            }
        }
    }

    /** CSS URL 표현식이 같은 SVG 내부 fragment 외부를 참조하는지 확인한다. */
    private boolean hasExternalSvgUrl(String value) {

        // 내부 fragment URL을 제거한 뒤 다른 URL 표현식이 남아 있는지 반환한다
        return value.replaceAll("url\\(\\s*#[^)]+\\)", "").contains("url(");
    }

    /** SVG width와 height 또는 viewBox에서 정사각형 검증용 크기를 읽는다. */
    private double[] getSvgSize(Element root) {

        // width와 height가 모두 있으면 px 단위 또는 단위 없는 숫자를 우선 사용한다
        if (!StringUtil.isEmpty(root.getAttribute("width")) && !StringUtil.isEmpty(root.getAttribute("height"))) {
            // 루트 크기 속성에서 계산한 너비와 높이를 반환한다
            return new double[] {parseSvgLength(root.getAttribute("width")), parseSvgLength(root.getAttribute("height"))};
        }
        // 크기 속성이 없으면 네 개 숫자로 구성된 viewBox를 검사한다
        String[] viewBox = root.getAttribute("viewBox").trim().split("[\\s,]+");
        if (viewBox.length != 4) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.ALIM_ICON_INVALID);
        }
        // viewBox의 너비와 높이를 반환한다
        return new double[] {parseSvgNumber(viewBox[2]), parseSvgNumber(viewBox[3])};
    }

    /** SVG 길이에서 허용하는 px 단위 또는 단위 없는 숫자를 읽는다. */
    private double parseSvgLength(String value) {

        // 상대 단위와 계산식은 화면별 크기 차이를 만들 수 있어 허용하지 않는다
        if (!value.trim().matches("[0-9]+(?:\\.[0-9]+)?(?:px)?")) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.ALIM_ICON_INVALID);
        }
        // px 접미사를 제거한 숫자 값을 반환한다
        return parseSvgNumber(value.trim().replaceFirst("(?i)px$", ""));
    }

    /** SVG 크기 문자열을 유한한 양수로 변환한다. */
    private double parseSvgNumber(String value) {

        try {
            // 파싱된 크기가 유한한 양수인지 확인한다
            double number = Double.parseDouble(value);
            if (!Double.isFinite(number) || number <= 0) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.ALIM_ICON_INVALID);
            }
            // 검증된 SVG 크기를 반환한다
            return number;
        }
        // 숫자가 아닌 크기는 동일한 업로드 오류로 변환한다
        catch (NumberFormatException exception) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.ALIM_ICON_INVALID);
        }
    }

    /** 아이콘을 식별하는 알림 상황 코드 형식을 검증한다. */
    private void validateAlimSitu(String alimSitu) {

        // 알림 상황이 비어 있으면 공통코드와 아이콘 행을 특정할 수 없어 저장을 중단한다
        if (StringUtil.isEmpty(alimSitu)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }
        // 공통코드의 물리 규격과 동일한 영문 대문자, 숫자, 밑줄 조합만 허용한다
        if (!alimSitu.matches(ALIM_SITU_PATTERN)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.ALIM_ICON_INVALID);
        }
    }

    /** 업로드 바이트가 PNG 고정 시그니처로 시작하는지 확인한다. */
    private boolean hasPngSignature(byte[] content) {

        // PNG 시그니처 전체 길이보다 짧은 파일은 즉시 잘못된 이미지로 판정한다
        if (StringUtil.isEmpty(content) || content.length < 24) {
            // PNG 형식이 아님을 반환한다
            return false;
        }
        // PNG 표준의 8바이트 고정 시그니처가 모두 일치하는지 반환한다
        return (content[0] & 0xff) == 0x89 && content[1] == 0x50 && content[2] == 0x4e && content[3] == 0x47
                && content[4] == 0x0d && content[5] == 0x0a && content[6] == 0x1a && content[7] == 0x0a
                && content[12] == 0x49 && content[13] == 0x48 && content[14] == 0x44 && content[15] == 0x52;
    }

    /** PNG IHDR의 부호 없는 32비트 크기 값을 읽는다. */
    private long readPngDimension(byte[] content, int offset) {

        // Java의 signed byte를 마스킹한 뒤 PNG big-endian 순서로 크기 값을 조합한다
        return ((content[offset] & 0xffL) << 24) | ((content[offset + 1] & 0xffL) << 16)
                | ((content[offset + 2] & 0xffL) << 8) | (content[offset + 3] & 0xffL);
    }

    /** 로그인 관리자 정보를 확인한다. */
    private void checkLogin(AdminSessionVO admin) {

        // 인증 객체가 없으면 아이콘 원본과 관리 정보를 조회하지 못하게 한다
        if (StringUtil.isEmpty(admin)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_REQUIRED_LOGIN);
        }
    }
}
