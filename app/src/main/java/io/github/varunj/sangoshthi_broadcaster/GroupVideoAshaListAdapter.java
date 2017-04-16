package io.github.varunj.sangoshthi_broadcaster;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Varun Jain on 16-Apr-17.
 */

public class GroupVideoAshaListAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<String> ashalist;
    private final ArrayList<Integer> img1;
    private final ArrayList<Integer> img2;
    private final ArrayList<Integer> img3;

    public GroupVideoAshaListAdapter(Activity context, ArrayList<String> ashalist, ArrayList<Integer> img1, ArrayList<Integer> img2, ArrayList<Integer> img3) {
        super(context, R.layout.groupvideo_ashalist, ashalist);
        this.context = context;
        this.ashalist = ashalist;
        this.img1 = img1;
        this.img2 = img2;
        this.img3 = img3;
    }

    public View getView(int position, View view , ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.groupvideo_ashalist, null, true);

        TextView groupvideo_ashalist_name = (TextView) rowView.findViewById(R.id.groupvideo_ashalist_name);
        ImageView groupvideo_ashalist_online = (ImageView) rowView.findViewById(R.id.groupvideo_ashalist_online);
        ImageView groupvideo_ashalist_query = (ImageView) rowView.findViewById(R.id.groupvideo_ashalist_query);
        ImageView groupvideo_ashalist_mute = (ImageView) rowView.findViewById(R.id.groupvideo_ashalist_mute);

        groupvideo_ashalist_name.setText(ashalist.get(position));
        groupvideo_ashalist_online.setImageResource(img1.get(position));
        groupvideo_ashalist_query.setImageResource(img2.get(position));
        groupvideo_ashalist_mute.setImageResource(img3.get(position));
        return rowView;
    };
}