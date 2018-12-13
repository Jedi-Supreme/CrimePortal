package com.jedi_supreme.crimeportal.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.jedi_supreme.crimeportal.R;

import java.util.ArrayList;

public class ice_contact_array_adapter extends ArrayAdapter<String> {

    public ice_contact_array_adapter(@NonNull Context context, ArrayList<String> relations) {
        super(context,R.layout.ice_row_layout, relations);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (convertView == null){
            convertView = inflater.inflate(R.layout.ice_row_layout,parent,false);
        }

        String rel_numb = getItem(position);
        TextView tv_ice_rel = convertView.findViewById(R.id.tv_ice_relation);
        TextView tv_ice_number = convertView.findViewById(R.id.tv_ice_number);

        if (rel_numb != null) {

            String[] rel_arr = rel_numb.split("-");

            tv_ice_rel.setText(rel_arr[0]);
            tv_ice_number.setText(rel_arr[1]);
        }

        return convertView;
    }
}
