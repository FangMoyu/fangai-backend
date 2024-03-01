package com.yupi.springbootinit.mq;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class mulConsumer {
    // 队列名称和要接收的队列名称一致
    private final static String QUEUE_NAME = "hello";
    public static void main(String[] args) throws Exception {
        // 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        // 设置 RabbitMQ 的 Host
        factory.setHost("localhost");
        // 创建连接
        Connection connection = factory.newConnection();
        // 创建通道
        for(int i = 0; i < 2; i++){
            Channel channel = connection.createChannel();
            // 让消费者消费完一个消息后才能消费下一个，也就是消费者只有消费完当前的消息，才能接收下一个消息
            channel.basicQos(1);
            // 声明队列,要和发布者的队列声明一致。
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            // 接收消息，这其实相当于开辟了新线程去执行 deliverCallback 的处理
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                // 测试多消费者情况，加入休眠,模拟消费任务繁忙的情况
                try {
                    String message = new String(delivery.getBody(), "UTF-8");
                    System.out.println(" [x] Received '" + message + "'");
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // ack 消息
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };
            /**
             * 参数表示
             * 1. 队列名称
             * 2. 是否自动 ACK
             * 3. 消息回调参数，指定了接收到的消息的具体执行方案
             * 4， 空的一个 Lambda 表达式
             */
            channel.basicConsume(QUEUE_NAME, false, deliverCallback,  consumerTag -> { });
        }
    }
}

