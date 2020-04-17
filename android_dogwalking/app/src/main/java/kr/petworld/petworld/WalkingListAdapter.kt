package kr.petworld.petworld

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import kr.petworld.petworld.databinding.ItemWalkListBinding
import kr.petworld.petworld.db.AppDatabase
import kr.petworld.petworld.model.Walks
import java.text.DecimalFormat
import java.util.*

// 산책하기 목록 adapter
class WalkingListAdapter(private val walksArrayList: ArrayList<Walks>) :
    RecyclerView.Adapter<WalkingListAdapter.WalkingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalkingViewHolder {
        //layout binding
        val itemWalkListBinding =
            ItemWalkListBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return WalkingViewHolder(itemWalkListBinding)
    }

    override fun onBindViewHolder(holder: WalkingViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return walksArrayList.size
    }

    inner class WalkingViewHolder(private val itemWalkListBinding: ItemWalkListBinding) :
        RecyclerView.ViewHolder(itemWalkListBinding.root) {

        fun bind(position: Int) {
            //산책과 list 내용 바인딩
            val walks = walksArrayList[position]
            itemWalkListBinding.dogName.text =
                PreferencesManager.getInstance(itemWalkListBinding.root.context).getValue(
                    String::class.java,
                    PreferencesManager.Key.dogName,
                    ""
                ).toString()
            //산책 날짜
            itemWalkListBinding.dateOfWalkingTextView.text = walks.wDate
            //산책 내용
            itemWalkListBinding.memoTextView.text = walks.memo

            //산책 시간
            val hour = walks.duration / 3600
            val min = (walks.duration - hour * 3600) / 60
            val sec = walks.duration - hour * 3600 - min * 60

            //산책 시간 표시
            itemWalkListBinding.durationOfWalkingTextView.text = (if (hour > 9)
                hour
            else
                "0$hour").toString() + ":" + (if (min > 9)
                min
            else
                "0$min") + ":" +
                    if (sec > 9) sec else "0$sec"


            //산책 click 산책 일기 페이지 이동
            itemWalkListBinding.root.setOnClickListener {
                val intent =
                    Intent(itemWalkListBinding.root.context, DetailsWalkingActivity::class.java)
                intent.putExtra("walk", walks)
                itemWalkListBinding.root.context.startActivity(intent)
            }

            //산책 삭제 click
            itemWalkListBinding.deleteButton.setOnClickListener {
                val builder1 = AlertDialog.Builder(itemWalkListBinding.root.context)
                builder1.setMessage("삭제하시겠습니까?")
                builder1.setCancelable(true)

                builder1.setPositiveButton(
                    "확인"
                ) { dialog, id ->
                    //산책 db에서 삭제
                    AppDatabase.getInstance(itemWalkListBinding.root.context).databaseDao
                        .deleteWalks(walks)
                    walksArrayList.clear()
                    //산책 list db에서 다시 받아오기
                    walksArrayList.addAll(AppDatabase.getInstance(itemWalkListBinding.root.context).databaseDao.waksList)
                    notifyDataSetChanged()

                    dialog.cancel()
                }

                builder1.setNegativeButton(
                    "취소"
                ) { dialog, id -> dialog.cancel() }

                val alert11 = builder1.create()
                alert11.show()
            }


            var newFormat = DecimalFormat("####.###");
            if (walks.distance == null) {
                walks.distance = 0.0
            }
            itemWalkListBinding!!.walkingDistance.text = newFormat.format(walks.distance!!)+" km"

        }

    }
}
