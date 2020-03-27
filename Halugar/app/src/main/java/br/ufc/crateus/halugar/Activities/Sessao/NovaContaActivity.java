package br.ufc.crateus.halugar.Activities.Sessao;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import br.ufc.crateus.halugar.Banco.Banco;
import br.ufc.crateus.halugar.Model.Usuario;
import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.InternetCheck;
import br.ufc.crateus.halugar.Util.Transition;
import br.ufc.crateus.halugar.Util.Validacao;

public class NovaContaActivity extends AppCompatActivity {

    EditText etNomeCompletoCadastrar, etEmailCadastrar, etTelefoneCadastrar, etSenhaCadastrar;
    Button btnCadastrar;
    ImageButton btnVoltarNovaConta;
    CheckBox cbSenha;
    TextView labelNome, labelEmail, labelTelefone, labelSenha;

    Context context = this;

    Banco banco;
    Sessao sessao;

    ConstraintLayout pbNovaContaLayout;

    long ultimoClique = 0;

    Bundle extras;
    String callerIntent;

    boolean nomeInvalido=false, emailInvalido=false, telefoneInvalido=false, senhaInvalida=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nova_conta);

        btnCadastrar = (Button) findViewById(R.id.btnCadastrar);
        etNomeCompletoCadastrar = (EditText) findViewById(R.id.etNomeCompletoCadastrar);
        etEmailCadastrar = (EditText) findViewById(R.id.etEmailCadastrar);
        etTelefoneCadastrar = (EditText) findViewById(R.id.etTelefoneCadastrar);
        etSenhaCadastrar = (EditText) findViewById(R.id.etSenhaCadastrar);
        cbSenha = (CheckBox) findViewById(R.id.cbSenha);
        btnVoltarNovaConta = (ImageButton) findViewById(R.id.btnVoltarNovaConta);
        pbNovaContaLayout = (ConstraintLayout)findViewById(R.id.pbNovaContaLayout);
        labelNome = (TextView) findViewById(R.id.labelNome);
        labelEmail =(TextView)findViewById(R.id.labelEmail);
        labelTelefone = (TextView)findViewById(R.id.labelTelefone);
        labelSenha = (TextView) findViewById(R.id.labelSenha);

        banco = Banco.getInstance();
        sessao = Sessao.getInstance();

        carregarInformacoes();

        etNomeCompletoCadastrar.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        etNomeCompletoCadastrar.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        etEmailCadastrar.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);

        btnCadastrar.setOnClickListener(view -> criarConta());

        btnVoltarNovaConta.setOnClickListener(view -> voltar());

        cbSenha.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // checkbox status is changed from uncheck to checked.
                if (!isChecked) {
                    // hide password
                    etSenhaCadastrar.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    // show password
                    etSenhaCadastrar.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
            }
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

    // Autenticação:
    private void sendEmailVerification() {
        sessao.getUsuario().sendEmailVerification()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Não está exibindo mensagens (Toast):
                        if (task.isSuccessful()) {
                            Log.i("CONTA", "Enviou email...");
                            //CustomToast.mostrarMensagem(getApplicationContext(), "Link de verificação enviado para \n" + etEmailCadastrar.getText().toString());
                        } else {
                            //Log.e(TAG, "sendEmailVerification", task.getException());
                            Log.i("CONTA", "Falha ao enviar email...");
                            //CustomToast.mostrarMensagem(getApplicationContext(), "Falha ao enviar link de verificação para\n" + etEmailCadastrar.getText().toString());
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        voltar();
    }

    public void voltar(){

        Intent i = new Intent(NovaContaActivity.this, EntrarActivity.class);

        i.putExtra("callerIntent", "NovaContaActivity");

        startActivity(i);
        Transition.backTransition(NovaContaActivity.this);
        finish();
    }

    public void criarConta(){
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{

                // Prevenindo clique duplo por um limite de 3 segundos
                if (SystemClock.elapsedRealtime() - ultimoClique < 3000) return;
                ultimoClique = SystemClock.elapsedRealtime();

                final String nomeCompleto, email, telefone, senha;

                nomeCompleto = etNomeCompletoCadastrar.getText().toString().trim().replaceAll(" +", " ");
                email = etEmailCadastrar.getText().toString().trim().replaceAll(" +", " ");
                telefone = etTelefoneCadastrar.getText().toString().trim().replaceAll(" +", " ");
                senha = etSenhaCadastrar.getText().toString();

                nomeInvalido = telefoneInvalido = emailInvalido = senhaInvalida = false;

                if(nomeCompleto.equals("") || Validacao.validarNome(nomeCompleto)==false){
                    //msgDadosInvalidos += "\n   - Nome completo";
                    //dadosInvalidos = true;
                    nomeInvalido = true;
                }
                else{
                    nomeInvalido = false;
                }

                if(email.equals("") || Validacao.validarEmail(email)==false){
                    //msgDadosInvalidos += "\n   - Email válido";
                    //dadosInvalidos = true;
                    emailInvalido = true;
                }
                else{
                    emailInvalido = false;
                }

                if(!telefone.equals("")){
                    if((telefone.length()!=11 && telefone.length()!=10) || ((telefone.length()==11 || telefone.length()==10) && Validacao.validarTelefone(telefone)==false)){
                        //msgDadosInvalidos += "\n   - Telefone";
                        //dadosInvalidos = true;
                        telefoneInvalido = true;
                    }
                    else{
                        telefoneInvalido = false;
                    }
                }
                else{
                    telefoneInvalido = false;
                }

                if(senha.equals("") || senha.length()<6){
                    //msgDadosInvalidos += "\n   - Senha (6 ou mais caracteres)";
                    //dadosInvalidos = true;
                    senhaInvalida = true;
                }
                else{
                    senhaInvalida = false;
                }

                if(nomeInvalido || emailInvalido || telefoneInvalido || senhaInvalida){
                    //Log.i("Nova Conta", "Dados invalidos ou campos vazios");
                    CustomToast.mostrarMensagem(getApplicationContext(), "Preencha os campos corretamente", Toast.LENGTH_SHORT);
                    configurarCamposInvalidosUI();
                }
                else{

                    pbNovaContaLayout.setVisibility(View.VISIBLE);
                    configurarCamposInvalidosUI();
                    desabilitarUI();

                    sessao.getAutenticacaoUsuario().createUserWithEmailAndPassword(email, senha)
                            .addOnCompleteListener(NovaContaActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        Usuario usuario = new Usuario(sessao.getIdUsuario(), "", nomeCompleto, email, telefone);
                                        banco.adicionarUsuario(usuario);

                                        sendEmailVerification();

                                        // Mostrar mensagem para o usuário:
                                        LayoutInflater li = LayoutInflater.from(context);
                                        View verificarEmail = li.inflate(R.layout.custom_alert_dialog, null);
                                        TextView tvLinkVerificacao = (TextView) verificarEmail.findViewById(R.id.tvMensagemAlertDialog);

                                        tvLinkVerificacao.setText("Acesse o link de verificação que foi enviado para o seu email.");

                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                                context);

                                        alertDialogBuilder.setTitle("Confirmação");

                                        // set prompts.xml to alertdialog builder
                                        alertDialogBuilder.setView(verificarEmail);

                                        // set dialog message
                                        alertDialogBuilder
                                                .setCancelable(false)
                                                .setPositiveButton("OK",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog,int id) {
                                                                startActivity(new Intent(NovaContaActivity.this, EntrarActivity.class));
                                                                Transition.enterTransition(NovaContaActivity.this);
                                                                finish();
                                                            }
                                                        });

                                        // create alert dialog
                                        AlertDialog alertDialog = alertDialogBuilder.create();

                                        // show it
                                        alertDialog.show();

                                    } else {

                                        // Mostrar mensagem para o usuário:
                                        LayoutInflater li = LayoutInflater.from(context);
                                        View verificarEmail = li.inflate(R.layout.custom_alert_dialog, null);
                                        TextView tvEmailCadastrado = (TextView) verificarEmail.findViewById(R.id.tvMensagemAlertDialog);

                                        tvEmailCadastrado.setText("Email já cadastrado.");

                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                                context);

                                        alertDialogBuilder.setTitle("Falha ao criar conta");

                                        // set prompts.xml to alertdialog builder
                                        alertDialogBuilder.setView(verificarEmail);

                                        // set dialog message
                                        alertDialogBuilder
                                                .setCancelable(false)
                                                .setPositiveButton("Entrar com email",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog,int id) {
                                                                pbNovaContaLayout.setVisibility(View.INVISIBLE);
                                                                startActivity(new Intent(NovaContaActivity.this, EntrarActivity.class));
                                                                Transition.enterTransition(NovaContaActivity.this);
                                                                finish();
                                                            }
                                                        })
                                                .setNegativeButton("Cancelar",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int id) {
                                                                dialog.cancel();
                                                                habilitarUI();
                                                            }
                                                        });;

                                        // create alert dialog
                                        AlertDialog alertDialog = alertDialogBuilder.create();

                                        // show it
                                        alertDialog.show();

                                        pbNovaContaLayout.setVisibility(View.INVISIBLE);
                                        habilitarUI();
                                    }
                                }
                            });
                }
            }
        });
    }

    // Durante a execução da Progress Bar:
    public void habilitarUI(){
        etNomeCompletoCadastrar.setEnabled(true);
        etEmailCadastrar.setEnabled(true);
        etTelefoneCadastrar.setEnabled(true);
        etSenhaCadastrar.setEnabled(true);
    }

    public void desabilitarUI(){
        etNomeCompletoCadastrar.setEnabled(false);
        etEmailCadastrar.setEnabled(false);
        etTelefoneCadastrar.setEnabled(false);
        etSenhaCadastrar.setEnabled(false);
    }

    public void carregarInformacoes(){

        extras = getIntent().getExtras();

        if(extras!=null){
            callerIntent = extras.getString("callerIntent");
        }
    }

    public void configurarCamposInvalidosUI(){

        if(nomeInvalido){
            etNomeCompletoCadastrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelNome.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etNomeCompletoCadastrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelNome.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        if(emailInvalido){
            etEmailCadastrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelEmail.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etEmailCadastrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelEmail.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        if(telefoneInvalido){
            etTelefoneCadastrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelTelefone.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etTelefoneCadastrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelTelefone.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }

        if(senhaInvalida){
            etSenhaCadastrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
            labelSenha.setText("Senha (6 ou mais caracteres)");
            labelSenha.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorInvalidInput)));
        }
        else{
            etSenhaCadastrar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
            labelSenha.setTextColor(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
        }
    }
}