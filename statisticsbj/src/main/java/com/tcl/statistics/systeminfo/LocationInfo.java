package com.tcl.statistics.systeminfo;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;

import com.tcl.statistics.agent.StatisticsConfig;
import com.tcl.statistics.util.LogUtils;
import com.tcl.statistics.util.PermissionUtil;


/**
 * 位置信息
 */
public class LocationInfo {

    /**
     * 获取GPS位置
     *
     * @param context
     * @return
     */
    public static String getGPSLoc(Context context) {
        String str = "";
        try {
            if (PermissionUtil.hanPermission(context,
                    "android.permission.ACCESS_FINE_LOCATION")) {
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                LogUtils.D("gps location: " + loc);
                if (loc != null) {
                    str = String.format(
                            "%s_%s_%s",
                            new Object[]{Long.valueOf(loc.getTime()),
                                    Double.valueOf(loc.getLongitude()),
                                    Double.valueOf(loc.getLatitude())});
                    return str;
                }
            }
        } catch (SecurityException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return str;
    }

    public static String getLoc(Context context) {
        String noFindAddress = String.format(
                "%s_%s_%s",
                new Object[]{Integer.valueOf(0), Integer.valueOf(0),
                        Integer.valueOf(0)});
        try {
            if (PermissionUtil.hanPermission(context,
                    "android.permission.ACCESS_COARSE_LOCATION")) {

                if (TextUtils.isEmpty(StatisticsConfig.getLocationinfo(context))) {
                    return requestLocation(context);
                } else {
                    String locationinfo = StatisticsConfig
                            .getLocationinfo(context);
                    String[] loc = locationinfo.split("_");
                    if (loc != null && loc.length > 1) {
                        return String.format("%s_%s_%s", new Object[]{loc[0],
                                loc[1], 0});
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return (String) noFindAddress;
    }

    private static String requestLocation(final Context context) {
        String noFindAddress = String.format("%s_%s_%s", new Object[]{Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0)});
        final LocationManager locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            final LocationListener locationListener = new LocationListener() {

                // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
                @Override
                public void onStatusChanged(String provider, int status,
                                            Bundle extras) {

                }

                // Provider被enable时触发此函数，比如GPS被打开
                @Override
                public void onProviderEnabled(String provider) {

                }

                // Provider被disable时触发此函数，比如GPS被关闭
                @Override
                public void onProviderDisabled(String provider) {

                }

                // 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        LogUtils.D("get new location:" + "Lat: "
                                + location.getLatitude() + " Lng: "
                                + location.getLongitude());
                        if (TextUtils.isEmpty(StatisticsConfig
                                .getLocationinfo(context))) {
                            String loc = location.getLatitude() + "_"
                                    + location.getLongitude();
                            StatisticsConfig.saveLocationinfo(context, loc);
                        }
                        try {
                            locationManager.removeUpdates(this);
                        } catch (SecurityException se) {
                            se.printStackTrace();
                        }

                    }
                }

            };
            Location location = null;
            try {
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            } catch (SecurityException se) {
                se.printStackTrace();
            }
            if (location != null) {
                double latitude = location.getLatitude(); // 经度
                double longitude = location.getLongitude(); // 纬度
                LogUtils.I("get save location :latitude:" + latitude
                        + ",longitude:" + longitude);
                if (TextUtils
                        .isEmpty(StatisticsConfig.getLocationinfo(context))) {
                    String loc = location.getLatitude() + "_"
                            + location.getLongitude();
                    StatisticsConfig.saveLocationinfo(context, loc);
                    return loc;
                } else {
                    return StatisticsConfig.getLocationinfo(context);
                }
            } else {
                try {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                } catch (SecurityException security) {
                    security.printStackTrace();
                }
            }
        }
        return noFindAddress;
    }
}
