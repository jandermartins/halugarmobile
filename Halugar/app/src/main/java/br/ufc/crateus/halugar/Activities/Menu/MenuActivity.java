package br.ufc.crateus.halugar.Activities.Menu;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import br.ufc.crateus.halugar.Activities.Anunciar.AnunciarDadosImovelActivity;
import br.ufc.crateus.halugar.Activities.Anunciar.AnunciarFotosImovelActivity;
import br.ufc.crateus.halugar.Activities.Favoritos.FavoritosActivity;
import br.ufc.crateus.halugar.Activities.MeuCadastro.EditarCadastroActivity;
import br.ufc.crateus.halugar.Activities.MeusAnuncios.EditarMeuAnuncioDados;
import br.ufc.crateus.halugar.Activities.MeusAnuncios.EditarMeuAnuncioFotos;
import br.ufc.crateus.halugar.Activities.MeusAnuncios.MeuAnuncioDados;
import br.ufc.crateus.halugar.Activities.MeusAnuncios.MeuAnuncioFotos;
import br.ufc.crateus.halugar.Activities.Procurar.FiltrarActivity;
import br.ufc.crateus.halugar.Activities.Procurar.ProximidadesActivity;
import br.ufc.crateus.halugar.Activities.main.MainActivity;
import br.ufc.crateus.halugar.Activities.MeusAnuncios.MeusAnunciosActivity;
import br.ufc.crateus.halugar.Activities.MeuCadastro.MeuCadastroActivity;
import br.ufc.crateus.halugar.Activities.Sessao.Sessao;
import br.ufc.crateus.halugar.Activities.Procurar.ProcurarActivity;
import br.ufc.crateus.halugar.Banco.Banco;
import br.ufc.crateus.halugar.Model.Usuario;
import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.InternetCheck;
import br.ufc.crateus.halugar.Util.OnSwipeTouchListener;
import br.ufc.crateus.halugar.Util.Transition;

public class MenuActivity extends AppCompatActivity {

    ImageButton btnVoltarMenu, btnTodosAnuncios, btnAnunciarMenu, btnMeuCadastro, btnMeuAnuncio, btnProcurarMenu, btnFavoritos;
    Button btnSair;

    final Context context = this;

    View viewMenu;
    ConstraintLayout layoutMenu;

    Sessao sessao;
    Banco banco;

    Bundle extras;
    String callerIntent;

    // AnunciarDadosImovelActivity e AnunciarFotosImovelActivity e EditarMeuAnuncioFotos e EditarMeuAnuncioDados extras:
    String endereco="", numero="", complemento="", cep="", bairro="", cidade="", estadoSelecionado="", estadoAtual="", preco="", vagas="", informacoes="";
    String enderecoNovo="", numeroNovo="", complementoNovo="", cepNovo="", bairroNovo="", cidadeNovo="", precoNovo="", vagasNovo="", informacoesNovo="";
    String urlImagemPrincipal="", urlImagemDois="", urlImagemTres="", urlImagemQuatro="", urlImagemCinco="";
    String urlImagemPrincipalNovo="", urlImagemDoisNovo="", urlImagemTresNovo="", urlImagemQuatroNovo="", urlImagemCincoNovo="";
    int numeroCasa, qtdVagas=0;
    double precoAluguel;
    String idAnuncio="", idUsuario="";
    boolean estadoAtualizado;

    // EditarMeuCadastroActivity extras:
    String nome="", telefone="", senhaAtual="", novaSenha="";

    // ProcurarActivity e Proximidades extras:
    String termoPesquisado = "", estado = "", ordenacao=""; //, cidade = "", bairro = "";
    double precoMinimo=0, precoMaximo=0, raioDistancia=0;
    boolean procuraRealizada=false, onBackPressedFlag=false, resultadoFiltragem=false;
    double[] latitudes, longitudes, precos;
    double latitudeUsuario, longitudeUsuario;

    // FiltrarActivity extras:
    String precoMinimoStr="", precoMaximoStr="", raioDistanciaStr="", qtdVagasStr="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        btnTodosAnuncios = (ImageButton)findViewById(R.id.btnTodosAnunciosMenu);
        btnAnunciarMenu = (ImageButton)findViewById(R.id.btnAnunciarMenu);
        btnMeuCadastro = (ImageButton)findViewById(R.id.btnMeuCadastro);
        btnMeuAnuncio = (ImageButton)findViewById(R.id.btnMeuAnuncio);
        btnProcurarMenu = (ImageButton)findViewById(R.id.btnProcurarMenu);
        btnFavoritos = (ImageButton)findViewById(R.id.btnFavoritos);
        btnSair = (Button)findViewById(R.id.btnSair);
        btnVoltarMenu = (ImageButton)findViewById(R.id.btnVoltarMenu);

        viewMenu = findViewById(android.R.id.content).getRootView();
        layoutMenu = findViewById(R.id.layoutMenu);

        //Transition.enterTransition(this);

        carregarExtras();

        habilitarSwipeBack();

        sessao = Sessao.getInstance();
        banco = Banco.getInstance();

        if(sessao.getUsuario()==null){
            Sessao.logout();
            startActivity(new Intent(MenuActivity.this, MainActivity.class));
            Transition.enterTransition(this);
        }
        else{

            btnVoltarMenu.setOnClickListener(view -> voltar());

            btnTodosAnuncios.setOnClickListener(view -> mostrarTodosAnuncios());

            btnAnunciarMenu.setOnClickListener(view -> anunciar());

            btnProcurarMenu.setOnClickListener(view -> procurar());

            btnMeuCadastro.setOnClickListener(view -> exibirCadastro());

            btnMeuAnuncio.setOnClickListener(view -> exibirMeusAnuncios());

            btnSair.setOnClickListener(v -> sair());

            btnFavoritos.setOnClickListener(view -> exibirFavoritos());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //removerFotos();
    }

