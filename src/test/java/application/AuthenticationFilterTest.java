package application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;

import br.com.esig.domain.models.Usuario;
import br.com.esig.infra.filters.AuthenticationFilter;

class AuthenticationFilterTest {

    private final AuthenticationFilter filter = new AuthenticationFilter();

    @Test
    void deveRedirecionarParaLoginQuandoNaoLogado() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getContextPath()).thenReturn("/esig-teste");
        when(req.getRequestURI()).thenReturn("/esig-teste/listagem.xhtml");
        when(req.getSession(false)).thenReturn(null);

        filter.doFilter(req, res, chain);

        verify(res).sendRedirect("/esig-teste/login");
        verify(chain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }

    @Test
    void devePermitirPaginaDeLogin() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getContextPath()).thenReturn("/esig-teste");
        when(req.getRequestURI()).thenReturn("/esig-teste/login.xhtml");

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verify(res, never()).sendRedirect(anyString());
    }

    @Test
    void devePermitirRecursosJSF() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        when(req.getContextPath()).thenReturn("/esig-teste");
        when(req.getRequestURI()).thenReturn("/esig-teste/javax.faces.resource/primefaces.js");

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verify(res, never()).sendRedirect(anyString());
    }

    @Test
    void devePermitirQuandoLogado() throws Exception {
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse res = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);
        HttpSession session = mock(HttpSession.class);

        when(req.getContextPath()).thenReturn("/esig-teste");
        when(req.getRequestURI()).thenReturn("/esig-teste/listagem.xhtml");
        when(req.getSession(false)).thenReturn(session);
        when(session.getAttribute("usuarioLogado")).thenReturn(new Usuario());

        filter.doFilter(req, res, chain);

        verify(chain).doFilter(req, res);
        verify(res, never()).sendRedirect(anyString());
    }
}
