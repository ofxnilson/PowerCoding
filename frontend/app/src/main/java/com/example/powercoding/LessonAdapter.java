package com.example.powercoding;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.*;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.powercoding.models.Lesson;

import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private List<Lesson> lessonList;
    private Context context;
    private OnLessonClickListener clickListener;

    public interface OnLessonClickListener {
        void onLessonClick(int position, Lesson lesson);
    }

    public LessonAdapter(Context context, List<Lesson> lessonList, OnLessonClickListener clickListener) {
        this.context = context;
        this.lessonList = lessonList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_lesson_bubble, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessonList.get(position);
        holder.lessonIcon.setImageResource(lesson.getIconResId());
        holder.lessonTitle.setText(lesson.getTitle());

        // Stagger bubbles: alternate left/right by showing/hiding spacers
        if (position % 2 == 0) {
            // Left bubble
            holder.leftSpace.setVisibility(View.GONE);
            holder.rightSpace.setVisibility(View.VISIBLE);
        } else {
            // Right bubble
            holder.leftSpace.setVisibility(View.VISIBLE);
            holder.rightSpace.setVisibility(View.GONE);
        }

        // State: set color, lock overlay, etc.
        switch (lesson.getState()) {
            case ACTIVE:
                holder.lessonIcon.setColorFilter(null);
                holder.lessonIcon.setBackgroundResource(R.drawable.bubble_background_active); // green
                holder.lockIcon.setVisibility(View.GONE);
                break;
            case COMPLETED:
                holder.lessonIcon.setColorFilter(null);
                holder.lessonIcon.setBackgroundResource(R.drawable.bubble_background_completed); // blue or gold
                holder.lockIcon.setVisibility(View.GONE);
                break;
            case LOCKED:
                holder.lessonIcon.setColorFilter(0xFFCCCCCC, PorterDuff.Mode.SRC_ATOP); // gray out
                holder.lessonIcon.setBackgroundResource(R.drawable.bubble_background_locked); // gray
                holder.lockIcon.setVisibility(View.VISIBLE);
                break;
        }

        holder.itemView.setOnClickListener(v -> {
            if (lesson.getState() == Lesson.LessonState.ACTIVE || lesson.getState() == Lesson.LessonState.COMPLETED) {
                clickListener.onLessonClick(position, lesson);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lessonList.size();
    }

    static class LessonViewHolder extends RecyclerView.ViewHolder {
        LinearLayout bubbleContainer;
        ImageView lessonIcon, lockIcon;
        TextView lessonTitle;
        Space leftSpace, rightSpace; // <--- declare here

        LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            bubbleContainer = itemView.findViewById(R.id.bubbleContainer);
            lessonIcon = itemView.findViewById(R.id.lessonIcon);
            lockIcon = itemView.findViewById(R.id.lockIcon);
            lessonTitle = itemView.findViewById(R.id.lessonTitle);
            leftSpace = itemView.findViewById(R.id.leftSpace);
            rightSpace = itemView.findViewById(R.id.rightSpace);
        }
    }

}



