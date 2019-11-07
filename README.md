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
WS /user/1/feed/1/stream
```

Stream to all feeds posts
```
WS /user/1/feeds/stream
```

