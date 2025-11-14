package com.nic.hostelapppanna;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class StudentHomeActivity extends AppCompatActivity {

    private LinearLayout complainLayout;
    private LinearLayout historyLayout;
    private String userId;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        userId = getIntent().getStringExtra("USER_ID");
        phoneNumber = getIntent().getStringExtra("PHONE_NUMBER");

        complainLayout = findViewById(R.id.complain_layout);
        historyLayout = findViewById(R.id.history_layout);

        complainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentHomeActivity.this, ComplaintActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            }
        });

        historyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentHomeActivity.this, StudentHistoryActivity.class);
                intent.putExtra("USER_ID", userId);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra("PHONE_NUMBER", phoneNumber);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}