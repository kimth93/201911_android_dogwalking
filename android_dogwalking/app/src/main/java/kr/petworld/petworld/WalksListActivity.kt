package kr.petworld.petworld

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import kr.petworld.petworld.databinding.ActivityWalkListBinding
import kr.petworld.petworld.db.AppDatabase
import kr.petworld.petworld.model.Walks


// 산책하기
class WalksListActivity : AppCompatActivity() {


    //  산책하기 페이지 layout binding
    lateinit var activityWalkListBinding: ActivityWalkListBinding
    //  산책하기 목록 adapter
    lateinit var walkinListAdapter: WalkingListAdapter
    var walkArrayList = ArrayList<Walks>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //layout binding
        activityWalkListBinding = DataBindingUtil.setContentView(this, R.layout.activity_walk_list)
        activityWalkListBinding.executePendingBindings()

        //db에 산책 list 받는 곳
        walkArrayList.addAll(AppDatabase.getInstance(this).databaseDao.waksList)
        //산책 list를 adapter에 보내는 곳
        walkinListAdapter = WalkingListAdapter(walkArrayList);
        //산책 일기 출력 방향
        activityWalkListBinding.walkListRecyclerView.layoutManager = LinearLayoutManager(this)
        //산책 일기 adapter link
        activityWalkListBinding.walkListRecyclerView.adapter=walkinListAdapter


    }
}