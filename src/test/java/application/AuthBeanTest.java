package application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import br.com.esig.application.service.UsuarioService;
import br.com.esig.domain.models.Usuario;
import br.com.esig.infra.listeners.SessionListener;
import br.com.esig.managedbeans.AuthBean;

class AuthBeanTest {

    private static AuthBean novoAuthBeanCom(UsuarioService usuarioService) throws Exception {
        AuthBean bean = new AuthBean();
        Field f = AuthBean.class.getDeclaredField("usuarioService");
        f.setAccessible(true);
        f.set(bean, usuarioService);
        return bean;
    }

    @Test
    void login_sucesso_deveRedirecionarERegistrarSessao() throws Exception {
        UsuarioService usuarioService = mock(UsuarioService.class);
        FacesContext faces = mock(FacesContext.class);
        ExternalContext ext = mock(ExternalContext.class);
        HttpSession session = mock(HttpSession.class);

        Usuario usuario = new Usuario();
        usuario.setEmail("admin@esig.com.br");
        usuario.setNome("Admin");
        usuario.setAtivo(true);
        usuario.setSenha("$2a$hashfalso");

        when(usuarioService.findUsuarioByEmail("admin@esig.com.br")).thenReturn(usuario);
        when(usuarioService.checkPassword("123", "$2a$hashfalso")).thenReturn(true);

        when(faces.getExternalContext()).thenReturn(ext);
        when(ext.getSession(true)).thenReturn(session);

        try (MockedStatic<FacesContext> mFc = mockStatic(FacesContext.class);
             MockedStatic<SessionListener> mSL = mockStatic(SessionListener.class)) {

            mFc.when(FacesContext::getCurrentInstance).thenReturn(faces);

            AuthBean bean = novoAuthBeanCom(usuarioService);
            bean.setEmail("admin@esig.com.br");
            bean.setSenha("123");

            String outcome = bean.login();

            assertEquals("/listagem.xhtml?faces-redirect=true", outcome);
            mSL.verify(() -> SessionListener.registrarLogin(usuario, session), times(1));
        }
    }

    @Test
    void login_falha_senhaInvalida_deveAdicionarMensagemEVoltarNull() throws Exception {
        UsuarioService usuarioService = mock(UsuarioService.class);
        FacesContext faces = mock(FacesContext.class);

        Usuario usuario = new Usuario();
        usuario.setEmail("user@x.com");
        usuario.setAtivo(true);
        usuario.setSenha("$2a$hashfalso");

        when(usuarioService.findUsuarioByEmail("user@x.com")).thenReturn(usuario);
        when(usuarioService.checkPassword("errada", "$2a$hashfalso")).thenReturn(false);

        try (MockedStatic<FacesContext> mFc = mockStatic(FacesContext.class)) {
            mFc.when(FacesContext::getCurrentInstance).thenReturn(faces);

            AuthBean bean = novoAuthBeanCom(usuarioService);
            bean.setEmail("user@x.com");
            bean.setSenha("errada");

            String outcome = bean.login();
            assertNull(outcome);

            verify(faces).addMessage(eq(null), any(FacesMessage.class));
        }
    }

    @Test
    void logout_deveInvalidarSessaoERedirecionarParaLogin() throws Exception {
        UsuarioService usuarioService = mock(UsuarioService.class);
        FacesContext faces = mock(FacesContext.class);
        ExternalContext ext = mock(ExternalContext.class);

        when(faces.getExternalContext()).thenReturn(ext);

        try (MockedStatic<FacesContext> mFc = mockStatic(FacesContext.class)) {
            mFc.when(FacesContext::getCurrentInstance).thenReturn(faces);

            AuthBean bean = novoAuthBeanCom(usuarioService);
            String outcome = bean.logout();

            assertEquals("/login.xhtml?faces-redirect=true", outcome);
            verify(ext).invalidateSession();
        }
    }
}