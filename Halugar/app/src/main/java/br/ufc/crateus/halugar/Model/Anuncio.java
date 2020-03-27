package br.ufc.crateus.halugar.Model;

public class Anuncio implements Comparable <Anuncio>{

    private String aId;
    private String endereco;
    private int numero;
    private String complemento;
    private String cep;
    private String bairro;
    private String cidade;
    private String estado;
    private double precoAluguel;
    private int qtdVagas;
    private String informacoesAdicionais;
    private String keyUsuario;
    private String urlImagemPrincipal, urlImagemDois, urlImagemTres, urlImagemQuatro, urlImagemCinco;
    double latitude, longitude;

    public Anuncio(){

    }

    // Construtor para Anunciar e Editar Anuncio etc.

    public Anuncio(String endereco, int numero, String complemento, String cep, String bairro, String cidade, String estado, double precoAluguel, int qtdVagas, String informacoesAdicionais, String keyUsuario, String urlImagemPrincipal, String urlImagemDois, String urlImagemTres, String urlImagemQuatro, String urlImagemCinco, double latitude, double longitude) {

        this.endereco = endereco;
        this.numero = numero;
        this.complemento = complemento;
        this.cep = cep;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.precoAluguel = precoAluguel;
        this.qtdVagas = qtdVagas;
        this.informacoesAdicionais = informacoesAdicionais;
        this.keyUsuario = keyUsuario;
        this.urlImagemPrincipal = urlImagemPrincipal;
        this.urlImagemDois = urlImagemDois;
        this.urlImagemTres = urlImagemTres;
        this.urlImagemQuatro = urlImagemQuatro;
        this.urlImagemCinco = urlImagemCinco;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Construtor para o adapter (visualizar dados dos anúncios)

    public Anuncio(String anuncioKey, String endereco, int numero, String complemento, String cep, String bairro, String cidade, String estado, double precoAluguel, int qtdVagas, String informacoesAdicionais, String keyUsuario, String urlImagemPrincipal, String urlImagemDois, String urlImagemTres, String urlImagemQuatro, String urlImagemCinco, double latitude, double longitude) {
        this.aId = anuncioKey;
        this.endereco = endereco;
        this.numero = numero;
        this.complemento = complemento;
        this.cep = cep;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.precoAluguel = precoAluguel;
        this.qtdVagas = qtdVagas;
        this.informacoesAdicionais = informacoesAdicionais;
        this.keyUsuario = keyUsuario;
        this.urlImagemPrincipal = urlImagemPrincipal;
        this.urlImagemDois = urlImagemDois;
        this.urlImagemTres = urlImagemTres;
        this.urlImagemQuatro = urlImagemQuatro;
        this.urlImagemCinco = urlImagemCinco;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getaId() {
        return aId;
    }

    public void setaId(String aId) {
        this.aId = aId;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public double getPrecoAluguel() {
        return precoAluguel;
    }

    public void setPrecoAluguel(double precoAluguel) {
        this.precoAluguel = precoAluguel;
    }

    public int getQtdVagas() {
        return qtdVagas;
    }

    public void setQtdVagas(int qtdVagas) {
        this.qtdVagas = qtdVagas;
    }

    public String getInformacoesAdicionais() {
        return informacoesAdicionais;
    }

    public void setInformacoesAdicionais(String informacoesAdicionais) {
        this.informacoesAdicionais = informacoesAdicionais;
    }

    public String getKeyUsuario() {
        return keyUsuario;
    }

    public void setKeyUsuario(String keyUsuario) {
        this.keyUsuario = keyUsuario;
    }

    public String getUrlImagemPrincipal() {
        return urlImagemPrincipal;
    }

    public void setUrlImagemPrincipal(String urlImagemPrincipal) {
        this.urlImagemPrincipal = urlImagemPrincipal;
    }

    public String getUrlImagemDois() {
        return urlImagemDois;
    }

    public void setUrlImagemDois(String urlImagemDois) {
        this.urlImagemDois = urlImagemDois;
    }

    public String getUrlImagemTres() {
        return urlImagemTres;
    }

    public void setUrlImagemTres(String urlImagemTres) {
        this.urlImagemTres = urlImagemTres;
    }

    public String getUrlImagemQuatro() {
        return urlImagemQuatro;
    }

    public void setUrlImagemQuatro(String urlImagemQuatro) {
        this.urlImagemQuatro = urlImagemQuatro;
    }

    public String getUrlImagemCinco() {
        return urlImagemCinco;
    }

    public void setUrlImagemCinco(String urlImagemCinco) {
        this.urlImagemCinco = urlImagemCinco;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitute) {
        this.latitude = latitute;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Anuncio{" +
                "aId='" + aId + '\'' +
                ", endereco='" + endereco + '\'' +
                ", numero=" + numero +
                ", complemento='" + complemento + '\'' +
                ", cep='" + cep + '\'' +
                ", bairro='" + bairro + '\'' +
                ", cidade='" + cidade + '\'' +
                ", estado='" + estado + '\'' +
                ", precoAluguel=" + precoAluguel +
                ", qtdVagas=" + qtdVagas +
                ", informacoesAdicionais='" + informacoesAdicionais + '\'' +
                ", keyUsuario='" + keyUsuario + '\'' +
                ", urlImagemPrincipal='" + urlImagemPrincipal + '\'' +
                ", urlImagemDois='" + urlImagemDois + '\'' +
                ", urlImagemTres='" + urlImagemTres + '\'' +
                ", urlImagemQuatro='" + urlImagemQuatro + '\'' +
                ", urlImagemCinco='" + urlImagemCinco + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    @Override
    public int compareTo(Anuncio anuncio) {

        double preco = anuncio.precoAluguel;

        return (int)(this.precoAluguel-preco); // Ordem crescente
    }
}
