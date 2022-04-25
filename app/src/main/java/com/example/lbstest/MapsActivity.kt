package com.example.lbstest

import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MyLocationConfiguration
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.PoiDetailInfo
import com.baidu.mapapi.search.core.PoiInfo
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.poi.*
import com.example.lbstest.databinding.ActivityMapsBinding


import kotlin.math.abs


class MapsActivity : AppCompatActivity(), SensorEventListener, View.OnClickListener {
    private val instance by lazy { this } //这里用到了委托，只有使用到了instance才会执行该行代码
    private lateinit var binding: ActivityMapsBinding
    private lateinit var inputText: EditText
    private lateinit var buttonDelete: ImageView

    private lateinit var mLocationClient: LocationClient
    private lateinit var mSensorManager: SensorManager
    private lateinit var mPoiSearch: PoiSearch

    private lateinit var baiduMap: BaiduMap

    private var isFirstLocate: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LocationClient.setAgreePrivacy(true)
        //后台持续定位
        mLocationClient = LocationClient(applicationContext)
        mLocationClient.registerLocationListener(MyLocationListener())
        SDKInitializer.setAgreePrivacy(applicationContext, true)
        SDKInitializer.initialize(applicationContext)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)  //activity设置无titleBar

        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)   //设置LinearLayoutManager

        inputText = binding.searchEditText
        inputText.addTextChangedListener(MyTextWatcher())
        buttonDelete = binding.searchEditDelete
        buttonDelete.setOnClickListener(this)

        //检索位置
        mPoiSearch = PoiSearch.newInstance()
        mPoiSearch.setOnGetPoiSearchResultListener(MyOnGetPoiSearchResultListener())

        baiduMap = binding.bMapView.map
        baiduMap.isMyLocationEnabled = true

        val myLocationConfiguration =
            MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, true, null)
        baiduMap.setMyLocationConfiguration(myLocationConfiguration)
        // 获取传感器管理服务
        mSensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        // 为系统的方向传感器注册监听器
        mSensorManager.registerListener(
            this,
            mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
            SensorManager.SENSOR_DELAY_UI
        )

        val permissionList: MutableList<String> = ArrayList<String>()

        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionList.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionList.add(android.Manifest.permission.READ_PHONE_STATE)
        }
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionList.isNotEmpty()) {
            val permissions = permissionList.toTypedArray()
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else {
            requestLocation()
        }
    }

    private fun requestLocation() {
        initLocation()
        mLocationClient.start()
    }

    private fun initLocation() {
        val option: LocationClientOption = LocationClientOption()
        option.setIsNeedAddress(true)
        option.locationMode = LocationClientOption.LocationMode.Hight_Accuracy
        option.setCoorType("bd0911")
        option.setNeedDeviceDirect(true)
        option.setIsNeedLocationDescribe(true)
        option.scanSpan = 3000
        mLocationClient.locOption = option
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> if (grantResults.size > 0) {
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show()
                        finish()
                        return
                    }
                }
                requestLocation()
            } else {
                Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    inner class MyLocationListener : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            if (location == null) return
            if (location.locType == BDLocation.TypeNetWorkLocation || location.locType == BDLocation.TypeGpsLocation) {
                navigateTo(location)
            }
        }
    }

    private var mLocation: BDLocation? = null
    private fun navigateTo(location: BDLocation?) {
        if (location == null) return
        mLocation = location      //mLocation更新需要在此处，可不断刷新mLocation的值
        if (isFirstLocate) {
            val ll = LatLng(location.latitude, location.longitude)
            var update = MapStatusUpdateFactory.newLatLng(ll)
            baiduMap.animateMapStatus(update)    //定位到地图该位置
            update = MapStatusUpdateFactory.zoomTo(16f)
            baiduMap.animateMapStatus(update)
            isFirstLocate = false
        }
        var locationBuilder = MyLocationData.Builder()
        locationBuilder.direction(mCurrentDirection)
        locationBuilder.latitude(location.latitude)
        locationBuilder.longitude(location.longitude)
        var locationData = locationBuilder.build()
        baiduMap.setMyLocationData(locationData)
    }

    override fun onResume() {
        super.onResume()
        binding.bMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.bMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mLocationClient.stop()
        mPoiSearch.destroy()
        binding.bMapView.onDestroy()
        baiduMap.isMyLocationEnabled = false
    }

    /**
     * 传感器方向信息回调
     */
    private var lastX = 0.0f
    private var mCurrentDirection = lastX
    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val x = event.values[SensorManager.DATA_X]
        if (abs(x - lastX) > 1.0) {
            mCurrentDirection = x
            //构造定位图层数据
            navigateTo(mLocation)
        }
        lastX = x
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    //删除按钮
    override fun onClick(v: View?) {
        inputText.text.clear()
        buttonDelete.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
    }

    inner class MyTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (null == s || s.isEmpty()) {
                buttonDelete.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
            } else {
                buttonDelete.visibility = View.VISIBLE
                doSearchQuery()
            }
        }

        override fun afterTextChanged(s: Editable?) {

        }
    }

    private lateinit var mKeyword: String
    private fun doSearchQuery() {

        mKeyword = inputText.text.toString().trim()
        mPoiSearch.searchInCity(
            PoiCitySearchOption().city("上海").keyword(mKeyword).pageNum(0).cityLimit(false).scope(2)
        )
    }

    private var mPoiItemAdapter: PoiItemAdapter ?=null

    inner class MyOnGetPoiSearchResultListener : OnGetPoiSearchResultListener {
        override fun onGetPoiResult(poiResult: PoiResult?) {
            if (poiResult == null || null == poiResult.allPoi || poiResult.error == SearchResult.ERRORNO.KEY_ERROR) {
                Toast.makeText(instance, "未找到poiResult结果", Toast.LENGTH_SHORT).show()
                binding.recyclerView.visibility = View.GONE
                return
            }
            val poiInfos: MutableList<PoiInfo> = poiResult.allPoi
            binding.recyclerView.visibility = View.VISIBLE
            if(mPoiItemAdapter!=null) mPoiItemAdapter = null
            mPoiItemAdapter = PoiItemAdapter(poiInfos)
            //点击搜索出来的结果时，定位到搜索位置
            mPoiItemAdapter!!.setOnItemClickListener(object: PoiItemAdapter.OnItemClickListener{
                override fun onItemClick(position: Int) {
                    val clickPoiInfo = poiInfos[position]
                    val latLng = clickPoiInfo.location
                    val update = MapStatusUpdateFactory.newLatLng(latLng)
                    baiduMap.animateMapStatus(update)
                    binding.recyclerView.visibility = View.GONE

                }
            })
            binding.recyclerView.adapter = mPoiItemAdapter
            //添加自定义分割线
            val divider = DividerItemDecoration(this@MapsActivity,DividerItemDecoration.VERTICAL)
            ContextCompat.getDrawable(this@MapsActivity,R.drawable.custom_divider)
                ?.let { divider.setDrawable(it) }
            binding.recyclerView.addItemDecoration(divider)
        }

        override fun onGetPoiDetailResult(p0: PoiDetailResult?) {

        }

        override fun onGetPoiDetailResult(poiDetailSearchResult: PoiDetailSearchResult?) {
            if (poiDetailSearchResult == null || null == poiDetailSearchResult.poiDetailInfoList || poiDetailSearchResult.error == SearchResult.ERRORNO.KEY_ERROR) {
                Toast.makeText(instance, "未找到poiDetailSearchResult结果", Toast.LENGTH_SHORT).show()
                binding.recyclerView.visibility = View.GONE
                return
            }

        }

        override fun onGetPoiIndoorResult(p0: PoiIndoorResult?) {

        }

    }
}


