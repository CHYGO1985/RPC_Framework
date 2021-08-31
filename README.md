# RPC_Framework

This is a RPC framework based on SpringBoot framework and ZooKeeper. The project is for you to know me via a real project:  
1) my understanding of Java: Java SE, Netty, SpringBoot 
2) my understanding of Software Engineering (How will I organize a software project): Clean Code, design patterns, OOP  
3) some distributed system knowledge: ZooKeeper, RPC, LoadBlancer etc
 
The basic workflow is as shown below:

Service provider (Server) <-----register service-----> ZooKeeper <------invoke service ------> Service Consumer (Client)

## Communication between server and client
Based on Netty (Java NIO). I implemeneted customized encoder and decoder based on the self-defined request and response objects.

## Service Register Center
The serivces will be registered on ZooKeeper. 

## Load Balancer
I added a random based loadbalancer to ZooKeeper, hence, the client will receive response from one of the servers randomly.

# Demo
Please find the demo from the below YouTube link:
https://www.youtube.com/watch?v=d64ex38jtWY&ab_channel=JingjieJianghttps://www.youtube.com/watch?v=d64ex38jtWY&ab_channel=JingjieJiang

0:00 ~ 3:40: project structure explanation
3:40: demo





