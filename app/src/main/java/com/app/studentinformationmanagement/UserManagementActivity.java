package com.app.studentinformationmanagement;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.app.studentinformationmanagement.adapter.RecyclerItemClickListener;
import com.app.studentinformationmanagement.adapter.UserActionModeCallback;
import com.app.studentinformationmanagement.adapter.UserAdapter;
import com.app.studentinformationmanagement.models.User;
import com.app.studentinformationmanagement.services.UserService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class UserManagementActivity extends AppCompatActivity implements UserAdapter.UserItemClickListener{

    private RecyclerView usersRecyclerView;
    private UserAdapter userAdapter;
    private ActionMode actionMode;
    private UserService userService;
    private ProgressBar loadingIndicator;
    private UserActionModeCallback actionModeCallback;
    private FloatingActionButton fabAddUser;
    private String userRole;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_management);

        userRole = getIntent().getStringExtra("USER_ROLE");

        setupRecyclerView(userRole);
        setupSearchEditText();
        loadUsers();

    }

    private void setupRecyclerView(String userRole) {
        List<User> usersList = new ArrayList<>();
        loadingIndicator = findViewById(R.id.loadingIndicator);
        usersRecyclerView = findViewById(R.id.rvUsers);
        loadingIndicator.setVisibility(View.VISIBLE);
        userAdapter = new UserAdapter(usersList, this);
        usersRecyclerView.setAdapter(userAdapter);
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        fabAddUser = findViewById(R.id.fabAddUser);

        if (userRole.equals("Manager") || userRole.equals("Employee")) {
            fabAddUser.setOnClickListener(view -> {
                Toast.makeText(UserManagementActivity.this, "You do not have permission to add a user", Toast.LENGTH_SHORT).show();
            });
            usersRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, usersRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if (actionMode != null) {
                        toggleSelection(position);
                    }
                }

                @Override
                public void onItemLongClick(View view, int position) {
                    Toast.makeText(UserManagementActivity.this, "You do not have permission to delete a user", Toast.LENGTH_SHORT).show();
                }
            }));
        } else {
            fabAddUser.setOnClickListener(view -> {
                Intent intent = new Intent(UserManagementActivity.this, AddUserActivity.class);
                startActivity(intent);
                finish();
            });
            usersRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, usersRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    if (actionMode != null) {
                        toggleSelection(position);
                    }
                }

                @Override
                public void onItemLongClick(View view, int position) {
                    if (actionMode == null) {
                        actionModeCallback = new UserActionModeCallback(UserManagementActivity.this, userAdapter, actionMode);
                        actionMode = startSupportActionMode(actionModeCallback);
                        userAdapter.setActionModeEnabled(true);
                        toggleSelection(position);
                    }
                }
            }));
        }
    }

    private void setupSearchEditText() {
        EditText etSearchUser = findViewById(R.id.etSearchUser);
        etSearchUser.addTextChangedListener(new UserManagementActivity.CustomTextWatcher());
    }

    private class CustomTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            userAdapter.getFilter().filter(s);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

    private void loadUsers() {
        userService = new UserService();
        actionModeCallback = new UserActionModeCallback(this, userAdapter, actionMode);
        userService.getAllUsers(new UserService.UserListDataCallback() {
            @Override
            public void onDataReceived(List<User> users) {
                userAdapter.updateData(users);
            }

            @Override
            public void onError(Exception e) {
                Log.e("UserManagement", "Error fetching users", e);
            }
        });
        loadingIndicator.setVisibility(View.GONE);
    }

    private void toggleSelection(int position) {
        userAdapter.toggleSelection(position);
        int selectedCount = userAdapter.getSelectedItemCount();

        if (selectedCount == 0 && actionMode != null) {
            actionMode.finish();
        } else if (actionMode != null) {
            actionMode.setTitle(selectedCount + " selected");
        }
    }

    @Override
    public void onItemClicked(User user) {
        Intent intent = new Intent(UserManagementActivity.this, UserDetailActivity.class);
        intent.putExtra("USER_ID", user.getId());
        intent.putExtra("USER_ROLE", userRole);
        startActivity(intent);
    }

    @Override
    public void onMoreOptionsClicked(View view, int position) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.options_menu);

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (item.getItemId() == R.id.delete) {
                if (userRole.equals("Manager") || userRole.equals("Employee")) {
                    Toast.makeText(UserManagementActivity.this, "You do not have permission to delete a user", Toast.LENGTH_SHORT).show();
                    return false;
                }
                confirmAndDeleteItem(position);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void confirmAndDeleteItem(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    String userId = userAdapter.getUserId(position);
                    deleteUserFromFirebase(userId, position);
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteUserFromFirebase(String userId, int position) {
        userService.deleteUser(userId, new UserService.UserDeleteCallback() {
            @Override
            public void onSuccess() {
                userAdapter.deleteItem(position);
                Toast.makeText(UserManagementActivity.this, "User deleted successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(Exception e) {
                Log.e("UserDelete", "Error deleting user", e);
                Toast.makeText(UserManagementActivity.this, "Error deleting user: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onItemLongClicked(int position) {
        if (userRole.equals("Manager") || userRole.equals("Employee")) {
            Toast.makeText(UserManagementActivity.this, "You do not have permission to delete a user", Toast.LENGTH_SHORT).show();
        }else{
            if (actionMode == null) {
                actionMode = startSupportActionMode(actionModeCallback);
            }
            userAdapter.toggleSelection(position);
        }

    }

    public  void confirmAndDeleteSelectedUsers() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Selected Users")
                .setMessage("Are you sure you want to delete these users?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteSelectedUsers())
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteSelectedUsers() {
        List<Integer> selectedItemPositions = userAdapter.getSelectedItemsPositions();
        for (int position : selectedItemPositions) {
            String userId = userAdapter.getUserId(position);
            deleteUserFromFirebase(userId, position);
        }

        actionMode.finish();
    }

    public void onActionModeFinished() {
        actionMode = null;
    }

}