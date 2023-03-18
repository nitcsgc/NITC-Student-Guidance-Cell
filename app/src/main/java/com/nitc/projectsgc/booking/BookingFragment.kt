package com.nitc.projectsgc.booking

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.database.*
import com.nitc.projectsgc.ConsultationType
import com.nitc.projectsgc.Mentors
import com.nitc.projectsgc.admin.access.MentorsAccess
import com.nitc.projectsgc.databinding.FragmentBookingBinding

class BookingFragment : Fragment() {
    lateinit var binding : FragmentBookingBinding
    lateinit var mentorType : String
    var mentorNameSelected = "NA"
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        val database : FirebaseDatabase = FirebaseDatabase.getInstance()
        val reference : DatabaseReference = database.reference.child("types")
        var mentorNames = arrayOf<String>()
        var mentors = arrayListOf<Mentors>()

        var mentorTypeSelected = "NA"
        binding = FragmentBookingBinding.inflate(inflater,container,false)
        binding.mentorTypeButtonInBookingFragment.setOnClickListener{
            var mentorTypesLive = context?.let { it1 -> MentorsAccess(it1).getMentorTypes() }
            if (mentorTypesLive != null) {
                mentorTypesLive.observe(viewLifecycleOwner) { mentorTypes ->
                    if(mentorTypes != null) {
                        val mentorTypeBuilder = AlertDialog.Builder(context)
                        mentorTypeBuilder.setTitle("Choose Mentor Type")
                        mentorTypeBuilder.setSingleChoiceItems(
                            mentorTypes.map { it }.toTypedArray(),
                            0
                        ) { dialog, selectedIndex ->
                            mentorTypeSelected = mentorTypes[selectedIndex].toString()
                            dialog.dismiss()
                        }
                        mentorTypeBuilder.setPositiveButton("Go") { dialog, which ->
                            mentorTypeSelected = mentorTypes[0].toString()
                            dialog.dismiss()
                        }
                        mentorTypeBuilder.create().show()
                    }
                }
            }
        }
        binding.mentorNameButtonInBookingFragment.setOnClickListener{
            if(mentorTypeSelected != "NA"){

                reference.addValueEventListener(object:ValueEventListener{


                    override fun onDataChange(snapshot: DataSnapshot) {

                        var mentorNamesSnapshot = snapshot.child("$mentorTypeSelected/mentors").children

                        for(mentor in mentorNamesSnapshot){
                            mentors.add(mentor.getValue(Mentors::class.java)!!)
                        }
                        val mentorNameBuilder = AlertDialog.Builder(context)
                        mentorNameBuilder.setTitle("Choose Mentor Name")
                        mentorNameBuilder.setSingleChoiceItems(mentors.map{it.name}.toTypedArray(),0) { dialog, selectedIndex ->
                            mentorNameSelected = mentors[selectedIndex].name.toString()
                            dialog.dismiss()
                        }
                        mentorNameBuilder.setPositiveButton("Go"){dialog,which->
                            mentorNameSelected = mentors[0].name.toString()
                            dialog.dismiss()
                        }
                        mentorNameBuilder.create().show()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

            }
            else{
                Toast.makeText(context,"Some error occurred",Toast.LENGTH_SHORT).show()
            }

        }


        return binding.root
    }

}