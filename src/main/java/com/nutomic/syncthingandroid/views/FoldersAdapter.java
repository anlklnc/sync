package com.nutomic.syncthingandroid.views;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.nutomic.syncthingandroid.BuildConfig;
import com.nutomic.syncthingandroid.R;
import com.nutomic.syncthingandroid.databinding.ItemFolderListBinding;
import com.nutomic.syncthingandroid.model.Folder;
import com.nutomic.syncthingandroid.model.Model;
import com.nutomic.syncthingandroid.service.RestApi;
import com.nutomic.syncthingandroid.util.Util;

import java.io.File;
import java.util.HashMap;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Generates item views for folder items.
 */
public class FoldersAdapter extends ArrayAdapter<Folder> {

    private final HashMap<String, Model> mModels = new HashMap<>();

    public FoldersAdapter(Context context) {
        super(context, 0);
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ItemFolderListBinding binding = (convertView == null)
                ? DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.item_folder_list, parent, false)
                : DataBindingUtil.bind(convertView);

        Folder folder = getItem(position);
        Model model = mModels.get(folder.id);
        binding.label.setText(TextUtils.isEmpty(folder.label) ? folder.id : folder.label);
        binding.state.setTextColor(ContextCompat.getColor(getContext(), R.color.text_green));
        binding.directory.setText(folder.path);
        binding.openFolder.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(folder.path)), "resource/folder");
            try {
                getContext().startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getContext(), R.string.toast_no_file_manager, Toast.LENGTH_SHORT).show();
            }
        });

        if (model != null) {
            int percentage = (model.localBytes != 0)
                    ? Math.round(100 * model.inSyncBytes / model.localBytes)
                    : 100;
            binding.state.setText(getLocalizedState(getContext(), model.state, percentage));
            binding.items.setVisibility(VISIBLE);
            binding.items.setText(getContext()
                    .getString(R.string.files, model.inSyncFiles, model.localFiles));
            binding.size.setVisibility(VISIBLE);
            binding.size.setText(getContext().getString(R.string.folder_size_format,
                    Util.readableFileSize(getContext(), model.inSyncBytes),
                    Util.readableFileSize(getContext(), model.localBytes)));
            setTextOrHide(binding.invalid, model.invalid);
        } else {
            binding.items.setVisibility(GONE);
            binding.size.setVisibility(GONE);
            setTextOrHide(binding.invalid, folder.invalid);
        }

        return binding.getRoot();
    }

    /**
     * Returns the folder's state as a localized string.
     */
    public static String getLocalizedState(Context c, String state, int percentage) {
        switch (state) {
            case "idle":     return c.getString(R.string.state_idle);
            case "scanning": return c.getString(R.string.state_scanning);
            case "cleaning": return c.getString(R.string.state_cleaning);
            case "syncing":  return c.getString(R.string.state_syncing, percentage);
            case "error":    return c.getString(R.string.state_error);
            case "unknown":  // Fallthrough
            case "":         return c.getString(R.string.state_unknown);
        }
        if (BuildConfig.DEBUG) {
            throw new AssertionError("Unexpected folder state " + state);
        }
        return "";
    }

    /**
     * Requests updated model info from the api for all visible items.
     */
    public void updateModel(RestApi api) {
        for (int i = 0; i < getCount(); i++) {
            api.getModel(getItem(i).id, this::onReceiveModel);
        }
    }

    public void onReceiveModel(String folderId, Model model) {
        mModels.put(folderId, model);
        notifyDataSetChanged();
    }

    private void setTextOrHide(TextView view, String text) {
        if (TextUtils.isEmpty(text)) {
            view.setVisibility(GONE);
        } else {
            view.setText(text);
            view.setVisibility(VISIBLE);
        }
    }

}
