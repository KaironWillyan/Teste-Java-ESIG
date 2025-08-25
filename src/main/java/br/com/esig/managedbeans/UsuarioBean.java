package br.com.esig.managedbeans;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import br.com.esig.application.service.RelatorioService;
import br.com.esig.application.service.UsuarioService;
import br.com.esig.domain.models.Usuario;
import br.com.esig.util.FacesUtils; 

@Named("usuarioBean")
@ViewScoped
public class UsuarioBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private UsuarioService usuarioService;
    @Inject
    private RelatorioService relatorioService;   

    private List<Usuario> usuarios;
    private Usuario selectedUsuario;
    
    private String filtroNome;
    private String filtroEmail;
    private boolean incluirInativos = false;
    private boolean filtroAdmin = false; 

    @PostConstruct
    public void init() {
        buscar(); 
    }

    public void buscar() {
        Boolean isAdminStatus = filtroAdmin ? true : null;
        usuarios = usuarioService.findUsuarios(filtroNome, filtroEmail, isAdminStatus, incluirInativos);
    }

    public void novoUsuario() {
        this.selectedUsuario = new Usuario();
    }
       
    public void salvar() {
        try {
            usuarioService.saveUsuario(selectedUsuario);
            buscar(); 
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage("Sucesso", "Usuário salvo com sucesso!"));
            
            org.primefaces.PrimeFaces.current().executeScript("PF('usuarioDialog').hide()");
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível salvar o usuário. Verifique se o email já existe."));
        }
    }
    
    public void toggleStatus(Usuario usuario) {
        try {
            boolean eraAtivo = usuario.isAtivo(); 
            usuarioService.toggleStatusUsuario(usuario);
            buscar();
            
            String acao = eraAtivo ? "desativado" : "ativado";
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage("Sucesso", "Usuário " + acao + " com sucesso!"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível alterar o status do usuário."));
        }
    }

    public void gerarRelatorioPDF() {
        try {
            List<Usuario> dados = this.usuarios;
            
            byte[] pdf = relatorioService.gerarRelatorioPDF("relatorio_usuarios", dados);

            FacesUtils.streamInline(pdf, "relatorio_usuarios.pdf");
        } catch (Exception e) {
        	 FacesContext.getCurrentInstance().addMessage(null, 
                     new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível gerar o relatório."));
                 e.printStackTrace();
        }
    }
    
    public List<Usuario> getUsuarios() {
    	return usuarios; 
    }
    
    public int getUsuariosCount() { 
    	return usuarios != null ? usuarios.size() : 0; 
    }
    
    public Usuario getSelectedUsuario() { 
    	return selectedUsuario;
    }
    
    public void setSelectedUsuario(Usuario selectedUsuario) { 
    	this.selectedUsuario = selectedUsuario; 
    }
    
    public String getFiltroNome() { 
    	return filtroNome; 
    }
    
    public void setFiltroNome(String filtroNome) { 
    	this.filtroNome = filtroNome; 
    }
    
    public String getFiltroEmail() { 
    	return filtroEmail; 
    }
    
    public void setFiltroEmail(String filtroEmail) { 
    	this.filtroEmail = filtroEmail; 
    }
    
    public boolean isIncluirInativos() { 
    	return incluirInativos; 
    }
    
    public void setIncluirInativos(boolean incluirInativos) { 
    	this.incluirInativos = incluirInativos; 
    }

	public boolean isFiltroAdmin() {
		return filtroAdmin;
	}

	public void setFiltroAdmin(boolean filtroAdmin) {
		this.filtroAdmin = filtroAdmin;
	}
}