package com.example.worktalkie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.ViewHolder;

import java.util.List;

import javax.annotation.Nullable;

public class ConversarActivity extends AppCompatActivity {

    private GroupAdapter adapter;
    private User user;
    private Button btnChat;
    private EditText editChat;
    private User me;
    private RecyclerView recy;
    private ConstraintLayout llt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversar);

        llt = (ConstraintLayout) findViewById(R.id.lt_chat);
        recy = (RecyclerView) findViewById(R.id.recyChat);


        user = getIntent().getExtras().getParcelable("user");
        getSupportActionBar().setTitle(user.getUsername());

        RecyclerView rv = findViewById(R.id.recyChat);
        btnChat = findViewById(R.id.btnChat);
        editChat = (EditText) findViewById(R.id.edtChat);
        EventoEnviar();
        adapter = new GroupAdapter();
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);


        FirebaseFirestore.getInstance().collection("/usuarios")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        me = documentSnapshot.toObject(User.class);
                        fetchMessages();
                    }
                });

    }

    private void fetchMessages() {
        if(me != null){
            String fromId = me.getUuid();
            String toId = user.getUuid();

            final Contato contato = new Contato();

            FirebaseFirestore.getInstance().collection("/conversas")
                    .document(fromId)
                    .collection(toId)
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            List<DocumentChange> documentChanges = queryDocumentSnapshots.getDocumentChanges();
                            if(documentChanges != null){
                                for (DocumentChange doc: documentChanges){
                                    if(doc.getType() == DocumentChange.Type.ADDED){
                                        Message message = doc.getDocument().toObject(Message.class);
                                        adapter.add(new MessageItem(message));
                                    }
                                }
                            }
                        }
                    });

        }
    }

    private void EventoEnviar() {
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    private void sendMessage() {
        String text = editChat.getText().toString();
        editChat.setText(null);

        final String fromID = FirebaseAuth.getInstance().getUid();
        final String toID = user.getUuid();
        long timestamp = System.currentTimeMillis();

        final Message message = new Message();
        message.setFromID(fromID);
        message.setToID(toID);
        message.setTimestamp(timestamp);
        message.setText(text);

        if(!message.getText().isEmpty()){
            FirebaseFirestore.getInstance().collection("/conversas").document(fromID).collection(toID).add(message).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Log.d("Teste",documentReference.getId());

                    Contato contato = new Contato();
                    contato.setUuid(toID);
                    contato.setUsername(user.getUsername());
                    contato.setNome(user.getUsername());
                    contato.setPhotoUrl(user.getProfileUrl());
                    contato.setTimestamp(message.getTimestamp());
                    contato.setUltimaMensagem(message.getText());;

                    FirebaseFirestore.getInstance().collection("/last-messages")
                            .document(fromID)
                            .collection("contatos")
                            .document(toID)
                            .set(contato);

                    if(!user.isOnline()){
                        Notification notification = new Notification();
                        notification.setFromID(message.getFromID());
                        notification.setToID(message.getToID());
                        notification.setTimestamp(message.getTimestamp());
                        notification.setText(message.getText());
                        notification.setFromName(me.getUsername());

                        FirebaseFirestore.getInstance().collection("/notifications")
                                .document(user.getToken())
                                .set(notification);
                    }
                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Teste",e.getMessage(),e);
                }
            });

            FirebaseFirestore.getInstance().collection("/conversas")
                    .document(toID)
                    .collection(fromID)
                    .add(message).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                        Log.d("Teste",documentReference.getId());

                    Contato contato = new Contato();
                    contato.setUuid(fromID);
                    contato.setUsername(me.getUsername());
                    contato.setNome("Eu");
                    contato.setPhotoUrl(me.getProfileUrl());
                    contato.setTimestamp(message.getTimestamp());
                    contato.setUltimaMensagem(message.getText());
                    FirebaseFirestore.getInstance().collection("/last-messages")
                            .document(toID)
                            .collection("contatos")
                            .document(fromID)
                            .set(contato);

                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("Teste",e.getMessage(),e);
                }
            });
        }
    }

    private class MessageItem extends Item<ViewHolder> {
        
        private final Message message;

        private MessageItem(Message message) {
            this.message = message;
        }


        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
                TextView txtMsg = viewHolder.itemView.findViewById(R.id.txtMessage);
                ImageView imgMessage = viewHolder.itemView.findViewById(R.id.imgFotoUser);
                txtMsg.setText(message.getText());
                Picasso.get().load(message.getFromID().equals(FirebaseAuth.getInstance().getUid())
                        ? me.getProfileUrl()
                        : user.getProfileUrl())
                        .into(imgMessage);
        }

        @Override
        public int getLayout() {

            return message.getFromID().equals(FirebaseAuth.getInstance().getUid()) ? R.layout.item_from_message : R.layout.item_to_message;
        }
    }
}
