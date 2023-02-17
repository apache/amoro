package com.netease.arctic.ams.server.utils;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @Author alex.wang
 * @Date 2023/2/17 17:11
 * @PackageName: com.netease.arctic.ams.server.utils
 * @Version 1.0
 */
public class JacksonUtilsTest {

    public static class Student{

        public Student(){
            //do nothing
        }
        private String name;
        private Integer age;
        private Long timeStamp;
        private Boolean chineseStudent;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public Long getTimeStamp() {
            return timeStamp;
        }

        public void setTimeStamp(Long timeStamp) {
            this.timeStamp = timeStamp;
        }

        public Boolean getChineseStudent() {
            return chineseStudent;
        }

        public void setChineseStudent(Boolean chineseStudent) {
            this.chineseStudent = chineseStudent;
        }

        public String toString(){
            return this.name + this.age + this.chineseStudent + this.timeStamp;
        }
    }

    @Test
    public void testJacksonUtils() throws Exception{
        Student wangxiao = new Student();
        wangxiao.setAge(20);
        wangxiao.setName("WangXiao");
        wangxiao.setChineseStudent(true);
        wangxiao.setTimeStamp(System.currentTimeMillis());

        Student alex = new Student();
        alex.setTimeStamp(System.currentTimeMillis());
        alex.setChineseStudent(false);
        alex.setName("Alex Mercer");
        alex.setAge(21);
        System.out.print(JacksonUtils.toIndentJSON(wangxiao));
        System.out.print(JacksonUtils.toIndentJSON(alex));
        System.out.print(JacksonUtils.toJSONString(wangxiao));
        System.out.print(JacksonUtils.toJSONString(alex));

        Student wx = JacksonUtils.parseObject(JacksonUtils.toJSONString(wangxiao), Student.class);
        System.out.print(wx.toString());
        Student mercer = JacksonUtils.parseObject(JacksonUtils.toJSONString(alex), Student.class);
        System.out.print(mercer.toString());


    }
}
