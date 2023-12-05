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
import com.app.studentinformationmanagement.models.Student;

import java.util.ArrayList;
import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private List<Student> students;
    private SparseBooleanArray selectedItems = new SparseBooleanArray();
    private boolean isActionModeEnabled = false;
    private StudentItemClickListener listener;
    private List<Student> allStudents;

    public StudentAdapter(List<Student> students, StudentItemClickListener listener) {
        this.students = students;
        this.listener = listener;
        this.allStudents = new ArrayList<>(students);
    }

    @Override
    public StudentViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.student_list_item, parent, false);
        return new StudentViewHolder(itemView);
    }

    public void filterList(String query) {
        query = query.toLowerCase();
        students.clear();

        if (query.isEmpty()) {
            students.addAll(allStudents);
        } else {
            for (Student student : allStudents) {
                if (student.getName().toLowerCase().contains(query)) {
                    students.add(student);
                }
            }
        }
    }

    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Student> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(allStudents);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (Student student : allStudents) {
                        if (student.getName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(student);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                students.clear();
                students.addAll((List<Student>) results.values);
                notifyDataSetChanged();
            }
        };
    }


    public Student getItem(int position) {
        return students.get(position);
    }

    public interface StudentItemClickListener {
        void onItemClicked(Student student);
        void onMoreOptionsClicked(View view, int position);
        void onItemLongClicked(int position);
    }

    @Override
    public void onBindViewHolder(StudentViewHolder holder, int position) {
        Student student = students.get(position);
        boolean isSelected = selectedItems.get(position, false);
        holder.bind(student, isSelected, position, listener, isActionModeEnabled);
    }


    @Override
    public int getItemCount() {
        return students.size();
    }

    // Custom ViewHolder
    public static class StudentViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        public TextView studentName;
        private boolean isSelected = false;
        CardView cardView;
        ImageView moreOptionsIcon;
        LinearLayout llStudentListItem;

        public StudentViewHolder(View view) {
            super(view);
            studentName = view.findViewById(R.id.studentName);
            moreOptionsIcon = view.findViewById(R.id.moreOptionsIcon);
            cardView = view.findViewById(R.id.cardView);
            llStudentListItem = view.findViewById(R.id.llStudentListItem);
        }

        public void bind(Student student, boolean isSelected, int position, StudentItemClickListener listener, boolean isActionModeEnabled) {
            studentName.setText(student.getName());
            cardView.setCardBackgroundColor(isActionModeEnabled && isSelected ? Color.LTGRAY : Color.DKGRAY);

            moreOptionsIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onMoreOptionsClicked(v, position);
                }
            });

            llStudentListItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isActionModeEnabled) {
                        listener.onItemClicked(student);
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

    public String getStudentId(int position) {
        return students.get(position).getId();
    }

    public List<Integer> getSelectedItemsPositions() {
        List<Integer> items = new ArrayList<>();
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public void deleteItem(int position) {
        students.remove(position);
        notifyItemRemoved(position);
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public void updateData(List<Student> students) {
        this.students = students;
        this.allStudents = new ArrayList<>(students);
        notifyDataSetChanged();
    }

}
