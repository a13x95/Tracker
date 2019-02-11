package com.licenta.tracker.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.licenta.tracker.R;
import com.licenta.tracker.activity.DisplayActivityDetails;
import com.licenta.tracker.activity.DisplayImage;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Bitmap> bitmapList;
    GridView gridView;

    public ImageAdapter(Context context, ArrayList<Bitmap> bitmapList){
        this.context = context;
        this.bitmapList = bitmapList;
    }

    @Override
    public int getCount() {
        return this.bitmapList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if(convertView == null){
            imageView = new ImageView(this.context);
            imageView.setLayoutParams(new GridView.LayoutParams(115, 115));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            imageView = (ImageView) convertView;
        }
        imageView.setImageBitmap(this.bitmapList.get(position));
        return imageView;
    }
}
