package br.ufc.crateus.halugar.Activities.Anuncio;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import br.ufc.crateus.halugar.Activities.Sessao.EntrarActivity;
import br.ufc.crateus.halugar.Activities.Menu.MenuActivity;
import br.ufc.crateus.halugar.Activities.Sessao.Sessao;
import br.ufc.crateus.halugar.Activities.main.MainActivity;
import br.ufc.crateus.halugar.Banco.Banco;
import br.ufc.crateus.halugar.Model.Usuario;
import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.InternetCheck;
import br.ufc.crateus.halugar.Util.Transition;

public class AnuncioActivity extends AppCompatActivity {

    ImageButton btnMenuAnuncio;

    private LocationRequest locationRequest;

    String nomeAnunciante, telefoneAnunciante, emailAnunciante;
    String endereco, complemento, cep, bairro, cidade, estado, informacoes;
    int numero, vagas;
    double preco, latitudeAnuncio, longitudeAnuncio, latitudeUsuario, longitudeUsuario;
    String urlImagemPrincipal, urlImagemDois, urlImagemTres, urlImagemQuatro, urlImagemCinco;

    FragmentManager fragmentManager = getSupportFragmentManager();

    Fragment informacoesFragment = new InformacoesFragment();
    Fragment contatoFragment = new ContatoFragment();
    Fragment fotosFragment = new FotosFragment();
    Fragment localizacaoFragment = new LocalizacaoFragment();

    CheckBox cbFavorito;

    Bundle argumentsInformacoes, argumentsContato, argumentsFotos, argumentsLocalizacao;

    FragmentTransaction fragmentTransaction;

    String idAnuncio;
    boolean encontrou = false;

