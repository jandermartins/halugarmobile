package br.ufc.crateus.halugar.Util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import br.ufc.crateus.halugar.R;

public class CustomToast {

    public static int POS_Y = 350; // Anterior: 450 (rodapé)

    @SuppressLint("ResourceAsColor")
    public static void mostrarMensagem(Context context, String mensagem, int duracao){
        // Create layout inflator object to inflate toast.xml file
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        // Call toast.xml file for toast layout
        View layout = inflater.inflate(R.layout.toast, null);

        TextView text = (TextView) layout.findViewById(R.id.tvMensagem);
        text.setText(mensagem);
        text.setTextColor(Color.BLACK);

        Toast toast = new Toast(context);

        // Set layout to toast
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, CustomToast.POS_Y);
        toast.setDuration(duracao);
        toast.show();
    }

    public static void mostrarMensagemCentralizada(Context context, String mensagem, int duracao){
        // Create layout inflator object to inflate toast.xml file
        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        // Call toast.xml file for toast layout
        View layout = inflater.inflate(R.layout.toast, null);

        TextView text = (TextView) layout.findViewById(R.id.tvMensagem);
        text.setText(mensagem);
        text.setGravity(Gravity.CENTER_HORIZONTAL);

        Toast toast = new Toast(context);

        // Set layout to toast
        toast.setView(layout);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, CustomToast.POS_Y);
        toast.setDuration(duracao);
        toast.show();
    }
}