    @Override
    public void onBackPressed() {

        if(callerIntent.equals("MainActivity")){

            Intent i = new Intent(MenuActivity.this, MainActivity.class);

            i.putExtra("callerIntent", "MenuActivity");

            startActivity(i);
            Transition.backTransition(MenuActivity.this);
            finish();
        }

        if(callerIntent.equals("AnunciarDadosImovelActivity")){

            Intent i = new Intent(MenuActivity.this, AnunciarDadosImovelActivity.class);

            i.putExtra("callerIntent", "MenuActivity");
            i.putExtra("Endereco", endereco);
            i.putExtra("Numero", numero);
            i.putExtra("Complemento", complemento);
            i.putExtra("CEP", cep);
            i.putExtra("Bairro", bairro);
            i.putExtra("Cidade", cidade);
            i.putExtra("Estado", estadoSelecionado);
            i.putExtra("Preco", preco);
            i.putExtra("Vagas", vagas);
            i.putExtra("Preco", preco);
            i.putExtra("Informacoes", informacoes);

            urlImagemPrincipal = extras.getString("urlImagemPrincipal");
            urlImagemDois = extras.getString("urlImagemDois");
            urlImagemTres = extras.getString("urlImagemTres");
            urlImagemQuatro = extras.getString("urlImagemQuatro");
            urlImagemCinco = extras.getString("urlImagemCinco");

            startActivity(i);
            Transition.backTransition(MenuActivity.this);
            finish();
        }

        if(callerIntent.equals("AnunciarFotosImovelActivity")){

            Intent i = new Intent(MenuActivity.this, AnunciarFotosImovelActivity.class);

            i.putExtra("callerIntent", "MenuActivity");

            i.putExtra("Endereco", endereco);
            i.putExtra("Numero", numeroCasa);
            i.putExtra("Complemento", complemento);
            i.putExtra("CEP", cep);
            i.putExtra("Bairro", bairro);
            i.putExtra("Cidade", cidade);
            i.putExtra("Estado", estadoSelecionado);
            i.putExtra("Vagas", qtdVagas);
            i.putExtra("Preco", precoAluguel);
            i.putExtra("Informacoes", informacoes);
            i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
            i.putExtra("urlImagemDois", urlImagemDois);
            i.putExtra("urlImagemTres", urlImagemTres);
            i.putExtra("urlImagemQuatro", urlImagemQuatro);
            i.putExtra("urlImagemCinco", urlImagemCinco);

            startActivity(i);
            Transition.backTransition(MenuActivity.this);
            finish();
        }

        if(callerIntent.equals("ProcurarActivity")){

            Intent i = new Intent(MenuActivity.this, ProcurarActivity.class);

            i.putExtra("callerIntent", "MenuActivity");

            i.putExtra("onBackPressedFlag", onBackPressedFlag);
            i.putExtra("FlagEditarFiltro", resultadoFiltragem);
            i.putExtra("ProcuraRealizada", procuraRealizada);

            i.putExtra("TermoPesquisado", termoPesquisado);
            i.putExtra("PrecoMinimo", precoMinimo);
            i.putExtra("PrecoMaximo", precoMaximo);
            i.putExtra("Bairro", bairro);
            i.putExtra("Cidade", cidade);
            i.putExtra("Estado", estado);
            i.putExtra("Vagas", qtdVagas);
            i.putExtra("RaioDistancia", raioDistancia);
            i.putExtra("Ordenacao", ordenacao);

            startActivity(i);
            Transition.backTransition(MenuActivity.this);
            finish();
        }

        if(callerIntent.equals("FiltrarActivity")){

            Intent i = new Intent(MenuActivity.this, FiltrarActivity.class);

            i.putExtra("callerIntent", "MenuActivity");

            i.putExtra("ProcuraRealizada", procuraRealizada);
            i.putExtra("FlagEditarFiltro", resultadoFiltragem);

            i.putExtra("TermoPesquisado", termoPesquisado);
            i.putExtra("PrecoMinimo", precoMinimoStr);
            i.putExtra("PrecoMaximo", precoMaximoStr);
            i.putExtra("Bairro", bairro);
            i.putExtra("Cidade", cidade);
            i.putExtra("Estado", estado);
            i.putExtra("Vagas", qtdVagasStr);
            i.putExtra("Distancia", raioDistanciaStr);
            i.putExtra("Ordenacao", ordenacao);

            startActivity(i);
            Transition.backTransition(MenuActivity.this);
            finish();
        }

        if(callerIntent.equals("ProximidadesActivity")){

            Intent i = new Intent(MenuActivity.this, ProximidadesActivity.class);

            i.putExtra("callerIntent", "MenuActivity");

            i.putExtra("onBackPressedFlag", onBackPressedFlag);
            i.putExtra("FlagEditarFiltro", resultadoFiltragem);

            i.putExtra("Latitudes", latitudes);
            i.putExtra("Longitudes", longitudes);
            i.putExtra("Precos", precos);

            i.putExtra("LatitudeUsuario", latitudeUsuario);
            i.putExtra("LongitudeUsuario", longitudeUsuario);

            i.putExtra("TermoPesquisado", termoPesquisado);
            i.putExtra("PrecoMinimo", precoMinimo);
            i.putExtra("PrecoMaximo", precoMaximo);
            i.putExtra("Bairro", bairro);
            i.putExtra("Cidade", cidade);
            i.putExtra("Estado", estado);
            i.putExtra("Vagas", qtdVagas);
            i.putExtra("RaioDistancia", raioDistancia);
            i.putExtra("Ordenacao", ordenacao);

            startActivity(i);
            Transition.backTransition(MenuActivity.this);
            finish();
        }

        if(callerIntent.equals("FavoritosActivity")){

            Intent i = new Intent(MenuActivity.this, FavoritosActivity.class);

            i.putExtra("callerIntent", "MenuActivity");

            startActivity(i);
            Transition.backTransition(MenuActivity.this);
            finish();
        }

        if(callerIntent.equals("MeuCadastroActivity")){

            Intent i = new Intent(MenuActivity.this, MeuCadastroActivity.class);

            i.putExtra("callerIntent", "MenuActivity");

            startActivity(i);
            Transition.backTransition(MenuActivity.this);
            finish();
        }

        if(callerIntent.equals("EditarCadastroActivity")){

            Intent i = new Intent(MenuActivity.this, EditarCadastroActivity.class);

            i.putExtra("callerIntent", "MenuActivity");

            i.putExtra("idUsuario", idUsuario);

            i.putExtra("Nome", nome);
            i.putExtra("Telefone", telefone);
            i.putExtra("SenhaAtual", senhaAtual);
            i.putExtra("NovaSenha", novaSenha);

            startActivity(i);
            Transition.backTransition(MenuActivity.this);
            finish();
        }

        if(callerIntent.equals("MeusAnunciosActivity")){

            Intent i = new Intent(MenuActivity.this, MeusAnunciosActivity.class);

            i.putExtra("callerIntent", "MenuActivity");

            startActivity(i);
            Transition.backTransition(MenuActivity.this);
            finish();
        }

        if(callerIntent.equals("MeuAnuncioDados")){

            Intent i = new Intent(MenuActivity.this, MeuAnuncioDados.class);

            i.putExtra("callerIntent", "MenuActivity");

            i.putExtra("idAnuncio", idAnuncio);

            i.putExtra("Endereco", endereco);
            i.putExtra("Numero", numeroCasa);
            i.putExtra("Complemento", complemento);
            i.putExtra("CEP", cep);
            i.putExtra("Bairro", bairro);
            i.putExtra("Cidade", cidade);
            i.putExtra("Estado", estadoSelecionado);
            i.putExtra("Vagas", qtdVagas);
            i.putExtra("Preco", precoAluguel);
            i.putExtra("Informacoes", informacoes);

            startActivity(i);
            Transition.backTransition(MenuActivity.this);
            finish();
        }

        if(callerIntent.equals("MeuAnuncioFotos")){

            Intent i = new Intent(MenuActivity.this, MeuAnuncioFotos.class);

            i.putExtra("callerIntent", "MenuActivity");

            i.putExtra("idAnuncio", idAnuncio);

            i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
            i.putExtra("urlImagemDois", urlImagemDois);
            i.putExtra("urlImagemTres", urlImagemTres);
            i.putExtra("urlImagemQuatro", urlImagemQuatro);
            i.putExtra("urlImagemCinco", urlImagemCinco);

            startActivity(i);
            Transition.backTransition(MenuActivity.this);
            finish();
        }

        if(callerIntent.equals("EditarMeuAnuncioDados")){

            Intent i = new Intent(MenuActivity.this, EditarMeuAnuncioDados.class);

            i.putExtra("callerIntent", "MenuActivity");

            i.putExtra("idAnuncio", idAnuncio);

            i.putExtra("EnderecoAtual", endereco);
            i.putExtra("NumeroAtual", Integer.parseInt(numero));
            i.putExtra("ComplementoAtual", complemento);
            i.putExtra("CepAtual", cep);
            i.putExtra("BairroAtual", bairro);
            i.putExtra("CidadeAtual", cidade);
            i.putExtra("EstadoAtual", estadoAtual);
            i.putExtra("EstadoSelecionado", estadoSelecionado);
            i.putExtra("VagasAtual", Integer.parseInt(vagas));
            i.putExtra("PrecoAtual", Double.parseDouble(preco));
            i.putExtra("InformacoesAtual", informacoes);

            i.putExtra("EnderecoNovo", enderecoNovo);
            i.putExtra("NumeroNovo", Integer.parseInt(numeroNovo));
            i.putExtra("ComplementoNovo", complementoNovo);
            i.putExtra("CepNovo", cepNovo);
            i.putExtra("BairroNovo", bairroNovo);
            i.putExtra("CidadeNovo", cidadeNovo);
            i.putExtra("EstadoAtual", estadoAtual);
            i.putExtra("EstadoSelecionado", estadoSelecionado);
            i.putExtra("VagasNovo", Integer.parseInt(vagasNovo));
            i.putExtra("PrecoNovo", Double.parseDouble(precoNovo));
            i.putExtra("InformacoesNovo", informacoesNovo);
            i.putExtra("EstadoAtualizado", estadoAtualizado);

            startActivity(i);
            Transition.backTransition(MenuActivity.this);
            finish();
        }

        if(callerIntent.equals("EditarMeuAnuncioFotos")){

            Intent i = new Intent(MenuActivity.this, EditarMeuAnuncioFotos.class);

            i.putExtra("callerIntent", "MenuActivity");

            i.putExtra("idAnuncio", idAnuncio);

            i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
            i.putExtra("urlImagemDois", urlImagemDois);
            i.putExtra("urlImagemTres", urlImagemTres);
            i.putExtra("urlImagemQuatro", urlImagemQuatro);
            i.putExtra("urlImagemCinco", urlImagemCinco);

            i.putExtra("urlImagemPrincipalNovo", urlImagemPrincipalNovo);
            i.putExtra("urlImagemDoisNovo", urlImagemDoisNovo);
            i.putExtra("urlImagemTresNovo", urlImagemTresNovo);
            i.putExtra("urlImagemQuatroNovo", urlImagemQuatroNovo);
            i.putExtra("urlImagemCincoNovo", urlImagemCincoNovo);

            startActivity(i);
            Transition.backTransition(MenuActivity.this);
            finish();
        }
    }

