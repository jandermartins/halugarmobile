package br.ufc.crateus.halugar.Activities.Procurar;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import br.ufc.crateus.halugar.Activities.Menu.MenuActivity;
import br.ufc.crateus.halugar.Activities.Sessao.EntrarActivity;
import br.ufc.crateus.halugar.Activities.Sessao.Sessao;
import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.InternetCheck;
import br.ufc.crateus.halugar.Util.Transition;

public class ProximidadesActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    double[] latitudes, longitudes, precos;
    double latitudeUsuario, longitudeUsuario;

    ImageButton btnMenuVoltarProximidades, btnEntrarIconeProximidades;

    // Extras de PesquisarActivity:
    String termoPesquisado="", bairro="", cidade="", estado="", ordenacao="";
    double precoMinimo, precoMaximo, raioDistancia;
    int vagas;
    boolean resultadoFiltragem, onBackPressedFlag;

    Sessao sessao;
    Bundle extras;
    String callerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proximidades);

        btnMenuVoltarProximidades = (ImageButton)findViewById(R.id.btnVoltarProximidades);
        btnEntrarIconeProximidades = (ImageButton)findViewById(R.id.btnEntrarIconeProximidades);

        sessao = Sessao.getInstance();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapProximidades);
        mapFragment.getMapAsync(this);

        configurarSessaoUI();
        carregarInformacoes();

        btnMenuVoltarProximidades.setOnClickListener(view -> exibirMenuVoltar());

        // REMOVER:
        btnEntrarIconeProximidades.setOnClickListener(view -> {
            startActivity(new Intent(ProximidadesActivity.this, EntrarActivity.class));
            Transition.enterTransition(ProximidadesActivity.this);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        new InternetCheck(internet -> {
            if (!internet) {
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
        });
    }

    @Override
    public void onBackPressed() {

        if(callerIntent.equals("ProcurarActivity") || callerIntent.equals("MenuActivity")){

            Intent i = new Intent(ProximidadesActivity.this, ProcurarActivity.class);

            i.putExtra("callerIntent", "ProximidadesActivity");

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
            i.putExtra("Vagas", vagas);
            i.putExtra("RaioDistancia", raioDistancia);
            i.putExtra("Ordenacao", ordenacao);

            startActivity(i);
            Transition.backTransition(ProximidadesActivity.this);
            finish();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng usuario = new LatLng(latitudeUsuario, longitudeUsuario);

        if(latitudes!=null && longitudes!=null){
            for (int i=0; i<latitudes.length; i++){

                LatLng local = new LatLng(latitudes[i], longitudes[i]);

                String preco = String.format("%.2f", precos[i]);

                mMap.addMarker(new MarkerOptions()
                        .position(local)
                        .title("HáLugar")
                        .snippet("R$ " + preco)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_foreground))
                );
            }
        }

        mMap.addMarker(new MarkerOptions().
                position(usuario).
                title("Você está aqui")
                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_aqui_foreground)))
                .showInfoWindow();

        CameraPosition cameraPosition = new CameraPosition.Builder().target(usuario).zoom(13).build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    public void configurarSessaoUI(){

        if(sessao.getUsuario()!=null){

            btnEntrarIconeProximidades.setVisibility(View.INVISIBLE);
        }
        else{

            btnEntrarIconeProximidades.setVisibility(View.INVISIBLE);
            btnMenuVoltarProximidades.setImageResource(R.mipmap.ic_backarrow_foreground);
            btnMenuVoltarProximidades.getLayoutParams().height = 40;
            btnMenuVoltarProximidades.getLayoutParams().width = 40;
        }
    }

    public void carregarInformacoes(){

        extras = getIntent().getExtras();

        if(extras!=null){

            callerIntent = extras.getString("callerIntent");

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
            vagas = extras.getInt("Vagas");
            raioDistancia = extras.getDouble("RaioDistancia");
            ordenacao = extras.getString("Ordenacao");
        }
    }

    public void exibirMenuVoltar(){

        // Usuário tem acesso ao botão VOLTAR:
        if(sessao.getUsuario()==null){

            Intent i = new Intent(ProximidadesActivity.this, ProcurarActivity.class);

            i.putExtra("callerIntent", "ProximidadesActivity");

            i.putExtra("onBackPressedFlag", onBackPressedFlag);
            i.putExtra("FlagEditarFiltro", resultadoFiltragem);

            i.putExtra("TermoPesquisado", termoPesquisado);
            i.putExtra("PrecoMinimo", precoMinimo);
            i.putExtra("PrecoMaximo", precoMaximo);
            i.putExtra("Bairro", bairro);
            i.putExtra("Cidade", cidade);
            i.putExtra("Estado", estado);
            i.putExtra("Vagas", vagas);
            i.putExtra("RaioDistancia", raioDistancia);
            i.putExtra("Ordenacao", ordenacao);

            startActivity(i);
            Transition.backTransition(ProximidadesActivity.this);
            finish();
        }
        // Usuário tem acesso ao botão MENU
        else{

            if(callerIntent.equals("ProcurarActivity") || callerIntent.equals("MenuActivity")){

                Intent i = new Intent(ProximidadesActivity.this, MenuActivity.class);

                i.putExtra("callerIntent", "ProximidadesActivity");

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
                i.putExtra("Vagas", vagas);
                i.putExtra("RaioDistancia", raioDistancia);
                i.putExtra("Ordenacao", ordenacao);

                i.putExtra("FlagEditarFiltro", resultadoFiltragem);

                startActivity(i);
                Transition.enterTransition(ProximidadesActivity.this);
                finish();
            }
        }

    }
}
