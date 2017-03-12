package io.github.varunj.sangoshthi_broadcaster;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Varun on 04-03-2017.
 */

public class ListGroupsFragment extends Fragment {
    ArrayList<HashMap<String,String>> groupNames;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.activity_list_groups, null);
        ListView listView = (ListView)v.findViewById(R.id.listView);
        groupNames = new ArrayList<>();

        // xxx: change it: add group names to ArrayList
        HashMap<String, String> temp = new HashMap<>();
        temp.put("groupName", "dummyGroup1");
        groupNames.add(temp);
        HashMap<String, String> temp2 = new HashMap<>();
        temp2.put("groupName", "dummyGroup2");
        groupNames.add(temp2);

        ListAdapter adapter = new SimpleAdapter( getActivity(), groupNames,
                R.layout.list_layout_group, new String[] {
                "groupName"}, new int[] {
                R.id.groupname}
        );

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
                Intent i = new Intent(getActivity(), GroupVideoActivity.class);
                String GroupName = ((TextView) view.findViewById(R.id.groupname)).getText().toString();
                i.putExtra("groupName", GroupName);
                startActivity(i);
            }
        });
        return v;
    }
}
