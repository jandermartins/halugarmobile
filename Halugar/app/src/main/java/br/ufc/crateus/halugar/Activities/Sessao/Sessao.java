package br.ufc.crateus.halugar.Activities.Sessao;

import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Sessao {

    FirebaseUser usuario;
    FirebaseAuth autenticacaoUsuario;
    String idUsuario, emailUsuario;

    private static Sessao instancia = new Sessao();

    public Sessao(){

        autenticacaoUsuario = FirebaseAuth.getInstance();

        if(autenticacaoUsuario!=null) {
            usuario = autenticacaoUsuario.getCurrentUser();
        }

        if(usuario!=null) {
            idUsuario = usuario.getUid();
            emailUsuario = usuario.getEmail();
        }
    }

    public FirebaseAuth getAutenticacaoUsuario(){

        autenticacaoUsuario = FirebaseAuth.getInstance();

        return autenticacaoUsuario;
    }

    public FirebaseUser getUsuario(){

        autenticacaoUsuario = FirebaseAuth.getInstance();

        if(autenticacaoUsuario!=null) {
            return autenticacaoUsuario.getCurrentUser();
        }

        return null;
    }

    public String getIdUsuario(){

        autenticacaoUsuario = FirebaseAuth.getInstance();

        if(autenticacaoUsuario!=null) {
            usuario = autenticacaoUsuario.getCurrentUser();

            if(usuario!=null){
                return usuario.getUid();
            }
        }

        return null;
    }

    public String getEmailUsuario(){
        autenticacaoUsuario = FirebaseAuth.getInstance();
        if(autenticacaoUsuario!=null) {
            usuario = autenticacaoUsuario.getCurrentUser();
            if(usuario!=null){
                return usuario.getEmail();
            }
        }
        return null;
    }

    public static Sessao getInstance(){
        if(instancia==null) {
            return new Sessao();
        }
        return instancia;
    }

    public static void logout(){
        instancia.getAutenticacaoUsuario().signOut(); // Logout Firebase (Email or Gmail account)
        LoginManager.getInstance().logOut(); // Logout (Facebook account)
    }
}
