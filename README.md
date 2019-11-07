## Example groups app

### Running application

To run this sample, type: 

```bash
sbt run
``` 


This command will run 3 process in a single jvm and will simulate 2 seed nodes and one additional node with random port assigned to it. 

### Running application

Web server is open on http://localhost:8080

#### REST API

Joining group:
```
POST /user/1/join/1
{
  "userName": "Kamil"
}
```

Listing user groups:
```
GET /user/1/groups
```

Posting in group:
```
POST /user/1/post/1
{
  "msg": "Text to send"
}
```

Get group feed last 20 posts:
```
GET /user/1/feed/1
```

Get all feeds last 20 posts:
```
GET /user/1/feeds
```

#### WEB SOCKET

Stream to feed posts
```
GET /user/1/feed/1/stream
```

Stream to all feeds posts
```
GET /user/1/feeds/stream
```

### Large volume handling

In term of large volume handling we can change in memory database to ex. Cassandra which can be cluster easily, increase number of nodes in the cluster.
There is no need to return all feed data to the http service. I think only latest posts are necessary and oldest ones we can load progressively.
On the client side we can keep track of oldest messages for each feed and load older ones if necessary. There are methods in FeedRepository to handle that. 
We can even keep track of latest messages in feeds by keeping them in the group actors and use only database to fetch older ones.
In this scenario we can keep fixed memory use for each group.
Table for feed messages can be divided per group so each group would have separate table to increase performance of fetching old data as well as indexing etc.


### At most one delivery
Probably some changes should be provided to avoid messages lost using distributed PubSub, probably own subscribing model as well as acking and routing.
   
