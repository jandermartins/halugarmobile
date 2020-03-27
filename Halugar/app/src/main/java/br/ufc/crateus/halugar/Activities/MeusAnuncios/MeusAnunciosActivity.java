package br.ufc.crateus.halugar.Activities.MeusAnuncios;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import br.ufc.crateus.halugar.Activities.main.MainActivity;
import br.ufc.crateus.halugar.Activities.Menu.MenuActivity;
import br.ufc.crateus.halugar.Activities.Sessao.Sessao;
import br.ufc.crateus.halugar.Banco.Banco;
import br.ufc.crateus.halugar.Model.Anuncio;
import br.ufc.crateus.halugar.Model.Usuario;
import br.ufc.crateus.halugar.ListagemAnuncios.MeusAnunciosAdapter;
import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.InternetCheck;
import br.ufc.crateus.halugar.Util.OnSwipeTouchListener;
import br.ufc.crateus.halugar.Util.Transition;

public class MeusAnunciosActivity extends AppCompatActivity {

    private String anuncioKey, idMeuAnuncio;

    RecyclerView rvMeusAnuncios;
    MeusAnunciosAdapter mAdapter;

    boolean encontrou=false;
    boolean atualizarFlag=true;

    ImageButton btnMenuMeusAnuncios;
    TextView labelQtdAnuncios, labelMegafone;
    ImageView ivNenhumMeusAnuncios;

    ConstraintLayout pbMeusAnunciosLayout;

    int qtdAnuncios=0;

    CheckBox cbOrdenar;

    Banco banco;
    Sessao sessao;

    Toolbar.LayoutParams params;

