package com.ruoyi.project.weixin.utils;

import com.ruoyi.project.weixin.dto.WxMpXmlMessageDTO;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @Classname FatherToChildUtils
 * @Description
 * @Date 2020-04-20 10:49
 * @Created by pjz
 */
public class FatherToChildUtils {
    /*
     * 将父类所有的属性COPY到子类中。
     * 类定义中child一定要extends father；
     * 而且child和father一定为严格javabean写法，属性为deleteDate，方法为getDeleteDate
     */
    public static void fatherToChild (Object father,Object child){
        if(!(child.getClass().getSuperclass()==father.getClass())){
            System.err.println("child不是father的子类");
        }
        Class fatherClass= father.getClass();
        Field declaredFields[]= fatherClass.getDeclaredFields();
        for(int i=0;i<declaredFields.length;i++){
            Field field=declaredFields[i];
            try {
                Method method=fatherClass.getDeclaredMethod("get"+upperHeadChar(field.getName()));
                Object obj = method.invoke(father);
                field.setAccessible(true);
                field.set(child,obj);
            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    /**
     * 首字母大写，in:deleteDate，out:DeleteDate
     */
    private static String upperHeadChar(String in){
        String head=in.substring(0,1);
        String out=head.toUpperCase()+in.substring(1,in.length());
        return out;
    }

    public static void main(String[] args) {
        WxMpXmlMessage wxMpXmlMessage = new WxMpXmlMessage();
        wxMpXmlMessage.setFromUser("123");
        wxMpXmlMessage.setContent("123321");
        WxMpXmlMessageDTO wxMpXmlMessageDTO = new WxMpXmlMessageDTO();
        fatherToChild(wxMpXmlMessage, wxMpXmlMessageDTO);
        System.out.println(wxMpXmlMessageDTO.toString());
    }
}
