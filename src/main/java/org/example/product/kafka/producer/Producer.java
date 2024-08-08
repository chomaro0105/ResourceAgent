package org.example.product.kafka.producer;


import lombok.Data;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.properties.PropertiesLoader;
import org.example.string.StringMapper;

@Data
public class Producer {

    PropertiesLoader propertiesLoader = new PropertiesLoader();

    public Producer(){

    }

    public void running(String topic, String message){
        ProducerHandler producerHandler = new ProducerHandler(StringMapper.SYNC);
        producerHandler.setProducerConfig(propertiesLoader.getProperties().getValue("kafka","bootstrap.servers"),
                "org.apache.kafka.common.serialization.StringSerializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        producerHandler.pushMessageSync(topic, message);



//        producerHandler.changeType(StringMapper.ASYNC);
//        ProducerRecord<String, String> record = new ProducerRecord<>("gon-basic01", "asdfasdfasdfasdfasdfasdfasdfasdfa"); //ProducerRecord 오브젝트를 생성합니다.
//        producerHandler.pushMessageAsync(record, new ProducerGonCallback(record));

        producerHandler.close();
    }

    public void running(String topic, String key, String message){
        ProducerHandler producerHandler = new ProducerHandler(StringMapper.SYNC);
        producerHandler.setProducerConfig(propertiesLoader.getProperties().getValue("kafka","bootstrap.servers"),
                "org.apache.kafka.common.serialization.StringSerializer",
                "org.apache.kafka.common.serialization.StringSerializer");
        producerHandler.pushMessageSync(topic, key, message);



//        producerHandler.changeType(StringMapper.ASYNC);
//        ProducerRecord<String, String> record = new ProducerRecord<>("gon-basic01", "asdfasdfasdfasdfasdfasdfasdfasdfa"); //ProducerRecord 오브젝트를 생성합니다.
//        producerHandler.pushMessageAsync(record, new ProducerGonCallback(record));

        producerHandler.close();
    }

}
