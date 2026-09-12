package org.sadari.admin.sadariadmin.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CsrfRequestFilterTests {

    private final CsrfRequestFilter filter = new CsrfRequestFilter();

    @Test
    void rejectsWriteWithoutCsrf() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/welcome-pages/1/1/deploy");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentAsString()).contains("1004");
        verifyNoInteractions(chain);
    }

    @Test
    void acceptsWriteWithCsrf() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/menus/1/1");
        request.addHeader(CsrfRequestFilter.CSRF_HEADER_NAME, CsrfRequestFilter.CSRF_HEADER_VALUE);
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void acceptsSafeRequest() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/welcome-pages");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }
}
