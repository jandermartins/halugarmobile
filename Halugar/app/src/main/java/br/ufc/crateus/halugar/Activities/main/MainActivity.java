package br.ufc.crateus.halugar.Activities.main;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import br.ufc.crateus.halugar.Activities.Anunciar.AnunciarDadosImovelActivity;
import br.ufc.crateus.halugar.Activities.Menu.MenuActivity;
import br.ufc.crateus.halugar.Activities.MeuCadastro.EditarCadastroActivity;
import br.ufc.crateus.halugar.Activities.Sessao.EntrarActivity;
import br.ufc.crateus.halugar.Activities.Sessao.Sessao;
import br.ufc.crateus.halugar.Banco.Banco;
import br.ufc.crateus.halugar.Model.Anuncio;
import br.ufc.crateus.halugar.Model.Usuario;
import br.ufc.crateus.halugar.ListagemAnuncios.AnuncioAdapter;
import br.ufc.crateus.halugar.Activities.Procurar.ProcurarActivity;
import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.InternetCheck;
import br.ufc.crateus.halugar.Util.OnSwipeTouchListener;
import br.ufc.crateus.halugar.Util.Transition;

public class MainActivity extends AppCompatActivity {

    RecyclerView rvTodosAnuncios;
    AnuncioAdapter mAdapter;

    Banco banco;
    Sessao sessao;

    Button btnAnunciarMain, btnProcurarMain;

    ImageButton btnEntrarIconeMain, btnMenuMain;
    TextView tvNenhumAnuncio;
    ImageView ivNenhumAnuncio;

    ConstraintLayout pbMainLayout;

    SharedPreferences credentials;
    boolean emailVerified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rvTodosAnuncios = (RecyclerView) findViewById(R.id.rvTodosAnuncios);

        pbMainLayout = (ConstraintLayout)findViewById(R.id.pbMainLayout);

        btnAnunciarMain = (Button) findViewById(R.id.btnAnunciarMenu);
        btnProcurarMain = (Button) findViewById(R.id.btnProcurar);
        btnEntrarIconeMain = (ImageButton) findViewById(R.id.btnEntrarIconeMain);
        btnMenuMain = (ImageButton)findViewById(R.id.btnMenuMain);
        tvNenhumAnuncio = (TextView)findViewById(R.id.tvNenhumAnuncio);
        ivNenhumAnuncio = (ImageView)findViewById(R.id.ivNenhumAnuncioMain);

        banco = Banco.getInstance();
        sessao = Sessao.getInstance();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvTodosAnuncios.setLayoutManager(layoutManager);
        mAdapter = new AnuncioAdapter(new ArrayList<>(0), getApplicationContext());
        rvTodosAnuncios.setAdapter(mAdapter);

        credentials = getSharedPreferences("credentials", MODE_PRIVATE);

        Log.i("CONTA - CREDENCIAIS", "userKey = " + credentials.getString("userKey", "-") + " | userPass = " + credentials.getString("userPassword", "-") + " | userId = " + credentials.getString("userId", "-"));

        verificarCredenciais();
        configurarSessaoUI();
        carregarAnuncios();

        btnAnunciarMain.setOnClickListener(view -> anunciar());

        btnProcurarMain.setOnClickListener(view -> procurar());

        btnEntrarIconeMain.setOnClickListener(view -> entrar());

        btnMenuMain.setOnClickListener(view -> {

            Intent i = new Intent(MainActivity.this, MenuActivity.class);

            i.putExtra("callerIntent", "MainActivity");

            startActivity(i);
            Transition.enterTransition(MainActivity.this);
            finish();
        });
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
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void insertItem(Anuncio anuncio) {
        mAdapter.insertItem(anuncio);
    }

    @Override
    public void onBackPressed() {

        Log.i("CONTA" ,"onBackPressed = Saindo...");

        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(a);
        finish();
    }

