package fisiclinica.util;

import fisiclinica.model.Usuario;

public class UserSession {
    private static UserSession instance;
    private Usuario usuario;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) instance = new UserSession();
        return instance;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Usuario getUsuario() {
        return usuario;
    }
    
    public void logout() {
        this.usuario = null;
    }
}
