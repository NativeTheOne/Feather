package com.feather;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

class User{
    private String name;
    private String sex;
    private int age;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getSex() {
        return sex;
    }
    public void setSex(String sex) {
        this.sex = sex;
    }
    public int getAge() {
        return age;
    }
    public void setAge(int age) {
        this.age = age;
    }
    public User( String name,String sex,int age){
        this.name=name;
        this.sex=sex;
        this.age=age;
    }
}

public class DumpOOM {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = new Thread(()->{
            List<User> persons = new ArrayList<>();
            try{
                while(true){
                    persons.add(new User("feather","male",25));
                }
            }catch (Throwable t){
                System.out.println("a");
            }
            Integer integer = new Integer(10);
        });
        System.out.println("b");
        TimeUnit.SECONDS.sleep(5);
        Integer integer = new Integer(20);
        System.out.println("faker");
    }
}
