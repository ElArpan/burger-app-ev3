package com.B.carrasco.burgerapp.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.B.carrasco.burgerapp.R;
import java.util.List;

public class GuideAdapter extends RecyclerView.Adapter<GuideAdapter.GuideViewHolder> {

    private List<GuideItem> guideItems;

    public GuideAdapter(List<GuideItem> guideItems) {
        this.guideItems = guideItems;
    }

    @NonNull
    @Override
    public GuideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_guide_page, parent, false);
        return new GuideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GuideViewHolder holder, int position) {
        GuideItem item = guideItems.get(position);

        // CORRECCIÓN AQUÍ: Usamos .getTitle() y .getDescription()
        holder.tvTitle.setText(item.getTitle());
        holder.tvDescription.setText(item.getDescription());

        // Carga la imagen PNG/JPG correspondiente
        holder.ivImage.setImageResource(item.getImageResId());
    }

    @Override
    public int getItemCount() {
        return guideItems.size();
    }

    static class GuideViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription;
        ImageView ivImage;

        public GuideViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvGuideTitle);
            tvDescription = itemView.findViewById(R.id.tvGuideDescription);
            ivImage = itemView.findViewById(R.id.ivGuideImage);
        }
    }
}