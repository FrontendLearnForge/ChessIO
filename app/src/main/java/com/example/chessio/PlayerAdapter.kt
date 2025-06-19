package com.example.chessio
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//
//class PlayerAdapter(private var players: List<Player>) : RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder>() {
//
//    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val playerName: TextView = itemView.findViewById(R.id.player_name)
////        val playerAddress: TextView = itemView.findViewById(R.id.player_address)
//        val playerTeam: TextView = itemView.findViewById(R.id.player_team)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player, parent, false)
//        return PlayerViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
//        val player = players[position]
//        holder.playerName.text = player.fullName // ФИО
////        holder.playerAddress.text = player.address // Адрес
//        holder.playerTeam.text = player.teamName // Команда
//    }
//
//    override fun getItemCount() = players.size
//
//    fun updatePlayers(newPlayers: List<Player>) {
//        players = newPlayers
//        notifyDataSetChanged()
//    }
//}
