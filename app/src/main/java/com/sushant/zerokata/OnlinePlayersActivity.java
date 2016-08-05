package com.sushant.zerokata;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class OnlinePlayersActivity extends AppCompatActivity {
    private static final String TAG = "OnlinePlayersActivity";

    //TextView onlineEmail;
    String uId;
    String mail;
    ListView listView;
    ArrayList<String> onlinePlayersArray;
    ArrayList<String> onlineUidArray;
     ArrayAdapter adapter;
    HashMap<String,String> playerHashMap=new HashMap<>();

    DatabaseReference root =FirebaseDatabase.getInstance().getReference().getRoot();
    DatabaseReference child;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_players);

        //onlineEmail= (TextView) findViewById(R.id.tv_online_email);
        uId=getIntent().getStringExtra("uId");
        mail=getIntent().getStringExtra("email");
        //onlineEmail.setText(uId);

        listView= (ListView) findViewById(R.id.lv_online_players);


        onlinePlayersArray = new ArrayList<>();
        onlineUidArray = new ArrayList<>();



        adapter = new ArrayAdapter(this, R.layout.listlayout,onlinePlayersArray);
        listView.setAdapter(adapter);


        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                readOnlinePlayers(dataSnapshot);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        
        root.child(uId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PlayerPojo ppx=dataSnapshot.getValue(PlayerPojo.class);
                if(ppx.getEngaged()!=0){
                    Toast.makeText(OnlinePlayersActivity.this, "Game Begins", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String oppEmail=((TextView)view.findViewById(R.id.text1)).getText().toString();
                onListClick(oppEmail);
            }
        });


    }

    void readOnlinePlayers(DataSnapshot dataSnapshot){
        Iterator it=dataSnapshot.getChildren().iterator();
        onlinePlayersArray.clear();
        onlineUidArray.clear();
        playerHashMap.clear();
        Log.d(TAG, "readOnlinePlayers: array cleared"+onlinePlayersArray.size());
        while(it.hasNext()){
            final String onlineUId=((DataSnapshot) it.next()).getKey();
            onlineUidArray.add(onlineUId);
            child=root.child(onlineUId);
            Log.d(TAG, "onDataChange: uid="+onlineUId);
            child.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot2) {
                    PlayerPojo pp=dataSnapshot2.getValue(PlayerPojo.class);
                    if(!playerHashMap.containsKey(pp.getEmail())) {
                        onlinePlayersArray.add(pp.getEmail());
                        adapter.notifyDataSetChanged();
                    }
                    Log.d(TAG, "onDataChange: email "+pp.getEmail()+" size= "+onlinePlayersArray.size()+"parent="+onlineUId);
                    playerHashMap.put(pp.getEmail(),onlineUId);

                    //Toast.makeText(OnlinePlayersActivity.this, "email"+pp.getEmail(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }//adapter.notifyDataSetChanged();
        Log.d(TAG, "readOnlinePlayers: final array size"+onlinePlayersArray.size());
    }

    void onListClick(String oppEmail){
        //String oppEmail=((TextView)view).getText().toString();
        PlayerPojo oppPlayer=new PlayerPojo(oppEmail,1,1,1,1,mail,playerHashMap.get(mail),-1,-1);
        root.child(playerHashMap.get(oppEmail)).updateChildren(oppPlayer.gameStartMapper());

        PlayerPojo thisPlayer=new PlayerPojo(mail,1,1,2,1,oppEmail,playerHashMap.get(oppEmail),-1,-1);
        root.child(playerHashMap.get(mail)).updateChildren(thisPlayer.gameStartMapper());
    }
}
