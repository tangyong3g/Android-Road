package com.sny.tangyong.basic;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class NetUtils {

    /** Current network is EVDO revision 0 */
    private int NETWORK_TYPE_EVDO_0 = 5;
    /** Current network is EVDO revision A */
    private int NETWORK_TYPE_EVDO_A = 6;
    /** Current network is HSDPA */
    private int NETWORK_TYPE_HSDPA = 8;
    /** Current network is HSUPA */
    private int NETWORK_TYPE_HSUPA = 9;
    /** Current network is HSPA */
    private int NETWORK_TYPE_HSPA = 10;

    /**
     * 枚举网络状态 NET_NO：没有网络 NET_2G:2g网络 NET_3G：3g网络 NET_4G：4g网络 NET_WIFI：wifi NET_UNKNOWN：未知网络
     */
    public static enum NetState {
        NET_NO, NET_2G, NET_3G, NET_4G, NET_WIFI, NET_UNKNOWN
    };

    public static String getConnectType(Context context) {
        NetState netState = getConnectInfo(context);
        String result = null;
        switch (netState) {
            case NET_NO:
                result = "NO_NET";
                break;
            case NET_2G:
                result = "2G";
                break;
            case NET_3G:
                result = "3G";
                break;
            case NET_4G:
                result = "4G";
                break;
            case NET_WIFI:
                result = "WIFI";
                break;
            case NET_UNKNOWN:
                result = "UNKNOW";
                break;
        }

        return result;
    }

    /**
     * 获得网络连接类型
     *
     */
    private static NetState getConnectInfo(Context context) {
        NetState stateCode = NetState.NET_NO;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnectedOrConnecting()) {
            switch (ni.getType()) {
                case ConnectivityManager.TYPE_WIFI:
                    stateCode = NetState.NET_WIFI;
                    break;
                case ConnectivityManager.TYPE_MOBILE:
                    switch (ni.getSubtype()) {
                        case TelephonyManager.NETWORK_TYPE_GPRS: // 联通2g
                        case TelephonyManager.NETWORK_TYPE_CDMA: // 电信2g
                        case TelephonyManager.NETWORK_TYPE_EDGE: // 移动2g
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            stateCode = NetState.NET_2G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_EVDO_A: // 电信3g
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                            stateCode = NetState.NET_3G;
                            break;
                        case TelephonyManager.NETWORK_TYPE_LTE:
                            stateCode = NetState.NET_4G;
                            break;
                        default:
                            stateCode = NetState.NET_UNKNOWN;
                    }
                    break;
                default:
                    stateCode = NetState.NET_UNKNOWN;
            }

        }
        return stateCode;
    }

    /**
     * 区分2g，3g网络
     *
     */
    private String solveType(int subtype) {
        if (subtype == TelephonyManager.NETWORK_TYPE_UMTS || subtype == NETWORK_TYPE_HSDPA
                || subtype == NETWORK_TYPE_HSUPA || subtype == NETWORK_TYPE_HSPA || subtype == NETWORK_TYPE_EVDO_0
                || subtype == NETWORK_TYPE_EVDO_A) {
            return "3G";
        } else {
            return "2G";
        }

    }

    /**
     * WIFI是否可用
     * 
     * @param context
     * @return
     */
    public static boolean isWifiEnable(Context context) {
        return getConnectInfo(context) == NetState.NET_WIFI;
    }

    /**
     * 网络是否可用
     * 
     * @param context
     * @return
     */
    public static boolean isNetworkEnable(Context context) {
        NetState connectInfo = getConnectInfo(context);
        return connectInfo == NetState.NET_WIFI || connectInfo == NetState.NET_2G || connectInfo == NetState.NET_3G
                || connectInfo == NetState.NET_4G;
    }
}