    public void carregarAnuncios(){
        // Thread para aguardar o carregamento dos anúncios:
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        //Log.i("MAIN", "carregando anúncios...");
                        runOnUiThread( new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    pbMainLayout.setVisibility(View.INVISIBLE);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                };

                thread.start();
            }
        }, 2000);

        banco.getTabelaAnuncio().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot anuncio : dataSnapshot.getChildren()) {

                    final Anuncio novoAnuncio = new Anuncio();

                    novoAnuncio.setaId(anuncio.getKey());

                    novoAnuncio.setEndereco(anuncio.getValue(Anuncio.class).getEndereco());
                    novoAnuncio.setNumero(anuncio.getValue(Anuncio.class).getNumero());
                    novoAnuncio.setBairro(anuncio.getValue(Anuncio.class).getBairro());
                    novoAnuncio.setCep(anuncio.getValue(Anuncio.class).getCep());
                    novoAnuncio.setCidade(anuncio.getValue(Anuncio.class).getCidade());
                    novoAnuncio.setEstado(anuncio.getValue(Anuncio.class).getEstado());
                    novoAnuncio.setComplemento(anuncio.getValue(Anuncio.class).getComplemento());
                    novoAnuncio.setInformacoesAdicionais(anuncio.getValue(Anuncio.class).getInformacoesAdicionais());
                    novoAnuncio.setPrecoAluguel(anuncio.getValue(Anuncio.class).getPrecoAluguel());
                    novoAnuncio.setQtdVagas(anuncio.getValue(Anuncio.class).getQtdVagas());
                    novoAnuncio.setUrlImagemPrincipal(anuncio.getValue(Anuncio.class).getUrlImagemPrincipal());
                    novoAnuncio.setUrlImagemDois(anuncio.getValue(Anuncio.class).getUrlImagemDois());
                    novoAnuncio.setUrlImagemTres(anuncio.getValue(Anuncio.class).getUrlImagemTres());
                    novoAnuncio.setUrlImagemQuatro(anuncio.getValue(Anuncio.class).getUrlImagemQuatro());
                    novoAnuncio.setUrlImagemCinco(anuncio.getValue(Anuncio.class).getUrlImagemCinco());
                    novoAnuncio.setKeyUsuario(anuncio.getValue(Anuncio.class).getKeyUsuario());
                    novoAnuncio.setLatitude(anuncio.getValue(Anuncio.class).getLatitude());
                    novoAnuncio.setLongitude(anuncio.getValue(Anuncio.class).getLongitude());

                    insertItem(novoAnuncio);
                }

                if(mAdapter.getItemCount()==0){
                    ivNenhumAnuncio.setVisibility(View.VISIBLE);
                    tvNenhumAnuncio.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void verificarCredenciais(){
        if(credentials.contains("userVerified")){
            emailVerified = true;
        }
        else{
            emailVerified = false;
        }
    }

    public void configurarSessaoUI(){

        // APÓS CONFIGURAR SWIPE MENU/BACK, verificar se o emailVerified é necessário...

        if (sessao.getUsuario()==null) {

            btnEntrarIconeMain.setImageResource(R.mipmap.ic_entrar_foreground);
            btnMenuMain.setVisibility(View.INVISIBLE);
        }
        else
        if(sessao.getUsuario()!=null && emailVerified==false){

            btnEntrarIconeMain.setImageResource(R.mipmap.ic_entrar_foreground);
            btnMenuMain.setVisibility(View.INVISIBLE);
            Sessao.logout();
            Log.i("CONTA - MAIN", "Logado, mas não entrou corretamente, através da EntrarActivity || possui credenciais...");
        }

        if(sessao.getUsuario()!=null && emailVerified){

            btnMenuMain.setVisibility(View.VISIBLE);
            btnEntrarIconeMain.setVisibility(View.INVISIBLE);

            rvTodosAnuncios.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {
                public void onSwipeRight() {

                    Intent i = new Intent(MainActivity.this, MenuActivity.class);

                    i.putExtra("callerIntent", "MainActivity");

                    startActivity(i);
                    Transition.enterTransition(MainActivity.this);
                    finish();
                }
            });

            Log.i("CONTA - MAIN", "Logado e entrou corretamente, através da EntrarActivity || possui credenciais...");
        }
    }

    public void procurar(){

        Intent i = new Intent(MainActivity.this, ProcurarActivity.class);

        i.putExtra("callerIntent", "MainActivity");

        startActivity(i);
        Transition.enterTransition(MainActivity.this);
        finish();
    }

    public void anunciar(){
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                if(sessao.getUsuario()==null){

                    Intent i = new Intent(MainActivity.this, EntrarActivity.class);

                    i.putExtra("callerIntent", "MainActivity");

                    startActivity(i);
                    Transition.enterTransition(MainActivity.this);
                    finish();
                }
                else{

                    // Se o usuário não tiver cadastrado um telefone, exibir mensagem:
                    banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot usuario : dataSnapshot.getChildren()){
                                if(sessao.getIdUsuario().equals(usuario.getValue(Usuario.class).getuId())){
                                    if(usuario.getValue(Usuario.class).getTelefone().equals("")) {

                                        CustomToast.mostrarMensagem(getApplicationContext(), "Telefone para contato não cadastrado", Toast.LENGTH_LONG);

                                        //startActivity(new Intent(MainActivity.this, AnunciarDadosImovelActivity.class));
                                        Intent i = new Intent(MainActivity.this, AnunciarDadosImovelActivity.class);

                                        i.putExtra("callerIntent", "MainActivity");

                                        startActivity(i);
                                        Transition.enterTransition(MainActivity.this);
                                        finish();
                                    }
                                    else{

                                        Intent i = new Intent(MainActivity.this, AnunciarDadosImovelActivity.class);

                                        i.putExtra("callerIntent", "MainActivity");

                                        startActivity(i);
                                        Transition.enterTransition(MainActivity.this);
                                        finish();
                                    }
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

    public void entrar(){

        Intent i = new Intent(MainActivity.this, EntrarActivity.class);

        i.putExtra("callerIntent", "MainActivity");

        startActivity(i);
        Transition.enterTransition(this);
        finish();
    }
}