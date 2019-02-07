package com.licenta.tracker.helper;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.licenta.tracker.R;
import com.licenta.tracker.app.RouteDetails;

import java.util.List;

public class CustomListAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private Activity activity;
    private List<RouteDetails> activityDetailsList;

    public CustomListAdapter(Activity activity, List<RouteDetails> activityDetailsList){
        this.activity = activity;
        this.activityDetailsList = activityDetailsList;
    }


    @Override
    public int getCount() {
        return activityDetailsList.size();
    }

    @Override
    public Object getItem(int position) {
        return activityDetailsList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(layoutInflater ==null){
            layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if(convertView == null){
            convertView = layoutInflater.inflate(R.layout.listview_row, null);
        }

        TextView txtActivityName= (TextView) convertView.findViewById(R.id.txtActivityName);
        TextView txtTotalDistance = (TextView) convertView.findViewById(R.id.txtTotalDistance);
        TextView txtTotalTime = (TextView) convertView.findViewById(R.id.txtTotalTime);

        RouteDetails r = activityDetailsList.get(position);

        txtActivityName.setText(r.getActivityName());
        txtTotalDistance.setText(r.getTotalDistance() + " KM");
        txtTotalTime.setText(r.getTotaltime() + " s");

        return convertView;
    }
}
