package com.example.androidlatihanchat_mhusin.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.androidlatihanchat_mhusin.R
import com.example.androidlatihanchat_mhusin.activity.ChatDetailsActivity
import com.example.androidlatihanchat_mhusin.activity.MainActivity
import com.example.androidlatihanchat_mhusin.adapters.ChatListAdapter
import com.example.androidlatihanchat_mhusin.data.ParseFirebaseData
import com.example.androidlatihanchat_mhusin.data.SettingApi
import com.example.androidlatihanchat_mhusin.model.ChatMessage
import com.example.androidlatihanchat_mhusin.utilities.Const
import com.google.firebase.database.*



class ChatsFragment : Fragment() {
    lateinit var recyclerView: RecyclerView

    private var mLayoutManager: LinearLayoutManager? = null
    var mAdapter: ChatListAdapter? = null
    private var progressBar: ProgressBar? = null

    internal lateinit var valueEventListener: ValueEventListener
    internal lateinit var ref: DatabaseReference

    internal lateinit var view: View

    internal lateinit var pfbd: ParseFirebaseData
    internal lateinit var set: SettingApi

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        view = inflater.inflate(R.layout.fragment_chat, container, false)
        pfbd = ParseFirebaseData(context!!)
        set = SettingApi(context!!)

        // activate fragment menu
        setHasOptionsMenu(true)

        recyclerView = view.findViewById(com.example.androidlatihanchat_mhusin.R.id.recyclerView) as RecyclerView
        progressBar = view.findViewById(com.example.androidlatihanchat_mhusin.R.id.progressBar) as ProgressBar

        // use a linear layout manager
        mLayoutManager = LinearLayoutManager(activity)
        recyclerView.layoutManager = mLayoutManager
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(com.example.androidlatihanchat_mhusin.widgets.DividerItemDecoration(activity!!, com.example.androidlatihanchat_mhusin.widgets.DividerItemDecoration.VERTICAL_LIST))


        valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                Log.d(Const.LOG_TAG, "Data changed from fragment")
                if (dataSnapshot.value != null)
                // TODO: 25-05-2017 if number of items is 0 then show something else
                mAdapter = ChatListAdapter(context!!, pfbd.getAllLastMessages(dataSnapshot))
                recyclerView.adapter = mAdapter

                mAdapter?.setOnItemClickListener(object : ChatListAdapter.OnItemClickListener{
                    override fun onItemClick(view: View, obj: ChatMessage, position: Int) {
                        if (obj.receiver.id.equals(set.readSetting(Const.PREF_MY_ID)))
                            ChatDetailsActivity.navigate(
                                activity as MainActivity,
                                view.findViewById(com.example.androidlatihanchat_mhusin.R.id.lyt_parent),
                                obj.sender
                            )
                        else if (obj.sender.id.equals(set.readSetting(Const.PREF_MY_ID)))
                            ChatDetailsActivity.navigate(
                                activity as MainActivity,
                                view.findViewById(com.example.androidlatihanchat_mhusin.R.id.lyt_parent),
                                obj.receiver
                            )
                    }
                })

                bindView()
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        }

        ref = FirebaseDatabase.getInstance().getReference(Const.MESSAGE_CHILD)
        ref.addValueEventListener(valueEventListener)

        return view
    }

    fun bindView() {
        try {
            mAdapter!!.notifyDataSetChanged()
            progressBar!!.visibility = View.GONE
        } catch (e: Exception) {
        }

    }

    override fun onDestroy() {
        //Remove the listener, otherwise it will continue listening in the background
        //We have service to run in the background
        ref.removeEventListener(valueEventListener)
        super.onDestroy()
    }
}