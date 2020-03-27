package br.ufc.crateus.halugar.Activities.Procurar;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

import br.ufc.crateus.halugar.Activities.main.MainActivity;
import br.ufc.crateus.halugar.Activities.Menu.MenuActivity;
import br.ufc.crateus.halugar.Activities.Sessao.Sessao;
import br.ufc.crateus.halugar.Banco.Banco;
import br.ufc.crateus.halugar.Model.Anuncio;
import br.ufc.crateus.halugar.ListagemAnuncios.PesquisaAdapter;
import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.InternetCheck;
import br.ufc.crateus.halugar.Util.OnSwipeTouchListener;
import br.ufc.crateus.halugar.Util.Transition;
import br.ufc.crateus.halugar.Util.Validacao;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class ProcurarActivity extends AppCompatActivity {

    EditText etProcurar;
    ImageButton btnProcurar, btnEntrarIconeProcurar, btnMenuVoltarPesquisar;
    RecyclerView rvResultado;
    PesquisaAdapter mAdapter;
    Button btnFiltragem;
    TextView tvQtdResultados;
    ImageView ivNenhumAnuncioPesquisa;

    int cont = 0;

    String ordenacao="Preço crescente"; // Padrão
    boolean ordemCrescente=true, ordemDecrescente=false;

    FloatingActionButton btnProximidades;
    ConstraintLayout pbProcurarLayout;

    ArrayList<Anuncio> anunciosResultado = new ArrayList<>();

    private LocationRequest locationRequest;

    boolean procurarPalavraChave=false, procurarEstado=false, procurarCidade=false, procurarBairro=false, procurarVagas=false, procurarPrecoMinimo=false,
            procurarPrecoMaximo=false, procurarDistancia=false;

    String termoPesquisado = "", estado = "", cidade = "", bairro = "";
    double precoMinimo=0, precoMaximo=0, raioDistancia=0;
    int qtdVagas=0;
    boolean resultadoFiltragem=false, encontrou=false, procuraRealizada=false;

    double latitudeUsuario=0, longitudeUsuario=0, latitudeAnuncio=0, longitudeAnuncio=0, distancia=0;

    long ultimoClique = 0;

    double[] latitudes, longitudes, precos;

    Banco banco;
    Sessao sessao;

    Bundle extras;
    String callerIntent;

    boolean onBackPressedFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_procurar);

        etProcurar = (EditText) findViewById(R.id.itProcurar);
        btnProcurar = (ImageButton) findViewById(R.id.btnProcurar);
        btnEntrarIconeProcurar = (ImageButton) findViewById(R.id.btnEntrarIconeProcurar);
        btnMenuVoltarPesquisar = (ImageButton) findViewById(R.id.btnMenuPesquisar);
        btnFiltragem = (Button) findViewById(R.id.btnFiltragem);
        tvQtdResultados = (TextView) findViewById(R.id.tvQtdResultados);
        btnProximidades = (FloatingActionButton)findViewById(R.id.btnProximidades);
        rvResultado = (RecyclerView) findViewById(R.id.rvResultado);
        ivNenhumAnuncioPesquisa = (ImageView)findViewById(R.id.ivNenhumAnuncioPesquisa);

        pbProcurarLayout = (ConstraintLayout)findViewById(R.id.pbProcurarLayout);

        ordemCrescente = true; // Padrão

        procurarPalavraChave = procurarEstado = procurarCidade = procurarBairro = procurarVagas = procurarPrecoMinimo = procurarPrecoMaximo = procurarDistancia = false;

        banco = Banco.getInstance();
        sessao = Sessao.getInstance();

        tvQtdResultados.setVisibility(INVISIBLE);
        ivNenhumAnuncioPesquisa.setVisibility(INVISIBLE);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvResultado.setLayoutManager(layoutManager);

        mAdapter = new PesquisaAdapter(new ArrayList<>(0), getApplicationContext());
        rvResultado.setAdapter(mAdapter);

        btnProcurar.setOnClickListener(view -> procurar());

        pesquisarLocalizacao();
        carregarInformacoes();
        configurarSessaoUI();

        // Só realizar a pesquisa se o usuário tiver clicado no botão filtrar
        //if (resultadoFiltragem && !onBackPressedFlag) {
        //    Log.i("PESQUISANDO", "Filtrando pesquisa...");
        //    filtrarPesquisa();
        //}

        btnMenuVoltarPesquisar.setOnClickListener(view -> exibirMenuVoltar());

        btnFiltragem.setOnClickListener(view -> realizarFiltragem());

        btnProximidades.setOnClickListener(view -> mostrarProximidades());
    }

    @Override
    protected void onStart() {
        super.onStart();
        new InternetCheck(internet -> {
            if (!internet) {
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
                pbProcurarLayout.setVisibility(INVISIBLE);
            }
        });
    }

    public void insertItem(Anuncio anuncio) {
        mAdapter.insertItem(anuncio);

        if(ordemCrescente){
            sortListAscending();
        }

        if(ordemDecrescente){
            sortListDescending();
        }
    }

    public void clearList() {
        mAdapter.clearList();
    }

    public void sortListAscending(){
        mAdapter.sortListAscending();
    }

    public void sortListDescending(){
        mAdapter.sortListDescending();
    }

    @Override
    public void onBackPressed() {

        Intent i = new Intent(ProcurarActivity.this, MainActivity.class);

        startActivity(i);
        Transition.backTransition(ProcurarActivity.this);
        finish();
    }

    private void startLocationUpdates() {

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(200);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{
                LocationServices.getFusedLocationProviderClient(this).requestLocationUpdates
                        (locationRequest, new LocationCallback() {
                            @Override
                            public void onLocationResult(LocationResult locationResult) {
                                if (locationResult == null) {
                                    return;
                                }
                                for (Location location : locationResult.getLocations()) {
                                    if (location != null) {

                                        latitudeUsuario = location.getLatitude();
                                        longitudeUsuario = location.getLongitude();
                                        //Log.i("AQUI", ""+latitudeUsuario);
                                        //Log.i("AQUI", ""+longitudeUsuario);

                                        //showCustomAlert("AQUI\n\n" + latitudeUsuario + "\n" + longitudeUsuario);
                                    }
                                }
                            }
                        }, Looper.myLooper());
            }
        });
    }

    public void pesquisarLocalizacao(){
        // Resgatando localização atual para uma possível busca por raio de distância:
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ).withListener(new MultiplePermissionsListener() {

            @Override
            public void onPermissionsChecked(MultiplePermissionsReport report) {
                startLocationUpdates();
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
            }

        }).check();
    }

    public void carregarInformacoes(){

        extras = getIntent().getExtras();

        if (extras!=null) {

            callerIntent = extras.getString("callerIntent");

            if(callerIntent.equals("FiltrarActivity")){

                // Dados recebidos da tela FILTRAGEM:
                resultadoFiltragem = extras.getBoolean("FlagEditarFiltro");
                onBackPressedFlag = extras.getBoolean("onBackPressedFlag");
                procuraRealizada = extras.getBoolean("ProcuraRealizada");

                termoPesquisado = extras.getString("TermoPesquisado");
                estado = extras.getString("Estado");
                bairro = extras.getString("Bairro");
                cidade = extras.getString("Cidade");
                precoMinimo = extras.getDouble("PrecoMinimo");
                precoMaximo = extras.getDouble("PrecoMaximo");
                qtdVagas = extras.getInt("Vagas");
                raioDistancia = extras.getDouble("RaioDistancia");
                ordenacao = extras.getString("Ordenacao");

                etProcurar.setText(termoPesquisado);

                // Verificando quais campos serão utilizados na filtragem:
                if (!termoPesquisado.equals("")){
                    procurarPalavraChave = true;
                }

                if (!estado.equals("")){
                    procurarEstado = true;
                }

                if (!cidade.equals("")){
                    procurarCidade = true;
                }

                if (!bairro.equals("")){
                    procurarBairro = true;
                }

                if (qtdVagas != 0){
                    procurarVagas = true;
                }

                if (precoMinimo != 0){
                    procurarPrecoMinimo = true;
                }

                if (precoMaximo != 0){
                    procurarPrecoMaximo = true;
                }

                if (raioDistancia != 0){
                    procurarDistancia = true;
                }

                if(ordenacao.equals("Preço crescente")){
                    ordemCrescente = true;
                    ordemDecrescente = false;
                }

                if(ordenacao.equals("Preço decrescente")){
                    ordemDecrescente = true;
                    ordemCrescente = false;
                }

                System.out.println("\n\nPESQUISANDO - CAMPOS PARA FILTRAGEM (vindos de Filtrar)");

                Log.i("PESQUISANDO", "Termo = " + procurarPalavraChave);
                Log.i("PESQUISANDO", "Minimo = " + procurarPrecoMinimo);
                Log.i("PESQUISANDO", "Maximo = " + procurarPrecoMaximo);
                Log.i("PESQUISANDO", "Bairro = " + procurarBairro);
                Log.i("PESQUISANDO", "Cidade = " + procurarCidade);
                Log.i("PESQUISANDO", "Estado = " + procurarEstado);
                Log.i("PESQUISANDO", "Vagas = " + procurarVagas);
                Log.i("PESQUISANDO", "Distancia = " + procurarDistancia);
                Log.i("PESQUISANDO", "Ordenacao = " + ordenacao);
                Log.i("PESQUISANDO", "ProcuraRealizada = " + procuraRealizada);
                Log.i("PESQUISANDO", "FlagEditarFiltro = " + resultadoFiltragem);

                System.out.println("\n");

                // Recuperando pesquisa do usuário apenas se ele tiver clicado no botão buscar ao voltar com onBackPressed:
                if(procuraRealizada && onBackPressedFlag){
                    btnProcurar.performClick();
                }

                // Só realizar a pesquisa se o usuário tiver clicado no botão filtrar
                if (resultadoFiltragem && !onBackPressedFlag) {
                    Log.i("PESQUISANDO", "Filtrando pesquisa... (resultadoFiltragem==true && onBackPressed==false\n\n");
                    filtrarPesquisa();
                }
            }
            
            if(callerIntent.equals("MenuActivity")){

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
                resultadoFiltragem = extras.getBoolean("FlagEditarFiltro");

                // Configurando UI e pesquisa
                etProcurar.setText(termoPesquisado);

                // Verificando quais campos serão utilizados na filtragem:
                if (!termoPesquisado.equals("")){
                    procurarPalavraChave = true;
                }

                if (!estado.equals("")){
                    procurarEstado = true;
                }

                if (!cidade.equals("")){
                    procurarCidade = true;
                }

                if (!bairro.equals("")){
                    procurarBairro = true;
                }

                if (qtdVagas != 0){
                    procurarVagas = true;
                }

                if (precoMinimo != 0){
                    procurarPrecoMinimo = true;
                }

                if (precoMaximo != 0){
                    procurarPrecoMaximo = true;
                }

                if (raioDistancia != 0){
                    procurarDistancia = true;
                }

                if(ordenacao.equals("Preço crescente")){
                    ordemCrescente = true;
                    ordemDecrescente = false;
                }

                if(ordenacao.equals("Preço decrescente")){
                    ordemDecrescente = true;
                    ordemCrescente = false;
                }

                // Recuperando pesquisa (simples) do usuário apenas se ele tiver clicado no botão buscar:
                if(procuraRealizada && !resultadoFiltragem){
                    Log.i("PESQUISANDO", "Pesquisa (simples) novamente... [procuraRealizada==true + resultadoFiltragem==false]");
                    btnProcurar.performClick();
                }

                if(resultadoFiltragem){
                    System.out.println("\n\nPESQUISANDO - MÉTODO filtrarPesquisa()");
                    Log.i("PESQUISANDO", "Filtrando pesquisa novamente... [resultadoFiltragem == true]");
                    filtrarPesquisa();
                }

                /*
                // Só realizar a pesquisa se o usuário tiver clicado no botão filtrar
                if (resultadoFiltragem && !onBackPressedFlag) {
                    System.out.println("\n\nPESQUISANDO - MÉTODO filtrarPesquisa() + onBackPressed==false");
                    Log.i("PESQUISANDO", "Filtrando pesquisa...");
                    filtrarPesquisa();
                }

                 */
            }

            if(callerIntent.equals("ProximidadesActivity")){

                latitudes = extras.getDoubleArray("Latitudes");
                longitudes = extras.getDoubleArray("Longitudes");
                precos = extras.getDoubleArray("Precos");

                onBackPressedFlag = extras.getBoolean("onBackPressedFlag");
                resultadoFiltragem = extras.getBoolean("FlagEditarFiltro");

                // A localização já está sendo buscana nesta Activity:
                //latitudeUsuario= extras.getDouble("LatitudeUsuario");
                //longitudeUsuario = extras.getDouble("LongitudeUsuario");

                termoPesquisado = extras.getString("TermoPesquisado");
                precoMinimo = extras.getDouble("PrecoMinimo");
                precoMaximo = extras.getDouble("PrecoMaximo");
                bairro = extras.getString("Bairro");
                cidade = extras.getString("Cidade");
                estado = extras.getString("Estado");
                qtdVagas = extras.getInt("Vagas");
                raioDistancia = extras.getDouble("RaioDistancia");
                ordenacao = extras.getString("Ordenacao");

                etProcurar.setText(termoPesquisado);

                // Só realizar pesquisa (procurar ou filtragem) se onBackPressedFlag for false:
                if(resultadoFiltragem && !onBackPressedFlag){
                    filtrarPesquisa();
                }

                //if(!onBackPressedFlag){
                //    btnProcurar.performClick();
                //}

                // Se não for resultado de filtragem, executar a busca simples normalmente com os dados anteriores:
                if(!resultadoFiltragem){
                    btnProcurar.performClick();
                }
            }

            if(callerIntent.equals("MainActivity")){

            }
        }
    }

    public void configurarSessaoUI(){

        if (sessao.getUsuario() == null) {

            btnMenuVoltarPesquisar.setImageResource(R.mipmap.ic_backarrow_foreground);
            btnMenuVoltarPesquisar.getLayoutParams().height = 40;
            btnMenuVoltarPesquisar.getLayoutParams().width = 40;
            btnEntrarIconeProcurar.setVisibility(INVISIBLE); // Evitar empilhamento onbackpressed  -> REMOVER BOTÃO
        }
        else {

            btnEntrarIconeProcurar.setVisibility(INVISIBLE);

            rvResultado.setOnTouchListener(new OnSwipeTouchListener(ProcurarActivity.this) {
                public void onSwipeRight() {

                    Intent i = new Intent(ProcurarActivity.this, MenuActivity.class);

                    i.putExtra("callerIntent", "ProcurarActivity");

                    // Informações de filtragem para atualizar ao retornar:
                    i.putExtra("TermoPesquisado", etProcurar.getText().toString());
                    i.putExtra("PrecoMinimo", precoMinimo);
                    i.putExtra("PrecoMaximo", precoMaximo);
                    i.putExtra("Bairro", bairro);
                    i.putExtra("Cidade", cidade);
                    i.putExtra("Estado", estado);
                    i.putExtra("Vagas", qtdVagas);
                    i.putExtra("RaioDistancia", raioDistancia);

                    if(resultadoFiltragem){
                        i.putExtra("FlagEditarFiltro", true);
                    }
                    else{
                        i.putExtra("FlagEditarFiltro", false);
                    }

                    i.putExtra("Ordenacao", ordenacao);

                    if(onBackPressedFlag){
                        i.putExtra("onBackPressedFlag", true);
                    }
                    else{
                        i.putExtra("onBackPressedFlag", false);
                    }

                    // Se o usuário chamar o menu após fazer uma pesquisa, refazer a pesquisa ao exibirMenuVoltar:
                    i.putExtra("ProcuraRealizada", procuraRealizada);

                    startActivity(i);
                    Transition.enterTransition(ProcurarActivity.this);
                    finish();
                }
            });
        }
    }

    public void procurar(){

        tvQtdResultados.setText("");
        tvQtdResultados.setVisibility(INVISIBLE);
        ivNenhumAnuncioPesquisa.setVisibility(INVISIBLE);

        new InternetCheck(internet -> {
            if (!internet) {
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                Log.i("PESQUISANDO", "realizando pesquisa...");
                Log.i("PESQUISANDO", "filtrando = " + resultadoFiltragem);

                // Prevenindo clique duplo por um limite de 3 segundos
                if (SystemClock.elapsedRealtime() - ultimoClique < 3000) return;
                ultimoClique = SystemClock.elapsedRealtime();

                procuraRealizada = true;

                termoPesquisado = etProcurar.getText().toString().trim().replaceAll(" +", " ");

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etProcurar.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

                if (termoPesquisado == null || termoPesquisado.isEmpty() || termoPesquisado.trim().isEmpty() || Validacao.validarInput(termoPesquisado)==false) {
                    //Log.i("Procurar", "Campo de pesquisa vazio ou inválido");

                    clearList();
                    btnProximidades.hide();

                    CustomToast.mostrarMensagem(getApplicationContext(), "Digite um termo a ser pesquisado", Toast.LENGTH_SHORT);
                }
                else {

                    Log.i("TESTE", "pesquisando novamente...");

                    pbProcurarLayout.setVisibility(VISIBLE);
                    tvQtdResultados.setVisibility(VISIBLE);

                    clearList();

                    banco.getTabelaAnuncio().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                            if (children == null) {
                                tvQtdResultados.setText("Nenhum anúncio foi encontrado");
                                pbProcurarLayout.setVisibility(INVISIBLE);
                                ivNenhumAnuncioPesquisa.setVisibility(VISIBLE);
                                btnProximidades.hide();
                            }
                            else {

                                String textoPesquisado = etProcurar.getText().toString().trim().replaceAll(" +", " ");
                                encontrou = false;

                                // Comparando termo com os campos Endereco, Bairro, Cidade, Estado e informações adicionais:
                                for (DataSnapshot anuncio : children) {
                                    if (anuncio.getValue(Anuncio.class).getBairro().toLowerCase()
                                            .contains(textoPesquisado.toLowerCase())
                                            || anuncio.getValue(Anuncio.class).getEndereco().toLowerCase()
                                            .contains(textoPesquisado.toLowerCase())
                                            || anuncio.getValue(Anuncio.class).getCidade().toLowerCase()
                                            .contains(textoPesquisado.toLowerCase())
                                            || anuncio.getValue(Anuncio.class).getEstado().toLowerCase()
                                            .contains(textoPesquisado.toLowerCase())
                                            || anuncio.getValue(Anuncio.class).getInformacoesAdicionais().toLowerCase()
                                            .contains(textoPesquisado.toLowerCase())) {

                                        Anuncio anuncioEncontrado = new Anuncio();

                                        anuncioEncontrado.setaId(anuncio.getKey());

                                        anuncioEncontrado.setEndereco(anuncio.getValue(Anuncio.class).getEndereco());
                                        anuncioEncontrado.setNumero(anuncio.getValue(Anuncio.class).getNumero());
                                        anuncioEncontrado.setBairro(anuncio.getValue(Anuncio.class).getBairro());
                                        anuncioEncontrado.setCep(anuncio.getValue(Anuncio.class).getCep());
                                        anuncioEncontrado.setCidade(anuncio.getValue(Anuncio.class).getCidade());
                                        anuncioEncontrado.setEstado(anuncio.getValue(Anuncio.class).getEstado());
                                        anuncioEncontrado.setComplemento(anuncio.getValue(Anuncio.class).getComplemento());
                                        anuncioEncontrado.setInformacoesAdicionais(
                                                anuncio.getValue(Anuncio.class).getInformacoesAdicionais());
                                        anuncioEncontrado.setPrecoAluguel(anuncio.getValue(Anuncio.class).getPrecoAluguel());
                                        anuncioEncontrado.setQtdVagas(anuncio.getValue(Anuncio.class).getQtdVagas());
                                        anuncioEncontrado.setUrlImagemPrincipal(
                                                anuncio.getValue(Anuncio.class).getUrlImagemPrincipal());
                                        anuncioEncontrado.setUrlImagemDois(anuncio.getValue(Anuncio.class).getUrlImagemDois());
                                        anuncioEncontrado.setUrlImagemTres(anuncio.getValue(Anuncio.class).getUrlImagemTres());
                                        anuncioEncontrado.setUrlImagemQuatro(
                                                anuncio.getValue(Anuncio.class).getUrlImagemQuatro());
                                        anuncioEncontrado.setUrlImagemCinco(anuncio.getValue(Anuncio.class).getUrlImagemCinco());
                                        anuncioEncontrado.setKeyUsuario(anuncio.getValue(Anuncio.class).getKeyUsuario());

                                        anuncioEncontrado.setLatitude(anuncio.getValue(Anuncio.class).getLatitude());
                                        anuncioEncontrado.setLongitude(anuncio.getValue(Anuncio.class).getLongitude());

                                        insertItem(anuncioEncontrado);
                                        anunciosResultado.add(anuncioEncontrado);

                                        btnProximidades.show();
                                        encontrou = true;
                                        ++cont;
                                    }
                                }

                                if (cont == 0) {
                                    btnProximidades.hide();
                                    tvQtdResultados.setVisibility(VISIBLE);
                                    pbProcurarLayout.setVisibility(INVISIBLE);
                                    tvQtdResultados.setText("Nenhum anúncio foi encontrado");
                                    ivNenhumAnuncioPesquisa.setVisibility(VISIBLE);
                                    cont = 0;
                                }
                                else if (cont == 1) {
                                    tvQtdResultados.setVisibility(VISIBLE);
                                    pbProcurarLayout.setVisibility(INVISIBLE);
                                    tvQtdResultados.setText("Foi encontrado 1 anúncio");
                                    ivNenhumAnuncioPesquisa.setVisibility(INVISIBLE);
                                    cont = 0;
                                }
                                else {
                                    tvQtdResultados.setVisibility(VISIBLE);
                                    pbProcurarLayout.setVisibility(INVISIBLE);
                                    tvQtdResultados.setText("Foram encontrados " + cont + " anúncios");
                                    ivNenhumAnuncioPesquisa.setVisibility(INVISIBLE);
                                    cont = 0;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
        });
    }

    public void exibirMenuVoltar(){

        // O usuário não tem acesso ao VOLTAR (MainActivity):
        if (sessao.getUsuario()==null) {

            if(callerIntent.equals("MainActivity")){

                Intent i = new Intent(ProcurarActivity.this, MainActivity.class);

                i.putExtra("callerIntent", "ProcurarActivity");

                startActivity(i);
                Transition.backTransition(ProcurarActivity.this);
                finish();
            }
        }
        // O usuário tem acesso ao botão MENU:
        else {

            Intent i = new Intent(ProcurarActivity.this, MenuActivity.class);

            i.putExtra("callerIntent", "ProcurarActivity");

            // Informações de filtragem para atualizar ao retornar:
            i.putExtra("TermoPesquisado", etProcurar.getText().toString());
            i.putExtra("PrecoMinimo", precoMinimo);
            i.putExtra("PrecoMaximo", precoMaximo);
            i.putExtra("Bairro", bairro);
            i.putExtra("Cidade", cidade);
            i.putExtra("Estado", estado);
            i.putExtra("Vagas", qtdVagas);
            i.putExtra("RaioDistancia", raioDistancia);

            if(resultadoFiltragem){
                i.putExtra("FlagEditarFiltro", true);
            }
            else{
                i.putExtra("FlagEditarFiltro", false);
            }

            i.putExtra("Ordenacao", ordenacao);

            if(onBackPressedFlag){
                i.putExtra("onBackPressedFlag", true);
            }
            else{
                i.putExtra("onBackPressedFlag", false);
            }

            // Se o usuário chamar o menu após fazer uma pesquisa, refazer a pesquisa ao exibirMenuVoltar:
            i.putExtra("ProcuraRealizada", procuraRealizada);

            startActivity(i);
            Transition.enterTransition(ProcurarActivity.this);
            finish();
        }
    }

    public void realizarFiltragem(){

        new InternetCheck(internet -> {
            if (!internet) {
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else {

                Intent i = new Intent(ProcurarActivity.this, FiltrarActivity.class);

                if (resultadoFiltragem) {

                    // Enviar dados da filtragem atual para possível alteração (enviar flag editarFiltragem)...
                    i.putExtra("callerIntent", "ProcurarActivity");

                    i.putExtra("FlagEditarFiltro", true);
                    i.putExtra("ProcuraRealizada", procuraRealizada);

                    i.putExtra("TermoPesquisado", etProcurar.getText().toString());
                    i.putExtra("PrecoMinimo", precoMinimo);
                    i.putExtra("PrecoMaximo", precoMaximo);
                    i.putExtra("Bairro", bairro);
                    i.putExtra("Cidade", cidade);
                    i.putExtra("Estado", estado);
                    i.putExtra("Vagas", qtdVagas);
                    i.putExtra("RaioDistancia", raioDistancia);
                    i.putExtra("Ordenacao", ordenacao);

                    startActivity(i);
                    Transition.enterTransition(this);
                    finish();
                }
                else {

                    termoPesquisado = etProcurar.getText().toString();

                    i.putExtra("callerIntent", "ProcurarActivity");

                    i.putExtra("ProcuraRealizada", procuraRealizada);
                    i.putExtra("TermoPesquisado",  etProcurar.getText().toString().trim().replaceAll(" +", " "));

                    startActivity(i);
                    Transition.enterTransition(ProcurarActivity.this);
                    finish();
                }
            }
        });
    }

    public void mostrarProximidades(){

        new InternetCheck(internet -> {
            if (!internet) {
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else {

                Intent i = new Intent(ProcurarActivity.this, ProximidadesActivity.class);

                latitudes = new double[anunciosResultado.size()];
                longitudes = new double[anunciosResultado.size()];
                precos = new double[anunciosResultado.size()];

                // Criando vetores com as latitudes, longitudes e preços de cada anúncio:
                for(int j=0; j<anunciosResultado.size(); j++){
                    latitudes[j] = anunciosResultado.get(j).getLatitude();
                    longitudes[j] = anunciosResultado.get(j).getLongitude();
                    precos[j] = anunciosResultado.get(j).getPrecoAluguel();
                }

                i.putExtra("callerIntent", "ProcurarActivity");

                i.putExtra("Latitudes", latitudes);
                i.putExtra("Longitudes", longitudes);
                i.putExtra("Precos", precos);
                i.putExtra("LatitudeUsuario", latitudeUsuario);
                i.putExtra("LongitudeUsuario", longitudeUsuario);

                // Informações de filtragem para atualizar ao retornar:
                i.putExtra("TermoPesquisado", etProcurar.getText().toString());
                i.putExtra("PrecoMinimo", precoMinimo);
                i.putExtra("PrecoMaximo", precoMaximo);
                i.putExtra("Bairro", bairro);
                i.putExtra("Cidade", cidade);
                i.putExtra("Estado", estado);
                i.putExtra("Vagas", qtdVagas);
                i.putExtra("RaioDistancia", raioDistancia);

                if(resultadoFiltragem){
                    i.putExtra("FlagEditarFiltro", true);
                }
                else{
                    i.putExtra("FlagEditarFiltro", false);
                }

                i.putExtra("Ordenacao", ordenacao);

                if(onBackPressedFlag){
                    i.putExtra("onBackPressedFlag", true);
                }
                else{
                    i.putExtra("onBackPressedFlag", false);
                }

                startActivity(i);
                Transition.enterTransition(ProcurarActivity.this);
                finish();
            }
        });
    }

    public void filtrarPesquisa(){

        pbProcurarLayout.setVisibility(VISIBLE);
        tvQtdResultados.setVisibility(VISIBLE);

        clearList();

        // Realizando busca com os dados da filtragem:

        // Procurar por PALAVRA-CHAVE (nos campos Endereco, Bairro, Cidade, Estado e Informações adicionais):
        if (procurarPalavraChave && !procurarPrecoMinimo && !procurarPrecoMaximo && !procurarBairro
                && !procurarCidade && !procurarEstado && !procurarVagas && !procurarDistancia) {

            banco.getTabelaAnuncio().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();

                    if (children == null) {
                        tvQtdResultados.setText("Nenhum anúncio foi encontrado");
                        ivNenhumAnuncioPesquisa.setVisibility(VISIBLE);
                        pbProcurarLayout.setVisibility(INVISIBLE);
                    }
                    else {

                        String textoPesquisado = etProcurar.getText().toString();
                        boolean encontrou = false;

                        for (DataSnapshot snapshot : children) {

                            if (snapshot.getValue(Anuncio.class).getBairro().toLowerCase()
                                    .contains(textoPesquisado.toLowerCase())
                                    || snapshot.getValue(Anuncio.class).getEndereco().toLowerCase()
                                    .contains(textoPesquisado.toLowerCase())
                                    || snapshot.getValue(Anuncio.class).getCidade().toLowerCase()
                                    .contains(textoPesquisado.toLowerCase())
                                    || snapshot.getValue(Anuncio.class).getEstado().toLowerCase()
                                    .contains(textoPesquisado.toLowerCase())
                                    || snapshot.getValue(Anuncio.class).getInformacoesAdicionais().toLowerCase()
                                    .contains(textoPesquisado.toLowerCase())) {

                                Anuncio anuncio = new Anuncio();

                                anuncio.setaId(snapshot.getKey());

                                anuncio.setEndereco(snapshot.getValue(Anuncio.class).getEndereco());
                                anuncio.setNumero(snapshot.getValue(Anuncio.class).getNumero());
                                anuncio.setBairro(snapshot.getValue(Anuncio.class).getBairro());
                                anuncio.setCep(snapshot.getValue(Anuncio.class).getCep());
                                anuncio.setCidade(snapshot.getValue(Anuncio.class).getCidade());
                                anuncio.setEstado(snapshot.getValue(Anuncio.class).getEstado());
                                anuncio.setComplemento(snapshot.getValue(Anuncio.class).getComplemento());
                                anuncio.setInformacoesAdicionais(
                                        snapshot.getValue(Anuncio.class).getInformacoesAdicionais());
                                anuncio.setPrecoAluguel(snapshot.getValue(Anuncio.class).getPrecoAluguel());
                                anuncio.setQtdVagas(snapshot.getValue(Anuncio.class).getQtdVagas());
                                anuncio.setUrlImagemPrincipal(
                                        snapshot.getValue(Anuncio.class).getUrlImagemPrincipal());
                                anuncio.setUrlImagemDois(snapshot.getValue(Anuncio.class).getUrlImagemDois());
                                anuncio.setUrlImagemTres(snapshot.getValue(Anuncio.class).getUrlImagemTres());
                                anuncio.setUrlImagemQuatro(snapshot.getValue(Anuncio.class).getUrlImagemQuatro());
                                anuncio.setUrlImagemCinco(snapshot.getValue(Anuncio.class).getUrlImagemCinco());
                                anuncio.setKeyUsuario(snapshot.getValue(Anuncio.class).getKeyUsuario());
                                anuncio.setLatitude(snapshot.getValue(Anuncio.class).getLatitude());
                                anuncio.setLongitude(snapshot.getValue(Anuncio.class).getLongitude());

                                anunciosResultado.add(anuncio);
                                encontrou = true;
                            }
                        }

                        if(encontrou) {
                            btnProximidades.show();
                        }

                        for (Anuncio anuncio : anunciosResultado) {
                            insertItem(anuncio);
                        }

                        tvQtdResultados.setVisibility(View.VISIBLE);

                        if (anunciosResultado.size() == 0) {

                            pbProcurarLayout.setVisibility(INVISIBLE);
                            tvQtdResultados.setText("Nenhum anúncio foi encontrado");
                            ivNenhumAnuncioPesquisa.setVisibility(VISIBLE);
                        }
                        else
                        if (anunciosResultado.size() == 1) {

                            pbProcurarLayout.setVisibility(INVISIBLE);
                            tvQtdResultados.setText("Foi encontrado 1 anúncio");
                            ivNenhumAnuncioPesquisa.setVisibility(INVISIBLE);
                        }
                        else {

                            pbProcurarLayout.setVisibility(INVISIBLE);
                            tvQtdResultados.setText("Foram encontrados " + anunciosResultado.size() + " anúncios");
                            ivNenhumAnuncioPesquisa.setVisibility(INVISIBLE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        // RESTANTE RECORTADO (MUITO PESADO)...
    }
}
