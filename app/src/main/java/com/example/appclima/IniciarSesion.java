package com.example.appclima;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

public class IniciarSesion extends AppCompatActivity {

    TextView CorreoElectronico, Contrasenia;
    Button Ingresar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciar_sesion);

        CorreoElectronico = findViewById(R.id.CorreoElectronico);
        Contrasenia = findViewById(R.id.Contrasenia);

        Ingresar = findViewById(R.id.Ingresar);

        Ingresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(IniciarSesion.this, MainActivity.class);
                startActivity(i);

                /*
                String email = CorreoElectronico.getText().toString();
                String pass = Contrasenia.getText().toString();

                if(email.isEmpty()){
                    Toast.makeText(IniciarSesion.this, "Ingrese un correo electronico", Toast.LENGTH_SHORT).show();
                }
                
                if(pass.isEmpty()){
                    Toast.makeText(IniciarSesion.this, "Ingrese una contraseña", Toast.LENGTH_SHORT).show();
                }else if(pass.length()<6){
                    Toast.makeText(IniciarSesion.this, "Contraseña debe ser mayor a 6", Toast.LENGTH_SHORT).show();
                }

                 */
            }
        });




    }
}