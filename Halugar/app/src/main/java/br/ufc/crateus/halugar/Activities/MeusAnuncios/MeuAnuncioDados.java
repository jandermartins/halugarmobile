package br.ufc.crateus.halugar.Activities.MeusAnuncios;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import br.ufc.crateus.halugar.Activities.Menu.MenuActivity;
import br.ufc.crateus.halugar.Activities.Sessao.Sessao;
import br.ufc.crateus.halugar.Banco.Banco;
import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.Formatacao;
import br.ufc.crateus.halugar.Util.InternetCheck;
import br.ufc.crateus.halugar.Util.OnSwipeTouchListener;
import br.ufc.crateus.halugar.Util.Transition;

public class MeuAnuncioDados extends AppCompatActivity {

    TextView tvEnderecoMeuAnuncio, tvNumeroMeuAnuncio, tvComplementoMeuAnuncio, tvCepMeuAnuncio, tvCidadeMeuAnuncio,
             tvBairroMeuAnuncio, tvPrecoMeuAnuncio, tvQtdVagasMeuAnuncio, tvInformacoesMeuAnuncio, tvEstadoMeuAnuncio;

    ImageButton btnMenuMeuAnuncioDados, btnExcluirAnuncio;

    Button btnAtualizarDadosMeuAnuncio;

    String anuncioId;

    String endereco="", complemento="", cep="", bairro="", cidade="", estado="", informacoesAdicionais="";
    int qtdVagas, numero;
    double precoAluguel;

    Banco banco;
    Sessao sessao;

    ScrollView scrollView;
    ConstraintLayout pbMeuAnuncioDadosLayoutRemover;

    final Context context = this;