    public void habilitarSwipeBack(){

        viewMenu.setOnTouchListener(new OnSwipeTouchListener(MenuActivity.this) {
            public void onSwipeLeft() {

                if(callerIntent.equals("MainActivity")){

                    Intent i = new Intent(MenuActivity.this, MainActivity.class);

                    i.putExtra("callerIntent", "MenuActivity");

                    startActivity(i);
                    Transition.backTransition(MenuActivity.this);
                    finish();
                }

                if(callerIntent.equals("AnunciarDadosImovelActivity")){

                    Intent i = new Intent(MenuActivity.this, AnunciarDadosImovelActivity.class);

                    i.putExtra("callerIntent", "MenuActivity");
                    i.putExtra("Endereco", endereco);
                    i.putExtra("Numero", numero);
                    i.putExtra("Complemento", complemento);
                    i.putExtra("CEP", cep);
                    i.putExtra("Bairro", bairro);
                    i.putExtra("Cidade", cidade);
                    i.putExtra("Estado", estadoSelecionado);
                    i.putExtra("Preco", preco);
                    i.putExtra("Vagas", vagas);
                    i.putExtra("Preco", preco);
                    i.putExtra("Informacoes", informacoes);

                    i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
                    i.putExtra("urlImagemDois", urlImagemDois);
                    i.putExtra("urlImagemTres", urlImagemTres);
                    i.putExtra("urlImagemQuatro", urlImagemQuatro);
                    i.putExtra("urlImagemCinco", urlImagemCinco);

                    startActivity(i);
                    Transition.backTransition(MenuActivity.this);
                    finish();
                }

                if(callerIntent.equals("AnunciarFotosImovelActivity")){

                    Intent i = new Intent(MenuActivity.this, AnunciarFotosImovelActivity.class);

                    i.putExtra("callerIntent", "MenuActivity");

                    i.putExtra("Endereco", endereco);
                    i.putExtra("Numero", numeroCasa);
                    i.putExtra("Complemento", complemento);
                    i.putExtra("CEP", cep);
                    i.putExtra("Bairro", bairro);
                    i.putExtra("Cidade", cidade);
                    i.putExtra("Estado", estadoSelecionado);
                    i.putExtra("Vagas", qtdVagas);
                    i.putExtra("Preco", precoAluguel);
                    i.putExtra("Informacoes", informacoes);
                    i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
                    i.putExtra("urlImagemDois", urlImagemDois);
                    i.putExtra("urlImagemTres", urlImagemTres);
                    i.putExtra("urlImagemQuatro", urlImagemQuatro);
                    i.putExtra("urlImagemCinco", urlImagemCinco);

                    startActivity(i);
                    Transition.backTransition(MenuActivity.this);
                    finish();
                }

                if(callerIntent.equals("InformacoesFragment")){
                    onBackPressed();
                    finish();
                }

                if(callerIntent.equals("ContatoFragment")){
                    onBackPressed();
                    finish();
                }

                if(callerIntent.equals("FotosFragment")){
                    onBackPressed();
                    finish();
                }

                if(callerIntent.equals("MeuCadastroActivity")){

                    Intent i = new Intent(MenuActivity.this, MeuCadastroActivity.class);

                    i.putExtra("callerIntent", "MenuActivity");

                    startActivity(i);
                    Transition.backTransition(MenuActivity.this);
                    finish();
                }

                if(callerIntent.equals("EditarCadastroActivity")){

                    Intent i = new Intent(MenuActivity.this, EditarCadastroActivity.class);

                    i.putExtra("callerIntent", "MenuActivity");

                    i.putExtra("idUsuario", idUsuario);

                    i.putExtra("Nome", nome);
                    i.putExtra("Telefone", telefone);
                    i.putExtra("SenhaAtual", senhaAtual);
                    i.putExtra("NovaSenha", novaSenha);

                    startActivity(i);
                    Transition.backTransition(MenuActivity.this);
                    finish();
                }

                if(callerIntent.equals("MeusAnunciosActivity")){

                    Intent i = new Intent(MenuActivity.this, MeusAnunciosActivity.class);

                    i.putExtra("callerIntent", "MenuActivity");

                    startActivity(i);
                    Transition.backTransition(MenuActivity.this);
                    finish();
                }

                if(callerIntent.equals("MeuAnuncioFotos")){

                    Intent i = new Intent(MenuActivity.this, MeuAnuncioFotos.class);

                    i.putExtra("callerIntent", "MenuActivity");

                    i.putExtra("idAnuncio", idAnuncio);

                    i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
                    i.putExtra("urlImagemDois", urlImagemDois);
                    i.putExtra("urlImagemTres", urlImagemTres);
                    i.putExtra("urlImagemQuatro", urlImagemQuatro);
                    i.putExtra("urlImagemCinco", urlImagemCinco);

                    startActivity(i);
                    Transition.backTransition(MenuActivity.this);
                    finish();
                }

                if(callerIntent.equals("EditarMeuAnuncioFotos")){

                    Intent i = new Intent(MenuActivity.this, EditarMeuAnuncioFotos.class);

                    i.putExtra("callerIntent", "MenuActivity");

                    i.putExtra("idAnuncio", idAnuncio);

                    i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
                    i.putExtra("urlImagemDois", urlImagemDois);
                    i.putExtra("urlImagemTres", urlImagemTres);
                    i.putExtra("urlImagemQuatro", urlImagemQuatro);
                    i.putExtra("urlImagemCinco", urlImagemCinco);

                    i.putExtra("urlImagemPrincipalNovo", urlImagemPrincipalNovo);
                    i.putExtra("urlImagemDoisNovo", urlImagemDoisNovo);
                    i.putExtra("urlImagemTresNovo", urlImagemTresNovo);
                    i.putExtra("urlImagemQuatroNovo", urlImagemQuatroNovo);
                    i.putExtra("urlImagemCincoNovo", urlImagemCincoNovo);

                    startActivity(i);
                    Transition.backTransition(MenuActivity.this);
                    finish();
                }

                if(callerIntent.equals("MeuAnuncioDados")){

                    Intent i = new Intent(MenuActivity.this, MeuAnuncioDados.class);

                    i.putExtra("callerIntent", "MenuActivity");

                    i.putExtra("idAnuncio", idAnuncio);

                    i.putExtra("Endereco", endereco);
                    i.putExtra("Numero", numeroCasa);
                    i.putExtra("Complemento", complemento);
                    i.putExtra("CEP", cep);
                    i.putExtra("Bairro", bairro);
                    i.putExtra("Cidade", cidade);
                    i.putExtra("Estado", estadoSelecionado);
                    i.putExtra("Vagas", qtdVagas);
                    i.putExtra("Preco", precoAluguel);
                    i.putExtra("Informacoes", informacoes);

                    startActivity(i);
                    Transition.backTransition(MenuActivity.this);
                    finish();
                }

                if(callerIntent.equals("EditarMeuAnuncioDados")){

                    Intent i = new Intent(MenuActivity.this, EditarMeuAnuncioDados.class);

                    i.putExtra("callerIntent", "MenuActivity");

                    i.putExtra("idAnuncio", idAnuncio);

                    i.putExtra("EnderecoAtual", endereco);
                    i.putExtra("NumeroAtual", Integer.parseInt(numero));
                    i.putExtra("ComplementoAtual", complemento);
                    i.putExtra("CepAtual", cep);
                    i.putExtra("BairroAtual", bairro);
                    i.putExtra("CidadeAtual", cidade);
                    i.putExtra("EstadoAtual", estadoAtual);
                    i.putExtra("EstadoSelecionado", estadoSelecionado);
                    i.putExtra("VagasAtual", Integer.parseInt(vagas));
                    i.putExtra("PrecoAtual", Double.parseDouble(preco));
                    i.putExtra("InformacoesAtual", informacoes);

                    i.putExtra("EnderecoNovo", enderecoNovo);
                    i.putExtra("NumeroNovo", Integer.parseInt(numeroNovo));
                    i.putExtra("ComplementoNovo", complementoNovo);
                    i.putExtra("CepNovo", cepNovo);
                    i.putExtra("BairroNovo", bairroNovo);
                    i.putExtra("CidadeNovo", cidadeNovo);
                    i.putExtra("EstadoAtual", estadoAtual);
                    i.putExtra("EstadoSelecionado", estadoSelecionado);
                    i.putExtra("VagasNovo", Integer.parseInt(vagasNovo));
                    i.putExtra("PrecoNovo", Double.parseDouble(precoNovo));
                    i.putExtra("InformacoesNovo", informacoesNovo);
                    i.putExtra("EstadoAtualizado", estadoAtualizado);

                    startActivity(i);
                    Transition.backTransition(MenuActivity.this);
                    finish();
                }

                if(callerIntent.equals("FavoritosActivity")){

                    Intent i = new Intent(MenuActivity.this, FavoritosActivity.class);

                    i.putExtra("callerIntent", "MenuActivity");

                    startActivity(i);
                    Transition.backTransition(MenuActivity.this);
                    finish();
                }

                if(callerIntent.equals("ProcurarActivity")){

                    Intent i = new Intent(MenuActivity.this, ProcurarActivity.class);

                    i.putExtra("callerIntent", "MenuActivity");

                    i.putExtra("onBackPressedFlag", onBackPressedFlag);
                    i.putExtra("FlagEditarFiltro", resultadoFiltragem);
                    i.putExtra("ProcuraRealizada", procuraRealizada);

                    i.putExtra("TermoPesquisado", termoPesquisado);
                    i.putExtra("PrecoMinimo", precoMinimo);
                    i.putExtra("PrecoMaximo", precoMaximo);
                    i.putExtra("Bairro", bairro);
                    i.putExtra("Cidade", cidade);
                    i.putExtra("Estado", estado);
                    i.putExtra("Vagas", qtdVagas);
                    i.putExtra("RaioDistancia", raioDistancia);
                    i.putExtra("Ordenacao", ordenacao);

                    startActivity(i);
                    Transition.backTransition(MenuActivity.this);
                    finish();
                }

                if(callerIntent.equals("FiltrarActivity")){

                    Intent i = new Intent(MenuActivity.this, FiltrarActivity.class);

                    i.putExtra("callerIntent", "MenuActivity");

                    i.putExtra("ProcuraRealizada", procuraRealizada);
                    i.putExtra("FlagEditarFiltro", resultadoFiltragem);

                    i.putExtra("TermoPesquisado", termoPesquisado);
                    i.putExtra("PrecoMinimo", precoMinimoStr);
                    i.putExtra("PrecoMaximo", precoMaximoStr);
                    i.putExtra("Bairro", bairro);
                    i.putExtra("Cidade", cidade);
                    i.putExtra("Estado", estado);
                    i.putExtra("Vagas", qtdVagasStr);
                    i.putExtra("Distancia", raioDistanciaStr);
                    i.putExtra("Ordenacao", ordenacao);

                    startActivity(i);
                    Transition.backTransition(MenuActivity.this);
                    finish();
                }

                if(callerIntent.equals("ProximidadesActivity")){

                    Intent i = new Intent(MenuActivity.this, ProximidadesActivity.class);

                    i.putExtra("callerIntent", "MenuActivity");

                    i.putExtra("onBackPressedFlag", onBackPressedFlag);

                    i.putExtra("Latitudes", latitudes);
                    i.putExtra("Longitudes", longitudes);
                    i.putExtra("Precos", precos);

                    i.putExtra("LatitudeUsuario", latitudeUsuario);
                    i.putExtra("LongitudeUsuario", longitudeUsuario);

                    i.putExtra("TermoPesquisado", termoPesquisado);
                    i.putExtra("PrecoMinimo", precoMinimo);
                    i.putExtra("PrecoMaximo", precoMaximo);
                    i.putExtra("Bairro", bairro);
                    i.putExtra("Cidade", cidade);
                    i.putExtra("Estado", estado);
                    i.putExtra("Vagas", qtdVagas);
                    i.putExtra("RaioDistancia", raioDistancia);
                    i.putExtra("Ordenacao", ordenacao);

                    i.putExtra("FlagEditarFiltro", resultadoFiltragem);

                    startActivity(i);
                    Transition.backTransition(MenuActivity.this);
                    finish();
                }
            }
        });
    }

