package br.ufc.crateus.halugar.Activities.Anunciar;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import br.ufc.crateus.halugar.Activities.main.MainActivity;
import br.ufc.crateus.halugar.Activities.Menu.MenuActivity;
import br.ufc.crateus.halugar.Activities.Sessao.Sessao;

import br.ufc.crateus.halugar.Banco.Banco;
import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.InternetCheck;
import br.ufc.crateus.halugar.Util.OnSwipeTouchListener;
import br.ufc.crateus.halugar.Util.Transition;
import br.ufc.crateus.halugar.Util.Validacao;

public class AnunciarDadosImovelActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    EditText etEndereco, etNumero, etComplemento, etCep, etBairro, etCidade, etEstado, etPrecoAluguel, etQtdVagas, etInformacoesAdicionais;
    TextView labelEndereco, labelNumero, labelComplemento, labelCep, labelBairro, labelCidade, labelEstado, labelPreco, labelVagas, labelInformacoes;

    String endereco="", complemento="", cep="", bairro="", cidade="", estado="", informacoes="";
    int numero=0, vagas=0;
    double preco=0.0;

    Button btnProximoAnunciar;

    ImageButton btnMenuAnunciar;

    Spinner spEstadosAnunciar;

    String[] estadosBrasil = {"", "Acre (AC)", "Alagoas (AL)", "Amapá (AP)", "Amazonas (AM)", "Bahia (BA)",
            "Ceará (CE)", "Distrito Federal (DF)", "Espírito Santo (ES)", "Goiás (GO)",
            "Maranhão (MA)", "Mato Grosso (MT)", "Mato Grosso do Sul (MS)", "Minas Gerais (MG)",
            "Pará (PA)", "Paraíba (PB)", "Paraná (PR)", "Pernambuco (PE)", "Piauí (PI)",
            "Rio de Janeiro (RJ)", "Rio Grande do Norte (RN)", "Rio Grande do Sul (RS)",
            "Rondônia (RO)", "Roraima (RR)", "Santa Catarina (SC)", "São Paulo (SP)",
            "Sergipe (SE)", "Tocantins (TO)"};

    ArrayAdapter<String> aa;

    boolean enderecoInvalido, numeroInvalido, complementoInvalido, cepInvalido, bairroInvalido, cidadeInvalida, estadoInvalido, precoInvalido, vagasInvalida, informacoesInvalida;

    ScrollView scrollView;

    Sessao sessao;
    Banco banco;

    Bundle extras;
    String callerIntent;

    // Caso o usuário pressione onBackPressed:
    String urlImagemPrincipal="", urlImagemDois="", urlImagemTres="", urlImagemQuatro="", urlImagemCinco="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anunciar_dados_imovel);

        sessao = Sessao.getInstance();
        banco = Banco.getInstance();

        if(sessao.getUsuario()==null){

            Sessao.logout();
            startActivity(new Intent(AnunciarDadosImovelActivity.this, MainActivity.class));
            Transition.enterTransition(this);
        }
        else{

            etEndereco = (EditText) findViewById(R.id.etEndereco);
            etNumero = (EditText) findViewById(R.id.etNumero);
            etCep = (EditText) findViewById(R.id.etCep);
            etBairro = (EditText) findViewById(R.id.etBairro);
            etCidade = (EditText)findViewById(R.id.etCidade);
            etEstado = (EditText)findViewById(R.id.etEstado);
            etPrecoAluguel = (EditText) findViewById(R.id.etPrecoAluguel);
            etQtdVagas = (EditText) findViewById(R.id.etQtdVagas);
            etInformacoesAdicionais = (EditText) findViewById(R.id.etInformacoesAdicionais);
            btnProximoAnunciar = (Button) findViewById(R.id.btnProximoAnunciar);
            etComplemento = (EditText) findViewById(R.id.etComplemento);
            btnMenuAnunciar = (ImageButton)findViewById(R.id.btnMenuAnunciarDados);
            spEstadosAnunciar = (Spinner)findViewById(R.id.spEstadoAnunciar);

            labelEndereco = (TextView)findViewById(R.id.labelEndereco);
            labelNumero = (TextView)findViewById(R.id.labelNumero);
            labelComplemento = (TextView)findViewById(R.id.labelComplemento);
            labelCep = (TextView)findViewById(R.id.labelCep);
            labelBairro = (TextView)findViewById(R.id.labelBairro);
            labelCidade = (TextView)findViewById(R.id.labelCidade);
            labelEstado = (TextView)findViewById(R.id.labelEstado);
            labelPreco = (TextView)findViewById(R.id.labelPreco);
            labelVagas = (TextView)findViewById(R.id.labelVagas);
            labelInformacoes = (TextView)findViewById(R.id.labelinfos);

            scrollView = (ScrollView)findViewById(R.id.scrollView3);

            etEndereco.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            etBairro.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
            etCidade.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

            etEndereco.requestFocus();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

            spEstadosAnunciar.setOnItemSelectedListener(this);

            carregarExtras();
            habilitarSwipeMenu();

            btnProximoAnunciar.setOnClickListener(view -> salvarDados());

            btnMenuAnunciar.setOnClickListener(view -> {

                Intent i = new Intent(AnunciarDadosImovelActivity.this, MenuActivity.class);

                i.putExtra("callerIntent", "AnunciarDadosImovelActivity");
                i.putExtra("Endereco", etEndereco.getText().toString());
                i.putExtra("Numero", etNumero.getText().toString());
                i.putExtra("Complemento", etComplemento.getText().toString());
                i.putExtra("CEP", etCep.getText().toString());
                i.putExtra("Bairro", etBairro.getText().toString());
                i.putExtra("Cidade", etCidade.getText().toString());
                i.putExtra("Estado", estado);
                i.putExtra("Preco", etQtdVagas.getText().toString());
                i.putExtra("Vagas", etQtdVagas.getText().toString());
                i.putExtra("Informacoes", etInformacoesAdicionais.getText().toString());
                i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
                i.putExtra("urlImagemDois", urlImagemDois);
                i.putExtra("urlImagemTres", urlImagemTres);
                i.putExtra("urlImagemQuatro", urlImagemQuatro);
                i.putExtra("urlImagemCinco", urlImagemCinco);

                startActivity(i);
                Transition.enterTransition(AnunciarDadosImovelActivity.this);
                finish();
            });
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
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {}

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {}

    @Override
    public void onBackPressed() {

        Intent i = new Intent(AnunciarDadosImovelActivity.this, MainActivity.class);

        removerFotos();

        startActivity(i);
        Transition.backTransition(AnunciarDadosImovelActivity.this);
        finish();
    }

    public void salvarDados(){

        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                enderecoInvalido = numeroInvalido = complementoInvalido = cepInvalido = bairroInvalido = cidadeInvalida =
                estadoInvalido = precoInvalido = vagasInvalida = informacoesInvalida = false;

                if(etEndereco.getText().toString().trim().replaceAll(" +", " ").equals("")){
                    enderecoInvalido = true;
                }
                else
                if(Validacao.validarInput(etEndereco.getText().toString())==false){ // Caso existam emojis
                    enderecoInvalido = true;
                }

                if(etNumero.getText().toString().equals("") || Integer.parseInt(etNumero.getText().toString())<1){
                    numeroInvalido = true;
                }

                if(!etComplemento.getText().toString().trim().replaceAll(" +", " ").equals("") && Validacao.validarInput(etComplemento.getText().toString())==false){ // Caso existam emojis
                    complementoInvalido = true;
                }

                if(!etCep.getText().toString().equals("") && etCep.getText().toString().length()!=8){
                    cepInvalido = true;
                }
                else
                if(Validacao.validarInput(etCep.getText().toString())==false){ // Caso existam emojis
                    cepInvalido = true;
                }

                if(etBairro.getText().toString().trim().replaceAll(" +", " ").equals("")){
                    bairroInvalido = true;
                }
                else
                if(Validacao.validarInput(etBairro.getText().toString())==false){ // Caso existam emojis
                    bairroInvalido = true;
                }

                if(etCidade.getText().toString().trim().replaceAll(" +", " ").equals("")){
                    cidadeInvalida = true;
                }
                else
                if(Validacao.validarInput(etCidade.getText().toString())==false){ // Caso existam emojis
                    cidadeInvalida = true;
                }

                if(estado.equals("")){
                    estadoInvalido = true;
                }

                if(etPrecoAluguel.getText().toString().equals("") || Double.parseDouble(etPrecoAluguel.getText().toString())<=0.00){
                    precoInvalido = true;
                }

                if(etQtdVagas.getText().toString().equals("") || Integer.parseInt(etQtdVagas.getText().toString())<1){
                    vagasInvalida = true;
                }

                if(!etInformacoesAdicionais.getText().toString().trim().replaceAll(" +", " ").equals("") && Validacao.validarInput(etInformacoesAdicionais.getText().toString())==false){ // Caso existam emojis
                    informacoesInvalida = true;
                }

                if(enderecoInvalido || numeroInvalido || complementoInvalido || cepInvalido || bairroInvalido ||
                   cidadeInvalida || estadoInvalido || precoInvalido || vagasInvalida || informacoesInvalida){

                    configurarCamposInvalidosUI();
                    CustomToast.mostrarMensagem(getApplicationContext(), "Preencha os campos corretamente", Toast.LENGTH_SHORT);
                }
                else{

                    configurarCamposInvalidosUI();

                    final Intent i = new Intent(AnunciarDadosImovelActivity.this, AnunciarFotosImovelActivity.class);

                    endereco = etEndereco.getText().toString().trim().replaceAll(" +", " ");
                    numero = Integer.parseInt(etNumero.getText().toString());
                    complemento = etComplemento.getText().toString().trim().replaceAll(" +", " ");
                    cep = etCep.getText().toString();
                    bairro = etBairro.getText().toString().trim().replaceAll(" +", " ");
                    cidade = etCidade.getText().toString().trim().replaceAll(" +", " ");
                    preco = Double.parseDouble(etPrecoAluguel.getText().toString());
                    vagas = Integer.parseInt(etQtdVagas.getText().toString());
                    informacoes = etInformacoesAdicionais.getText().toString().trim().replaceAll(" +", " ");

                    i.putExtra("callerIntent", "AnunciarDadosImovelActivity");
                    i.putExtra("Endereco", endereco.trim().replaceAll(" +", " "));
                    i.putExtra("Numero", numero);
                    i.putExtra("Complemento", complemento.trim().replaceAll(" +", " "));
                    i.putExtra("CEP", cep.trim().replaceAll(" +", " "));
                    i.putExtra("Bairro", bairro.trim().replaceAll(" +", " "));
                    i.putExtra("Cidade", cidade.trim().replaceAll(" +", " "));
                    i.putExtra("Estado", estado);
                    i.putExtra("Preco", preco);
                    i.putExtra("Vagas", vagas);
                    i.putExtra("Informacoes", informacoes.trim().replaceAll(" +", " "));
                    i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
                    i.putExtra("urlImagemDois", urlImagemDois);
                    i.putExtra("urlImagemTres", urlImagemTres);
                    i.putExtra("urlImagemQuatro", urlImagemQuatro);
                    i.putExtra("urlImagemCinco", urlImagemCinco);

                    startActivity(i);
                    Transition.enterTransition(this);
                    finish();
                }
            }
        });
    }

    public void habilitarSwipeMenu(){
        scrollView.setOnTouchListener(new OnSwipeTouchListener(AnunciarDadosImovelActivity.this) {
            public void onSwipeRight() {

                Intent i = new Intent(AnunciarDadosImovelActivity.this, MenuActivity.class);

                i.putExtra("callerIntent", "AnunciarDadosImovelActivity");
                i.putExtra("Endereco", etEndereco.getText().toString());
                i.putExtra("Numero", etNumero.getText().toString());
                i.putExtra("Complemento", etComplemento.getText().toString());
                i.putExtra("CEP", etCep.getText().toString());
                i.putExtra("Bairro", etBairro.getText().toString());
                i.putExtra("Cidade", etCidade.getText().toString());
                i.putExtra("Estado", estado);
                i.putExtra("Preco", etQtdVagas.getText().toString());
                i.putExtra("Vagas", etQtdVagas.getText().toString());
                i.putExtra("Informacoes", etInformacoesAdicionais.getText().toString());
                i.putExtra("urlImagemPrincipal", urlImagemPrincipal);
                i.putExtra("urlImagemDois", urlImagemDois);
                i.putExtra("urlImagemTres", urlImagemTres);
                i.putExtra("urlImagemQuatro", urlImagemQuatro);
                i.putExtra("urlImagemCinco", urlImagemCinco);

                startActivity(i);
                Transition.enterTransition(AnunciarDadosImovelActivity.this);
                finish();
            }
        });
    }

    public void carregarExtras(){

        extras = getIntent().getExtras();

        if(extras!=null){

            callerIntent = extras.getString("callerIntent"); // Main ou Menu

            aa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, estadosBrasil){
                @Override
                public boolean isEnabled(int position){
                    if(position == 0){
                        // Disabilita a primeira posição (hint) = Vazia
                        return false;
                    } else {
                        return true;
                    }
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
            spEstadosAnunciar.setAdapter(aa);

            spEstadosAnunciar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedItemText = (String) parent.getItemAtPosition(position);
                    ((TextView) parent.getChildAt(0)).setTextSize(18);
                    //((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorGray));

                    if(position > 0){
                        estado = selectedItemText;
                        ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            if(callerIntent.equals("MainActivity")){
                // Configurando spinner normalmente:
                spEstadosAnunciar.setSelection(0);
            }

            if(callerIntent.equals("MenuActivity")){

                // Configurar UI:
                etEndereco.setText(extras.getString("Endereco"));
                etNumero.setText(extras.getString("Numero"));
                etComplemento.setText(extras.getString("Complemento"));
                etCep.setText(extras.getString("CEP"));
                etBairro.setText(extras.getString("Bairro"));
                etCidade.setText(extras.getString("Cidade"));
                estado = extras.getString("Estado");
                etPrecoAluguel.setText(extras.getString("Preco"));
                etQtdVagas.setText(extras.getString("Vagas"));
                etInformacoesAdicionais.setText(extras.getString("Informacoes"));

                // Procurar posição do estado selecionado:
                for(int i=0; i<estadosBrasil.length; i++){
                    if(estado.equals(estadosBrasil[i])){
                        spEstadosAnunciar.setSelection(i);
                        break;
                    }
                }
            }

            if(callerIntent.equals("AnunciarFotosImovelActivity")){

                //Configurar UI:
                etEndereco.setText(extras.getString("Endereco"));
                etNumero.setText(String.valueOf(extras.getInt("Numero")));
                etComplemento.setText(extras.getString("Complemento"));
                etCep.setText(extras.getString("CEP"));
                etBairro.setText(extras.getString("Bairro"));
                etCidade.setText(extras.getString("Cidade"));

                estado = extras.getString("Estado");

                // Procurar posição do estado selecionado:
                for(int i=0; i<estadosBrasil.length; i++){
                    if(estado.equals(estadosBrasil[i])){
                        spEstadosAnunciar.setSelection(i);
                        break;
                    }
                }

                String precoStr = String.format("%.2f", extras.getDouble("Preco"));
                etPrecoAluguel.setText(precoStr);

                etQtdVagas.setText(String.valueOf(extras.getInt("Vagas")));
                etInformacoesAdicionais.setText(extras.getString("Informacoes"));

                urlImagemPrincipal = extras.getString("urlImagemPrincipal");
                urlImagemDois = extras.getString("urlImagemDois");
                urlImagemTres = extras.getString("urlImagemTres");
                urlImagemQuatro = extras.getString("urlImagemQuatro");
                urlImagemCinco = extras.getString("urlImagemCinco");
            }
        }
    }

    public void configurarCamposInvalidosUI(){

        if(enderecoInvalido){
            etEndereco.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelEndereco.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etEndereco.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelEndereco.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        if(numeroInvalido){
            etNumero.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelNumero.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etNumero.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelNumero.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        if(complementoInvalido){
            etComplemento.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelComplemento.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etComplemento.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelComplemento.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        if(cepInvalido){
            etCep.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelCep.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etCep.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelCep.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        if(bairroInvalido){
            etBairro.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelBairro.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etBairro.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelBairro.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        if(cidadeInvalida){
            etCidade.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelCidade.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etCidade.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelCidade.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        if(estadoInvalido){
            etEstado.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelEstado.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etEstado.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelEstado.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        if(precoInvalido){
            etPrecoAluguel.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelPreco.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etPrecoAluguel.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelPreco.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        if(vagasInvalida){
            etQtdVagas.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelVagas.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etQtdVagas.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelVagas.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        if(informacoesInvalida){
            etInformacoesAdicionais.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelInformacoes.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etInformacoesAdicionais.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelInformacoes.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }
    }

    public void removerFotos(){

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
}
