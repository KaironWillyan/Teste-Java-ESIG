package br.com.esig.managedbeans;

import java.io.IOException;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import br.com.esig.application.service.UsuarioService;
import br.com.esig.domain.models.Usuario;
import br.com.esig.infra.listeners.SessionListener;

@Named("authBean")
@SessionScoped
public class AuthBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private UsuarioService usuarioService;
    
    private String email;
    private String senha;
    private Usuario usuarioLogado;

    public String login() {

        Usuario usuario = usuarioService.findUsuarioByEmail(this.email);

        if (usuario != null && usuario.isAtivo() && usuarioService.checkPassword(this.senha, usuario.getSenha())) {
            this.usuarioLogado = usuario;

            FacesContext context = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) context.getExternalContext().getSession(true);
            
            SessionListener.registrarLogin(usuario, session);
            
            return "/listagem.xhtml?faces-redirect=true";
        } else {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro de Login", "Email ou senha inválidos."));
            return null; 
        }
    }
    
    public String logout() throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }

    public boolean isLogado() {
        return this.usuarioLogado != null;
    }

    public boolean isAdmin() {
        return this.usuarioLogado != null && this.usuarioLogado.isAdmin();
    }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }
    public Usuario getUsuarioLogado() { return usuarioLogado; }
}