    public void voltar(){

        if(sessao.getUsuario()==null){

            Sessao.logout();
            startActivity(new Intent(MenuActivity.this, MainActivity.class));
            Transition.backTransition(this);
            finish();
        }
        else{

            if(callerIntent.equals("MainActivity")){

                Intent i = new Intent(MenuActivity.this, MainActivity.class);

                i.putExtra("callerIntent", "MenuActivity");

                startActivity(i);
                Transition.backTransition(MenuActivity.this);
                finish();
            }
            else
            if(callerIntent.equals("AnunciarDadosImovelActivity")){

                Intent i = new Intent(MenuActivity.this, AnunciarDadosImovelActivity.class);

                i.putExtra("callerIntent", "MenuActivity");

                i.putExtra("Endereco", endereco);
                i.putExtra("Numero", numero);
                i.putExtra("Complemento", complemento);
                i.putExtra("CEP", cep);
                i.putExtra("Bairro", bairro);
                i.putExtra("Cidade", cidade);
                i.putExtra("Estado", estadoSelecionado);
                i.putExtra("Preco", preco);
                i.putExtra("Vagas", vagas);
                i.putExtra("Informacoes", informacoes);

                i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
                i.putExtra("urlImagemDois", urlImagemDois);
                i.putExtra("urlImagemTres", urlImagemTres);
                i.putExtra("urlImagemQuatro", urlImagemQuatro);
                i.putExtra("urlImagemCinco", urlImagemCinco);

                startActivity(i);
                Transition.backTransition(MenuActivity.this);
                finish();
            }
            else
            if(callerIntent.equals("AnunciarFotosImovelActivity")){

                Intent i = new Intent(MenuActivity.this, AnunciarFotosImovelActivity.class);

                i.putExtra("callerIntent", "MenuActivity");

                i.putExtra("Endereco", endereco);
                i.putExtra("Numero", numeroCasa);
                i.putExtra("Complemento", complemento);
                i.putExtra("CEP", cep);
                i.putExtra("Bairro", bairro);
                i.putExtra("Cidade", cidade);
                i.putExtra("Estado", estadoSelecionado);
                i.putExtra("Vagas", qtdVagas);
                i.putExtra("Preco", precoAluguel);
                i.putExtra("Informacoes", informacoes);
                i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
                i.putExtra("urlImagemDois", urlImagemDois);
                i.putExtra("urlImagemTres", urlImagemTres);
                i.putExtra("urlImagemQuatro", urlImagemQuatro);
                i.putExtra("urlImagemCinco", urlImagemCinco);

                startActivity(i);
                Transition.backTransition(MenuActivity.this);
                finish();
            }
            else
            if (callerIntent.equals("AnuncioActivity")){
                onBackPressed();
                Transition.backTransition(MenuActivity.this);
                finish();
            }
            else
            if(callerIntent.equals("MeuCadastroActivity")){

                Intent i = new Intent(MenuActivity.this, MeuCadastroActivity.class);

                i.putExtra("callerIntent", "MenuActivity");

                startActivity(i);
                Transition.backTransition(MenuActivity.this);
                finish();
            }
            else
            if(callerIntent.equals("EditarCadastroActivity")){

                Intent i = new Intent(MenuActivity.this, EditarCadastroActivity.class);

                i.putExtra("callerIntent", "MenuActivity");

                i.putExtra("idUsuario", idUsuario);

                i.putExtra("Nome", nome);
                i.putExtra("Telefone", telefone);
                i.putExtra("SenhaAtual", senhaAtual);
                i.putExtra("NovaSenha", novaSenha);

                startActivity(i);
                Transition.backTransition(MenuActivity.this);
                finish();
            }
            else
            if(callerIntent.equals("MeusAnunciosActivity")){

                Intent i = new Intent(MenuActivity.this, MeusAnunciosActivity.class);

                i.putExtra("callerIntent", "MenuActivity");

                startActivity(i);
                Transition.backTransition(MenuActivity.this);
                finish();
            }
            else
            if(callerIntent.equals("MeuAnuncioFotos")){

                Intent i = new Intent(MenuActivity.this, MeuAnuncioFotos.class);

                i.putExtra("callerIntent", "MenuActivity");

                i.putExtra("idAnuncio", idAnuncio);

                i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
                i.putExtra("urlImagemDois", urlImagemDois);
                i.putExtra("urlImagemTres", urlImagemTres);
                i.putExtra("urlImagemQuatro", urlImagemQuatro);
                i.putExtra("urlImagemCinco", urlImagemCinco);

                startActivity(i);
                Transition.backTransition(MenuActivity.this);
                finish();
            }
            else
            if(callerIntent.equals("EditarMeuAnuncioFotos")){

                Intent i = new Intent(MenuActivity.this, EditarMeuAnuncioFotos.class);

                i.putExtra("callerIntent", "MenuActivity");

                i.putExtra("idAnuncio", idAnuncio);

                i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
                i.putExtra("urlImagemDois", urlImagemDois);
                i.putExtra("urlImagemTres", urlImagemTres);
                i.putExtra("urlImagemQuatro", urlImagemQuatro);
                i.putExtra("urlImagemCinco", urlImagemCinco);

                i.putExtra("urlImagemPrincipalNovo", urlImagemPrincipalNovo);
                i.putExtra("urlImagemDoisNovo", urlImagemDoisNovo);
                i.putExtra("urlImagemTresNovo", urlImagemTresNovo);
                i.putExtra("urlImagemQuatroNovo", urlImagemQuatroNovo);
                i.putExtra("urlImagemCincoNovo", urlImagemCincoNovo);

                startActivity(i);
                Transition.backTransition(MenuActivity.this);
                finish();
            }
            else
            if(callerIntent.equals("MeuAnuncioDados")){

                Intent i = new Intent(MenuActivity.this, MeuAnuncioDados.class);

                i.putExtra("callerIntent", "MenuActivity");

                i.putExtra("idAnuncio", idAnuncio);

                i.putExtra("Endereco", endereco);
                i.putExtra("Numero", numeroCasa);
                i.putExtra("Complemento", complemento);
                i.putExtra("CEP", cep);
                i.putExtra("Bairro", bairro);
                i.putExtra("Cidade", cidade);
                i.putExtra("Estado", estadoSelecionado);
                i.putExtra("Vagas", qtdVagas);
                i.putExtra("Preco", precoAluguel);
                i.putExtra("Informacoes", informacoes);

                startActivity(i);
                Transition.backTransition(MenuActivity.this);
                finish();
            }
            else
            if(callerIntent.equals("EditarMeuAnuncioDados")){

                Intent i = new Intent(MenuActivity.this, EditarMeuAnuncioDados.class);

                i.putExtra("callerIntent", "MenuActivity");

                i.putExtra("idAnuncio", idAnuncio);

                i.putExtra("EnderecoAtual", endereco);
                i.putExtra("NumeroAtual", Integer.parseInt(numero));
                i.putExtra("ComplementoAtual", complemento);
                i.putExtra("CepAtual", cep);
                i.putExtra("BairroAtual", bairro);
                i.putExtra("CidadeAtual", cidade);
                i.putExtra("EstadoAtual", estadoAtual);
                i.putExtra("EstadoSelecionado", estadoSelecionado);
                i.putExtra("VagasAtual", Integer.parseInt(vagas));
                i.putExtra("PrecoAtual", Double.parseDouble(preco));
                i.putExtra("InformacoesAtual", informacoes);

                i.putExtra("EnderecoNovo", enderecoNovo);
                i.putExtra("NumeroNovo", Integer.parseInt(numeroNovo));
                i.putExtra("ComplementoNovo", complementoNovo);
                i.putExtra("CepNovo", cepNovo);
                i.putExtra("BairroNovo", bairroNovo);
                i.putExtra("CidadeNovo", cidadeNovo);
                i.putExtra("EstadoAtual", estadoAtual);
                i.putExtra("EstadoSelecionado", estadoSelecionado);
                i.putExtra("VagasNovo", Integer.parseInt(vagasNovo));
                i.putExtra("PrecoNovo", Double.parseDouble(precoNovo));
                i.putExtra("InformacoesNovo", informacoesNovo);
                i.putExtra("EstadoAtualizado", estadoAtualizado);

                startActivity(i);
                Transition.backTransition(MenuActivity.this);
                finish();
            }
            else
            if(callerIntent.equals("FavoritosActivity")){

                Intent i = new Intent(MenuActivity.this, FavoritosActivity.class);

                i.putExtra("callerIntent", "MenuActivity");

                startActivity(i);
                Transition.backTransition(MenuActivity.this);
                finish();
            }
            else
            if(callerIntent.equals("ProcurarActivity")){

                Intent i = new Intent(MenuActivity.this, ProcurarActivity.class);

                i.putExtra("callerIntent", "MenuActivity");

                i.putExtra("onBackPressedFlag", onBackPressedFlag);
                i.putExtra("FlagEditarFiltro", resultadoFiltragem);
                i.putExtra("ProcuraRealizada", procuraRealizada);

                i.putExtra("TermoPesquisado", termoPesquisado);
                i.putExtra("PrecoMinimo", precoMinimo);
                i.putExtra("PrecoMaximo", precoMaximo);
                i.putExtra("Bairro", bairro);
                i.putExtra("Cidade", cidade);
                i.putExtra("Estado", estado);
                i.putExtra("Vagas", qtdVagas);
                i.putExtra("RaioDistancia", raioDistancia);
                i.putExtra("Ordenacao", ordenacao);

                startActivity(i);
                Transition.backTransition(MenuActivity.this);
                finish();
            }
            else
            if(callerIntent.equals("FiltrarActivity")){

                Intent i = new Intent(MenuActivity.this, FiltrarActivity.class);

                i.putExtra("callerIntent", "MenuActivity");

                i.putExtra("ProcuraRealizada", procuraRealizada);
                i.putExtra("FlagEditarFiltro", resultadoFiltragem);

                i.putExtra("TermoPesquisado", termoPesquisado);
                i.putExtra("PrecoMinimo", precoMinimoStr);
                i.putExtra("PrecoMaximo", precoMaximoStr);
                i.putExtra("Bairro", bairro);
                i.putExtra("Cidade", cidade);
                i.putExtra("Estado", estado);
                i.putExtra("Vagas", qtdVagasStr);
                i.putExtra("Distancia", raioDistanciaStr);
                i.putExtra("Ordenacao", ordenacao);

                startActivity(i);
                Transition.backTransition(MenuActivity.this);
                finish();
            }
            else
            if(callerIntent.equals("ProximidadesActivity")){

                Intent i = new Intent(MenuActivity.this, ProximidadesActivity.class);

                i.putExtra("callerIntent", "MenuActivity");

                i.putExtra("onBackPressedFlag", onBackPressedFlag);

                i.putExtra("Latitudes", latitudes);
                i.putExtra("Longitudes", longitudes);
                i.putExtra("Precos", precos);

                i.putExtra("LatitudeUsuario", latitudeUsuario);
                i.putExtra("LongitudeUsuario", longitudeUsuario);

                i.putExtra("TermoPesquisado", termoPesquisado);
                i.putExtra("PrecoMinimo", precoMinimo);
                i.putExtra("PrecoMaximo", precoMaximo);
                i.putExtra("Bairro", bairro);
                i.putExtra("Cidade", cidade);
                i.putExtra("Estado", estado);
                i.putExtra("Vagas", qtdVagas);
                i.putExtra("RaioDistancia", raioDistancia);
                i.putExtra("Ordenacao", ordenacao);

                i.putExtra("FlagEditarFiltro", resultadoFiltragem);

                startActivity(i);
                Transition.backTransition(MenuActivity.this);
                finish();
            }
        }
    }

