# OrderMatch

This is a trade engine mainly implemented by SpringBoot, Kafka and Flink based on [FIFO matching orders algorithm](https://www.investopedia.com/terms/m/matchingorders.asp "FIFO matching orders algorithm").

## Architecture
![Architecture drawio](https://user-images.githubusercontent.com/11186640/190896350-35eeb4a7-7817-4835-93a0-c8f92e56a87f.png)

## Requirements
1. Install JDK 1.8 or other higher versions.
2. Install and start Zookeeper and Kafka server on port 2181 and 9092 respectively.
3. Install JMeter and Postman for testing.
4. If you encounter any **java.lang.reflect.InaccessibleObjectException**, please add the following vm options before starting the application again.

`--add-opens java.base/java.util=ALL-UNNAMED`

`--add-opens java.base/java.lang=ALL-UNNAMED`

`--add-opens java.base/java.util.concurrent.atomic=ALL-UNNAMED`

`--add-opens java.base/jdk.internal.module=ALL-UNNAMED`

## Testing
1. You can find the [JMeter script here](https://github.com/damncan/OrderMatch/blob/main/scripts/autotest_220918.jmx "JMeter script").
2. Before you start running the auto-testing script, you can define your simulation situation by adjusting the **Thread Properties** first. For example, as following setting, there will be 10 simultaneous http requests lasting for 60 seconds.
   ![JMeter Thread Properties](https://user-images.githubusercontent.com/11186640/190897670-50447167-a436-4e13-ad43-8477a78cfe98.png)
3. After running the script, it will generate several buy/sell side orders and start to match unmatched orders stored in unmatched sell/buy side queues.
4. For example, the following picture shows the trading result after matching 10 orders. After the first 5 orders are matched, the total matched order, amount and quantity on sell side is 1750, 104465 and 1745.
   ![Running Result](https://user-images.githubusercontent.com/11186640/190898105-095d9dae-2307-4899-b796-53ad4536b27a.png)
5. Apart from the console log, you can also use **GET /summary** to query the real time trading result.
   ![GET summary](https://user-images.githubusercontent.com/11186640/190898192-89a6c25d-d7ee-42e1-bc05-22cf0d8735cc.png)
