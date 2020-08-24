package com.example.worktalkie;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class Home extends AppCompatActivity {
    private Button butnChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ChatApplication application = (ChatApplication) getApplication();

        getApplication().registerActivityLifecycleCallbacks(application);

        butnChat = (Button) findViewById(R.id.btnChat);
        eventClicks();
        verifyAuth();
    }

    private void verifyAuth() {
        if(FirebaseAuth.getInstance().getUid() == null){
           Intent i = new Intent(Home.this,MainActivity.class);
           i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK   | Intent.FLAG_ACTIVITY_NEW_TASK);
           startActivity(i);
        }
    }

    public void eventClicks(){
        butnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Home.this, Chat.class);
                startActivity(i);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logoutmenu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.Logout:
                FirebaseAuth.getInstance().signOut();
                verifyAuth();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
