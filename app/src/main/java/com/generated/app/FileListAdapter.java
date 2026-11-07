package com.generated.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class FileListAdapter extends BaseAdapter {

    public interface OnItemActionListener {
        void onMoreOptionsClicked(FileItem item);
        void onItemSelectionChanged();
    }

    private final Context context;
    private final List<FileItem> itemList;
    private boolean selectionMode = false;
    private OnItemActionListener listener;

    public FileListAdapter(Context context, List<FileItem> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.listener = listener;
    }

    public void setSelectionMode(boolean selectionMode) {
        this.selectionMode = selectionMode;
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public FileItem getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_file, parent, false);
            holder = new ViewHolder();
            holder.cbSelect = convertView.findViewById(R.id.cb_select);
            holder.ivIcon = convertView.findViewById(R.id.iv_icon);
            holder.tvName = convertView.findViewById(R.id.tv_name);
            holder.tvDetails = convertView.findViewById(R.id.tv_details);
            holder.btnMore = convertView.findViewById(R.id.btn_more);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final FileItem item = getItem(position);
        holder.tvName.setText(item.getName());

        if (item.isDirectory()) {
            holder.ivIcon.setImageResource(R.drawable.ic_folder);
            holder.tvDetails.setText("תיקייה | " + FileUtils.formatDate(item.lastModified()));
        } else {
            holder.tvDetails.setText(FileUtils.formatSize(item.length()) + " | " + FileUtils.formatDate(item.lastModified()));
            switch (item.getCategory()) {
                case IMAGE:
                    holder.ivIcon.setImageResource(R.drawable.ic_image);
                    break;
                case VIDEO:
                    holder.ivIcon.setImageResource(R.drawable.ic_video);
                    break;
                case AUDIO:
                    holder.ivIcon.setImageResource(R.drawable.ic_audio);
                    break;
                case PDF:
                    holder.ivIcon.setImageResource(R.drawable.ic_pdf);
                    break;
                case ARCHIVE:
                    holder.ivIcon.setImageResource(R.drawable.ic_zip);
                    break;
                case APK:
                    holder.ivIcon.setImageResource(R.drawable.ic_apk);
                    break;
                case CODE:
                    holder.ivIcon.setImageResource(R.drawable.ic_code);
                    break;
                default:
                    holder.ivIcon.setImageResource(R.drawable.ic_file);
                    break;
            }
        }

        holder.cbSelect.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
        holder.cbSelect.setChecked(item.isSelected());

        holder.cbSelect.setOnClickListener(v -> {
            item.setSelected(holder.cbSelect.isChecked());
            if (listener != null) listener.onItemSelectionChanged();
        });

        holder.btnMore.setOnClickListener(v -> {
            if (listener != null) listener.onMoreOptionsClicked(item);
        });

        return convertView;
    }

    private static class ViewHolder {
        CheckBox cbSelect;
        ImageView ivIcon;
        TextView tvName;
        TextView tvDetails;
        ImageButton btnMore;
    }
}