    public void mostrarTodosAnuncios(){

        Intent i = new Intent(MenuActivity.this, MainActivity.class);

        i.putExtra("callerIntent", "MenuActivity");

        removerFotos();

        startActivity(i);
        Transition.enterTransition(MenuActivity.this);
        finish();
    }

    public void anunciar(){

        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                // Se o usuário não tiver cadastrado um telefone, exibir mensagem:
                banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot usuario : dataSnapshot.getChildren()){
                            if(sessao.getIdUsuario().equals(usuario.getValue(Usuario.class).getuId())){
                                if(usuario.getValue(Usuario.class).getTelefone().equals("")) {
                                    CustomToast.mostrarMensagem(getApplicationContext(), "Cadastre um telefone para contato", Toast.LENGTH_LONG);
                                }

                                Intent i = new Intent(MenuActivity.this, AnunciarDadosImovelActivity.class);

                                removerFotos();

                                i.putExtra("callerIntent", "MenuActivity");

                                i.putExtra("Endereco", "");
                                i.putExtra("Numero", "");
                                i.putExtra("Complemento", "");
                                i.putExtra("CEP", "");
                                i.putExtra("Bairro", "");
                                i.putExtra("Cidade", "");
                                i.putExtra("Estado", "");
                                i.putExtra("Preco", "");
                                i.putExtra("Vagas", "");
                                i.putExtra("Preco", "");
                                i.putExtra("Informacoes", "");

                                startActivity(i);
                                Transition.enterTransition(MenuActivity.this);
                                finish();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    public void procurar(){
        if(sessao.getUsuario()==null){
            Sessao.logout();
            startActivity(new Intent(MenuActivity.this, MainActivity.class));
            Transition.enterTransition(this);
        }
        else{

            removerFotos();

            startActivity(new Intent(MenuActivity.this, ProcurarActivity.class));
            Transition.enterTransition(this);
            finish();
        }
    }

    public void exibirCadastro(){

        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else {

                Intent i = new Intent(MenuActivity.this, MeuCadastroActivity.class);

                removerFotos();

                i.putExtra("callerIntent", "MenuActivity");
                i.putExtra("idUsuario", sessao.getIdUsuario());

                startActivity(i);
                Transition.enterTransition(this);
                finish();
            }
        });
    }

