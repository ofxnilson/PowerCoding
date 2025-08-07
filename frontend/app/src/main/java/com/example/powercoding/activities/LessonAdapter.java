package com.example.powercoding.activities;

import android.content.Context;
import android.view.*;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.powercoding.R;
import com.example.powercoding.models.Lesson;

import java.util.List;

public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.LessonViewHolder> {

    private static final Object ACTIVE = null;
    private List<Lesson> lessonList;
    private Context context;
    private OnLessonClickListener clickListener;
    private static final String PREF_NAME = "powerCodingPrefs";

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
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_lesson_bubble, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        Lesson lesson = lessonList.get(position);

        // Reset views to default state
        holder.lessonIcon.setAlpha(1.0f);
        holder.lockIcon.setVisibility(View.GONE);

        // SPECIAL HANDLING FOR TRIANGLE BUBBLE (position 5)
        if (position == 5) {
            configureTriangleBubble(holder, lesson);
        } else {
            configureRegularBubble(holder, lesson);
        }

        // Set title (for all bubbles)
        holder.lessonTitle.setText(lesson.getTitle());

        // Left-to-right zig-zag positioning
        boolean isLeft = (position % 2 == 0);
        holder.leftSpace.setVisibility(isLeft ? View.VISIBLE : View.GONE);
        holder.rightSpace.setVisibility(isLeft ? View.GONE : View.VISIBLE);

        // Click handling
        holder.itemView.setOnClickListener(v -> handleLessonClick(position, lesson));
    }

    private void configureTriangleBubble(LessonViewHolder holder, Lesson lesson) {
        // Special triangle background
        holder.lessonIconBg.setImageResource(R.drawable.ic_locked_triangle);

        // Set triangle icon based on state
        int triangleIcon = lesson.getState() == Lesson.LessonState.LOCKED ?
                R.drawable.ic_locked_triangle : R.drawable.ic_unlocked_triangle;
        holder.lessonIcon.setImageResource(triangleIcon);

        // Adjust lock icon
        if (lesson.getState() == Lesson.LessonState.LOCKED) {
            holder.lockIcon.setVisibility(View.VISIBLE);
            holder.lessonIcon.setAlpha(0.5f);
        }
    }

    private void configureRegularBubble(LessonViewHolder holder, Lesson lesson) {
        // Set bubble appearance based on state
        switch (lesson.getState()) {
            case COMPLETED:
                holder.lessonIconBg.setImageResource(R.drawable.bubble_background_completed);
                break;
            case ACTIVE:
                holder.lessonIconBg.setImageResource(R.drawable.bubble_background_active);
                break;
            case LOCKED:
                holder.lessonIconBg.setImageResource(R.drawable.bubble_background_locked);
                holder.lockIcon.setVisibility(View.VISIBLE);
                holder.lessonIcon.setAlpha(0.5f);
                break;
        }
        holder.lessonIcon.setImageResource(lesson.getIconResId());
    }

    private void handleLessonClick(int position, Lesson lesson) {
        if (lesson.getState() != Lesson.LessonState.LOCKED) {
            clickListener.onLessonClick(position, lesson);
        } else {
            String message = position == 5 ?
                    "Complete all 5 main lessons first!" :
                    "Complete lesson " + position + " first!";
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return lessonList.size();
    }

    static class LessonViewHolder extends RecyclerView.ViewHolder {
        FrameLayout bubbleFrame;
        ImageView  lessonIconBg, lessonIcon, lockIcon;
        TextView   lessonTitle;
        Space      leftSpace, rightSpace;

        LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            bubbleFrame    = itemView.findViewById(R.id.bubbleFrame);
            lessonIconBg   = itemView.findViewById(R.id.lessonIconBg);
            lessonIcon     = itemView.findViewById(R.id.lessonIcon);
            lockIcon       = itemView.findViewById(R.id.lockIcon);
            lessonTitle    = itemView.findViewById(R.id.lessonTitle);
            leftSpace      = itemView.findViewById(R.id.leftSpace);
            rightSpace     = itemView.findViewById(R.id.rightSpace);
        }
    }
}

