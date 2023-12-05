package com.app.studentinformationmanagement.adapter;

import android.graphics.Color;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.app.studentinformationmanagement.R;
import com.app.studentinformationmanagement.models.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> users;
    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    private boolean isActionModeEnabled = false;
    private UserItemClickListener listener;
    private List<User> allUsers;

    public UserAdapter(List<User> users, UserItemClickListener listener) {
        this.users = users;
        this.listener = listener;
        this.allUsers = new ArrayList<>(users);
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_item, parent, false);
        return new UserViewHolder(itemView);
    }

    public void filterList(String query) {
        query = query.toLowerCase();
        users.clear();

        if (query.isEmpty()) {
            users.addAll(allUsers);
        } else {
            for (User user : allUsers) {
                if (user.getName().toLowerCase().contains(query)) {
                    users.add(user);
                }
            }
        }
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<User> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(allUsers);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (User user : allUsers) {
                        if (user.getName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(user);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                users.clear();
                users.addAll((List<User>) results.values);
                notifyDataSetChanged();
            }
        };
    }


    public User getItem(int position) {
        return users.get(position);
    }

    public interface UserItemClickListener {
        void onItemClicked(User user);
        void onMoreOptionsClicked(View view, int position);
        void onItemLongClicked(int position);
    }

    @Override
    public void onBindViewHolder(UserViewHolder holder, int position) {
        User user = users.get(position);
        boolean isSelected = selectedItems.get(position, false);
        holder.bind(user, isSelected, position, listener, isActionModeEnabled);
    }


    @Override
    public int getItemCount() {
        return users.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public TextView userName;
        private boolean isSelected = false;
        CardView cardView;
        ImageView moreOptionsIcon;
        LinearLayout llUserListItem;

        public UserViewHolder(View view) {
            super(view);
            userName = view.findViewById(R.id.userName);
            moreOptionsIcon = view.findViewById(R.id.userMoreOptionsIcon);
            cardView = view.findViewById(R.id.userCardView);
            llUserListItem = view.findViewById(R.id.llUserListItem);
        }

        public void bind(User user, boolean isSelected, int position, UserItemClickListener listener, boolean isActionModeEnabled) {
            userName.setText(user.getName());
            cardView.setCardBackgroundColor(isActionModeEnabled && isSelected ? Color.LTGRAY : Color.DKGRAY);

            moreOptionsIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onMoreOptionsClicked(v, position);
                }
            });

            llUserListItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isActionModeEnabled) {
                        listener.onItemClicked(user);
                    }
                }
            });

            cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!isActionModeEnabled) {
                        listener.onItemLongClicked(position);
                        return true;
                    }
                    return false;
                }
            });
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add(this.getAdapterPosition(), 101, 0, "Edit");
            menu.add(this.getAdapterPosition(), 102, 1, "Delete");
        }
    }

    public void toggleSelection(int position) {
        if (selectedItems.get(position, false)) {
            selectedItems.delete(position);
        } else {
            selectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    public void setActionModeEnabled(boolean enabled) {
        isActionModeEnabled = enabled;
        if (!enabled) {
            clearSelections();
        }
        notifyDataSetChanged();
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public String getUserId(int position) {
        return users.get(position).getId();
    }

    public List<Integer> getSelectedItemsPositions() {
        List<Integer> items = new ArrayList<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public void deleteItem(int position) {
        users.remove(position);
        notifyItemRemoved(position);
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public void updateData(List<User> users) {
        this.users = users;
        this.allUsers = new ArrayList<>(users);
        notifyDataSetChanged();
    }

}
