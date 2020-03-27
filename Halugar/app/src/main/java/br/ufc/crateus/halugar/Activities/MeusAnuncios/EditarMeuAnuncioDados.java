package br.ufc.crateus.halugar.Activities.MeusAnuncios;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import br.ufc.crateus.halugar.Activities.Menu.MenuActivity;
import br.ufc.crateus.halugar.Activities.Sessao.Sessao;
import br.ufc.crateus.halugar.Banco.Banco;
import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.Formatacao;
import br.ufc.crateus.halugar.Util.InternetCheck;
import br.ufc.crateus.halugar.Util.OnSwipeTouchListener;
import br.ufc.crateus.halugar.Util.Transition;
import br.ufc.crateus.halugar.Util.Validacao;

public class EditarMeuAnuncioDados extends AppCompatActivity  implements AdapterView.OnItemSelectedListener {

    EditText etEndereco, etNumero, etComplemento, etCep, etBairro, etCidade, etEstado, etPrecoAluguel, etQtdVagas, etInformacoesAdicionais;
    ImageButton btnMenuEditarDados, btnExcluirAnuncio;
    Button btnAtualizarAnuncio;
    TextView labelEndereco, labelNumero, labelComplemento, labelCep, labelBairro, labelCidade, labelEstado, labelPreco, labelVagas, labelInformacoes;

    boolean enderecoInvalido, numeroInvalido, complementoInvalido, cepInvalido, bairroInvalido, cidadeInvalida, estadoInvalido, precoInvalido, vagasInvalida, informacoesInvalida;

    String enderecoAtual="", cepAtual="", bairroAtual="", cidadeAtual="", estadoAtual="", complementoAtual="", informacoesAtual="";
    int vagasAtual, vagasNovo, numeroAtual, numeroNovo;
    double precoAtual, precoNovo;

    String novoEndereco, novoNumero, novoBairro, novoComplemento, novoCep, novoCidade, novoEstado, novoPreco, novoVagas, novoInfos;

    final Context context = this;
    ScrollView scrollView;
    ConstraintLayout pbEditarDadosAnuncioLayout, pbEditarDadosAnuncioLayoutRemover;

    Spinner spEstadosAnunciar;
    String estadoSelecionado ="";
    boolean atualizado=false, novaLocalizacao=false;
    String anuncioId;
    String[] estadosBrasil = {"Acre (AC)", "Alagoas (AL)", "Amapá (AP)", "Amazonas (AM)", "Bahia (BA)",
            "Ceará (CE)", "Distrito Federal (DF)", "Espírito Santo (ES)", "Goiás (GO)",
            "Maranhão (MA)", "Mato Grosso (MT)", "Mato Grosso do Sul (MS)", "Minas Gerais (MG)",
            "Pará (PA)", "Paraíba (PB)", "Paraná (PR)", "Pernambuco (PE)", "Piauí (PI)",
            "Rio de Janeiro (RJ)", "Rio Grande do Norte (RN)", "Rio Grande do Sul (RS)",
            "Rondônia (RO)", "Roraima (RR)", "Santa Catarina (SC)", "São Paulo (SP)",
            "Sergipe (SE)", "Tocantins (TO)"};

    boolean localizacaoEncontrada = false;
    String latitudeAnuncio, longitudeAnuncio;
    double latitude=0, longitude=0;
    String enderecoPesquisa = "";

    long ultimoClique = 0;

    InputStream is;
    String input;

    Banco banco;
    Sessao sessao;

    ArrayAdapter<String> aa;
    int posicaoEstado =0;
    boolean estadoAtualizado=false;

