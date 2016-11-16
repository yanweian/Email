package com.yan.email;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Yan on 2016/11/13.
 */

public class MyAdapter extends BaseAdapter{
    private ArrayList<Itemmail> itemmails;
    private LayoutInflater layoutInflater;

    public MyAdapter(Context context,ArrayList<Itemmail> itemmails) {
        this.itemmails = itemmails;
        layoutInflater=LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return itemmails.size();
    }

    @Override
    public Object getItem(int position) {
        return itemmails.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Viewholder viewholder=null;
        Itemmail itemmail=itemmails.get(position);
        if(convertView==null){
            convertView=layoutInflater.inflate(R.layout.itemlayout,null);
            viewholder=new Viewholder(convertView);
            convertView.setTag(viewholder);
        }else{
            viewholder= (Viewholder) convertView.getTag();
        }
        viewholder.from.setText(itemmail.from);
        viewholder.subject.setText(itemmail.subject);
        viewholder.time.setText(itemmail.time);
        return convertView;
    }

    class Viewholder{
        public TextView from;
        public TextView subject;
        public TextView time;

        public Viewholder(View v) {
            this.from = (TextView) v.findViewById(R.id.from);
            this.subject = (TextView) v.findViewById(R.id.subject);
            this.time = (TextView) v.findViewById(R.id.time);
        }
    }
}
