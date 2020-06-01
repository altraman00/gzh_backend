package com.ruoyi.framework.weixin;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ruoyi.common.utils.http.HttpClient;
import com.ruoyi.framework.web.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Slf4j
public class WeixinUtil {

    /**
     * 根据access_token获取分享的签名
     *
     * @param access_token
     * @param url
     * @return
     * @throws IOException
     */
    public static Map<String, String> getShareSignByAccessToken(String access_token, String url) throws IOException {
        String jsapiTicket = getJsapiTicketByAccessToken(access_token);
        Map<String, String> ret = getShareSignByJsapiTicket(jsapiTicket, url);
        return ret;
    }


    /**
     * 获取分享链接的签名
     *
     * @param jsapi_ticket
     * @param url
     * @return
     */
    public static Map<String, String> getShareSignByJsapiTicket(String jsapi_ticket, String url) {
        Map<String, String> ret = new HashMap<String, String>();
        String nonce_str = create_nonce_str();
        String timestamp = create_timestamp();
        String signature = "";
        url = URLDecoder.decode(url);

        //注意这里参数名必须全部小写，且必须有序
        String string1 = "jsapi_ticket=" + jsapi_ticket +
                "&noncestr=" + nonce_str +
                "&timestamp=" + timestamp +
                "&url=" + url;

        log.info("【WeixinUtilService】getShareSignByJsapiTicket,string1:{}", string1);

        try {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(string1.getBytes("UTF-8"));
            signature = byteToHex(crypt.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        ret.put("url", url);
        ret.put("nonceStr", nonce_str);
        ret.put("timestamp", timestamp);
        ret.put("signature", signature);
        return ret;
    }


    /**
     * 通过code获取JsapiTicket
     *
     * @param access_token
     * @return
     * @throws IOException
     */
    public static String getJsapiTicketByAccessToken(String access_token) throws IOException {

        String jsapi_ticket_url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=%s&type=jsapi";
        jsapi_ticket_url = String.format(jsapi_ticket_url, access_token, access_token);

        String access_token_result = HttpClient.doGet(jsapi_ticket_url);
        log.info("【WeixinUtilService】access_token_result:{}", access_token_result);

        JSONObject jsonObject = JSONUtil.parseObj(access_token_result);

        Integer errcode = jsonObject.get("errcode") == null ? null : Integer.valueOf(jsonObject.get("errcode").toString());
        String errmsg = jsonObject.get("errmsg") == null ? null : jsonObject.get("errmsg").toString();
        if (errcode != null && errcode != 0) {
            throw new GlobalException(errcode, errmsg);
        }

        String jsapi_ticket = jsonObject.get("ticket") == null ? null : jsonObject.get("ticket").toString();
        log.info("【WeixinUtilService】jsapi_ticket:{}", jsapi_ticket);
        return jsapi_ticket;
    }

    private static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    private static String create_nonce_str() {
        return UUID.randomUUID().toString();
    }

    private static String create_timestamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }


}