    Bundle extras;
    String callerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_meu_anuncio_dados);

        scrollView = (ScrollView)findViewById(R.id.scrollViewEditarAnuncio);

        etEndereco = (EditText) findViewById(R.id.etEnderecoEditar);
        etNumero = (EditText) findViewById(R.id.etNumeroEditar);
        etComplemento = (EditText) findViewById(R.id.etComplementoEditar);
        etCep = (EditText) findViewById(R.id.etCepEditar);
        etBairro = (EditText) findViewById(R.id.etBairroEditar);
        etCidade = (EditText)findViewById(R.id.etCidadeEditar);
        etEstado = (EditText)findViewById(R.id.etEstadoEditar);
        etPrecoAluguel = (EditText) findViewById(R.id.etPrecoAluguelEditar);
        etQtdVagas = (EditText) findViewById(R.id.etQtdVagasEditar);
        etInformacoesAdicionais = (EditText) findViewById(R.id.etInformacoesAdicionaisEditar);
        spEstadosAnunciar = (Spinner)findViewById(R.id.spEstadoEditar);

        labelEndereco = (TextView)findViewById(R.id.labelEnderecoEdit);
        labelNumero = (TextView)findViewById(R.id.labelNumeroEdit);
        labelComplemento = (TextView)findViewById(R.id.labelComplementoEdit);
        labelCep = (TextView)findViewById(R.id.labelCepEdit);
        labelBairro = (TextView)findViewById(R.id.labelBairroEdit);
        labelCidade = (TextView)findViewById(R.id.labelCidadeEdit);
        labelEstado = (TextView)findViewById(R.id.labelEstadoEdit);
        labelPreco = (TextView)findViewById(R.id.labelPrecoEdit);
        labelVagas = (TextView)findViewById(R.id.labelVagasEdit);
        labelInformacoes = (TextView)findViewById(R.id.labelInfoEdit);

        pbEditarDadosAnuncioLayout = (ConstraintLayout)findViewById(R.id.pbEditarDadosAnuncioLayout);
        pbEditarDadosAnuncioLayoutRemover = (ConstraintLayout)findViewById(R.id.pbEditarDadosAnuncioLayoutRemover);

        btnAtualizarAnuncio = (Button) findViewById(R.id.btnAtualizarAnuncio);
        btnMenuEditarDados = (ImageButton)findViewById(R.id.btnMenuEditarDados);
        btnExcluirAnuncio = (ImageButton)findViewById(R.id.btnExcluirAnuncio);

        etEndereco.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        etBairro.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        etCidade.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);

        banco = Banco.getInstance();
        sessao = Sessao.getInstance();

        spEstadosAnunciar.setOnItemSelectedListener(this);

        carregarInformacoes();
        habilitarSwipeMenu();

        btnAtualizarAnuncio.setOnClickListener(view -> atualizarInformacoes());

        btnMenuEditarDados.setOnClickListener(view -> {

            Intent i = new Intent(EditarMeuAnuncioDados.this, MenuActivity.class);

            i.putExtra("callerIntent", "EditarMeuAnuncioDados");

            i.putExtra("idAnuncio", anuncioId);

            if(etEndereco.getText().toString().equals("")){
                i.putExtra("EnderecoAtual", enderecoAtual);
                i.putExtra("EnderecoNovo", "");
            }
            else{
                i.putExtra("EnderecoAtual", enderecoAtual);
                i.putExtra("EnderecoNovo", etEndereco.getText().toString());
            }

            if(etNumero.getText().toString().equals("")){
                i.putExtra("NumeroAtual", String.valueOf(numeroAtual));
                i.putExtra("NumeroNovo", "0");
            }
            else{
                i.putExtra("NumeroAtual", String.valueOf(numeroAtual));
                i.putExtra("NumeroNovo", etNumero.getText().toString());
            }

            if(etComplemento.getText().toString().equals("")){
                i.putExtra("ComplementoAtual", complementoAtual);
                i.putExtra("ComplementoNovo", "");
            }
            else{
                i.putExtra("ComplementoAtual", complementoAtual);
                i.putExtra("ComplementoNovo", etComplemento.getText().toString());
            }

            if(etCep.getText().toString().equals("")){
                i.putExtra("CepAtual", cepAtual);
                i.putExtra("CepNovo", "");
            }
            else{
                i.putExtra("CepAtual", cepAtual);
                i.putExtra("CepNovo", etCep.getText().toString());
            }

            if(etBairro.getText().toString().equals("")){
                i.putExtra("BairroAtual", bairroAtual);
                i.putExtra("BairroNovo", "");
            }
            else{
                i.putExtra("BairroAtual", bairroAtual);
                i.putExtra("BairroNovo", etBairro.getText().toString());
            }

            if(etCidade.getText().toString().equals("")){
                i.putExtra("CidadeAtual", cidadeAtual);
                i.putExtra("CidadeNovo", "");
            }
            else{
                i.putExtra("CidadeAtual", cidadeAtual);
                i.putExtra("CidadeNovo", etCidade.getText().toString());
            }

            i.putExtra("EstadoAtual", estadoAtual);
            i.putExtra("EstadoSelecionado", estadoSelecionado);

            if(etPrecoAluguel.getText().toString().equals("")){
                i.putExtra("PrecoAtual", String.valueOf(precoAtual));
                i.putExtra("PrecoNovo", "0");
            }
            else{
                i.putExtra("PrecoAtual", String.valueOf(precoAtual));
                i.putExtra("PrecoNovo", etPrecoAluguel.getText().toString());
            }

            if(etQtdVagas.getText().toString().equals("")){
                i.putExtra("VagasAtual", String.valueOf(vagasAtual));
                i.putExtra("VagasNovo", "0");
            }
            else{
                i.putExtra("VagasAtual", String.valueOf(vagasAtual));
                i.putExtra("VagasNovo", etQtdVagas.getText().toString());
            }

            if(etInformacoesAdicionais.getText().toString().equals("")){
                i.putExtra("InformacoesAtual", informacoesAtual);
                i.putExtra("InformacoesNovo", "");
            }
            else{
                i.putExtra("InformacoesAtual", informacoesAtual);
                i.putExtra("InformacoesNovo", etInformacoesAdicionais.getText().toString());
            }

            i.putExtra("EstadoAtualizado", estadoAtualizado);

            startActivity(i);
            Transition.enterTransition(EditarMeuAnuncioDados.this);
            finish();
        });

        btnExcluirAnuncio.setOnClickListener(view -> removerAnuncio());
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
    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    @Override
    public void onBackPressed() {

        if(callerIntent.equals("MeuAnuncioDados")){

            Intent i = new Intent(EditarMeuAnuncioDados.this, MeuAnuncioDados.class);

            i.putExtra("callerIntent", "EditarMeuAnuncioDados");

            i.putExtra("anuncioId", anuncioId);

            if(complementoAtual.equals("")){
                complementoAtual = "Não informado";
            }

            if(cepAtual.equals("")){
                cepAtual = "Não informado";
            }

            if(informacoesAtual.equals("")){
                informacoesAtual = "Nenhuma";
            }

            i.putExtra("idAnuncio", anuncioId);

            i.putExtra("Endereco", enderecoAtual);
            i.putExtra("Numero", numeroAtual);
            i.putExtra("Complemento", complementoAtual); // Não obrigatório
            i.putExtra("CEP", cepAtual); // Não obrigatório
            i.putExtra("Bairro", bairroAtual);
            i.putExtra("Cidade", cidadeAtual);
            i.putExtra("Estado", estadoAtual);
            i.putExtra("Preco", precoAtual);
            i.putExtra("Vagas", vagasAtual);
            i.putExtra("Informacoes", informacoesAtual); // Não obrigatório

            startActivity(i);
            Transition.backTransition(EditarMeuAnuncioDados.this);
            finish();
        }
    }

    public class Async extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            try {
                URL data = new URL(strings[0]);
                HttpURLConnection conn = (HttpURLConnection) data.openConnection();
                conn.setDoInput(true);
                conn.connect();
                is = conn.getInputStream();
                input = convertInputStreamToString(is);

                if(input.equals("[]")){
                    localizacaoEncontrada = false;
                }

                Log.i("LOCALIZACAO (async)", "Lat: " + latitudeAnuncio + " & Lon: " + longitudeAnuncio);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return input;
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);

            JSONArray jsonArray = null;

            try {
                jsonArray = new JSONArray(string);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                String str = jsonArray.getJSONObject(0).toString();
                latitudeAnuncio = jsonArray.getJSONObject(0).getString("lat");
                longitudeAnuncio = jsonArray.getJSONObject(0).getString("lon");

                // FAZER VERIFICAÇÂO DO OBJETO JSON (nulo)

                Log.i("LOCALIZACAO (retorno)", "Lat: " + latitudeAnuncio + " & Lon: " + longitudeAnuncio);
                Log.i("LOCALIZACAO (json)", str);

                if(latitudeAnuncio==null || longitudeAnuncio==null){
                    localizacaoEncontrada = false;

                } else {

                    if (latitudeAnuncio!=null && longitudeAnuncio!=null) {
                        banco.setLatitude(anuncioId, latitudeAnuncio);
                        banco.setLongitude(anuncioId, longitudeAnuncio);

                        localizacaoEncontrada = true;

                        Log.i("AQUI - NO Async", "LAT: " + latitude + " | LON: " + longitude);
                    }
                    else{
                        banco.setLatitude(anuncioId, "0");
                        banco.setLongitude(anuncioId, "0");
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {

        BufferedReader bufferedReader;
        bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";

        while ((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        inputStream.close();

        if(result.equals("[]")){
            localizacaoEncontrada = false;
        }

        return result;
    }

    public void habilitarSwipeMenu(){

        scrollView.setOnTouchListener(new OnSwipeTouchListener(EditarMeuAnuncioDados.this) {
            public void onSwipeRight() {

                Intent i = new Intent(EditarMeuAnuncioDados.this, MenuActivity.class);

                i.putExtra("callerIntent", "EditarMeuAnuncioDados");

                i.putExtra("idAnuncio", anuncioId);

                if(etEndereco.getText().toString().equals("")){
                    i.putExtra("EnderecoAtual", enderecoAtual);
                    i.putExtra("EnderecoNovo", "");
                }
                else{
                    i.putExtra("EnderecoAtual", enderecoAtual);
                    i.putExtra("EnderecoNovo", etEndereco.getText().toString());
                }

                if(etNumero.getText().toString().equals("")){
                    i.putExtra("NumeroAtual", String.valueOf(numeroAtual));
                    i.putExtra("NumeroNovo", "0");
                }
                else{
                    i.putExtra("NumeroAtual", String.valueOf(numeroAtual));
                    i.putExtra("NumeroNovo", etNumero.getText().toString());
                }

                if(etComplemento.getText().toString().equals("")){
                    i.putExtra("ComplementoAtual", complementoAtual);
                    i.putExtra("ComplementoNovo", "");
                }
                else{
                    i.putExtra("ComplementoAtual", complementoAtual);
                    i.putExtra("ComplementoNovo", etComplemento.getText().toString());
                }

                if(etCep.getText().toString().equals("")){
                    i.putExtra("CepAtual", cepAtual);
                    i.putExtra("CepNovo", "");
                }
                else{
                    i.putExtra("CepAtual", cepAtual);
                    i.putExtra("CepNovo", etCep.getText().toString());
                }

                if(etBairro.getText().toString().equals("")){
                    i.putExtra("BairroAtual", bairroAtual);
                    i.putExtra("BairroNovo", "");
                }
                else{
                    i.putExtra("BairroAtual", bairroAtual);
                    i.putExtra("BairroNovo", etBairro.getText().toString());
                }

                if(etCidade.getText().toString().equals("")){
                    i.putExtra("CidadeAtual", cidadeAtual);
                    i.putExtra("CidadeNovo", "");
                }
                else{
                    i.putExtra("CidadeAtual", cidadeAtual);
                    i.putExtra("CidadeNovo", etCidade.getText().toString());
                }

                i.putExtra("EstadoAtual", estadoAtual);
                i.putExtra("EstadoSelecionado", estadoSelecionado);

                if(etPrecoAluguel.getText().toString().equals("")){
                    i.putExtra("PrecoAtual", String.valueOf(precoAtual));
                    i.putExtra("PrecoNovo", "0");
                }
                else{
                    i.putExtra("PrecoAtual", String.valueOf(precoAtual));
                    i.putExtra("PrecoNovo", etPrecoAluguel.getText().toString());
                }

                if(etQtdVagas.getText().toString().equals("")){
                    i.putExtra("VagasAtual", String.valueOf(vagasAtual));
                    i.putExtra("VagasNovo", "0");
                }
                else{
                    i.putExtra("VagasAtual", String.valueOf(vagasAtual));
                    i.putExtra("VagasNovo", etQtdVagas.getText().toString());
                }

                if(etInformacoesAdicionais.getText().toString().equals("")){
                    i.putExtra("InformacoesAtual", informacoesAtual);
                    i.putExtra("InformacoesNovo", "");
                }
                else{
                    i.putExtra("InformacoesAtual", informacoesAtual);
                    i.putExtra("InformacoesNovo", etInformacoesAdicionais.getText().toString());
                }

                i.putExtra("EstadoAtualizado", estadoAtualizado);

                startActivity(i);
                Transition.enterTransition(EditarMeuAnuncioDados.this);
                finish();
            }
        });
    }

    public void carregarInformacoes(){

        extras = getIntent().getExtras();

        if(extras!=null){

            callerIntent = extras.getString("callerIntent");

            // Se o usuário vier de MeuAnuncioDados, carregar spinner normal e preencher informações...
            if(extras.getString("callerIntent").equals("MeuAnuncioDados")){

                anuncioId = extras.getString("idAnuncio");

                enderecoAtual = extras.getString("Endereco");
                numeroAtual = extras.getInt("Numero");
                complementoAtual = extras.getString("Complemento");
                cepAtual = extras.getString("CEP");
                bairroAtual = extras.getString("Bairro");
                cidadeAtual = extras.getString("Cidade");
                estadoAtual = extras.getString("Estado");
                precoAtual = extras.getDouble("Preco");
                vagasAtual = extras.getInt("Vagas");
                informacoesAtual = extras.getString("Informacoes");

                // Configurando UI com os dados recebidos:
                etEndereco.setHint(enderecoAtual);
                etNumero.setHint(String.valueOf(numeroAtual));

                etComplemento.setText(complementoAtual);

                if(!cepAtual.equals("")){

                    if(!cepAtual.equals("Não informado")){
                        etCep.setText(cepAtual);
                    }
                    else{
                        etCep.setHint(cepAtual);
                    }
                }

                etCidade.setHint(cidadeAtual);
                etBairro.setHint(bairroAtual);

                String strDouble = String.format("%.2f", precoAtual);
                etPrecoAluguel.setHint("R$ " + strDouble);

                etQtdVagas.setHint(String.valueOf(vagasAtual));

                etInformacoesAdicionais.setText(informacoesAtual);

                // Procurar estado atual para atualizar spinner:
                for(int i=0; i<estadosBrasil.length; i++){
                    if(estadoAtual.equals(estadosBrasil[i])){
                        posicaoEstado = i;
                        break;
                    }
                }

                aa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, estadosBrasil){
                    @Override
                    public boolean isEnabled(int position){
                        return true; // Todos os itens habilitados
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
                spEstadosAnunciar.setAdapter(aa);

                spEstadosAnunciar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        String selectedItemText = (String) parent.getItemAtPosition(position);
                        ((TextView) parent.getChildAt(0)).setTextSize(18);

                        if(position==posicaoEstado){
                            estadoSelecionado = selectedItemText;
                            ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorDisabled));
                            estadoAtualizado = false;
                        }
                        else{
                            estadoSelecionado = selectedItemText;
                            ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                            estadoAtualizado = true;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                spEstadosAnunciar.setSelection(posicaoEstado);
            }

            if(callerIntent.equals("MenuActivity")){

                anuncioId = extras.getString("idAnuncio");

                enderecoAtual = extras.getString("EnderecoAtual");
                numeroAtual = extras.getInt("NumeroAtual");
                complementoAtual = extras.getString("ComplementoAtual");
                cepAtual = extras.getString("CepAtual");
                bairroAtual = extras.getString("BairroAtual");
                cidadeAtual = extras.getString("CidadeAtual");
                estadoAtual = extras.getString("EstadoAtual");
                estadoSelecionado = extras.getString("EstadoSelecionado");
                precoAtual = extras.getDouble("PrecoAtual");
                vagasAtual = extras.getInt("VagasAtual");
                informacoesAtual = extras.getString("InformacoesAtual");

                novoEndereco = extras.getString("EnderecoNovo");
                numeroNovo = extras.getInt("NumeroNovo");
                novoComplemento = extras.getString("ComplementoNovo");
                novoCep = extras.getString("CepNovo");
                novoBairro = extras.getString("BairroNovo");
                novoCidade = extras.getString("CidadeNovo");
                precoNovo = extras.getDouble("PrecoNovo");
                vagasNovo = extras.getInt("VagasNovo");
                novoInfos = extras.getString("InformacoesNovo");
                estadoAtualizado = extras.getBoolean("EstadoAtualizado");


                // Configurando UI (text e hint) com os dados recebidos:

                if(novoEndereco.equals("")){
                    etEndereco.setHint(enderecoAtual);
                }
                else{
                    etEndereco.setHint(enderecoAtual);
                    etEndereco.setText(novoEndereco);
                }

                if(numeroNovo==0){
                    etNumero.setHint(String.valueOf(numeroAtual));
                }
                else{
                    etNumero.setHint(String.valueOf(numeroAtual));
                    etNumero.setText(String.valueOf(numeroNovo));
                }

                if(novoComplemento.equals("")){
                    etComplemento.setText(complementoAtual);
                }
                else{
                    //etComplemento.setHint(complementoAtual);
                    etComplemento.setText(novoComplemento);
                }

                if(novoCep.equals("")){

                    if(cepAtual.equals("Não informado")){
                        etCep.setHint(cepAtual);
                    }
                    else{
                        etCep.setText(cepAtual);
                    }
                }
                else{

                    if(cepAtual.equals("Não informado")){
                        etCep.setHint(cepAtual);
                    }
                    else{
                        //etCep.setText(cepAtual);
                        etCep.setText(novoCep);

                    }

                    //etCep.setText(novoCep);
                }

                if(novoBairro.equals("")){
                    etBairro.setHint(bairroAtual);
                }
                else{
                    etBairro.setHint(bairroAtual);
                    etBairro.setText(novoBairro);
                }

                if(novoCidade.equals("")){
                    etCidade.setHint(cidadeAtual);
                }
                else {
                    etCidade.setHint(cidadeAtual);
                    etCidade.setText(novoCidade);
                }

                // Procurar posicao do estado atual para atualizar spinner:
                if(estadoSelecionado.equals("")){
                    for(int i=0; i<estadosBrasil.length; i++){
                        if(estadoAtual.equals(estadosBrasil[i])){
                            posicaoEstado = i;
                            break;
                        }
                    }
                }
                // Procurar estado novo para atualizar spinner:
                else{
                    for(int i=0; i<estadosBrasil.length; i++){
                        if(estadoSelecionado.equals(estadosBrasil[i])){
                            posicaoEstado = i;
                            estadoAtualizado = true;
                            break;
                        }
                    }
                }

                aa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, estadosBrasil){
                    @Override
                    public boolean isEnabled(int position){
                        return true; // Todos os itens habilitados
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
                spEstadosAnunciar.setAdapter(aa);

                spEstadosAnunciar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        String selectedItemText = (String) parent.getItemAtPosition(position);
                        ((TextView) parent.getChildAt(0)).setTextSize(18);

                        if(position==posicaoEstado){

                            estadoSelecionado = selectedItemText;

                            if(estadoAtual.equals(estadoSelecionado)){
                                ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorDisabled));
                                estadoAtualizado = false;
                            }
                            else {
                                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                                estadoAtualizado = true;
                            }
                        }
                        else{

                            estadoSelecionado = selectedItemText;

                            if(estadoAtual.equals(estadoSelecionado)){
                                ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorDisabled));
                                estadoAtualizado = false;
                            }
                            else {
                                ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                                estadoAtualizado = true;
                            }

                            //((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                            //estadoAtualizado = true;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                spEstadosAnunciar.setSelection(posicaoEstado);

                if(precoNovo==0){
                    String strDouble = String.format("%.2f", precoAtual);
                    etPrecoAluguel.setHint("R$ " + strDouble);
                }
                else{

                    String strDouble = String.format("%.2f", precoAtual);
                    etPrecoAluguel.setHint("R$ " + strDouble);

                    strDouble = String.format("%.2f", precoNovo);
                    etPrecoAluguel.setText(strDouble);
                }

                if(vagasNovo==0){
                    etQtdVagas.setHint(String.valueOf(vagasAtual));
                }
                else{
                    etQtdVagas.setHint(String.valueOf(vagasAtual));
                    etQtdVagas.setText(String.valueOf(vagasNovo));
                }

                if(novoInfos.equals("")){
                    etInformacoesAdicionais.setText(informacoesAtual);
                }
                else{
                    //etInformacoesAdicionais.setHint(informacoesAtual);
                    etInformacoesAdicionais.setText(novoInfos);
                }
            }

            // Se alterar o valor do spinner e acessar o menu, enviar estadoSelecionado selecionado e montar novo spinner com estadoSelecionado do topo:
            // Preencher informações novamente (text e hint)...
            /*
            else{

                estadoSelecionado = extras.getString("EstadoSelecionado");

                int j=1;

                if(estadoSelecionado==null){

                    estados[0] = extras.getString("EstadoAtual");

                    for(int k=0; k<estadosBrasil.length; k++){
                        if(!estadosBrasil[k].equals(estados[0])){
                            estados[j++] = estadosBrasil[k];
                        }
                    }
                }
                else{

                    estados[0] = extras.getString("EstadoSelecionado");

                    novoEstadoSelecionado = true;

                    for(int k=0; k<estadosBrasil.length; k++){
                        if(!estadosBrasil[k].equals(estadoSelecionado)){
                            estados[j++] = estadosBrasil[k];
                        }
                    }
                }

                etEndereco.setHint(enderecoAtual);
                etNumero.setHint(String.valueOf(numeroAtual));
                etComplemento.setHint(complementoAtual);

                if(!cepAtual.equals("")){

                    if(!cepAtual.equals("Não informado")){
                        etCep.setHint(Formatacao.formatarCep(cepAtual));
                    }
                    else{
                        etCep.setHint(cepAtual);
                    }
                }

                etCidade.setHint(cidadeAtual);
                etBairro.setHint(bairroAtual);

                String strDouble = String.format("%.2f", precoAtual);
                etPrecoAluguel.setHint("R$ " + strDouble);

                etQtdVagas.setHint(String.valueOf(vagasAtual));

                etInformacoesAdicionais.setHint(informacoesAtual);

                novaLocalizacao = false;

                aa = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, estados){

                    @Override
                    public View getDropDownView(int position, View convertView, ViewGroup parent) {

                        View view = super.getDropDownView(position, convertView, parent);
                        TextView tv = (TextView) view;

                        if(position==0){
                            // Deixa o hint com a cor cinza ( efeito de desabilitado)
                            tv.setTextColor(Color.LTGRAY);

                        }else {
                            tv.setTextColor(Color.BLACK);
                        }

                        return view;
                    }
                };

                //Creating the ArrayAdapter instance having the country list
                aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                //Setting the ArrayAdapter data on the Spinner
                spEstadosAnunciar.setAdapter(aa);

                spEstadosAnunciar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        String selectedItemText = (String) parent.getItemAtPosition(position);

                        if(!novoEstadoSelecionado){
                            ((TextView) parent.getChildAt(0)).setTextSize(18);
                            ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorGray));
                        }
                        else{
                            ((TextView) parent.getChildAt(0)).setTextSize(18);
                            ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                        }

                        if(position > 0){
                            estadoSelecionado = selectedItemText;
                            ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                        }


                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

             */
        }
    }

    public void atualizarInformacoes(){

        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                enderecoInvalido = numeroInvalido = complementoInvalido = cepInvalido = bairroInvalido = cidadeInvalida =
                estadoInvalido = precoInvalido = vagasInvalida = informacoesInvalida = false;

                atualizado = false;

                novoEndereco = etEndereco.getText().toString().trim().replaceAll(" +", " ");
                novoNumero = etNumero.getText().toString();
                novoComplemento = etComplemento.getText().toString().trim().replaceAll(" +", " ");
                novoBairro = etBairro.getText().toString().trim().replaceAll(" +", " ");
                novoCep = etCep.getText().toString().trim().replaceAll(" +", " ");
                novoCidade = etCidade.getText().toString().trim().replaceAll(" +", " ");
                novoEstado = estadoSelecionado;
                novoPreco = etPrecoAluguel.getText().toString();
                novoVagas = etQtdVagas.getText().toString();
                novoInfos = etInformacoesAdicionais.getText().toString().trim().replaceAll(" +", " ");

                if(!novoEndereco.equals("") && Validacao.validarInput(novoEndereco)==false){ // Caso existam emojis
                    enderecoInvalido = true;
                }

                if(!novoNumero.equals("")){
                    if(Integer.parseInt(novoNumero)<1){
                        numeroInvalido = true;
                    }
                }

                if(!novoComplemento.equals("") && Validacao.validarInput(novoComplemento)==false){ // Caso existam emojis
                    complementoInvalido = true;
                }

                if(!novoCep.equals("") && etCep.getText().toString().length()!=8){
                    cepInvalido = true;
                }
                else
                if(Validacao.validarInput(novoCep)==false){ // Caso existam emojis
                    cepInvalido = true;
                }

                if(!novoBairro.equals("") && Validacao.validarInput(novoBairro)==false){ // Caso existam emojis
                    bairroInvalido = true;
                }

                if(!novoCidade.equals("") && Validacao.validarInput(novoCidade)==false){ // Caso existam emojis
                    cidadeInvalida = true;
                }

                if(!novoPreco.equals("") && Double.parseDouble(etPrecoAluguel.getText().toString())<=0.00){
                    precoInvalido = true;
                }

                if(!novoVagas.equals("")){
                    if(Integer.parseInt(etQtdVagas.getText().toString())<1){
                        vagasInvalida = true;
                    }
                }

                if(!novoInfos.equals("") && Validacao.validarInput(novoInfos)==false){
                    informacoesInvalida = true;
                }

                if(enderecoInvalido || numeroInvalido || complementoInvalido || cepInvalido || bairroInvalido || cidadeInvalida || precoInvalido || vagasInvalida || informacoesInvalida){

                    configurarCamposInvalidosUI();
                    CustomToast.mostrarMensagem(getApplicationContext(), "Preencha os campos corretamente", Toast.LENGTH_SHORT);
                }
                else{

                    desabilitarUI();
                    configurarCamposInvalidosUI();

                    // Prevenindo clique duplo por um limite de 3 segundos
                    if (SystemClock.elapsedRealtime() - ultimoClique < 3000) return;
                    ultimoClique = SystemClock.elapsedRealtime();

                    if(!novoEndereco.equals("")) {

                        enderecoPesquisa += novoEndereco + ", ";
                        novaLocalizacao = true;

                        banco.setEndereco(anuncioId, novoEndereco);

                        atualizado = true;
                    }
                    else{

                        enderecoPesquisa += enderecoAtual + ", ";
                    }

                    if(!novoNumero.equals("")){

                        enderecoPesquisa += novoNumero + ", ";
                        novaLocalizacao = true;

                        banco.setNumero(anuncioId, novoNumero);

                        atualizado = true;
                    }
                    else{

                        enderecoPesquisa += numeroAtual + ", ";
                    }

                    if(!novoComplemento.equals("") || novoComplemento.equals("")){

                        banco.setComplemento(anuncioId, novoComplemento);

                        atualizado = true;
                    }

                    if(!novoCep.equals("") || novoCep.equals("")){

                        banco.setCep(anuncioId, novoCep);

                        atualizado = true;
                    }

                    if(!novoBairro.equals("")){

                        enderecoPesquisa += novoBairro + ", ";
                        novaLocalizacao = true;

                        banco.setBairro(anuncioId, novoBairro);

                        atualizado = true;
                    }
                    else{

                        enderecoPesquisa += bairroAtual + ", ";
                    }

                    if(!novoCidade.equals("")){

                        enderecoPesquisa += novoCidade + ", ";

                        banco.setCidade(anuncioId, novoCidade);
                        atualizado = true;
                    }
                    else{

                        enderecoPesquisa += cidadeAtual + ", ";
                    }

                    if(estadoAtualizado){
                        String nomeEstado = estadoSelecionado.substring(0, estadoSelecionado.length()-3); // Removendo sigla
                        banco.setEstado(anuncioId, estadoSelecionado);
                        enderecoPesquisa += nomeEstado + ", ";
                        atualizado = true;
                    }
                    else{
                        String nomeEstado = estadoAtual.substring(0, estadoAtual.length()-3); // Removendo sigla
                        enderecoPesquisa += nomeEstado + ", ";
                    }

                    if(!novoPreco.equals("")){

                        banco.setPrecoAluguel(anuncioId, novoPreco);
                        atualizado = true;
                    }

                    if(!novoVagas.equals("")){

                        banco.setQuantidadeVagas(anuncioId, novoVagas);
                        atualizado = true;
                    }

                    if(!novoInfos.equals("") || novoInfos.equals("")){

                        banco.setInformacoesAdicionais(anuncioId, novoInfos);
                        atualizado = true;
                    }

                    // Resgatando nova Latitude/Longitude se o endereço tiver sido alterado:
                    if(novaLocalizacao){
                        try {
                            Log.i("NOVO ENDERECO", "Pesquisando...");
                            new Async().execute("https://nominatim.openstreetmap.org/search?q="+ URLEncoder.encode(enderecoPesquisa, "UTF-8")+"&format=json");
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }

                    if(atualizado){

                        pbEditarDadosAnuncioLayout.setVisibility(View.VISIBLE);

                        // Thread para aguardar o salvamento das alterações no banco:
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                Thread thread = new Thread() {
                                    @Override
                                    public void run() {
                                        Log.i("MAIN", "salvando alterações...");
                                        runOnUiThread( new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    CustomToast.mostrarMensagem(getApplicationContext(), "Alterações salvas com sucesso", Toast.LENGTH_SHORT);
                                                    startActivity(new Intent(EditarMeuAnuncioDados.this, MeusAnunciosActivity.class));
                                                    Transition.enterTransition(EditarMeuAnuncioDados.this);
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


                    }
                    else{
                        CustomToast.mostrarMensagem(getApplicationContext(), "Não houveram alterações", Toast.LENGTH_SHORT);
                        configurarCamposInvalidosUI();
                        habilitarUI();
                    }
                }
            }
        });
    }

    public void removerAnuncio(){

        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else {
                // Exibir tela para confirmar exclusão:

                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View excluirAnuncio = li.inflate(R.layout.excluir_anuncio, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(excluirAnuncio);
                alertDialogBuilder.setTitle("Confirmação");

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Sim",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {

                                        pbEditarDadosAnuncioLayoutRemover.setVisibility(View.VISIBLE);
                                        desabilitarUI();

                                        banco.removerAnuncio(sessao.getIdUsuario(), anuncioId);

                                        // Thread para aguardar a remoção do anúncio do banco:
                                        Handler handler = new Handler();
                                        handler.postDelayed(new Runnable() {
                                            public void run() {
                                                Thread thread = new Thread() {
                                                    @Override
                                                    public void run() {
                                                        Log.i("MAIN", "removendo anúncio...");
                                                        runOnUiThread( new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                try {
                                                                    CustomToast.mostrarMensagem(getApplicationContext(), "Anúncio removido com sucesso", Toast.LENGTH_SHORT);

                                                                    startActivity(new Intent(EditarMeuAnuncioDados.this, MeusAnunciosActivity.class));
                                                                    Transition.enterTransition(EditarMeuAnuncioDados.this);
                                                                    finish();
                                                                }
                                                                catch (Exception e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        });
                                                    }
                                                };

                                                thread.start();
                                            }
                                        }, 2000);
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
        });
    }

    public void habilitarUI(){
        etEndereco.setEnabled(true);
        etNumero.setEnabled(true);
        etComplemento.setEnabled(true);
        etCep.setEnabled(true);
        etBairro.setEnabled(true);
        etCidade.setEnabled(true);
        spEstadosAnunciar.setEnabled(true);
        etPrecoAluguel.setEnabled(true);
        etQtdVagas.setEnabled(true);
        etInformacoesAdicionais.setEnabled(true);

        btnAtualizarAnuncio.setEnabled(true);
        btnMenuEditarDados.setEnabled(true);
        btnExcluirAnuncio.setEnabled(true);
    }

    // Durante a execução da Progress Bar:
    public void desabilitarUI(){
        etEndereco.setEnabled(false);
        etNumero.setEnabled(false);
        etComplemento.setEnabled(false);
        etCep.setEnabled(false);
        etBairro.setEnabled(false);
        etCidade.setEnabled(false);
        spEstadosAnunciar.setEnabled(false);
        etPrecoAluguel.setEnabled(false);
        etQtdVagas.setEnabled(false);
        etInformacoesAdicionais.setEnabled(false);

        btnAtualizarAnuncio.setEnabled(false);
        btnMenuEditarDados.setEnabled(false);
        btnExcluirAnuncio.setEnabled(false);
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
}