package br.com.esig.infra.listeners;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import br.com.esig.domain.models.Usuario;

@WebListener
public class SessionListener implements HttpSessionListener {

    private static final Map<String, HttpSession> SESSIONS = new ConcurrentHashMap<>();

    @Override
    public void sessionCreated(HttpSessionEvent se) {
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        SESSIONS.values().remove(se.getSession());
    }

    public static void registrarLogin(Usuario usuario, HttpSession newSession) {
        String email = usuario.getEmail();
        
        if (SESSIONS.containsKey(email)) {
            HttpSession oldSession = SESSIONS.get(email);
            if (oldSession != null && !oldSession.getId().equals(newSession.getId())) {
                try {
                    System.out.println(">>> Utilizador " + email + " já tem uma sessão ativa. Invalidando a sessão antiga: " + oldSession.getId());
                    oldSession.invalidate(); 
                } catch (IllegalStateException e) {
                }
            }
        }
        
        SESSIONS.put(email, newSession);
        newSession.setAttribute("usuarioLogado", usuario);
    }
}