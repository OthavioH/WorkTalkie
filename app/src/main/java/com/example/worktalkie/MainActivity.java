package com.example.worktalkie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {

    private Button btnLogin;
    private EditText txtEmail, txtSenha;
    public LinearLayout ltLogin;
    private TextView txtCadastro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inicializaComponentes();
        eventoClicks();
    }

    private void eventoClicks(){
        txtCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),cadastro_.class);
                startActivity(i);

            }
        });
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = txtEmail.getText().toString();
                String senha = txtSenha.getText().toString();
                Log.i("Teste", email);
                Log.i("Teste", senha);

                if(email == null || email.isEmpty() || senha == null || senha.isEmpty()){
                    Toast.makeText(MainActivity.this,"Todos os campos devem ser preenchidos", Toast.LENGTH_SHORT).show();
                    return;
                }
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email,senha)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.i("Teste", task.getResult().getUser().getUid());

                                    Intent i = new Intent(MainActivity.this,Home.class);

                                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);

                                    startActivity(i);
                                }
                            }


                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.i("Teste",e.getMessage());
                            }
                        });
            }
        });
    }

    private void inicializaComponentes(){
        btnLogin = (Button) findViewById(R.id.btnLogin);
        txtCadastro = (TextView) findViewById(R.id.txtCadastro);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtSenha = (EditText) findViewById(R.id.txtSenha);
    }
}
