package com.nutomic.syncthingandroid.kife;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asd on 20.11.2017.
 */

public abstract class BaseArrayAdapter<T> extends ArrayAdapter<T> {

    LayoutInflater inflater;
    int layout;

    public BaseArrayAdapter(@NonNull Context context, int resource, ArrayList<T> items) {
        super(context, resource, items);
        layout = resource;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public BaseArrayAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<T> objects) {
        super(context, resource, textViewResourceId, objects);
        layout = resource;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if(convertView == null) {
            convertView = inflater.inflate(layout, null);
        }
        handleView(convertView, position);
        return convertView;
    }

    public abstract void handleView(View view, int position);
}
