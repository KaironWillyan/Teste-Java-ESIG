package br.com.esig.infra.filters;

import java.io.IOException;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.com.esig.domain.models.Usuario;

public class AuthenticationFilter implements Filter {
	private static final Set<String> PUBLIC_PATHS = Set.of(
	        "/login", "/login.xhtml", "/acesso-negado", "/acesso_negado.xhtml", "/"
	    );

	    @Override
	    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
	            throws IOException, ServletException {

	        HttpServletRequest request  = (HttpServletRequest) req;
	        HttpServletResponse response = (HttpServletResponse) res;

	        String contextPath = request.getContextPath();
	        String path = request.getRequestURI().substring(contextPath.length());

	        boolean isResourceRequest = path.startsWith("/javax.faces.resource/");
	        boolean isPublic = isResourceRequest || PUBLIC_PATHS.contains(path);

	        Usuario usuario = null;
	        if (request.getSession(false) != null) {
	            Object u = request.getSession(false).getAttribute("usuarioLogado");
	            if (u instanceof Usuario) usuario = (Usuario) u;
	        }
	        boolean loggedIn = (usuario != null);
	        boolean isAdmin = loggedIn && Boolean.TRUE.equals(usuario.isAdmin());

	        boolean adminOnly = requiresAdmin(path);

	        System.out.printf(">>> [Filtro] path=%s | logged=%s | admin=%s | adminOnly=%s | public=%s%n",
	                path, loggedIn, isAdmin, adminOnly, isPublic);

	        if (!loggedIn && !isPublic) {
	            response.sendRedirect(contextPath + "/login");
	            return;
	        }

	        if (adminOnly && !isAdmin) {
	            response.sendRedirect(contextPath + "/acesso-negado");
	            return;
	        }

	        chain.doFilter(req, res);
	    }

	    private boolean requiresAdmin(String path) {
	        return path.startsWith("/admin")
	            || path.startsWith("/cadastro");
	    }
}