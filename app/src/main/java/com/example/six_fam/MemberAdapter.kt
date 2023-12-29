package com.example.six_fam


import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MemberAdapter(private val listMembers: List<MemberModel>) : RecyclerView.Adapter<MemberAdapter.viewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberAdapter.viewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val item = inflater.inflate(R.layout.item_member,parent,false)
        return viewHolder(item)
    }

    override fun onBindViewHolder(holder: MemberAdapter.viewHolder, position: Int) {

        val item = listMembers[position]
        holder.name.text=item.name
        holder.battery.text= item.battery.toString()+"%"
        holder.loaction.text=item.location
        holder.time.text=item.time
        // Load image using Glide (assuming you have a URL for the image)
        Glide.with(holder.image.context)
            .load(item.imageUrl)
            .placeholder(R.drawable.ic_profile)
            .circleCrop()
            .into(holder.image)

    }

    override fun getItemCount(): Int {
        return listMembers.size
    }

    class viewHolder(val item:View): RecyclerView.ViewHolder(item) {
        val name = item.findViewById<TextView>(R.id.user_name)
        val battery = item.findViewById<TextView>(R.id.battery_percent)
        val image = item.findViewById<ImageView>(R.id.img_user)
        val loaction = item.findViewById<TextView>(R.id.address)
        val time = item.findViewById<TextView>(R.id.time)
    }
}
