package com.B.carrasco.burgerapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.B.carrasco.burgerapp.R;
import com.B.carrasco.burgerapp.models.Order;
import java.util.List;

public class OrderAdapter extends ArrayAdapter<Order> {
    private Context context;
    private List<Order> orders;
    private OnReorderClickListener onReorderClickListener;

    public interface OnReorderClickListener {
        void onReorderClick(Order order);
    }

    public void setOnReorderClickListener(OnReorderClickListener listener) {
        this.onReorderClickListener = listener;
    }

    public OrderAdapter(Context context, List<Order> orders) {
        super(context, R.layout.item_order, orders);
        this.context = context;
        this.orders = orders;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
            holder = new ViewHolder();
            holder.tvOrderNumber = convertView.findViewById(R.id.tvOrderNumber);
            holder.tvOrderDate = convertView.findViewById(R.id.tvOrderDate);
            holder.tvOrderTotal = convertView.findViewById(R.id.tvOrderTotal);
            holder.tvOrderStatus = convertView.findViewById(R.id.tvOrderStatus);
            holder.tvBurgerName = convertView.findViewById(R.id.tvBurgerName);
            holder.btnReorder = convertView.findViewById(R.id.btnReorder);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Order order = orders.get(position);

        // Configurar datos
        holder.tvOrderNumber.setText("Pedido #" + (position + 1));
        holder.tvOrderDate.setText(order.getFormattedDate());
        holder.tvOrderTotal.setText(order.getFormattedAmount());
        holder.tvBurgerName.setText(order.getBurgerName());
        holder.tvOrderStatus.setText(order.getStatus());

        // Color según estado
        switch (order.getStatus().toLowerCase()) {
            case "entregado":
                holder.tvOrderStatus.setTextColor(Color.parseColor("#4CAF50"));
                holder.btnReorder.setVisibility(View.VISIBLE);
                break;
            case "pendiente":
                holder.tvOrderStatus.setTextColor(Color.parseColor("#2196F3"));
                holder.btnReorder.setVisibility(View.GONE);
                break;
            case "cancelado":
                holder.tvOrderStatus.setTextColor(Color.parseColor("#FF5722"));
                holder.btnReorder.setVisibility(View.GONE);
                break;
            default:
                holder.tvOrderStatus.setTextColor(Color.parseColor("#9E9E9E"));
                holder.btnReorder.setVisibility(View.GONE);
        }

        // Configurar botón de repetir
        holder.btnReorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onReorderClickListener != null) {
                    onReorderClickListener.onReorderClick(order);
                }
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView tvOrderNumber;
        TextView tvOrderDate;
        TextView tvOrderTotal;
        TextView tvOrderStatus;
        TextView tvBurgerName;
        Button btnReorder;
    }
}