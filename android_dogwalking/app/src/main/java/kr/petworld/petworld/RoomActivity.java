package kr.petworld.petworld;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//산책매칭 페이지
public class RoomActivity extends AppCompatActivity {

    private ListView listView;
    private EditText editText;
    private Button button;

    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> room = new ArrayList<>();
    private DatabaseReference reference = FirebaseDatabase.getInstance()
            .getReference().getRoot();
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        listView = (ListView) findViewById(R.id.list);
        editText = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);

        //채팅방 구성
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, room);

        listView.setAdapter(arrayAdapter);

        listView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);


        //db에 저장된 반려견 이름 가져오기
        createUserName();

        button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put(editText.getText().toString(), "");
                reference.updateChildren(map);
            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override public void onDataChange(DataSnapshot dataSnapshot) {

                Set<String> set = new HashSet<String>();
                Iterator i = dataSnapshot.getChildren().iterator();

                while (i.hasNext()) {
                    set.add(((DataSnapshot) i.next()).getKey());
                }

                room.clear();
                room.addAll(set);

                arrayAdapter.notifyDataSetChanged();
            }

            @Override public void onCancelled(DatabaseError databaseError) {

            }
        });
        //유저이름, 채팅방 이름 넘겨주기
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.putExtra("chat_room_name", ((TextView) view).getText().toString());
                intent.putExtra("chat_user_name", name);
                startActivity(intent);
            }
        });
    }

    private void createUserName() {
        Intent intent = getIntent();
        name = intent.getStringExtra("name");



    }
}
