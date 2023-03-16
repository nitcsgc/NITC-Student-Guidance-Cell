package com.nitc.projectsgc.booking

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.database.*
import com.nitc.projectsgc.ConsultationType
import com.nitc.projectsgc.Mentors
import com.nitc.projectsgc.databinding.FragmentBookingBinding

class BookingFragment : Fragment() {
    lateinit var binding : FragmentBookingBinding
    lateinit var mentorType : String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        var database : FirebaseDatabase = FirebaseDatabase.getInstance()
        var reference : DatabaseReference = database.reference.child("types")
        val mentorTypes = arrayListOf<ConsultationType>()
        val mentorNames = mutableMapOf<String,Array<String>>()
        mentorNames["carrier"] = arrayOf<String>("Dr. Ram","Dr. Manish","Dr. Raghu")
        mentorNames["relationship"] = arrayOf<String>("Dr. Ramya","Dr. Manisha","Dr. Sasmita")
        mentorNames["health"] = arrayOf<String>("Dr. Subham","Dr. Lalit","Dr. Malhotra")

        var mentorTypeSelected = "NA"
        binding = FragmentBookingBinding.inflate(inflater,container,false)
        binding.mentorTypeButtonInBookingFragment.setOnClickListener{
            reference.addValueEventListener(object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(typeChild in snapshot.children){
                        var mentors = typeChild.child("mentors").getValue(object : GenericTypeIndicator<ArrayList<Mentors>>(){})
                        mentorTypes.add(ConsultationType(
                            type = typeChild.key,
                            mentors = mentors
                        ))
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
            val mentorTypeBuilder = AlertDialog.Builder(context)
            mentorTypeBuilder.setTitle("Choose Mentor Type")
            mentorTypeBuilder.setSingleChoiceItems(mentorTypes,0) { dialog, selectedIndex ->
                    mentorTypeSelected = mentorTypes[selectedIndex].toString()
                    dialog.dismiss()
            }
            mentorTypeBuilder.setPositiveButton("Go"){dialog,which->
                    mentorTypeSelected = mentorTypes[0].toString()
                    dialog.dismiss()
                }
            mentorTypeBuilder.create().show()

        }
        binding.mentorNameButtonInBookingFragment.setOnClickListener{
            val mentorNameBuilder = AlertDialog.Builder(context)
            mentorNameBuilder.setTitle("Choose Mentor Name")
            mentorNameBuilder.setSingleChoiceItems(mentorNames[mentorTypeSelected],0) { dialog, selectedIndex ->
                    mentorNameSelected = mentorNames[mentorTypeSelected]!![selectedIndex]
                    dialog.dismiss()
            }
            mentorNameBuilder.setPositiveButton("Go"){dialog,which->
                    mentorNameSelected = mentorNames[mentorTypeSelected]!![0]
                    dialog.dismiss()
                }
            mentorNameBuilder.create().show()

        }
        return binding.root
    }

}