    public void exibirMeusAnuncios(){
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{
                if(sessao.getUsuario()==null){
                    Sessao.logout();
                    startActivity(new Intent(MenuActivity.this, MainActivity.class));
                    Transition.enterTransition(this);
                }
                else{

                    removerFotos();

                    startActivity(new Intent(MenuActivity.this, MeusAnunciosActivity.class));
                    Transition.enterTransition(MenuActivity.this);
                    finish();
                }
            }
        });
    }

    public void sair(){

        if(sessao.getUsuario()==null){
            Sessao.logout();
            startActivity(new Intent(MenuActivity.this, MainActivity.class));
            Transition.enterTransition(this);
            finish();
        }
        else{
            // Exibir tela para confirmar logout:

            // get prompts.xml view
            LayoutInflater li = LayoutInflater.from(context);
            View sairConta = li.inflate(R.layout.sair_confirmacao, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    context);

            // set prompts.xml to alertdialog builder
            alertDialogBuilder.setView(sairConta);
            alertDialogBuilder.setTitle("Confirmação");

            // set dialog message
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("Sim",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {

                                    removerFotos(); // SERÁ QUE PERDE AS CREDENCIAIS???

                                    // Removendo credenciais do usuário do Shared Preferences:
                                    context.getSharedPreferences("credentials", 0).edit().clear().apply();

                                    Sessao.logout();
                                    Log.i("Sair", "Logout realizado com sucesso!");
                                    startActivity(new Intent(MenuActivity.this, MainActivity.class));
                                    Transition.enterTransition(MenuActivity.this);
                                    finish();
                                }
                            })
                    .setNegativeButton("Não",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    dialog.cancel();
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        }
    }

    public void exibirFavoritos(){

        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                Intent i = new Intent(MenuActivity.this, FavoritosActivity.class);

                removerFotos();

                i.putExtra("callerIntent", "MenuActivity");

                startActivity(i);
                Transition.enterTransition(MenuActivity.this);
                finish();
            }
        });
    }

    public void carregarExtras(){

        extras = getIntent().getExtras();

        if(extras!=null){

            callerIntent = extras.getString("callerIntent");

            if(callerIntent.equals("MainActivity")){}

            if(callerIntent.equals("AnunciarDadosImovelActivity")){

                endereco = extras.getString("Endereco");
                numero = extras.getString("Numero");
                complemento = extras.getString("Complemento");
                cep = extras.getString("CEP");
                bairro = extras.getString("Bairro");
                cidade = extras.getString("Cidade");
                estadoSelecionado = extras.getString("Estado");
                preco = extras.getString("Preco");
                vagas = extras.getString("Vagas");
                informacoes = extras.getString("Informacoes");
                urlImagemPrincipal = extras.getString("urlImagemPrincipal");
                urlImagemDois = extras.getString("urlImagemDois");
                urlImagemTres = extras.getString("urlImagemTres");
                urlImagemQuatro = extras.getString("urlImagemQuatro");
                urlImagemCinco = extras.getString("urlImagemCinco");
            }

            if(callerIntent.equals("AnunciarFotosImovelActivity")){

                endereco = extras.getString("Endereco");
                numeroCasa = extras.getInt("Numero");
                complemento = extras.getString("Complemento");
                cep = extras.getString("CEP");
                bairro = extras.getString("Bairro");
                cidade = extras.getString("Cidade");
                estadoSelecionado = extras.getString("Estado");
                precoAluguel = extras.getDouble("Preco");
                qtdVagas = extras.getInt("Vagas");
                informacoes = extras.getString("Informacoes");
                urlImagemPrincipal = extras.getString("urlImagemPrincipal");
                urlImagemDois = extras.getString("urlImagemDois");
                urlImagemTres = extras.getString("urlImagemTres");
                urlImagemQuatro = extras.getString("urlImagemQuatro");
                urlImagemCinco = extras.getString("urlImagemCinco");
            }

            if(callerIntent.equals("EditarCadastroActivity")){

                // Dados digitados pelo usuário ao chamar MenuActivity:
                idUsuario = extras.getString("idUsuario");
                nome = extras.getString("Nome");
                telefone = extras.getString("Telefone");
                senhaAtual = extras.getString("SenhaAtual");
                novaSenha = extras.getString("NovaSenha");
            }

            if(callerIntent.equals("MeuAnuncioFotos")){

                idAnuncio = extras.getString("idAnuncio");

                urlImagemPrincipal = extras.getString("urlImagemPrincipal");
                urlImagemDois = extras.getString("urlImagemDois");
                urlImagemTres = extras.getString("urlImagemTres");
                urlImagemQuatro = extras.getString("urlImagemQuatro");
                urlImagemCinco = extras.getString("urlImagemCinco");
            }

            if(callerIntent.equals("EditarMeuAnuncioFotos")){

                idAnuncio = extras.getString("idAnuncio");

                urlImagemPrincipal = extras.getString("urlImagemPrincipal");
                urlImagemDois = extras.getString("urlImagemDois");
                urlImagemTres = extras.getString("urlImagemTres");
                urlImagemQuatro = extras.getString("urlImagemQuatro");
                urlImagemCinco = extras.getString("urlImagemCinco");

                urlImagemPrincipalNovo = extras.getString("urlImagemPrincipalNovo");
                urlImagemDoisNovo = extras.getString("urlImagemDoisNovo");
                urlImagemTresNovo = extras.getString("urlImagemTresNovo");
                urlImagemQuatroNovo = extras.getString("urlImagemQuatroNovo");
                urlImagemCincoNovo = extras.getString("urlImagemCincoNovo");
            }

            if(callerIntent.equals("MeuAnuncioDados")){

                idAnuncio = extras.getString("idAnuncio");
                endereco = extras.getString("Endereco");
                numeroCasa = extras.getInt("Numero");
                complemento = extras.getString("Complemento");
                cep = extras.getString("CEP");
                bairro = extras.getString("Bairro");
                cidade = extras.getString("Cidade");
                estadoSelecionado = extras.getString("Estado");
                precoAluguel = extras.getDouble("Preco");
                qtdVagas = extras.getInt("Vagas");
                informacoes = extras.getString("Informacoes");
            }

            if(callerIntent.equals("EditarMeuAnuncioDados")){

                idAnuncio = extras.getString("idAnuncio");

                // Atual e Novo para diferenciar o setText do setHint:

                // Informações originais:
                endereco = extras.getString("EnderecoAtual");
                numero = extras.getString("NumeroAtual");
                complemento = extras.getString("ComplementoAtual");
                cep = extras.getString("CepAtual");
                bairro = extras.getString("BairroAtual");
                cidade = extras.getString("CidadeAtual");
                estadoAtual = extras.getString("EstadoAtual");
                estadoSelecionado = extras.getString("EstadoSelecionado");
                preco = extras.getString("PrecoAtual");
                vagas = extras.getString("VagasAtual");
                informacoes = extras.getString("InformacoesAtual");

                // Novas informações digitadas pelo usuário:
                enderecoNovo = extras.getString("EnderecoNovo");
                numeroNovo = extras.getString("NumeroNovo");
                complementoNovo = extras.getString("ComplementoNovo");
                cepNovo = extras.getString("CepNovo");
                bairroNovo = extras.getString("BairroNovo");
                cidadeNovo = extras.getString("CidadeNovo");
                estadoAtual = extras.getString("EstadoAtual");
                estadoSelecionado = extras.getString("EstadoSelecionado");
                precoNovo = extras.getString("PrecoNovo");
                vagasNovo = extras.getString("VagasNovo");
                informacoesNovo = extras.getString("InformacoesNovo");
                estadoAtualizado = extras.getBoolean("EstadoAtualizado");
            }

            if(callerIntent.equals("ProcurarActivity")){

                termoPesquisado = extras.getString("TermoPesquisado");
                precoMinimo = extras.getDouble("PrecoMinimo");
                precoMaximo = extras.getDouble("PrecoMaximo");
                bairro = extras.getString("Bairro");
                cidade = extras.getString("Cidade");
                estado = extras.getString("Estado");
                qtdVagas = extras.getInt("Vagas");
                raioDistancia = extras.getDouble("Distancia");
                ordenacao = extras.getString("Ordenacao");

                procuraRealizada = extras.getBoolean("ProcuraRealizada");
                onBackPressedFlag = extras.getBoolean("onBackPressedFlag");
                resultadoFiltragem = extras.getBoolean("FlagEditarFiltro");

                System.out.println("\n\nPESQUISANDO (MENU) - DADOS RECEBIDOS DO PROCURAR");

                Log.i("PESQUISANDO", "Termo = " + termoPesquisado);
                Log.i("PESQUISANDO", "Minimo = " + precoMinimo);
                Log.i("PESQUISANDO", "Maximo = " + precoMaximo);
                Log.i("PESQUISANDO", "Bairro = " + bairro);
                Log.i("PESQUISANDO", "Cidade = " + cidade);
                Log.i("PESQUISANDO", "Estado = " + estado);
                Log.i("PESQUISANDO", "Vagas = " + qtdVagas);
                Log.i("PESQUISANDO", "Distancia = " + raioDistancia);
                Log.i("PESQUISANDO", "Ordenaca = " + ordenacao);
                Log.i("PESQUISANDO", "Procura realizada = " + procuraRealizada);
                Log.i("PESQUISANDO", "onBackPressedFlag = " + onBackPressedFlag);
                Log.i("PESQUISANDO", "resultadoFiltragem = " + resultadoFiltragem);

                System.out.println("\n\n");
            }

            // Dados digitados pelo usuário antes de acionar o MenuActivity:
            if(callerIntent.equals("FiltrarActivity")){

                procuraRealizada = extras.getBoolean("ProcuraRealizada");
                resultadoFiltragem = extras.getBoolean("FlagEditarFiltro");

                termoPesquisado = extras.getString("TermoPesquisado");
                precoMinimoStr = extras.getString("PrecoMinimo");
                precoMaximoStr = extras.getString("PrecoMaximo");
                bairro = extras.getString("Bairro");
                cidade = extras.getString("Cidade");
                estado = extras.getString("Estado");
                qtdVagasStr = extras.getString("Vagas");
                raioDistanciaStr = extras.getString("Distancia");
                ordenacao = extras.getString("Ordenacao");

                System.out.println("\n\nMENU - DADOS RECEBIDOS DO FILTRAR");

                Log.i("DADOS", "Termo = " + termoPesquisado);
                Log.i("DADOS", "Minimo = " + precoMinimoStr);
                Log.i("DADOS", "Maximo = " + precoMaximoStr);
                Log.i("DADOS", "Bairro = " + bairro);
                Log.i("DADOS", "Cidade = " + cidade);
                Log.i("DADOS", "Estado = " + estado);
                Log.i("DADOS", "Vagas = " + qtdVagasStr);
                Log.i("DADOS", "Distancia = " + raioDistanciaStr);
                Log.i("DADOS", "Ordenaca = " + ordenacao);
                Log.i("DADOS", "ProcuraRealizada = " + procuraRealizada);
                Log.i("DADOS", "FlagEditarFiltro = " + resultadoFiltragem);

                System.out.println("\n");
            }

            if(callerIntent.equals("ProximidadesActivity")){

                onBackPressedFlag = extras.getBoolean("onBackPressedFlag");
                resultadoFiltragem = extras.getBoolean("FlagEditarFiltro");

                latitudes = extras.getDoubleArray("Latitudes");
                longitudes = extras.getDoubleArray("Longitudes");
                precos = extras.getDoubleArray("Precos");

                latitudeUsuario= extras.getDouble("LatitudeUsuario");
                longitudeUsuario = extras.getDouble("LongitudeUsuario");

                termoPesquisado = extras.getString("TermoPesquisado");
                precoMinimo = extras.getDouble("PrecoMinimo");
                precoMaximo = extras.getDouble("PrecoMaximo");
                bairro = extras.getString("Bairro");
                cidade = extras.getString("Cidade");
                estado = extras.getString("Estado");
                qtdVagas = extras.getInt("Vagas");
                raioDistancia = extras.getDouble("RaioDistancia");
                ordenacao = extras.getString("Ordenacao");

                System.out.println("\n\nDADOS RECEBIDOS DO PROXIMIDADES");

                Log.i("DADOS", "Termo = " + termoPesquisado);
                Log.i("DADOS", "Minimo = " + precoMinimo);
                Log.i("DADOS", "Maximo = " + precoMaximo);
                Log.i("DADOS", "Bairro = " + bairro);
                Log.i("DADOS", "Cidade = " + cidade);
                Log.i("DADOS", "Estado = " + estado);
                Log.i("DADOS", "Vagas = " + qtdVagas);
                Log.i("DADOS", "Distancia = " + raioDistancia);
                Log.i("DADOS", "Ordenaca = " + ordenacao);
                Log.i("DADOS", "onBackPressedFlag = " + onBackPressedFlag);
                Log.i("DADOS", "FlagEditarFiltro = " + resultadoFiltragem);
                Log.i("DADOS", "LatitudeUsuario = " + latitudeUsuario);
                Log.i("DADOS", "LongitudeUsuario = " + longitudeUsuario);
                Log.i("DADOS", "Latitudes = " + latitudes.toString());
                Log.i("DADOS", "Longitudes = " + longitudes.toString());
                Log.i("DADOS", "Precos = " + precos.toString());

                System.out.println("\n");
            }

            if(callerIntent.equals("Favoritos")){

            }

            if(callerIntent.equals("MeuCadastroActivity")){

            }

            if(callerIntent.equals("MeusAnunciosActivity")){

            }
        }
    }

    // Caso o usuário esteja cadastrando/editando um anúncio e cancele a ação:
    public void removerFotos(){

        if(!callerIntent.equals("EditarMeuAnuncioFotos")){

            if(!urlImagemPrincipal.equals("")){
                banco.removerImagem(urlImagemPrincipal);
            }

            if(!urlImagemDois.equals("")){
                banco.removerImagem(urlImagemDois);
            }

            if(!urlImagemTres.equals("")){
                banco.removerImagem(urlImagemTres);
            }

            if(!urlImagemQuatro.equals("")){
                banco.removerImagem(urlImagemQuatro);
            }

            if(!urlImagemCinco.equals("")){
                banco.removerImagem(urlImagemCinco);
            }
        }
        else{

            if(!urlImagemPrincipalNovo.equals("")){
                banco.removerImagem(urlImagemPrincipalNovo);
            }

            if(!urlImagemDoisNovo.equals("")){
                banco.removerImagem(urlImagemDoisNovo);
            }

            if(!urlImagemTresNovo.equals("")){
                banco.removerImagem(urlImagemTresNovo);
            }

            if(!urlImagemQuatroNovo.equals("")){
                banco.removerImagem(urlImagemQuatroNovo);
            }

            if(!urlImagemCincoNovo.equals("")){
                banco.removerImagem(urlImagemCincoNovo);
            }
        }
    }
}
