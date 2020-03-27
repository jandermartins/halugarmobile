package br.ufc.crateus.halugar.Activities.Anuncio;

import android.content.Intent;
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
import br.ufc.crateus.halugar.Util.Transition;

public class InformacoesFragment extends Fragment {

    String endereco, complemento, cep, bairro, informacoes, cidade, estado;
    int numero, vagas;
    double preco;
    Bundle arguments;

    TextView tvEnderecoAnuncio, tvNumeroAnuncio, tvComplementoAnuncio, tvCepAnuncio, tvBairroAnuncio, tvCidadeAnuncio,
            tvPrecoAnuncio, tvQtdVagasAnuncio, tvInformacoesAdicionaisAnuncio, tvEstadoAnuncio;

    ScrollView scrollView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        arguments = getArguments();

        //Transition.enterTransition(getActivity());

        if(arguments==null){
            Log.i("TESTE - FRAGMENT", "arguments==null");
            return;
        }
        else{
            // Recebendo as informações a AnuncioActivity:

            Log.i("TESTE - FRAGMENT = ", arguments.toString());

            endereco = arguments.getString("Endereco");
            numero = Integer.parseInt(arguments.getString("Numero"));

            if(!arguments.getString("Complemento").equals("")){
                complemento = arguments.getString("Complemento");
            }
            else{
                complemento = "Não informado";
            }
            if(!arguments.getString("Cep").equals("")){
                cep = Formatacao.formatarCep(arguments.getString("Cep"));
            }
            else{
                cep = "Não informado";
            }

            bairro = arguments.getString("Bairro");
            cidade = arguments.getString("Cidade");
            estado = arguments.getString("Estado");
            preco = Double.parseDouble(arguments.getString("Preco"));
            vagas = Integer.parseInt(arguments.getString("Vagas"));

            if(!arguments.getString("Informacoes").equals("")){
                informacoes = arguments.getString("Informacoes");
            }
            else{
                informacoes = "Nenhuma";
            }
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

        InformacoesActivity informacoesActivity;

        informacoesActivity = ViewModelProviders.of(this).get(InformacoesActivity.class);
        View root = inflater.inflate(R.layout.fragment_informacoes, container, false);

        scrollView = (ScrollView)root.findViewById(R.id.scrollViewInformacoes);

        tvEnderecoAnuncio = (TextView) root.findViewById(R.id.tvEnderecoAnuncio);
        tvNumeroAnuncio = (TextView) root.findViewById(R.id.tvNumeroAnuncio);
        tvComplementoAnuncio = (TextView) root.findViewById(R.id.tvComplementoAnuncio);
        tvCepAnuncio = (TextView) root.findViewById(R.id.tvCepAnuncio);
        tvBairroAnuncio = (TextView) root.findViewById(R.id.tvBairroAnuncio);
        tvCidadeAnuncio = (TextView) root.findViewById(R.id.tvCidadeAnuncio);
        tvEstadoAnuncio = (TextView) root.findViewById(R.id.tvEstadoAnuncio);
        tvPrecoAnuncio = (TextView) root.findViewById(R.id.tvPrecoAnuncio);
        tvQtdVagasAnuncio = (TextView) root.findViewById(R.id.tvQtdVagasAnuncio);
        tvInformacoesAdicionaisAnuncio = (TextView) root.findViewById(R.id.tvInformacoesAdicionaisAnuncio);

        habilitarSwipeMenu();

        informacoesActivity.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {

                tvEnderecoAnuncio.setText(endereco);
                tvNumeroAnuncio.setText(String.valueOf(numero));
                tvComplementoAnuncio.setText(complemento);
                tvCepAnuncio.setText(cep);
                tvBairroAnuncio.setText(bairro);
                tvCidadeAnuncio.setText(cidade);
                tvEstadoAnuncio.setText(estado);
                String strDouble = String.format("%.2f", preco);
                tvPrecoAnuncio.setText("R$ " + strDouble);
                tvQtdVagasAnuncio.setText(String.valueOf(vagas));
                tvInformacoesAdicionaisAnuncio.setText(informacoes);
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
                i.putExtra("callerIntent", "InformacoesFragment");
                startActivity(i);
            }
        });
    }
}