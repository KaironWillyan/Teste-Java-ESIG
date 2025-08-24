package application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mindrot.jbcrypt.BCrypt;

import br.com.esig.application.service.UsuarioService;
import br.com.esig.domain.models.Usuario;
import br.com.esig.domain.repositories.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock private EntityManager manager;
    @Mock private EntityTransaction tx;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService service;

    @Test
    void saveUsuario_deveHashearSenhaSeForPlana_eCommitar() {
        when(manager.getTransaction()).thenReturn(tx);

        Usuario u = new Usuario();
        u.setId(1L);
        u.setEmail("a@b.com");
        u.setSenha("segredo123");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

        service.saveUsuario(u);

        verify(tx).begin();
        verify(usuarioRepository).saveMerge(captor.capture());
        verify(tx).commit();

        Usuario salvo = captor.getValue();
        assertNotNull(salvo.getSenha());
        assertTrue(salvo.getSenha().startsWith("$2a$"));
        assertTrue(BCrypt.checkpw("segredo123", salvo.getSenha()));
    }

    @Test
    void saveUsuario_naoDeveRehashearSeJaForBCrypt() {
        when(manager.getTransaction()).thenReturn(tx);

        String hash = BCrypt.hashpw("abc", BCrypt.gensalt());

        Usuario u = new Usuario();
        u.setId(2L);
        u.setEmail("c@d.com");
        u.setSenha(hash);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);

        service.saveUsuario(u);

        verify(usuarioRepository).saveMerge(captor.capture());
        assertEquals(hash, captor.getValue().getSenha());
    }

    @Test
    void toggleStatusUsuario_deveAlternarESalvar_eCommitar() {
        when(manager.getTransaction()).thenReturn(tx);

        Usuario existente = new Usuario();
        existente.setId(3L);
        existente.setAtivo(true);

        when(usuarioRepository.find(3L)).thenReturn(existente);

        Usuario arg = new Usuario();
        arg.setId(3L);

        service.toggleStatusUsuario(arg);

        assertFalse(existente.isAtivo());
        verify(usuarioRepository).save(existente);
        verify(tx).begin();
        verify(tx).commit();
    }

    @Test
    void toggleStatusUsuario_quandoNaoEncontrarAindaCommitarSemSalvar() {
        when(manager.getTransaction()).thenReturn(tx);
        when(usuarioRepository.find(99L)).thenReturn(null);

        Usuario arg = new Usuario();
        arg.setId(99L);

        service.toggleStatusUsuario(arg);

        verify(usuarioRepository, never()).save(any());
        verify(tx).commit();
    }

    @Test
    void toggleStatusUsuario_emExcecaoDeveFazerRollbackEPropagar() {
        when(manager.getTransaction()).thenReturn(tx);
        when(tx.isActive()).thenReturn(true);

        Usuario existente = new Usuario();
        existente.setId(4L);
        existente.setAtivo(true);

        when(usuarioRepository.find(4L)).thenReturn(existente);
        doThrow(new RuntimeException("falha")).when(usuarioRepository).save(existente);

        Usuario arg = new Usuario();
        arg.setId(4L);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.toggleStatusUsuario(arg));
        assertEquals("falha", ex.getMessage());

        verify(tx).rollback();
    }


    @Test
    void findUsuarios_deveDelegarParaRepositorio() {
        when(usuarioRepository.findWithFilters("nome", "email@x", true, false))
            .thenReturn(List.of(new Usuario()));

        var out = service.findUsuarios("nome", "email@x", true, false);

        assertEquals(1, out.size());
    }

    @Test
    void findUsuarioByEmail_deveDelegar() {
        Usuario u = new Usuario();
        u.setEmail("z@y.com");
        when(usuarioRepository.findByEmail("z@y.com")).thenReturn(u);

        var out = service.findUsuarioByEmail("z@y.com");

        assertNotNull(out);
        assertEquals("z@y.com", out.getEmail());
    }

    @Test
    void checkPassword_casosBasicos() {
        assertFalse(service.checkPassword(null, null));
        assertFalse(service.checkPassword("abc", null));
        assertFalse(service.checkPassword(null, "hash"));

        String hash = BCrypt.hashpw("segredo", BCrypt.gensalt());
        assertTrue(service.checkPassword("segredo", hash));
        assertFalse(service.checkPassword("errado", hash));
    }
}
