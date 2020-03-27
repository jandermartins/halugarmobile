package br.ufc.crateus.halugar.Activities.Anuncio;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import br.ufc.crateus.halugar.Activities.Menu.MenuActivity;
import br.ufc.crateus.halugar.R;
import br.ufc.crateus.halugar.Util.CustomToast;
import br.ufc.crateus.halugar.Util.Formatacao;
import br.ufc.crateus.halugar.Util.InternetCheck;
import br.ufc.crateus.halugar.Util.OnSwipeTouchListener;

public class ContatoFragment extends Fragment {

    String nome, telefone, email;
    Bundle arguments;
    ImageButton btnEmail, btnLigar;

    ScrollView scrollView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        arguments =  getArguments();

        if(arguments==null){
            Log.i("TESTE - FRAGMENT", "CONTATO: arguments==null");
            return;
        }
        else{
            nome = arguments.getString("Nome");
            telefone = Formatacao.formatarTelefone(arguments.getString("Telefone"));
            email = arguments.getString("Email");

            Log.i("TESTE - ANUNCIANTE", arguments.toString());
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getActivity().getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
        });
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ContatoActivity contatoActivity;
        final TextView tvNomeCompletoAnuncio, tvTelefoneAnuncio, tvEmailAnuncio;

        contatoActivity = ViewModelProviders.of(this).get(ContatoActivity.class);
        View root = inflater.inflate(R.layout.fragment_contato, container, false);

        scrollView = (ScrollView)root.findViewById(R.id.teste);

        tvNomeCompletoAnuncio = (TextView)root.findViewById(R.id.tvNomeCompletoAnuncio);
        tvTelefoneAnuncio = (TextView)root.findViewById(R.id.tvTelefoneAnuncio);
        tvEmailAnuncio = (TextView)root.findViewById(R.id.tvEmailAnuncio);

        btnEmail = (ImageButton)root.findViewById(R.id.btnEmail);
        btnLigar = (ImageButton)root.findViewById(R.id.btnLigar);

        habilitarSwipeMenu();

        contatoActivity.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                tvNomeCompletoAnuncio.setText(nome);
                tvTelefoneAnuncio.setText(telefone);
                tvEmailAnuncio.setText(email);
            }
        });

        btnLigar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                realizarLigacao();
            }
        });

        btnEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarEmail();
            }
        });

        return root;
    }

    public void habilitarSwipeMenu(){
        scrollView.setOnTouchListener(new OnSwipeTouchListener(getActivity()) {
            public void onSwipeRight() {
                //Toast.makeText(getApplicationContext(), "RIGHT", Toast.LENGTH_SHORT).show();
                //startActivity(new Intent(getActivity(), MenuActivity.class));
                Intent i = new Intent(getActivity(), MenuActivity.class);
                i.putExtra("callerIntent", "ContatoFragment");
                startActivity(i);
            }
        });
    }

    public void realizarLigacao(){
        // Use format with "tel:" and phoneNumber created is
        // stored in u.
        Uri u = Uri.parse("tel:" + telefone);

        // Create the intent and set the data for the
        // intent as the phone number.
        Intent i = new Intent(Intent.ACTION_DIAL, u);
        startActivity(i);
    }

    public void enviarEmail(){
        //showCustomAlert("Email");
        new InternetCheck(internet -> {
            if(!internet){
                CustomToast.mostrarMensagem(getActivity().getApplicationContext(), "Verifique sua conexão com a internet", Toast.LENGTH_SHORT);
            }
            else{
                Intent i = new Intent(Intent.ACTION_SENDTO);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL  , new String[]{email});
                i.putExtra(Intent.EXTRA_SUBJECT, "Anúncio - HáLugar");
                i.putExtra(Intent.EXTRA_TEXT   , "Digite sua mensagem...");

                try {
                    startActivity(Intent.createChooser(i, "Enviar email:"));
                } catch (android.content.ActivityNotFoundException ex) {

                    CustomToast.mostrarMensagem(getActivity().getApplicationContext(), "Nenhum aplicativo de email encontrado", Toast.LENGTH_SHORT);
                }
            }
        });
    }
}
