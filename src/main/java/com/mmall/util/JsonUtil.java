package com.mmall.util;

import com.google.common.collect.Lists;
import com.mmall.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by changlei on 2018/11/22.
 *
 * 对Jackson 的封装，操作json的jsonUtil
 */
@Slf4j
public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 对象的所有字段全部列入
        /*ALWAYS 所有属性都会序列化
        NON_NULL 所有非NULL值的属性 序列化
        NON_DEFAULT 属性值不是默认值的时候才会序列化
        NON_EMPTY  属性不是empty 时序列化，比null更严格*/
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.ALWAYS);

        // 取消默认的timestaps形式（也就是取消毫秒数的形式）
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATE_KEYS_AS_TIMESTAMPS, false);

        // 忽略空 bean 转 json 的错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);

        // 所有日期格式转化为一下格式
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));

        //反序列化
        // false 忽略在json字符串存在但在java不存在对应属性的应对情况，                  防止错误
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    }

    public static <T> String ojb2String(T obj) {
        if (null == obj) {
            return null;
        }else{
            try {
                return obj instanceof String ? (String)obj:objectMapper.writeValueAsString(obj);
            } catch (IOException e) {
                log.error("parse object to error", e);
                return null;
            }
        }
    }

    public static <T> String ojb2StringPretty(T obj) {
        if (null == obj) {
            return null;
        }else{
            try {
                return obj instanceof String ? (String)obj : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            } catch (IOException e) {
                log.error("parse Object to str error", e);
                return null;
            }
        }
    }

    @Deprecated
    public static <T> T string2Obj(String str, Class<T> clazz){
         if(StringUtils.isBlank(str)){
             return null;
         }else{
             try {
                 return String.class.equals(clazz)?(T)str : objectMapper.readValue(str,clazz);
             } catch (IOException e) {
                 log.error("parse Strign to Object error", e);
                 return null;
             }
         }
    }

    /**
     * 反序列化，
     * @param str
     * @param typeReference
     * @param <T> 泛型， 转化后的具体类型
     * @return
     */
    public static <T> T string2Obj(String str, TypeReference<T> typeReference){
        if(StringUtils.isBlank(str) || typeReference == null){
            return null;
        }else{
            try {
                return (T)(String.class.equals(typeReference.getType())?(T)str : objectMapper.readValue(str,typeReference));
            } catch (IOException e) {
                log.error("parse Strign to Object error", e);
                return null;
            }
        }
    }

    /**
     * 反序列化
     * @param str
     * @param collectionClass  集合类class
     * @param elementClass  元素类class
     * @param <T>
     * @return
     */
    public static <T> T string2Obj(String str, Class<?> collectionClass, Class<?> ...elementClass){
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClass);
            try {
                return objectMapper.readValue(str,javaType);
            } catch (IOException e) {
                log.error("parse Strign to Object error", e);
                return null;
            }
    }


    public static void main(String[] args) {
        User u1 = new User();
        u1.setId(1);
        u1.setEmail("changleia@yonyou.com");
        User u2 = new User();
        u2.setId(1);
        u2.setEmail("changleia2@yonyou.com");
        String userJson = JsonUtil.ojb2String(u1);
        String userJosnPretty = JsonUtil.ojb2StringPretty(u1);

        List<User> userList = Lists.newArrayList();
        userList.add(u1);
        userList.add(u2);
        JsonUtil.string2Obj(userJson,User.class);

        System.out.println("userJson:{}"+userJson);
        System.out.println("userJosnPretty"+userJosnPretty);

        String userListStr = JsonUtil.ojb2String(userList);
        System.out.println("userListStr"+userListStr);

      /*  List<User> userObjList = JsonUtil.string2Obj(userListStr, new TypeReference<List<User>>() {
        });*/
        List<User> userObjList = JsonUtil.string2Obj(userListStr, List.class,User.class);

        int a = 0;
    }

}
