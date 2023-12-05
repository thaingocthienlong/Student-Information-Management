package com.app.studentinformationmanagement.adapter;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.view.ActionMode;

import com.app.studentinformationmanagement.R;
import com.app.studentinformationmanagement.UserManagementActivity;

public class UserActionModeCallback implements ActionMode.Callback {
    private UserManagementActivity activity;
    private UserAdapter adapter;
    private ActionMode actionMode;

    public UserActionModeCallback(UserManagementActivity activity, UserAdapter adapter, ActionMode actionMode) {
        this.activity = activity;
        this.actionMode = actionMode;
        this.adapter = adapter;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.contextual_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.menu_delete) {
            confirmAndDeleteSelectedUsers();
            return true;
        }
        return false;
    }

    public interface ActionModeCallbackListener {
        void onActionModeFinished();
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        if (adapter != null) {
            adapter.clearSelections();
        }
        adapter.setActionModeEnabled(false);
        activity.onActionModeFinished();
    }


    private void confirmAndDeleteSelectedUsers() {
        activity.confirmAndDeleteSelectedUsers();
    }
}