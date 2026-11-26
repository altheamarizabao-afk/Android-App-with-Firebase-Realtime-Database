package com.example.textfieldinputipt;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etFirstInput, etSecondInput;
    private MaterialButton btnSubmit, btnView;
    private ListView listViewEntries;

    private ArrayList<EntryItem> entriesList;
    private SimpleAdapter adapter;
    private DatabaseReference databaseReference;

    private static final String DATABASE_URL =
            "https://text-field-input-ipt-default-rtdb.asia-southeast1.firebasedatabase.app/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left + 40, systemBars.top + 24, systemBars.right + 40, systemBars.bottom + 24);
            return insets;
        });

        etFirstInput = findViewById(R.id.etFirstInput);
        etSecondInput = findViewById(R.id.etSecondInput);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnView = findViewById(R.id.btnView);
        listViewEntries = findViewById(R.id.listViewEntries);

        entriesList = new ArrayList<>();
        adapter = new SimpleAdapter(
                this,
                entriesList,
                R.layout.item_entry,
                new String[]{"firstInput", "secondInput"},
                new int[]{R.id.tvFirstLine, R.id.tvSecondLine}
        );
        listViewEntries.setAdapter(adapter);

        databaseReference = FirebaseDatabase.getInstance(DATABASE_URL).getReference("entries");

        btnSubmit.setOnClickListener(v -> saveRecordToFirebase());
        btnView.setOnClickListener(v -> loadRecordsFromFirebase());
    }

    private void saveRecordToFirebase() {
        String firstText = etFirstInput.getText() != null ? etFirstInput.getText().toString().trim() : "";
        String secondText = etSecondInput.getText() != null ? etSecondInput.getText().toString().trim() : "";

        if (TextUtils.isEmpty(firstText) || TextUtils.isEmpty(secondText)) {
            Toast.makeText(this, "Please fill in both text fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        String recordId = databaseReference.push().getKey();
        if (recordId == null) {
            Toast.makeText(this, "Failed to generate record ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        EntryItem entryData = new EntryItem(firstText, secondText);

        databaseReference.child(recordId).setValue(entryData)
                .addOnSuccessListener(unused -> {
                    etFirstInput.setText("");
                    etSecondInput.setText("");
                    etFirstInput.requestFocus();
                    Toast.makeText(MainActivity.this, "Record saved successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void loadRecordsFromFirebase() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                entriesList.clear();

                if (!snapshot.exists() || snapshot.getChildrenCount() == 0) {
                    adapter.notifyDataSetChanged();
                    Toast.makeText(MainActivity.this, "No saved records found.", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot child : snapshot.getChildren()) {
                    String first = child.child("firstInput").getValue(String.class);
                    String second = child.child("secondInput").getValue(String.class);

                    if (first != null && second != null) {
                        entriesList.add(new EntryItem(first, second));
                    }
                }

                adapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Loaded " + entriesList.size() + " record(s).", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load records: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}