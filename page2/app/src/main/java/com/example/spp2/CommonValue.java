package com.example.spp2;

import java.util.concurrent.LinkedBlockingQueue;

public class CommonValue {


    private static CommonValue instance = null;

    // 私有构造方法，保证外部无法通过new关键字创建对象
    private CommonValue() {
    }
    private LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<String>();

    // 公共静态方法，返回Singleton实例
    public static CommonValue getInstance() {
        if(instance == null) {
            instance = new CommonValue();
        }
        return instance;
    }
    public  void addData(String data){
        queue.add(data);
    }

    public  LinkedBlockingQueue<String> getData(){
      return queue;
    }
}
