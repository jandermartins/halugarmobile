package br.ufc.crateus.halugar.Model;

public class Usuario {

    private String uId;
    private String uKey;
    private String nomeCompleto;
    private String email;
    private String telefone;

    public Usuario(String uId, String uKey, String nomeCompleto, String email, String telefone) {
        this.uId = uId;
        this.uKey = uKey;
        this.nomeCompleto = nomeCompleto;
        this.email = email;
        this.telefone = telefone;
    }

    public Usuario(){

    }

    @Override
    public String toString() {
        return "Usuario{" +
                "uId='" + uId + '\'' +
                ", uKey='" + uKey + '\'' +
                ", nomeCompleto='" + nomeCompleto + '\'' +
                ", email='" + email + '\'' +
                ", telefone='" + telefone + '\'' +
                '}';
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getuKey() {
        return uKey;
    }

    public void setuKey(String uKey) {
        this.uKey = uKey;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }
}
