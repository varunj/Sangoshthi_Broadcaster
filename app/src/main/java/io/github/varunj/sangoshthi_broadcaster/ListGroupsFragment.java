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

import static io.github.varunj.sangoshthi_broadcaster.ListSessionsActivity.dataList;

/**
 * Created by Varun on 04-03-2017.
 */

public class ListGroupsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_list_groups, null);
        ListView listView = (ListView)v.findViewById(R.id.listView);

        ListAdapter adapter = new SimpleAdapter(getActivity(), dataList,
                R.layout.list_layout_group, new String[]{
                "showName", "timeOfAir", "videoName", "ashaList"}, new int[] {
                R.id.showname, R.id.timeofair, R.id.videoname, R.id.ashalist});

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
                Intent i = new Intent(getActivity(), StartShowCallsActivity.class);
                i.putExtra("showName", ((TextView) view.findViewById(R.id.showname)).getText().toString());
                i.putExtra("videoname", ((TextView) view.findViewById(R.id.videoname)).getText().toString());
                i.putExtra("ashalist", ((TextView) view.findViewById(R.id.ashalist)).getText().toString());

//                i.putExtra("timeofair", ((TextView) view.findViewById(R.id.timeofair)).getText().toString());
                startActivity(i);
            }
        });
        return v;
    }
}
