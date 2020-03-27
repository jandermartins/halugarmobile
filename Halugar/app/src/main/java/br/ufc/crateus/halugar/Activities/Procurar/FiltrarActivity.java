package br.ufc.crateus.halugar.Activities.Procurar;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import br.ufc.crateus.halugar.Activities.Menu.MenuActivity;
import br.ufc.crateus.halugar.Activities.Sessao.Sessao;
import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.InternetCheck;
import br.ufc.crateus.halugar.Util.OnSwipeTouchListener;
import br.ufc.crateus.halugar.Util.Transition;
import br.ufc.crateus.halugar.Util.Validacao;

public class FiltrarActivity extends AppCompatActivity {

    ImageButton btnMenuVoltarFiltrar, btnEntrarIconeFiltrar;
    Button btnFiltrar, btnLimparFiltros;
    Spinner spEstadoFiltrar;
    EditText etTermoFiltrar, etPrecoMaximoFiltrar, etPrecoMinimoFiltrar, etQuatidadeVagasFiltrar, etRaioDistanciaFiltrar, etBairroFiltrar, etCidadeFiltrar;
    RadioGroup rbOrdenar;
    RadioButton rbCrescente, rbDecrestente;
    TextView labelTermo, labelPrecoMinimo, labelPrecoMaximo, labelBairro, labelCidade, labelVagas, labelDistancia;
    boolean termoInvalido=false, precoMinimoInvalido=false, precoMaximoInvalido=false, intervaloPrecoInvalido=false,
            bairroInvalido=false, cidadeInvalida=false, vagasInvalida=false, distanciaInvalida=false;
    boolean precoMinimoVazio=false, precoMaximoVazio=false, vagasVazio=false, raioVazio=false;

    ScrollView scrollView;

    String ordenacao = "Preço crescente"; // Padrão

    String estadoSelecionado = "", bairro="", cidade="", termoPesquisado="", precoMinimo="0", precoMaximo="0", qtdVagas="0", raioDistancia="0";
    String[] estadosBrasil = {"", "Acre", "Alagoas", "Amapá", "Amazonas", "Bahia",
            "Ceará", "Distrito Federal", "Espírito Santo", "Goiás",
            "Maranhão", "Mato Grosso", "Mato Grosso do Sul", "Minas Gerais",
            "Pará", "Paraíba", "Paraná", "Pernambuco", "Piauí",
            "Rio de Janeiro", "Rio Grande do Norte", "Rio Grande do Sul",
            "Rondônia", "Roraima", "Santa Catarina", "São Paulo",
            "Sergipe", "Tocantins"};

    ArrayAdapter<String> aa;

    final Context context = this;

    boolean flagEditarFiltro=false, procuraRealizada=false;

    long ultimoClique=0;

    Sessao sessao;

