package com.example.lbstest

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.SDKInitializer
import com.baidu.mapapi.common.BaiduMapSDKException
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.example.lbstest.databinding.ActivityMapsBinding


class MapsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapsBinding

    private lateinit var mLocationClient: LocationClient

    private lateinit var baiduMap : BaiduMap

    private var isFirstLocate : Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocationClient.setAgreePrivacy(true)
        mLocationClient = LocationClient(applicationContext)
        mLocationClient.registerLocationListener(MyLocationListener())
        SDKInitializer.setAgreePrivacy(applicationContext,true)
        SDKInitializer.initialize(applicationContext)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        baiduMap = binding.bmapView.map
        baiduMap.isMyLocationEnabled = true
        val permissionList : MutableList<String> = ArrayList<String>()

        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.READ_PHONE_STATE)
        }
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if(permissionList.isNotEmpty()){
            val permissions = permissionList.toTypedArray()
            ActivityCompat.requestPermissions(this,permissions,1);
        }else{
            requestLocation()
        }
    }

    private fun requestLocation(){
        initLocation()
        mLocationClient.start()
    }

    private fun initLocation(){
        val option : LocationClientOption = LocationClientOption()
        option.scanSpan = 5000
        option.setIsNeedAddress(true)
//        option.locationMode = LocationClientOption.LocationMode.Device_Sensors    //传感器模式，只允许GPS定位
        mLocationClient.locOption = option
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            1 -> if(grantResults.size>0){
                for(result in grantResults){
                    if(result != PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(this,"必须同意所有权限才能使用本程序",Toast.LENGTH_SHORT).show()
                        finish()
                        return
                    }
                }
                requestLocation()
            }else{
                Toast.makeText(this,"发生未知错误",Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    inner class MyLocationListener : BDAbstractLocationListener() {
        override fun onReceiveLocation(location: BDLocation?) {
            if (location == null) return
            if(location.locType == BDLocation.TypeNetWorkLocation || location.locType == BDLocation.TypeGpsLocation){
                navigateTo(location)
            }

//            runOnUiThread {
//                val currentPosition = StringBuilder()
//                currentPosition.append("纬度：").append(location.latitude).append("\n")
//                currentPosition.append("经线：").append(location.longitude).append("\n")
//                currentPosition.append("国家：").append(location.country).append("\n")
//                currentPosition.append("省份：").append(location.province).append("\n")
//                currentPosition.append("市：").append(location.city).append("\n")
//                currentPosition.append("区：").append(location.street).append("\n")
//                currentPosition.append("定位方式： ")
//                if (location.locType == BDLocation.TypeGpsLocation) {
//                    currentPosition.append("GPS")
//                } else if (location.locType == BDLocation.TypeNetWorkLocation) {
//                    currentPosition.append("网络")
//                }
//                binding.positionTextView.text = currentPosition
//            }
        }
    }

    private fun navigateTo(location:BDLocation){
        if(isFirstLocate){
            val ll = LatLng(location.latitude,location.longitude)
            var update = MapStatusUpdateFactory.newLatLng(ll)
            baiduMap.animateMapStatus(update)
            update = MapStatusUpdateFactory.zoomTo(16f)
            baiduMap.animateMapStatus(update)
            isFirstLocate = false
        }
        var locationBuilder = MyLocationData.Builder()
        locationBuilder.latitude(location.latitude)
        locationBuilder.longitude(location.longitude)
        var locationData  = locationBuilder.build()
        baiduMap.setMyLocationData(locationData)
    }

    override fun onResume() {
        super.onResume()
        binding.bmapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.bmapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mLocationClient.stop()
        binding.bmapView.onDestroy()
        baiduMap.isMyLocationEnabled = false
    }
}