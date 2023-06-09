package com.nitc.projectsgc.mentors.adapters

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.nitc.projectsgc.Appointment
import com.nitc.projectsgc.R
import com.nitc.projectsgc.SharedViewModel
import com.nitc.projectsgc.admin.access.StudentsAccess
import com.nitc.projectsgc.mentors.access.MentorAppointmentsAccess
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

class MentorAppointmentsAdapter(
    var context : Context,
    var appointments : ArrayList<Appointment>,
    var parentFragment: Fragment,
    var sharedViewModel: SharedViewModel
) : RecyclerView.Adapter<MentorAppointmentsAdapter.MentorAppointmentsViewHolder>() {
    class MentorAppointmentsViewHolder(itemView : View):RecyclerView.ViewHolder(itemView){
        var nameOfTheStudent = itemView.findViewById<TextView>(R.id.nameInMentorAppointmentsCard)
        var genderPic = itemView.findViewById<CircleImageView>(R.id.imageInMentorAppointmentsCard)
        var dobText = itemView.findViewById<TextView>(R.id.dobInMentorAppointmentsCard)
        var phoneText = itemView.findViewById<TextView>(R.id.phoneInMentorAppointmentsCard)
        var rollText = itemView.findViewById<TextView>(R.id.rollNoInMentorAppointmentsCard)
        var timeText = itemView.findViewById<TextView>(R.id.timeTextInMentorAppointmentsCard)
        var viewPastRecord = itemView.findViewById<Button>(R.id.viewPastRecordButtonInMentorAppointmentsCard)
        var cancelButton = itemView.findViewById<Button>(R.id.cancelButtonInMentorAppointmentsCard)
        var statusCard = itemView.findViewById<CardView>(R.id.statusCardInMentorAppointmentsCard)
        var statusText = itemView.findViewById<TextView>(R.id.statusTextInMentorAppointmentsCard)
        var remarksLayout = itemView.findViewById<ConstraintLayout>(R.id.remarksLayoutInMentorAppointmentsCard)
        var reactLayout = itemView.findViewById<ConstraintLayout>(R.id.reactLayoutInMentorAppointmentsCard)
        var completeButton = itemView.findViewById<Button>(R.id.completedButtonInMentorAppointmentsCard)
        var dontCancel = itemView.findViewById<Button>(R.id.dontCancelButtonInMentorAppointmentsCard)
        var submitRemarks = itemView.findViewById<Button>(R.id.submitRemarksButtonInMentorAppointmentsCard)
        var submitCancelNote = itemView.findViewById<Button>(R.id.submitCancelNoteButtonInMentorAppointmentsCard)
        var remarksInput = itemView.findViewById<EditText>(R.id.remarksInputInMentorAppointmentsCard)
        var cancelNoteInput = itemView.findViewById<EditText>(R.id.cancelNoteInputInMentorAppointmentsCard)
        var cancelLayout = itemView.findViewById<ConstraintLayout>(R.id.cancelLayoutInMentorAppointmentsCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentorAppointmentsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.mentor_appointments_card,parent,false)
        return MentorAppointmentsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return appointments.size
    }

    override fun onBindViewHolder(holder: MentorAppointmentsViewHolder, position: Int) {

        holder.nameOfTheStudent.text = appointments[position].studentName
        holder.timeText.text = appointments[position].timeSlot
        holder.statusCard.visibility = View.VISIBLE
        holder.statusText.text = appointments[position].status
        if(appointments[position].rescheduled){
            holder.cancelButton.visibility = View.GONE
            holder.viewPastRecord.visibility = View.GONE
            holder.completeButton.visibility = View.GONE
            holder.remarksLayout.visibility = View.GONE
        }
        if(appointments[position].cancelled) {
            holder.cancelButton.visibility = View.GONE
            holder.viewPastRecord.visibility = View.GONE
            holder.completeButton.visibility = View.GONE
            holder.remarksLayout.visibility = View.VISIBLE
        }
        if(appointments[position].completed){
            holder.reactLayout.visibility = View.GONE
            holder.remarksLayout.visibility = View.VISIBLE
            holder.remarksInput.setText(appointments[position].remarks)
            holder.remarksInput.isEnabled = false
            holder.submitRemarks.visibility = View.GONE
            holder.completeButton.visibility = View.GONE
        }

        var stdId = appointments[position].studentID.toString()

        var coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            var student = StudentsAccess(context, parentFragment).getStudent(stdId)
            coroutineScope.cancel()
            if (student != null) {
                holder.nameOfTheStudent.text = student.name.toString()
                holder.dobText.text = student.dateOfBirth.toString()
                holder.phoneText.text = student.phoneNumber.toString()
                holder.rollText.text = student.rollNo.toString()
                if (student.gender == "Male") {
                    holder.genderPic.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            parentFragment.resources,
                            R.drawable.boy_face,
                            null
                        )
                    )
                } else {
                    holder.genderPic.setImageDrawable(
                        ResourcesCompat.getDrawable(
                            parentFragment.resources,
                            R.drawable.girl_face,
                            null
                        )
                    )
                }
            }
        }

        holder.cancelButton.setOnClickListener {

            holder.cancelLayout.visibility = View.VISIBLE
            holder.reactLayout.visibility = View.GONE
            holder.dontCancel.visibility = View.VISIBLE
            holder.completeButton.visibility = View.GONE
            holder.submitCancelNote.setOnClickListener {

                var confirmDeleteBuilder = AlertDialog.Builder(context)
                confirmDeleteBuilder.setTitle("Are you sure ?")
                    .setMessage("You want to cancel this appointment?")
                    .setPositiveButton("Yes") { dialog, which ->
                        dialog.dismiss()
                        var cancelNoteInput = holder.cancelNoteInput.text.toString()
                        if (cancelNoteInput.isEmpty()) {
                            Toast.makeText(
                                context,
                                "Add some cancel note first",
                                Toast.LENGTH_SHORT
                            ).show()

                        } else {
                            var cancelAppointment = appointments[position]
                            cancelAppointment.cancelled = true
                            cancelAppointment.status =
                                "Cancelled by mentor" + "\n" + cancelNoteInput
                            var cancelLive = MentorAppointmentsAccess(
                                context,
                                sharedViewModel = sharedViewModel
                            ).cancelAppointment(appointment = cancelAppointment)
                            if (cancelLive != null) {
                                cancelLive.observe(parentFragment.viewLifecycleOwner) { cancelSuccess ->
                                    if (cancelSuccess) {
                                        val studentCoroutineScope = CoroutineScope(Dispatchers.Main)
                                        studentCoroutineScope.launch {
                                            val student =
                                                StudentsAccess(context, parentFragment).getStudent(
                                                    stdId
                                                )
                                            if(student != null) {
                                                val message =
                                                    RemoteMessage.Builder(student.fcmToken)
                                                        .setMessageId(UUID.randomUUID().toString())
                                                        .setData(
                                                            mapOf(
                                                                "title" to "Appointment Cancelled",
                                                                "body" to "Appointment with ${sharedViewModel.currentMentor.name} on ${appointments[position].date} is cancelled"
                                                            )
                                                        )
                                                        .build()
                                                FirebaseMessaging.getInstance().send(message)
                                                Log.d("messagePush","worked")
                                            }
                                            studentCoroutineScope.cancel()
                                        }
                                        Toast.makeText(context, "Cancelled", Toast.LENGTH_SHORT)
                                            .show()
                                        appointments[position] = cancelAppointment
                                        notifyItemChanged(position)
                                    }
                                }
                            }
                        }
                    }
                    .setNegativeButton("No"){dialog,which->
                        dialog.dismiss()
                    }
                    confirmDeleteBuilder.create().show()

            }
        }
        holder.dontCancel.setOnClickListener {
            holder.dontCancel.visibility = View.GONE
            holder.cancelLayout.visibility = View.GONE
            holder.reactLayout.visibility = View.VISIBLE
            holder.completeButton.visibility = View.VISIBLE
        }

        holder.viewPastRecord.setOnClickListener {
            var appointmentsLive = MentorAppointmentsAccess(context, sharedViewModel).getStudentRecord(studentID = appointments[position].studentID.toString())
            if(appointmentsLive != null){
                appointmentsLive.observe(parentFragment.viewLifecycleOwner){pastAppointments->
                    if(pastAppointments == null || pastAppointments.isEmpty()){
                        Toast.makeText(context,"No past record found",Toast.LENGTH_SHORT).show()
                        appointmentsLive.removeObservers(parentFragment.viewLifecycleOwner)
                    }else{
                        sharedViewModel.pastRecordStudentID = appointments[position].studentID.toString()
                        appointmentsLive.removeObservers(parentFragment.viewLifecycleOwner)
                        parentFragment.findNavController().navigate(R.id.pastRecordFragment)
                    }
                }
            }
        }
        if(appointments[position].expanded){
            holder.reactLayout.visibility = View.GONE
            holder.remarksLayout.visibility = View.VISIBLE
        }else{
            holder.reactLayout.visibility = View.VISIBLE
            holder.remarksLayout.visibility = View.GONE
        }
        holder.submitRemarks.setOnClickListener {
            var remarksInput = holder.remarksInput.text.toString()
            if(remarksInput.isEmpty()){
                holder.remarksInput.error = "Enter remarks"
                holder.remarksInput.requestFocus()
                return@setOnClickListener
            }
            var remarkAppointment = appointments[position]
            remarkAppointment.remarks = remarksInput
            remarkAppointment.status = "Completed"
            remarkAppointment.completed = true
            var remarksLive = MentorAppointmentsAccess(context, sharedViewModel).giveRemarks(remarkAppointment)
            if(remarksLive != null){
                remarksLive.observe(parentFragment.viewLifecycleOwner){remarked->
                    if(remarked){
                        appointments[position] = remarkAppointment
                        notifyItemChanged(position)
                    }
                }
            }
        }

        holder.completeButton.setOnClickListener {
            appointments[position].expanded = !appointments[position].expanded
            notifyItemChanged(position)
        }


    }
}