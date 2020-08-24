package com.example.worktalkie;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

import io.opencensus.common.ServerStatsFieldEnums;

public class cadastro_ extends AppCompatActivity {

    private Button btnVoltar,btnRegistrar,btnSelFoto;
    private EditText txtEmail, txtSenha,txtNome;
    private Uri selectedUri;
    private ImageView imgFoto;
    private String[] tipo_usuario = new String[]{"Funcionário","Administrador"};
    private Spinner sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_);
        inicializaComponentes();
        eventoClicks();

        /*ArrayAdapter<String> adapter= new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,tipo_usuario);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        sp = (Spinner) findViewById(R.id.spinner);
        sp.setAdapter(adapter);

        sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(cadastro_.this, "Deu certo", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });*/
    }
    //EXEMPLO DE COMO PEGAR OS SELECTS:
    /*public void show_Elemento(View view){
        String nome =(String) sp.getSelectedItem() ;
        long id= sp.getSelectedItemId();
        int posicao=sp.getSelectedItemPosition();

        Toast.makeText(this,"Tipo de user:"+nome+" -> ID: "+id+" -> Posição: "+posicao,Toast.LENGTH_SHORT).show();
    }*/
    private void inicializaComponentes(){
        btnRegistrar = (Button) findViewById(R.id.btnRegistro);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtSenha = (EditText) findViewById(R.id.txtSenha);
        txtNome = (EditText) findViewById(R.id.txtNome);
        btnSelFoto = (Button) findViewById(R.id.btnFoto);
        imgFoto = (ImageView) findViewById(R.id.imgFoto);

    }

    private void eventoClicks(){
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                criarUsuario();
            }
        });
        btnSelFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selecionarFoto();
            }
        });
    }
    private void criarUsuario(){
        String email = txtEmail.getText().toString();
        String senha = txtSenha.getText().toString();
        String nome = txtNome.getText().toString();

        if(email == null || email.isEmpty() || senha == null || senha.isEmpty() || nome == null || nome.isEmpty()){
            Toast.makeText(this,"Todos os campos devem ser preenchidos", Toast.LENGTH_SHORT).show();

            return;
        }
        Toast.makeText(this,"Aguarde...",Toast.LENGTH_SHORT).show();
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,senha)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            Log.i("Teste", task.getResult().getUser().getUid());
                            salvarUsuarioFB();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 0){
            selectedUri = data.getData();

            Bitmap bitAAA = null;
            try {
                bitAAA = MediaStore.Images.Media.getBitmap(getContentResolver(),selectedUri);
                imgFoto.setImageDrawable(new BitmapDrawable(bitAAA));
                btnSelFoto.setAlpha(0);
            } catch (IOException e) {
            }

        }
    }

    private void selecionarFoto(){
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i, 0);
    }
    private void salvarUsuarioFB() {
        String filename = UUID.randomUUID().toString();
        final StorageReference ref = FirebaseStorage.getInstance().getReference("/images/" + filename);
        ref.putFile(selectedUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Log.i("Teste", uri.toString());

                                String uid = FirebaseAuth.getInstance().getUid();
                                String username = txtNome.getText().toString();
                                String profileUrl = uri.toString();
                                String password = uri.toString();
                                User user = new User(uid,username,profileUrl,password);

                                FirebaseFirestore.getInstance().collection("usuarios")
                                        .document(uid)
                                        .set(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Intent i = new Intent(cadastro_.this, Home.class);
                                                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(i);
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
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Teste",e.getMessage(),e);
                    }
                });
    }
}
