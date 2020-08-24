package com.example.worktalkie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;
import com.xwray.groupie.ViewHolder;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Nullable;

public class Chat extends AppCompatActivity {

    private FloatingActionButton btnContatos;
    private GroupAdapter adapter;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ChatApplication application = (ChatApplication) getApplication();

        getApplication().registerActivityLifecycleCallbacks(application);


        RecyclerView rv = findViewById(R.id.recycler_contato);
        adapter = new GroupAdapter<>();
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));

        verifyAuth();

        updateToken();

        fetchLastMessage();
    }

    private void updateToken() {
        String token = FirebaseInstanceId.getInstance().getToken();
        String uid = FirebaseAuth.getInstance().getUid();

        if(uid != null){
            FirebaseFirestore.getInstance().collection("usuarios")
                    .document(uid)
                    .update("token",token);
        }
    }

    private void fetchLastMessage() {
        String uid = FirebaseAuth.getInstance().getUid(); //Pega o ID do usuário atual

        FirebaseFirestore.getInstance().collection("/last-messages")
                .document(uid)
                .collection("contatos")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();

                        if(documentChanges != null){
                            for (DocumentChange doc: documentChanges) {
                                if(doc.getType() == DocumentChange.Type.ADDED){
                                    Contato contato = doc.getDocument().toObject(Contato.class);
                                    adapter.add(new ContatoItem(contato));

                                }
                            }
                        }
                    }
                });
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    private void verifyAuth() {
        if(FirebaseAuth.getInstance().getUid() == null){
            Intent i = new Intent(Chat.this,MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK   | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }


        @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.Contatos:
                Intent i = new Intent(Chat.this,ContactsActivity.class);
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


        private class ContatoItem extends Item<ViewHolder>{

        private final Contato contato;

        private ContatoItem(Contato contato) {
            this.contato = contato;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {

            TextView username = viewHolder.itemView.findViewById(R.id.txt_Username);
            TextView nome = viewHolder.itemView.findViewById(R.id.txtNomeCara);
            TextView message = viewHolder.itemView.findViewById(R.id.txtLastMessage);
            ImageView imgPhoto = viewHolder.itemView.findViewById(R.id.imgUser);

            username.setText(contato.getUsername());
            if(!contato.getUsername().equals(contato.getNome())){
                nome.setText(contato.getUsername());

            }
            else {
                nome.setText("Eu");
            }
            message.setText(contato.getUltimaMensagem());
            Picasso.get().load(contato.getPhotoUrl()).into(imgPhoto);
        }

        @Override
        public int getLayout() {
            return R.layout.item_user_message;
        }
    }
}
