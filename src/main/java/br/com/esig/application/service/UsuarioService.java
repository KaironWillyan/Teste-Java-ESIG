package br.com.esig.application.service;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.mindrot.jbcrypt.BCrypt;

import br.com.esig.domain.models.Usuario;
import br.com.esig.domain.repositories.UsuarioRepository;

@ApplicationScoped
public class UsuarioService implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject 
    private EntityManager manager;
    @Inject 
    private UsuarioRepository usuarioRepository;

    public void saveUsuario(Usuario usuario) {
        try {
            manager.getTransaction().begin();
            
            if (usuario.getSenha() != null && !usuario.getSenha().startsWith("$2a$")) {
                String hash = BCrypt.hashpw(usuario.getSenha(), BCrypt.gensalt());
                usuario.setSenha(hash);
            }
            
            usuarioRepository.saveMerge(usuario);
            manager.getTransaction().commit();
        } catch (Exception e) {
            if (manager.getTransaction().isActive()) manager.getTransaction().rollback();
            throw e;
        }
    }
    
    public List<Usuario> findUsuarios(String nome, String email, Boolean isAdmin, boolean incluirInativos) {
        return usuarioRepository.findWithFilters(nome, email, isAdmin, incluirInativos);
    }
    
    public void toggleStatusUsuario(Usuario user) {
        try {
            manager.getTransaction().begin();
            Usuario usuario = usuarioRepository.find(user.getId());
            if (usuario != null) {
            	usuario.setAtivo(!usuario.isAtivo()); 
                usuarioRepository.save(usuario);
            }
            manager.getTransaction().commit();
        } catch (Exception e) {
            if (manager.getTransaction().isActive()) {
                manager.getTransaction().rollback();
            }
            throw e;
        }
    }
    
    public Usuario findUsuarioByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public boolean checkPassword(String senhaPlana, String hashDaSenha) {
        if (senhaPlana == null || hashDaSenha == null) {
            return false;
        }
        return BCrypt.checkpw(senhaPlana, hashDaSenha);
    }
}