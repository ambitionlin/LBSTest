package com.example.lbstest

import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.widget.Adapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.poi.*

class PoiCitySearchActivity : AppCompatActivity() {

//    private lateinit var mPoiItemAdapter:PoiItemAdapter
//    private lateinit var mRecyclerView:RecyclerView
//
//    override fun onGetPoiResult(poiResult : PoiResult?) {
//        if(poiResult == null || poiResult.error == SearchResult.ERRORNO.KEY_ERROR){
//            Toast.makeText(this, "未找到结果", Toast.LENGTH_SHORT).show()
//            return
//        }
//        // 隐藏之前的
//        hidePoiInfoLayout()
//
//        mRecyclerView.visibility = View.VISIBLE
//
//        if(null == mPoiItemAdapter){
//            mPoiItemAdapter = PoiItemAdapter(poiResult)
//        }else{
//            mPoiItemAdapter.updateData(poiResult)
//        }
//    }
//
//    private fun hidePoiInfoLayout() {
//
//    }
//
//    override fun onGetPoiDetailResult(p0: PoiDetailResult?) {
//
//    }
//
//    override fun onGetPoiDetailResult(p0: PoiDetailSearchResult?) {
//
//    }
//
//    override fun onGetPoiIndoorResult(p0: PoiIndoorResult?) {
//
//    }

}