    Bundle extras;
    String callerIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meu_anuncio_dados);

        scrollView = (ScrollView)findViewById(R.id.scrollView2);

        btnAtualizarDadosMeuAnuncio = (Button) findViewById(R.id.btnAtualizarDadosAnuncio);

        tvEnderecoMeuAnuncio = (TextView) findViewById(R.id.tvEnderecoMeuAnuncio);
        tvNumeroMeuAnuncio = (TextView) findViewById(R.id.tvNumeroMeuAnuncio);
        tvComplementoMeuAnuncio = (TextView) findViewById(R.id.tvComplementoMeuAnuncio);
        tvCepMeuAnuncio = (TextView) findViewById(R.id.tvCepMeuAnuncio);
        tvBairroMeuAnuncio = (TextView) findViewById(R.id.tvBairroMeuAnuncio);
        tvCidadeMeuAnuncio = (TextView)findViewById(R.id.tvCidadeMeuAnuncio);
        tvEstadoMeuAnuncio = (TextView)findViewById(R.id.tvEstadoMeuAnuncio);
        tvPrecoMeuAnuncio = (TextView) findViewById(R.id.tvPrecoMeuAnuncio);
        tvQtdVagasMeuAnuncio = (TextView) findViewById(R.id.tvQtdVagasMeuAnuncio);
        tvInformacoesMeuAnuncio = (TextView) findViewById(R.id.tvInformacoesAdicionaisMeuAnuncio);

        pbMeuAnuncioDadosLayoutRemover = (ConstraintLayout)findViewById(R.id.pbMeuAnuncioDadosLayoutRemover);

        btnMenuMeuAnuncioDados = (ImageButton) findViewById(R.id.btnMenuMeuAnuncioDados);
        btnExcluirAnuncio = (ImageButton)findViewById(R.id.btnExcluirAnuncio);

        banco = Banco.getInstance();
        sessao = Sessao.getInstance();

        habilitarSwipeMenu();
        carregarInformacoes();

        if(callerIntent.equals("MeusAnunciosActivity")){
            Transition.enterTransition(MeuAnuncioDados.this); // Vem do adapter
        }

        btnAtualizarDadosMeuAnuncio.setOnClickListener(view -> atualizarInformacoes());

        btnMenuMeuAnuncioDados.setOnClickListener(view -> {

            Intent i = new Intent(MeuAnuncioDados.this, MenuActivity.class);

            i.putExtra("callerIntent", "MeuAnuncioDados");

            i.putExtra("idAnuncio", anuncioId);

            i.putExtra("Endereco", endereco);
            i.putExtra("Numero", numero);
            i.putExtra("Complemento", complemento);
            i.putExtra("CEP", cep);
            i.putExtra("Bairro", bairro);
            i.putExtra("Cidade", cidade);
            i.putExtra("Estado", estado);
            i.putExtra("Preco", precoAluguel);
            i.putExtra("Vagas", qtdVagas);
            i.putExtra("Informacoes", informacoesAdicionais);

            startActivity(i);
            Transition.enterTransition(MeuAnuncioDados.this);
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
    public void onBackPressed() {

        Intent i = new Intent(MeuAnuncioDados.this, MeusAnunciosActivity.class);

        i.putExtra("callerIntent", "MeuAnuncioDados");

        startActivity(i);
        Transition.backTransition(MeuAnuncioDados.this);
        finish();
    }

    public void habilitarSwipeMenu(){

        scrollView.setOnTouchListener(new OnSwipeTouchListener(MeuAnuncioDados.this) {
            public void onSwipeRight() {

                Intent i = new Intent(MeuAnuncioDados.this, MenuActivity.class);

                i.putExtra("callerIntent", "MeuAnuncioDados");

                i.putExtra("idAnuncio", anuncioId);

                i.putExtra("Endereco", endereco);
                i.putExtra("Numero", numero);
                i.putExtra("Complemento", complemento);
                i.putExtra("CEP", cep);
                i.putExtra("Bairro", bairro);
                i.putExtra("Cidade", cidade);
                i.putExtra("Estado", estado);
                i.putExtra("Preco", precoAluguel);
                i.putExtra("Vagas", qtdVagas);
                i.putExtra("Informacoes", informacoesAdicionais);

                startActivity(i);
                Transition.enterTransition(MeuAnuncioDados.this);
                finish();
            }
        });
    }

    public void carregarInformacoes(){

        extras = getIntent().getExtras();

        if(extras!=null){

            callerIntent = extras.getString("callerIntent");

            anuncioId = extras.getString("idAnuncio");

            endereco = extras.getString("Endereco");
            numero = extras.getInt("Numero");
            complemento = extras.getString("Complemento");
            cep = extras.getString("CEP");
            bairro = extras.getString("Bairro");
            cidade = extras.getString("Cidade");
            estado = extras.getString("Estado");
            precoAluguel = extras.getDouble("Preco");
            qtdVagas = extras.getInt("Vagas");
            informacoesAdicionais = extras.getString("Informacoes");

            // Setando valores:
            String strDouble = String.format("%.2f", precoAluguel);

            tvEnderecoMeuAnuncio.setText(endereco);
            tvNumeroMeuAnuncio.setText(String.valueOf(numero));

            if(!complemento.equals("")){
                tvComplementoMeuAnuncio.setText(complemento);
            }
            else{
                tvComplementoMeuAnuncio.setText("Não informado");
            }

            if(!cep.equals("")){
                tvCepMeuAnuncio.setText(Formatacao.formatarCep(cep));
            }
            else {
                tvCepMeuAnuncio.setText("Não informado");
            }

            tvCidadeMeuAnuncio.setText(cidade);
            tvEstadoMeuAnuncio.setText(estado);
            tvBairroMeuAnuncio.setText(bairro);
            tvPrecoMeuAnuncio.setText("R$ " + strDouble);
            tvQtdVagasMeuAnuncio.setText(String.valueOf(qtdVagas));

            if(!informacoesAdicionais.equals("")){
                tvInformacoesMeuAnuncio.setText(informacoesAdicionais);
            }
            else{
                tvInformacoesMeuAnuncio.setText("Nenhuma");
            }
        }
    }

    public void atualizarInformacoes(){

        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                Intent i = new Intent(MeuAnuncioDados.this, EditarMeuAnuncioDados.class);

                i.putExtra("callerIntent", "MeuAnuncioDados");

                i.putExtra("anuncioId", anuncioId);

                if(complemento.equals("")){
                    complemento = "Não informado";
                }

                if(cep.equals("")){
                    cep = "Não informado";
                }

                if(informacoesAdicionais.equals("")){
                    informacoesAdicionais = "Nenhuma";
                }

                i.putExtra("idAnuncio", anuncioId);

                i.putExtra("Endereco", endereco);
                i.putExtra("Numero", numero);
                i.putExtra("Complemento", complemento); // Não obrigatório
                i.putExtra("CEP", cep); // Não obrigatório
                i.putExtra("Bairro", bairro);
                i.putExtra("Cidade", cidade);
                i.putExtra("Estado", estado);
                i.putExtra("Preco", precoAluguel);
                i.putExtra("Vagas", qtdVagas);
                i.putExtra("Informacoes", informacoesAdicionais); // Não obrigatório

                startActivity(i);
                Transition.enterTransition(MeuAnuncioDados.this);
                finish();
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

                                        pbMeuAnuncioDadosLayoutRemover.setVisibility(View.VISIBLE);
                                        desabilitarUI();

                                        banco.removerAnuncio(sessao.getIdUsuario(), anuncioId);

                                        // Thread para aguardar a remoção do anúncio do banco: - DEIXAR APENAS O HANDLER
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

                                                                    startActivity(new Intent(MeuAnuncioDados.this, MeusAnunciosActivity.class));
                                                                    Transition.enterTransition(MeuAnuncioDados.this);
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
        btnMenuMeuAnuncioDados.setEnabled(true);
        btnAtualizarDadosMeuAnuncio.setEnabled(true);
        btnExcluirAnuncio.setEnabled(true);
    }

    public void desabilitarUI(){
        btnMenuMeuAnuncioDados.setEnabled(false);
        btnAtualizarDadosMeuAnuncio.setEnabled(false);
        btnExcluirAnuncio.setEnabled(false);
    }
}
