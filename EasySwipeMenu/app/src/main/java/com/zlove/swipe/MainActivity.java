package com.zlove.swipe;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private SwipeAdapter adapter;
    private List<String> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.list);
        adapter = new SwipeAdapter(this, data);
        listView.setAdapter(adapter);
        initData();
    }

    private void initData() {
        for (int i = 0; i < 30; i++) {
            String sss = "item ---> " + i;
            data.add(sss);
        }
        adapter.notifyDataSetChanged();
    }

}