    Banco banco;
    Sessao sessao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anuncio);

        cbFavorito = (CheckBox)findViewById(R.id.cbFavorito);
        btnMenuAnuncio = (ImageButton)findViewById(R.id.btnMenuAnuncio);
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);

        banco = Banco.getInstance();
        sessao = Sessao.getInstance();

        if(sessao.getUsuario()==null){
            Sessao.logout();
            startActivity(new Intent(AnuncioActivity.this, MainActivity.class));
            Transition.enterTransition(this);
        }
        else{
            cbFavorito.setVisibility(View.INVISIBLE);

            Transition.enterTransition(this); // Pois vem do adapter

            verificarLocalizacaoUsuario();

            Bundle extras = getIntent().getExtras();

            if(extras==null){
                Log.i("ERRO", "extras==null");
            }
            else{
                idAnuncio = extras.getString("idAnuncio");

                endereco = extras.getString("Endereco"); // Utilizado para passar os dados para LocalizacaoActivity
                numero = extras.getInt("Numero"); // Utilizado para passar os dados para LocalizacaoActivity
                complemento = extras.getString("Complemento");
                cep = extras.getString("CEP");
                bairro = extras.getString("Bairro"); // Utilizado para passar os dados para LocalizacaoActivity
                cidade = extras.getString("Cidade");
                estado = extras.getString("Estado");
                preco = extras.getDouble("Preco");
                vagas = extras.getInt("Vagas");
                informacoes = extras.getString("Informacoes");

                // Informações a serem passadas para LocalizacaoFragment:
                latitudeAnuncio = extras.getDouble("LatitudeAnuncio");
                longitudeAnuncio = extras.getDouble("LongitudeAnuncio");
                latitudeUsuario = extras.getDouble("LatitudeUsuario");
                longitudeUsuario = extras.getDouble("LongitudeUsuario");

                // Urls das imagens a serem passadas para FotosFrament:
                urlImagemPrincipal = extras.getString("urlImagemPrincipal");
                urlImagemDois = extras.getString("urlImagemDois");
                urlImagemTres = extras.getString("urlImagemTres");
                urlImagemQuatro = extras.getString("urlImagemQuatro");
                urlImagemCinco = extras.getString("urlImagemCinco");

                nomeAnunciante = extras.getString("nomeAnunciante");
                emailAnunciante = extras.getString("emailAnunciante");
                telefoneAnunciante = extras.getString("telefoneAnunciante");
            }

            verificarFavorito();

            // Realizar atualização do favorito ao clicar no check box:
            cbFavorito.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    atualizarFavorito(v);
                }
            });

            if (sessao.getUsuario() != null) {
                Log.i("MENSAGEM", "Usuário cadastrado acessou anúncio!");
            }
            else {
                startActivity(new Intent(AnuncioActivity.this, EntrarActivity.class));
                Transition.enterTransition(this);
            }

            Log.i("FROM ADAPTER", nomeAnunciante + " " + emailAnunciante + " " + telefoneAnunciante);

            argumentsInformacoes = new Bundle();
            argumentsFotos = new Bundle();
            argumentsContato = new Bundle();
            argumentsLocalizacao = new Bundle();

            argumentsInformacoes.putString("Endereco", endereco);
            argumentsInformacoes.putString("Numero", String.valueOf(numero));
            argumentsInformacoes.putString("Complemento", complemento);
            argumentsInformacoes.putString("Cep", cep);
            argumentsInformacoes.putString("Bairro", bairro);
            argumentsInformacoes.putString("Cidade", cidade);
            argumentsInformacoes.putString("Estado", estado);
            argumentsInformacoes.putString("Preco", String.valueOf(preco));
            argumentsInformacoes.putString("Vagas", String.valueOf(vagas));
            argumentsInformacoes.putString("Informacoes", informacoes);

            argumentsContato.putString("Nome", nomeAnunciante);
            argumentsContato.putString("Email", emailAnunciante);
            argumentsContato.putString("Telefone", telefoneAnunciante);

            argumentsFotos.putString("UrlImagemPrincipal", urlImagemPrincipal);
            argumentsFotos.putString("UrlImagemDois", urlImagemDois);
            argumentsFotos.putString("UrlImagemTres", urlImagemTres);
            argumentsFotos.putString("UrlImagemQuatro", urlImagemQuatro);
            argumentsFotos.putString("UrlImagemCinco", urlImagemCinco);

            argumentsLocalizacao.putDouble("LatitudeAnuncio", latitudeAnuncio);
            argumentsLocalizacao.putDouble("LongitudeAnuncio", longitudeAnuncio);
            argumentsLocalizacao.putDouble("LatitudeUsuario", latitudeUsuario);
            argumentsLocalizacao.putDouble("LongitudeUsuario", longitudeUsuario);

            informacoesFragment.setArguments(argumentsInformacoes);
            contatoFragment.setArguments(argumentsContato);
            fotosFragment.setArguments(argumentsFotos);
            localizacaoFragment.setArguments(argumentsLocalizacao);

            // handle navigation selection
            bottomNavigationView.setOnNavigationItemSelectedListener(
                    new BottomNavigationView.OnNavigationItemSelectedListener() {
                        @Override
                        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.navigation_informacoes:
                                    acessarDescricao();
                                    break;
                                case R.id.navigation_contato:
                                    acessarContato();
                                    break;
                                case R.id.navigation_fotos:
                                    acessarImagens();
                                    break;
                                case R.id.navigation_localizacao:
                                    acessarLocalizacao();
                                    break;
                                default: return true;
                            }
                            return true;
                        }
                    });

            // Set default selection
            bottomNavigationView.setSelectedItemId(R.id.navigation_informacoes);

            btnMenuAnuncio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //startActivity(new Intent(AnuncioActivity.this, MenuActivity.class));
                    Intent i = new Intent(AnuncioActivity.this, MenuActivity.class);
                    i.putExtra("callerIntent", "AnuncioActivity");
                    startActivity(i);
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Transition.backTransition(this);
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

                                        argumentsLocalizacao.putDouble("LatitudeUsuario", latitudeUsuario);
                                        argumentsLocalizacao.putDouble("LongitudeUsuario", longitudeUsuario);
                                    }
                                }
                            }
                        }, Looper.myLooper());
            }
        });
    }

    public void verificarLocalizacaoUsuario(){
        // Recuperando posição do usuário para enviar para LocalizacaoFragment:
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

    public void verificarFavorito(){
        // Verificar se o anúncio é favorito ou não e atualizar check box:

        banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                    if (sessao.getIdUsuario().equals(snapshot.getValue(Usuario.class).getuId())) {

                        String userKey = snapshot.getKey();

                        // Percorrendo tabela de favoritos do usuário atual:

                        banco.getTabelaFavoritos(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for(DataSnapshot favorito : dataSnapshot.getChildren()){

                                    // comparar id do anuncio atual com ids da lista de favoritos...

                                    if(favorito.getValue().equals(idAnuncio)){
                                        encontrou=true;
                                    }
                                }

                                // Atualizando checkbox:

                                if(encontrou){
                                    cbFavorito.setChecked(true);
                                    cbFavorito.setVisibility(View.VISIBLE);
                                }
                                else{
                                    cbFavorito.setChecked(false);
                                    cbFavorito.setVisibility(View.VISIBLE);
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

    public void atualizarFavorito(View v){
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{
                //is chkIos checked?
                if (!((CheckBox) v).isChecked()) { // É favorito
                    // Remover dos favoritos:
                    banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for(DataSnapshot usuario : dataSnapshot.getChildren()){

                                if (sessao.getIdUsuario().equals(usuario.getValue(Usuario.class).getuId())) {

                                    String userKey = usuario.getKey();

                                    // Percorrendo tabela de favoritos do usuário atual:

                                    banco.getTabelaFavoritos(userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                            for(DataSnapshot favorito : dataSnapshot.getChildren()){

                                                // comparar id do anuncio atual com ids da lista de favoritos...

                                                if(favorito.getValue().equals(idAnuncio)){

                                                    String favoritoKey = favorito.getKey();
                                                    encontrou=true;
                                                    banco.getTabelaFavoritos(userKey).child(favoritoKey).removeValue();
                                                    cbFavorito.setChecked(false);
                                                    CustomToast.mostrarMensagem(getApplicationContext(), "Removido dos favoritos", Toast.LENGTH_SHORT);
                                                }
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
                else{ // Não é favorito

                    // Adicionar aos favoritos:

                    banco.getTabelaUsuario().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                                if (sessao.getIdUsuario().equals(snapshot.getValue(Usuario.class).getuId())) {

                                    String userKey = snapshot.getKey();

                                    banco.getTabelaFavoritos(userKey).push().setValue(idAnuncio);
                                    cbFavorito.setChecked(true);
                                    CustomToast.mostrarMensagem(getApplicationContext(), "Adicionado aos favoritos", Toast.LENGTH_SHORT);
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

    public void acessarDescricao(){
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{
                // Abre uma transação e adiciona
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, informacoesFragment);
                fragmentTransaction.commit();

                Log.i("TESTE - NAVIGATION", "Informações");
            }
        });
    }

    public void acessarContato(){
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{
                // Abre uma transação e adiciona
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, contatoFragment);
                fragmentTransaction.commit();

                Log.i("TESTE - NAVIGATION", "Contato");
            }
        });
    }

    public void acessarImagens(){
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else {
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, fotosFragment);
                fragmentTransaction.commit();

                Log.i("TESTE - NAVIGATION", "Fotos");
            }
        });
    }

    public void acessarLocalizacao(){
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{
                // Abre uma transação e adiciona
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.nav_host_fragment, localizacaoFragment);
                fragmentTransaction.commit();

                Log.i("TESTE - NAVIGATION", "Localização");
            }
        });
    }
}