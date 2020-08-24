package com.example.worktalkie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;
import com.xwray.groupie.GroupAdapter;
import com.xwray.groupie.Item;
import com.xwray.groupie.OnItemClickListener;
import com.xwray.groupie.ViewHolder;

import java.util.List;

import javax.annotation.Nullable;

public class ContactsActivity extends AppCompatActivity {

    private GroupAdapter<ViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        RecyclerView rv = findViewById(R.id.recycler);
        adapter = new GroupAdapter<>();
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull Item item, @NonNull View view) {
                Intent i = new Intent(ContactsActivity.this, ConversarActivity.class);
                UserItem userItem = (UserItem) item;

                i.putExtra("user", userItem.user);

                startActivity(i);
            }
        });

        fetchUsers();
    }

    private class UserItem extends Item<ViewHolder> {

        private final User user;

        private UserItem(User user) {
            this.user = user;
        }

        @Override
        public void bind(@NonNull ViewHolder viewHolder, int position) {
            TextView txtUsername = viewHolder.itemView.findViewById(R.id.txt_Username);
            ImageView imgFoto = viewHolder.itemView.findViewById(R.id.imgUser);

            txtUsername.setText(user.getUsername());

            Picasso.get().load(user.getProfileUrl()).into(imgFoto);
        }

        @Override
        public int getLayout() {
            return R.layout.item_user;
        }
    }

    private void fetchUsers(){
        FirebaseFirestore.getInstance().collection("/usuarios")
        .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(e != null){
                    Log.e("Teste",e.getMessage(), e);
                    return;
                }
                List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                adapter.clear();
                for (DocumentSnapshot doc: docs){
                    User user = doc.toObject(User.class);
                    String uid = FirebaseAuth.getInstance().getUid();
                    if(user.getUuid().equals(uid)){
                        continue;
                    }

                    Log.d("Teste",user.getUsername());

                    adapter.add(new UserItem(user));
                }
            }
        });
    }


}