    Bundle extras;
    String callerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtrar);

        scrollView = (ScrollView)findViewById(R.id.scrollViewFiltro);

        btnMenuVoltarFiltrar = (ImageButton)findViewById(R.id.btnMenuFiltrar);
        btnEntrarIconeFiltrar = (ImageButton)findViewById(R.id.btnEntrarIconeFiltrar);
        btnFiltrar = (Button) findViewById(R.id.btnFiltrar);
        btnLimparFiltros = (Button)findViewById(R.id.btnLimparFiltros);
        spEstadoFiltrar = (Spinner)findViewById(R.id.spEstadoFiltrar);

        etPrecoMinimoFiltrar = (EditText)findViewById(R.id.etPrecoMinimoFiltrar);
        etPrecoMaximoFiltrar = (EditText)findViewById(R.id.etPrecoMaximoFiltrar);
        etQuatidadeVagasFiltrar = (EditText)findViewById(R.id.etQuatidadeVagasFiltrar);
        etRaioDistanciaFiltrar = (EditText)findViewById(R.id.etRaioDistanciaFiltrar);
        etTermoFiltrar = (EditText)findViewById(R.id.etTermoFiltrar);
        etBairroFiltrar = (EditText)findViewById(R.id.etBairroFiltrar);
        etCidadeFiltrar = (EditText)findViewById(R.id.etCidadeFiltrar);

        labelTermo = (TextView)findViewById(R.id.labelTermo);
        labelPrecoMinimo = (TextView)findViewById(R.id.labelPrecoMinimo);
        labelPrecoMaximo = (TextView)findViewById(R.id.labelPrecoMaximo);
        labelBairro = (TextView)findViewById(R.id.labelBairro);
        labelCidade = (TextView)findViewById(R.id.labelCidade);
        labelVagas = (TextView)findViewById(R.id.labelVagas);
        labelDistancia = (TextView)findViewById(R.id.labelRaio);

        rbOrdenar = (RadioGroup)findViewById(R.id.rbOrdenar);
        rbCrescente = (RadioButton)findViewById(R.id.rbCrescente);
        rbDecrestente = (RadioButton)findViewById(R.id.rbDecrescente);

        etBairroFiltrar.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        etCidadeFiltrar.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        sessao = Sessao.getInstance();

        carregarInformacoes();
        configurarSessaoUI();

        rbOrdenar.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton checkedRadioButton = (RadioButton) findViewById(checkedId);
                ordenacao = checkedRadioButton.getText().toString();
            }
        });

        btnFiltrar.setOnClickListener(view -> filtrar());

        btnMenuVoltarFiltrar.setOnClickListener(view -> exibirMenuVoltar());

        btnLimparFiltros.setOnClickListener(view -> limparFiltros());
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
    public void onBackPressed() {

        Intent i = new Intent(FiltrarActivity.this, ProcurarActivity.class);

        i.putExtra("callerIntent", "FiltrarActivity");

        // Flag para não realizar pesquisa ao exibirMenuVoltar, pois os valores não foram validados:
        i.putExtra("onBackPressedFlag", true);

        i.putExtra("FlagEditarFiltro", flagEditarFiltro);
        i.putExtra("ProcuraRealizada", procuraRealizada);

        // Se a filtragem já aconteceu, enviar apenas os dados anteriores
        if(flagEditarFiltro){

            i.putExtra("TermoPesquisado", termoPesquisado);
            i.putExtra("PrecoMinimo", Double.parseDouble(precoMinimo));
            i.putExtra("PrecoMaximo", Double.parseDouble(precoMaximo));
            i.putExtra("Bairro", bairro);
            i.putExtra("Cidade", cidade);
            i.putExtra("Estado", estadoSelecionado);
            i.putExtra("Vagas", Integer.parseInt(qtdVagas));
            i.putExtra("RaioDistancia", Double.parseDouble(raioDistancia));
            i.putExtra("Ordenacao", ordenacao);
        }
        // Enviando os inputs digitados...
        else{

            i.putExtra("TermoPesquisado", etTermoFiltrar.getText().toString());

            if(etPrecoMinimoFiltrar.getText().toString().equals("")){
                i.putExtra("PrecoMinimo", Double.parseDouble("0"));
            }
            else{
                i.putExtra("PrecoMinimo", Double.parseDouble(etPrecoMinimoFiltrar.getText().toString()));
            }

            if(etPrecoMaximoFiltrar.getText().toString().equals("")){
                i.putExtra("PrecoMaximo", Double.parseDouble("0"));
            }
            else{
                i.putExtra("PrecoMaximo", Double.parseDouble(etPrecoMaximoFiltrar.getText().toString()));
            }

            i.putExtra("Bairro", etBairroFiltrar.getText().toString());
            i.putExtra("Cidade", etCidadeFiltrar.getText().toString());
            i.putExtra("Estado", estadoSelecionado);

            if(etQuatidadeVagasFiltrar.getText().toString().equals("")){
                i.putExtra("Vagas", Integer.parseInt("0"));
            }
            else{
                i.putExtra("Vagas", Integer.parseInt(etQuatidadeVagasFiltrar.getText().toString()));
            }

            if(etRaioDistanciaFiltrar.getText().toString().equals("")){
                i.putExtra("RaioDistancia", Double.parseDouble("0"));
            }
            else{
                i.putExtra("RaioDistancia", Double.parseDouble(etRaioDistanciaFiltrar.getText().toString()));
            }

            i.putExtra("Ordenacao", ordenacao);
        }

        System.out.println("PROCURANDO - Enviando para ProcurarActivity (via onBackPressed)");

        Log.i("PROCURANDO", "FlagEditarFiltro = " + flagEditarFiltro);
        Log.i("PROCURANDO", "Termo = " + termoPesquisado);
        Log.i("PROCURANDO", "Minimo = " + precoMinimo);
        Log.i("PROCURANDO", "Maximo = " + precoMaximo);
        Log.i("PROCURANDO", "Bairro = " + bairro);
        Log.i("PROCURANDO", "Cidade = " + cidade);
        Log.i("PROCURANDO", "Estado = " + estadoSelecionado);
        Log.i("PROCURANDO", "Vagas = " + qtdVagas);
        Log.i("PROCURANDO", "Distancia = " + raioDistancia);
        Log.i("PROCURANDO", "Ordenadao = " + ordenacao);
        Log.i("PROCURANDO", "ProcuraRealizada = " + procuraRealizada);

        startActivity(i);
        Transition.backTransition(FiltrarActivity.this);
        finish();
    }

    public void exibirMenuVoltar(){

        // Usuário tem acesso ao botão VOLTAR:
        if(sessao.getUsuario()==null){

            Intent i = new Intent(FiltrarActivity.this, ProcurarActivity.class);

            i.putExtra("callerIntent", "FiltrarActivity");

            // Flag para não realizar pesquisa ao exibirMenuVoltar, pois os valores não foram validados:
            i.putExtra("onBackPressedFlag", true);
            i.putExtra("ProcuraRealizada", procuraRealizada);

            i.putExtra("FlagEditarFiltro", flagEditarFiltro);
            i.putExtra("TermoPesquisado", etTermoFiltrar.getText().toString());

            if(etPrecoMinimoFiltrar.getText().toString().equals("")){
                i.putExtra("PrecoMinimo", Double.parseDouble("0"));
            }
            else{
                i.putExtra("PrecoMinimo", Double.parseDouble(etPrecoMinimoFiltrar.getText().toString()));
            }

            if(etPrecoMaximoFiltrar.getText().toString().equals("")){
                i.putExtra("PrecoMaximo", Double.parseDouble("0"));
            }
            else{
                i.putExtra("PrecoMaximo", Double.parseDouble(etPrecoMaximoFiltrar.getText().toString()));
            }

            i.putExtra("Bairro", etBairroFiltrar.getText().toString());
            i.putExtra("Cidade", etCidadeFiltrar.getText().toString());
            i.putExtra("Estado", estadoSelecionado);

            if(etQuatidadeVagasFiltrar.getText().toString().equals("")){
                i.putExtra("Vagas", Integer.parseInt("0"));
            }
            else{
                i.putExtra("Vagas", Integer.parseInt(etQuatidadeVagasFiltrar.getText().toString()));
            }

            if(etRaioDistanciaFiltrar.getText().toString().equals("")){
                i.putExtra("RaioDistancia", Double.parseDouble("0"));
            }
            else{
                i.putExtra("RaioDistancia", Double.parseDouble(etRaioDistanciaFiltrar.getText().toString()));
            }

            i.putExtra("Ordenacao", ordenacao);

            System.out.println("PROCURANDO - Enviando para ProcurarActivity");

            Log.i("PROCURANDO", "Termo = " + termoPesquisado);
            Log.i("PROCURANDO", "Minimo = " + precoMinimo);
            Log.i("PROCURANDO", "Maximo = " + precoMaximo);
            Log.i("PROCURANDO", "Bairro = " + bairro);
            Log.i("PROCURANDO", "Cidade = " + cidade);
            Log.i("PROCURANDO", "Estado = " + estadoSelecionado);
            Log.i("PROCURANDO", "Vagas = " + qtdVagas);
            Log.i("PROCURANDO", "Distancia = " + raioDistancia);

            startActivity(i);
            Transition.enterTransition(FiltrarActivity.this);
            finish();
        }
        // Usuário tem acesso ao botão MENU:
        else{

            Intent i = new Intent(FiltrarActivity.this, MenuActivity.class);

            // Para recuperar os anúncios de uma pequisa local ou feita através dos dados de uma filtragem
            i.putExtra("callerIntent", "FiltrarActivity");

            i.putExtra("ProcuraRealizada", procuraRealizada);
            i.putExtra("FlagEditarFiltro", flagEditarFiltro);

            i.putExtra("TermoPesquisado", etTermoFiltrar.getText().toString());
            i.putExtra("PrecoMinimo", etPrecoMinimoFiltrar.getText().toString());
            i.putExtra("PrecoMaximo", etPrecoMaximoFiltrar.getText().toString());
            i.putExtra("Bairro", etBairroFiltrar.getText().toString());
            i.putExtra("Cidade", etCidadeFiltrar.getText().toString());
            i.putExtra("Estado", estadoSelecionado);
            i.putExtra("Vagas", etQuatidadeVagasFiltrar.getText().toString());
            i.putExtra("Distancia", etRaioDistanciaFiltrar.getText().toString());
            i.putExtra("Ordenacao", ordenacao);

            startActivity(i);
            Transition.enterTransition(FiltrarActivity.this);
            finish();
        }
    }

    public void carregarInformacoes(){

        extras = getIntent().getExtras();

        if(extras!=null){

            callerIntent = extras.getString("callerIntent");

            if(callerIntent.equals("MenuActivity")){

                procuraRealizada = extras.getBoolean("ProcuraRealizada");
                flagEditarFiltro = extras.getBoolean("FlagEditarFiltro");

                etTermoFiltrar.setText(extras.getString("TermoPesquisado"));
                etPrecoMinimoFiltrar.setText(extras.getString("PrecoMinimo"));
                etPrecoMaximoFiltrar.setText(extras.getString("PrecoMaximo"));
                etBairroFiltrar.setText(extras.getString("Bairro"));
                etCidadeFiltrar.setText(extras.getString("Cidade"));
                estadoSelecionado = extras.getString("Estado");
                ordenacao = extras.getString("Ordenacao");

                // Configurando spinner do Estado:
                aa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, estadosBrasil){
                    @Override
                    public boolean isEnabled(int position){
                        return true; // Habilita todos os itens do spinner
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        View view = super.getDropDownView(position, convertView, parent);
                        TextView tv = (TextView) view;
                        tv.setTextColor(Color.BLACK);

                        return view;
                    }
                };

                //Creating the ArrayAdapter instance having the country list
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                //Setting the ArrayAdapter data on the Spinner
                spEstadoFiltrar.setAdapter(aa);

                spEstadoFiltrar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedItemText = (String) parent.getItemAtPosition(position);
                        ((TextView) parent.getChildAt(0)).setTextSize(18);
                        //((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorGray));

                        if(position >= 0){
                            estadoSelecionado = selectedItemText;
                            //((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                // Carregando spinner original:
                if(estadoSelecionado.equals("")){

                    spEstadoFiltrar.setSelection(0);
                }
                // Carregar spinner estadoSelecionado no topo...
                else{

                    // Procurar posição do estado selecionado:
                    for(int i=0; i<estadosBrasil.length; i++){
                        if(estadoSelecionado.equals(estadosBrasil[i])){
                            spEstadoFiltrar.setSelection(i);
                            break;
                        }
                    }
                }

                etQuatidadeVagasFiltrar.setText(extras.getString("Vagas"));
                etRaioDistanciaFiltrar.setText(extras.getString("Distancia"));

                if(ordenacao.equals("Preço crescente")){
                    RadioButton b = (RadioButton) findViewById(R.id.rbCrescente);
                    b.setChecked(true);
                }
                else{
                    RadioButton b = (RadioButton) findViewById(R.id.rbDecrescente);
                    b.setChecked(true);
                }
            }

            // Usuário veio da ProcurarActivity para editar os dados de filtragem:
            if(callerIntent.equals("ProcurarActivity")){

                termoPesquisado = extras.getString("TermoPesquisado");

                flagEditarFiltro = extras.getBoolean("FlagEditarFiltro");
                procuraRealizada = extras.getBoolean("ProcuraRealizada");

                aa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, estadosBrasil){
                    @Override
                    public boolean isEnabled(int position){
                        return true; // Todas as opções habilitadas
                    }

                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {
                        View view = super.getDropDownView(position, convertView, parent);
                        TextView tv = (TextView) view;

                        tv.setTextColor(Color.BLACK);

                        return view;
                    }
                };

                //Creating the ArrayAdapter instance having the country list
                //ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, estadosBrasil);
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //Setting the ArrayAdapter data on the Spinner
                spEstadoFiltrar.setAdapter(aa);

                spEstadoFiltrar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String selectedItemText = (String) parent.getItemAtPosition(position);
                        ((TextView) parent.getChildAt(0)).setTextSize(18);

                        if(position >= 0){
                            estadoSelecionado = selectedItemText;
                            //((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                if(flagEditarFiltro){

                    double precoMinimo, precoMaximo, raio;
                    int qtdVagas;
                    String bairro, cidade;

                    precoMinimo = extras.getDouble("PrecoMinimo");
                    precoMaximo = extras.getDouble("PrecoMaximo");
                    bairro = extras.getString("Bairro");
                    cidade = extras.getString("Cidade");
                    estadoSelecionado = extras.getString("Estado");
                    qtdVagas = extras.getInt("Vagas");
                    raio = extras.getDouble("RaioDistancia");
                    ordenacao = extras.getString("Ordenacao");

                    // Configurando UI com os dados recebidos:

                    if(ordenacao.equals("Preço crescente")){
                        RadioButton b = (RadioButton) findViewById(R.id.rbCrescente);
                        b.setChecked(true);
                    }
                    else{
                        RadioButton b = (RadioButton) findViewById(R.id.rbDecrescente);
                        b.setChecked(true);
                    }

                    if(!termoPesquisado.equals("")){
                        etTermoFiltrar.setText(termoPesquisado);
                    }

                    if(!cidade.equals("")){
                        etCidadeFiltrar.setText(cidade);
                    }

                    if(!bairro.equals("")){
                        etBairroFiltrar.setText(bairro);
                    }

                    if(!estadoSelecionado.equals("")){
                        // Procurar posição do estado selecionado:
                        for(int i=0; i<estadosBrasil.length; i++){
                            if(estadoSelecionado.equals(estadosBrasil[i])){
                                spEstadoFiltrar.setSelection(i);
                                break;
                            }
                        }
                    }
                    else{
                        spEstadoFiltrar.setSelection(0);
                    }

                    if(precoMinimo>0){
                        String strPrecoMinimo = String.format("%.2f", precoMinimo);
                        etPrecoMinimoFiltrar.setText(strPrecoMinimo);
                    }

                    if(precoMaximo>0) {
                        String strPrecoMaximo = String.format("%.2f", precoMaximo);
                        etPrecoMaximoFiltrar.setText(strPrecoMaximo);
                    }

                    if(qtdVagas>0) {
                        etQuatidadeVagasFiltrar.setText(String.valueOf(qtdVagas));
                    }

                    if(raio>0) {
                        String strRaio = String.format("%.2f", raio);
                        etRaioDistanciaFiltrar.setText(strRaio);
                    }

                    System.out.println("\n\nPROCURANDO - DADOS RECEBIDOS DE PROCURAR (flagEditarFiltro==true)");

                    Log.i("PROCURANDO", "Termo = " + termoPesquisado);
                    Log.i("PROCURANDO", "Minimo = " + precoMinimo);
                    Log.i("PROCURANDO", "Maximo = " + precoMaximo);
                    Log.i("PROCURANDO", "Bairro = " + bairro);
                    Log.i("PROCURANDO", "Cidade = " + cidade);
                    Log.i("PROCURANDO", "Estado = " + estadoSelecionado);
                    Log.i("PROCURANDO", "Vagas = " + qtdVagas);
                    Log.i("PROCURANDO", "Distancia = " + raioDistancia);
                    Log.i("PROCURANDO", "Ordenadao = " + ordenacao);
                    Log.i("PROCURANDO", "ProcuraRealizada = " + procuraRealizada);

                    System.out.println("\n");

                }
                else{

                    if(!termoPesquisado.equals("")){
                        etTermoFiltrar.setText(termoPesquisado);
                    }

                    System.out.println("\n\nPROCURANDO - DADOS RECEBIDOS DE PROCURAR (flagEditarFiltro==false)");

                    Log.i("PROCURANDO", "Termo = " + termoPesquisado);
                    Log.i("PROCURANDO", "Minimo = " + precoMinimo);
                    Log.i("PROCURANDO", "Maximo = " + precoMaximo);
                    Log.i("PROCURANDO", "Bairro = " + bairro);
                    Log.i("PROCURANDO", "Cidade = " + cidade);
                    Log.i("PROCURANDO", "Estado = " + estadoSelecionado);
                    Log.i("PROCURANDO", "Vagas = " + qtdVagas);
                    Log.i("PROCURANDO", "Distancia = " + raioDistancia);
                    Log.i("PROCURANDO", "Ordenadao = " + ordenacao);
                    Log.i("PROCURANDO", "ProcuraRealizada = " + procuraRealizada);

                    System.out.println("\n");
                }

                if(flagEditarFiltro==false){
                    spEstadoFiltrar.setSelection(0);
                }
            }
        }
    }

    public void filtrar(){

        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                precoMinimo = precoMaximo = qtdVagas = raioDistancia = "";
                termoInvalido = precoMinimoInvalido = precoMaximoInvalido = intervaloPrecoInvalido =
                bairroInvalido = cidadeInvalida = vagasInvalida = distanciaInvalida = false;
                precoMinimoVazio = precoMaximoVazio = vagasVazio = raioVazio = false;

                // Prevenindo clique duplo por um limite de 3 segundos
                if (SystemClock.elapsedRealtime() - ultimoClique < 3000) return;
                ultimoClique = SystemClock.elapsedRealtime();

                // Verificando validade dos campos:
                termoPesquisado = etTermoFiltrar.getText().toString().trim().replaceAll(" +", " ");
                bairro = etBairroFiltrar.getText().toString().trim().replaceAll(" +", " ");
                cidade = etCidadeFiltrar.getText().toString().trim().replaceAll(" +", " ");

                precoMinimo = etPrecoMinimoFiltrar.getText().toString();
                precoMaximo = etPrecoMaximoFiltrar.getText().toString();
                qtdVagas = etQuatidadeVagasFiltrar.getText().toString();
                raioDistancia = etRaioDistanciaFiltrar.getText().toString();

                if(termoPesquisado.equals("") && precoMinimo.equals("") && precoMaximo.equals("") && cidade.equals("") && bairro.equals("") && estadoSelecionado.equals("") && qtdVagas.equals("") && raioDistancia.equals("")){

                    CustomToast.mostrarMensagem(getApplicationContext(), "Preencha algum campo", Toast.LENGTH_SHORT);
                    termoInvalido = precoMinimoInvalido = precoMaximoInvalido = bairroInvalido = cidadeInvalida = vagasInvalida = distanciaInvalida = false;
                    configurarCamposInvalidosUI();
                    return;
                }

                if(precoMinimo.equals("")) precoMinimoVazio=true;
                if(precoMaximo.equals("")) precoMaximoVazio=true;
                if(qtdVagas.equals("")) vagasVazio=true;
                if(raioDistancia.equals("")) raioVazio=true;

                if(!termoPesquisado.equals("") && Validacao.validarInput(termoPesquisado)==false){
                    termoInvalido = true;
                }
                else{
                    termoInvalido = false;
                }

                if(!precoMinimoVazio){

                    if(Double.parseDouble(precoMinimo)<=0){
                        precoMinimoInvalido = true;
                    }
                    else {
                        precoMinimoInvalido = false;
                    }
                }
                else{
                    precoMinimoInvalido = false;
                }

                if(!precoMaximoVazio){

                    if(Double.parseDouble(precoMaximo)<=0){
                        precoMaximoInvalido = true;
                    }
                    else{
                        precoMaximoInvalido = false;
                    }
                }
                else{
                    precoMaximoInvalido = false;
                }

                if(!bairro.equals("")){

                    if(Validacao.validarInput(bairro)==false){ // Caso existam emoijs

                        //dadosInvalidos = true;
                        //msgDadosInvalidos += "\n   - Bairro";
                        bairroInvalido = true;
                    }
                    else{
                        bairroInvalido = false;
                    }
                }

                if(!cidade.equals("")){

                    if(Validacao.validarInput(bairro)==false){ // Caso existam emoijs

                        //dadosInvalidos = true;
                        //msgDadosInvalidos += "\n   - Cidade";
                        cidadeInvalida = true;
                    }
                    else{
                        cidadeInvalida = false;
                    }
                }

                if(!precoMinimoVazio && !precoMaximoVazio){

                    if((Double.parseDouble(precoMinimo)>Double.parseDouble(precoMaximo) && !precoMaximoVazio) ||
                       (Double.parseDouble(precoMinimo)>Double.parseDouble(precoMaximo) && precoMinimoInvalido) ||
                       (Double.parseDouble(precoMinimo)>Double.parseDouble(precoMaximo) && precoMaximoInvalido) ||
                       (precoMinimoInvalido && Double.parseDouble(precoMinimo)<Double.parseDouble(precoMaximo)) ||
                       (precoMaximoInvalido && Double.parseDouble(precoMinimo)<Double.parseDouble(precoMaximo)) ||
                       (precoMinimoInvalido && precoMaximoInvalido)){

                        intervaloPrecoInvalido = true;
                    }
                    else{
                        intervaloPrecoInvalido = false;
                    }
                }

                if(!vagasVazio){

                    if(Integer.parseInt(qtdVagas)<1){
                        vagasInvalida = true;
                    }
                    else{
                        vagasInvalida = false;
                    }
                }
                else{
                    vagasInvalida = false;
                }

                if(!raioVazio){

                    if(Double.parseDouble(raioDistancia)<=0){
                        distanciaInvalida = true;
                    }
                    else{
                        distanciaInvalida = false;
                    }
                }
                else{
                    distanciaInvalida = false;
                }

                if(termoInvalido || precoMinimoInvalido || precoMaximoInvalido || intervaloPrecoInvalido || bairroInvalido || cidadeInvalida || vagasInvalida || distanciaInvalida){

                    CustomToast.mostrarMensagem(getApplicationContext(), "Preencha os campos corretamente", Toast.LENGTH_SHORT);
                    configurarCamposInvalidosUI();
                }
                else{

                    configurarCamposInvalidosUI();

                    Intent i = new Intent(FiltrarActivity.this, ProcurarActivity.class);

                    if(precoMinimoVazio) precoMinimo="0";
                    if(precoMaximoVazio) precoMaximo="0";
                    if(vagasVazio) qtdVagas="0";
                    if(raioVazio) raioDistancia="0";

                    i.putExtra("callerIntent", "FiltrarActivity");

                    // Flag para realizar pesquisa ao exibirMenuVoltar:
                    i.putExtra("onBackPressedFlag", false);
                    i.putExtra("FlagEditarFiltro", true);

                    i.putExtra("TermoPesquisado", termoPesquisado);
                    i.putExtra("PrecoMinimo", Double.parseDouble(precoMinimo));
                    i.putExtra("PrecoMaximo", Double.parseDouble(precoMaximo));
                    i.putExtra("Bairro", bairro);
                    i.putExtra("Cidade", cidade);
                    i.putExtra("Estado", estadoSelecionado);
                    i.putExtra("Vagas", Integer.parseInt(qtdVagas));
                    i.putExtra("RaioDistancia", Double.parseDouble(raioDistancia));
                    i.putExtra("Ordenacao", ordenacao);

                    startActivity(i);
                    Transition.enterTransition(FiltrarActivity.this);
                    finish();
                }
            }
        });
    }

    public void limparFiltros(){
        // Exibir tela para confirmar exclusão:

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(context);
        View limparFiltros = li.inflate(R.layout.limpar_filtro, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(limparFiltros);
        alertDialogBuilder.setTitle("Confirmação");

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Sim",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {

                                RadioButton b = (RadioButton) findViewById(R.id.rbCrescente);
                                b.setChecked(true);

                                etTermoFiltrar.setText("");
                                etBairroFiltrar.setText("");
                                etCidadeFiltrar.setText("");
                                estadoSelecionado = "";
                                etPrecoMinimoFiltrar.setText("");
                                etPrecoMaximoFiltrar.setText("");
                                etQuatidadeVagasFiltrar.setText("");
                                etRaioDistanciaFiltrar.setText("");

                                spEstadoFiltrar.setSelection(0);
                            }
                        })
                .setNegativeButton("Não",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public void configurarSessaoUI(){

        if (sessao.getUsuario()==null) {

            //btnEntrarIconeProcurar.setImageResource(R.mipmap.ic_entrar_foreground);
            btnMenuVoltarFiltrar.setImageResource(R.mipmap.ic_backarrow_foreground);
            btnMenuVoltarFiltrar.getLayoutParams().height=40;
            btnMenuVoltarFiltrar.getLayoutParams().width=40;
            btnEntrarIconeFiltrar.setVisibility(View.INVISIBLE); // Evitar empilhamento das intents (onbackpressed) -> REMOVER BOTÃO
        }
        else{

            btnEntrarIconeFiltrar.setVisibility(View.INVISIBLE);

            scrollView.setOnTouchListener(new OnSwipeTouchListener(FiltrarActivity.this) {
                public void onSwipeRight() {

                    Intent i = new Intent(FiltrarActivity.this, MenuActivity.class);

                    // Para recuperar os anúncios de uma pequisa local ou feita através dos dados de uma filtragem
                    i.putExtra("callerIntent", "FiltrarActivity");

                    i.putExtra("ProcuraRealizada", procuraRealizada);
                    i.putExtra("FlagEditarFiltro", flagEditarFiltro);

                    i.putExtra("TermoPesquisado", etTermoFiltrar.getText().toString());
                    i.putExtra("PrecoMinimo", etPrecoMinimoFiltrar.getText().toString());
                    i.putExtra("PrecoMaximo", etPrecoMaximoFiltrar.getText().toString());
                    i.putExtra("Bairro", etBairroFiltrar.getText().toString());
                    i.putExtra("Cidade", etCidadeFiltrar.getText().toString());
                    i.putExtra("Estado", estadoSelecionado);
                    i.putExtra("Vagas", etQuatidadeVagasFiltrar.getText().toString());
                    i.putExtra("Distancia", etRaioDistanciaFiltrar.getText().toString());
                    i.putExtra("Ordenacao", ordenacao);

                    startActivity(i);
                    Transition.enterTransition(FiltrarActivity.this);
                    finish();
                }
            });
        }
    }

    public void configurarCamposInvalidosUI(){

        if(termoInvalido){
            etTermoFiltrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelTermo.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etTermoFiltrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelTermo.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        if(precoMinimoInvalido){
            etPrecoMinimoFiltrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelPrecoMinimo.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etPrecoMinimoFiltrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelPrecoMinimo.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        if(precoMaximoInvalido){
            etPrecoMaximoFiltrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelPrecoMaximo.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etPrecoMaximoFiltrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelPrecoMaximo.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        if(intervaloPrecoInvalido){
            etPrecoMinimoFiltrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelPrecoMinimo.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            etPrecoMaximoFiltrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelPrecoMaximo.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{

            if(!precoMinimoInvalido){
                etPrecoMinimoFiltrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                labelPrecoMinimo.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            }

            if(!precoMaximoInvalido){
                etPrecoMaximoFiltrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                labelPrecoMaximo.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            }
        }

        if(bairroInvalido){
            etBairroFiltrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelBairro.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etBairroFiltrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelBairro.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        if(cidadeInvalida){
            etCidadeFiltrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelCidade.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etCidadeFiltrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelCidade.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        if(vagasInvalida){
            etQuatidadeVagasFiltrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelVagas.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etQuatidadeVagasFiltrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelVagas.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        if(distanciaInvalida){
            etRaioDistanciaFiltrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelDistancia.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etRaioDistanciaFiltrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelDistancia.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }
    }
}