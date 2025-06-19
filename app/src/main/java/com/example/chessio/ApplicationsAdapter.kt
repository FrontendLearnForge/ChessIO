package com.example.chessio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

class ApplicationsAdapter(
    private val onStatusChanged: (Application, Boolean) -> Unit,
    private val isTeamTournament: Boolean,
    private val onUserClicked: (String) -> Unit
) : RecyclerView.Adapter<ApplicationsAdapter.ViewHolder>() {

    private val applications = mutableListOf<Application>()

    fun submitList(newList: List<Application>) {
        val diffResult = DiffUtil.calculateDiff(ApplicationDiffCallback(applications, newList))
        applications.clear()
        applications.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userName: TextView = view.findViewById(R.id.text_user_name)
        val teamName: TextView = view.findViewById(R.id.text_team_name)
        val rate: TextView = view.findViewById(R.id.text_rate)
        val btnApprove: ImageButton = view.findViewById(R.id.button_approve)
        val btnReject: ImageButton = view.findViewById(R.id.button_reject)
        val userInfoContainer: LinearLayout = view.findViewById(R.id.user_info_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_application, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val application = applications[position]

        holder.userName.text = application.userName
        holder.teamName.text = application.teamName ?: "Личное участие"
        holder.rate.text = "Рейтинг: ${application.rate}"

        if (isTeamTournament && !application.teamName.isNullOrEmpty()) {
            holder.teamName.text = "Команда: ${application.teamName}"
            holder.teamName.visibility = View.VISIBLE
        } else {
            holder.teamName.visibility = View.GONE
        }

        if (application.status != "На рассмотрении") {
            holder.btnApprove.visibility = View.GONE
            holder.btnReject.visibility = View.GONE
        } else {
            holder.btnApprove.visibility = View.VISIBLE
            holder.btnReject.visibility = View.VISIBLE
        }

        holder.btnApprove.setOnClickListener {
            onStatusChanged(application, true)
        }

        holder.btnReject.setOnClickListener {
            onStatusChanged(application, false)
        }

        holder.userInfoContainer.setOnClickListener {
            onUserClicked(application.userLogin)
        }
    }

    override fun getItemCount() = applications.size

    private class ApplicationDiffCallback(
        private val oldList: List<Application>,
        private val newList: List<Application>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldPos: Int, newPos: Int) =
            oldList[oldPos].id == newList[newPos].id

        override fun areContentsTheSame(oldPos: Int, newPos: Int) =
            oldList[oldPos] == newList[newPos]
    }
}