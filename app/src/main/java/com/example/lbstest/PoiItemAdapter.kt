package com.example.lbstest

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.baidu.mapapi.search.core.PoiDetailInfo
import com.baidu.mapapi.search.core.PoiInfo
import com.baidu.mapapi.search.poi.PoiAddrInfo
import com.baidu.mapapi.search.poi.PoiResult
import com.example.lbstest.databinding.ItemSearchResultBinding


class PoiItemAdapter(poiInfos: List<PoiInfo>) : RecyclerView.Adapter<PoiItemAdapter.MyViewHolder>() {

    //通过构造方法获取数据
    private var poiInfos = poiInfos
    /**
     * 创建ViewHolder(条目的界面)并返回
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return MyViewHolder(binding)
    }

    /**
     * ViewHolder容纳了ItemView的实例。
     */
    inner class MyViewHolder(binding: ItemSearchResultBinding) : RecyclerView.ViewHolder(binding.root) {
       val searchResultItemText: TextView = binding.searchResultItemText
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val addr = poiInfos[position]
        holder.searchResultItemText.text = addr.address
        //通过为条目设置点击事件触发回调
        if (listener != null) {
            holder.searchResultItemText.setOnClickListener { listener.onItemClick(position) }
        }
    }

    override fun getItemCount(): Int {
        if(null == poiInfos ) return 0
        return poiInfos.size
    }

    private lateinit var listener: OnItemClickListener
    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }
}
