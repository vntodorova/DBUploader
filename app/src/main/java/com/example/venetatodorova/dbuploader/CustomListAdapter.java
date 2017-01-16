package com.example.venetatodorova.dbuploader;


import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomListAdapter extends ArrayAdapter<FileModel> {

    ArrayList<FileModel> files;

    private class ViewHolder{
        TextView textView;
        CheckBox checkBox;
    }

    CustomListAdapter(Context context, ArrayList<FileModel> files) {
        super(context, 0, files);
        this.files = files;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.file_name);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
            convertView.setTag(holder);

            holder.checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CheckBox cb = (CheckBox) view;
                    FileModel file = (FileModel) cb.getTag();
                    file.setChecked(cb.isChecked());
                }
            });
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FileModel file = files.get(position);
        holder.textView.setText(file.getName());
        holder.checkBox.setChecked(file.setChecked());
        holder.checkBox.setTag(file);

        return convertView;
    }
}