    Bundle extras;
    String callerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meus_anuncios);

        btnMenuMeusAnuncios = (ImageButton)findViewById(R.id.btnMenuMeusAnuncios);
        labelQtdAnuncios = (TextView)findViewById(R.id.labelQtdAnuncios);
        labelMegafone = (TextView)findViewById(R.id.labelMegafone);
        cbOrdenar = (CheckBox)findViewById(R.id.cbOrdenarMeusAnuncios);
        ivNenhumMeusAnuncios = (ImageView)findViewById(R.id.ivNenhumMeusAnuncios);

        rvMeusAnuncios= (RecyclerView) findViewById(R.id.rvMeusAnuncios);

        pbMeusAnunciosLayout = (ConstraintLayout)findViewById(R.id.pbMeusAnunciosLayout);

        params = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.WRAP_CONTENT
        );

        banco = Banco.getInstance();
        sessao = Sessao.getInstance();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvMeusAnuncios.setLayoutManager(layoutManager);
        mAdapter = new MeusAnunciosAdapter(new ArrayList<>(0), getApplicationContext());
        rvMeusAnuncios.setAdapter(mAdapter);

        carregarExtras();
        habilitarSwipeMenu();
        carregarAnuncios();

        btnMenuMeusAnuncios.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                atualizarFlag = false;

                Intent i = new Intent(MeusAnunciosActivity.this, MenuActivity.class);

                i.putExtra("callerIntent", "MeusAnunciosActivity");

                startActivity(i);
                Transition.enterTransition(MeusAnunciosActivity.this);
                finish();
            }
        });

        cbOrdenar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ordenarAnuncios();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
                pbMeusAnunciosLayout.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public void onBackPressed() {

        Intent i = new Intent(MeusAnunciosActivity.this, MainActivity.class);

        startActivity(i);
        Transition.backTransition(MeusAnunciosActivity.this);
        finish();
    }

    public void insertItem(Anuncio anuncio) {
        mAdapter.insertItem(anuncio);
        sortListAscending();
    }

    public void sortListAscending(){
        mAdapter.sortListAscending();
    }

    public void sortListDescending(){
        mAdapter.sortListDescending();
    }

    public void ordenarAnuncios(){
        if (!cbOrdenar.isChecked()) {
            sortListDescending();
        }
        else{
            sortListAscending();
        }
    }

    public void habilitarSwipeMenu(){

        rvMeusAnuncios.setOnTouchListener(new OnSwipeTouchListener(MeusAnunciosActivity.this) {
            public void onSwipeRight() {
                atualizarFlag = false;

                Intent i = new Intent(MeusAnunciosActivity.this, MenuActivity.class);

                i.putExtra("callerIntent", "MeusAnunciosActivity");

                startActivity(i);
                Transition.enterTransition(MeusAnunciosActivity.this);
                finish();
            }
        });
    }

    public void carregarAnuncios(){

        banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot usuario : dataSnapshot.getChildren()){
                    if (sessao.getIdUsuario().equals(usuario.getValue(Usuario.class).getuId())) {

                        String userKey = usuario.getKey();

                        // Percorrendo tabela de anúncios do usuário:
                        banco.getTabelaMeusAnuncios(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if(dataSnapshot.getValue()==null){

                                    labelMegafone.setVisibility(View.GONE);
                                    labelQtdAnuncios.setText("NENHUM ANÚNCIO CADASTRADO");
                                    labelQtdAnuncios.setVisibility(View.VISIBLE);
                                    ivNenhumMeusAnuncios.setVisibility(View.VISIBLE);

                                    pbMeusAnunciosLayout.setVisibility(View.INVISIBLE);
                                }

                                for(DataSnapshot meuAnuncio : dataSnapshot.getChildren()){
                                    // Adicionar anuncios no recycler view:
                                    idMeuAnuncio = meuAnuncio.getValue().toString();

                                    // Procurar o anuncio do usuário na tabela de anúncios:
                                    banco.getTabelaAnuncio().addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            for (DataSnapshot anuncio : dataSnapshot.getChildren()) {

                                                idMeuAnuncio = meuAnuncio.getValue().toString();
                                                anuncioKey = anuncio.getKey();

                                                if(idMeuAnuncio.equals(anuncioKey)){

                                                    pbMeusAnunciosLayout.setVisibility(View.INVISIBLE);

                                                    // Inserir anúncio
                                                    final Anuncio meuAnuncioNovo = new Anuncio();
                                                    meuAnuncioNovo.setaId(anuncio.getKey());
                                                    meuAnuncioNovo.setEndereco(anuncio.getValue(Anuncio.class).getEndereco());
                                                    meuAnuncioNovo.setNumero(anuncio.getValue(Anuncio.class).getNumero());
                                                    meuAnuncioNovo.setBairro(anuncio.getValue(Anuncio.class).getBairro());
                                                    meuAnuncioNovo.setCep(anuncio.getValue(Anuncio.class).getCep());
                                                    meuAnuncioNovo.setCidade(anuncio.getValue(Anuncio.class).getCidade());
                                                    meuAnuncioNovo.setEstado(anuncio.getValue(Anuncio.class).getEstado());
                                                    meuAnuncioNovo.setComplemento(anuncio.getValue(Anuncio.class).getComplemento());
                                                    meuAnuncioNovo.setInformacoesAdicionais(anuncio.getValue(Anuncio.class).getInformacoesAdicionais());
                                                    meuAnuncioNovo.setPrecoAluguel(anuncio.getValue(Anuncio.class).getPrecoAluguel());
                                                    meuAnuncioNovo.setQtdVagas(anuncio.getValue(Anuncio.class).getQtdVagas());
                                                    meuAnuncioNovo.setUrlImagemPrincipal(anuncio.getValue(Anuncio.class).getUrlImagemPrincipal());
                                                    meuAnuncioNovo.setUrlImagemDois(anuncio.getValue(Anuncio.class).getUrlImagemDois());
                                                    meuAnuncioNovo.setUrlImagemTres(anuncio.getValue(Anuncio.class).getUrlImagemTres());
                                                    meuAnuncioNovo.setUrlImagemQuatro(anuncio.getValue(Anuncio.class).getUrlImagemQuatro());
                                                    meuAnuncioNovo.setUrlImagemCinco(anuncio.getValue(Anuncio.class).getUrlImagemCinco());
                                                    meuAnuncioNovo.setKeyUsuario(anuncio.getValue(Anuncio.class).getKeyUsuario());
                                                    meuAnuncioNovo.setLatitude(anuncio.getValue(Anuncio.class).getLatitude());
                                                    meuAnuncioNovo.setLongitude(anuncio.getValue(Anuncio.class).getLongitude());

                                                    insertItem(meuAnuncioNovo);

                                                    encontrou = true;
                                                    ++qtdAnuncios;

                                                    if(qtdAnuncios==1){
                                                        labelQtdAnuncios.setText(qtdAnuncios + " ANÚNCIO CADASTRADO");
                                                        params.setMargins(145, 0, 0, 0);
                                                        labelMegafone.setLayoutParams(params);
                                                        labelQtdAnuncios.setVisibility(View.VISIBLE);
                                                        labelMegafone.setVisibility(View.VISIBLE);
                                                        ivNenhumMeusAnuncios.setVisibility(View.INVISIBLE);
                                                    }
                                                    else
                                                    if(qtdAnuncios>1 && qtdAnuncios<10){
                                                        labelQtdAnuncios.setText(qtdAnuncios + " ANÚNCIOS CADASTRADOS");
                                                        params.setMargins(125, 0, 0, 0);
                                                        labelMegafone.setLayoutParams(params);
                                                        labelQtdAnuncios.setVisibility(View.VISIBLE);
                                                        labelMegafone.setVisibility(View.VISIBLE);
                                                        cbOrdenar.setVisibility(View.VISIBLE);
                                                        ivNenhumMeusAnuncios.setVisibility(View.INVISIBLE);
                                                    }
                                                    else
                                                    if(qtdAnuncios>=10 && qtdAnuncios<=99){
                                                        labelQtdAnuncios.setText(qtdAnuncios + " ANÚNCIOS CADASTRADOS");
                                                        params.setMargins(120, 0, 0, 0);
                                                        labelMegafone.setLayoutParams(params);
                                                        labelQtdAnuncios.setVisibility(View.VISIBLE);
                                                        labelMegafone.setVisibility(View.VISIBLE);
                                                        cbOrdenar.setVisibility(View.VISIBLE);
                                                        ivNenhumMeusAnuncios.setVisibility(View.INVISIBLE);
                                                    }
                                                    else{
                                                        labelQtdAnuncios.setText(qtdAnuncios + " ANÚNCIOS CADASTRADOS");
                                                        params.setMargins(115, 0, 0, 0);
                                                        labelMegafone.setLayoutParams(params);
                                                        labelQtdAnuncios.setVisibility(View.VISIBLE);
                                                        labelMegafone.setVisibility(View.VISIBLE);
                                                        cbOrdenar.setVisibility(View.VISIBLE);
                                                        ivNenhumMeusAnuncios.setVisibility(View.INVISIBLE);
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

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public void carregarExtras(){

        extras = getIntent().getExtras();

        if(extras!=null){

            callerIntent = extras.getString("callerIntent");
        }
    }
}
